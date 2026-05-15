package com.gentheowl.aomc_utils.renaming.commands;

import com.gentheowl.aomc_utils.renaming.utils.TextUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public abstract class CommandRoot {
    public abstract String getRootName();
    public abstract List<Subcommand> getSubcommands();

    public void sendHelp(CommandSourceStack src) {
        String root = this.getRootName();
        List<Subcommand> subcommands = this.getSubcommands();

        src.sendSuccess(() -> TextUtil.header("/" + root + " Commands"), false);
        for (Subcommand sub : subcommands) {
            Component cmdLine = Component.literal("• ")
                    .append(Component.literal("/" + root + " " + sub.getName())
                            .withStyle(s -> s.withColor(ChatFormatting.AQUA)))
                    .append(Component.literal(" " + sub.getUsage()).withStyle(s -> s.withColor(ChatFormatting.DARK_AQUA)));
            src.sendSuccess(() -> cmdLine, false);

            Component descLine = Component.literal("    " + sub.getDescription())
                    .withStyle(s -> s.withColor(ChatFormatting.GRAY).withItalic(true));
            src.sendSuccess(() -> descLine, false);
        }
    }

    public LiteralArgumentBuilder<CommandSourceStack> register() {
        LiteralArgumentBuilder<CommandSourceStack> root =
                Commands.literal(getRootName())
                        .requires(this::getRequiredPermissions)
                        .executes(ctx -> {
                            sendHelp(ctx.getSource());
                            return 1;
                        });

        for (Subcommand sub : getSubcommands()) {
            root.then(sub.attach(this));
        }

        return root;
    }

    protected abstract boolean getRequiredPermissions(CommandSourceStack source);
}
