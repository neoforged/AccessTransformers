package net.neoforged.accesstransformer.parser;

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

public class AccessTransformerFiles {
    private final Map<Target, Transformation> accessTransformers = new HashMap<>();
    private final Map<Target, Transformation> accessTransformersExposed = Collections.unmodifiableMap(accessTransformers);
    private Set<String> targetedClassCache = Collections.emptySet();

    public void loadFromResource(final String resourceName) throws URISyntaxException, IOException {
        final Path path = Paths.get(getClass().getClassLoader().getResource(resourceName).toURI());
        loadFromPath(path);
    }

    public void loadFromPath(final Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            loadAT(reader, path.toAbsolutePath().toString());
        }
    }

    public void loadAT(Reader reader, String originName) throws IOException {
        final HashMap<Target, Transformation> localATCopy = new HashMap<>(accessTransformers);
        AtParser.parse(reader, originName, mergeAccessTransformers(localATCopy));
        final Map<Target, Transformation> invalidTransformers = invalidTransformers(localATCopy);
        if (!invalidTransformers.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Target, Transformation> entry : invalidTransformers.entrySet()) {
                sb.append("Invalid access transform final state for target ")
                  .append(entry.getKey())
                  .append(". Referred in resources ")
                  .append(entry.getValue().origins())
                  .append("\n");
            }
            throw new RuntimeException(sb.toString());
        }
        this.accessTransformers.clear();
        this.accessTransformers.putAll(localATCopy);

        Set<String> targets = new java.util.HashSet<String>();
        for (Target t : this.accessTransformers.keySet()) {
            targets.add(t.className());
        }
        this.targetedClassCache = targets;
    }

    private BiConsumer<Target, Transformation> mergeAccessTransformers(final Map<Target, Transformation> accessTransformers) {
        return (k, v) -> {
            if (accessTransformers.containsKey(k)) {
                accessTransformers.put(k, accessTransformers.get(k).mergeStates(v));
            } else {
                accessTransformers.put(k, v);
            }
        };
    }

    private Map<Target, Transformation> invalidTransformers(final HashMap<Target, Transformation> accessTransformers) {
        HashMap<Target, Transformation> invalid = new HashMap<>();
        for (Map.Entry<Target, Transformation> entry : accessTransformers.entrySet()) {
            if (!entry.getValue().isValid()) {
                invalid.put(entry.getKey(), entry.getValue());
            }
        }
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
