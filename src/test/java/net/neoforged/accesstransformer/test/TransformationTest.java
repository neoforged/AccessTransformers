package net.neoforged.accesstransformer.test;

import net.neoforged.accesstransformer.api.AccessTransformerEngine;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransformationTest {
    private static final String testJarsPath = "build/classes/java/testJars";

    @BeforeAll
    public static void setup() {
        Configurator.setRootLevel(Level.DEBUG);
    }

    @Test
    public void testTransformation(@TempDir Path tempDir) throws Throwable {
        // Process the testJars
        var engine = AccessTransformerEngine.newEngine();
        engine.loadATFromResource("test_at.cfg");

        var testJarsFolder = Paths.get(testJarsPath).toAbsolutePath();
        var transformedJar = tempDir.resolve("testJars_transformed.jar");
        try (var stream = Files.walk(testJarsFolder);
                var os = Files.newOutputStream(transformedJar);
                var zos = new ZipOutputStream(os)) {
            stream.forEach(p -> {
                if (Files.isDirectory(p)) {
                    return;
                }

                var relativePath = testJarsFolder.relativize(p).toString();

                // Read
                byte[] inputBytes;
                try {
                    inputBytes = Files.readAllBytes(p);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                ClassReader cr = new ClassReader(inputBytes);
                ClassNode cn = new ClassNode();
                cr.accept(cn, 0);
                // Transform
                var type = Type.getObjectType(cn.name.replace('.', '/'));
                boolean transformed = engine.transform(cn, type);
                assertTrue(transformed);
                // Write
                var cw = new ClassWriter(cr, 0);
                cn.accept(cw);
                try {
                    zos.putNextEntry(new ZipEntry(relativePath.replace('\\', '/')));
                    zos.write(cw.toByteArray());
                    zos.closeEntry();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        // Shove the transformed jar into a URLClassLoader, and then run some tests on the loaded classes
        var testJarsUrl = new URL[] { transformedJar.toUri().toURL() };
        try (var testJarsClassLoader = new URLClassLoader(testJarsUrl, TransformationTest.class.getClassLoader())) {
            var transformedClass = Class.forName("net.neoforged.accesstransformer.testJar.ATTestClass", true, testJarsClassLoader);
            var transformedClass2 = Class.forName("net.neoforged.accesstransformer.testJar.DefaultClass", true, testJarsClassLoader);
            var transformedClass3 = Class.forName("net.neoforged.accesstransformer.testJar.DefaultClass$Inner", true, testJarsClassLoader);

            assertAll(
                    () -> assertTrue(Modifier.isPublic(transformedClass2.getModifiers()), "public class"),
                    () -> assertTrue(Modifier.isPublic(transformedClass3.getModifiers()), "public inner class"),
                    () -> assertTrue(Modifier.isProtected(transformedClass.getDeclaredField("privateField").getModifiers()), "public field"),
                    () -> assertTrue(Modifier.isPublic(transformedClass.getDeclaredField("finalPrivateField").getModifiers()), "public field"),
                    () -> assertTrue(!Modifier.isFinal(transformedClass.getDeclaredField("finalPrivateField").getModifiers()), "nonfinal field"),
                    () -> assertTrue(Modifier.isPublic(transformedClass.getDeclaredMethod("privateMethod").getModifiers()), "nonfinal method")
            );
        }
    }
}
