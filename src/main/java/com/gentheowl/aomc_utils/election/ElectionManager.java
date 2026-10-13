package com.gentheowl.aomc_utils.election;

import com.gentheowl.aomc_utils.election.commands.VoteCommand;
import parliament.api.Parliament;

public class ElectionManager {
    public static void initialize(Parliament parliament) {
        VoteManager.loadConfig(parliament.configDirectory());
        VoteCommand.register(parliament.commands());
        VoteEvents.register(parliament.events());

        parliament.events().onPlayerDisconnect(player -> VoteManager.cancelSession(player.uuid()));
    }
}
