package com.myclient.mixin;

import com.myclient.MyClient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true)
    private static void onShouldRenderFace(BlockState state, BlockState adjacentState, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (!MyClient.isModuleEnabled("X-Ray")) return;

        Block block = state.getBlock();

        boolean isValuable = block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE ||
                block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE ||
                block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE ||
                block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE ||
                block == Blocks.ANCIENT_DEBRIS || block == Blocks.CHEST || block == Blocks.SPAWNER;

        cir.setReturnValue(isValuable);
    }
}
