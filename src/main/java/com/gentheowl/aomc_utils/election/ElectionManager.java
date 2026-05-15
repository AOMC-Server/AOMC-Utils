package com.gentheowl.aomc_utils.election;

import com.gentheowl.aomc_utils.election.commands.VoteCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class ElectionManager {
    public static void initialize() {
        VoteManager.loadConfig();
        CommandRegistrationCallback.EVENT.register(((dispatcher, registry, env) -> {
            VoteCommand.register(dispatcher);
        }));

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            VoteManager.cancelSession(player.getUUID());
        });
    }
}
