package com.gentheowl.aomc_utils.renaming.commands;

import parliament.api.command.Command;

public interface Subcommand {
    String getName();
    String getDescription();
    String getUsage();

    Command attach(CommandRoot parent);

}
