package com.myclient.mixin;

import com.myclient.MyClient;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.ClientLevelData.class)
public abstract class ClientLevelTimeMixin {
    @Inject(method = "getDayTime", at = @At("HEAD"), cancellable = true)
    private void myclient$lockDayTime(CallbackInfoReturnable<Long> cir) {
        if (MyClient.isModuleEnabled("Day")) {
            cir.setReturnValue(6000L);
        }
    }
}
