package cpw.mods.accesstransformer.service;

import cpw.mods.modlauncher.serviceapi.*;
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

    }

    @Override
    public ClassNode processClass(final ClassNode classNode) {
        return null;
    }

    @Override
    public Void getExtension() {
        return null;
    }
}
