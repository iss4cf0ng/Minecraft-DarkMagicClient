package com.myclient.mixin;

import com.myclient.MyClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class NoBlindMixin {

    @Inject(method = "hasEffect", at = @At("HEAD"), cancellable = true)
    private void myclient$ignoreBlindness(Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof LocalPlayer)) {
            return;
        }
        if (!MyClient.isModuleEnabled("NoBlind")) {
            return;
        }

        if (effect.is(MobEffects.BLINDNESS) || effect.is(MobEffects.DARKNESS)) {
            cir.setReturnValue(false);
        }
    }
}