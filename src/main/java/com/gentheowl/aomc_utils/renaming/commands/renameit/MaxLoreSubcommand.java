package com.gentheowl.aomc_utils.renaming.commands.renameit;

import com.gentheowl.aomc_utils.AOMCUtils;
import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import com.gentheowl.aomc_utils.renaming.item.ItemModificationResult;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

@SuppressWarnings("ALL")
public class MaxLoreSubcommand implements Subcommand {
    private static final String NAME = "max_lore";
    private static final String DESC = "Allows you to set the max lore (in lines).";
    private static final String USAGE = "<amount 1-256>";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public String getUsage() {
        return USAGE;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> attach(CommandRoot parent) {
        return Commands.literal(NAME)
                .then(Commands.argument("lines", IntegerArgumentType.integer())
                        .executes(this::setMaxLoreLines));
    }

    private int setMaxLoreLines(CommandContext<CommandSourceStack> ctx) {
        int lines = IntegerArgumentType.getInteger(ctx, "lines");
        AOMCUtils.CONFIG.setMaxLoreLines(lines);
        ctx.getSource().sendSuccess(ItemModificationResult.GENERAL_SUCCESS::getMessage, true);
        return 1;
    }
}
