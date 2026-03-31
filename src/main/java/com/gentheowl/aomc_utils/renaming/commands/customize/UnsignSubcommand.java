package com.gentheowl.aomc_utils.renaming.commands;

import com.gentheowl.aomc_utils.renaming.item.ItemManager;
import com.gentheowl.aomc_utils.renaming.item.ItemModificationResult;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class UnsignSubcommand implements DescribableCommand {
    private static final String NAME = "unsign";
    private static final String USAGE = "";
    private static final String DESC  = "Unlock a signed item to re-enable editing (only if you are the one who signed it!)";

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> attach() {
        return CommandManager.literal(NAME).executes(this::execute);
    }

    @Override public String getName()        { return NAME; }
    @Override public String getUsage()       { return USAGE; }
    @Override public String getDescription() { return DESC;  }

    private int execute(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity player = src.getPlayer();
        ItemModificationResult result = ItemManager.UNSIGN.validateAndRun(player, player);
        src.sendFeedback(result::getMessage, false);
        return 1;
    }
}