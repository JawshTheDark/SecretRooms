package com.wynprice.secretrooms.server.items;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

public class TrueVisionGogglesHandler {

    private static final ResourceKey<LootTable> END_CHEST_LOOT = BuiltInLootTables.END_CITY_TREASURE;

    public static final float GOGGLES_BREAK_CHANCE = 0.2F;

    public static void init() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (END_CHEST_LOOT.equals(key)) {
                tableBuilder.withPool(LootPool.lootPool()
                    .add(LootItem.lootTableItem(SecretItems.TRUE_VISION_GOGGLES)
                        .when(LootItemRandomChanceCondition.randomChance(0.1f))
                    )
                );
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.tickCount % 20 == 0 && player.getRandom().nextFloat() < GOGGLES_BREAK_CHANCE) {
                    ItemStack stack = player.getItemBySlot(EquipmentSlot.HEAD);
                    if (stack.getItem() == SecretItems.TRUE_VISION_GOGGLES) {
                        stack.hurtAndBreak(1, player, EquipmentSlot.HEAD);
                    }
                }
            }
        });
    }
}
