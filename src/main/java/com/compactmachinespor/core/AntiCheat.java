package com.compactmachinespor.core;

import com.compactmachinespor.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class AntiCheat {
    public interface IAntiCheat {
        default void preScan(ServerLevel level, String roomCode, AABB roomAABB) {}
        default void scanBlock(ServerLevel level, BlockPos pos, BlockState state, String roomCode) {}
        default void postScan(ServerLevel level, String roomCode, AABB roomAABB) {}
    }

    private static final List<IAntiCheat> ANTI_CHEATS = new ArrayList<>();
    private static TagKey<Block> BanBlocksTag = null;

    static {
        // Default anti-cheat implementation
        registerAntiCheat(new IAntiCheat() {
            @Override
            public void scanBlock(ServerLevel level, BlockPos pos, BlockState state, String roomCode) {
                if (Config.ENABLE_SCAN.get()) {
                    if (BanBlocksTag == null) {
                        ResourceLocation tagId = ResourceLocation.tryParse(Config.SCAN_TAG.get());
                        if (tagId != null) {
                            BanBlocksTag = BlockTags.create(tagId);
                        }
                    }
                    if (BanBlocksTag != null && state.is(BanBlocksTag)) {
                        level.destroyBlock(pos, true);
                    }
                }
            }
        });
    }

    public static void registerAntiCheat(IAntiCheat antiCheat) {
        ANTI_CHEATS.add(antiCheat);
    }

    public static void runPreScan(ServerLevel level, String roomCode, AABB roomAABB) {
        for (IAntiCheat antiCheat : ANTI_CHEATS) {
            antiCheat.preScan(level, roomCode, roomAABB);
        }
    }

    public static void runScanBlock(ServerLevel level, BlockPos pos, BlockState state, String roomCode) {
        for (IAntiCheat antiCheat : ANTI_CHEATS) {
            antiCheat.scanBlock(level, pos, state, roomCode);
        }
    }

    public static void runPostScan(ServerLevel level, String roomCode, AABB roomAABB) {
        for (IAntiCheat antiCheat : ANTI_CHEATS) {
            antiCheat.postScan(level, roomCode, roomAABB);
        }
    }

    /**
     * Centralized unpack check for easy Mixin by add-on mods.
     * @return true if unpacking is allowed.
     */
    public static boolean checkUnpack(Player player, String roomCode, Level level, ItemStack stack, InteractionHand hand) {
        return player.hasPermissions(Config.UNPACK_PERMISSION_LEVEL.get());
    }
}
