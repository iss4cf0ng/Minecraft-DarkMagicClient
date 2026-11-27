package com.myclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.shaders.FogShape;
import com.myclient.MyClient;
import java.lang.reflect.Constructor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FogRenderer.class)
public class WaterVisionMixin {

    @ModifyReturnValue(method = "setupFog", at = @At("RETURN"), require = 0)
    private static FogParameters onSetupFogReturnValue(FogParameters fog) {
        if (fog == null || !MyClient.isModuleEnabled("WaterVision")) {
            return fog;
        }

        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer != null ? mc.gameRenderer.getMainCamera() : null;
        if (mc.player == null || mc.level == null || camera == null) {
            return fog;
        }

        if (camera.getFluidInCamera() == FogType.WATER || mc.player.isEyeInFluid(FluidTags.WATER)) {
            FogParameters updated = createAdjustedWaterFog(fog);
            if (updated != null) {
                return updated;
            }
        }

        return fog;
    }

    private static FogParameters createAdjustedWaterFog(FogParameters original) {
        try {
            float red = invokeFloatMethod(original, "red", 0.05F);
            float green = invokeFloatMethod(original, "green", 0.15F);
            float blue = invokeFloatMethod(original, "blue", 0.35F);
            float alpha = invokeFloatMethod(original, "alpha", 1.0F);

            float newStart = 48.0F;
            float newEnd = 256.0F;
            FogShape newShape = FogShape.CYLINDER;
            float newAlpha = Math.min(alpha, 0.35F);

            for (Constructor<?> ctor : FogParameters.class.getDeclaredConstructors()) {
                Class<?>[] params = ctor.getParameterTypes();
                if (params.length == 7
                        && params[0] == float.class
                        && params[1] == float.class
                        && params[2] == FogShape.class
                        && params[3] == float.class
                        && params[4] == float.class
                        && params[5] == float.class
                        && params[6] == float.class) {
                    ctor.setAccessible(true);

                    return (FogParameters) ctor.newInstance(newStart, newEnd, newShape, red, green, blue, newAlpha);
                }
            }
        } catch (ReflectiveOperationException ignored) {

        }

        return null;
    }

    private static float invokeFloatMethod(Object target, String methodName, float fallback) {
        try {
            var m = target.getClass().getDeclaredMethod(methodName);
            m.setAccessible(true);
            Object val = m.invoke(target);
            if (val instanceof Float f) {
                return f;
            }
        } catch (ReflectiveOperationException ignored) {

        }

        return fallback;
    }
}