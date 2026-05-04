package com.yumocmspor;

import com.mojang.logging.LogUtils;
import com.yumocmspor.block.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(yumocompactmachinespor.MODID)
public class yumocompactmachinespor {
    public static final String MODID = "yumocompactmachinespor";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredBlock<Block> INPUT_BLOCK = BLOCKS.register("input_block", () -> new InputBlock());
    public static final DeferredBlock<Block> OUTPUT_BLOCK = BLOCKS.register("output_block", () -> new OutputBlock());
    public static final DeferredBlock<Block> FACTORY_BLOCK = BLOCKS.register("factory_block", () -> new FactoryBlock());
    public static final DeferredBlock<Block> EVALUATOR_BLOCK = BLOCKS.register("evaluator_block", () -> new EvaluatorBlock());

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredItem<BlockItem> INPUT_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("input_block", INPUT_BLOCK);
    public static final DeferredItem<BlockItem> OUTPUT_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("output_block", OUTPUT_BLOCK);
    public static final DeferredItem<BlockItem> FACTORY_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("factory_block", FACTORY_BLOCK);
    public static final DeferredItem<BlockItem> EVALUATOR_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("evaluator_block", EVALUATOR_BLOCK);
    public static final DeferredItem<Item> LAUNCHER_STICK = ITEMS.registerSimpleItem("launcher_stick", new Item.Properties().stacksTo(1));

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InputBlockEntity>> INPUT_BLOCK_ENTITY = BLOCK_ENTITIES.register("input_block", () -> BlockEntityType.Builder.of(InputBlockEntity::new, INPUT_BLOCK.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OutputBlockEntity>> OUTPUT_BLOCK_ENTITY = BLOCK_ENTITIES.register("output_block", () -> BlockEntityType.Builder.of(OutputBlockEntity::new, OUTPUT_BLOCK.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FactoryBlockEntity>> FACTORY_BLOCK_ENTITY = BLOCK_ENTITIES.register("factory_block", () -> BlockEntityType.Builder.of(FactoryBlockEntity::new, FACTORY_BLOCK.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EvaluatorBlockEntity>> EVALUATOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("evaluator_block", () -> BlockEntityType.Builder.of(EvaluatorBlockEntity::new, EVALUATOR_BLOCK.get()).build(null));

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.yumocompactmachinespor"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> INPUT_BLOCK_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(INPUT_BLOCK_ITEM.get());
                output.accept(OUTPUT_BLOCK_ITEM.get());
                output.accept(FACTORY_BLOCK_ITEM.get());
                output.accept(EVALUATOR_BLOCK_ITEM.get());
                output.accept(LAUNCHER_STICK.get());
            }).build());

    public yumocompactmachinespor(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::registerCapabilities);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, INPUT_BLOCK_ENTITY.get(), (be, side) -> be.getItemHandler());
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, INPUT_BLOCK_ENTITY.get(), (be, side) -> be.getFluidHandler());
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, INPUT_BLOCK_ENTITY.get(), (be, side) -> be.getEnergyHandler());

        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, OUTPUT_BLOCK_ENTITY.get(), (be, side) -> be.getItemHandler());
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, OUTPUT_BLOCK_ENTITY.get(), (be, side) -> be.getFluidHandler());
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, OUTPUT_BLOCK_ENTITY.get(), (be, side) -> be.getEnergyHandler());

        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, FACTORY_BLOCK_ENTITY.get(), (be, side) -> be.getItemHandler());
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, FACTORY_BLOCK_ENTITY.get(), (be, side) -> be.getFluidHandler());
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, FACTORY_BLOCK_ENTITY.get(), (be, side) -> be.getEnergyHandler());
    }
}
