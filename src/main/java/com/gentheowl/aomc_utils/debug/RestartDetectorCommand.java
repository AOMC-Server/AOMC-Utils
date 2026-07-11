package com.gentheowl.aomc_utils.debug;

import com.gentheowl.aomc_utils.renaming.utils.TextUtil;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.Nullable;

import java.time.Clock;
import java.time.LocalTime;

/**
 * Op-only override
 */
public final class RestartDetectorCommand {
    @Nullable
    private static volatile Boolean override = null;

    private RestartDetectorCommand() {}

    /** null = follow the real-time window; true/false = forced state. */
    @Nullable
    public static Boolean override() {
        return override;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("restartdetector")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(ctx -> status(ctx.getSource()))
                .then(Commands.literal("on").executes(ctx -> set(ctx.getSource(), true)))
                .then(Commands.literal("off").executes(ctx -> set(ctx.getSource(), false)))
                .then(Commands.literal("auto").executes(ctx -> set(ctx.getSource(), null)))
        );
    }

    private static int set(CommandSourceStack src, @Nullable Boolean value) {
        override = value;
        String mode = value == null ? "AUTO (real-time window)" : value ? "FORCED ON" : "FORCED OFF";
        src.sendSuccess(() -> TextUtil.success("Restart detector mode: " + mode
                + (value != null ? " — takes effect within a second; resets to auto on restart." : "")), true);
        return 1;
    }

    private static int status(CommandSourceStack src) {
        Boolean current = override;
        String mode = current == null ? "AUTO (real-time window)" : current ? "FORCED ON" : "FORCED OFF";
        LocalTime now = LocalTime.now(Clock.systemUTC());
        src.sendSuccess(() -> TextUtil.info(
                "Restart detector mode: " + mode + " | UTC now: "
                        + String.format("%02d:%02d", now.getHour(), now.getMinute())
                        + " | window: 23:45-00:05 UTC"), false);
        return 1;
    }
}
