package com.yumocmspor.plugin.providers.client;

import com.yumocmspor.block.BaseIOBlock;
import com.yumocmspor.plugin.CMPJadePlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;


public enum ActiveComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(
            ITooltip tooltip,
            BlockAccessor accessor,
            IPluginConfig config
    ) {
        tooltip.add(Component.literal("Active: "+ accessor.getBlockState().getValue(BaseIOBlock.ACTIVE)));
    }

    @Override
    public ResourceLocation getUid() {
        return CMPJadePlugin.ACTIVE;
    }
}
