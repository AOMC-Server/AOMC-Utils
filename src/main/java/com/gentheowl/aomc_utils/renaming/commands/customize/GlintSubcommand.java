package com.gentheowl.aomc_utils.renaming.commands;

import com.gentheowl.aomc_utils.renaming.item.ItemManager;
import com.gentheowl.aomc_utils.renaming.item.ItemModificationResult;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class GlintSubcommand implements DescribableCommand {
    private static final String NAME = "glint";
    private static final String USAGE = "<true | false>";
    private static final String DESC  = "Toggle enchantment glint effect on the item in hand.";

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> attach() {
        return CommandManager.literal(NAME)
                .executes(ctx -> { CustomizeCommand.sendHelp(ctx.getSource()); return 1; })
                .then(CommandManager.argument("value", BoolArgumentType.bool())
                        .executes(this::execute)
                );
    }

    @Override public String getName()        { return NAME; }
    @Override public String getUsage()       { return USAGE; }
    @Override public String getDescription() { return DESC;  }

    private int execute(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity player = src.getPlayer();
        boolean shouldGlint = BoolArgumentType.getBool(ctx, "value");
        ItemModificationResult result = ItemManager.GLINT.validateAndRun(player, shouldGlint);
        src.sendFeedback(result::getMessage, false);
        return 1;
    }
}
