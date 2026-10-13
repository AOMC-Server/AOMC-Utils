package com.gentheowl.aomc_utils.renaming.commands.renameit;

import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import com.gentheowl.aomc_utils.renaming.item.ItemModificationResult;
import com.gentheowl.aomc_utils.renaming.utils.RenameitConfig;
import parliament.api.command.Argument;
import parliament.api.command.Command;
import parliament.api.command.CommandContext;

/**
 * Still writes the {@code use_permissions_api} config flag, but the flag is
 * decorative since the Parliament port: permission nodes are always checked,
 * with an operator-tier fallback when no permission mod is installed.
 */
public class PermissionAPISubcommand implements Subcommand {
    private static final String NAME = "use_permission_api";
    private static final String DESC = "Toggle whether or not to use the permissions API.";
    private static final String USAGE = "<true | false>";

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
                .child(Command.argument(Argument.ofBoolean("value"))
                        .executes(this::setPermissionAPI));
    }

    private void setPermissionAPI(CommandContext ctx) {
        boolean value = ctx.getBoolean("value");
        RenameitConfig.get().setPermissionAPIenabled(value);
        ctx.sendSuccess(ItemModificationResult.GENERAL_SUCCESS.getMessage());
    }
}
