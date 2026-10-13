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

public class GlintSubcommand implements Subcommand {
    private static final String NAME = ConfigSettings.GLINT;
    private static final String USAGE = "<true | false>";
    private static final String DESC  = "Toggle enchantment glint effect on the item in hand.";

    @Override
    public Command attach(CommandRoot parent) {
        return Command.literal(NAME)
                .requiresPermission("renameit.customize.glint", PermissionLevel.EVERYONE)
                .executes(parent::sendHelp)
                .child(Command.argument(Argument.ofBoolean("value"))
                        .executes(this::execute)
                );
    }

    @Override public String getName()        { return NAME; }
    @Override public String getUsage()       { return USAGE; }
    @Override public String getDescription() { return DESC;  }

    private void execute(CommandContext ctx) {
        Player player = ctx.player().orElse(null);
        boolean shouldGlint = ctx.getBoolean("value");
        ItemModificationResult result = ItemManager.GLINT.validateAndRun(player, shouldGlint);
        ctx.sendSuccess(result.getMessage());
    }
}
