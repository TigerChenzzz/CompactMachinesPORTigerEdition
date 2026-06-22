package com.compactmachinespor.block;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.compactmachinespor.Config;
import com.compactmachinespor.Cyumocompactmachinespor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

public class FactoryBlockEntity extends RoomCodeBlockEntity {
    public static class Container {
        public final double capacity;
        public final double rate;
        public double amount;

        public Container(double capacity, double rate) {
            this.capacity = capacity;
            this.rate = rate;
        }

        // input: 外界输入工厂，实际内容减少
        public int simulateInput() {
            return (int)(amount / rate);
        }

        public void operateInput(int times) {
            amount = Math.max(0, amount - rate * times);
        }

        // input: 工厂输出外界，实际内容增加
        public int simulateOutput() {
            return (int)((capacity - amount) / rate);
        }

        public void operateOutput(int times) {
            amount = Math.min(capacity, amount + rate * times);
        }
    }

    private static Container newContainer(double rate, double minCapacity) {
        double expectedCapacity = Math.floor(rate * Config.FACTORY_CACHE_TIME.get() * 20);
        double capacity = Math.max(minCapacity, expectedCapacity);
        return new Container(capacity, rate);
    }
    private static Container newItemContainer(double rate) {
        return newContainer(rate, 64);
    }
    private static Container newFluidContainer(double rate) {
        return newContainer(rate, 1000);
    }
    private static Container newEnergyContainer(double rate) {
        return newContainer(rate, 1000);
    }

    private static final int TICK_LOOP = 20;

    private int tickCount = 0;
    private boolean lazyLoad = false;
    private final Map<Item, Container> inputItems = new LinkedHashMap<>();
    private final Map<Item, Container> outputItems = new LinkedHashMap<>();

    private final Map<Fluid, Container> inputFluids = new LinkedHashMap<>();
    private final Map<Fluid, Container> outputFluids = new LinkedHashMap<>();

    private final List<Item> inputItemList = new ArrayList<>();
    private final List<Item> outputItemList = new ArrayList<>();
    private final List<Fluid> inputFluidList = new ArrayList<>();
    private final List<Fluid> outputFluidList = new ArrayList<>();

    private Container inputEnergy = null;
    private Container outputEnergy = null;

    private void updateLists() {
        inputItemList.clear();
        inputItemList.addAll(inputItems.keySet());
        outputItemList.clear();
        outputItemList.addAll(outputItems.keySet());
        inputFluidList.clear();
        inputFluidList.addAll(inputFluids.keySet());
        outputFluidList.clear();
        outputFluidList.addAll(outputFluids.keySet());
    }

