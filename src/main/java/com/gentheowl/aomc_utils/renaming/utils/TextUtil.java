package com.gentheowl.aomc_utils.renaming.utils;

import java.util.List;
import parliament.api.text.Color;
import parliament.api.text.Format;
import parliament.api.text.Message;

public class TextUtil {
    public static final String UNIFORM_FONT = "minecraft:uniform";

    public static Message error(String msg) {
        return Message.literal("Error: " + msg).color(Color.RED);
    }

    public static Message success(String msg) {
        return Message.literal(msg).color(Color.GREEN);
    }

    public static Message info(String msg) {
        return Message.literal(msg).color(Color.YELLOW);
    }

    public static Message parse(String input) {
        return Message.userFormatted(input, configuredFormats());
    }

    public static Message signature(String name) {
        return Message.literal(" Signed by " + name)
                .font(UNIFORM_FONT)
                .color(Color.GRAY)
                .italic(false);
    }

    public static Message header(String msg) {
        return Message.literal(msg)
                .color(Color.GOLD)
                .underlined(true);
    }

    private static Format[] configuredFormats() {
        List<String> options = RenameitConfig.get().format_options();
        Format[] formats = new Format[options.size()];
        for (int i = 0; i < options.size(); i++) {
            formats[i] = switch (options.get(i)) {
                case "legacyColor" -> Format.LEGACY_COLOR;
                case "legacyAll" -> Format.LEGACY_ALL;
                case "simplified" -> Format.SIMPLIFIED;
                case "markdown" -> Format.MARKDOWN;
                default -> throw new IllegalArgumentException("Unknown format options for text parser: " + options.get(i));
            };
        }
        return formats;
    }
}
