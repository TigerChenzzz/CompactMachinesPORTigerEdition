package com.yumocmspor.block;

import com.yumocmspor.Config;
import com.yumocmspor.core.Core;
import dev.compactmods.machines.api.component.CMDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

import static com.yumocmspor.Cyumocompactmachinespor.FACTORY_BLOCK;

public class FactoryBlockItem extends BlockItem {
    public FactoryBlockItem(Properties properties) {
        super(FACTORY_BLOCK.get(), properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            ItemStack itemInHand = context.getItemInHand();
            CustomData s = itemInHand.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            if (s.contains("room_code") && !s.copyTag().getString("room_code").isEmpty()) {
                String roomCode = s.copyTag().getString("room_code");
                if (!context.getLevel().isClientSide) {
                    if (context.getPlayer().hasPermissions(Config.UNPACK_PERMISSION_LEVEL.get())) {
                        itemInHand.shrink(1);
                        if (context.getPlayer() != null) {
                            context.getPlayer().getInventory().add(Core.unpackToItem(roomCode));
                            context.getPlayer().inventoryMenu.broadcastChanges();
                        }
                    }
                }
                return InteractionResult.CONSUME;
            }
        }
        return super.useOn(context);
    }
}
