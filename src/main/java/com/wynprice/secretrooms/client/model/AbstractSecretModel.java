package com.wynprice.secretrooms.client.model;

import com.wynprice.secretrooms.client.TrueVisionClient;
import com.wynprice.secretrooms.server.tileentity.SecretTileEntity;
import com.wynprice.secretrooms.world.MirrorWorld;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadTransform;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The model of every secret block. It doesn't have any geometry of its own, it asks the model of the block that the
 * secret block is mirroring for its quads, and then (depending on the subclass) changes them.
 */
public abstract class AbstractSecretModel implements BlockStateModel {

    /**
     * How far above the surface of a block the true vision overlay is drawn, to stop it z-fighting.
     */
    private static final float OVERLAY_OFFSET = 0.001F;

    private final Material.Baked overlay;

    protected AbstractSecretModel(Material.Baked overlay) {
        this.overlay = overlay;
    }

    /**
     * Everything that is needed to work out the quads of a secret block.
     */
    protected record Context(
        QuadEmitter emitter,
        BlockAndTintGetter level,
        BlockAndTintGetter mirrorLevel,
        BlockPos pos,
        BlockState state,
        SecretTileEntity.RenderData data,
        RandomSource random,
        Predicate<@Nullable Direction> cullTest
    ) {
        public BlockState mirror() {
            return this.data.mirrorState();
        }

        public BlockStateModel mirrorModel() {
            return modelOf(this.mirror());
        }
    }

    protected static BlockStateModel modelOf(BlockState state) {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
    }

    protected static Optional<SecretTileEntity.RenderData> renderData(BlockAndTintGetter level, BlockPos pos) {
        if (level.getBlockEntityRenderData(pos) instanceof SecretTileEntity.RenderData data) {
            return Optional.of(data);
        }
        return Optional.empty();
    }

    /**
     * Adds the quads of this block to the emitter in the context.
     */
    protected abstract void emit(Context context);

    @Override
    public final void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        Optional<SecretTileEntity.RenderData> data = renderData(level, pos);
        if (data.isEmpty()) {
            return;
        }
        this.emit(new Context(emitter, level, MirrorWorld.wrap(level), pos, state, data.get(), random, cullTest));
    }

    /**
     * Collects all of the quads of the mirrored model into a mesh, after the tint of the mirrored block has been
     * applied to them.
     *
     * @param cullTest which faces should be left out, usually none of them so that the quads can be changed first
     */
    protected static MutableMesh collectMirror(Context context, Predicate<@Nullable Direction> cullTest) {
        MutableMesh mesh = Renderer.get().mutableMesh();
        QuadEmitter emitter = mesh.emitter();
        emitter.pushTransform(tint(context));
        context.mirrorModel().emitQuads(emitter, context.mirrorLevel(), context.pos(), context.mirror(), context.random(), cullTest);
        emitter.popTransform();
        return mesh;
    }

    /**
     * Tints use the block state of the block being rendered, which is the secret block. Because it doesn't know
     * anything about the mirrored block's colors, they are worked out here and baked into the quads.
     */
    protected static QuadTransform tint(Context context) {
        BlockState mirror = context.mirror();
        List<BlockTintSource> sources = Minecraft.getInstance().getBlockColors().getTintSources(mirror);
        int[] colors = new int[sources.size()];
        boolean[] computed = new boolean[sources.size()];
        return quad -> {
            int index = quad.tintIndex();
            if (index >= 0) {
                if (index < sources.size()) {
                    if (!computed[index]) {
                        colors[index] = sources.get(index).colorInWorld(mirror, context.level(), context.pos());
                        computed[index] = true;
                    }
                    quad.multiplyColor(colors[index]);
                }
                quad.tintIndex(-1);
            }
            return true;
        };
    }

    /**
     * Emits the given quads, and if the player can see the secret blocks, the overlay that shows where they are.
     */
    protected void emitWithOverlay(Context context, Consumer<Consumer<QuadView>> quads) {
        QuadEmitter emitter = context.emitter();
        boolean trueVision = TrueVisionClient.isWearingGoggles();
        quads.accept(quad -> {
            emitter.copyFrom(quad).emit();
            if (trueVision) {
                this.emitOverlay(emitter, quad);
            }
        });
    }

    protected void emitOverlay(QuadEmitter emitter, QuadView quad) {
        Direction face = quad.lightFace();
        emitter.copyFrom(quad);
        for (int i = 0; i < 4; i++) {
            emitter.pos(i, quad.x(i) + OVERLAY_OFFSET * face.getStepX(), quad.y(i) + OVERLAY_OFFSET * face.getStepY(), quad.z(i) + OVERLAY_OFFSET * face.getStepZ());
        }
        emitter.nominalFace(face);
        emitter.tintIndex(-1);
        emitter.color(-1, -1, -1, -1);
        emitter.materialBake(this.overlay, MutableQuadView.BAKE_LOCK_UV);
        emitter.chunkLayer(ChunkSectionLayer.TRANSLUCENT);
        emitter.emissive(true);
        emitter.emit();
    }

    // The vanilla way of getting the geometry of a model can't know where it is, so there is nothing to give it

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
    }

    @Override
    public Material.Baked particleMaterial() {
        return modelOf(Blocks.STONE.defaultBlockState()).particleMaterial();
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return BakedQuad.FLAG_TRANSLUCENT | BakedQuad.FLAG_ANIMATED;
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        Optional<SecretTileEntity.RenderData> data = renderData(level, pos);
        if (data.isEmpty()) {
            return this.particleMaterial();
        }
        return modelOf(data.get().mirrorState()).particleMaterial(MirrorWorld.wrap(level), pos, data.get().mirrorState());
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        Optional<SecretTileEntity.RenderData> data = renderData(level, pos);
        if (data.isEmpty()) {
            return 0;
        }
        BlockState mirror = data.get().mirrorState();
        int flags = modelOf(mirror).materialFlags(MirrorWorld.wrap(level), pos, mirror, random);
        if (TrueVisionClient.isWearingGoggles()) {
            flags |= BakedQuad.FLAG_TRANSLUCENT;
        }
        return this.adjustMaterialFlags(flags);
    }

    /**
     * Gives subclasses the chance to say that they will also emit quads of a different kind than the mirrored block.
     */
    protected @BakedQuad.MaterialFlags int adjustMaterialFlags(@BakedQuad.MaterialFlags int flags) {
        return flags;
    }
}
