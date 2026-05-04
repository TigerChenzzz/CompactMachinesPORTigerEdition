package com.yumocmspor;

import com.yumocmspor.core.Core;
import com.yumocmspor.core.Machine;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ServerTick {
    private static int tickCount = 0;
    private static final Map<String, Long> remainingTasks = new HashMap<>();
    private static final int TOTAL_LIFE_TICKS = (Machine.EVALUATE_SECONDS + 1) * 20;
    private static final int MAX_TICK_COUNT = Mth.clamp(Machine.EVALUATE_SECONDS - 1, 1, 60) * 20;

    public static void onServerTickEvent(ServerTickEvent.Post event) {
        if (tickCount++ >= MAX_TICK_COUNT) {
            tickCount = 0;
            refreshTaskPool(event.getServer().getTickCount());
        }
        processCountdowns();
    }

    private static void refreshTaskPool(int currentTick) {
        remainingTasks.clear();

        Core.getMachines().forEach((roomCode, machine) -> {
            long elapsedTicks = currentTick - machine.StartTick.get();
            long remainingTicks = TOTAL_LIFE_TICKS - elapsedTicks;

            if (remainingTicks > 0 && remainingTicks < MAX_TICK_COUNT) {
                remainingTasks.put(roomCode, remainingTicks);
            }
        });
    }

    private static void processCountdowns() {
        Iterator<Map.Entry<String, Long>> iterator = remainingTasks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            String roomCode = entry.getKey();
            long remaining = entry.getValue();
            Machine machine = Core.getMachine(roomCode);
            if (machine == null) {
                iterator.remove();
                continue;
            }

            long newRemaining = remaining - 1;
            if (newRemaining <= 0) {
                Core.finish(roomCode, machine.TargetPos);
                iterator.remove();
            } else {
                entry.setValue(newRemaining);
            }
        }
    }
}