package com.gentheowl.aomc_utils.renaming.commands.customize;

import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import com.gentheowl.aomc_utils.renaming.item.ItemManager;
import com.gentheowl.aomc_utils.renaming.item.ItemModificationResult;
import com.gentheowl.aomc_utils.renaming.utils.ConfigSettings;
import parliament.api.command.Argument;
import parliament.api.command.Command;
import parliament.api.command.CommandContext;
import parliament.api.entity.Player;
import parliament.api.permission.PermissionLevel;

public class SignSubcommand implements Subcommand {
    private static final String NAME = ConfigSettings.SIGN;
    private static final String USAGE = "<true | false>";
    private static final String DESC  = "Sign the item so it can no longer be edited.";

    @Override
    public Command attach(CommandRoot parent) {
        return Command.literal(NAME)
                .requiresPermission("renameit.customize.sign", PermissionLevel.EVERYONE)
                .executes(parent::sendHelp)
                .child(Command.argument(Argument.ofBoolean("withText"))
                        .executes(this::execute)
                );
    }

    @Override public String getName()        { return NAME; }
    @Override public String getUsage()       { return USAGE; }
    @Override public String getDescription() { return DESC;  }

    private void execute(CommandContext ctx) {
        Player player = ctx.player().orElse(null);
        boolean withText = ctx.getBoolean("withText");
        ItemModificationResult result = ItemManager.SIGN.validateAndRun(player, new ItemManager.SignParams(player, withText));
        ctx.sendSuccess(result.getMessage());
    }
}
