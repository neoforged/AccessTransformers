package net.neoforged.accesstransformer.test;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class TestJarUtil {
    private TestJarUtil() {
    }

    public static ClassNode loadTestClass(String className) throws IOException {
        final ClassNode classNode = new ClassNode();
        try (InputStream is = Files.newInputStream(Paths.get("build/classes/java/testJars/" + className.replace('.', '/') + ".class"))) {
            final ClassReader classReader = new ClassReader(is);
            classReader.accept(classNode, 0);
        }
        return classNode;
    }
}
