package com.compactmachinespor.block;

import com.compactmachinespor.Config;
import com.compactmachinespor.core.Core;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import static com.compactmachinespor.Cyumocompactmachinespor.FACTORY_BLOCK;

public class FactoryBlockItem extends BlockItem {
    public FactoryBlockItem(Properties properties) {
        super(FACTORY_BLOCK.get(), properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (player.onGround() && player.isShiftKeyDown()) {
            ItemStack itemInHand = player.getItemInHand(usedHand);
            CustomData s = itemInHand.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            if (s.contains("room_code") && !s.copyTag().getString("room_code").isEmpty()) {
                String roomCode = s.copyTag().getString("room_code");
                if (!level.isClientSide) {
//                    Map<Item, FactoryBlockEntity.Container> tmp1 = new LinkedHashMap<>();
//                    FactoryBlockEntity.loadItemMap(s.copyTag(), "input_items", tmp1);
//                    Map<Fluid, FactoryBlockEntity.Container> tmp2 = new LinkedHashMap<>();
//                    FactoryBlockEntity.loadFluidMap(s.copyTag(), "input_fluids", tmp2);
                    if (player.hasPermissions(Config.UNPACK_PERMISSION_LEVEL.get())) {
                        itemInHand.shrink(1);
                        player.getInventory().add(Core.unpackToItem(roomCode));
                        player.inventoryMenu.broadcastChanges();
                    }
                }
                return InteractionResultHolder.consume(itemInHand);
            }
        }
        return super.use(level, player, usedHand);
    }
}
