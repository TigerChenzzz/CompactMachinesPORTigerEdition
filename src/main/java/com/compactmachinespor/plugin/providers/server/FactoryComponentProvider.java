package com.compactmachinespor.plugin.providers.server;

import com.compactmachinespor.block.FactoryBlockEntity;
import com.compactmachinespor.plugin.CMPJadePlugin;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import java.util.List;


public enum FactoryComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    public static IElement getIcon(ItemStack stack) {
        IElementHelper elements = IElementHelper.get();
        return elements.item(stack, 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1));
    }

    public static IElement getIcon(Item item) {
        return getIcon(new ItemStack(item));
    }

    public static IElement getIcon(Object object) {
        return switch (object) {
            case Item item -> getIcon(item);
            case ItemStack itemStack -> getIcon(itemStack);
            case Fluid fluid -> getIcon(fluid);
            default -> throw new IllegalStateException("Unexpected value: " + object);
        };
    }

    public static IElement getIcon(Fluid fluid) {
        IElementHelper elements = IElementHelper.get();
        return elements.fluid(JadeFluidObject.of(fluid)).size(new Vec2(10, 10)).translate(new Vec2(0, -1));
    }

    public static int getId(Object obj) {
        return switch (obj) {
            case Item i -> BuiltInRegistries.ITEM.getId(i);
            case Fluid f -> BuiltInRegistries.FLUID.getId(f);
            default -> 0;
        };
    }

    public static int[] getIntArray(List<?> list) {
        int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = getId(list.get(i));
        }
        return result;
    }

    public static void writeIntArray(ITooltip tooltip, int[] array, Registry<?> registry) {
        for (int i = 0; i < array.length; i++) {
            tooltip.append(getIcon(registry.getHolder(array[i]).get().value()));
            if (i != array.length - 1) {
                tooltip.append(Component.literal("+"));
            }
        }
    }

    @Override
    public void appendTooltip(
            ITooltip tooltip,
            BlockAccessor accessor,
            IPluginConfig config
    ) {
        CompoundTag serverData = accessor.getServerData();
        tooltip.add(Component.literal(""));
        writeIntArray(tooltip, serverData.getIntArray("ii"), BuiltInRegistries.ITEM);
        if (serverData.getIntArray("ii").length > 0 && serverData.getIntArray("if").length > 0) {
            tooltip.append(Component.literal("+"));
        }
        writeIntArray(tooltip, serverData.getIntArray("if"), BuiltInRegistries.FLUID);

        tooltip.append(Component.literal("->"));

        writeIntArray(tooltip, serverData.getIntArray("oi"), BuiltInRegistries.ITEM);
        if (serverData.getIntArray("oi").length > 0 && serverData.getIntArray("of").length > 0) {
            tooltip.append(Component.literal("+"));
        }
        writeIntArray(tooltip, serverData.getIntArray("of"), BuiltInRegistries.FLUID);
    }

    @Override
    public ResourceLocation getUid() {
        return CMPJadePlugin.PRODUCE;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor blockAccessor) {
        FactoryBlockEntity be = (FactoryBlockEntity) blockAccessor.getBlockEntity();
        List<List<?>> forShow = be.getForShow();
        data.putIntArray("ii", getIntArray(forShow.get(0)));
        data.putIntArray("if", getIntArray(forShow.get(1)));
        data.putIntArray("oi", getIntArray(forShow.get(2)));
        data.putIntArray("of", getIntArray(forShow.get(3)));
    }
}

