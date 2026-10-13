package com.gentheowl.aomc_utils.renaming.commands.renameit;

import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import com.gentheowl.aomc_utils.renaming.item.ItemModificationResult;
import com.gentheowl.aomc_utils.renaming.utils.RenameitConfig;
import parliament.api.command.Argument;
import parliament.api.command.Command;
import parliament.api.command.CommandContext;

public class ModifySubcommand implements Subcommand {
    private static final String NAME = "command";
    private static final String DESC = "Toggles commands on or off.";
    private static final String USAGE = "<command name> enable | disable";

    @Override
    public String getName() { return NAME; }

    @Override
    public String getDescription() { return DESC; }

    @Override
    public String getUsage() { return USAGE; }

    @Override
    public Command attach(CommandRoot parent) {
        Command base = Command.literal(NAME);
        for (String commandName : RenameitConfig.get().command_data().keySet()) {
            base.child(
                    Command.literal(commandName)
                            .child(Command.literal("enable").executes(ctx -> toggleCommand(ctx, commandName, true)))
                            .child(Command.literal("disable").executes(ctx -> toggleCommand(ctx, commandName, false)))
                            .child(Command.literal("cost")
                                    .child(Command.argument(Argument.ofInteger("amount"))
                                            .executes(
                                                    ctx -> toggleCost(ctx, commandName, ctx.getInteger("amount")
                                                    )
                                            )
                                    )
                            )
            );
        }

        return base;
    }

    private void toggleCommand(CommandContext ctx, String commandName, boolean enabled) {
        RenameitConfig.get().toggleCommand(commandName, enabled);
        RenameitConfig.reload();
        ctx.sendSuccess(ItemModificationResult.GENERAL_SUCCESS.getMessage());
    }

    private void toggleCost(CommandContext ctx, String commandName, int amount) {
        RenameitConfig.get().setCost(commandName, amount);
        RenameitConfig.reload();
        ctx.sendSuccess(ItemModificationResult.GENERAL_SUCCESS.getMessage());
    }
}
