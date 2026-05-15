package com.gentheowl.aomc_utils.renaming.commands;

import com.gentheowl.aomc_utils.AOMCUtils;
import com.gentheowl.aomc_utils.renaming.commands.customize.CustomizeCommand;
import com.gentheowl.aomc_utils.renaming.commands.renameit.RenameitCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CommandRegistry {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CustomizeCommand.getInstance().register());
            dispatcher.register(RenameitCommand.getInstance().register());
        });
        AOMCUtils.LOGGER.info("Commands registered");
    }

}
