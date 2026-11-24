package com.myclient.module;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.phys.Vec3;

public class UnlimitedVoid {

    private static final Random RANDOM = new Random();

    public static void triggerForPlayer(Minecraft mc, String targetName, int count, double spreadRadius) {
        if (mc == null || mc.player == null) {
            return;
        }

        MinecraftServer server = mc.getSingleplayerServer();
        if (server == null) {
            mc.player.displayClientMessage(
                    Component.literal("§c[MyClient] Only works on integrated server in this test setup."),
                    false
            );
            return;
        }

        int safeCount = Math.max(1, Math.min(count, 10000));
        double clampedRadius = Math.max(0.01, Math.min(spreadRadius, 0.5));

        server.execute(() -> {
            ServerPlayer target = server.getPlayerList().getPlayerByName(targetName);
            if (target == null || !(target.level() instanceof ServerLevel serverLevel)) {
                mc.execute(() -> {
                    if (mc.player != null) {
                        mc.player.displayClientMessage(
                                Component.literal("§c[MyClient] Target not found: §f" + targetName),
                                false
                        );
                    }
                });
                return;
            }

            Vec3 basePos = target.position();
            int spawned = 0;

            for (int i = 0; i < safeCount; i++) {
                double dx = (RANDOM.nextDouble() * 2.0 - 1.0) * clampedRadius;
                double dy = RANDOM.nextDouble() * 0.1;
                double dz = (RANDOM.nextDouble() * 2.0 - 1.0) * clampedRadius;

                Arrow arrow = EntityType.ARROW.create(serverLevel, EntitySpawnReason.COMMAND);
                if (arrow != null) {
                    arrow.setPos(basePos.x + dx, basePos.y + dy, basePos.z + dz);
                    arrow.setDeltaMovement(0.0, -0.01, 0.0);
                    arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                    serverLevel.addFreshEntity(arrow);
                    spawned++;
                }
            }

            final int finalSpawned = spawned;
            mc.execute(() -> {
                if (mc.player != null) {
                    mc.player.displayClientMessage(
                            Component.literal("§b[MyClient] §fUnlimitedVoid -> §e" + targetName + " §f(§e" + finalSpawned + "§f)"),
                            false
                    );
                }
            });
        });
    }
}