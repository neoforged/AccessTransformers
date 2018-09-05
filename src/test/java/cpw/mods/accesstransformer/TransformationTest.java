package cpw.mods.accesstransformer;

import cpw.mods.accesstransformer.parser.*;
import cpw.mods.modlauncher.*;
import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.config.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.concurrent.*;
import org.powermock.reflect.*;

import static org.junit.jupiter.api.Assertions.*;

public class TransformationTest {
    @BeforeAll
    public static void setup() {
        Configurator.setRootLevel(Level.DEBUG);
    }

    @AfterEach
    public void cleanUp() {
        Whitebox.setInternalState(AccessTransformerEngine.INSTANCE, "masterList", new AccessTransformerList());
    }

    boolean calledback;
    Class<?> transformedClass;
    Class<?> transformedClass2;

    @Test
    public void testTestingLaunchHandler() throws IOException, URISyntaxException {
        System.setProperty("test.harness", "build/classes/java/testJars");
        System.setProperty("test.harness.callable", "cpw.mods.accesstransformer.TransformationTest$TestCallback");
        calledback = false;
        TestCallback.callable = () -> {
            calledback = true;
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            transformedClass = Class.forName("cpw.mods.accesstransformer.testJar.ATTestClass", true, contextClassLoader);
            transformedClass2 = Class.forName("cpw.mods.accesstransformer.testJar.DefaultClass", true, contextClassLoader);
            return null;
        };
        AccessTransformerList list = Whitebox.getInternalState(AccessTransformerEngine.INSTANCE, "masterList");
        list.loadFromResource("test_at.cfg");
        Launcher.main("--version", "1.0", "--launchTarget", "testharness");
        assertTrue(calledback, "We got called back");
        assertAll(
                ()-> assertTrue(Modifier.isPublic(transformedClass2.getModifiers()), "public class"),
                ()-> assertTrue(Modifier.isProtected(transformedClass.getDeclaredField("privateField").getModifiers()), "public field"),
                ()-> assertTrue(Modifier.isPublic(transformedClass.getDeclaredField("finalPrivateField").getModifiers()), "public field"),
                ()-> assertTrue(!Modifier.isFinal(transformedClass.getDeclaredField("finalPrivateField").getModifiers()), "nonfinal field"),
                ()-> assertTrue(Modifier.isPublic(transformedClass.getDeclaredMethod("privateMethod").getModifiers()), "nonfinal method")
        );
    }

    private String toBinary(int num)
    {
        return String.format("%16s", Integer.toBinaryString(num)).replace(' ', '0');
    }

    public static class TestCallback {
        private static Callable<Void> callable;
        public static Callable<Void> supplier() {
            return callable;
        }
    }
}
