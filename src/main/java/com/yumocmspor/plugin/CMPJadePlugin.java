package com.yumocmspor.plugin;

import com.yumocmspor.block.EvaluatorBlock;
import com.yumocmspor.block.FactoryBlock;
import com.yumocmspor.block.InputBlock;
import com.yumocmspor.block.OutputBlock;
import com.yumocmspor.plugin.providers.client.ActiveComponentProvider;
import com.yumocmspor.plugin.providers.server.EvalProgressComponentProvider;
import com.yumocmspor.plugin.providers.server.RoomCodeComponentProvider;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import static com.yumocmspor.Cyumocompactmachinespor.MODID;

@WailaPlugin
public class CMPJadePlugin implements IWailaPlugin {
    public static final ResourceLocation ACTIVE = ResourceLocation.fromNamespaceAndPath(MODID, "active");
    public static final ResourceLocation ROOM_CODE = ResourceLocation.fromNamespaceAndPath(MODID, "room_code");
    public static final ResourceLocation PROGRESS = ResourceLocation.fromNamespaceAndPath(MODID, "progress");


    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(RoomCodeComponentProvider.INSTANCE, InputBlock.class);
        registration.registerBlockDataProvider(RoomCodeComponentProvider.INSTANCE, OutputBlock.class);
        registration.registerBlockDataProvider(RoomCodeComponentProvider.INSTANCE, EvaluatorBlock.class);
        registration.registerBlockDataProvider(RoomCodeComponentProvider.INSTANCE, FactoryBlock.class);

        registration.registerBlockDataProvider(EvalProgressComponentProvider.INSTANCE, EvaluatorBlock.class);
        //TODO register data providers
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(ActiveComponentProvider.INSTANCE, InputBlock.class);
        registration.registerBlockComponent(ActiveComponentProvider.INSTANCE, OutputBlock.class);

        registration.registerBlockComponent(RoomCodeComponentProvider.INSTANCE, InputBlock.class);
        registration.registerBlockComponent(RoomCodeComponentProvider.INSTANCE, OutputBlock.class);
        registration.registerBlockComponent(RoomCodeComponentProvider.INSTANCE, EvaluatorBlock.class);
        registration.registerBlockComponent(RoomCodeComponentProvider.INSTANCE, FactoryBlock.class);

        registration.registerBlockComponent(EvalProgressComponentProvider.INSTANCE, EvaluatorBlock.class);

//        registration.registerBlockComponent(RoomCodeComponentProvider.INSTANCE, InputBlock.class);
        //TODO register component providers, icon providers, callbacks, and config options here
    }
}