    private final IItemHandler itemHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return inputItemList.size() + outputItemList.size();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            int inputSize = inputItemList.size();
            if (slot < inputSize) {
                Item item = inputItemList.get(slot);
                Container c = inputItems.get(item);
                return new ItemStack(item, (int)c.amount);
            } else {
                int outputSlot = slot - inputSize;
                if (outputSlot < outputItemList.size()) {
                    Item item = outputItemList.get(outputSlot);
                    Container c = outputItems.get(item);
                    return new ItemStack(item, (int)c.amount);
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int inputSize = inputItemList.size();
            if (slot < inputSize) {
                Item item = inputItemList.get(slot);
                if (item == stack.getItem()) {
                    Container c = inputItems.get(item);
                    int space = (int)(c.capacity - c.amount);
                    int toAdd = Math.min(space, stack.getCount());
                    if (!simulate && toAdd > 0) {
                        c.amount += toAdd;
                        setChanged();
                        lazyLoad = false;
                    }
                    if (toAdd == stack.getCount()) return ItemStack.EMPTY;
                    ItemStack result = stack.copy();
                    result.shrink(toAdd);
                    return result;
                }
            }
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int inputSize = inputItemList.size();
            if (slot >= inputSize) {
                int outputSlot = slot - inputSize;
                if (outputSlot < outputItemList.size()) {
                    Item item = outputItemList.get(outputSlot);
                    Container c = outputItems.get(item);
                    int toExtract = Math.min((int)c.amount, amount);
                    ItemStack result = new ItemStack(item, toExtract);
                    if (!simulate && toExtract > 0) {
                        c.amount -= toExtract;
                        setChanged();
                        lazyLoad = false;
                    }
                    return result;
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            int inputSize = inputItemList.size();
            if (slot < inputSize) {
                return (int)inputItems.get(inputItemList.get(slot)).capacity;
            } else {
                int outputSlot = slot - inputSize;
                if (outputSlot < outputItemList.size()) {
                    return (int)outputItems.get(outputItemList.get(outputSlot)).capacity;
                }
            }
            return 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            int inputSize = inputItemList.size();
            if (slot < inputSize) {
                return inputItemList.get(slot) == stack.getItem();
            }
            return false;
        }
    };

    private final IFluidHandler fluidHandler = new IFluidHandler() {
        @Override
        public int getTanks() {
            return inputFluidList.size() + outputFluidList.size();
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            int inputSize = inputFluidList.size();
            if (tank < inputSize) {
                Fluid fluid = inputFluidList.get(tank);
                Container c = inputFluids.get(fluid);
                return new FluidStack(fluid, (int)c.amount);
            } else {
                int outputTank = tank - inputSize;
                if (outputTank < outputFluidList.size()) {
                    Fluid fluid = outputFluidList.get(outputTank);
                    Container c = outputFluids.get(fluid);
                    return new FluidStack(fluid, (int)c.amount);
                }
            }
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            int inputSize = inputFluidList.size();
            if (tank < inputSize) {
                return (int)inputFluids.get(inputFluidList.get(tank)).capacity;
            } else {
                int outputTank = tank - inputSize;
                if (outputTank < outputFluidList.size()) {
                    return (int)outputFluids.get(outputFluidList.get(outputTank)).capacity;
                }
            }
            return 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            int inputSize = inputFluidList.size();
            if (tank < inputSize) {
                return inputFluidList.get(tank) == stack.getFluid();
            }
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            int totalFilled = 0;
            FluidStack toFill = resource.copy();
            for (Map.Entry<Fluid, Container> entry : inputFluids.entrySet()) {
                if (entry.getKey() == toFill.getFluid()) {
                    Container c = entry.getValue();
                    int space = (int)(c.capacity - c.amount);
                    int filled = Math.min(space, toFill.getAmount());
                    if (action.execute() && filled > 0) {
                        c.amount += filled;
                        setChanged();
                        lazyLoad = false;
                    }
                    totalFilled += filled;
                    toFill.shrink(filled);
                    if (toFill.isEmpty()) break;
                }
            }
            return totalFilled;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            int totalDrained = 0;
            for (Map.Entry<Fluid, Container> entry : outputFluids.entrySet()) {
                if (entry.getKey() == resource.getFluid()) {
                    Container c = entry.getValue();
                    int drained = Math.min((int)c.amount, resource.getAmount() - totalDrained);
                    if (action.execute() && drained > 0) {
                        c.amount -= drained;
                        setChanged();
                        lazyLoad = false;
                    }
                    totalDrained += drained;
                    if (totalDrained >= resource.getAmount()) break;
                }
            }
            return new FluidStack(resource.getFluid(), totalDrained);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            for (Map.Entry<Fluid, Container> entry : outputFluids.entrySet()) {
                Container c = entry.getValue();
                if (c.amount > 0) {
                    int toDrain = Math.min((int)c.amount, maxDrain);
                    FluidStack result = new FluidStack(entry.getKey(), toDrain);
                    if (action.execute()) {
                        c.amount -= toDrain;
                        setChanged();
                        lazyLoad = false;
                    }
                    return result;
                }
            }
            return FluidStack.EMPTY;
        }
    };

    private final IEnergyStorage energyHandler = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (inputEnergy == null || maxReceive <= 0) return 0;
            int received = Math.min(maxReceive, (int)(inputEnergy.capacity - inputEnergy.amount));
            if (!simulate && received > 0) {
                inputEnergy.amount += received;
                setChanged();
                lazyLoad = false;
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (outputEnergy == null || maxExtract <= 0) return 0;
            int extracted = Math.min(maxExtract, (int)outputEnergy.amount);
            if (!simulate && extracted > 0) {
                outputEnergy.amount -= extracted;
                setChanged();
                lazyLoad = false;
            }
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            double total = 0;
            if (inputEnergy != null) total += inputEnergy.amount;
            if (outputEnergy != null) total += outputEnergy.amount;
            return (int)total;
        }

        @Override
        public int getMaxEnergyStored() {
            double total = 0;
            if (inputEnergy != null) total += inputEnergy.capacity;
            if (outputEnergy != null) total += outputEnergy.capacity;
            return (int)total;
        }

        @Override
        public boolean canExtract() {
            return outputEnergy != null;
        }

        @Override
        public boolean canReceive() {
            return inputEnergy != null;
        }
    };

    public FactoryBlockEntity(BlockPos pos, BlockState state) {
        super(Cyumocompactmachinespor.FACTORY_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void loadCommon(CompoundTag tag) {
        super.loadCommon(tag);
        loadItemMap(tag, "input_items", inputItems);
        loadItemMap(tag, "output_items", outputItems);
        loadFluidMap(tag, "input_fluids", inputFluids);
        loadFluidMap(tag, "output_fluids", outputFluids);
        inputEnergy = loadEnergy(tag, "input_energy");
        outputEnergy = loadEnergy(tag, "output_energy");
        if (tag.contains("original_attachments")) {
            originalAttachments = tag.getCompound("original_attachments");
        }
        updateLists();
    }

    @Override
    protected void saveCommon(CompoundTag tag) {
        super.saveCommon(tag);
        saveItemMap(tag, "input_items", inputItems);
        saveItemMap(tag, "output_items", outputItems);
        saveFluidMap(tag, "input_fluids", inputFluids);
        saveFluidMap(tag, "output_fluids", outputFluids);
        saveEnergy(tag, "input_energy", inputEnergy);
        saveEnergy(tag, "output_energy", outputEnergy);
        if (originalAttachments != null) {
            tag.put("original_attachments", originalAttachments);
        }
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public IEnergyStorage getEnergyHandler() {
        return energyHandler;
    }

    // Only call it once for the block, so it's safe to do no clear.
    public void initTanks(Map<Holder<?>, Double> inputMap, Map<Holder<?>, Double> outputMap) {
        parseConfigMap(inputMap, true);
        parseConfigMap(outputMap, false);
        updateLists();
        setChanged();
    }

    private void parseConfigMap(Map<Holder<?>, Double> configMap, boolean isInput) {
        for (Map.Entry<Holder<?>, Double> entry : configMap.entrySet()) {
            Holder<?> holder = entry.getKey();
            Object value = holder.value();
            double rate = entry.getValue();
            if (rate <= 0) continue;

            switch (value) {
                case null -> {
                    if (isInput) {
                        inputEnergy = newEnergyContainer(rate);
                    } else {
                        outputEnergy = newEnergyContainer(rate);
                    }
                }
                case Item item -> {
                    if (isInput) {
                        inputItems.put(item, newItemContainer(rate));
                    } else {
                        outputItems.put(item, newItemContainer(rate));
                    }
                }
                case Fluid fluid -> {
                    if (isInput) {
                        inputFluids.put(fluid, newFluidContainer(rate));
                    } else {
                        outputFluids.put(fluid, newFluidContainer(rate));
                    }
                }
                default -> {
                }
            }
        }
    }

    private boolean tryWork() {
        int times = TICK_LOOP;
        if ((times = simulate(inputItems, times, true)) == 0) return false;
        if ((times = simulate(outputItems, times, false)) == 0) return false;
        if ((times = simulate(inputFluids, times, true)) == 0) return false;
        if ((times = simulate(outputFluids, times, false)) == 0) return false;
        if ((times = simulate(inputEnergy, times, true)) == 0) return false;
        if ((times = simulate(outputEnergy, times, false)) == 0) return false;
        
        operate(inputItems, times, true);
        operate(outputItems, times, false);
        operate(inputFluids, times, true);
        operate(outputFluids, times, false);
        operate(inputEnergy, times, true);
        operate(outputEnergy, times, false);
        return true;
    }
    private static int simulate(Map<?, Container> map, int times, boolean inputOrOutput) {
        if (map == null || map.isEmpty()) return times;
        for (Container c : map.values()) {
            if ((times = simulate(c, times, inputOrOutput)) == 0) return 0;
        }
        return times;
    }
    private static int simulate(Container c, int times, boolean inputOrOutput) {
        if (c == null) return times;
        int s = inputOrOutput ? c.simulateInput() : c.simulateOutput();
        if (s == 0) return 0;
        if (s > 0) return Math.min(times, s);
        return times;
    }
    private static void operate(Map<?, Container> map, int times, boolean inputOrOutput) {
        if (map == null || map.isEmpty()) return;
        if (inputOrOutput) {
            for (Container c : map.values()) c.operateInput(times);
        }
        else {
            for (Container c : map.values()) c.operateOutput(times);
        }
    }
    private static void operate(Container c, int times, boolean inputOrOutput) {
        if (c == null) return;
        if (inputOrOutput) {
            c.operateInput(times);
        }
        else {
            c.operateOutput(times);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FactoryBlockEntity be) {
        if (level.isClientSide) {
            return;
        }
        be.tickCount++;
        if (be.tickCount >= TICK_LOOP) {
            be.tickCount -= TICK_LOOP;
            if (be.lazyLoad) {
                return;
            }
            be.lazyLoad = !be.tryWork();
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        CustomData customData = componentInput.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            loadCommon(customData.copyTag());
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        CompoundTag tag = new CompoundTag();
        saveCommon(tag);
        builder.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void saveItemMap(CompoundTag tag, String key, Map<Item, Container> map) {
        CompoundTag mapTag = new CompoundTag();
        for (Map.Entry<Item, Container> entry : map.entrySet()) {
            ResourceLocation rl = BuiltInRegistries.ITEM.getKey(entry.getKey());
            CompoundTag c = new CompoundTag();
            var container = entry.getValue();
            c.putDouble("amount", container.amount);
            c.putDouble("rate", container.rate);
            mapTag.put(rl.toString(), c);
        }
        tag.put(key, mapTag);
    }

    public static void loadItemMap(CompoundTag tag, String key, Map<Item, Container> map) {
        map.clear();
        CompoundTag mapTag = tag.getCompound(key);
        for (String k : mapTag.getAllKeys()) {
            ResourceLocation id = ResourceLocation.parse(k);
            if (BuiltInRegistries.ITEM.containsKey(id)) {
                Item item = BuiltInRegistries.ITEM.get(id);
                CompoundTag c = mapTag.getCompound(k);
                var container = newItemContainer(c.getDouble("rate"));
                container.amount = Math.clamp(c.getDouble("amount"), 0, container.capacity);
                map.put(item, container);
            }
        }
    }

    public static void saveFluidMap(CompoundTag tag, String key, Map<Fluid, Container> map) {
        CompoundTag mapTag = new CompoundTag();
        for (Map.Entry<Fluid, Container> entry : map.entrySet()) {
            ResourceLocation rl = BuiltInRegistries.FLUID.getKey(entry.getKey());
            CompoundTag c = new CompoundTag();
            Container container = entry.getValue();
            c.putDouble("amount", container.amount);
            c.putDouble("rate", container.rate);
            mapTag.put(rl.toString(), c);
        }
        tag.put(key, mapTag);
    }

    public static void loadFluidMap(CompoundTag tag, String key, Map<Fluid, Container> map) {
        map.clear();
        CompoundTag mapTag = tag.getCompound(key);
        for (String k : mapTag.getAllKeys()) {
            ResourceLocation id = ResourceLocation.parse(k);
            if (BuiltInRegistries.FLUID.containsKey(id)) {
                Fluid fluid = BuiltInRegistries.FLUID.get(id);
                CompoundTag c = mapTag.getCompound(k);
                var container = newItemContainer(c.getDouble("rate"));
                container.amount = Math.clamp(c.getDouble("amount"), 0, container.capacity);
                map.put(fluid, container);
            }
        }
    }

    public static void saveEnergy(CompoundTag tag, String key, Container energy) {
        if (energy != null) {
            CompoundTag c = new CompoundTag();
            c.putDouble("amount", energy.amount);
            c.putDouble("rate", energy.rate);
            tag.put(key, c);
        }
    }

    public static Container loadEnergy(CompoundTag tag, String key) {
        if (tag.contains(key)) {
            CompoundTag c = tag.getCompound(key);
            var container = newEnergyContainer(c.getDouble("rate"));
            container.amount = Math.clamp(c.getDouble("amount"), 0, container.capacity);
            return container;
        }
        return null;
    }

    // 原始 machine_color 所在 neoforge:attachments，由 Core.finish() 设置
    private CompoundTag originalAttachments;

    public CompoundTag getOriginalAttachments() {
        return originalAttachments;
    }

    public void setOriginalAttachments(CompoundTag attachments) {
        this.originalAttachments = attachments;
        setChanged();
    }

    public List<List<?>> getForShow() {
        return List.of(inputItemList, inputFluidList, outputItemList, outputFluidList);
    }
}