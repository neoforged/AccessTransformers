package com.demonwav.primeiron;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

class AtTest {

    @Test
    void test() {
        final CodePointCharStream stream = CharStreams.fromString("public net.minecraft.world.World func_175663_a(ILjava/lang/String;Lcom/mojang/authlib/GameProfile;IISLjava/lang/String;Z)Lnet/minecraft/util/math/BlockPos; # isAreaLoaded");
        final AtLexer lexer = new AtLexer(stream);
        final AtParser parser = new AtParser(new CommonTokenStream(lexer));
        parser.file();
    }
}
