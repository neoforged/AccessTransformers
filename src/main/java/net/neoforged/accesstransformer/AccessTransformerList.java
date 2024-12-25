package net.neoforged.accesstransformer;

import net.neoforged.accesstransformer.parser.AccessTransformerFiles;
import net.neoforged.accesstransformer.parser.Target;
import net.neoforged.accesstransformer.parser.TargetType;
import net.neoforged.accesstransformer.parser.Transformation;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AccessTransformerList {
    private final AccessTransformerFiles atFiles = new AccessTransformerFiles();

    public void loadFromResource(final String resourceName) throws URISyntaxException, IOException {
        atFiles.loadFromResource(resourceName);
    }

    public void loadFromPath(final Path path) throws IOException {
        atFiles.loadFromPath(path);
    }

    public void loadAT(Reader reader, String originName) throws IOException {
        atFiles.loadAT(reader, originName);
    }

    public Map<String, List<AccessTransformer<?>>> getAccessTransformers() {
        return atFiles.getAccessTransformers().entrySet().stream().collect(Collectors.groupingBy(
                e -> e.getKey().className(),
                HashMap::new,
                Collectors.mapping((Map.Entry<Target, Transformation> e) -> AccessTransformer.of(e.getKey(), e.getValue()), Collectors.toList()))
        );
    }

    public boolean containsClassTarget(final Type type) {
        return atFiles.containsClassTarget(type.getClassName());
    }

    public Set<Type> getTargets() {
        return atFiles.getTargets().stream().map(s -> Type.getObjectType(s.replace('.', '/'))).collect(Collectors.toSet());
    }

    public Map<TargetType, Map<String, AccessTransformer<?>>> getTransformersForTarget(final Type type) {
        return atFiles.getAccessTransformers().entrySet().stream()
                .filter(e -> type.getClassName().equals(e.getKey().className()))
                .map(e -> AccessTransformer.of(e.getKey(), e.getValue()))
                .collect(Collectors.groupingBy(
                        (Function<AccessTransformer<?>, TargetType>) AccessTransformer::getType,
                        HashMap::new,
                        Collectors.toMap((Function<AccessTransformer<?>, String>) AccessTransformer::targetName, Function.<AccessTransformer<?>>identity())
                ));
    }

    public Set<String> getSourcesForTarget(final String className, final TargetType type, final String targetName) {
        return atFiles.getAccessTransformers()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().matches(className, type, targetName))
                .map(Map.Entry::getValue)
                .map(Transformation::origins)
                .map(HashSet::new)
                .reduce((s1, s2) -> {
                    s1.addAll(s2);
                    return s1;
                })
                .orElse(null);
    }
}
