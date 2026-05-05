package com.compactmachinespor;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder().gameRestart();

    public static final ModConfigSpec.BooleanValue ENABLE_SCAN = BUILDER
            .comment("Scan")
            .translation("config.compactmachinespor.enable_scan")
            .define("Scan", false);

    public static final ModConfigSpec.ConfigValue<String> SCAN_TAG = BUILDER
            .comment("Block Tag to Scan")
            .translation("config.compactmachinespor.scan_tag")
            .define("ScanTag", "c:ores");

    public static final ModConfigSpec.IntValue EVALUATE_SECONDS = BUILDER
            .comment("Evaluate seconds")
            .translation("config.compactmachinespor.evaluate_seconds")
            .defineInRange("EvaluateSeconds", 300, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue UNPACK_PERMISSION_LEVEL = BUILDER
            .comment("Unpack permission level")
            .translation("config.compactmachinespor.unpack_permission_level")
            .defineInRange("PermissionLevel", 2, 0, 4);

    static final ModConfigSpec SPEC = BUILDER.build();
}
