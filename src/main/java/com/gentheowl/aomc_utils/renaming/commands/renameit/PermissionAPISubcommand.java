package com.gentheowl.aomc_utils.renaming.commands.renameit;

import com.gentheowl.aomc_utils.AOMCUtils;
import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import com.gentheowl.aomc_utils.renaming.item.ItemModificationResult;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class PermissionAPISubcommand implements Subcommand {
    private static final String NAME = "use_permission_api";
    private static final String DESC = "Toggle whether or not to use the permissions API.";
    private static final String USAGE = "<true | false>";

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
                .then(Commands.argument("value", BoolArgumentType.bool())
                        .executes(this::setMaxLoreLines));
    }

    @SuppressWarnings("SameReturnValue")
    private int setMaxLoreLines(CommandContext<CommandSourceStack> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        AOMCUtils.CONFIG.setPermissionAPIenabled(value);
        ctx.getSource().sendSuccess(ItemModificationResult.GENERAL_SUCCESS::getMessage, true);
        return 1;
    }
}
