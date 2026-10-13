package com.gentheowl.aomc_utils.renaming.commands.customize;

import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import com.gentheowl.aomc_utils.renaming.item.ItemManager;
import com.gentheowl.aomc_utils.renaming.item.ItemModificationResult;
import com.gentheowl.aomc_utils.renaming.utils.ConfigSettings;
import com.gentheowl.aomc_utils.renaming.utils.TextUtil;
import parliament.api.command.Argument;
import parliament.api.command.Command;
import parliament.api.command.CommandContext;
import parliament.api.entity.Player;
import parliament.api.permission.PermissionLevel;
import parliament.api.text.Message;

public class NameSubcommand implements Subcommand {
    private static final String NAME = ConfigSettings.MODIFY_NAME;
    private static final String USAGE = "<text>";
    private static final String DESCRIPTION = "Sets the display name of the item in hand, supports formatting codes.";

    @Override
    public Command attach(CommandRoot parent) {
        return Command.literal(NAME)
                .requiresPermission("renameit.customize.name", PermissionLevel.EVERYONE)
                .executes(parent::sendHelp)
                .child(Command.argument(Argument.ofRemainingText("text"))
                        .executes(this::executeSetName)
                );
    }

    @Override public String getName() { return NAME; }
    @Override public String getUsage() { return USAGE; }
    @Override public String getDescription() { return DESCRIPTION; }

    private void executeSetName(CommandContext ctx) {
        Player player = ctx.player().orElse(null);

        String raw = ctx.getText("text");
        Message formatted = TextUtil.parse(raw);
        ItemModificationResult result = ItemManager.MODIFY_NAME.validateAndRun(player, formatted);
        ctx.sendSuccess(result.getMessage());
    }
}
