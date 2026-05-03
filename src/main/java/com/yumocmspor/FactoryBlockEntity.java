package com.yumocmspor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class FactoryBlockEntity extends BlockEntity {
    private static final int EVALUATE_SECONDS = Config.EVALUATE_SECONDS.get();
    private static final int TPS = 20;
    public Map<Holder<?>, Data> InputData = new HashMap<>();
    public Map<Holder<?>, Data> OutputData = new HashMap<>();
    public List<Data> EnergyData = null;

    public FactoryBlockEntity(BlockPos pos, BlockState state) {
        super(yumocompactmachinespor.FACTORY_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FactoryBlockEntity blockEntity) {
        // Factory ticking logic
    }

    public Data newData() {
        return new Data(new int[EVALUATE_SECONDS * TPS], new AtomicInteger(0));
    }

    public void addData(DataSetType type, Holder<?> id, int data) {
        Map<Holder<?>, Data> set = switch (type) {
            case Input -> InputData;
            case Output -> OutputData;
        };
        if (!set.containsKey(id)) {
            set.put(id, newData());
        }
        Data dataInner = set.get(id);
        dataAdd(dataInner, data);
    }

    public void dataAdd(Data dataInner, int add) {
        int index = dataInner.size.getAndIncrement();
        if (index < dataInner.data.length) {
            dataInner.data[index] = add;
        }
    }

    public void addEnergyData(DataSetType type, int data) {
        if (EnergyData == null) {
            EnergyData = List.of(newData(), newData());
        }
        switch (type) {
            case Input:
                dataAdd(EnergyData.getFirst(), data);
                break;
            case Output:
                dataAdd(EnergyData.getLast(), data);
        }
    }

    public void clear() {
        InputData.clear();
        OutputData.clear();
        EnergyData = null;
    }

    public enum DataSetType {
        Input, Output
    }

    public record Data(int[] data, AtomicInteger size) {
    }
}
