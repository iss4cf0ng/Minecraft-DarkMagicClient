package com.myclient.mixin;

import com.myclient.MyClient;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class NoSlipMixin {

    @Inject(method = "getFriction", at = @At("RETURN"), cancellable = true)
    private void myclient$reduceIceSlip(CallbackInfoReturnable<Float> cir) {
        if (!MyClient.NoSlipModule.isActive()) return;

        float friction = cir.getReturnValueF();
        if (friction > 0.6F) {
            cir.setReturnValue(0.6F);
        }
    }
}