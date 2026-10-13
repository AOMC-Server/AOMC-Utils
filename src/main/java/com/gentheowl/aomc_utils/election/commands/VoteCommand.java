package com.gentheowl.aomc_utils.election.commands;

import com.gentheowl.aomc_utils.election.VoteManager;
import parliament.api.command.Argument;
import parliament.api.command.Command;
import parliament.api.command.CommandContext;
import parliament.api.command.Commands;
import parliament.api.entity.Player;
import parliament.api.permission.PermissionLevel;
import parliament.api.text.Message;

import java.util.Optional;

public class VoteCommand {
    public static void register(Commands commands) {
        commands.register(Command.literal("vote")
                .executes(VoteCommand::startVote)
                .child(Command.literal("answer")
                        .child(Command.argument(Argument.ofInteger("choice", 1, Integer.MAX_VALUE))
                                .executes(ctx -> answerVote(ctx, ctx.getInteger("choice")))
                        )
                )
                .child(Command.literal("skip")
                        .executes(VoteCommand::skipVote)
                )
                .child(Command.literal("toggle")
                        .requires(PermissionLevel.ADMIN) // admin only (level 2)
                        .executes(VoteCommand::toggleVote)
                )
        );
    }

    private static Optional<Player> requirePlayer(CommandContext context) {
        Optional<Player> player = context.player();
        if (player.isEmpty()) {
            context.sendFailure(Message.literal("Only players can use this command."));
        }
        return player;
    }

    private static void answerVote(CommandContext context, int choice) {
        requirePlayer(context).ifPresent(player -> VoteManager.handleAnswer(player, choice));
    }

    private static void skipVote(CommandContext context) {
        requirePlayer(context).ifPresent(VoteManager::handleSkip);
    }

    private static void startVote(CommandContext context) {
        requirePlayer(context).ifPresent(VoteManager::startSession);
    }

    private static void toggleVote(CommandContext context) {
        VoteManager.setVotingEnabled(!VoteManager.isVotingEnabled());
        String status = VoteManager.isVotingEnabled() ? "enabled" : "disabled";
        context.sendSuccess(Message.literal("Voting has been " + status + " by an administrator."));
    }
}
