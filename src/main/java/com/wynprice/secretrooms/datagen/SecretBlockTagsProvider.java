package com.wynprice.secretrooms.datagen;

import com.wynprice.secretrooms.server.blocks.SecretBlocks;
import com.wynprice.secretrooms.server.data.SecretBlockTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;

import java.util.concurrent.CompletableFuture;

public class SecretBlockTagsProvider extends FabricTagsProvider.BlockTagsProvider {

    public SecretBlockTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        builder(SecretBlockTags.ONE_WAY_GLASS_CULL)
            .add(BuiltInRegistries.BLOCK.getResourceKey(SecretBlocks.ONE_WAY_GLASS).orElseThrow())
            .addOptionalTag(ConventionalBlockTags.GLASS_BLOCKS);

        // Lets the secret clamber be climbed like a ladder
        builder(BlockTags.CLIMBABLE)
            .add(BuiltInRegistries.BLOCK.getResourceKey(SecretBlocks.SECRET_CLAMBER).orElseThrow());
    }
}
