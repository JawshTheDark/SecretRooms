package com.wynprice.secretrooms.mixin;

import com.wynprice.secretrooms.server.blocks.SecretBaseBlock;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevel {

    /**
     * The particles and the sound made while mining a block come from the block that is being looked at. For a secret
     * block that should be the block that it is mirroring, not the secret block itself.
     */
    @ModifyVariable(method = "addBreakingBlockEffects", at = @At("STORE"), ordinal = 0)
    private BlockState secretrooms$mirrorBreakingEffects(BlockState state, BlockPos pos, Direction direction, boolean playSound) {
        if (state.getBlock() instanceof SecretBaseBlock) {
            return SecretBaseBlock.getMirrorState((ClientLevel) (Object) this, pos).orElse(state);
        }
        return state;
    }
}
