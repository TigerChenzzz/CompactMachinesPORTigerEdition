package com.yumocmspor.block;

import com.yumocmspor.core.Core;
import com.yumocmspor.yumocompactmachinespor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class EvaluatorBlockEntity extends RoomCodeBlockEntity {
    public EvaluatorBlockEntity(BlockPos pos, BlockState blockState) {
        super(yumocompactmachinespor.EVALUATOR_BLOCK_ENTITY.get(), pos, blockState);
    }

    public void trigger() {
        if (roomCode != null && !roomCode.isEmpty() && getLevel() instanceof ServerLevel serverLevel) {
            Core.createMachine(serverLevel, roomCode, getBlockPos());
        }
    }
}
