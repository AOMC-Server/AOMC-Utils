package com.gentheowl.aomc_utils.election;

import parliament.api.entity.Player;
import parliament.api.event.Events;
import parliament.api.text.Color;
import parliament.api.text.Message;

public class VoteEvents {

    public static void register(Events events) {
        events.onPlayerJoin(VoteEvents::handlePlayerJoin);
    }

    private static void handlePlayerJoin(Player player) {
        if (!VoteManager.isVotingEnabled()) return; // voting disabled
        if (!VoteManager.eligibility.getOrDefault(player.uuid(), false)) return; // not eligible
        if (VoteManager.hasVoted(player.uuid())) return; // already voted

        player.sendMessage(Message.literal("A vote is currently open! Use /vote to participate.")
                .color(Color.GREEN));
    }
}
