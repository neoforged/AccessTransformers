package net.neoforged.accesstransformer.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class AccessTransformerFiles {
    private static final Logger LOGGER = LoggerFactory.getLogger("AXFORM");
    private static final Marker AXFORM_MARKER = MarkerFactory.getMarker("AXFORM");
    private final Map<Target, Transformation> accessTransformers = new HashMap<>();
    private final Map<Target, Transformation> accessTransformersExposed = Collections.unmodifiableMap(accessTransformers);
    private Set<String> targetedClassCache = Collections.emptySet();

    public void loadFromResource(final String resourceName) throws URISyntaxException, IOException {
        final Path path = Paths.get(getClass().getClassLoader().getResource(resourceName).toURI());
        loadFromPath(path);
    }

    public void loadFromPath(final Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            loadAT(reader, path.getFileName().toString());
        }
    }

    public void loadAT(Reader reader, String originName) throws IOException {
        final HashMap<Target, Transformation> localATCopy = new HashMap<>(accessTransformers);
        AtParser.parse(reader, originName, mergeAccessTransformers(localATCopy));
        final Map<Target, Transformation> invalidTransformers = invalidTransformers(localATCopy);
        if (!invalidTransformers.isEmpty()) {
            invalidTransformers.forEach((k, v) -> LOGGER.error(AXFORM_MARKER,"Invalid access transform final state for target {}. Referred in resources {}.",k, v.origins()));
            throw new IllegalArgumentException("Invalid AT final conflicts");
        }
        this.accessTransformers.clear();
        this.accessTransformers.putAll(localATCopy);
        this.targetedClassCache = this.accessTransformers.keySet().stream().map(Target::className).collect(Collectors.toSet());
        LOGGER.debug(AXFORM_MARKER,"Loaded access transformer {}", originName);
    }

    private BiConsumer<Target, Transformation> mergeAccessTransformers(final Map<Target, Transformation> accessTransformers) {
        return (k, v) -> {
            accessTransformers.merge(k, v, Transformation::mergeStates);
        };
    }

    private Map<Target, Transformation> invalidTransformers(final HashMap<Target, Transformation> accessTransformers) {
        HashMap<Target, Transformation> invalid = new HashMap<>();
        accessTransformers.forEach((target, transformation) -> {
            if (!transformation.isValid()) {
                invalid.put(target, transformation);
            }
        });
        return invalid;
    }

    public Map<Target, Transformation> getAccessTransformers() {
        return accessTransformersExposed;
    }

    public boolean containsClassTarget(final String name) {
        return getTargets().contains(name);
    }

    public Set<String> getTargets() {
        return targetedClassCache;
    }
}
