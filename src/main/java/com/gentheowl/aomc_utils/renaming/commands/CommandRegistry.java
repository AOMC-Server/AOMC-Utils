package com.gentheowl.aomc_utils.renaming.commands;

import com.gentheowl.aomc_utils.renaming.commands.customize.CustomizeCommand;
import com.gentheowl.aomc_utils.renaming.commands.renameit.RenameitCommand;
import com.gentheowl.aomc_utils.renaming.utils.RenameitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parliament.api.Parliament;

public class CommandRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger("aomc_utils");

    public static void initialize(Parliament parliament) {
        RenameitConfig.createAndLoad(parliament.configDirectory());
        parliament.commands().register(CustomizeCommand.getInstance().register());
        parliament.commands().register(RenameitCommand.getInstance().register());
        LOGGER.info("Commands registered");
    }

}
