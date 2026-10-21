package com.wynprice.secretrooms.server.blocks;

import com.wynprice.secretrooms.SecretRooms7;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SecretBlocks {

    private static final List<Block> ALL = new ArrayList<>();

    public static final Block GHOST_BLOCK = register("ghost_block", GhostBlock::new, srm());
    public static final Block SECRET_STAIRS = register("secret_stairs", SecretStairs::new, srm());
    public static final Block SECRET_LEVER = register("secret_lever", SecretLever::new, srm());
    public static final Block SECRET_REDSTONE = register("secret_redstone", SecretRedstone::new, srm());
    public static final Block ONE_WAY_GLASS = register("one_way_glass", OneWayGlass::new, srm());
    public static final Block SECRET_WOODEN_BUTTON = register("secret_wooden_button", p -> new SecretButton(p, true), srm());
    public static final Block SECRET_STONE_BUTTON = register("secret_stone_button", p -> new SecretButton(p, false), srm());
    public static final Block TORCH_LEVER = register("torch_lever", TorchLever::new, torch());
    public static final Block WALL_TORCH_LEVER = register("wall_torch_lever", WallTorchLever::new, torch().overrideLootTable(TORCH_LEVER.getLootTable()).overrideDescription(TORCH_LEVER.getDescriptionId()));
    public static final Block SECRET_PRESSURE_PLATE = register("secret_pressure_plate", p -> new SecretPressurePlate(p, entity -> true), srm());
    public static final Block SECRET_PLAYER_PRESSURE_PLATE = register("secret_player_pressure_plate", p -> new SecretPressurePlate(p, entity -> entity instanceof Player), srm());
    public static final Block SECRET_DOOR = register("secret_door", p -> new SecretDoor(p, BlockSetType.OAK), srm());
    public static final Block SECRET_IRON_DOOR = register("secret_iron_door", p -> new SecretDoor(p, BlockSetType.IRON), srmMetal());
    public static final Block SECRET_CHEST = register("secret_chest", SecretChest::new, srm());
    public static final Block SECRET_TRAPDOOR = register("secret_trapdoor", p -> new SecretTrapdoor(p, BlockSetType.OAK), srm());
    public static final Block SECRET_IRON_TRAPDOOR = register("secret_iron_trapdoor", p -> new SecretTrapdoor(p, BlockSetType.IRON), srmMetal());
    public static final Block SECRET_TRAPPED_CHEST = register("secret_trapped_chest", SecretTrappedChest::new, srm());
    public static final Block SECRET_GATE = register("secret_gate", SecretGateBlock::new, srm());
    public static final Block SECRET_DUMMY_BLOCK = register("secret_dummy_block", SecretBaseBlock::new, srm().noLootTable());
    public static final Block SECRET_DAYLIGHT_DETECTOR = register("secret_daylight_detector", SecretDaylightDetector::new, srm());
    public static final Block SECRET_OBSERVER = register("secret_observer", SecretObserver::new, srm());
    public static final Block SECRET_CLAMBER = register("secret_clamber", SecretClamber::new, srm());

    private static BlockBehaviour.Properties srm() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE);
    }

    private static BlockBehaviour.Properties srmMetal() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).noCollision();
    }

    private static BlockBehaviour.Properties torch() {
        return BlockBehaviour.Properties.of().noCollision().instabreak().lightLevel(s -> 14).sound(SoundType.WOOD).pushReaction(PushReaction.POPPED);
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, SecretRooms7.id(name));
        Block block = Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
        ALL.add(block);
        return block;
    }

    public static List<Block> all() {
        return ALL;
    }

    public static void init() {
    }
}
