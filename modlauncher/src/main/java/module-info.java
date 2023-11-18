import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import net.neoforged.accesstransformer.ml.AccessTransformerService;

module net.neoforged.accesstransformer.modlauncher {
    requires net.neoforged.accesstransformer;
    requires static cpw.mods.modlauncher;
    requires org.objectweb.asm;
    requires org.objectweb.asm.tree;

    exports net.neoforged.accesstransformer.ml;
    provides ILaunchPluginService with AccessTransformerService;
}