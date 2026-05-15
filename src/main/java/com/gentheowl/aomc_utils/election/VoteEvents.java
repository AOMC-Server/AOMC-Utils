package com.gentheowl.aomc_utils.election;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

public class VoteEvents {

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, a, b) -> {
            handlePlayerJoin(handler.getPlayer());
        });
    }

    private static void handlePlayerJoin(ServerPlayer player) {
        if (!VoteManager.isVotingEnabled()) return; // voting disabled
        if (!VoteManager.eligibility.getOrDefault(player.getUUID(), false)) return; // not eligible

        player.displayClientMessage(Component.literal("A vote is currently open! Use /vote to participate.")
                .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)), false);
    }
}
