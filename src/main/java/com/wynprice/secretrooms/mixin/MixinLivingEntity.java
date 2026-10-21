package com.wynprice.secretrooms.mixin;

import com.wynprice.secretrooms.server.blocks.SecretTrapdoor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Shadow
    private Optional<BlockPos> lastClimbablePos;

    /**
     * Vanilla only lets you climb an open trapdoor if it is a real trapdoor, so this lets secret trapdoors do it too.
     */
    @Inject(method = "onClimbable", at = @At("RETURN"), cancellable = true)
    private void secretrooms$climbSecretTrapdoor(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            return;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        BlockState state = self.getInBlockState();
        BlockPos pos = self.blockPosition();
        if (!self.isSpectator() && state.getBlock() instanceof SecretTrapdoor && SecretTrapdoor.isLadder(state, self.level(), pos)) {
            this.lastClimbablePos = Optional.of(pos);
            cir.setReturnValue(true);
        }
    }
}
