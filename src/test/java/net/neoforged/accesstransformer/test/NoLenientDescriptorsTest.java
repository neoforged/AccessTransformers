package net.neoforged.accesstransformer.test;

import net.neoforged.accesstransformer.parser.AtParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class NoLenientDescriptorsTest {
    @Test
    public void testParseInvalidDescriptor() throws IOException {
        try (
                InputStream stream = getClass().getClassLoader().getResourceAsStream("invalid_descriptor.cfg");
                Reader reader = new InputStreamReader(stream)) {
            assertThrows(RuntimeException.class, () -> {
                AtParser.parse(reader, "invalid_descriptor.cfg", ((target, transformation) -> {}));
            }, "Invalid method descriptor 'testMethod()Lnot.valid.anymore.to.avoid.Confusion;' at line 0");
        }
    }
}
