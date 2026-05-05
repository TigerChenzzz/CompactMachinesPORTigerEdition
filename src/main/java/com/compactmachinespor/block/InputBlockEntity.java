package com.compactmachinespor.block;

import com.compactmachinespor.Cyumocompactmachinespor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class InputBlockEntity extends BaseIOBlockEntity {

    private final IItemHandler itemHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return items.size();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            if (slot < 0 || slot >= items.size()) return ItemStack.EMPTY;
            return new ItemStack(BuiltInRegistries.ITEM.get(items.get(slot)), Integer.MAX_VALUE);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!isActive() || slot < 0 || slot >= items.size()) return ItemStack.EMPTY;
            ItemStack itemStack = new ItemStack(BuiltInRegistries.ITEM.get(items.get(slot)), amount);
            if (!simulate) handle(itemStack);
            return itemStack;
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    };

    private final IFluidHandler fluidHandler = new IFluidHandler() {
        @Override
        public int getTanks() {
            return fluids.size();
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            if (tank < 0 || tank >= fluids.size()) return FluidStack.EMPTY;
            return new FluidStack(BuiltInRegistries.FLUID.get(fluids.get(tank)), Integer.MAX_VALUE);
        }

        @Override
        public int getTankCapacity(int tank) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (!isActive()) return FluidStack.EMPTY;
            if (fluids.contains(BuiltInRegistries.FLUID.getKey(resource.getFluid()))) {
                if (action == FluidAction.EXECUTE) handle(resource);
                return resource.copy();
            }
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (!isActive() || maxDrain < 0 || fluids.isEmpty()) return FluidStack.EMPTY;
            FluidStack fluidStack = new FluidStack(BuiltInRegistries.FLUID.get(fluids.getFirst()), maxDrain);
            if (action == FluidAction.EXECUTE) handle(fluidStack);
            return fluidStack;
        }
    };

    private final IEnergyStorage energyHandler = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (isActive()) {
                if (!simulate) handle(maxExtract);
                return maxExtract;
            }
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getMaxEnergyStored() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean canExtract() {
            return isActive();
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    };

    public InputBlockEntity(BlockPos pos, BlockState state) {
        super(Cyumocompactmachinespor.INPUT_BLOCK_ENTITY.get(), pos, state);
    }


    @Override
    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    @Override
    public IEnergyStorage getEnergyHandler() {
        return energyHandler;
    }
}
