package net.neoforged.accesstransformer.test;

import cpw.mods.bootstraplauncher.BootstrapLauncher;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.ServiceRunner;
import net.neoforged.accesstransformer.ml.AccessTransformerService;
import net.neoforged.accesstransformer.parser.AccessTransformerList;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransformationTest {
    @BeforeAll
    public static void setup() {
        Configurator.setRootLevel(Level.DEBUG);
    }

    @Test
    public void testTransformation() throws Throwable {
        var originalClassLoader = Thread.currentThread().getContextClassLoader();

        System.setProperty("test.harness.game", "build/classes/java/testJars");
        System.setProperty("test.harness.callable", TestCallback.class.getName());

        try {
            BootstrapLauncher.main("--version", "1.0", "--launchTarget", "testharness");

            var transformingClassLoader = Thread.currentThread().getContextClassLoader();
            var mcBootClassLoader = transformingClassLoader.getClass().getClassLoader();

            assertEquals("TransformingClassLoader", transformingClassLoader.getClass().getSimpleName());
            assertEquals("ModuleClassLoader", mcBootClassLoader.getClass().getSimpleName());

            var testLogic = Class.forName("net.neoforged.accesstransformer.test.TransformationTest$TestLogic", true, mcBootClassLoader);
            testLogic.getDeclaredMethod("doTest", ClassLoader.class).invoke(null, transformingClassLoader);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    public static class TestCallback {
        public static ServiceRunner supplier() {
            // Return a NO-OP ServiceRunner to continue JUnit testing.
            return ServiceRunner.NOOP;
        }
    }

    /**
     * Separated test logic - this needs to run on the boot layer.
     */
    public static class TestLogic {
        static Class<?> transformedClass;
        static Class<?> transformedClass2;
        static Class<?> transformedClass3;

        public static void doTest(ClassLoader transformingClassLoader) {
            AccessTransformerList list = Whitebox.getInternalState(((AccessTransformerService) Launcher.INSTANCE.environment().findLaunchPlugin("accesstransformer").orElseThrow()).engine, "masterList");
            assertDoesNotThrow(() -> list.loadFromResource("test_at.cfg"));

            assertDoesNotThrow(() -> {
                transformedClass = Class.forName("net.neoforged.accesstransformer.testJar.ATTestClass", true, transformingClassLoader);
                transformedClass2 = Class.forName("net.neoforged.accesstransformer.testJar.DefaultClass", true, transformingClassLoader);
                transformedClass3 = Class.forName("net.neoforged.accesstransformer.testJar.DefaultClass$Inner", true, transformingClassLoader);
            });

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