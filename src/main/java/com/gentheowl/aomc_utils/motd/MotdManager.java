package com.gentheowl.aomc_utils.motd;

import parliament.api.server.Server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

public class MotdManager {
    private static List<String> motdList;
    private static final Random random = new Random();

    public static void initialize(Server server, Path configDirectory) {
        loadMotdList(configDirectory.resolve("motd_list.txt"));
        updateMotd(server);
    }

    private static void loadMotdList(Path configPath) {
        try {
            Files.createDirectories(configPath.getParent());
            if (!Files.exists(configPath)) {
                Files.writeString(configPath, "default_motd");
            }
            motdList = Files.readAllLines(configPath).stream().map(String::trim).filter(line -> !line.isEmpty())
                    .map(line -> line.replace("\\u00A7", "§")).toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config. Aborting.", e);
        }
    }

    private static void updateMotd(Server server) {
        if (motdList.isEmpty()) return;
        String selected = motdList.get(random.nextInt(motdList.size()));
        server.setMessageOfTheDay(selected);
    }
}
