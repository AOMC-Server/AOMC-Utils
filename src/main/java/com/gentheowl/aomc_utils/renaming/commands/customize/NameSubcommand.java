package com.gentheowl.aomc_utils.renaming.commands;

import com.gentheowl.aomc_utils.renaming.item.ItemModificationResult;
import com.gentheowl.aomc_utils.renaming.item.ItemManager;
import com.gentheowl.aomc_utils.renaming.utils.TextUtil;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class NameSubcommand implements DescribableCommand {
    private static final String NAME = "name";
    private static final String USAGE = "<text>";
    private static final String DESCRIPTION = "Sets the display name of the item in hand, supports formatting codes.";

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> attach() {
        return CommandManager.literal(NAME)
                .executes(ctx -> { CustomizeCommand.sendHelp(ctx.getSource()); return 1; })
                .then(CommandManager.argument("text", StringArgumentType.greedyString())
                        .executes(this::executeSetName)
                );
    }

    @Override public String getName() { return NAME; }
    @Override public String getUsage() { return USAGE; }
    @Override public String getDescription() { return DESCRIPTION; }

    private int executeSetName(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();

        String raw = StringArgumentType.getString(ctx, "text");
        Text formatted = TextUtil.parse(raw, ctx.getSource());
        ItemModificationResult result = ItemManager.MODIFY_NAME.validateAndRun(player, formatted);
        ctx.getSource().sendFeedback(result::getMessage, false);
        return 1;
    }
}

