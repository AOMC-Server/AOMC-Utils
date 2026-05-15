package com.gentheowl.aomc_utils.renaming.utils;

import com.gentheowl.aomc_utils.AOMCUtils;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.ParserBuilder;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class TextUtil {
    private static final NodeParser PARSER = getTextParser(AOMCUtils.CONFIG.format_options());
    public static final FontDescription UNFIFORM_FONT = new FontDescription.Resource(Identifier.withDefaultNamespace("uniform"));

    // Workaround for placeholder-api bug: <font:...> with an invalid ResourceLocation
    // path produces a Component that NPEs during network encoding and kicks the player.
    // Lowercase the tag value and strip any chars that aren't legal in a RL path.
    private static final Pattern FONT_TAG = Pattern.compile("<font:([^>]+)>");

    public static Component error(String msg) {
        return Component.literal("Error: " + msg).withStyle(ChatFormatting.RED);
    }

    public static Component success(String msg) {
        return Component.literal(msg).withStyle(ChatFormatting.GREEN);
    }

    public static Component info(String msg) {
        return Component.literal(msg).withStyle(ChatFormatting.YELLOW);
    }

    public static Component parse(String input, CommandSourceStack src) {
        String sanitized = FONT_TAG.matcher(input).replaceAll(m ->
                "<font:" + m.group(1).toLowerCase().replaceAll("[^a-z0-9/._:-]", "_") + ">");
        return PARSER.parseText(sanitized, PlaceholderContext.of(src).asParserContext());
    }

    public static Component signature(String name) {
        return Component.literal(" Signed by " + name).setStyle(Style.EMPTY.withFont(UNFIFORM_FONT).withColor(ChatFormatting.GRAY).withItalic(false));
    }

    public static Component header(String msg) {
        return Component.literal(msg)
                .withStyle(style -> style
                        .withColor(ChatFormatting.GOLD)
                        .withUnderlined(true)
                );
    }

    private static NodeParser getTextParser(List<String> options) {
        ParserBuilder builder = new ParserBuilder();
        for (String opt : options) {
            switch (opt) {
                case "legacyColor" -> builder.legacyColor();
                case "legacyAll" -> builder.legacyAll();
                case "simplified" -> builder.simplifiedTextFormat();
                case "markdown" -> builder.markdown();
                default -> throw new IllegalArgumentException("Unknown format options for text parser: " + opt);
            }
        }

        return builder.build();
    }
}
