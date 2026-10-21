package com.wynprice.secretrooms;

import com.wynprice.secretrooms.server.SecretCreativeTab;
import com.wynprice.secretrooms.server.blocks.SecretBlocks;
import com.wynprice.secretrooms.server.items.SecretItems;
import com.wynprice.secretrooms.server.items.TrueVisionGogglesHandler;
import com.wynprice.secretrooms.server.tileentity.SecretTileEntities;
import net.fabricmc.api.ModInitializer;

public class SecretRoomsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        SecretBlocks.init();
        SecretItems.init();
        SecretTileEntities.init();
        SecretCreativeTab.init();
        TrueVisionGogglesHandler.init();
    }
}
