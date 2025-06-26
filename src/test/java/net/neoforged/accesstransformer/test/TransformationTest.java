package net.neoforged.accesstransformer.test;

import net.neoforged.accesstransformer.api.AccessTransformerEngine;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransformationTest {
    @Test
    public void testMemberTransformations() throws Throwable {
        try (var cl = setupTest(
                "ATTestClass.java",
                """
                        package net.neoforged.accesstransformer.testJar;

                        public class ATTestClass {
                            private final String finalPrivateField = "EMPTY";
                            private String privateField = "EMPTY";

                            private void privateMethod() {
                            }

                            public void otherMethod() {
                                privateMethod();
                            }
                        }

                        """, """
                        public-f net.neoforged.accesstransformer.testJar.ATTestClass finalPrivateField
                        protected net.neoforged.accesstransformer.testJar.ATTestClass privateField

                        public net.neoforged.accesstransformer.testJar.ATTestClass privateMethod()V
                        """
        )) {
            var transformedClass = Class.forName("net.neoforged.accesstransformer.testJar.ATTestClass", true, cl);
            assertTrue(Modifier.isProtected(transformedClass.getDeclaredField("privateField").getModifiers()), "public field");

            assertTrue(Modifier.isPublic(transformedClass.getDeclaredField("finalPrivateField").getModifiers()), "public field");
            assertFalse(Modifier.isFinal(transformedClass.getDeclaredField("finalPrivateField").getModifiers()), "nonfinal field");

            assertTrue(Modifier.isPublic(transformedClass.getDeclaredMethod("privateMethod").getModifiers()), "nonfinal method");
        }
    }

    @Test
    public void testClassTransformation() throws Throwable {
        try (var cl = setupTest(
                "DefaultClass.java",
                """
                        package net.neoforged.accesstransformer.testJar;

                        class DefaultClass {

                            static class Inner {
                            }

                        }
                        """,
                """
                        public net.neoforged.accesstransformer.testJar.DefaultClass
                        public net.neoforged.accesstransformer.testJar.DefaultClass$Inner
                        """
        )) {
            var outer = Class.forName("net.neoforged.accesstransformer.testJar.DefaultClass", true, cl);
            var inner = Class.forName("net.neoforged.accesstransformer.testJar.DefaultClass$Inner", true, cl);

            assertTrue(Modifier.isPublic(outer.getModifiers()), "public class");
            assertTrue(Modifier.isPublic(inner.getModifiers()), "public inner class");
        }
    }

    /**
     * Validate that the INNERCLASS metadata is correctly updated on the outer class.
     */
    @Test
    public void testInnerClassModifierChangedOnParent() throws Throwable {
        try (var cl = setupTest(
                "OuterClass.java",
                """
                        package net.neoforged.accesstransformer.testJar;

                        class OuterClass {

                            static class Inner {
                            }

                        }
                        """,
                """
                        public net.neoforged.accesstransformer.testJar.OuterClass$Inner
                        """
        )) {
            var outer = Class.forName("net.neoforged.accesstransformer.testJar.OuterClass", true, cl);
            var inner = Class.forName("net.neoforged.accesstransformer.testJar.OuterClass$Inner", true, cl);

            assertFalse(Modifier.isPublic(outer.getModifiers()) || Modifier.isPrivate(outer.getModifiers()), "expected no changes on outer class modifiers");
            assertTrue(Modifier.isPublic(inner.getModifiers()), "expected inner class to be made public");

            final ClassNode outerNode = new ClassNode();
            try (InputStream is = Files.newInputStream(classOutputDir.resolve(outer.getName().replace('.', '/') + ".class"))) {
                final ClassReader classReader = new ClassReader(is);
                classReader.accept(outerNode, 0);
            }
            outerNode.innerClasses.forEach(node -> {
                assertTrue((node.access & Opcodes.ACC_PUBLIC) != 0);
            });
        }
    }

    @TempDir
    Path classOutputDir;

    private URLClassLoader setupTest(String fileName, @Language("java") String source, String accessTransformerFile) throws IOException {
        // First compile the source class
        var diagnostics = new DiagnosticCollector<>();
        var compiler = ToolProvider.getSystemJavaCompiler();
        var manager = compiler.getStandardFileManager(diagnostics, Locale.ROOT, StandardCharsets.UTF_8);
        manager.setLocationFromPaths(StandardLocation.CLASS_OUTPUT, List.of(classOutputDir));
        var task = compiler.getTask(
                null, manager, diagnostics,
                List.of("-proc:none"), null,
                List.of(new SimpleJavaFileObject(URI.create("string:///" + fileName), JavaFileObject.Kind.SOURCE) {
                    @Override
                    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                        return source;
                    }
                })
        );
        if (!task.call()) {
            diagnostics.getDiagnostics().forEach(diagnostic -> System.err.println("Failed to compile: " + diagnostic));
            throw new RuntimeException("Failed to compile class");
        }

        var engine = AccessTransformerEngine.newEngine();
        engine.loadAT(new StringReader(accessTransformerFile), "test.accesstransformer");

        // Then have the engine process each compiled class
        Files.walkFileTree(classOutputDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".class")) {
                    // Read the class
                    var inputBytes = Files.readAllBytes(file);
                    var reader = new ClassReader(inputBytes);
                    var node = new ClassNode();
                    reader.accept(node, 0);

                    // Transform the class
                    var type = Type.getObjectType(node.name.replace('.', '/'));
                    boolean transformed = engine.transform(node, type);

                    // We expect each test class to be transformed
                    assertTrue(transformed, "Expected class named " + node.name + " to be transformed by an access transformer");

                    // Write the class back but modified
                    var cw = new ClassWriter(reader, 0);
                    node.accept(cw);
                    Files.write(file, cw.toByteArray());
                }
                return FileVisitResult.CONTINUE;
            }
        });

        // And finally create a class loader for the compiled files
        return new URLClassLoader(new URL[] {classOutputDir.toUri().toURL()}, TransformationTest.class.getClassLoader());
    }
}
