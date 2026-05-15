package com.gentheowl.aomc_utils.renaming.utils;

import com.gentheowl.aomc_utils.AOMCUtils;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.component.ItemLore;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RenameitConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(AOMCUtils.MOD_ID);
    private static final File CONFIG_FILE = new File(CONFIG_PATH.toFile(), "renameit.json");

    private int max_lore_lines;
    private List<String> format_options;
    private final Map<String, ConfigSettings.CommandEntry> command_data;
    private boolean use_permissions_api;

    private static RenameitConfig INSTANCE;
    public static RenameitConfig get() {
        if (INSTANCE == null) {
            INSTANCE = createAndLoad();
        }
        return INSTANCE;
    }

    public static void reload() {
        INSTANCE = createAndLoad();
    }

    public RenameitConfig(int max_lore_lines, List<String> format_options, Map<String, ConfigSettings.CommandEntry> command_data, boolean use_permissions_api) {
        this.max_lore_lines = max_lore_lines;
        this.format_options = format_options;
        this.command_data = command_data;
        this.use_permissions_api = use_permissions_api;
    }

    public int max_lore_lines() { return max_lore_lines; }
    public List<String> format_options() { return format_options; }
    public Map<String, ConfigSettings.CommandEntry> command_data() {return command_data; }

    public void toggleCommand(String key, boolean enabled) {
        if (!command_data.containsKey(key)) return;
        command_data.computeIfPresent(key, (k, oldEntry) -> new ConfigSettings.CommandEntry(enabled, oldEntry.cost()));
        this.save();
    }

    public void setCost(String key, int cost) {
        if (!command_data.containsKey(key)) return;
        command_data.computeIfPresent(key, (k, oldEntry) -> new ConfigSettings.CommandEntry(oldEntry.enabled(), cost));
        this.save();
    }

    public void setMaxLoreLines(int amount) {
        this.max_lore_lines = amount;
        this.save();
    }

    public void setFormatOptions(List<String> options) {
        this.format_options = options;
        this.save();
    }

    public boolean shouldUsePermissionsAPI() {
        return this.use_permissions_api;
    }

    public void setPermissionAPIenabled(boolean value) {
        this.use_permissions_api = value;
        this.save();
    }

    /**
     * Creates and loads the configuration from the file system.
     * If the config file doesn't exist, it creates a default one.
     * If the loaded config is invalid, it falls back to default values for the invalid parts.
     *
     * @return An instance of RenameitConfig.
     */
    public static RenameitConfig createAndLoad() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                // Define the type for deserialization
                Type configType = new TypeToken<RenameitConfig>() {}.getType();
                RenameitConfig config = GSON.fromJson(reader, configType);

                // It's possible for GSON to create a null object if the json is empty
                if (config == null) {
                    return createDefaultConfig();
                }

                return validateAndFix(config);
            } catch (IOException | JsonSyntaxException e) {
                AOMCUtils.LOGGER.error("Failed to read or parse config file, creating a default one.", e);
                return createDefaultConfig();
            }
        } else {
            return createDefaultConfig();
        }
    }

    /**
     * Saves the current configuration instance to the JSON file.
     */
    public void save() {
        try {
            // Ensure the parent directory exists
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }
            // Write the config to the file
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            AOMCUtils.LOGGER.error("Could not save config file.", e);
        }
    }

    /**
     * Validates the loaded configuration and applies fallbacks for missing or invalid values.
     *
     * @param loadedConfig The configuration object loaded from the JSON file.
     * @return A validated and potentially corrected RenameitConfig instance.
     */
    private static RenameitConfig validateAndFix(RenameitConfig loadedConfig) {
        // validate permissions api usage
        boolean usingAPI = loadedConfig.shouldUsePermissionsAPI();

        // Validate max_lore_lines
        int validMaxLoreLines = (loadedConfig.max_lore_lines() > 0 && loadedConfig.max_lore_lines <= ItemLore.MAX_LINES)
                ? loadedConfig.max_lore_lines()
                : ConfigSettings.DEFAULT_LORE_AMOUNT;

        // Validate format_options
        List<String> validFormatOptions;
        if (loadedConfig.format_options() != null) {
            validFormatOptions = loadedConfig.format_options().stream()
                    .filter(ConfigSettings.ALLOWED_TEXT_FORMATS::contains)
                    .collect(Collectors.toList());
            if (validFormatOptions.isEmpty()) {
                validFormatOptions.add("simplified");
                validFormatOptions.add("markdown");
            }
        } else {
            validFormatOptions = List.of("simplified", "markdown");
        }


        // Validate command_data
        Map<String, ConfigSettings.CommandEntry> validCommandData = new HashMap<>();
        Map<String, ConfigSettings.CommandEntry> loadedCommandData = loadedConfig.command_data() != null ? loadedConfig.command_data() : new HashMap<>();

        for (String key : ConfigSettings.ALLOWED_MODIFICATION_KEYS) {
            ConfigSettings.CommandEntry entry = loadedCommandData.get(key);
            if (entry != null && entry.cost() >= 0) {
                validCommandData.put(key, entry);
            } else {
                // Add default entry if missing or invalid
                validCommandData.put(key, ConfigSettings.getDefaultCommandEntry(key));
            }
        }

        RenameitConfig newConfig = new RenameitConfig(validMaxLoreLines, validFormatOptions, validCommandData, usingAPI);

        // Save the corrected config back to the file
        newConfig.save();
        return newConfig;
    }

    /**
     * Creates a default configuration object and saves it to the file.
     *
     * @return The default RenameitConfig instance.
     */
    private static RenameitConfig createDefaultConfig() {
        AOMCUtils.LOGGER.info("Creating default config file.");
        Map<String, ConfigSettings.CommandEntry> defaultCommandData = new HashMap<>();
        for (String key : ConfigSettings.ALLOWED_MODIFICATION_KEYS) {
            defaultCommandData.put(key, ConfigSettings.getDefaultCommandEntry(key));
        }

        RenameitConfig defaultConfig = new RenameitConfig(
                ConfigSettings.DEFAULT_LORE_AMOUNT,
                List.of("simplified", "markdown"),
                defaultCommandData,
                ConfigSettings.DEFAULT_USING_PERMISSIONS_API
        );
        defaultConfig.save();
        return defaultConfig;
    }

}
