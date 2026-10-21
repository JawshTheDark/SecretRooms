package com.wynprice.secretrooms.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class SecretDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(SecretBlockTagsProvider::new);
        pack.addProvider(SecretItemTagsProvider::new);
        pack.addProvider(SecretRecipeProvider::new);
        pack.addProvider(SecretBlockLootTableProvider::new);
    }
}
