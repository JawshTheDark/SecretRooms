package com.wynprice.secretrooms.client.model;

import com.wynprice.secretrooms.client.TrueVisionClient;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.resources.model.sprite.Material;

/**
 * Looks exactly like the block that it is mirroring.
 */
public class SecretBlockStateModel extends AbstractSecretModel {

    public SecretBlockStateModel(Material.Baked overlay) {
        super(overlay);
    }

    @Override
    protected void emit(Context context) {
        if (!TrueVisionClient.isWearingGoggles()) {
            QuadEmitter emitter = context.emitter();
            emitter.pushTransform(tint(context));
            try {
                context.mirrorModel().emitQuads(emitter, context.mirrorLevel(), context.pos(), context.mirror(), context.random(), context.cullTest());
            } finally {
                emitter.popTransform();
            }
        } else {
            MutableMesh mesh = collectMirror(context, context.cullTest());
            this.emitWithOverlay(context, mesh::forEach);
        }
    }
}
