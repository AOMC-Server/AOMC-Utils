package com.gentheowl.aomc_utils.renaming.utils;

import java.util.Map;
import java.util.Set;

public class ConfigSettings {
    public static final int DEFAULT_LORE_AMOUNT = 32;
    public static final int CONFIG_VERSION = 1;
    public static final boolean DEFAULT_USING_PERMISSIONS_API = false;

    public static final Set<String> ALLOWED_TEXT_FORMATS = Set.of("legacyColor", "legacyAll", "simplified", "markdown");

    public static final String MODIFY_NAME = "name";
    public static final String LORE_BASE = "lore";
    public static final String SET_LORE = "lore.set";
    public static final String PUSH_LORE = "lore.push";
    public static final String REMOVE_LORE = "lore.remove";
    public static final String INSERT_LORE = "lore.insert";
    public static final String GLINT = "glint";
    public static final String SIGN = "sign";
    public static final String UNSIGN = "unsign";
    public static final Set<String> ALLOWED_MODIFICATION_KEYS = Set.of(
            MODIFY_NAME,
            SET_LORE,
            PUSH_LORE,
            REMOVE_LORE,
            INSERT_LORE,
            GLINT,
            SIGN,
            UNSIGN
    );

    public record CommandEntry(boolean enabled, int cost) {}
    public static ConfigSettings.CommandEntry getDefaultCommandEntry(String key) {
        return COMMAND_DEFAULTS.getOrDefault(key, new CommandEntry(true, 1));
    }

    private static final Map<String, CommandEntry> COMMAND_DEFAULTS = Map.of(
            MODIFY_NAME, new CommandEntry(true, 1),
            SET_LORE, new CommandEntry(true, 1),
            PUSH_LORE, new CommandEntry(true, 1),
            REMOVE_LORE, new CommandEntry(true, 1),
            INSERT_LORE, new CommandEntry(true, 1),
            GLINT, new CommandEntry(true, 1),
            SIGN, new CommandEntry(true, 1),
            UNSIGN, new CommandEntry(true, 1)
    );
}
