package com.myclient.mixin;

import com.myclient.MyClient;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LightTexture.class)
public class LightTextureMixin {

    @ModifyVariable(method = "updateLightTexture", at = @At("STORE"), ordinal = 1)
    private float onUpdateLightTextureGamma(float originalGamma) {
        if (MyClient.isModuleEnabled("Fullbright")) {
            return 15.0F;
        }
        return originalGamma;
    }
}
