package net.minecraftforge.accesstransformer.service;

import cpw.mods.modlauncher.serviceapi.*;
import net.minecraftforge.accesstransformer.*;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.nio.file.*;
import java.util.EnumSet;

@SuppressWarnings("unchecked")
public class ModLauncherService implements ILaunchPluginService {
    @Override
    public String name() {
        return "accesstransformer";
    }

    @Override
    public void addResource(final Path path, final String resourceName) {
        AccessTransformerEngine.INSTANCE.addResource(path, resourceName);
    }

    @Override
    public boolean processClass(final Phase phase, final ClassNode classNode, final Type classType) {
        return AccessTransformerEngine.INSTANCE.transform(classNode, classType);
    }

    private static final EnumSet<Phase> YAY = EnumSet.of(Phase.BEFORE);
    private static final EnumSet<Phase> NAY = EnumSet.noneOf(Phase.class);

    @Override
    public EnumSet<Phase> handlesClass(final Type classType, final boolean isEmpty) {
        return !isEmpty && AccessTransformerEngine.INSTANCE.handlesClass(classType) ? YAY : NAY;
    }
}
