package com.myclient.mixin;

import com.myclient.MyClient;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class NoDizzyMixin {

    @Shadow
    public float oSpinningEffectIntensity;

    @Shadow
    public float spinningEffectIntensity;

    @Inject(method = "handleNausea", at = @At("HEAD"), cancellable = true)
    private void myclient$disableNauseaEffect(CallbackInfo ci) {
        if (!MyClient.NoDizzyModule.isActive()) return;

        this.oSpinningEffectIntensity = 0.0F;
        this.spinningEffectIntensity = 0.0F;
        ci.cancel();
    }
}