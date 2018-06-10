package cpw.mods.accesstransformer.service;

import cpw.mods.accesstransformer.*;
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
    public void addResource(final Path path, final String resourceName) {
        AccessTransformerEngine.INSTANCE.addResource(path, resourceName);
    }

    @Override
    public ClassNode processClass(final ClassNode classNode, final Type classType) {
        return AccessTransformerEngine.INSTANCE.transform(classNode, classType);
    }

    @Override
    public boolean handlesClass(final Type classType, final boolean isEmpty) {
        return !isEmpty && AccessTransformerEngine.INSTANCE.handlesClass(classType);
    }

    @Override
    public Void getExtension() {
        return null;
    }
}
