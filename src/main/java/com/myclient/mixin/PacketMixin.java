package com.myclient.mixin;

import com.myclient.MyClient;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class PacketMixin {

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;Z)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, PacketSendListener listener, boolean flush, CallbackInfo ci) {
        if (MyClient.isModuleEnabled("Blink") && !MyClient.BlinkModule.isFlushing) {
            if (packet instanceof ServerboundMovePlayerPacket movePacket) {
                MyClient.BlinkModule.packetQueue.add(movePacket);
                ci.cancel();
            }
        }
    }
}
