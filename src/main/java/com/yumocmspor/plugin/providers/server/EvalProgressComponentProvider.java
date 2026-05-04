package com.yumocmspor.plugin.providers.server;

import com.yumocmspor.block.EvaluatorBlockEntity;
import com.yumocmspor.core.Core;
import com.yumocmspor.core.Machine;
import com.yumocmspor.plugin.CMPJadePlugin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;


public enum EvalProgressComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(
            ITooltip tooltip,
            BlockAccessor accessor,
            IPluginConfig config
    ) {
        tooltip.add(Component.literal("Progress: "+ accessor.getServerData().getInt("pg")));
    }

    @Override
    public ResourceLocation getUid() {
        return CMPJadePlugin.PROGRESS;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor blockAccessor) {
        var be =((EvaluatorBlockEntity)blockAccessor.getBlockEntity());
        if (Core.getMachine(be.roomCode) != null){
        int pg = Math.round(
                (be.getLevel().getServer().getTickCount() - Core.getMachine(be.roomCode).StartTick.get())
                        / (Machine.EVALUATE_SECONDS * 20f)*100);
        data.putInt("pg",pg);
        }
    }
}
