package com.gentheowl.aomc_utils.renaming.commands;

import com.gentheowl.aomc_utils.renaming.item.ItemManager;
import com.gentheowl.aomc_utils.renaming.item.ItemModificationResult;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class SignSubcommand implements DescribableCommand {
    private static final String NAME = "sign";
    private static final String USAGE = "<true | false>";
    private static final String DESC  = "Sign the item so it can no longer be edited.";

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> attach() {
        return CommandManager.literal(NAME)
                .executes(ctx -> { CustomizeCommand.sendHelp(ctx.getSource()); return 1; })
                .then(CommandManager.argument("withText", BoolArgumentType.bool())
                        .executes(this::execute)
                );
    }

    @Override public String getName()        { return NAME; }
    @Override public String getUsage()       { return USAGE; }
    @Override public String getDescription() { return DESC;  }

    private int execute(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity player = src.getPlayer();
        boolean withText = BoolArgumentType.getBool(ctx, "withText");
        ItemModificationResult result = ItemManager.SIGN.validateAndRun(player, new ItemManager.SignParams(player, withText));
        src.sendFeedback(result::getMessage, false);
        return 1;
    }
}
