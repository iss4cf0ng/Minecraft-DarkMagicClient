package com.myclient.mixin;

import com.myclient.MyClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void onPlayerGetDisplayName(CallbackInfoReturnable<Component> cir) {
        if (!MyClient.isModuleEnabled("NameSpoofer")) return;

        Player self = (Player) (Object) this;
        Minecraft mc = Minecraft.getInstance();

        if (self == mc.player) {
            cir.setReturnValue(MyClient.NameSpooferModule.getSpoofedComponent());
        }
    }
}
