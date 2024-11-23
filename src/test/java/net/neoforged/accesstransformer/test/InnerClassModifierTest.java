package net.neoforged.accesstransformer.test;

import net.neoforged.accesstransformer.api.AccessTransformerEngine;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InnerClassModifierTest {
    @BeforeAll
    public static void setup() {
        Configurator.setRootLevel(Level.DEBUG);
    }

    /**
     * Validate that the INNERCLASS metadata is correctly updated on the outer class.
     */
    @Test
    public void testInnerClassModifierChangedOnParent() throws Exception {
        AccessTransformerEngine engine = AccessTransformerEngine.newEngine();
        engine.loadATFromResource("innerclass_at.cfg");
        Type outerClass = Type.getObjectType("net.neoforged.accesstransformer.testJar.DefaultClass".replace('.', '/'));
        Type innerClass = Type.getObjectType("net.neoforged.accesstransformer.testJar.DefaultClass$Inner".replace('.', '/'));
        assertTrue(engine.containsClassTarget(outerClass));
        assertTrue(engine.containsClassTarget(innerClass));
        final ClassNode outerNode = new ClassNode();
        try (InputStream is = Files.newInputStream(Path.of("build/classes/java/testJars/" + outerClass.getInternalName() + ".class"))) {
            final ClassReader classReader = new ClassReader(is);
            classReader.accept(outerNode, 0);
        }
        engine.transform(outerNode, outerClass);
        outerNode.innerClasses.forEach(node -> {
            assertTrue((node.access & Opcodes.ACC_PUBLIC) != 0);
        });
    }
}
