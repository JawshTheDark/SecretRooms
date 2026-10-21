package com.wynprice.secretrooms.server.items;

import com.wynprice.secretrooms.SecretRooms7;
import com.wynprice.secretrooms.server.blocks.SecretBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SecretItems {

    private static final List<Item> ALL = new ArrayList<>();

    public static final Item GHOST_BLOCK = block("ghost_block", SecretBlocks.GHOST_BLOCK, SecretBlockItem::new);
    public static final Item SECRET_STAIRS = block("secret_stairs", SecretBlocks.SECRET_STAIRS, SecretBlockItem::new);
    public static final Item SECRET_LEVER = block("secret_lever", SecretBlocks.SECRET_LEVER, SecretBlockItem::new);
    public static final Item SECRET_REDSTONE = block("secret_redstone", SecretBlocks.SECRET_REDSTONE, SecretBlockItem::new);
    public static final Item ONE_WAY_GLASS = block("one_way_glass", SecretBlocks.ONE_WAY_GLASS, SecretBlockItem::new);
    public static final Item SECRET_WOODEN_BUTTON = block("secret_wooden_button", SecretBlocks.SECRET_WOODEN_BUTTON, SecretBlockItem::new);
    public static final Item SECRET_STONE_BUTTON = block("secret_stone_button", SecretBlocks.SECRET_STONE_BUTTON, SecretBlockItem::new);
    public static final Item TORCH_LEVER = register("torch_lever", p -> new StandingAndWallBlockItem(SecretBlocks.TORCH_LEVER, SecretBlocks.WALL_TORCH_LEVER, Direction.DOWN, p.useBlockDescriptionPrefix()));
    public static final Item SECRET_PRESSURE_PLATE = block("secret_pressure_plate", SecretBlocks.SECRET_PRESSURE_PLATE, SecretBlockItem::new);
    public static final Item SECRET_PLAYER_PRESSURE_PLATE = block("secret_player_pressure_plate", SecretBlocks.SECRET_PLAYER_PRESSURE_PLATE, SecretBlockItem::new);
    public static final Item SECRET_DOOR = block("secret_door", SecretBlocks.SECRET_DOOR, SecretDoubleBlockItem::new);
    public static final Item SECRET_IRON_DOOR = block("secret_iron_door", SecretBlocks.SECRET_IRON_DOOR, SecretDoubleBlockItem::new);
    public static final Item SECRET_CHEST = block("secret_chest", SecretBlocks.SECRET_CHEST, SecretBlockItem::new);
    public static final Item SECRET_TRAPDOOR = block("secret_trapdoor", SecretBlocks.SECRET_TRAPDOOR, SecretBlockItem::new);
    public static final Item SECRET_IRON_TRAPDOOR = block("secret_iron_trapdoor", SecretBlocks.SECRET_IRON_TRAPDOOR, SecretBlockItem::new);
    public static final Item SECRET_TRAPPED_CHEST = block("secret_trapped_chest", SecretBlocks.SECRET_TRAPPED_CHEST, SecretBlockItem::new);
    public static final Item SECRET_GATE = block("secret_gate", SecretBlocks.SECRET_GATE, SecretBlockItem::new);
    public static final Item SECRET_DUMMY_BLOCK = block("secret_dummy_block", SecretBlocks.SECRET_DUMMY_BLOCK, SecretBlockItem::new);
    public static final Item SECRET_DAYLIGHT_DETECTOR = block("secret_daylight_detector", SecretBlocks.SECRET_DAYLIGHT_DETECTOR, SecretBlockItem::new);
    public static final Item SECRET_OBSERVER = block("secret_observer", SecretBlocks.SECRET_OBSERVER, SecretBlockItem::new);
    public static final Item SECRET_CLAMBER = block("secret_clamber", SecretBlocks.SECRET_CLAMBER, SecretBlockItem::new);

    public static final Item CAMOUFLAGE_PASTE = register("camouflage_paste", Item::new);
    public static final Item SWITCH_PROBE = register("switch_probe", SwitchProbe::new);

    public static final Item TRUE_VISION_GOGGLES = register("true_vision_goggles", TrueVisionGoggles::new, TrueVisionGoggles.properties());

    private static Item block(String name, Block block, BiFunction<Block, Item.Properties, Item> factory) {
        return register(name, p -> factory.apply(block, p.useBlockDescriptionPrefix().requiredFeatures(block.requiredFeatures())));
    }

    private static Item register(String name, Function<Item.Properties, Item> factory) {
        return register(name, factory, new Item.Properties());
    }

    private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, SecretRooms7.id(name));
        Item item = factory.apply(properties.setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, item);
        ALL.add(item);
        return item;
    }

    public static List<Item> all() {
        return ALL;
    }

    public static void init() {
    }
}
