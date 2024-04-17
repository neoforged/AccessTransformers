module net.neoforged.accesstransformer {
    exports net.neoforged.accesstransformer.api;

    requires transitive org.objectweb.asm;
    requires transitive org.objectweb.asm.tree;
    requires transitive net.neoforged.accesstransformer.parser;

    requires org.slf4j;
}