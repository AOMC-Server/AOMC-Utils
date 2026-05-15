package com.gentheowl.aomc_utils.election.commands;

import com.gentheowl.aomc_utils.election.VoteManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.Objects;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class VoteCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vote")
                .executes(ctx -> startVote(ctx.getSource()))
                .then(Commands.literal("answer")
                        .then(Commands.argument("choice", IntegerArgumentType.integer(1))
                                .executes(ctx -> answerVote(ctx.getSource(), IntegerArgumentType.getInteger(ctx,"choice")))
                        )
                )
                .then(Commands.literal("skip")
                        .executes(ctx -> skipVote(ctx.getSource())
                        )
                )
                .then(Commands.literal("toggle")
                        .requires(Commands.hasPermission(Commands.LEVEL_ADMINS)) // admin only (level 2)
                        .executes(ctx -> toggleVote(ctx.getSource()))
                )
        );
    }

    private static int answerVote(CommandSourceStack source, int choice) {
        ServerPlayer player = source.getPlayer();
        VoteManager.handleAnswer(Objects.requireNonNull(player), choice);
        return 1;
    }

    private static int skipVote(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        VoteManager.handleSkip(Objects.requireNonNull(player));
        return 1;
    }

    private static int startVote(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        VoteManager.startSession(Objects.requireNonNull(player));
        return 1;
    }

    private static int toggleVote(CommandSourceStack source) {
        VoteManager.setVotingEnabled(!VoteManager.isVotingEnabled());
        String status = VoteManager.isVotingEnabled() ? "enabled" : "disabled";
        source.sendSuccess(() -> Component.literal("Voting has been " + status + " by an administrator."), true);
        return 1;
    }
}
