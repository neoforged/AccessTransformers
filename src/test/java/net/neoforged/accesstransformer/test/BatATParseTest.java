package net.neoforged.accesstransformer.test;

import net.neoforged.accesstransformer.AccessTransformer;
import net.neoforged.accesstransformer.parser.AtParser;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BatATParseTest {
    @Test
    public void testParseBadAT() throws IOException, URISyntaxException {
        try (
                InputStream stream = getClass().getClassLoader().getResourceAsStream("bad_at.cfg");
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            List<AccessTransformer> transformers = AtParser.parse(reader, "bad_at.cfg");
            List<String> lines = new ArrayList<>();
            transformers.forEach(t -> lines.add(t.toString()));
            final List<String> expectation = Files.readAllLines(Paths.get(getClass().getClassLoader().getResource("bad_at.cfg.txt").toURI()));
            assertEquals(expectation, lines);
        }
    }
}
