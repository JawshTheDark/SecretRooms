package com.wynprice.secretrooms.server.items;

import com.wynprice.secretrooms.SecretRooms7;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

/**
 * Worn on the head, and lets the wearer see the secret blocks. The rendering side of that is in the client package,
 * this just has to be something that can be worn.
 */
public class TrueVisionGoggles extends Item {

    public static final ResourceKey<EquipmentAsset> ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, SecretRooms7.id("true_vision_goggles"));

    public TrueVisionGoggles(Properties properties) {
        super(properties);
    }

    public static Properties properties() {
        return new Properties()
            .durability(900)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD)
                .setEquipSound(SoundEvents.ARMOR_EQUIP_CHAIN)
                .setAsset(ASSET)
                .build()
            );
    }
}
