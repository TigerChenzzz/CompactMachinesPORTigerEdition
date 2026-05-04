package com.yumocmspor.plugin.providers.server;

import com.yumocmspor.block.RoomCodeBlockEntity;
import com.yumocmspor.plugin.CMPJadePlugin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;


public enum RoomCodeComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(
            ITooltip tooltip,
            BlockAccessor accessor,
            IPluginConfig config
    ) {
        tooltip.add(Component.literal("RoomCode: "+ accessor.getServerData().getString("rc")));
    }

    @Override
    public ResourceLocation getUid() {
        return CMPJadePlugin.ROOM_CODE;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor blockAccessor) {
        String rc=((RoomCodeBlockEntity)blockAccessor.getBlockEntity()).roomCode;
        if (rc!=null && !rc.isEmpty()){
        data.putString("rc",rc);}
    }
}
