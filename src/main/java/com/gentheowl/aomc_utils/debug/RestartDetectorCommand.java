package com.gentheowl.aomc_utils.debug;

import parliament.api.Parliament;
import parliament.api.command.Command;
import parliament.api.command.CommandContext;
import parliament.api.permission.PermissionLevel;
import parliament.api.text.Color;
import parliament.api.text.Message;

import java.time.Clock;
import java.time.LocalTime;
import java.util.Map;

/**
 * Netherite restart detector: drives the redstone power of a daylight detector
 * sitting on a netherite block from a real-time UTC window, with an op-only
 * override.
 */
public final class RestartDetectorCommand {

    private static volatile boolean enabled;
    private static volatile int startMinuteOfDay;
    private static volatile int windowMinutes;
    private static volatile Boolean override;

    private RestartDetectorCommand() {}

    public static void register(Parliament parliament) {
        // The nightly restart window: 23:45-00:05 UTC.
        enable(23, 45, 20);

        parliament.blocks().onBlockTick("minecraft:daylight_detector", (world, pos) -> {
            if (!enabled) return false;
            boolean onNetherite = world.blockId(pos.offset(0, -1, 0)).equals("minecraft:netherite_block");
            if (!onNetherite) return false;
            world.setBlockProperties(pos, Map.of("power", Integer.toString(currentPower())));
            return true;
        });

        parliament.commands().register(Command.literal("restartdetector")
                .requires(PermissionLevel.GAMEMASTER)
                .executes(RestartDetectorCommand::status)
                .child(Command.literal("on").executes(context -> set(context, true)))
                .child(Command.literal("off").executes(context -> set(context, false)))
                .child(Command.literal("auto").executes(context -> set(context, null))));
    }

    private static void enable(int startHourUtc, int startMinuteUtc, int minutes) {
        if (startHourUtc < 0 || startHourUtc > 23) {
            throw new IllegalArgumentException("start hour must be 0-23");
        }
        if (startMinuteUtc < 0 || startMinuteUtc > 59) {
            throw new IllegalArgumentException("start minute must be 0-59");
        }
        if (minutes < 1 || minutes > 1440) {
            throw new IllegalArgumentException("window minutes must be 1-1440");
        }
        startMinuteOfDay = startHourUtc * 60 + startMinuteUtc;
        windowMinutes = minutes;
        enabled = true;
    }

    private static void setOverride(Boolean value) {
        override = value;
    }

    private static Boolean override() {
        return override;
    }

    private static int currentPower() {
        if (override != null) return override ? 15 : 0;
        LocalTime now = LocalTime.now(Clock.systemUTC());
        int minutesSinceStart = Math.floorMod(now.getHour() * 60 + now.getMinute() - startMinuteOfDay, 1440);
        if (minutesSinceStart >= windowMinutes) return 0;
        return Math.min(minutesSinceStart + 1, 15);
    }

    private static void set(CommandContext context, Boolean value) {
        setOverride(value);
        String mode = value == null ? "AUTO (real-time window)" : value ? "FORCED ON" : "FORCED OFF";
        context.sendSuccess(Message.literal("Restart detector mode: " + mode
                + (value != null ? " — takes effect within a second; resets to auto on restart." : ""))
                .color(Color.GREEN));
    }

    private static void status(CommandContext context) {
        Boolean current = override();
        String mode = current == null ? "AUTO (real-time window)" : current ? "FORCED ON" : "FORCED OFF";
        LocalTime now = LocalTime.now(Clock.systemUTC());
        context.sendSuccess(Message.literal(
                "Restart detector mode: " + mode + " | UTC now: "
                        + String.format("%02d:%02d", now.getHour(), now.getMinute())
                        + " | window: 23:45-00:05 UTC")
                .color(Color.YELLOW));
    }
}
