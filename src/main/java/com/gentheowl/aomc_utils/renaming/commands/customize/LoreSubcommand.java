package com.gentheowl.aomc_utils.renaming.commands.customize;

import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import com.gentheowl.aomc_utils.renaming.item.ItemManager;
import com.gentheowl.aomc_utils.renaming.item.ItemModificationResult;
import com.gentheowl.aomc_utils.renaming.utils.ConfigSettings;
import com.gentheowl.aomc_utils.renaming.utils.TextUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import parliament.api.command.Argument;
import parliament.api.command.Command;
import parliament.api.command.CommandContext;
import parliament.api.entity.Player;
import parliament.api.permission.PermissionLevel;
import parliament.api.text.Message;

public class LoreSubcommand implements Subcommand {
    private static final String NAME = ConfigSettings.LORE_BASE;
    private static final String USAGE = "<set | add | remove | insert> [index] [text]";
    private static final String DESC  = "Manage lore lines on the item: set all, add one, remove by index, or insert at index.";

    @Override
    public Command attach(CommandRoot parent) {
        return Command.literal(NAME)
                .requiresPermission("renameit.customize.lore", PermissionLevel.EVERYONE)
                .executes(parent::sendHelp)
                .child(Command.literal("set")
                        .child(Command.argument(Argument.ofRemainingText("text"))
                                .executes(ctx -> apply(ctx, Action.SET, ctx.getText("text"), 0))
                        )
                )
                .child(Command.literal("add")
                        .child(Command.argument(Argument.ofRemainingText("text"))
                                .executes(ctx -> apply(ctx, Action.ADD, ctx.getText("text"), -1))
                        )
                )
                .child(Command.literal("remove").executes(ctx -> apply(ctx, Action.REMOVE, null, null))
                        .child(Command.argument(Argument.ofInteger("index", 1, Integer.MAX_VALUE))
                                .executes(ctx -> apply(ctx, Action.REMOVE, null, ctx.getInteger("index") - 1))
                        )
                )
                .child(Command.literal("insert")
                        .child(Command.argument(Argument.ofInteger("index", 1, Integer.MAX_VALUE))
                                .child(Command.argument(Argument.ofRemainingText("text"))
                                        .executes(ctx -> apply(ctx, Action.INSERT,
                                                ctx.getText("text"),
                                                ctx.getInteger("index") - 1))
                                )
                        )
                );
    }

    @Override public String getName()        { return NAME; }
    @Override public String getUsage()       { return USAGE; }
    @Override public String getDescription() { return DESC;  }

    private void apply(CommandContext ctx, Action action, @Nullable String raw, @Nullable Integer idx) {
        Player player = ctx.player().orElse(null);
        if ((raw == null || idx == null) && action != Action.REMOVE) {
            ctx.sendSuccess(ItemModificationResult.GENERIC_FAIL.getMessage());
            return;
        }
        Message line = raw != null ? TextUtil.parse(raw) : null;
        ItemModificationResult result;
        switch (action) {
            case SET    -> result = ItemManager.SET_LORE.validateAndRun(player, List.of(line));
            case ADD    -> result = ItemManager.PUSH_LORE.validateAndRun(player, line);
            case REMOVE -> result = ItemManager.REMOVE_LORE_LINE.validateAndRun(player, idx);
            case INSERT -> result = ItemManager.INSERT_LORE_LINE.validateAndRun(player, new ItemManager.InsertLoreParams(idx, line));
            default     -> { return; }
        }
        ctx.sendSuccess(result.getMessage());
    }

    private enum Action { SET, ADD, REMOVE, INSERT }
}
