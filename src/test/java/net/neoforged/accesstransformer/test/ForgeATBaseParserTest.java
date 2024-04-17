package net.neoforged.accesstransformer.test;

import net.neoforged.accesstransformer.parser.AtParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ForgeATBaseParserTest {
    @Test
    public void testLoadingForgeAT() throws IOException, URISyntaxException {
        final Path path = Paths.get(getClass().getClassLoader().getResource("forge_at.cfg").toURI());
        try (Reader reader = Files.newBufferedReader(path)) {
            AtParser.parse(reader, "forge_at.cfg", (k, v) -> {});
        }
    }
}
