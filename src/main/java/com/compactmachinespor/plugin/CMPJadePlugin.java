package com.compactmachinespor.plugin;

import com.compactmachinespor.block.EvaluatorBlock;
import com.compactmachinespor.block.FactoryBlock;
import com.compactmachinespor.block.InputBlock;
import com.compactmachinespor.block.OutputBlock;
import com.compactmachinespor.plugin.providers.client.ActiveComponentProvider;
import com.compactmachinespor.plugin.providers.server.EvalComponentProvider;
import com.compactmachinespor.plugin.providers.server.FactoryComponentProvider;
import com.compactmachinespor.plugin.providers.server.RoomCodeComponentProvider;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import static com.compactmachinespor.Cyumocompactmachinespor.MODID;

@WailaPlugin
public class CMPJadePlugin implements IWailaPlugin {
    public static final ResourceLocation ACTIVE = ResourceLocation.fromNamespaceAndPath(MODID, "active");
    public static final ResourceLocation ROOM_CODE = ResourceLocation.fromNamespaceAndPath(MODID, "room_code");
    public static final ResourceLocation PROGRESS = ResourceLocation.fromNamespaceAndPath(MODID, "progress");
    public static final ResourceLocation PRODUCE = ResourceLocation.fromNamespaceAndPath(MODID, "produce");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(RoomCodeComponentProvider.INSTANCE, InputBlock.class);
        registration.registerBlockDataProvider(RoomCodeComponentProvider.INSTANCE, OutputBlock.class);
        registration.registerBlockDataProvider(RoomCodeComponentProvider.INSTANCE, EvaluatorBlock.class);
        registration.registerBlockDataProvider(RoomCodeComponentProvider.INSTANCE, FactoryBlock.class);

        registration.registerBlockDataProvider(EvalComponentProvider.INSTANCE, EvaluatorBlock.class);

        registration.registerBlockDataProvider(FactoryComponentProvider.INSTANCE, FactoryBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(ActiveComponentProvider.INSTANCE, InputBlock.class);
        registration.registerBlockComponent(ActiveComponentProvider.INSTANCE, OutputBlock.class);
        registration.addConfig(ActiveComponentProvider.INSTANCE.getUid(), false);

        registration.registerBlockComponent(RoomCodeComponentProvider.INSTANCE, InputBlock.class);
        registration.registerBlockComponent(RoomCodeComponentProvider.INSTANCE, OutputBlock.class);
        registration.registerBlockComponent(RoomCodeComponentProvider.INSTANCE, EvaluatorBlock.class);
        registration.registerBlockComponent(RoomCodeComponentProvider.INSTANCE, FactoryBlock.class);

        registration.registerBlockComponent(EvalComponentProvider.INSTANCE, EvaluatorBlock.class);

        registration.registerBlockComponent(FactoryComponentProvider.INSTANCE, FactoryBlock.class);

    }
}
