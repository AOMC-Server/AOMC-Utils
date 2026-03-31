package com.gentheowl.aomc_utils.renaming.commands;

import com.gentheowl.aomc_utils.renaming.utils.TextUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class CustomizeCommand {
    private static final String ROOT = "customize";
    private static final List<DescribableCommand> SUBCOMMANDS = List.of(
            new NameSubcommand(),
            new LoreSubcommand(),
            new GlintSubcommand(),
            new SignSubcommand(),
            new UnsignSubcommand()
    );

    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        LiteralArgumentBuilder<ServerCommandSource> root =
                CommandManager.literal(ROOT)
                        .requires(src -> src.hasPermissionLevel(0))
                        .executes(ctx -> {
                            sendHelp(ctx.getSource());
                            return 1;
                        });

        for (DescribableCommand sub : SUBCOMMANDS) {
            root.then(sub.attach());
        }

        return root;
    }

    public static void sendHelp(ServerCommandSource src) {
        src.sendFeedback(() -> TextUtil.header("/" + ROOT + " Commands"), false);
        for (DescribableCommand sub : SUBCOMMANDS) {
            Text cmdLine = Text.literal("• ")
                    .append(Text.literal("/" + ROOT + " " + sub.getName())
                            .styled(s -> s.withColor(Formatting.AQUA)))
                    .append(Text.literal(" " + sub.getUsage()).styled(s -> s.withColor(Formatting.DARK_AQUA)));
            src.sendFeedback(() -> cmdLine, false);

            Text descLine = Text.literal("    " + sub.getDescription())
                    .styled(s -> s.withColor(Formatting.GRAY).withItalic(true));
            src.sendFeedback(() -> descLine, false);
        }
    }
}
