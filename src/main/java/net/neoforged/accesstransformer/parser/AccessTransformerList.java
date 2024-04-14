package net.neoforged.accesstransformer.parser;

import net.neoforged.accesstransformer.api.AccessTransformer;
import net.neoforged.accesstransformer.api.Target;
import net.neoforged.accesstransformer.api.TargetType;
import net.neoforged.accesstransformer.generated.*;

import org.antlr.v4.runtime.*;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.*;
import java.net.*;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class AccessTransformerList {
    private static final Logger LOGGER = LoggerFactory.getLogger("AXFORM");
    private static final Marker AXFORM_MARKER = MarkerFactory.getMarker("AXFORM");
    private final Map<Target<?>, AccessTransformer> accessTransformers = new HashMap<>();
    private Set<Type> targetedClassCache = Collections.emptySet();

    public void loadFromResource(final String resourceName) throws URISyntaxException, IOException {
        final Path path = Paths.get(getClass().getClassLoader().getResource(resourceName).toURI());
        loadFromPath(path);
    }

    public void loadFromPath(final Path path) throws IOException {
        long size = Files.size(path);
        try (ReadableByteChannel channel = Files.newByteChannel(path)) {
            loadAT(CharStreams.fromChannel(
                    channel,
                    StandardCharsets.UTF_8,
                    4096, // CharStreams.DEFAULT_BUFFER_SIZE
                    CodingErrorAction.REPLACE,
                    path.getFileName().toString(),
                    size));
        }
    }

    public void loadAT(final CharStream stream) {
        LOGGER.debug(AXFORM_MARKER, "Loading access transformer {}", stream.getSourceName());
        final AtLexer lexer = new AtLexer(stream);
        final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        final AtParser parser = new AtParser(tokenStream);
        parser.addErrorListener(new AtParserErrorListener());
        final AtParser.FileContext file = parser.file();
        final AccessTransformVisitor accessTransformVisitor = new AccessTransformVisitor(stream.getSourceName());
        file.accept(accessTransformVisitor);
        final HashMap<Target<?>, AccessTransformer> localATCopy = new HashMap<>(accessTransformers);
        mergeAccessTransformers(accessTransformVisitor.getAccessTransformers(), localATCopy, stream.getSourceName());
        final List<AccessTransformer> invalidTransformers = invalidTransformers(localATCopy);
        if (!invalidTransformers.isEmpty()) {
            invalidTransformers.forEach(at -> LOGGER.error(AXFORM_MARKER,"Invalid access transform final state for target {}. Referred in resources {}.",at.getTarget(), at.getOrigins()));
            throw new IllegalArgumentException("Invalid AT final conflicts");
        }
        this.accessTransformers.clear();
        this.accessTransformers.putAll(localATCopy);
        this.targetedClassCache = this.accessTransformers.keySet().stream().map(Target::getASMType).collect(Collectors.toSet());
        LOGGER.debug(AXFORM_MARKER,"Loaded access transformer {}", stream.getSourceName());
    }

    private void mergeAccessTransformers(final List<AccessTransformer> atList, final Map<Target<?>, AccessTransformer> accessTransformers, final String resourceName) {
        atList.forEach(at -> accessTransformers.merge(at.getTarget(), at, (accessTransformer, at2) -> accessTransformer.mergeStates(at2, resourceName)));
    }

    private List<AccessTransformer> invalidTransformers(final HashMap<Target<?>, AccessTransformer> accessTransformers) {
        return accessTransformers.values().stream().filter(e -> !e.isValid()).collect(Collectors.toList());
    }

    public Map<String, List<AccessTransformer>> getAccessTransformers() {
        return accessTransformers.entrySet().stream().collect(Collectors.groupingBy(
                (Map.Entry<Target<?>, AccessTransformer> e) -> e.getValue().getTarget().getClassName(),
                HashMap::new,
                Collectors.mapping(Map.Entry::getValue, Collectors.toList()))
        );
    }

    public boolean containsClassTarget(final Type type) {
        return getTargets().contains(type);
    }

    public Set<Type> getTargets() {
        return targetedClassCache;
    }

    public Map<TargetType, Map<String,AccessTransformer>> getTransformersForTarget(final Type type) {
        return accessTransformers.entrySet().stream()
                .filter(e -> type.equals(e.getKey().getASMType()))
                .map(Map.Entry::getValue)
                .collect(Collectors.groupingBy(o->o.getTarget().getType(), HashMap::new, Collectors.toMap(at->at.getTarget().targetName(), Function.identity())));
    }

}
