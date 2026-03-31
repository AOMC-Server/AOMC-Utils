package com.gentheowl.aomc_utils.renaming;

import com.gentheowl.aomc_utils.renaming.utils.RenameitConfig;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.ParserBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.w3c.dom.Node;

import javax.swing.text.AttributeSet;

public class TextUtil {
    private static final RenameitConfig CONFIG = RenameitConfig.get();

    private static final NodeParser PARSER = ParserBuilder.of().markdown().simplifiedTextFormat().build();
    public static final Identifier UNFIFORM_FONT = Identifier.ofVanilla("uniform");

    public static Text error(String msg) {
        return Text.literal("Error: " + msg).formatted(Formatting.RED);
    }

    public static Text success(String msg) {
        return Text.literal(msg).formatted(Formatting.GREEN);
    }

    public static Text info(String msg) {
        return Text.literal(msg).formatted(Formatting.YELLOW);
    }

    public static Text parse(String input, ServerCommandSource src) {
        return PARSER.parseText(input, PlaceholderContext.of(src).asParserContext());
    }

    public static Text signature(String name) {
        return Text.literal(" Signed by " + name).setStyle(Style.EMPTY.withFont(UNFIFORM_FONT).withColor(Formatting.GRAY).withItalic(false));
    }

    public static Text header(String msg) {
        return Text.literal(msg)
                .styled(style -> style
                        .withColor(Formatting.GOLD)
                        .withUnderline(true)
                );
    }
}
