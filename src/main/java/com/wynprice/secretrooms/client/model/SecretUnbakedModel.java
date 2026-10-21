package com.wynprice.secretrooms.client.model;

import com.mojang.serialization.MapCodec;
import com.wynprice.secretrooms.SecretRooms7;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

/**
 * Lets the blockstate files of the secret blocks say which of the secret models they use:
 * <pre>{@code
 * { "variants": { "": { "fabric:type": "secretroomsmod:secret_block" } } }
 * }</pre>
 */
public record SecretUnbakedModel(Kind kind) implements CustomUnbakedBlockStateModel {

    private static final Identifier OVERLAY = SecretRooms7.id("block/overlay");
    private static final Identifier GLASS = Identifier.withDefaultNamespace("block/glass");

    public enum Kind {
        SECRET_BLOCK("secret_block", (overlay, glass) -> new SecretBlockStateModel(overlay)),
        MAPPED("secret_mapped_model", (overlay, glass) -> new SecretMappedBlockStateModel(overlay)),
        ONE_WAY_GLASS("one_way_glass", OneWayGlassBlockStateModel::new);

        private final String id;
        private final MapCodec<SecretUnbakedModel> codec;
        private final ModelFactory factory;

        Kind(String id, ModelFactory factory) {
            this.id = id;
            this.codec = MapCodec.unit(() -> new SecretUnbakedModel(this));
            this.factory = factory;
        }

        interface ModelFactory {
            BlockStateModel create(Material.Baked overlay, Material.Baked glass);
        }
    }

    public static void register() {
        for (Kind kind : Kind.values()) {
            CustomUnbakedBlockStateModel.register(SecretRooms7.id(kind.id), kind.codec);
        }
    }

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return this.kind.codec;
    }

    @Override
    public BlockStateModel bake(ModelBaker baker) {
        Function<Identifier, Material.Baked> materials = id -> baker.materials().get(new Material(id), () -> "secret rooms model");
        return this.kind.factory.create(materials.apply(OVERLAY), materials.apply(GLASS));
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
    }
}
