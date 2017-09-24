package cpw.mods.accesstransformer.parser;

import com.demonwav.primeiron.*;
import cpw.mods.accesstransformer.*;
import org.antlr.v4.runtime.*;
import org.apache.logging.log4j.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class AccessTransformerList {
    private static final Logger LOG = LogManager.getLogger();
    private final Map<Target, AccessTransformer> accessTransformers = new HashMap<>();

    public void loadFromResource(String resourceName) throws URISyntaxException, IOException {
        final Path path = Paths.get(getClass().getClassLoader().getResource(resourceName).toURI());
        final CharStream stream = CharStreams.fromPath(path);
        final AtLexer lexer = new AtLexer(stream);
        final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        final AtParser parser = new AtParser(tokenStream);
        parser.addErrorListener(new AtParserErrorListener());
        final AtParser.FileContext file = parser.file();
        final AccessTransformVisitor accessTransformVisitor = new AccessTransformVisitor(resourceName);
        file.accept(accessTransformVisitor);
        final HashMap<Target, AccessTransformer> localATCopy = new HashMap<>(accessTransformers);
        mergeAccessTransformers(accessTransformVisitor.getAccessTransformers(), localATCopy, resourceName);
        final List<AccessTransformer> invalidTransformers = invalidTransformers(localATCopy);
        if (!invalidTransformers.isEmpty()) {
            invalidTransformers.forEach(at -> LOG.error("Invalid access transform final state for target {}. Referred in resources {}.",at.getTarget(), at.getOrigins()));
            throw new IllegalArgumentException("Invalid AT final conflicts");
        }
        this.accessTransformers.clear();
        this.accessTransformers.putAll(localATCopy);
    }

    private void mergeAccessTransformers(final List<AccessTransformer> atList, final Map<Target, AccessTransformer> accessTransformers, final String resourceName) {
        atList.forEach(at -> accessTransformers.merge(at.getTarget(), at, (accessTransformer, at2) -> accessTransformer.mergeStates(at2, resourceName)));
    }

    private List<AccessTransformer> invalidTransformers(final HashMap<Target, AccessTransformer> accessTransformers) {
        return accessTransformers.values().stream().filter(e -> !e.isValid()).collect(Collectors.toList());
    }


    public Map<String, List<AccessTransformer>> getAccessTransformers() {
        final HashMap<String, List<AccessTransformer>> collect = accessTransformers.entrySet().stream().collect(Collectors.groupingBy(
                e -> e.getValue().getTarget().getClassName(),
                HashMap::new,
                Collectors.mapping(e -> e.getValue(), Collectors.toList()))
        );
        return collect;
    }
}
