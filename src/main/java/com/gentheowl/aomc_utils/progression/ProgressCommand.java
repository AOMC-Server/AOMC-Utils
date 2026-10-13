package com.gentheowl.aomc_utils.progression;

import parliament.api.Parliament;
import parliament.api.command.Argument;
import parliament.api.command.Command;
import parliament.api.command.CommandContext;
import parliament.api.data.Counter;
import parliament.api.entity.Player;
import parliament.api.text.Color;
import parliament.api.text.Message;

import java.util.List;

/** The {@code /progress} command, ported to Parliament. */
final class ProgressCommand {

    private static final List<String> COMBAT_COUNTERS = List.of(
            "dragon_kills", "warden_kills", "wither_kills", "elder_guardian_kills",
            "zombie_kills", "piglin_kills", "enderman_kills", "mob_kills");
    private static final List<String> EXPLORATION_COUNTERS = List.of(
            "generated_loot_chests", "trial_vault_loots", "walk_cm", "elytra_cm");
    private static final List<String> ALL_NAMES = List.of(
            "dragon_kills", "warden_kills", "wither_kills", "elder_guardian_kills",
            "zombie_kills", "piglin_kills", "enderman_kills", "mob_kills",
            "generated_loot_chests", "trial_vault_loots", "walk_cm", "elytra_cm",
            "stronghold_visits");

    private ProgressCommand() {
    }

    static void register(Parliament parliament, AomcProgression progression) {
        parliament.commands().register(
                Command.literal("progress")
                        .executes(context -> showAll(context, progression))
                        .child(Command.argument(
                                        Argument.ofWord("counter").suggests(context -> ALL_NAMES))
                                .executes(context -> showOne(context, progression))));
    }

    private static void showAll(CommandContext context, AomcProgression progression) {
        Player player = context.player().orElse(null);
        if (player == null) {
            context.sendFailure(Message.literal("Players only."));
            return;
        }
        player.sendMessage(Message.literal("=== Your Progress ===").color(Color.GOLD).bold(true));

        player.sendMessage(Message.literal("Combat:").color(Color.RED).bold(true));
        for (String name : COMBAT_COUNTERS) {
            sendCounter(player, name, progression.counterByName(name).get(player));
        }

        player.sendMessage(Message.literal("Exploration:").color(Color.GREEN).bold(true));
        for (String name : EXPLORATION_COUNTERS) {
            sendCounter(player, name, progression.counterByName(name).get(player));
        }
        sendCounter(player, "stronghold_visits", progression.strongholdVisitCount(player));
    }

    private static void showOne(CommandContext context, AomcProgression progression) {
        Player player = context.player().orElse(null);
        if (player == null) {
            context.sendFailure(Message.literal("Players only."));
            return;
        }
        String name = context.getText("counter");
        int value;
        if (name.equals("stronghold_visits")) {
            value = progression.strongholdVisitCount(player);
        } else {
            Counter counter = progression.counterByName(name);
            if (counter == null) {
                context.sendFailure(Message.literal("Unknown counter: " + name));
                return;
            }
            value = counter.get(player);
        }
        sendCounter(player, name, value);
    }

    private static void sendCounter(Player player, String name, int value) {
        player.sendMessage(Message.literal("  " + name + ": ").color(Color.GRAY)
                .append(Message.literal(String.valueOf(value)).color(Color.WHITE)));
    }
}
