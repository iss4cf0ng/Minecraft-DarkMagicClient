package com.myclient.mixin;

import com.myclient.MyClient;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "setup", at = @At("TAIL"))
    private void myclient$cameraOverride(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        if (MyClient.FreecamModule.isActive()) {
            Vec3 pos = MyClient.FreecamModule.getCamPos();
            this.setPosition(pos.x, pos.y, pos.z);
            this.setRotation(MyClient.FreecamModule.getCamYaw(), MyClient.FreecamModule.getCamPitch());
        } else if (MyClient.SpectateModule.isActive()) {
            LivingEntity target = MyClient.SpectateModule.getTargetEntity();
            if (target != null) {
                Vec3 eyePos = target.getEyePosition(partialTick);
                this.setPosition(eyePos.x, eyePos.y, eyePos.z);
                this.setRotation(target.getViewYRot(partialTick), target.getViewXRot(partialTick));
            }
        }
    }
}