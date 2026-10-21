package com.wynprice.secretrooms.datagen;

import com.wynprice.secretrooms.server.blocks.SecretBlocks;
import com.wynprice.secretrooms.server.items.SecretItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class SecretBlockLootTableProvider extends FabricBlockLootSubProvider {

    public SecretBlockLootTableProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void generate() {
        this.dropSelf(SecretBlocks.GHOST_BLOCK);
        this.dropSelf(SecretBlocks.SECRET_STAIRS);
        this.dropSelf(SecretBlocks.SECRET_LEVER);
        this.dropSelf(SecretBlocks.SECRET_REDSTONE);
        this.dropSelf(SecretBlocks.ONE_WAY_GLASS);
        this.dropSelf(SecretBlocks.SECRET_WOODEN_BUTTON);
        this.dropSelf(SecretBlocks.SECRET_STONE_BUTTON);
        this.dropSelf(SecretBlocks.TORCH_LEVER);
        this.dropOther(SecretBlocks.WALL_TORCH_LEVER, SecretItems.TORCH_LEVER);
        this.dropSelf(SecretBlocks.SECRET_PRESSURE_PLATE);
        this.dropSelf(SecretBlocks.SECRET_PLAYER_PRESSURE_PLATE);
        this.add(SecretBlocks.SECRET_DOOR, this.createDoorTable(SecretBlocks.SECRET_DOOR));
        this.add(SecretBlocks.SECRET_IRON_DOOR, this.createDoorTable(SecretBlocks.SECRET_IRON_DOOR));
        this.dropSelf(SecretBlocks.SECRET_CHEST);
        this.dropSelf(SecretBlocks.SECRET_TRAPDOOR);
        this.dropSelf(SecretBlocks.SECRET_IRON_TRAPDOOR);
        this.dropSelf(SecretBlocks.SECRET_TRAPPED_CHEST);
        this.dropSelf(SecretBlocks.SECRET_GATE);
        this.dropSelf(SecretBlocks.SECRET_DAYLIGHT_DETECTOR);
        this.dropSelf(SecretBlocks.SECRET_OBSERVER);
        this.dropSelf(SecretBlocks.SECRET_CLAMBER);
    }
}
