package com.wynprice.secretrooms.datagen;

import com.wynprice.secretrooms.server.data.SecretItemTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class SecretItemTagsProvider extends FabricTagsProvider.ItemTagsProvider {

    public SecretItemTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        builder(SecretItemTags.EARTH_ITEM)
            .add(BuiltInRegistries.ITEM.getResourceKey(Items.CLAY_BALL).orElseThrow())
            .add(BuiltInRegistries.ITEM.getResourceKey(Items.DIRT).orElseThrow())
            .add(BuiltInRegistries.ITEM.getResourceKey(Items.SAND).orElseThrow());
        builder(SecretItemTags.SECRET_RECIPE_ITEMS)
            .add(BuiltInRegistries.ITEM.getResourceKey(Items.ROTTEN_FLESH).orElseThrow())
            .addOptionalTag(ItemTags.WOOL);
        builder(SecretItemTags.CLEAR_GLASS)
            .add(BuiltInRegistries.ITEM.getResourceKey(Items.GLASS).orElseThrow());
    }
}
