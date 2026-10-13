package com.gentheowl.aomc_utils.renaming.commands.customize;

import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import com.gentheowl.aomc_utils.renaming.item.ItemManager;
import com.gentheowl.aomc_utils.renaming.item.ItemModificationResult;
import com.gentheowl.aomc_utils.renaming.utils.ConfigSettings;
import parliament.api.command.Command;
import parliament.api.command.CommandContext;
import parliament.api.entity.Player;
import parliament.api.permission.PermissionLevel;

public class UnsignSubcommand implements Subcommand {
    private static final String NAME = ConfigSettings.UNSIGN;
    private static final String USAGE = "";
    private static final String DESC  = "Unlock a signed item to re-enable editing (only if you are the one who signed it!)";

    @Override
    public Command attach(CommandRoot parent) {
        return Command.literal(NAME)
                .requiresPermission("renameit.customize.unsign", PermissionLevel.EVERYONE)
                .executes(this::execute);
    }

    @Override public String getName()        { return NAME; }
    @Override public String getUsage()       { return USAGE; }
    @Override public String getDescription() { return DESC;  }

    private void execute(CommandContext ctx) {
        Player player = ctx.player().orElse(null);
        ItemModificationResult result = ItemManager.UNSIGN.validateAndRun(player, player);
        ctx.sendSuccess(result.getMessage());
    }
}
