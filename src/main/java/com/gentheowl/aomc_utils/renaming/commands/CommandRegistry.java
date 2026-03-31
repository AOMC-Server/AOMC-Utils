package com.gentheowl.aomc_utils;

import com.gentheowl.aomc_utils.renaming.commands.CustomizeCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CommandRegistry {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CustomizeCommand.register());
        });
        AOMCUtils.LOGGER.info("/customize command registered");
    }
}
