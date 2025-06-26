package net.neoforged.accesstransformer.test;

import net.neoforged.accesstransformer.AccessTransformer;
import net.neoforged.accesstransformer.parser.AtParser;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BadATParseTest {
    @Test
    public void testParseBadAT() throws IOException, URISyntaxException {
        try (
                InputStream stream = getClass().getClassLoader().getResourceAsStream("bad_at.cfg");
                Reader reader = new InputStreamReader(stream)) {
            List<AccessTransformer<?>> transformers = new ArrayList<>();
            AtParser.parse(reader, "bad_at.cfg", ((target, transformation) -> {
                transformers.add(AccessTransformer.of(target, transformation));
            }));
            List<String> lines = new ArrayList<>();
            transformers.forEach(t -> lines.add(t.toString()));
            final List<String> expectation = Files.readAllLines(Paths.get(getClass().getClassLoader().getResource("bad_at.cfg.txt").toURI()));
            assertEquals(expectation, lines);
        }
    }
}
