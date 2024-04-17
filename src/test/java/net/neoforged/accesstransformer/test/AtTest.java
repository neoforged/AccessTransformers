package net.neoforged.accesstransformer.test;

import net.neoforged.accesstransformer.parser.AtParser;
import org.junit.jupiter.api.Test;

import java.io.*;

class AtTest {
    @Test
    void test() {
        String string = "public net.minecraft.world.World func_175663_a(ILjava/lang/String;Lcom/mojang/authlib/GameProfile;IISLjava/lang/String;Z)Lnet/minecraft/util/math/BlockPos; # isAreaLoaded";
        try (Reader reader = new StringReader(string)) {
            AtParser.parse(reader, "test", (k, v) -> {});
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
