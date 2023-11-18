package net.neoforged.accesstransformer.ml;

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import net.neoforged.accesstransformer.api.AccessTransformerEngine;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;

public class AccessTransformerService implements ILaunchPluginService {
    public final AccessTransformerEngine engine = AccessTransformerEngine.newEngine();

    @Override
    public String name() {
        return "accesstransformer";
    }

    /**
     * @deprecated Use {@link AccessTransformerEngine#loadATFromPath(Path)} instead
     */
    @Override
    @Deprecated(forRemoval = true, since = "10.0")
    public void offerResource(final Path path, final String resourceName) {
        try {
            engine.loadATFromPath(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load AT file " + path, e);
        }
    }

    @Override
    public int processClassWithFlags(final Phase phase, final ClassNode classNode, final Type classType, final String reason) {
        return engine.transform(classNode, classType) ? ComputeFlags.SIMPLE_REWRITE : ComputeFlags.NO_REWRITE;
    }

    private static final EnumSet<Phase> YAY = EnumSet.of(Phase.BEFORE);
    private static final EnumSet<Phase> NAY = EnumSet.noneOf(Phase.class);

    @Override
    public EnumSet<Phase> handlesClass(final Type classType, final boolean isEmpty) {
        return !isEmpty && engine.getTargets().contains(classType) ? YAY : NAY;
    }
}
