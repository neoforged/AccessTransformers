package cpw.mods.accesstransformer;

import cpw.mods.accesstransformer.parser.*;
import cpw.mods.accesstransformer.service.*;
import cpw.mods.accesstransformer.transformer.*;
import org.junit.jupiter.api.*;
import org.powermock.reflect.*;
import org.powermock.reflect.internal.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class AccessTransformerLoadTest {
    @Test
    public void testLoadForgeAT() throws IOException, URISyntaxException {
        final AccessTransformerList atLoader = new AccessTransformerList();
        atLoader.loadFromResource("forge_at.cfg");
        final Map<String, List<AccessTransformer>> accessTransformers = atLoader.getAccessTransformers();
        accessTransformers.forEach((k,v) -> System.out.printf("Got %d ATs for %s:\n\t%s\n", v.size(), k, v.stream().map(Object::toString).collect(Collectors.joining("\n\t"))));
    }

    @Test
    public void testLoadATFromJar() throws IOException {
        final ModLauncherService mls = new ModLauncherService();
        final FileSystem jarFS = FileSystems.newFileSystem(FileSystems.getDefault().getPath("src","test","resources","testatmod.jar"), getClass().getClassLoader());
        final Path atPath = jarFS.getPath("META-INF", "forge_at.cfg");
        mls.addResource(atPath,"forge_at.cfg");
        AccessTransformerList list = WhiteboxImpl.getInternalState(AccessTransformerEngine.INSTANCE,"masterList");
        final Map<String, List<AccessTransformer>> accessTransformers = list.getAccessTransformers();
        accessTransformers.forEach((k,v) -> System.out.printf("Got %d ATs for %s:\n\t%s\n", v.size(), k, v.stream().map(Object::toString).collect(Collectors.joining("\n\t"))));
    }
}
