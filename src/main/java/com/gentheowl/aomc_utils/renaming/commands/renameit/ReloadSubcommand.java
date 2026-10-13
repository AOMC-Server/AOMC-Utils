package com.gentheowl.aomc_utils.renaming.commands.renameit;

import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import com.gentheowl.aomc_utils.renaming.utils.RenameitConfig;
import parliament.api.command.Command;

public class ReloadSubcommand implements Subcommand {
    private static final String NAME = "reload";
    private static final String DESC = "Reloads the config file.";
    private static final String USAGE = "";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public String getUsage() {
        return USAGE;
    }

    @Override
    public Command attach(CommandRoot parent) {
        return Command.literal(NAME).executes((ctx) -> RenameitConfig.reload());
    }
}
