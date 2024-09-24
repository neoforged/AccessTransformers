package net.neoforged.accesstransformer.test;

import net.neoforged.accesstransformer.api.TargetType;
import net.neoforged.accesstransformer.parser.AccessTransformerList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

public class AccessTransformerPresenceTest {
    @Test
    public void testSourceLookup() throws IOException, URISyntaxException {
        final AccessTransformerList atLoader = new AccessTransformerList();
        atLoader.loadFromResource("forge_at.cfg");

        // Class
        Assertions.assertEquals(Set.of("forge_at.cfg:99"), atLoader.getSourcesForTarget("net.minecraft.item.crafting.RecipeTippedArrow", TargetType.CLASS, null));
        // Inner class
        Assertions.assertEquals(Set.of("forge_at.cfg:76"), atLoader.getSourcesForTarget("net.minecraft.world.gen.structure.StructureStrongholdPieces$Stronghold", TargetType.CLASS, null));
        // Method
        Assertions.assertEquals(Set.of("forge_at.cfg:6"), atLoader.getSourcesForTarget("net.minecraft.block.Block", TargetType.METHOD, "func_149752_b(F)Lnet/minecraft/block/Block;"));
        // Field
        Assertions.assertEquals(Set.of("forge_at.cfg:22"), atLoader.getSourcesForTarget("net.minecraft.entity.EntityTrackerEntry", TargetType.FIELD, "field_73134_o"));
        // Method wildcard
        Assertions.assertEquals(Set.of("forge_at.cfg:29"), atLoader.getSourcesForTarget("net.minecraft.world.biome.Biome", TargetType.METHOD, "this_is_ignored(F)Ldoes/not/Matter;"));
        // Field wildcard
        Assertions.assertEquals(Set.of("forge_at.cfg:51"), atLoader.getSourcesForTarget("net.minecraft.world.biome.BiomeDecorator", TargetType.FIELD, "this_is_ignored"));
    }
}
