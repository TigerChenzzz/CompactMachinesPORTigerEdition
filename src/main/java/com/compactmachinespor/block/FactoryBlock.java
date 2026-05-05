package com.compactmachinespor.block;

import com.mojang.serialization.MapCodec;
import com.compactmachinespor.Cyumocompactmachinespor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

public class FactoryBlock extends BaseEntityBlock {
    public static final MapCodec<FactoryBlock> CODEC = simpleCodec(FactoryBlock::new);

    public FactoryBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .strength(3.0f, 6.0f));
    }

    public FactoryBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FactoryBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, Cyumocompactmachinespor.FACTORY_BLOCK_ENTITY.get(), FactoryBlockEntity::tick);
    }
}
