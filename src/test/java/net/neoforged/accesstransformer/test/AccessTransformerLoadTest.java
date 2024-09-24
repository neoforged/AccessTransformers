package net.neoforged.accesstransformer.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.neoforged.accesstransformer.AccessTransformer;
import net.neoforged.accesstransformer.ml.AccessTransformerService;
import net.neoforged.accesstransformer.parser.AccessTransformerList;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class AccessTransformerLoadTest {
    @Test
    public void testLoadForgeAT() throws IOException, URISyntaxException {
        final AccessTransformerList atLoader = new AccessTransformerList();
        atLoader.loadFromResource("forge_at.cfg");
        final Map<String, List<AccessTransformer>> accessTransformers = atLoader.getAccessTransformers();
        testText(accessTransformers, Paths.get(ClassLoader.getSystemClassLoader().getResource("forge_at.cfg").toURI()));
    }

    @Test
    public void testLoadATFromJar() throws IOException, URISyntaxException {
        final AccessTransformerService mls = new AccessTransformerService();
        try (final FileSystem jarFS = FileSystems.newFileSystem(FileSystems.getDefault().getPath("src","test","resources","testatmod.jar"), getClass().getClassLoader())) {
            final Path atPath = jarFS.getPath("META-INF", "forge_at.cfg");
            mls.engine.loadATFromPath(atPath);
            final AccessTransformerList list = Whitebox.getInternalState(mls.engine, "masterList");
            final Map<String, List<AccessTransformer>> accessTransformers = list.getAccessTransformers();
            testText(accessTransformers, atPath);
        }
    }

    private static void testText(final Map<String, List<AccessTransformer>> accessTransformers, Path source) throws URISyntaxException, IOException {
        accessTransformers.forEach((k,v) -> System.out.printf("Got %d ATs for %s:\n\t%s\n", v.size(), k, v.stream().map(Object::toString).collect(Collectors.joining("\n\t"))));

        final TreeMap<String, List<String>> testOutput = accessTransformers.entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().map(AccessTransformer::toString).sorted().collect(Collectors.toList()),
                        (l1, l2) -> { throw new RuntimeException("duplicate keys"); },
                        TreeMap::new
                )
        );

        final String text = Files.readString(Paths.get(ClassLoader.getSystemClassLoader().getResource("forge_at.cfg.json").toURI()), StandardCharsets.UTF_8);
        final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
        final TreeMap<String, List<String>> expectation = GSON.fromJson(text, new TypeToken<TreeMap<String, List<String>>>() {}.getType());
        expectation.values().forEach(value -> value.replaceAll(str -> str.replace("{cfgfile}", source.toAbsolutePath().toString())));

        final String output = GSON.toJson(testOutput);
        final String expect = GSON.toJson(expectation);

        Assertions.assertThat(output).isEqualToNormalizingNewlines(expect);
    }
}
