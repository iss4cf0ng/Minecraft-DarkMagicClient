package com.myclient;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BowItem;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.BlockItem;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MyClient implements ModInitializer {
	public static final String MOD_ID = "myclient";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final List<Module> modules = new ArrayList<>();
	private static KeyMapping openGuiKey;

	public static boolean isModuleEnabled(String moduleName) {
		for (Module m : modules) {
			if (m.getName().equalsIgnoreCase(moduleName)) {
				return m.isEnabled();
			}
		}
		return false;
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing...");

		// Modules
		modules.add(new GlowEspModule());
		modules.add(new TracersModule());
		modules.add(new AutoSprintModule());
		modules.add(new XRayModule());
		modules.add(new MachineGunBowModule());
		modules.add(new ShotgunBowModule());
		modules.add(new PotionMachineGunModule());
		modules.add(new KillAuraModule());
		modules.add(new TpAuraModule());
		modules.add(new BlinkAuraModule());
		modules.add(new MultiAuraModule());
		modules.add(new FireballCannonModule());
		modules.add(new WitherBarrageModule());
		modules.add(new HealthTagsModule());
		modules.add(new MinimapModule());
		modules.add(new AreaMinerModule());
		modules.add(new MegaLighterModule());
		modules.add(new TntLaserModule());
		modules.add(new JesusModule());
		modules.add(new NoFallModule());
		modules.add(new StepModule());
		modules.add(new SpeedModule());
		modules.add(new SpiderModule());
		modules.add(new HighJumpModule());
		modules.add(new GhostHandModule());
		modules.add(new NameSpooferModule());
		modules.add(new DeflectModule());
		modules.add(new FullbrightModule());
		modules.add(new IceGrimoireModule());
		modules.add(new CurseKillModule());
		modules.add(new CurseRoarModule());
		modules.add(new AquaForceModule());
		modules.add(new EdenShardModule());
		modules.add(new SingularityModule());
		modules.add(new FlightModule());
		modules.add(new ScaffoldModule());
		modules.add(new ChestStealerModule());
		modules.add(new TriggerBotModule());
		modules.add(new BlinkModule());
		modules.add(new FastEatModule());
		modules.add(new LiquidsModule());

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!world.isClientSide() && isModuleEnabled("MegaLighter") && player.getItemInHand(hand).is(Items.FLINT_AND_STEEL)) {
				BlockPos centerPos = hitResult.getBlockPos();
				for (int dx = -3; dx != 4; dx++) {
					for (int dy = -2; dy != 3; dy++) {
						for (int dz = -3; dz != 4; dz++) {
							BlockPos targetPos = centerPos.offset(dx, dy, dz);
							BlockPos abovePos = targetPos.above();

							if (!world.getBlockState(targetPos).isAir() && world.getBlockState(abovePos).isAir()) {
								world.setBlockAndUpdate(abovePos, BaseFireBlock.getState(world, abovePos));
							}
						}
					}
				}
			}

			return InteractionResult.PASS;
		});

		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (world.isClientSide() || !isModuleEnabled("AreaMiner")) return;

			if (AreaMinerModule.isBreaking) return;

			ItemStack tool = player.getMainHandItem();
			if (tool.isEmpty() || !tool.isCorrectToolForDrops(state)) return;

			AreaMinerModule.isBreaking = true;
			try {
				for (int dx = -1; dx != 2; dx++) {
					for (int dy = -1; dy != 2; dy++) {
						for (int dz = -1; dz != 2; dz++) {
							if (dx == 0 && dy == 0 && dz == 0) continue;

							BlockPos targetPos = pos.offset(dx, dy, dz);
							BlockState targetState = world.getBlockState(targetPos);

							if (targetState.isAir() || !(targetState.getDestroySpeed(world, targetPos) >= 0)) continue;

							if (tool.isCorrectToolForDrops(targetState)) {
								if (!player.isCreative()) {
									Block.dropResources(targetState, world, targetPos, world.getBlockEntity(targetPos), player, tool);
								}

								world.destroyBlock(targetPos, false, player);
							}
						}
					}
				}
			} finally {
				AreaMinerModule.isBreaking = false;
			}
		});

		openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.myclient.open_gui",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				"category.myclient"
		));

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (openGuiKey.consumeClick()) {
				mc.setScreen(new ClickGuiScreen());
			}

			if (mc.player == null || mc.level == null) return;

			for (Module m : modules) {
				if (m.isEnabled()) {
					m.onTick();
				}
			}
		});

		HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null) return;

			guiGraphics.drawString(mc.font, "§b§SuperCraft §7v1.0 §f[Press Right-Shift to open menu]", 10, 10, 0xFFFFFF);

			int yOffset = 24;
			for (Module m : modules) {
				if (m.isEnabled()) {
					guiGraphics.drawString(mc.font, "§a> §f" + m.getName(), 10, yOffset, 0xFFFFFF);
					yOffset += 11;
					m.onRender2D(guiGraphics);
				}
			}
		});

		WorldRenderEvents.LAST.register(context -> {
			for (Module m : modules) {
				if (m.isEnabled()) {
					m.onRender3D(context);
				}
			}
		});

		net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
			if (message.startsWith(".name ")) {
				String newName = message.substring(6).replace("&", "§");
				NameSpooferModule.customName = newName;
				NameSpooferModule.currentMode = 2;

				Minecraft mc = Minecraft.getInstance();
				if (mc.player != null) {
					mc.player.displayClientMessage(Component.literal("§b[MyClient] §fCustomized ID: §r" + newName), false);
				}
				return false;
			}
			return true;
		});
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	public static class GlowEspModule extends Module {
		public GlowEspModule() {
			super("GlowESP", Category.RENDER, GLFW.GLFW_KEY_G);
		}
	}

	public static class TracersModule extends Module {
		public TracersModule() {
			super("Tracers", Category.RENDER, GLFW.GLFW_KEY_H);
		}

		@Override
		public void onRender3D(WorldRenderContext context) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null) return;

			PoseStack poseStack = context.matrixStack();
			Camera camera = context.camera();
			if (poseStack == null || camera == null) return;

			Vec3 camPos = camera.getPosition();
			Vec3 lookVec = Vec3.directionFromRotation(camera.getXRot(), camera.getYRot());
			Vec3 startPos = lookVec.scale(1.0);

			RenderSystem.disableDepthTest();
			VertexConsumer lineConsumer = context.consumers().getBuffer(RenderType.lines());
			PoseStack.Pose pose = poseStack.last();

			for (Entity entity : mc.level.entitiesForRendering()) {
				if (entity == mc.player || !(entity instanceof LivingEntity)) continue;
				double distance = mc.player.distanceTo(entity);
				if (distance > 50.0) continue;

				Vec3 targetPos = entity.position().add(0, entity.getBbHeight() / 2.0, 0).subtract(camPos);
				float ratio = (float) Math.min(distance / 30.0, 1.0);
				float red = 1.0f - ratio;
				float green = ratio;

				lineConsumer.addVertex(pose, (float) startPos.x, (float) startPos.y, (float) startPos.z)
						.setColor(red, green, blue(0.2f), 1.0f)
						.setNormal(pose, 0.0f, 1.0f, 0.0f);

				lineConsumer.addVertex(pose, (float) targetPos.x, (float) targetPos.y, (float) targetPos.z)
						.setColor(red, green, blue(0.2f), 1.0f)
						.setNormal(pose, 0.0f, 1.0f, 0.0f);
			}
			RenderSystem.enableDepthTest();
		}

		private float blue(float val) { return val; }
	}

	public static class AutoSprintModule extends Module {
		public AutoSprintModule() {
			super("AutoSprint", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player != null && mc.player.zza > 0 && !mc.player.isShiftKeyDown() && !mc.player.horizontalCollision) {
				mc.player.setSprinting(true);
			}
		}
	}

	public static class XRayModule extends Module {
		public XRayModule() {
			super("X-Ray", Category.RENDER, GLFW.GLFW_KEY_X);
		}

		@Override
		public void toggle() {
			super.toggle();
			Minecraft mc = Minecraft.getInstance();
			if (mc.levelRenderer != null) {
				mc.levelRenderer.allChanged();
			}
		}
	}

	public static class MachineGunBowModule extends Module {
		private int fireTimer = 0;

		public MachineGunBowModule() {
			super("MachineGunBow", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.gameMode == null) return;

			if (mc.player.isUsingItem() && mc.player.getUseItem().getItem() instanceof BowItem) {
				if (mc.getSingleplayerServer() != null) {
					fireTimer++;
					if (fireTimer >= 2) {
						fireTimer = 0;
						ServerPlayer sp = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
						if (sp != null) {
							ServerLevel level = sp.serverLevel();
							Arrow arrow = new Arrow(level, sp, Items.ARROW.getDefaultInstance(), null);
							arrow.shootFromRotation(sp, sp.getXRot(), sp.getYRot(), 0.0F, 3.2F, 1.5F);
							arrow.setCritArrow(true);
							level.addFreshEntity(arrow);
						}
					}
				} else {
					if (mc.player.getTicksUsingItem() >= 4) {
						mc.gameMode.releaseUsingItem(mc.player);
						mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
					}
				}
			} else {
				fireTimer = 0;
			}
		}
	}

	public static class ShotgunBowModule extends Module {
		private int cooldown = 0;
		private int burstIndex = 0;

		public ShotgunBowModule() {
			super("ShotgunBow", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.gameMode == null || mc.getConnection() == null) return;

			if (cooldown > 0) {
				cooldown--;
			}

			if (mc.player.isUsingItem() && mc.player.getUseItem().getItem() instanceof BowItem) {
				if (mc.getSingleplayerServer() != null) {
					if (cooldown == 0) {
						cooldown = 8;
						ServerPlayer serverPlayer = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
						if (serverPlayer != null) {
							ServerLevel serverLevel = serverPlayer.serverLevel();
							for (int i = 0; i != 12; i++) {
								Arrow arrow = new Arrow(serverLevel, serverPlayer, Items.ARROW.getDefaultInstance(), null);
								float yawOffset = (float) ((Math.random() - 0.5) * 18.0);
								float pitchOffset = (float) ((Math.random() - 0.5) * 10.0);

								arrow.shootFromRotation(
										serverPlayer,
										serverPlayer.getXRot() + pitchOffset,
										serverPlayer.getYRot() + yawOffset,
										0.0F,
										3.0F,
										1.0F
								);
								arrow.setCritArrow(true);
								serverLevel.addFreshEntity(arrow);
							}
						}
					}
				} else {
					if (mc.player.getTicksUsingItem() >= 4) {
						float spreadYaw = mc.player.getYRot() + (burstIndex - 2) * 6.0F;
						burstIndex = (burstIndex + 1) % 5;

						mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(
								spreadYaw,
								mc.player.getXRot(),
								mc.player.onGround(),
								mc.player.horizontalCollision
						));
						mc.gameMode.releaseUsingItem(mc.player);
						mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
					}
				}
			} else {
				cooldown = 0;
				burstIndex = 0;
			}
		}
	}

	public static class PotionMachineGunModule extends Module {
		public PotionMachineGunModule() {
			super("PotionMachineGun", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null) return;

			if (mc.options.keyUse.isDown() && mc.player.getMainHandItem().getItem() instanceof ThrowablePotionItem) {
				if (mc.getSingleplayerServer() != null) {
					ServerPlayer serverPlayer = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
					if (serverPlayer != null) {
						ServerLevel serverLevel = serverPlayer.serverLevel();

						ThrownPotion potionEntity = new ThrownPotion(serverLevel, serverPlayer, mc.player.getMainHandItem().copy());

						potionEntity.shootFromRotation(
								serverPlayer,
								serverPlayer.getXRot(),
								serverPlayer.getYRot(),
								-10.0F,
								2.5F,
								1.0F
						);

						serverLevel.addFreshEntity(potionEntity);
					}
				}

				mc.player.swing(InteractionHand.MAIN_HAND);
			}
		}
	}

	public static class KillAuraModule extends Module {
		public KillAuraModule() {
			super("KillAura", Category.COMBAT, GLFW.GLFW_KEY_R);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null || mc.gameMode == null || mc.getConnection() == null) return;

			if (mc.player.getAttackStrengthScale(0.5F) >= 0.95F) {
				LivingEntity closestTarget = null;
				double closestDistance = 4.5;

				for (Entity entity : mc.level.entitiesForRendering()) {
					if (entity == mc.player || !(entity instanceof LivingEntity living)) continue;
					if (!living.isAlive() || living.getHealth() <= 0) continue;

					double dist = mc.player.distanceTo(living);
					if (dist > closestDistance) continue;

					closestDistance = dist;
					closestTarget = living;
				}

				if (closestTarget != null) {
					Vec3 eyePos = mc.player.getEyePosition();
					Vec3 targetCenter = closestTarget.position().add(0, closestTarget.getBbHeight() / 2.0, 0);

					double diffX = targetCenter.x - eyePos.x;
					double diffY = targetCenter.y - eyePos.y;
					double diffZ = targetCenter.z - eyePos.z;
					double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

					float targetYaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
					float targetPitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

					mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(
							targetYaw,
							targetPitch,
							mc.player.onGround(),
							mc.player.horizontalCollision
					));

					mc.gameMode.attack(mc.player, closestTarget);
					mc.player.swing(InteractionHand.MAIN_HAND);
				}
			}
		}
	}

	public static class TpAuraModule extends Module {
		private LivingEntity lastTpTarget = null;
		private int renderTrailTicks = 0;

		public TpAuraModule() {
			super("TpAura", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null || mc.gameMode == null || mc.getConnection() == null) return;

			if (renderTrailTicks > 0) {
				renderTrailTicks--;
			}

			if (mc.player.getAttackStrengthScale(0.5F) >= 0.95F) {
				LivingEntity target = null;
				double maxRange = 25.0;

				for (Entity entity : mc.level.entitiesForRendering()) {
					if (entity == mc.player || !(entity instanceof LivingEntity living)) continue;
					if (!living.isAlive() || !(living.getHealth() > 0)) continue;

					double dist = mc.player.distanceTo(living);
					if (dist > maxRange) continue;

					maxRange = dist;
					target = living;
				}

				if (target != null) {
					Vec3 startPos = mc.player.position();
					Vec3 targetPos = target.position();

					double totalDist = startPos.distanceTo(targetPos);
					int steps = Math.max(1, (int) Math.ceil(totalDist / 5.0));

					for (int i = 1; i != steps + 1; i++) {
						double progress = (double) i / steps;
						double nextX = startPos.x + (targetPos.x - startPos.x) * progress;
						double nextY = startPos.y + (targetPos.y - startPos.y) * progress;
						double nextZ = startPos.z + (targetPos.z - startPos.z) * progress;

						mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(
								nextX, nextY, nextZ, false, mc.player.horizontalCollision
						));
					}

					mc.gameMode.attack(mc.player, target);
					mc.player.swing(InteractionHand.MAIN_HAND);

					for (int i = steps - 1; i >= 0; i--) {
						double progress = (double) i / steps;
						double backX = startPos.x + (targetPos.x - startPos.x) * progress;
						double backY = startPos.y + (targetPos.y - startPos.y) * progress;
						double backZ = startPos.z + (targetPos.z - startPos.z) * progress;

						mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(
								backX, backY, backZ, i == 0 && mc.player.onGround(), mc.player.horizontalCollision
						));
					}

					lastTpTarget = target;
					renderTrailTicks = 6;
				}
			}
		}

		@Override
		public void onRender3D(WorldRenderContext context) {
			if (renderTrailTicks == 0 || lastTpTarget == null || !lastTpTarget.isAlive()) return;

			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) return;

			PoseStack poseStack = context.matrixStack();
			Camera camera = context.camera();
			if (poseStack == null || camera == null) return;

			Vec3 camPos = camera.getPosition();
			Vec3 start = mc.player.position().add(0, 1.0, 0).subtract(camPos);
			Vec3 end = lastTpTarget.position().add(0, lastTpTarget.getBbHeight() / 2.0, 0).subtract(camPos);

			RenderSystem.disableDepthTest();
			VertexConsumer lineConsumer = context.consumers().getBuffer(RenderType.lines());
			PoseStack.Pose pose = poseStack.last();

			lineConsumer.addVertex(pose, (float) start.x, (float) start.y, (float) start.z)
					.setColor(0.0f, 1.0f, 1.0f, 1.0f)
					.setNormal(pose, 0.0f, 1.0f, 0.0f);

			lineConsumer.addVertex(pose, (float) end.x, (float) end.y, (float) end.z)
					.setColor(1.0f, 0.2f, 1.0f, 1.0f)
					.setNormal(pose, 0.0f, 1.0f, 0.0f);

			RenderSystem.enableDepthTest();
		}
	}

	public static class MultiAuraModule extends Module {
		public MultiAuraModule() {
			super("MultiAura", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null || mc.gameMode == null || mc.getConnection() == null) return;

			if (mc.player.getAttackStrengthScale(0.5F) >= 0.95F) {
				boolean attackedAny = false;
				Vec3 eyePos = mc.player.getEyePosition();

				for (Entity entity : mc.level.entitiesForRendering()) {
					if (entity == mc.player || !(entity instanceof LivingEntity living)) continue;
					if (!living.isAlive() || !(living.getHealth() > 0)) continue;

					double dist = mc.player.distanceTo(living);
					if (dist > 5.5) continue;

					Vec3 targetCenter = living.position().add(0, living.getBbHeight() / 2.0, 0);
					double diffX = targetCenter.x - eyePos.x;
					double diffY = targetCenter.y - eyePos.y;
					double diffZ = targetCenter.z - eyePos.z;
					double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

					float targetYaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
					float targetPitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

					mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(
							targetYaw, targetPitch, mc.player.onGround(), mc.player.horizontalCollision
					));
					mc.gameMode.attack(mc.player, living);
					attackedAny = true;
				}

				if (attackedAny) {
					mc.player.swing(InteractionHand.MAIN_HAND);
				}
			}
		}
	}

	public static class BlinkAuraModule extends Module {
		private int teleportDelay = 0;

		public BlinkAuraModule() {
			super("BlinkAura", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null || mc.gameMode == null) return;

			LivingEntity target = null;
			double maxRange = 8.0;

			for (Entity entity : mc.level.entitiesForRendering()) {
				if (entity == mc.player || !(entity instanceof LivingEntity living)) continue;
				if (!living.isAlive() || !(living.getHealth() > 0)) continue;

				double dist = mc.player.distanceTo(living);
				if (dist > maxRange) continue;

				maxRange = dist;
				target = living;
			}

			if (target == null) return;

			teleportDelay++;
			if (teleportDelay >= 2) {
				teleportDelay = 0;

				double randomAngle = Math.random() * Math.PI * 2.0;
				double radius = 2.8;

				double nextX = target.getX() + Math.cos(randomAngle) * radius;
				double nextZ = target.getZ() + Math.sin(randomAngle) * radius;
				double nextY = target.getY() + 0.2;

				mc.player.setPos(nextX, nextY, nextZ);
				mc.player.setDeltaMovement(0, 0, 0);

				if (mc.getSingleplayerServer() != null) {
					ServerPlayer serverPlayer = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
					if (serverPlayer != null) {
						serverPlayer.teleportTo(nextX, nextY, nextZ);
					}
				}

				Vec3 eyePos = mc.player.getEyePosition();
				Vec3 targetCenter = target.position().add(0, target.getBbHeight() / 2.0, 0);
				double diffX = targetCenter.x - eyePos.x;
				double diffY = targetCenter.y - eyePos.y;
				double diffZ = targetCenter.z - eyePos.z;
				double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

				float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
				float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

				mc.player.setYRot(yaw);
				mc.player.setXRot(pitch);
			}

			if (mc.player.getAttackStrengthScale(0.5F) >= 0.90F) {
				mc.gameMode.attack(mc.player, target);
				mc.player.swing(InteractionHand.MAIN_HAND);
			}
		}
	}

	public static class FireballCannonModule extends Module {
		public FireballCannonModule() {
			super("FireballCannon", Category.MISC, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null) return;

			if (mc.options.keyUse.isDown()) {
				if (mc.getSingleplayerServer() != null) {
					ServerPlayer serverPlayer = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
					if (serverPlayer != null) {
						ServerLevel serverLevel = serverPlayer.serverLevel();
						Vec3 look = serverPlayer.getLookAngle();

						LargeFireball fireball = new LargeFireball(
								serverLevel,
								serverPlayer,
								look.scale(10.0),
								2
						);

						fireball.setPos(
								serverPlayer.getX() + look.x * 2.5,
								serverPlayer.getEyeY() + look.y * 2.5,
								serverPlayer.getZ() + look.z * 2.5
						);

						fireball.shoot(look.x, look.y, look.z, 5.0F, 0.0F);

						serverLevel.addFreshEntity(fireball);
					}
				}
				mc.player.swing(InteractionHand.MAIN_HAND);
			}
		}
	}

	public static class WitherBarrageModule extends Module {
		public WitherBarrageModule() {
			super("WitherBarrage", Category.MISC, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null) return;

			if (mc.options.keyUse.isDown()) {
				if (mc.getSingleplayerServer() != null) {
					ServerPlayer serverPlayer = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
					if (serverPlayer != null) {
						ServerLevel serverLevel = serverPlayer.serverLevel();
						Vec3 look = serverPlayer.getLookAngle();

						WitherSkull skull = new WitherSkull(serverLevel, serverPlayer, look.scale(10.0));

						if (Math.random() > 0.75) {
							skull.setDangerous(true);
						}

						skull.setPos(
								serverPlayer.getX() + look.x * 2.5,
								serverPlayer.getEyeY() + look.y * 2.5,
								serverPlayer.getZ() + look.z * 2.5
						);

						skull.shoot(look.x, look.y, look.z, 5.5F, 1.5F);

						serverLevel.addFreshEntity(skull);
					}
				}
				mc.player.swing(InteractionHand.MAIN_HAND);
			}
		}
	}

	public static class MinimapModule extends Module {
		public MinimapModule() {
			super("Minimap", Category.RENDER, GLFW.GLFW_KEY_M);
		}

		@Override
		public void onRender2D(GuiGraphics g) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null) return;

			int screenWidth = mc.getWindow().getGuiScaledWidth();
			int mapSize = 90;
			int mapX = screenWidth - mapSize - 10;
			int mapY = 10;
			int centerX = mapX + mapSize / 2;
			int centerY = mapY + mapSize / 2;

			g.fill(mapX, mapY, mapX + mapSize, mapY + mapSize, 0xAA0A141E);
			g.fill(mapX, mapY, mapX + mapSize, mapY + 1, 0xFF00FFFF);
			g.fill(mapX, mapY + mapSize - 1, mapX + mapSize, mapY + mapSize, 0xFF00FFFF);
			g.fill(mapX, mapY, mapX + 1, mapY + mapSize, 0xFF00FFFF);
			g.fill(mapX + mapSize - 1, mapY, mapX + mapSize, mapY + mapSize, 0xFF00FFFF);

			g.fill(centerX, mapY + 2, centerX + 1, mapY + mapSize - 2, 0x33FFFFFF);
			g.fill(mapX + 2, centerY, mapX + mapSize - 2, centerY + 1, 0x33FFFFFF);

			double yawRad = Math.toRadians(mc.player.getYRot());
			double cos = Math.cos(yawRad);
			double sin = Math.sin(yawRad);
			double scale = 1.1;

			for (Entity entity : mc.level.entitiesForRendering()) {
				if (entity == mc.player || !(entity instanceof LivingEntity living)) continue;
				if (!living.isAlive()) continue;

				double dx = entity.getX() - mc.player.getX();
				double dz = entity.getZ() - mc.player.getZ();

				double rotX = -(dx * cos + dz * sin) * scale;
				double rotY = (dx * sin - dz * cos) * scale;

				int maxOffset = (mapSize / 2) - 4;
				if (Math.abs(rotX) > maxOffset || Math.abs(rotY) > maxOffset) continue;

				int dotX = (int) (centerX + rotX);
				int dotY = (int) (centerY + rotY);

				int dotColor = (entity instanceof net.minecraft.world.entity.player.Player) ? 0xFFFF2222 : 0xFFFFAA00;
				g.fill(dotX - 1, dotY - 1, dotX + 2, dotY + 2, dotColor);
			}

			g.fill(centerX - 2, centerY - 2, centerX + 2, centerY + 2, 0xFF00FFFF);
			g.drawCenteredString(mc.font, "§bN", centerX, mapY + mapSize + 3, 0xFFFFFF);
		}
	}

	public static class HealthTagsModule extends Module {
		public HealthTagsModule() {
			super("HealthTags", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onRender2D(GuiGraphics g) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null) return;

			int screenWidth = mc.getWindow().getGuiScaledWidth();
			int screenHeight = mc.getWindow().getGuiScaledHeight();

			if (mc.crosshairPickEntity instanceof LivingEntity target && target.isAlive()) {
				float hp = target.getHealth();
				float maxHp = target.getMaxHealth();
				float ratio = Math.min(1.0F, Math.max(0.0F, hp / maxHp));

				java.util.Collection<MobEffectInstance> effects = target.getActiveEffects();
				if (mc.getSingleplayerServer() != null) {
					ServerPlayer sp = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
					if (sp != null) {
						Entity serverEntity = sp.serverLevel().getEntity(target.getId());
						if (serverEntity instanceof LivingEntity serverLiving) {
							effects = serverLiving.getActiveEffects();
						}
					}
				}

				StringBuilder statusBuilder = new StringBuilder();
				for (MobEffectInstance effect : effects) {
					String effectName = effect.getEffect().value().getDisplayName().getString();
					int amplifier = effect.getAmplifier() + 1;
					double secondsLeft = effect.getDuration() / 20.0;
					String color = effect.getEffect().value().isBeneficial() ? "§b" : "§d";
					statusBuilder.append(String.format("%s[%s %d: %.1fs] ", color, effectName, amplifier, secondsLeft));
				}
				String statusText = statusBuilder.toString().trim();
				boolean hasEffects = !statusText.isEmpty();

				int textWidth = hasEffects ? mc.font.width(statusText) + 12 : 115;
				int hudW = Math.max(115, textWidth);
				int hudH = hasEffects ? 38 : 26;
				int hudX = (screenWidth / 2) + 12;
				int hudY = (screenHeight / 2) + 12;

				g.fill(hudX, hudY, hudX + hudW, hudY + hudH, 0xCC10141A);
				g.fill(hudX, hudY, hudX + hudW, hudY + 1, 0xFF00FFFF);

				String info = String.format("§f%s §7(%.1f/%.0f)", target.getName().getString(), hp, maxHp);
				g.drawString(mc.font, info, hudX + 5, hudY + 4, 0xFFFFFF);

				int barX = hudX + 5;
				int barY = hudY + 16;
				int barMaxW = hudW - 10;
				g.fill(barX, barY, barX + barMaxW, barY + 5, 0xFF333333);

				int barColor = ratio > 0.5F ? 0xFF22EE22 : (ratio > 0.25F ? 0xFFFFCC00 : 0xFFFF2222);
				g.fill(barX, barY, barX + (int) (barMaxW * ratio), barY + 5, barColor);

				if (hasEffects) {
					g.drawString(mc.font, statusText, hudX + 5, hudY + 25, 0xFFFFFF);
				}
			}

			int listY = screenHeight - 60;
			int count = 0;
			for (Entity entity : mc.level.entitiesForRendering()) {
				if (entity == mc.player || !(entity instanceof LivingEntity living)) continue;
				if (!living.isAlive() || mc.player.distanceTo(living) > 15.0) continue;
				if (count >= 5) break;

				float hp = living.getHealth();
				String hpColor = hp > (living.getMaxHealth() * 0.5F) ? "§a" : "§c";
				String line = String.format("§7[%.1fm] §f%s %s%.1f❤",
						mc.player.distanceTo(living), living.getName().getString(), hpColor, hp);

				g.drawString(mc.font, line, 10, listY, 0xFFFFFF);
				listY -= 11;
				count++;
			}
		}
	}

	public static class AreaMinerModule extends Module {
		public static boolean isBreaking = false;

		public AreaMinerModule() {
			super("AreaMiner", Category.MISC, GLFW.GLFW_KEY_UNKNOWN);
		}
	}

	public static class MegaLighterModule extends Module {
		public MegaLighterModule() {
			super("MegaLighter", Category.GRIEFING, GLFW.GLFW_KEY_UNKNOWN);
		}
	}

	public static class TntLaserModule extends Module {
		private int spawnCooldown = 0;

		public TntLaserModule() {
			super("TntLaser", Category.GRIEFING, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null) return;

			if (spawnCooldown > 0) {
				spawnCooldown--;
			}

			if (mc.options.keyUse.isDown() && spawnCooldown == 0) {
				HitResult hit = mc.player.pick(100.0, 0.0F, false);

				if (hit.getType() == HitResult.Type.BLOCK) {
					spawnCooldown = 4;
					BlockHitResult blockHit = (BlockHitResult) hit;
					BlockPos spawnPos = blockHit.getBlockPos().above();

					if (mc.getSingleplayerServer() != null) {
						ServerPlayer serverPlayer = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
						if (serverPlayer != null) {
							ServerLevel serverLevel = serverPlayer.serverLevel();

							PrimedTnt tnt = new PrimedTnt(
									serverLevel,
									spawnPos.getX() + 0.5,
									spawnPos.getY(),
									spawnPos.getZ() + 0.5,
									serverPlayer
							);

							tnt.setFuse(40);
							serverLevel.addFreshEntity(tnt);
						}
					}
					mc.player.swing(InteractionHand.MAIN_HAND);
				}
			}
		}
	}

	public static class JesusModule extends Module {
		public JesusModule() {
			super("Jesus", Category.MOVEMENT, GLFW.GLFW_KEY_J);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null) return;

			if (mc.player.isShiftKeyDown()) return;

			if (mc.player.isInWater() || mc.player.isInLava()) {
				Vec3 vel = mc.player.getDeltaMovement();
				mc.player.setDeltaMovement(vel.x, 0.11, vel.z);
				return;
			}

			BlockPos belowPos = BlockPos.containing(mc.player.getX(), mc.player.getY() - 0.1, mc.player.getZ());
			Block blockBelow = mc.level.getBlockState(belowPos).getBlock();

			if (blockBelow == Blocks.WATER || blockBelow == Blocks.LAVA) {
				Vec3 vel = mc.player.getDeltaMovement();
				if (!(vel.y >= 0)) {
					mc.player.setDeltaMovement(vel.x, 0.0, vel.z);
					mc.player.setOnGround(true);
				}
			}
		}
	}

	public static class NoFallModule extends Module {
		public NoFallModule() {
			super("NoFall", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.getConnection() == null) return;

			if (mc.player.fallDistance > 2.5F) {
				mc.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(
						true,
						mc.player.horizontalCollision
				));

				mc.player.fallDistance = 0.0F;
				if (mc.getSingleplayerServer() != null) {
					ServerPlayer sp = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
					if (sp != null) sp.fallDistance = 0.0F;
				}
			}
		}
	}

	public static class StepModule extends Module {
		public StepModule() {
			super("Step", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) return;

			AttributeInstance stepAttr = mc.player.getAttribute(Attributes.STEP_HEIGHT);
			if (stepAttr != null && stepAttr.getBaseValue() != 1.25) {
				stepAttr.setBaseValue(1.25);
			}
		}

		@Override
		public void toggle() {
			super.toggle();
			if (!isEnabled()) {
				Minecraft mc = Minecraft.getInstance();
				if (mc.player != null) {
					AttributeInstance stepAttr = mc.player.getAttribute(Attributes.STEP_HEIGHT);
					if (stepAttr != null) {
						stepAttr.setBaseValue(0.6);
					}
				}
			}
		}
	}

	public static class SpeedModule extends Module {
		public SpeedModule() {
			super("Speed", Category.MOVEMENT, GLFW.GLFW_KEY_V);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.player.isShiftKeyDown()) return;

			float forward = mc.player.zza;
			float strafe = mc.player.xxa;

			if (forward == 0.0F && strafe == 0.0F) {
				if (mc.player.onGround()) {
					mc.player.setDeltaMovement(0.0, mc.player.getDeltaMovement().y, 0.0);
				}
				return;
			}

			double yaw = Math.toRadians(mc.player.getYRot());
			double moveAngle = yaw - Math.atan2(strafe, forward);

			double speedMultiplier = 0.65;

			double motionX = -Math.sin(moveAngle) * speedMultiplier;
			double motionZ = Math.cos(moveAngle) * speedMultiplier;

			mc.player.setDeltaMovement(motionX, mc.player.getDeltaMovement().y, motionZ);
		}
	}

	public static class SpiderModule extends Module {
		public SpiderModule() {
			super("Spider", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) return;

			if (mc.player.horizontalCollision && !mc.player.isShiftKeyDown()) {
				Vec3 vel = mc.player.getDeltaMovement();
				mc.player.setDeltaMovement(vel.x, 0.25, vel.z);
			}
		}
	}

	public static class HighJumpModule extends Module {
		public HighJumpModule() {
			super("HighJump", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) return;

			AttributeInstance jumpAttr = mc.player.getAttribute(Attributes.JUMP_STRENGTH);
			if (jumpAttr != null && jumpAttr.getBaseValue() != 1.05) {
				jumpAttr.setBaseValue(1.05);
			}
		}

		@Override
		public void toggle() {
			super.toggle();
			if (!isEnabled()) {
				Minecraft mc = Minecraft.getInstance();
				if (mc.player != null) {
					AttributeInstance jumpAttr = mc.player.getAttribute(Attributes.JUMP_STRENGTH);
					if (jumpAttr != null) {
						jumpAttr.setBaseValue(0.42);
					}
				}
			}
		}
	}

	public static class GhostHandModule extends Module {
		private int interactCooldown = 0;

		public GhostHandModule() {
			super("GhostHand", Category.MISC, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null || mc.getConnection() == null) return;

			if (interactCooldown > 0) {
				interactCooldown--;
			}

			if (mc.options.keyUse.isDown() && interactCooldown == 0) {
				Vec3 eyePos = mc.player.getEyePosition();
				Vec3 look = mc.player.getLookAngle();

				for (int step = 2; step != 30; step++) {
					double dist = step * 0.2;
					Vec3 checkVec = eyePos.add(look.scale(dist));
					BlockPos targetPos = BlockPos.containing(checkVec.x, checkVec.y, checkVec.z);
					Block block = mc.level.getBlockState(targetPos).getBlock();

					boolean isTargetContainerOrTnt = (block instanceof ChestBlock) ||
							(block instanceof BarrelBlock) ||
							(block instanceof TntBlock) ||
							(block instanceof LeverBlock) ||
							(block instanceof ButtonBlock);

					if (isTargetContainerOrTnt) {
						interactCooldown = 6;

						BlockHitResult fakeHit = new BlockHitResult(
								checkVec,
								Direction.UP,
								targetPos,
								false
						);

						mc.getConnection().send(new ServerboundUseItemOnPacket(
								InteractionHand.MAIN_HAND,
								fakeHit,
								0
						));
						mc.player.swing(InteractionHand.MAIN_HAND);
						break;
					}
				}
			}
		}
	}

	public static class NameSpooferModule extends Module {
		public static int currentMode = 0;
		public static String customName = "§b§lHackerPro";

		private static final String[] NORMAL_NAMES = {
				"§a[VIP] §fAlex_TW",
				"§b[MVP+] §fShadowBlade99",
				"§7Notch",
				"§e[PRO] §fSkyWalker_01",
				"§fSteve_Builder",
				"§d[YT] §fNightWolf"
		};
		private static int normalIndex = 0;
		private static int slowTimer = 0;

		public NameSpooferModule() {
			super("NameSpoofer", Category.MISC, GLFW.GLFW_KEY_UNKNOWN);
		}

		public void cycleMode() {
			currentMode = (currentMode + 1) % 3;
			Minecraft mc = Minecraft.getInstance();
			if (mc.player != null) {
				String modeName = currentMode == 0 ? "Matrix" : (currentMode == 1 ? "Normal" : "Custom");
				mc.player.displayClientMessage(Component.literal("§b[NameSpoofer] §fMode: §e" + modeName), true);
			}
		}

		@Override
		public String getDisplayLabel() {
			String tag = currentMode == 0 ? " §8[M]" : (currentMode == 1 ? " §8[N]" : " §8[C]");
			return getName() + tag;
		}

		@Override
		public void onTick() {
			if (currentMode == 1) {
				slowTimer++;
				// 10 tick: 0.5s
				if (slowTimer >= 10) {
					slowTimer = 0;
					normalIndex = (normalIndex + 1) % NORMAL_NAMES.length;
				}
			}
		}

		public static Component getSpoofedComponent() {
			if (currentMode == 0) {
				String[] colors = {"§a", "§b", "§c", "§d", "§e", "§2", "§3"};
				String randomColor = colors[(int) (Math.random() * colors.length)];
				int length = 6 + (int) (Math.random() * 9);
				StringBuilder scrambled = new StringBuilder();
				for (int i = 0; i != length; i++) {
					scrambled.append((char) ('A' + (int) (Math.random() * 26)));
				}
				return Component.literal("§8[" + randomColor + "§k" + scrambled + "§r§8]");
			} else if (currentMode == 1) {
				return Component.literal(NORMAL_NAMES[normalIndex]);
			} else {
				return Component.literal(customName);
			}
		}
	}

	public static class DeflectModule extends Module {
		public DeflectModule() {
			super("Deflect", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null) return;

			if (mc.getSingleplayerServer() != null) {
				ServerPlayer serverPlayer = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
				if (serverPlayer != null) {
					ServerLevel serverLevel = serverPlayer.serverLevel();
					AABB shieldBox = serverPlayer.getBoundingBox().inflate(4.5);

					for (Projectile proj : serverLevel.getEntitiesOfClass(Projectile.class, shieldBox)) {
						if (proj.getOwner() == serverPlayer) continue;

						if (!(proj.getDeltaMovement().lengthSqr() > 0.01)) continue;

						Entity originalShooter = proj.getOwner();
						proj.setOwner(serverPlayer);

						Vec3 reflectDir;
						if (originalShooter != null && originalShooter.isAlive()) {
							Vec3 shooterCenter = originalShooter.position().add(0, originalShooter.getBbHeight() / 2.0, 0);
							reflectDir = shooterCenter.subtract(proj.position()).normalize();
						} else {
							reflectDir = serverPlayer.getLookAngle().normalize();
						}

						proj.shoot(reflectDir.x, reflectDir.y, reflectDir.z, 2.5F, 0.0F);
						proj.hasImpulse = true;

						serverLevel.playSound(null, serverPlayer.blockPosition(),
								net.minecraft.sounds.SoundEvents.SHIELD_BLOCK,
								net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.2F);
						mc.player.swing(InteractionHand.MAIN_HAND);
					}
				}
			}
		}
	}

	public static class FullbrightModule extends Module {
		public FullbrightModule() {
			super("Fullbright", Category.RENDER, GLFW.GLFW_KEY_B);
		}
	}

	public static class IceGrimoireModule extends Module {
		private int castCooldown = 0;

		public IceGrimoireModule() {
			super("IceGrimoire", Category.DARK_MAGIC, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null) return;

			if (castCooldown > 0) {
				castCooldown--;
			}

			if (mc.options.keyUse.isDown() && castCooldown == 0) {
				castCooldown = 10;

				MinecraftServer server = mc.getSingleplayerServer();
				if (server != null) {
					java.util.UUID playerId = mc.player.getUUID();
					Vec3 eyePos = mc.player.getEyePosition();
					Vec3 look = mc.player.getLookAngle().normalize();

					server.execute(() -> {
						ServerPlayer serverPlayer = server.getPlayerList().getPlayer(playerId);
						if (serverPlayer == null) return;

						ServerLevel serverLevel = serverPlayer.serverLevel();
						serverLevel.playSound(null, serverPlayer.blockPosition(),
								SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

						java.util.Set<LivingEntity> hitEntities = new java.util.LinkedHashSet<>();

						for (int step = 2; step < 76; step++) {
							double dist = step * 0.4;
							Vec3 point = eyePos.add(look.scale(dist));

							double coneRadius = 0.8 + (dist * 0.30);

							int particleCount = Math.max(3, (int) (coneRadius * 4));
							serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
									point.x, point.y, point.z,
									particleCount, coneRadius * 0.55, coneRadius * 0.35, coneRadius * 0.55, 0.02);

							AABB coneBox = new AABB(
									point.x - coneRadius, point.y - coneRadius * 0.7, point.z - coneRadius,
									point.x + coneRadius, point.y + coneRadius * 0.7, point.z + coneRadius
							);

							for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, coneBox)) {
								if (target == serverPlayer || !target.isAlive() || target.isRemoved()) continue;
								hitEntities.add(target);
							}
						}

						for (LivingEntity target : hitEntities) {
							if (!target.isAlive() || target.isRemoved()) continue;

							target.hurtServer(serverLevel, serverLevel.damageSources().freeze(), 10.0F);

							target.knockback(1.2, -look.x, -look.z);
							target.hurtMarked = true;

							target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
							target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 4));

							serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getEyeY(), target.getZ(), 20, 0.4, 0.5, 0.4, 0.08);
							serverLevel.playSound(null, target.blockPosition(),
									SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.8F);
						}
					});
				}

				mc.player.swing(InteractionHand.MAIN_HAND);
			}
		}
	}

	public static class CurseKillModule extends Module {
		private int castCooldown = 0;

		public CurseKillModule() {
			super("CurseKill", Category.DARK_MAGIC, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null) return;

			if (castCooldown > 0) {
				castCooldown--;
			}

			if (mc.options.keyUse.isDown() && castCooldown == 0) {
				castCooldown = 10;

				if (mc.getSingleplayerServer() != null) {
					ServerPlayer serverPlayer = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
					if (serverPlayer != null) {
						ServerLevel serverLevel = serverPlayer.serverLevel();
						Vec3 eyePos = serverPlayer.getEyePosition();
						Vec3 look = serverPlayer.getLookAngle().normalize();

						serverLevel.playSound(null, serverPlayer.blockPosition(),
								SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.0F, 1.0F);

						java.util.Set<Entity> hitEntities = new java.util.HashSet<>();

						for (int step = 2; step != 122; step++) {
							double dist = step * 0.5;
							Vec3 point = eyePos.add(look.scale(dist));

							double coneRadius = 0.8 + (dist * 0.30);

							int particleCount = Math.max(3, (int) (coneRadius * 3));
							serverLevel.sendParticles(ParticleTypes.WITCH,
									point.x, point.y, point.z,
									particleCount, coneRadius * 0.55, coneRadius * 0.35, coneRadius * 0.55, 0.02);

							AABB coneBox = new AABB(
									point.x - coneRadius, point.y - coneRadius * 0.7, point.z - coneRadius,
									point.x + coneRadius, point.y + coneRadius * 0.7, point.z + coneRadius
							);

							for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, coneBox)) {
								if (target == serverPlayer || !target.isAlive() || hitEntities.contains(target)) continue;

								hitEntities.add(target);

								target.hurtServer(serverLevel, serverLevel.damageSources().magic(), 20.0F);

								target.knockback(1.5, -look.x, -look.z);
								target.hurtMarked = true;

								net.minecraft.core.particles.ColorParticleOption poisonParticle =
										net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF4E9331);
								net.minecraft.core.particles.ColorParticleOption regenParticle =
										net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFFCD5CAB);

								serverLevel.sendParticles(poisonParticle, target.getX(), target.getEyeY(), target.getZ(), 25, 0.5, 0.5, 0.5, 0.1);
								serverLevel.sendParticles(regenParticle, target.getX(), target.getEyeY(), target.getZ(), 25, 0.5, 0.5, 0.5, 0.1);
								serverLevel.playSound(null, target.blockPosition(),
										SoundEvents.SPLASH_POTION_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
							}
						}
					}
				}
				mc.player.swing(InteractionHand.MAIN_HAND);
			}
		}
	}

	public static class CurseRoarModule extends Module {
		private int burstRemaining = 0;
		private int burstInterval = 0;
		private int globalCooldown = 0;

		private static class DelayedExplosion {
			final ServerLevel level;
			final LivingEntity target;
			Vec3 lastKnownPos;
			final ServerPlayer caster;
			int fuseTicks = 40;

			DelayedExplosion(ServerLevel level, LivingEntity target, ServerPlayer caster) {
				this.level = level;
				this.target = target;
				this.lastKnownPos = target.position();
				this.caster = caster;
			}
		}

		private final java.util.List<DelayedExplosion> pendingExplosions = new java.util.ArrayList<>();

		public CurseRoarModule() {
			super("CurseRoar", Category.DARK_MAGIC, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null) return;

			if (globalCooldown > 0) globalCooldown--;
			if (burstInterval > 0) burstInterval--;

			java.util.Iterator<DelayedExplosion> it = pendingExplosions.iterator();
			while (it.hasNext()) {
				DelayedExplosion exp = it.next();
				exp.fuseTicks--;

				if (exp.target != null && exp.target.isAlive()) {
					exp.lastKnownPos = exp.target.position();
				}

				if (exp.fuseTicks % 4 == 0) {
					exp.level.sendParticles(ParticleTypes.DRAGON_BREATH,
							exp.lastKnownPos.x, exp.lastKnownPos.y + 1.0, exp.lastKnownPos.z, 6, 0.3, 0.4, 0.3, 0.02);
				}

				if (exp.fuseTicks == 0) {
					exp.level.playSound(null, BlockPos.containing(exp.lastKnownPos.x, exp.lastKnownPos.y, exp.lastKnownPos.z),
							SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.5F, 1.0F);

					exp.level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
							exp.lastKnownPos.x, exp.lastKnownPos.y + 0.5, exp.lastKnownPos.z, 1, 0.0, 0.0, 0.0, 0.0);

					AABB blastBox = new AABB(
							exp.lastKnownPos.x - 4.0, exp.lastKnownPos.y - 3.0, exp.lastKnownPos.z - 4.0,
							exp.lastKnownPos.x + 4.0, exp.lastKnownPos.y + 3.0, exp.lastKnownPos.z + 4.0
					);
					for (LivingEntity victim : exp.level.getEntitiesOfClass(LivingEntity.class, blastBox)) {
						if (victim == exp.caster || !victim.isAlive()) continue;

						victim.hurtServer(exp.level, exp.level.damageSources().explosion(exp.caster, exp.caster), 8.0F);

						if (victim == exp.target) {
							victim.push(0.0, 0.65, 0.0);
						} else {
							Vec3 pushDir = victim.position().subtract(exp.lastKnownPos).normalize();
							victim.push(pushDir.x * 0.8, 0.45, pushDir.z * 0.8);
						}
						victim.hurtMarked = true;
					}

					it.remove();
				}
			}


			if (mc.options.keyUse.isDown() && burstRemaining == 0 && globalCooldown == 0) {
				burstRemaining = 5;
				burstInterval = 0;
				globalCooldown = 25;
			}

			if (burstRemaining > 0 && burstInterval == 0) {
				burstRemaining--;
				burstInterval = 3;

				if (mc.getSingleplayerServer() != null) {
					ServerPlayer serverPlayer = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
					if (serverPlayer != null) {
						ServerLevel serverLevel = serverPlayer.serverLevel();
						Vec3 eyePos = serverPlayer.getEyePosition();
						Vec3 look = serverPlayer.getLookAngle().normalize();

						serverLevel.playSound(null, serverPlayer.blockPosition(),
								SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.8F, 1.4F);

						java.util.Set<Entity> hitEntities = new java.util.HashSet<>();
						Vec3 endPoint = eyePos.add(look.scale(30.0));
						int explosionsAddedThisShot = 0;

						for (int step = 2; step != 122; step++) {
							double dist = step * 0.5;
							Vec3 point = eyePos.add(look.scale(dist));
							double coneRadius = 0.8 + (dist * 0.30);

							if (step % 6 == 0) {
								serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
							}
							serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
									point.x, point.y, point.z,
									Math.max(2, (int) (coneRadius * 2)), coneRadius * 0.5, coneRadius * 0.3, coneRadius * 0.5, 0.01);

							AABB coneBox = new AABB(
									point.x - coneRadius, point.y - coneRadius * 0.7, point.z - coneRadius,
									point.x + coneRadius, point.y + coneRadius * 0.7, point.z + coneRadius
							);

							for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, coneBox)) {
								if (target == serverPlayer || !target.isAlive() || hitEntities.contains(target)) continue;

								hitEntities.add(target);

								Vec3 hitLocation = new Vec3(target.getX(), target.getY(), target.getZ());

								target.hurtServer(serverLevel, serverLevel.damageSources().sonicBoom(serverPlayer), 2.0F);

								target.knockback(0.4, -look.x, -look.z);
								target.hurtMarked = true;

								if (explosionsAddedThisShot != 2) {
									pendingExplosions.add(new DelayedExplosion(serverLevel, target, serverPlayer));
									explosionsAddedThisShot++;
								}

							}
						}
					}
				}
				mc.player.swing(InteractionHand.MAIN_HAND);
			}
		}
	}

	public static class AquaForceModule extends Module {
		private int castCooldown = 0;

		private static class TrackedWaterBomb {
			final ServerLevel level;
			final ThrownPotion potion;
			final ServerPlayer caster;
			Vec3 lastPos;

			TrackedWaterBomb(ServerLevel level, ThrownPotion potion, ServerPlayer caster) {
				this.level = level;
				this.potion = potion;
				this.caster = caster;
				this.lastPos = potion.position();
			}
		}

		private final java.util.List<TrackedWaterBomb> activeBombs = new java.util.ArrayList<>();

		public AquaForceModule() {
			super("AquaForce", Category.DARK_MAGIC, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null) return;

			if (castCooldown > 0) {
				castCooldown--;
			}

			java.util.Iterator<TrackedWaterBomb> it = activeBombs.iterator();
			while (it.hasNext()) {
				TrackedWaterBomb bomb = it.next();

				if (bomb.potion.isAlive()) {
					bomb.lastPos = bomb.potion.position();
					bomb.level.sendParticles(ParticleTypes.SPLASH, bomb.lastPos.x, bomb.lastPos.y, bomb.lastPos.z, 4, 0.1, 0.1, 0.1, 0.05);
					bomb.level.sendParticles(ParticleTypes.NAUTILUS, bomb.lastPos.x, bomb.lastPos.y, bomb.lastPos.z, 2, 0.1, 0.1, 0.1, 0.02);
				} else {
					Vec3 center = bomb.lastPos;

					bomb.level.playSound(null, BlockPos.containing(center.x, center.y, center.z),
							SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.8F, 0.85F);
					bomb.level.playSound(null, BlockPos.containing(center.x, center.y, center.z),
							SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 1.2F, 1.4F);

					bomb.level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y + 0.5, center.z, 3, 1.2, 0.5, 1.2, 0.0);
					bomb.level.sendParticles(ParticleTypes.SPLASH, center.x, center.y + 1.0, center.z, 80, 2.5, 1.5, 2.5, 0.3);
					bomb.level.sendParticles(ParticleTypes.NAUTILUS, center.x, center.y + 1.0, center.z, 50, 2.0, 1.5, 2.0, 0.2);

					double pullRadius = 12.0;
					AABB vortexBox = new AABB(
							center.x - pullRadius, center.y - 6.0, center.z - pullRadius,
							center.x + pullRadius, center.y + 6.0, center.z + pullRadius
					);

					for (LivingEntity victim : bomb.level.getEntitiesOfClass(LivingEntity.class, vortexBox)) {
						if (!victim.isAlive()) continue;

						double dist = victim.position().distanceTo(center);
						if (dist > pullRadius) continue;

						if (victim != bomb.caster) {
							victim.hurtServer(bomb.level, bomb.level.damageSources().magic(), 20.0F);
						} else {
							victim.fallDistance = 0.0F;
						}

						Vec3 pullVector = center.add(0, 0.8, 0).subtract(victim.position());
						double pullStrength = Math.min(3.5, 1.25 + (dist * 0.22));
						Vec3 pullVelocity = pullVector.normalize().scale(pullStrength);

						victim.setDeltaMovement(pullVelocity.x, Math.max(0.55, pullVelocity.y + 0.45), pullVelocity.z);
						victim.hurtMarked = true;
					}

					it.remove();
				}
			}

			if (mc.options.keyUse.isDown() && castCooldown == 0) {
				castCooldown = 12;

				if (mc.getSingleplayerServer() != null) {
					ServerPlayer serverPlayer = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
					if (serverPlayer != null) {
						ServerLevel serverLevel = serverPlayer.serverLevel();
						Vec3 look = serverPlayer.getLookAngle();

						ItemStack blueBottle = new ItemStack(Items.SPLASH_POTION);
						ThrownPotion potionEntity = new ThrownPotion(serverLevel, serverPlayer, blueBottle);

						potionEntity.setPos(
								serverPlayer.getX() + look.x * 1.5,
								serverPlayer.getEyeY() + look.y * 1.5,
								serverPlayer.getZ() + look.z * 1.5
						);

						potionEntity.shootFromRotation(serverPlayer, serverPlayer.getXRot(), serverPlayer.getYRot(), -5.0F, 2.4F, 0.5F);
						serverLevel.addFreshEntity(potionEntity);

						activeBombs.add(new TrackedWaterBomb(serverLevel, potionEntity, serverPlayer));

						serverLevel.playSound(null, serverPlayer.blockPosition(),
								SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 1.0F, 0.8F);
					}
				}
				mc.player.swing(InteractionHand.MAIN_HAND);
			}
		}
	}

	public static class EdenShardModule extends Module {
		private int castCooldown = 0;

		private final java.util.Set<java.util.UUID> edenArrows = new java.util.HashSet<>();

		public EdenShardModule() {
			super("EdenShard", Category.DARK_MAGIC, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null) return;

			if (castCooldown > 0) {
				castCooldown--;
			}

			if (mc.getSingleplayerServer() != null && !edenArrows.isEmpty()) {
				ServerPlayer serverPlayer = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
				if (serverPlayer != null) {
					ServerLevel serverLevel = serverPlayer.serverLevel();
					java.util.Iterator<java.util.UUID> it = edenArrows.iterator();

					while (it.hasNext()) {
						java.util.UUID arrowId = it.next();
						Entity entity = serverLevel.getEntity(arrowId);

						if (entity instanceof Arrow arrow && arrow.isAlive() && !(arrow.getDeltaMovement().lengthSqr() == 0.0)) {
							if (arrow.tickCount % 2 == 0) {
								serverLevel.sendParticles(ParticleTypes.END_ROD, arrow.getX(), arrow.getY(), arrow.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
							}

							AABB hitCheckBox = arrow.getBoundingBox().inflate(0.9);
							for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, hitCheckBox)) {
								if (target == serverPlayer || !target.isAlive()) continue;

								target.invulnerableTime = 0;

								target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
								target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 4));

								Vec3 arrowDir = arrow.getDeltaMovement().normalize();
								target.knockback(2.2, -arrowDir.x, -arrowDir.z);
								target.hurtMarked = true;

								serverLevel.sendParticles(ParticleTypes.ENCHANT, target.getX(), target.getEyeY(), target.getZ(), 20, 0.4, 0.5, 0.4, 0.2);
							}
						} else {
							it.remove();
						}
					}
				}
			}

			if (mc.options.keyUse.isDown() && castCooldown == 0) {
				castCooldown = 9;

				if (mc.getSingleplayerServer() != null) {
					ServerPlayer serverPlayer = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
					if (serverPlayer != null) {
						ServerLevel serverLevel = serverPlayer.serverLevel();

						serverLevel.playSound(null, serverPlayer.blockPosition(),
								SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.8F);
						serverLevel.playSound(null, serverPlayer.blockPosition(),
								SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.2F, 1.2F);

						for (int i = 0; i != 28; i++) {
							Arrow shardArrow = new Arrow(serverLevel, serverPlayer, Items.ARROW.getDefaultInstance(), null);

							float yawOffset = (float) ((Math.random() - 0.5) * 24.0);
							float pitchOffset = (float) ((Math.random() - 0.5) * 14.0);

							shardArrow.shootFromRotation(
									serverPlayer,
									serverPlayer.getXRot() + pitchOffset,
									serverPlayer.getYRot() + yawOffset,
									0.0F,
									3.5F,
									1.0F
							);

							shardArrow.setBaseDamage(180.0);
							shardArrow.setCritArrow(true);

							serverLevel.addFreshEntity(shardArrow);
							edenArrows.add(shardArrow.getUUID());
						}
					}
				}
				mc.player.swing(InteractionHand.MAIN_HAND);
			}
		}
	}

	public static class SingularityModule extends Module {
		private int castCooldown = 0;

		private static class TrackedSnowball {
			final ServerLevel level;
			final net.minecraft.world.entity.projectile.Snowball snowball;
			final ServerPlayer caster;
			Vec3 lastPos;

			TrackedSnowball(ServerLevel level, net.minecraft.world.entity.projectile.Snowball snowball, ServerPlayer caster) {
				this.level = level;
				this.snowball = snowball;
				this.caster = caster;
				this.lastPos = snowball.position();
			}
		}

		private static class ActiveBlackHole {
			final ServerLevel level;
			final Vec3 center;
			final ServerPlayer caster;
			int remainingTicks = 100;

			ActiveBlackHole(ServerLevel level, Vec3 center, ServerPlayer caster) {
				this.level = level;
				this.center = center;
				this.caster = caster;
			}
		}

		private final java.util.List<TrackedSnowball> flyingBalls = new java.util.ArrayList<>();
		private final java.util.List<ActiveBlackHole> blackHoles = new java.util.ArrayList<>();

		public SingularityModule() {
			super("Singularity", Category.DARK_MAGIC, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null) return;

			if (castCooldown > 0) {
				castCooldown--;
			}

			java.util.Iterator<TrackedSnowball> ballIt = flyingBalls.iterator();
			while (ballIt.hasNext()) {
				TrackedSnowball ball = ballIt.next();
				if (ball.snowball.isAlive()) {
					ball.lastPos = ball.snowball.position();
					ball.level.sendParticles(ParticleTypes.PORTAL, ball.lastPos.x, ball.lastPos.y, ball.lastPos.z, 5, 0.1, 0.1, 0.1, 0.05);
					ball.level.sendParticles(ParticleTypes.END_ROD, ball.lastPos.x, ball.lastPos.y, ball.lastPos.z, 1, 0.0, 0.0, 0.0, 0.0);
				} else {
					blackHoles.add(new ActiveBlackHole(ball.level, ball.lastPos, ball.caster));

					ball.level.playSound(null, BlockPos.containing(ball.lastPos.x, ball.lastPos.y, ball.lastPos.z),
							SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.5F, 1.4F);
					ballIt.remove();
				}
			}

			java.util.Iterator<ActiveBlackHole> holeIt = blackHoles.iterator();
			while (holeIt.hasNext()) {
				ActiveBlackHole hole = holeIt.next();
				hole.remainingTicks--;

				Vec3 c = hole.center;
				double singularityRadius = 16.0;

				hole.level.sendParticles(ParticleTypes.SQUID_INK, c.x, c.y + 1.2, c.z, 12, 0.8, 0.8, 0.8, 0.02);
				hole.level.sendParticles(ParticleTypes.PORTAL, c.x, c.y + 1.2, c.z, 35, 3.5, 2.0, 3.5, 0.5);

				if (hole.remainingTicks % 5 == 0) {
					hole.level.playSound(null, BlockPos.containing(c.x, c.y, c.z),
							SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.4F, 0.75F);

					double ox = (Math.random() - 0.5) * 3.0;
					double oz = (Math.random() - 0.5) * 3.0;
					hole.level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, c.x + ox, c.y + 1.0, c.z + oz, 2, 0.8, 0.5, 0.8, 0.0);
				}

				AABB gravityBox = new AABB(
						c.x - singularityRadius, c.y - 8.0, c.z - singularityRadius,
						c.x + singularityRadius, c.y + 8.0, c.z + singularityRadius
				);

				for (LivingEntity victim : hole.level.getEntitiesOfClass(LivingEntity.class, gravityBox)) {
					if (!victim.isAlive()) continue;

					double dist = victim.position().distanceTo(c);
					if (dist > singularityRadius) continue;

					Vec3 pullDir = c.add(0, 1.0, 0).subtract(victim.position());
					double pullSpeed = Math.min(1.6, 0.45 + (dist * 0.08));
					Vec3 pullVel = pullDir.normalize().scale(pullSpeed);

					victim.setDeltaMovement(pullVel.x, pullVel.y + 0.08, pullVel.z);
					victim.hurtMarked = true;

					if (victim != hole.caster) {
						if (hole.remainingTicks % 5 == 0) {
							victim.invulnerableTime = 0;
							victim.hurtServer(hole.level, hole.level.damageSources().magic(), 5.0F);
						}
					} else {
						victim.fallDistance = 0.0F;
					}
				}

				if (hole.remainingTicks == 0) {
					holeIt.remove();
				}
			}

			if (mc.options.keyUse.isDown() && castCooldown == 0) {
				castCooldown = 20;

				if (mc.getSingleplayerServer() != null) {
					ServerPlayer serverPlayer = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
					if (serverPlayer != null) {
						ServerLevel serverLevel = serverPlayer.serverLevel();
						Vec3 look = serverPlayer.getLookAngle();

						net.minecraft.world.entity.projectile.Snowball snowball =
								new net.minecraft.world.entity.projectile.Snowball(serverLevel, serverPlayer, Items.SNOWBALL.getDefaultInstance());

						snowball.setPos(
								serverPlayer.getX() + look.x * 1.5,
								serverPlayer.getEyeY() + look.y * 1.5,
								serverPlayer.getZ() + look.z * 1.5
						);

						snowball.shootFromRotation(serverPlayer, serverPlayer.getXRot(), serverPlayer.getYRot(), 0.0F, 2.6F, 0.2F);
						serverLevel.addFreshEntity(snowball);

						flyingBalls.add(new TrackedSnowball(serverLevel, snowball, serverPlayer));

						serverLevel.playSound(null, serverPlayer.blockPosition(),
								SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0F, 0.5F);
					}
				}
				mc.player.swing(InteractionHand.MAIN_HAND);
			}
		}
	}

	public static class FlightModule extends Module {
		private int antiKickTimer = 0;

		public FlightModule() {
			super("Flight", Category.MOVEMENT, GLFW.GLFW_KEY_F);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) return;

			float forward = mc.player.zza;
			float strafe = mc.player.xxa;
			double flySpeed = 0.8;

			if (forward == 0.0F && strafe == 0.0F) {
				mc.player.setDeltaMovement(0.0, 0.0, 0.0);
			} else {
				double yaw = Math.toRadians(mc.player.getYRot());
				double moveAngle = yaw - Math.atan2(strafe, forward);
				mc.player.setDeltaMovement(-Math.sin(moveAngle) * flySpeed, 0.0, Math.cos(moveAngle) * flySpeed);
			}

			double vy = 0.0;
			if (mc.options.keyJump.isDown()) {
				vy = 0.6;
			} else if (mc.options.keyShift.isDown()) {
				vy = -0.6;
			}

			antiKickTimer++;
			if (antiKickTimer >= 20) {
				antiKickTimer = 0;
				vy = -0.04;
			}

			Vec3 current = mc.player.getDeltaMovement();
			mc.player.setDeltaMovement(current.x, vy, current.z);
			mc.player.fallDistance = 0.0F;
		}
	}

	public static class ScaffoldModule extends Module {
		public ScaffoldModule() {
			super("Scaffold", Category.MISC, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null || mc.gameMode == null) return;

			BlockPos belowPos = BlockPos.containing(mc.player.getX(), mc.player.getY() - 0.5, mc.player.getZ());

			if (mc.level.getBlockState(belowPos).canBeReplaced()) {
				int blockSlot = -1;
				for (int i = 0; i != 9; i++) {
					if (mc.player.getInventory().getItem(i).getItem() instanceof BlockItem) {
						blockSlot = i;
						break;
					}
				}

				if (blockSlot != -1) {
					int oldSlot = mc.player.getInventory().selected;
					mc.player.getInventory().selected = blockSlot;

					BlockHitResult placeHit = new BlockHitResult(
							Vec3.atCenterOf(belowPos),
							Direction.UP,
							belowPos,
							false
					);
					mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, placeHit);
					mc.player.swing(InteractionHand.MAIN_HAND);

					mc.player.getInventory().selected = oldSlot;
				}
			}
		}
	}

	public static class ChestStealerModule extends Module {
		private int stealDelay = 0;

		public ChestStealerModule() {
			super("ChestStealer", Category.MISC, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.gameMode == null) return;

			if (mc.screen instanceof ContainerScreen containerScreen) {
				if (stealDelay > 0) {
					stealDelay--;
					return;
				}

				ChestMenu menu = containerScreen.getMenu();
				int containerSlots = menu.getRowCount() * 9;

				for (int i = 0; i != containerSlots; i++) {
					if (!menu.getSlot(i).getItem().isEmpty()) {
						mc.gameMode.handleInventoryMouseClick(menu.containerId, i, 0, ClickType.QUICK_MOVE, mc.player);
						stealDelay = 1;
						return;
					}
				}

				mc.player.closeContainer();
			}
		}
	}

	public static class TriggerBotModule extends Module {
		public TriggerBotModule() {
			super("TriggerBot", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.gameMode == null) return;

			if (mc.player.getAttackStrengthScale(0.5F) >= 0.95F) {
				if (mc.crosshairPickEntity instanceof LivingEntity target && target.isAlive() && target != mc.player) {
					mc.gameMode.attack(mc.player, target);
					mc.player.swing(InteractionHand.MAIN_HAND);
				}
			}
		}
	}

	public static class BlinkModule extends Module {
		public static final java.util.Queue<ServerboundMovePlayerPacket> packetQueue = new java.util.concurrent.ConcurrentLinkedQueue<>();
		public static boolean isFlushing = false;
		private Vec3 startSoulPos = null;

		public BlinkModule() {
			super("Blink", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void toggle() {
			super.toggle();
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) return;

			if (isEnabled()) {
				startSoulPos = mc.player.position();
				packetQueue.clear();
			} else {
				if (mc.getConnection() != null && !packetQueue.isEmpty()) {
					isFlushing = true;
					try {
						while (!packetQueue.isEmpty()) {
							mc.getConnection().send(packetQueue.poll());
						}
					} finally {
						isFlushing = false;
					}
				}
				startSoulPos = null;
			}
		}

		@Override
		public void onRender3D(WorldRenderContext context) {
			if (startSoulPos == null) return;
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) return;

			PoseStack poseStack = context.matrixStack();
			Camera camera = context.camera();
			if (poseStack == null || camera == null) return;

			Vec3 camPos = camera.getPosition();
			Vec3 soulStart = startSoulPos.add(0, 1.0, 0).subtract(camPos);
			Vec3 currentBody = mc.player.position().add(0, 1.0, 0).subtract(camPos);

			RenderSystem.disableDepthTest();
			VertexConsumer lineConsumer = context.consumers().getBuffer(RenderType.lines());
			PoseStack.Pose pose = poseStack.last();

			lineConsumer.addVertex(pose, (float) soulStart.x, (float) soulStart.y, (float) soulStart.z)
					.setColor(0.0f, 1.0f, 1.0f, 1.0f)
					.setNormal(pose, 0.0f, 1.0f, 0.0f);

			lineConsumer.addVertex(pose, (float) currentBody.x, (float) currentBody.y, (float) currentBody.z)
					.setColor(0.0f, 1.0f, 1.0f, 1.0f)
					.setNormal(pose, 0.0f, 1.0f, 0.0f);

			RenderSystem.enableDepthTest();
		}
	}

	public static class FastEatModule extends Module {
		public FastEatModule() {
			super("FastEat", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.getConnection() == null) return;

			if (mc.player.isUsingItem() && mc.player.getUseItem().has(net.minecraft.core.component.DataComponents.FOOD)) {
				if (mc.player.getTicksUsingItem() >= 2) {
					for (int i = 0; i != 32; i++) {
						mc.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(
								mc.player.onGround(),
								mc.player.horizontalCollision
						));
					}

					if (mc.getSingleplayerServer() != null) {
						ServerPlayer sp = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
						if (sp != null && sp.isUsingItem()) {
							sp.getUseItem().finishUsingItem(sp.serverLevel(), sp);
							sp.stopUsingItem();
						}
					}

					mc.player.stopUsingItem();
				}
			}
		}
	}

	public static class LiquidsModule extends Module {
		private int breakCooldown = 0;

		public LiquidsModule() {
			super("Liquids", Category.GRIEFING, GLFW.GLFW_KEY_UNKNOWN);
		}

		@Override
		public void onTick() {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null) return;

			if (breakCooldown > 0) {
				breakCooldown--;
			}

			if (mc.options.keyAttack.isDown() && breakCooldown == 0) {
				if (mc.hitResult instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {
					BlockPos targetPos = blockHit.getBlockPos();
					Block targetBlock = mc.level.getBlockState(targetPos).getBlock();

					if (targetBlock == Blocks.LAVA || targetBlock == Blocks.WATER) {
						breakCooldown = 3; // 0.15s

						if (mc.getSingleplayerServer() != null) {
							ServerPlayer sp = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
							if (sp != null) {
								ServerLevel serverLevel = sp.serverLevel();
								serverLevel.setBlockAndUpdate(targetPos, Blocks.AIR.defaultBlockState());
								serverLevel.playSound(null, targetPos,
										SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 1.3F);
								serverLevel.sendParticles(ParticleTypes.CLOUD,
										targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
										8, 0.2, 0.2, 0.2, 0.02);
							}
						}
						mc.player.swing(InteractionHand.MAIN_HAND);
					}
				}
			}
		}
	}
}