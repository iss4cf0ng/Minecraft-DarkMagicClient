package com.myclient.mixin;

import com.myclient.MyClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class SafeWalkMixin {

    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3 myclient$safeWalkClamp(Vec3 movement, MoverType type) {
        if (type != MoverType.SELF) {
            return movement;
        }

        Entity self = (Entity) (Object) this;
        if (!(self instanceof LocalPlayer player)) {
            return movement;
        }

        if (!MyClient.isModuleEnabled("SafeWalk")) {
            return movement;
        }

        if (!player.onGround() || player.isShiftKeyDown() || Minecraft.getInstance().options.keyJump.isDown()) {
            return movement;
        }

        Level level = player.level();
        double feetY = player.getY();

        double minY = feetY - 1.05;
        double maxY = feetY - 0.05;

        double dx = movement.x;
        double dz = movement.z;

        if (Math.abs(dx) > 1.0e-6) {
            double edgeX = player.getX() + dx + Math.copySign(0.30, dx);
            AABB probeX = new AABB(
                    edgeX - 0.04, minY, player.getZ() - 0.25,
                    edgeX + 0.04, maxY, player.getZ() + 0.25
            );
            if (level.noCollision(player, probeX)) {
                dx = 0.0;
            }
        }

        if (Math.abs(dz) > 1.0e-6) {
            double edgeZ = player.getZ() + dz + Math.copySign(0.30, dz);
            AABB probeZ = new AABB(
                    player.getX() - 0.25, minY, edgeZ - 0.04,
                    player.getX() + 0.25, maxY, edgeZ + 0.04
            );
            if (level.noCollision(player, probeZ)) {
                dz = 0.0;
            }
        }

        if (dx != movement.x || dz != movement.z) {
            Vec3 vel = player.getDeltaMovement();
            player.setDeltaMovement(
                    dx == 0.0 ? 0.0 : vel.x,
                    vel.y,
                    dz == 0.0 ? 0.0 : vel.z
            );
            return new Vec3(dx, movement.y, dz);
        }

        return movement;
    }
}