package cpw.mods.accesstransformer.service;

import cpw.mods.accesstransformer.transformer.*;
import cpw.mods.modlauncher.serviceapi.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.nio.file.*;

@SuppressWarnings("unchecked")
public class ModLauncherService implements ILaunchPluginService {
    @Override
    public String name() {
        return "accesstransformer";
    }

    @Override
    public void addResource(final Path path) {
        AccessTransformerEngine.INSTANCE.addResource(path);
    }

    @Override
    public ClassNode processClass(final ClassNode classNode, final Type classType) {
        return null;
    }

    @Override
    public boolean handlesClass(final Type classType) {
        return AccessTransformerEngine.INSTANCE.handlesClass(classType.getClassName());
    }

    @Override
    public Void getExtension() {
        return null;
    }
}
