package com.gentheowl.aomc_utils.renaming.commands.customize;

import com.gentheowl.aomc_utils.AOMCUtils;
import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import com.gentheowl.aomc_utils.renaming.item.ItemManager;
import com.gentheowl.aomc_utils.renaming.item.ItemModificationResult;
import com.gentheowl.aomc_utils.renaming.utils.ConfigSettings;
import com.gentheowl.aomc_utils.renaming.utils.TextUtil;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minecraft.commands.Commands.literal;

public class LoreSubcommand implements Subcommand {
    private static final String NAME = ConfigSettings.LORE_BASE;
    private static final String USAGE = "<set | add | remove | insert> [index] [text]";
    private static final String DESC  = "Manage lore lines on the item: set all, add one, remove by index, or insert at index.";

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> attach(CommandRoot parent) {
        return Commands.literal(NAME)
                .requires(this::getRequiredPermission)
                .executes(ctx -> { parent.sendHelp(ctx.getSource()); return 1; })
                .then(literal("set")
                        .then(Commands.argument("text", StringArgumentType.greedyString())
                                .executes(ctx -> apply(ctx, Action.SET, StringArgumentType.getString(ctx, "text"), 0))
                        )
                )
                .then(literal("add")
                        .then(Commands.argument("text", StringArgumentType.greedyString())
                                .executes(ctx -> apply(ctx, Action.ADD, StringArgumentType.getString(ctx, "text"), -1))
                        )
                )
                .then(literal("remove").executes(ctx -> apply(ctx, Action.REMOVE, null, null))
                        .then(Commands.argument("index", IntegerArgumentType.integer(1))
                                .executes(ctx -> apply(ctx, Action.REMOVE, null, IntegerArgumentType.getInteger(ctx, "index") - 1))
                        )
                )
                .then(literal("insert")
                        .then(Commands.argument("index", IntegerArgumentType.integer(1))
                                .then(Commands.argument("text", StringArgumentType.greedyString())
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

    private int apply(CommandContext<CommandSourceStack> ctx, Action action, String raw, @Nullable Integer idx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayer();
        if ((raw == null || idx == null) && action != Action.REMOVE) {
            src.sendSuccess(ItemModificationResult.GENERIC_FAIL::getMessage, false);
            return 0;
        }
        Component line = raw != null ? TextUtil.parse(raw, src) : null;
        ItemModificationResult result;
        switch (action) {
            case SET    -> result = ItemManager.SET_LORE.validateAndRun(player, List.of(line));
            case ADD    -> result = ItemManager.PUSH_LORE.validateAndRun(player, line);
            case REMOVE -> result = ItemManager.REMOVE_LORE_LINE.validateAndRun(player, idx);
            case INSERT -> result = ItemManager.INSERT_LORE_LINE.validateAndRun(player, new ItemManager.InsertLoreParams(idx, line));
            default     -> { return 0; }
        }
        src.sendSuccess(result::getMessage, false);
        return 1;
    }

    @Override
    public boolean getRequiredPermission(CommandSourceStack src) {
        return !AOMCUtils.CONFIG.shouldUsePermissionsAPI() || Permissions.check(src, "renameit.customize.lore");
    }

    private enum Action { SET, ADD, REMOVE, INSERT }
}