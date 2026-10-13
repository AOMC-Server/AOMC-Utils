package com.gentheowl.aomc_utils.renaming.commands.renameit;

import com.gentheowl.aomc_utils.renaming.commands.CommandRoot;
import com.gentheowl.aomc_utils.renaming.commands.Subcommand;
import com.gentheowl.aomc_utils.renaming.item.ItemModificationResult;
import com.gentheowl.aomc_utils.renaming.utils.ConfigSettings;
import com.gentheowl.aomc_utils.renaming.utils.RenameitConfig;

import java.util.ArrayList;
import java.util.List;
import parliament.api.command.Argument;
import parliament.api.command.Command;
import parliament.api.command.CommandContext;
import parliament.api.command.SuggestionProvider;

public class FormatSubcommand implements Subcommand {
    private static final String NAME = "format";
    private static final String DESC = "Allows you to set the text formatting options for customization.";
    private static final String USAGE = "<simplified | markdown | legacyAll | legacyColor>...";

    /** Suggests the formats not already used by the arguments before this one. */
    private static SuggestionProvider formatSuggestions(int index) {
        return ctx -> {
            List<String> used = collectFormats(ctx, index - 1);

            List<String> suggestions = new ArrayList<>();
            for (String fmt : ConfigSettings.ALLOWED_TEXT_FORMATS) {
                if (!used.contains(fmt)) {
                    suggestions.add(fmt);
                }
            }
            return suggestions;
        };
    }

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
    public Command attach(CommandRoot parent) {
        return Command.literal(NAME).child(buildArgChain(0));
    }

    private Command buildArgChain(int index) {
        String argName = "format" + index;

        Command node = Command.argument(Argument.ofWord(argName).suggests(formatSuggestions(index)));

        if (index < ConfigSettings.ALLOWED_TEXT_FORMATS.size()) {
            node.child(buildArgChain(index + 1));
        }

        node.executes(ctx -> setFormats(ctx, index));
        return node;
    }


    private void setFormats(CommandContext ctx, int maxIndex) {
        List<String> formats = new ArrayList<>();
        for (String val : collectFormats(ctx, maxIndex)) {
            if (!ConfigSettings.ALLOWED_TEXT_FORMATS.contains(val)) {
                ctx.sendFailure(ItemModificationResult.failure("Invalid format: " + val).getMessage());
                return;
            }

            if (formats.contains(val)) {
                ctx.sendFailure(ItemModificationResult.failure("Format repeated: " + val).getMessage());
                return;
            }

            formats.add(val);
        }

        RenameitConfig.get().setFormatOptions(formats);
        ctx.sendSuccess(ItemModificationResult.GENERAL_SUCCESS.getMessage());
    }

    /** The values of the {@code format0..formatMaxIndex} arguments that have been parsed so far. */
    private static List<String> collectFormats(CommandContext ctx, int maxIndex) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i <= maxIndex; i++) {
            try {
                values.add(ctx.getText("format" + i));
            } catch (IllegalArgumentException absent) {
                break;
            }
        }
        return values;
    }
}
