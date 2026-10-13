package com.gentheowl.aomc_utils.renaming.commands.renameit;

import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import com.gentheowl.aomc_utils.renaming.item.ItemModificationResult;
import com.gentheowl.aomc_utils.renaming.utils.RenameitConfig;
import parliament.api.command.Argument;
import parliament.api.command.Command;
import parliament.api.command.CommandContext;

public class MaxLoreSubcommand implements Subcommand {
    private static final String NAME = "max_lore";
    private static final String DESC = "Allows you to set the max lore (in lines).";
    private static final String USAGE = "<amount 1-256>";

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
        return Command.literal(NAME)
                .child(Command.argument(Argument.ofInteger("lines"))
                        .executes(this::setMaxLoreLines));
    }

    private void setMaxLoreLines(CommandContext ctx) {
        int lines = ctx.getInteger("lines");
        RenameitConfig.get().setMaxLoreLines(lines);
        ctx.sendSuccess(ItemModificationResult.GENERAL_SUCCESS.getMessage());
    }
}
