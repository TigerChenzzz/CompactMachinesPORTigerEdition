package com.yumocmspor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseIOBlockEntity extends BlockEntity {
    private final boolean DEBUG = true;
    protected List<ResourceLocation> items = new ArrayList<>();
    protected List<ResourceLocation> fluids = new ArrayList<>();
    protected WeakReference<FactoryBlockEntity> master = new WeakReference<>(null);

    public BaseIOBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract IItemHandler getItemHandler();

    public abstract IFluidHandler getFluidHandler();

    public abstract IEnergyStorage getEnergyHandler();

    protected boolean checkOrDelete() {
        if (DEBUG) {
            master = new WeakReference<>((FactoryBlockEntity) getLevel().getBlockEntity(getBlockPos().below(2)));
        }
        if (master.get() == null) {
            setRemoved();
            this.invalidateCapabilities();
            if (getLevel() != null) {

                getLevel().destroyBlock(getBlockPos(), true);
            }
            return false;
        }
        return true;
    }

    protected void handle(ItemStack itemStack) {
        handle(itemStack.getItemHolder(), itemStack.getCount());
    }

    protected void handle(FluidStack fluidStack) {
        handle(fluidStack.getFluidHolder(), fluidStack.getAmount());
    }

    protected FactoryBlockEntity.DataSetType getDataSetType() {
        if (this instanceof InputBlockEntity) {
            return FactoryBlockEntity.DataSetType.Input;
        } else if (this instanceof OutputBlockEntity) {
            return FactoryBlockEntity.DataSetType.Output;
        } else {
            return null;
        }
    }

    protected void handle(Holder<?> holder, int count) {
        if (!checkOrDelete()) return;
        master.get().addData(
                getDataSetType(),
                holder,
                count);
    }

    protected void handle(int energy) {
        if (!checkOrDelete()) return;
        master.get().addEnergyData(
                getDataSetType(),
                energy);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        if (tag.contains("items", Tag.TAG_LIST)) {
            ListTag list = tag.getList("items", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                ResourceLocation rl = ResourceLocation.tryParse(list.getString(i));
                if (rl != null && BuiltInRegistries.ITEM.containsKey(rl)) items.add(rl);
            }
        }
        fluids.clear();
        if (tag.contains("fluids", Tag.TAG_LIST)) {
            ListTag list = tag.getList("fluids", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                ResourceLocation rl = ResourceLocation.tryParse(list.getString(i));
                if (rl != null && BuiltInRegistries.FLUID.containsKey(rl)) fluids.add(rl);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag itemsTag = new ListTag();
        for (ResourceLocation id : items) {
            itemsTag.add(StringTag.valueOf(id.toString()));
        }
        tag.put("items", itemsTag);

        ListTag fluidsTag = new ListTag();
        for (ResourceLocation id : fluids) {
            fluidsTag.add(StringTag.valueOf(id.toString()));
        }
        tag.put("fluids", fluidsTag);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
