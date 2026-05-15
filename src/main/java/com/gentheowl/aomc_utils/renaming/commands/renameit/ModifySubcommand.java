package com.gentheowl.aomc_utils.renaming.commands.renameit;

import com.gentheowl.aomc_utils.AOMCUtils;
import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import com.gentheowl.aomc_utils.renaming.commands.customize.CustomizeCommand;
import com.gentheowl.aomc_utils.renaming.item.ItemModificationResult;
import com.gentheowl.aomc_utils.renaming.utils.ConfigSettings;
import com.gentheowl.aomc_utils.renaming.utils.RenameitConfig;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

@SuppressWarnings("SameReturnValue")
public class ModifySubcommand implements Subcommand {
    private static final String NAME = "command";
    private static final String DESC = "Toggles commands on or off.";
    private static final String USAGE = "<command name> enable | disable";

    @Override
    public String getName() { return NAME; }

    @Override
    public String getDescription() { return DESC; }

    @Override
    public String getUsage() { return USAGE; }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> attach(CommandRoot parent) {
        LiteralArgumentBuilder<CommandSourceStack> base = Commands.literal(NAME);
        for (String commandName : AOMCUtils.CONFIG.command_data().keySet()) {
            base.then(
                    Commands.literal(commandName)
                            .then(Commands.literal("enable").executes(ctx -> toggleCommand(ctx, commandName,true)))
                            .then(Commands.literal("disable").executes(ctx -> toggleCommand(ctx, commandName,false)))
                            .then(Commands.literal("cost")
                                    .then(Commands.argument("amount", IntegerArgumentType.integer())
                                            .executes(
                                                    ctx -> toggleCost(ctx, commandName, IntegerArgumentType.getInteger(ctx, "amount")
                                                    )
                                            )
                                    )
                            )
            );
        }

        return base;
    }

    @SuppressWarnings("SameReturnValue")
    private int toggleCommand(CommandContext<CommandSourceStack> ctx, String commandName, boolean enabled) {
        AOMCUtils.CONFIG.toggleCommand(commandName, enabled);
        RenameitConfig.reload();
        ctx.getSource().sendSuccess(ItemModificationResult.GENERAL_SUCCESS::getMessage, true);
        return 1;
    }

    @SuppressWarnings("SameReturnValue")
    private int toggleCost(CommandContext<CommandSourceStack> ctx, String commandName, int amount) {
        AOMCUtils.CONFIG.setCost(commandName, amount);
        RenameitConfig.reload();
        ctx.getSource().sendSuccess(ItemModificationResult.GENERAL_SUCCESS::getMessage, true);
        return 1;
    }
}
