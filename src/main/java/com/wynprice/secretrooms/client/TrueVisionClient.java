package com.wynprice.secretrooms.client;

import com.wynprice.secretrooms.server.items.SecretItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * Keeps track of whether the local player is wearing the goggles of true vision. While they are, secret blocks are
 * drawn with an overlay, so the chunks have to be rebuilt whenever the player puts them on or takes them off.
 */
public class TrueVisionClient {

    private static volatile boolean wearing;

    /**
     * Safe to call from the chunk building threads.
     */
    public static boolean isWearingGoggles() {
        return wearing;
    }

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        ClientLevel level = client.level;
        boolean nowWearing = player != null && level != null && player.getItemBySlot(EquipmentSlot.HEAD).getItem() == SecretItems.TRUE_VISION_GOGGLES;
        if (nowWearing != wearing) {
            wearing = nowWearing;
            client.levelExtractor.allChanged();
        }
    }
}
