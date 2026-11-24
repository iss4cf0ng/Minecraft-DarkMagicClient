package com.myclient.mixin;

import com.myclient.MyClient;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LightTexture.class)
public class LightTextureMixin {

    @ModifyVariable(method = "updateLightTexture", at = @At("STORE"), ordinal = 1, require = 0)
    private float onUpdateLightTextureGamma(float originalGamma) {
        if (MyClient.isModuleEnabled("Fullbright")) {
            return 15.0F;
        }
        return originalGamma;
    }

    @ModifyArg(
            method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/NativeImage;setPixel(III)V"
            ),
            index = 2,
            require = 0
    )
    private int onSetLightPixelColor(int argb) {
        if (!MyClient.isModuleEnabled("Fullbright")) {
            return argb;
        }

        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;

        int floor = 230;
        int nr = Math.max(r, floor);
        int ng = Math.max(g, floor);
        int nb = Math.max(b, floor);

        return (a << 24) | (nr << 16) | (ng << 8) | nb;
    }
}