package net.neoforged.accesstransformer.test;

import net.neoforged.accesstransformer.AccessTransformerList;
import net.neoforged.accesstransformer.parser.TargetType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Set;

public class AccessTransformerPresenceTest {
    @Test
    public void testSourceLookup() throws IOException, URISyntaxException {
        final AccessTransformerList atLoader = new AccessTransformerList();

        Path filePath = Path.of(getClass().getClassLoader().getResource("forge_at.cfg").toURI());
        atLoader.loadFromPath(filePath);

        String sourcePrefix = filePath.toString();

        // Class
        Assertions.assertEquals(Set.of(sourcePrefix + ":99"), atLoader.getSourcesForTarget("net.minecraft.item.crafting.RecipeTippedArrow", TargetType.CLASS, null));
        // Inner class
        Assertions.assertEquals(Set.of(sourcePrefix + ":76"), atLoader.getSourcesForTarget("net.minecraft.world.gen.structure.StructureStrongholdPieces$Stronghold", TargetType.CLASS, null));
        // Method
        Assertions.assertEquals(Set.of(sourcePrefix + ":6"), atLoader.getSourcesForTarget("net.minecraft.block.Block", TargetType.METHOD, "func_149752_b(F)Lnet/minecraft/block/Block;"));
        // Field
        Assertions.assertEquals(Set.of(sourcePrefix + ":22"), atLoader.getSourcesForTarget("net.minecraft.entity.EntityTrackerEntry", TargetType.FIELD, "field_73134_o"));
        // Method wildcard
        Assertions.assertEquals(Set.of(sourcePrefix + ":29"), atLoader.getSourcesForTarget("net.minecraft.world.biome.Biome", TargetType.METHOD, "this_is_ignored(F)Ldoes/not/Matter;"));
        // Field wildcard
        Assertions.assertEquals(Set.of(sourcePrefix + ":51"), atLoader.getSourcesForTarget("net.minecraft.world.biome.BiomeDecorator", TargetType.FIELD, "this_is_ignored"));
    }
}
