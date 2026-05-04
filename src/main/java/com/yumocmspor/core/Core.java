package com.yumocmspor.core;

import com.yumocmspor.Config;
import com.yumocmspor.block.BaseIOBlock;
import com.yumocmspor.block.BaseIOBlockEntity;
import com.yumocmspor.block.FactoryBlockEntity;
import com.yumocmspor.Cyumocompactmachinespor;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.machine.MachineColor;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.spatial.IRoomBoundaries;
import dev.compactmods.machines.server.CompactMachinesServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static dev.compactmods.machines.machine.Machines.Items.BOUND_MACHINE;
import static net.minecraft.world.phys.shapes.Shapes.EPSILON;


public class Core {
    private static final Map<String, Machine> MACHINES = new HashMap<>();
    private static final Map<String, UUID> ROOM2UUID = new HashMap<>();

    public static TagKey<Block> BanBlocksTag = null;

    public static Map<String, Machine> getMachines(){
        return MACHINES;
    }

    public static void createMachine(ServerLevel level, String roomCode, BlockPos targetPos) {
        if (MACHINES.containsKey(roomCode)) return;
        MACHINES.put(roomCode, new Machine(getTicks(level), roomCode, targetPos));
        ROOM2UUID.put(roomCode, UUID.randomUUID());
        loadRoom(level, roomCode);
        scanRoom(level, roomCode,true);
    }

    public static Machine getMachine(String roomCode) {
        return MACHINES.get(roomCode);
    }

    public static void setMachineData(String roomCode, Machine.DataSetType type, Holder<?> id, int data, long tickTime) {
        getMachine(roomCode).addData(type, id, data, tickTime);
    }

    public static long getTicks(ServerLevel level) {
        return level.getServer().getTickCount();
    }

    public static void loadRoom(ServerLevel level, String roomCode) {
        forceRoom(level, roomCode, true);
    }

    public static void unLoadRoom(ServerLevel level, String roomCode) {
        forceRoom(level, roomCode, false);
    }

    public static void forceRoom(ServerLevel level, String roomCode, boolean add) {
        Objects.requireNonNull(getRoomBoundaries(level, roomCode)).innerChunkPositions().forEach(
                chunkPos -> CompactMachinesServer.CHUNK_TICKET_CONTROLLER.forceChunk(
                        level,
                        ROOM2UUID.get(roomCode),
                        chunkPos.x, chunkPos.z, add, true
                )
        );

    }

    public static void scanRoom(ServerLevel level, String roomCode, boolean active) {
        AABB roomAABB = Objects.requireNonNull(getRoomBoundaries(level, roomCode)).outerBounds();
        int startX = (int) Math.floor(roomAABB.minX);
        int startY = (int) Math.floor(roomAABB.minY);
        int startZ = (int) Math.floor(roomAABB.minZ);
        int endX = (int) Math.floor(roomAABB.maxX - EPSILON);
        int endY = (int) Math.floor(roomAABB.maxY - EPSILON);
        int endZ = (int) Math.floor(roomAABB.maxZ - EPSILON);

        if (Config.ENABLE_SCAN.get()){
            ResourceLocation tagId=ResourceLocation.tryParse(Config.SCAN_TAG.get());
            if (tagId!=null){
                BanBlocksTag=BlockTags.create(tagId);
            }
        }

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    processBlock(level, x, y, z, roomCode,active);
                }
            }
        }
    }

    private static void processBlock(ServerLevel level, int x, int y, int z, String roomCode, boolean active) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState blockState = level.getBlockState(pos);
        if (active && Config.ENABLE_SCAN.get()) antiCheatBlock(level, pos, blockState);
        if (blockState.is(Cyumocompactmachinespor.INPUT_BLOCK) || blockState.is(Cyumocompactmachinespor.OUTPUT_BLOCK)) {
            ((BaseIOBlockEntity) Objects.requireNonNull(level.getBlockEntity(pos))).setRoomCode(roomCode);
            getMachine(roomCode).IOBlocks.add(pos);
            level.setBlock(pos, blockState.setValue(BaseIOBlock.ACTIVE, active),Block.UPDATE_NEIGHBORS);
        }
    }


    private static void antiCheatBlock(ServerLevel level, BlockPos pos, BlockState blockState) {
        if (BanBlocksTag!=null){
            if (blockState.is(BanBlocksTag)){
                level.destroyBlock(pos, true);
            }
        }
    }

    @Nullable
    public static IRoomBoundaries getRoomBoundaries(ServerLevel level, String roomCode) {
        Optional<RoomInstance> room = CompactMachines.room(level.getServer(), roomCode);
        return room.map(RoomInstance::boundaries).orElse(null);
    }

    public static void finish(String roomCode, BlockPos overworldPos) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;
        ServerLevel compactWorld = server.getLevel(CompactDimension.LEVEL_KEY);
        if (compactWorld == null) {
            MACHINES.remove(roomCode);
            return;
        }
        Machine machine = getMachine(roomCode);
        Map<Holder<?>, Double> inputData = calculate(machine.InputData);
        if (machine.EnergyData != null){
            inputData.put(Holder.direct(null),RateEvaluator.evaluateStableRate(machine.EnergyData.getFirst().data()));
        }
        Map<Holder<?>, Double> outputData = calculate(machine.OutputData);
        if (machine.EnergyData != null) outputData.put(Holder.direct(null),RateEvaluator.evaluateStableRate(machine.EnergyData.getLast().data()));
        machine.IOBlocks.forEach(
                pos ->
                        compactWorld.setBlock(
                                pos,
                                compactWorld.getBlockState(pos)
                                        .setValue(BaseIOBlock.ACTIVE, false),
                                Block.UPDATE_NEIGHBORS
                        )
        );
        Objects.requireNonNull(getRoomBoundaries(compactWorld, roomCode)).innerChunkPositions().forEach(
                chunkPos -> compactWorld.getChunk(chunkPos.x, chunkPos.z).setUnsaved(true));
        unLoadRoom(compactWorld,roomCode);
        replaceBlock(overworld,overworldPos, Cyumocompactmachinespor.FACTORY_BLOCK);
        FactoryBlockEntity be = (FactoryBlockEntity) Objects.requireNonNull(overworld.getBlockEntity(overworldPos));
        be.setRoomCode(roomCode);
        be.initTanks(inputData, outputData);
        MACHINES.remove(roomCode);
        ROOM2UUID.remove(roomCode);
    }

    public static Map<Holder<?>, Double> calculate(Map<Holder<?>, Machine.Data> dataMap) {
        return dataMap
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                e -> RateEvaluator.evaluateStableRate(e.getValue().data())
                        )
                );
    }

    public static void replaceBlock(ServerLevel level, BlockPos pos, Holder<Block> target) {
        level.removeBlockEntity(pos);
        level.setBlockAndUpdate(pos, target.value().defaultBlockState());
    }

    public static ItemStack unpackToItem(String roomCode) {
        ItemStack stack = BOUND_MACHINE.toStack();
        stack.set(CMDataComponents.BOUND_ROOM_CODE, roomCode);
        stack.set(CMDataComponents.MACHINE_COLOR, MachineColor.fromARGB(0xFFC95B13));
        return stack;
    }

}
