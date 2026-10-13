package com.gentheowl.aomc_utils.debug;

import parliament.api.Parliament;
import parliament.api.command.Command;
import parliament.api.command.CommandContext;
import parliament.api.permission.PermissionLevel;
import parliament.api.text.Color;
import parliament.api.text.Message;

import java.time.Clock;
import java.time.LocalTime;

/**
 * Op-only override for the netherite restart detector
 */
public final class RestartDetectorCommand {

    private RestartDetectorCommand() {}

    public static void register(Parliament parliament) {
        // The nightly restart window: 23:45-00:05 UTC.
        parliament.restartClock().enableNetheriteDetector(23, 45, 20);

        parliament.commands().register(Command.literal("restartdetector")
                .requires(PermissionLevel.GAMEMASTER)
                .executes(context -> status(parliament, context))
                .child(Command.literal("on").executes(context -> set(parliament, context, true)))
                .child(Command.literal("off").executes(context -> set(parliament, context, false)))
                .child(Command.literal("auto").executes(context -> set(parliament, context, null))));
    }

    private static void set(Parliament parliament, CommandContext context, Boolean value) {
        parliament.restartClock().setOverride(value);
        String mode = value == null ? "AUTO (real-time window)" : value ? "FORCED ON" : "FORCED OFF";
        context.sendSuccess(Message.literal("Restart detector mode: " + mode
                + (value != null ? " — takes effect within a second; resets to auto on restart." : ""))
                .color(Color.GREEN));
    }

    private static void status(Parliament parliament, CommandContext context) {
        Boolean current = parliament.restartClock().override();
        String mode = current == null ? "AUTO (real-time window)" : current ? "FORCED ON" : "FORCED OFF";
        LocalTime now = LocalTime.now(Clock.systemUTC());
        context.sendSuccess(Message.literal(
                "Restart detector mode: " + mode + " | UTC now: "
                        + String.format("%02d:%02d", now.getHour(), now.getMinute())
                        + " | window: 23:45-00:05 UTC")
                .color(Color.YELLOW));
    }
}
