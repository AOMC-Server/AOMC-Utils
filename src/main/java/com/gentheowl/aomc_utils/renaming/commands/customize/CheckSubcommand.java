package com.gentheowl.aomc_utils.renaming.commands.customize;

import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import com.gentheowl.aomc_utils.renaming.utils.TextUtil;
import parliament.api.command.Command;
import parliament.api.command.CommandContext;
import parliament.api.data.Attachment;
import parliament.api.entity.Player;
import parliament.api.item.ItemStack;
import parliament.api.permission.PermissionLevel;
import parliament.api.text.Color;
import parliament.api.text.Message;

public class CheckSubcommand implements Subcommand {
    private static final String NAME = "check";
    private static final String USAGE = "";
    private static final String DESC  = "Check whether the held item is genuinely signed, and by whom.";

    @Override
    public Command attach(CommandRoot parent) {
        return Command.literal(NAME)
                .requiresPermission("renameit.customize.check", PermissionLevel.EVERYONE)
                .executes(this::execute);
    }

    @Override public String getName()        { return NAME; }
    @Override public String getUsage()       { return USAGE; }
    @Override public String getDescription() { return DESC;  }

    private void execute(CommandContext ctx) {
        Player player = ctx.player().orElse(null);
        if (player == null) {
            ctx.sendFailure(TextUtil.error("This can only be run by players."));
            return;
        }

        ItemStack stack = player.mainHandItem();
        if (stack.isEmpty()) {
            ctx.sendFailure(TextUtil.error("No item in hand."));
            return;
        }

        Attachment data = stack.attachment();
        if (!data.has("signed")) {
            ctx.sendSuccess(TextUtil.info("This item is not signed."));
            return;
        }

        String signerUuid = data.getString("signer", "");
        String storedName = data.getString("signer_name", "");
        String signerName = storedName.isEmpty() ? "unknown" : storedName;

        ctx.sendSuccess(TextUtil.success("This item is signed."));
        ctx.sendSuccess(detailLine("Signer", signerName));
        ctx.sendSuccess(detailLine("UUID", signerUuid.isEmpty() ? "unknown" : signerUuid));
    }

    private static Message detailLine(String label, String value) {
        return Message.literal("  " + label + ": ").color(Color.GRAY)
                .append(Message.literal(value).color(Color.WHITE));
    }
}
