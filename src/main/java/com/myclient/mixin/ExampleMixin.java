package com.myclient.mixin;

import com.myclient.MyClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class ExampleMixin {

	@Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
	private void onIsCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
		if (!MyClient.isModuleEnabled("GlowESP")) {
			return;
		}

		Entity self = (Entity) (Object) this;
		Minecraft mc = Minecraft.getInstance();

		if (self instanceof LivingEntity && self != mc.player) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
	private void onGetDisplayName(CallbackInfoReturnable<Component> cir) {
		if (!MyClient.isModuleEnabled("NameSpoofer")) {
			return;
		}

		Entity self = (Entity) (Object) this;
		Minecraft mc = Minecraft.getInstance();

		if (self instanceof Player && self == mc.player) {
			String[] colors = {"§a", "§b", "§c", "§d", "§e", "§2", "§3"};
			String randomColor = colors[(int) (Math.random() * colors.length)];

			int length = 6 + (int) (Math.random() * 9);
			StringBuilder scrambled = new StringBuilder();
			for (int i = 0; i != length; i++) {
				char randomChar = (char) ('A' + (int) (Math.random() * 26));
				scrambled.append(randomChar);
			}

			String hackerTag = "§8[" + randomColor + "§k" + scrambled + "§r§8]";
			cir.setReturnValue(Component.literal(hackerTag));
		}
	}
}
