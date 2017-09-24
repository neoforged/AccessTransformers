package cpw.mods.accesstransformer;

import com.demonwav.primeiron.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.fail;

public class ForgeATBaseParserTest {
    @Test
    public void testLoadingForgeAT() throws IOException, URISyntaxException {
        final Path path = Paths.get(getClass().getClassLoader().getResource("forge_at.cfg").toURI());
        final CharStream stream = CharStreams.fromPath(path);
        final AtLexer lexer = new AtLexer(stream);
        final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        final AtParser parser = new AtParser(tokenStream);
        parser.addErrorListener(new ANTLRErrorListener() {
            @Override
            public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine, final String msg, final RecognitionException e) {
                fail("syntax error");
            }

            @Override
            public void reportAmbiguity(final Parser recognizer, final DFA dfa, final int startIndex, final int stopIndex, final boolean exact, final BitSet ambigAlts, final ATNConfigSet configs) {
            }

            @Override
            public void reportAttemptingFullContext(final Parser recognizer, final DFA dfa, final int startIndex, final int stopIndex, final BitSet conflictingAlts, final ATNConfigSet configs) {
            }

            @Override
            public void reportContextSensitivity(final Parser recognizer, final DFA dfa, final int startIndex, final int stopIndex, final int prediction, final ATNConfigSet configs) {
            }
        });
        parser.file();
    }
}
