package com.gentheowl.aomc_utils.renaming.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;

public interface SubcommandInfo {
    String getName();
    String getDescription();
    String getUsage();
    LiteralArgumentBuilder<ServerCommandSource> attach();

}
