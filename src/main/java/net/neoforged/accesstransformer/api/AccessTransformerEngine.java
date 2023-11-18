package net.neoforged.accesstransformer.api;

import net.neoforged.accesstransformer.AccessTransformerEngineImpl;
import org.antlr.v4.runtime.CharStream;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Set;

/**
 * API methods for interacting with the AT Engine.
 */
public interface AccessTransformerEngine {

    /**
     * Loads an AT from the given {@code charStream}.
     *
     * @param charStream the stream to load the AT from
     */
    void loadAT(CharStream charStream);

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
