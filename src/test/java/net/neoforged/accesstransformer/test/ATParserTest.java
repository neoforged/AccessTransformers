package net.neoforged.accesstransformer.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.neoforged.accesstransformer.AccessTransformer;
import net.neoforged.accesstransformer.AccessTransformerList;
import net.neoforged.accesstransformer.parser.AtParser;
import net.neoforged.accesstransformer.parser.Target;
import net.neoforged.accesstransformer.parser.Transformation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

class ATParserTest {
    @Test
    void testSimpleParse() {
        String string = "public net.minecraft.client.Minecraft reloadResourcePacks()Ljava/util/concurrent/CompletableFuture; # reloadResourcePacks";

        var transformations = new HashMap<Target, Transformation>();
        Assertions.assertThatNoException()
                .isThrownBy(() -> AtParser.parse(new StringReader(string), "test", transformations::put));
        Assertions.assertThat(transformations)
                .containsExactly(
                        Map.entry(
                                new Target.MethodTarget(
                                        "net.minecraft.client.Minecraft",
                                        "reloadResourcePacks",
                                        "()Ljava/util/concurrent/CompletableFuture;"
                                ),
                                new Transformation(
                                        Transformation.Modifier.PUBLIC,
                                        Transformation.FinalState.LEAVE,
                                        "test", 1
                                )
                        )
                );
    }

    @Test
    void testParse() throws IOException, URISyntaxException {
        final AccessTransformerList atLoader = new AccessTransformerList();
        atLoader.loadFromResource("test_at.cfg");
        final Map<String, List<AccessTransformer<?>>> accessTransformers = atLoader.getAccessTransformers();
        testText(accessTransformers, Paths.get(ClassLoader.getSystemClassLoader().getResource("test_at.cfg").toURI()));
    }

    @Test
    void testParseFromFile(@TempDir Path tempDir) throws IOException, URISyntaxException {
        final AccessTransformerList atLoader = new AccessTransformerList();
        final Path testFile = tempDir.resolve("test.accesstransformer");

        try (var in = getClass().getResourceAsStream("/test_at.cfg");
            var out = Files.newOutputStream(testFile)) {
            in.transferTo(out);
        }

        atLoader.loadFromPath(testFile);
        final Map<String, List<AccessTransformer<?>>> accessTransformers = atLoader.getAccessTransformers();
        testText(accessTransformers, testFile);
    }

    private static void testText(final Map<String, List<AccessTransformer<?>>> accessTransformers, Path source) throws URISyntaxException, IOException {
        accessTransformers.forEach((k,v) -> System.out.printf("Got %d ATs for %s:\n\t%s\n", v.size(), k, v.stream().map(Object::toString).collect(Collectors.joining("\n\t"))));

        final TreeMap<String, List<String>> testOutput = accessTransformers.entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().map(AccessTransformer::toString).sorted().collect(Collectors.toList()),
                        (l1, l2) -> { throw new RuntimeException("duplicate keys"); },
                        TreeMap::new
                )
        );

        final String text = Files.readString(Paths.get(ClassLoader.getSystemClassLoader().getResource("test_at.cfg.json").toURI()), StandardCharsets.UTF_8);
        final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
        final TreeMap<String, List<String>> expectation = GSON.fromJson(text, new TypeToken<TreeMap<String, List<String>>>() {}.getType());
        expectation.values().forEach(value -> value.replaceAll(str -> str.replace("{cfgfile}", source.toAbsolutePath().toString())));

        final String output = GSON.toJson(testOutput);
        final String expect = GSON.toJson(expectation);

        Assertions.assertThat(output).isEqualToNormalizingNewlines(expect);
    }
}
