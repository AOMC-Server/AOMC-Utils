package com.gentheowl.aomc_utils.advancement.core;

import com.gentheowl.aomc_utils.advancement.core.persistent.PlayerCounters;
import com.gentheowl.aomc_utils.advancement.core.persistent.StrongholdVisits;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public class CheckCommand {
    private static final SuggestionProvider<CommandSourceStack> COUNTER_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(new String[]{
                    "dragon_kills",
                    "warden_kills",
                    "wither_kills",
                    "elder_guardian_kills",
                    "zombie_kills",
                    "piglin_kills",
                    "enderman_kills",
                    "mob_kills",
                    "generated_loot_chests",
                    "trial_vault_loots",
                    "walk_cm",
                    "elytra_cm",
                    "stronghold_visits"
            }, builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("check")
                .executes(ctx -> showAllCounters(ctx.getSource()))
                .then(Commands.argument("counter", StringArgumentType.string())
                        .suggests(COUNTER_SUGGESTIONS)
                        .executes(ctx -> showCounter(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "counter")
                        ))
                )
        );
    }

    private static int showAllCounters(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        Objects.requireNonNull(player);

        player.sendSystemMessage(Component.literal("=== Your Progress ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        PlayerCounters.CounterData counters = PlayerCounters.get(player);
        StrongholdVisits strongholds = PlayerCounters.strongholds(player);

        // Combat
        player.sendSystemMessage(Component.literal("Combat:").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        displayCounter(player, "dragon_kills", counters.get(PlayerCounters.CounterKey.DRAGON_KILLS));
        displayCounter(player, "warden_kills", counters.get(PlayerCounters.CounterKey.WARDEN_KILLS));
        displayCounter(player, "wither_kills", counters.get(PlayerCounters.CounterKey.WITHER_KILLS));
        displayCounter(player, "elder_guardian_kills", counters.get(PlayerCounters.CounterKey.ELDER_GUARDIAN_KILLS));
        displayCounter(player, "zombie_kills", counters.get(PlayerCounters.CounterKey.ZOMBIE_KILLS));
        displayCounter(player, "piglin_kills", counters.get(PlayerCounters.CounterKey.PIGLIN_KILLS));
        displayCounter(player, "enderman_kills", counters.get(PlayerCounters.CounterKey.ENDERMAN_KILLS));
        displayCounter(player, "mob_kills", counters.get(PlayerCounters.CounterKey.MOB_KILLS));

        // Exploration
        player.sendSystemMessage(Component.literal("Exploration:").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        displayCounter(player, "generated_loot_chests", counters.get(PlayerCounters.CounterKey.GENERATED_LOOT_CHESTS));
        displayCounter(player, "trial_vault_loots", counters.get(PlayerCounters.CounterKey.TRIAL_VAULT_LOOTS));
        displayCounter(player, "walk_cm", counters.get(PlayerCounters.CounterKey.WALK_CM));
        displayCounter(player, "elytra_cm", counters.get(PlayerCounters.CounterKey.ELYTRA_CM));
        displayCounter(player, "stronghold_visits", strongholds.count());

        return 1;
    }

    private static int showCounter(CommandSourceStack source, String counterName) {
        ServerPlayer player = source.getPlayer();
        Objects.requireNonNull(player);

        PlayerCounters.CounterData counters = PlayerCounters.get(player);
        StrongholdVisits strongholds = PlayerCounters.strongholds(player);

        try {
            int value;
            if (counterName.equals("stronghold_visits")) {
                value = strongholds.count();
            } else {
                PlayerCounters.CounterKey key = PlayerCounters.CounterKey.valueOf(counterName.toUpperCase());
                value = counters.get(key);
            }

            displayCounter(player, counterName, value);
            return 1;

        } catch (IllegalArgumentException e) {
            player.sendSystemMessage(Component.literal("Unknown counter: " + counterName)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static void displayCounter(ServerPlayer player, String name, int value) {
        String displayName = formatCounterName(name);
        String formattedValue = formatValue(name, value);

        Component message = Component.literal("  " + displayName + ": ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(formattedValue)
                        .withStyle(ChatFormatting.WHITE));

        player.sendSystemMessage(message);
    }

    private static String formatCounterName(String name) {
        return java.util.Arrays.stream(name.split("_"))
                .map(word -> word.isEmpty() ? "" :
                        Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(java.util.stream.Collectors.joining(" "));
    }

    private static String formatValue(String counterName, int value) {
        if (counterName.equals("walk_cm")) {
            return String.format("%,d cm (%,d m)", value, value / 100);
        } else if (counterName.equals("elytra_cm")) {
            return String.format("%,d cm (%,d m)", value, value / 100);
        } else {
            return String.format("%,d", value);
        }
    }
}