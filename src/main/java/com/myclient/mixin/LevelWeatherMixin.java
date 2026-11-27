package com.myclient.mixin;

import com.myclient.MyClient;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelWeatherMixin {
    @Inject(method = "getRainLevel", at = @At("HEAD"), cancellable = true)
    private void myclient$noRain(float partialTick, CallbackInfoReturnable<Float> cir) {
        Level self = (Level)(Object)this;
        if (self.isClientSide && MyClient.isModuleEnabled("NoWeather")) {
            cir.setReturnValue(0.0F);
        }
    }

    @Inject(method = "getThunderLevel", at = @At("HEAD"), cancellable = true)
    private void myclient$noThunder(float partialTick, CallbackInfoReturnable<Float> cir) {
        Level self = (Level)(Object)this;
        if (self.isClientSide && MyClient.isModuleEnabled("NoWeather")) {
            cir.setReturnValue(0.0F);
        }
    }
}
