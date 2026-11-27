package com.myclient.mixin;

import com.myclient.MyClient;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public abstract class NoSlowMixin {
    @Inject(method = "aiStep", at = @At("TAIL"))
    private void myclient$noSlowOnUseItem(CallbackInfo ci) {
        if (!MyClient.NoSlowModule.isActive())
            return;

        LocalPlayer player = (LocalPlayer) (Object) this;
        if (player.isUsingItem() && !player.isPassenger()) {
            player.input.leftImpulse /= 0.2F;
            player.input.forwardImpulse /= 0.2F;

            player.input.leftImpulse = Math.max(-1.0F, Math.min(1.0F, player.input.leftImpulse));
            player.input.forwardImpulse = Math.max(-1.0F, Math.min(1.0F, player.input.forwardImpulse));
        }
    }
}
