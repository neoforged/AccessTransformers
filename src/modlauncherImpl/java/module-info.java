import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import net.neoforged.accesstransformer.service.AccessTransformerService;

module net.neoforged.accesstransformers.modlauncher {
    requires net.neoforged.accesstransformers;
    requires cpw.mods.modlauncher;
    requires org.objectweb.asm;
    requires org.objectweb.asm.tree;
    provides ILaunchPluginService with AccessTransformerService;
}