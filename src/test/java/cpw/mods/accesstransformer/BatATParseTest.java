package cpw.mods.accesstransformer;

import com.demonwav.primeiron.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import org.antlr.v4.runtime.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class BatATParseTest {
    @Test
    public void testParseBadAT() throws URISyntaxException, IOException {
        final CharStream charStream = CharStreams.fromStream(getClass().getClassLoader().getResourceAsStream("bad_at.cfg"));
        final AtLexer lexer = new AtLexer(charStream);
        final Vocabulary vocab = lexer.getVocabulary();

        final List<String> tokens = new ArrayList<>();

        int type;
        while ((type = lexer.nextToken().getType()) != AtLexer.EOF) {
            if (type != AtLexer.WS) {
                tokens.add(vocab.getSymbolicName(type));
            }
        }

        final List<String> expectation = Files.readAllLines(Paths.get(getClass().getClassLoader().getResource("bad_at.cfg.txt").toURI()));
        assertEquals(expectation, tokens);
    }
}
