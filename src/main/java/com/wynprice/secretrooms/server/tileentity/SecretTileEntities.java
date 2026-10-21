package com.wynprice.secretrooms.server.tileentity;

import com.wynprice.secretrooms.SecretRooms7;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

import static com.wynprice.secretrooms.server.blocks.SecretBlocks.*;

public class SecretTileEntities {

    public static final BlockEntityType<SecretTileEntity> SECRET_TILE_ENTITY = register("secret_tile_entity", SecretTileEntity::new,
        GHOST_BLOCK, SECRET_STAIRS, SECRET_LEVER, SECRET_REDSTONE, ONE_WAY_GLASS, SECRET_WOODEN_BUTTON,
        SECRET_STONE_BUTTON, SECRET_PRESSURE_PLATE, SECRET_PLAYER_PRESSURE_PLATE, SECRET_DOOR, SECRET_IRON_DOOR,
        SECRET_TRAPDOOR, SECRET_IRON_TRAPDOOR, SECRET_OBSERVER, SECRET_CLAMBER, SECRET_GATE, SECRET_DUMMY_BLOCK
    );

    public static final BlockEntityType<SecretChestTileEntity> SECRET_CHEST_TILE_ENTITY = register("secret_chest_tile_entity",
        SecretChestTileEntity::new, SECRET_CHEST, SECRET_TRAPPED_CHEST);

    public static final BlockEntityType<SecretDaylightDetectorTileEntity> SECRET_DAYLIGHT_DETECTOR_TILE_ENTITY = register("secret_daylight_detector_tile_entity",
        SecretDaylightDetectorTileEntity::new, SECRET_DAYLIGHT_DETECTOR);

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType.BlockEntitySupplier<T> factory, Block... blocks) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SecretRooms7.id(name), new BlockEntityType<>(factory, Set.of(blocks)));
    }

    public static void init() {
    }
}
