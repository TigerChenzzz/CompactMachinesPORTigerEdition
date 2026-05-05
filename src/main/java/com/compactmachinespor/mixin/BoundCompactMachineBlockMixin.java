package com.compactmachinespor.mixin;

import com.compactmachinespor.Cyumocompactmachinespor;
import com.compactmachinespor.block.EvaluatorBlockEntity;
import com.compactmachinespor.core.Core;
import dev.compactmods.machines.machine.block.BoundCompactMachineBlock;
import dev.compactmods.machines.machine.block.BoundCompactMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(BoundCompactMachineBlock.class)
public class BoundCompactMachineBlockMixin {
    @Inject(
            method = "useItemOn",
            at = @At("HEAD"),
            cancellable = true
    )
    private void interceptUseItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<ItemInteractionResult> cir
    ) {
        if (stack.is(Cyumocompactmachinespor.LAUNCHER_STICK)) {
            if (level.getBlockEntity(pos) instanceof BoundCompactMachineBlockEntity) {
                stack.shrink(1);
                if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                    player.inventoryMenu.broadcastChanges();
                    String roomCode = ((BoundCompactMachineBlockEntity) Objects.requireNonNull(level.getBlockEntity(pos))).connectedRoom();
                    Core.replaceBlock(serverLevel, pos, Cyumocompactmachinespor.EVALUATOR_BLOCK);
                    ((EvaluatorBlockEntity) Objects.requireNonNull(serverLevel.getBlockEntity(pos))).setRoomCode(roomCode);
                    cir.setReturnValue(ItemInteractionResult.SUCCESS);
                }
            }
        }
    }
}
