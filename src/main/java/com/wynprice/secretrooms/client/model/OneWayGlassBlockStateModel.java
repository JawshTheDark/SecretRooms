package com.wynprice.secretrooms.client.model;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The sides of the block that are "glass" show up as glass, and the other sides look like the block that is being
 * mirrored.
 */
public class OneWayGlassBlockStateModel extends AbstractSecretModel {

    private final Material.Baked glass;

    public OneWayGlassBlockStateModel(Material.Baked overlay, Material.Baked glass) {
        super(overlay);
        this.glass = glass;
    }

    @Override
    protected void emit(Context context) {
        BlockState state = context.state();
        MutableMesh mesh = collectMirror(context, direction -> false);
        this.emitWithOverlay(context, sink -> mesh.forEachMutable(quad -> {
            Direction face = quad.lightFace();
            if (state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(face))) {
                this.makeGlass(quad, face);
            }
            sink.accept(quad);
        }));
    }

    private void makeGlass(MutableQuadView quad, Direction face) {
        quad.tintIndex(-1);
        quad.color(-1, -1, -1, -1);
        quad.nominalFace(face);
        quad.materialBake(this.glass, MutableQuadView.BAKE_LOCK_UV);
    }
}
