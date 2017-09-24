package cpw.mods.accesstransformer;

import cpw.mods.accesstransformer.parser.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.*;

public class AccessTransformerLoadTest {
    @Test
    public void testLoadForgeAT() throws IOException, URISyntaxException {
        final AccessTransformerList atLoader = new AccessTransformerList();
        atLoader.loadFromResource("forge_at.cfg");
        final Map<String, List<AccessTransformer>> accessTransformers = atLoader.getAccessTransformers();
        accessTransformers.forEach((k,v) -> System.out.printf("Got %d ATs for %s:\n\t%s\n", v.size(), k, v.stream().map(Object::toString).collect(Collectors.joining(",\n\t"))));
    }
}
