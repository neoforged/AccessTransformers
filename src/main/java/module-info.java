module net.neoforged.accesstransformer {
    exports net.neoforged.accesstransformer.api;
    requires org.antlr.antlr4.runtime;

    requires transitive org.objectweb.asm;
    requires transitive org.objectweb.asm.tree;

    requires org.slf4j;
}