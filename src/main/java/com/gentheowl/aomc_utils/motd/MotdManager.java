package com.gentheowl.aomc_utils.motd;

import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

public class MotdManager {
    private static final Path CONFIG_PATH = Path.of("config", "motd_list.txt");
    private static List<String> motdList;
    private static final Random random = new Random();

    public static void initialize(MinecraftServer server) {
        loadMotdList();
        updateMotd(server);
    }

    private static void loadMotdList() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            if (!Files.exists(CONFIG_PATH)) {
                Files.writeString(CONFIG_PATH, "default_motd");
            }
            motdList = Files.readAllLines(CONFIG_PATH).stream().map(String::trim).filter(line -> !line.isEmpty())
                    .map(line -> line.replace("\\u00A7", "§")).toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config. Aborting.", e);
        }
    }

    private static void updateMotd(MinecraftServer server) {
        if (motdList.isEmpty()) return;
        String selected = motdList.get(random.nextInt(motdList.size()));
        server.setMotd(selected);
    }
}
