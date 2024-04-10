package net.neoforged.accesstransformer.parser;

import net.neoforged.accesstransformer.AccessTransformer;
import net.neoforged.accesstransformer.Target;
import net.neoforged.accesstransformer.TargetType;
import org.objectweb.asm.Type;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        try (Reader reader = Files.newBufferedReader(path)) {
            loadAT(reader, path.getFileName().toString());
        }
    }

    public void loadAT(Reader reader, String originName) throws IOException {
        final HashMap<Target<?>, AccessTransformer> localATCopy = new HashMap<>(accessTransformers);
        mergeAccessTransformers(AtParser.parse(reader, originName), localATCopy, originName);
        final List<AccessTransformer> invalidTransformers = invalidTransformers(localATCopy);
        if (!invalidTransformers.isEmpty()) {
            invalidTransformers.forEach(at -> LOGGER.error(AXFORM_MARKER,"Invalid access transform final state for target {}. Referred in resources {}.",at.getTarget(), at.getOrigins()));
            throw new IllegalArgumentException("Invalid AT final conflicts");
        }
        this.accessTransformers.clear();
        this.accessTransformers.putAll(localATCopy);
        this.targetedClassCache = this.accessTransformers.keySet().stream().map(Target::getASMType).collect(Collectors.toSet());
        LOGGER.debug(AXFORM_MARKER,"Loaded access transformer {}", originName);
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
