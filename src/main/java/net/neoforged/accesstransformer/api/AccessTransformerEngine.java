package net.neoforged.accesstransformer.api;

import net.neoforged.accesstransformer.AccessTransformerEngineImpl;
import net.neoforged.accesstransformer.parser.TargetType;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Set;

/**
 * API methods for interacting with the AT Engine.
 */
public interface AccessTransformerEngine {

    /**
     * Loads an AT from the given {@code reader}.
     *
     * @param reader the reader to load the AT from
     * @param originName the name of the origin of the AT
     */
    void loadAT(Reader reader, String originName) throws IOException;

    /**
     * Loads an AT from the given {@code path}.
     *
     * @param path the path of the AT file
     * @throws IOException if the file could not be read
     */
    void loadATFromPath(final Path path) throws IOException;

    /**
     * Loads an AT from a {@link ClassLoader#getResource(String) resource} with the given name.
     *
     * @param resourceName the name of the AT file
     * @throws IOException        if the resource could not be read, or found
     * @throws URISyntaxException if the resource name is invalid
     */
    void loadATFromResource(final String resourceName) throws URISyntaxException, IOException;

    /**
     * {@return the classes targeted by the loaded ATs}
     */
    Set<Type> getTargets();

    /**
     * {@return true if the given {@code type} is targeted by the loaded ATs}
     * @param type the type to check
     */
    boolean containsClassTarget(Type type);

    /**
     * Looks up AT file sources of entries matching the given target
     *
     * @param className the class containing the target
     * @param type the type of the target
     * @param targetName the target's name (null for class targets, full descriptor for method targets, name for field targets)
     * @return The list of sources specifying the given target or null if none apply
     */
    default Set<String> getSourcesForTarget(final String className, final TargetType type, final String targetName) {
        return null;
    }

    /**
     * Attempts to transform the given {@code classNode}, and apply ATs, if any.
     * @param classNode the class to transform
     * @param name the name of the class
     * @return {@code true} if the class was transformed, or {@code false} otherwise
     */
    boolean transform(ClassNode classNode, Type name);

    /**
     * {@return a new AT engine}
     */
    static AccessTransformerEngine newEngine() {
        return new AccessTransformerEngineImpl();
    }

    // TODO: implement ATs using the visitor API: ClassVisitor getClassVisitor();
}
