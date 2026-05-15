package com.gentheowl.aomc_utils.renaming.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

public interface Subcommand {
    String getName();
    String getDescription();
    String getUsage();
// --Commented out by Inspection START (10/21/2025 11:54 AM):
    default boolean getRequiredPermission(CommandSourceStack src) {
        return true;
    }
// --Commented out by Inspection STOP (10/21/2025 11:54 AM)

    LiteralArgumentBuilder<CommandSourceStack> attach(CommandRoot parent);

}
