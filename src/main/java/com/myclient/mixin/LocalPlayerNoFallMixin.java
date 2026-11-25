package com.myclient.mixin;

import com.myclient.MyClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LocalPlayer.class)
public class LocalPlayerNoFallMixin {

    @ModifyArg(
            method = "sendPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"
            ),
            index = 0,
            require = 0
    )
    private Packet<?> myclient$spoofMovePacket(Packet<?> packet) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) {
            return packet;
        }
        if (!MyClient.isNoFallEnabled()) {
            return packet;
        }

        LocalPlayer player = (LocalPlayer) (Object) this;
        if (player.isFallFlying() || player.isCreative() || player.isSpectator()) {
            return packet;
        }

        if (!player.onGround() && player.getDeltaMovement().y < -0.08) {
            try {
                double x = move.getX(player.getX());
                double y = move.getY(player.getY());
                double z = move.getZ(player.getZ());
                float yRot = move.getYRot(player.getYRot());
                float xRot = move.getXRot(player.getXRot());
                boolean horiz = move.horizontalCollision();

                if (move instanceof ServerboundMovePlayerPacket.PosRot) {
                    return new ServerboundMovePlayerPacket.PosRot(x, y, z, yRot, xRot, true, horiz);
                } else if (move instanceof ServerboundMovePlayerPacket.Pos) {
                    return new ServerboundMovePlayerPacket.Pos(x, y, z, true, horiz);
                } else if (move instanceof ServerboundMovePlayerPacket.Rot) {
                    return new ServerboundMovePlayerPacket.Rot(yRot, xRot, true, horiz);
                } else if (move instanceof ServerboundMovePlayerPacket.StatusOnly) {
                    return new ServerboundMovePlayerPacket.StatusOnly(true, horiz);
                }
            } catch (Throwable ignored) {
            }
        }
        return packet;
    }
}