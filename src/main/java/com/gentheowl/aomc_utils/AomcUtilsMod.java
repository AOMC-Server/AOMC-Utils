package com.gentheowl.aomc_utils;

import com.gentheowl.aomc_utils.debug.RestartDetectorCommand;
import com.gentheowl.aomc_utils.election.ElectionManager;
import com.gentheowl.aomc_utils.motd.MotdManager;
import com.gentheowl.aomc_utils.progression.AomcProgression;
import com.gentheowl.aomc_utils.renaming.commands.CommandRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parliament.api.Parliament;
import parliament.api.ParliamentMod;
import parliament.api.fix.Fix;

public final class AomcUtilsMod implements ParliamentMod {

    public static final String MOD_ID = "aomc_utils";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private final AomcProgression progression = new AomcProgression();

    @Override
    public void initialize(Parliament parliament) {
        parliament.fixes().enable(Fix.FALLING_BLOCK_DUPLICATION);

        progression.initialize(parliament);
        ElectionManager.initialize(parliament);
        CommandRegistry.initialize(parliament);
        RestartDetectorCommand.register(parliament);

        parliament.events().onServerStarted(server ->
                MotdManager.initialize(server, parliament.configDirectory()));
    }
}
