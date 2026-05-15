package com.gentheowl.aomc_utils.renaming.commands.renameit;

import com.gentheowl.aomc_utils.AOMCUtils;
import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import com.gentheowl.aomc_utils.renaming.item.ItemModificationResult;
import com.gentheowl.aomc_utils.renaming.utils.ConfigSettings;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class FormatSubcommand implements Subcommand {
    private static final String NAME = "format";
    private static final String DESC = "Allows you to set the text formatting options for customization.";
    private static final String USAGE = "<simplified | markdown | legacyAll | legacyColor>...";

    private static final SuggestionProvider<CommandSourceStack> FMT_SUGGESTIONS = (ctx, builder) -> {
        List<String> used = new ArrayList<>();

        ctx.getNodes().forEach(node -> {
            String name = node.getNode().getName();
            if (name.startsWith("format")) {
                try {
                    String value = ctx.getArgument(name, String.class);
                    used.add(value);
                } catch (IllegalArgumentException ignored) {}
            }
        });

        for (String fmt : ConfigSettings.ALLOWED_TEXT_FORMATS) {
            if (!used.contains(fmt) && fmt.startsWith(builder.getRemaining())) {
                builder.suggest(fmt);
            }
        }

        return builder.buildFuture();
    };

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
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(NAME);
        return root.then(buildArgChain(0));
    }

    private ArgumentBuilder<CommandSourceStack, ?> buildArgChain(int index) {
        String argName = "format" + index;

        ArgumentBuilder<CommandSourceStack, ?> node = Commands.argument(argName, StringArgumentType.word())
                .suggests(FMT_SUGGESTIONS);

        if (index < ConfigSettings.ALLOWED_TEXT_FORMATS.size()) { // Allow up to 10 formats
            node.then(buildArgChain(index + 1));
        }

        node.executes(ctx -> setFormats(ctx, index));
        return node;
    }


    private int setFormats(CommandContext<CommandSourceStack> ctx, int maxIndex) {
        List<String> formats = new ArrayList<>();
        for (int i = 0; i <= maxIndex; i++) {
            String key = "format" + i;
            if (ctx.getNodes().stream().noneMatch(n -> n.getNode().getName().equals(key))) break;

            String val = ctx.getArgument(key, String.class);

            if (!ConfigSettings.ALLOWED_TEXT_FORMATS.contains(val)) {
                ctx.getSource().sendFailure(ItemModificationResult.failure("Invalid format: " + val).getMessage());
                return 0;
            }

            if (formats.contains(val)) {
                ctx.getSource().sendFailure(ItemModificationResult.failure("Format repeated: " + val).getMessage());
                return 0;
            }

            formats.add(val);
        }

        AOMCUtils.CONFIG.setFormatOptions(formats);
        ctx.getSource().sendSuccess(ItemModificationResult.GENERAL_SUCCESS::getMessage, true);
        return 1;
    }
}