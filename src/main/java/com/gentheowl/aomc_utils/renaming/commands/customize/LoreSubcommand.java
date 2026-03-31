package com.gentheowl.aomc_utils.renaming.commands;

import com.gentheowl.aomc_utils.renaming.item.ItemManager;
import com.gentheowl.aomc_utils.renaming.item.ItemModificationResult;
import com.gentheowl.aomc_utils.renaming.utils.TextUtil;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

public class LoreSubcommand implements DescribableCommand {
    private static final String NAME = "lore";
    private static final String USAGE = "<set | add | remove | insert> [index] [text]";
    private static final String DESC  = "Manage lore lines on the item: set all, add one, remove by index, or insert at index.";

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> attach() {
        return CommandManager.literal(NAME)
                .executes(ctx -> { CustomizeCommand.sendHelp(ctx.getSource()); return 1; })
                .then(literal("set")
                        .then(CommandManager.argument("text", StringArgumentType.greedyString())
                                .executes(ctx -> apply(ctx, Action.SET, StringArgumentType.getString(ctx, "text"), 0))
                        )
                )
                .then(literal("add")
                        .then(CommandManager.argument("text", StringArgumentType.greedyString())
                                .executes(ctx -> apply(ctx, Action.ADD, StringArgumentType.getString(ctx, "text"), -1))
                        )
                )
                .then(literal("remove")
                        .then(CommandManager.argument("index", IntegerArgumentType.integer(1))
                                .executes(ctx -> apply(ctx, Action.REMOVE, null, IntegerArgumentType.getInteger(ctx, "index") - 1))
                        )
                )
                .then(literal("insert")
                        .then(CommandManager.argument("index", IntegerArgumentType.integer(1))
                                .then(CommandManager.argument("text", StringArgumentType.greedyString())
                                        .executes(ctx -> apply(ctx, Action.INSERT,
                                                StringArgumentType.getString(ctx, "text"),
                                                IntegerArgumentType.getInteger(ctx, "index") - 1))
                                )
                        )
                );
    }

    @Override public String getName()        { return NAME; }
    @Override public String getUsage()       { return USAGE; }
    @Override public String getDescription() { return DESC;  }

    private int apply(CommandContext<ServerCommandSource> ctx, Action action, String raw, int idx) {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity player = src.getPlayer();
        if (raw == null && action != Action.REMOVE) {
            src.sendFeedback(ItemModificationResult.GENERIC_FAIL::getMessage, false);
            return 0;
        }
        Text line = raw != null ? TextUtil.parse(raw, src) : null;
        ItemModificationResult result;
        switch (action) {
            case SET    -> result = ItemManager.SET_LORE.validateAndRun(player, List.of(line));
            case ADD    -> result = ItemManager.PUSH_LORE.validateAndRun(player, line);
            case REMOVE -> result = ItemManager.REMOVE_LORE_LINE.validateAndRun(player, idx);
            case INSERT -> result = ItemManager.INSERT_LORE_LINE.validateAndRun(player, new ItemManager.InsertLoreParams(idx, line));
            default     -> { return 0; }
        }
        src.sendFeedback(result::getMessage, false);
        return 1;
    }

    private enum Action { SET, ADD, REMOVE, INSERT }
}