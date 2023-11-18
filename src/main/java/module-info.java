module net.neoforged.accesstransformers {
    exports net.neoforged.accesstransformer.api;
    requires antlr.runtime;
    requires org.antlr.antlr4.runtime;

    requires org.objectweb.asm;
    requires org.objectweb.asm.tree;

    requires jopt.simple;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
}