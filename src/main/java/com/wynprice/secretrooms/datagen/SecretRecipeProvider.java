package com.wynprice.secretrooms.datagen;

import com.wynprice.secretrooms.SecretRooms7;
import com.wynprice.secretrooms.server.data.SecretItemTags;
import com.wynprice.secretrooms.server.items.SecretItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

import java.util.concurrent.CompletableFuture;

import static com.wynprice.secretrooms.server.items.SecretItems.CAMOUFLAGE_PASTE;
import static com.wynprice.secretrooms.server.items.SecretItems.SWITCH_PROBE;

public class SecretRecipeProvider extends FabricRecipeProvider {

    public SecretRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public String getName() {
        return "Secret Rooms Recipes";
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements) {
        return new RecipeProvider(recipes, advancements) {
            @Override
            public void buildRecipes() {
                Item camoPaste = CAMOUFLAGE_PASTE;
                shaped(RecipeCategory.MISC, SecretItems.TORCH_LEVER).define('T', Items.TORCH).define('L', Items.LEVER).pattern("T").pattern("L").group("torch_lever.json").unlockedBy("has_torch_lever", hasItems(Items.TORCH, Items.LEVER)).save(output);
                shaped(RecipeCategory.MISC, camoPaste, 9).define('X', ConventionalItemTags.DYES).define('#', Items.CLAY_BALL).pattern("XXX").pattern("X#X").pattern("XXX").group("camouflage_paste").unlockedBy("has_earth_item", has(SecretItemTags.EARTH_ITEM)).save(output);
                shaped(RecipeCategory.MISC, SecretItems.GHOST_BLOCK, 4).define('X', camoPaste).define('0', SecretItemTags.SECRET_RECIPE_ITEMS).pattern("X0X").pattern("0 0").pattern("X0X").group("ghost_block").unlockedBy("has_camo", has(camoPaste)).save(output);
                shaped(RecipeCategory.MISC, SecretItems.ONE_WAY_GLASS, 9).define('X', camoPaste).define('0', SecretItemTags.CLEAR_GLASS).pattern("XXX").pattern("000").pattern("000").group("one_way_glass").unlockedBy("has_camo", has(camoPaste)).save(output, SecretRooms7.id("one_way_glass_up").toString());
                shaped(RecipeCategory.MISC, SecretItems.ONE_WAY_GLASS, 9).define('X', camoPaste).define('0', SecretItemTags.CLEAR_GLASS).pattern("000").pattern("000").pattern("XXX").group("one_way_glass").unlockedBy("has_camo", has(camoPaste)).save(output, SecretRooms7.id("one_way_glass_down").toString());
                shaped(RecipeCategory.MISC, SecretItems.ONE_WAY_GLASS, 9).define('X', camoPaste).define('0', SecretItemTags.CLEAR_GLASS).pattern("X00").pattern("X00").pattern("X00").group("one_way_glass").unlockedBy("has_camo", has(camoPaste)).save(output, SecretRooms7.id("one_way_glass_left").toString());
                shaped(RecipeCategory.MISC, SecretItems.ONE_WAY_GLASS, 9).define('X', camoPaste).define('0', SecretItemTags.CLEAR_GLASS).pattern("00X").pattern("00X").pattern("00X").group("one_way_glass").unlockedBy("has_camo", has(camoPaste)).save(output, SecretRooms7.id("one_way_glass_right").toString());
                shaped(RecipeCategory.MISC, SecretItems.SECRET_GATE).define('X', ItemTags.PLANKS).define('0', camoPaste).define('R', Items.REDSTONE).define('A', Items.ENDER_PEARL).pattern("X0X").pattern("0A0").pattern("XRX").group("secret_gate").unlockedBy("has_camo", has(camoPaste)).save(output);

                camo(SWITCH_PROBE, "switch_probe", Items.REDSTONE_TORCH);
                shapeless(RecipeCategory.MISC, SecretItems.ONE_WAY_GLASS).requires(camoPaste).requires(SecretItemTags.CLEAR_GLASS).group("one_way_glass_shapeless").unlockedBy("has_camo", has(camoPaste)).save(output);
                camo(SecretItems.SECRET_CHEST, "secret_chest", Items.CHEST);
                camo(SecretItems.SECRET_PRESSURE_PLATE, "secret_pressure_plate", ItemTags.WOODEN_PRESSURE_PLATES);
                camo(SecretItems.SECRET_PLAYER_PRESSURE_PLATE, "secret_player_pressure_plate", Items.STONE_PRESSURE_PLATE);
                camo(SecretItems.SECRET_DOOR, "secret_door", ItemTags.WOODEN_DOORS);
                camo(SecretItems.SECRET_IRON_DOOR, "secret_iron_door", Items.IRON_DOOR);
                camo(SecretItems.SECRET_LEVER, "secret_lever", Items.LEVER);
                camo(SecretItems.SECRET_DAYLIGHT_DETECTOR, "secret_daylight_detector", Items.DAYLIGHT_DETECTOR);
                camo(SecretItems.SECRET_REDSTONE, "secret_redstone", Items.REDSTONE);
                camo(SecretItems.SECRET_STAIRS, "secret_stairs", BlockItemTags.STAIRS.item());
                camo(SecretItems.SECRET_WOODEN_BUTTON, "secret_wooden_button", ItemTags.WOODEN_BUTTONS);
                camo(SecretItems.SECRET_STONE_BUTTON, "secret_stone_button", Items.STONE_BUTTON);
                camo(SecretItems.SECRET_TRAPPED_CHEST, "secret_trapped_chest", Items.TRAPPED_CHEST);
                camo(SecretItems.SECRET_TRAPDOOR, "secret_trapdoor", ItemTags.WOODEN_TRAPDOORS);
                camo(SecretItems.SECRET_IRON_TRAPDOOR, "secret_iron_trapdoor", Items.IRON_TRAPDOOR);
                camo(SecretItems.SECRET_OBSERVER, "secret_observer", Items.OBSERVER);
                camo(SecretItems.SECRET_CLAMBER, "secret_clamber", Items.LADDER);
            }

            private void camo(ItemLike result, String group, ItemLike ingredient) {
                shapeless(RecipeCategory.MISC, result).requires(CAMOUFLAGE_PASTE).requires(ingredient).group(group).unlockedBy("has_camo", has(CAMOUFLAGE_PASTE)).save(output);
            }

            private void camo(ItemLike result, String group, net.minecraft.tags.TagKey<Item> ingredient) {
                shapeless(RecipeCategory.MISC, result).requires(CAMOUFLAGE_PASTE).requires(ingredient).group(group).unlockedBy("has_camo", has(CAMOUFLAGE_PASTE)).save(output);
            }

            private Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(ItemLike... items) {
                ItemPredicate.Builder[] predicates = new ItemPredicate.Builder[items.length];
                for (int i = 0; i < items.length; i++) {
                    predicates[i] = ItemPredicate.Builder.item().of(registries.lookupOrThrow(net.minecraft.core.registries.Registries.ITEM), items[i]);
                }
                return inventoryTrigger(predicates);
            }
        };
    }
}
