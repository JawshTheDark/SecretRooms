package com.wynprice.secretrooms.server;

import com.wynprice.secretrooms.SecretRooms7;
import com.wynprice.secretrooms.server.items.SecretItems;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SecretCreativeTab {

    public static final CreativeModeTab SECRETROOMS_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, SecretRooms7.id("secretrooms"),
        FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.secretroomsmod"))
            .icon(() -> new ItemStack(SecretItems.CAMOUFLAGE_PASTE))
            .displayItems((parameters, output) -> {
                for (Item item : SecretItems.all()) {
                    output.accept(item);
                }
            })
            .build()
    );

    public static void init() {
    }
}
