package com.gentheowl.aomc_utils.election;

import com.gentheowl.aomc_utils.AOMCUtils;
import com.gentheowl.aomc_utils.election.data.BallotConfig;
import com.gentheowl.aomc_utils.election.data.QuestionConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class VoteManager {
    private static BallotConfig ballot;
    private static final Map<UUID, VoteSession> sessions = new HashMap<>();
    public static final Map<UUID, Boolean> eligibility = new HashMap<>();
    private static boolean votingEnabled = true;

    // paths
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(AOMCUtils.MOD_ID);
    private static final Path BALLOT_FILE = CONFIG_DIR.resolve("ballot.json");
    public static final Path RESULTS_DIR = CONFIG_DIR.resolve("results");
    private static final Path ELIGIBILITY_FILE = CONFIG_DIR.resolve("eligibility.json");

    public static void loadEligibility() {
        try {
            if (!Files.exists(ELIGIBILITY_FILE)) {
                // create empty eligibility file
                Files.writeString(ELIGIBILITY_FILE, "{}");
            }
            Gson gson = new Gson();
            String json = Files.readString(ELIGIBILITY_FILE);
            Map<String, Boolean> map = gson.fromJson(json, Map.class);
            eligibility.clear();
            if (map != null) {
                for (var entry : map.entrySet()) {
                    try {
                        eligibility.put(UUID.fromString(entry.getKey()), entry.getValue());
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load eligibility file: " + e.getMessage());
        }
    }

    public static void loadConfig() {
        try {
            Files.createDirectories(CONFIG_DIR);
            Files.createDirectories(RESULTS_DIR);
        } catch (IOException e) {
            System.err.println("Failed to create config directory: " + e.getMessage());
        }

        if (!Files.exists(BALLOT_FILE)) {
            writeSampleConfig();
        }

        // read the ballot
        try (var reader = Files.newBufferedReader(BALLOT_FILE)) {
            Gson gson = new Gson();
            ballot = gson.fromJson(reader, BallotConfig.class);
            if (ballot == null || ballot.getQuestions().isEmpty()) {
                throw new RuntimeException("Ballot is empty or invalid");
            }
        } catch (IOException | JsonSyntaxException | JsonIOException e) {
            throw new RuntimeException("Failed to load ballot config: " + e.getMessage());
        }

        loadEligibility();
    }

    private static void writeSampleConfig() {
        BallotConfig sample = new BallotConfig();
        sample.questions = new ArrayList<>();
        // Example question (single-choice)
        sample.questions.add(new QuestionConfig(
                "What is your favorite color?",
                false,  // single choice
                Arrays.asList("Red", "Green", "Blue")
        ));
        // Example question (Condorcet ranking)
        sample.questions.add(new QuestionConfig(
                "Rank these animals by preference:",
                true,   // condorcet ranking
                Arrays.asList("Cat", "Dog", "Bird", "Fish")
        ));
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            Files.writeString(BALLOT_FILE, gson.toJson(sample));
        } catch (IOException e) {
            System.err.println("Failed to write sample ballot config: " + e.getMessage());
        }
    }

    public static void startSession(ServerPlayer player) {
        if (!votingEnabled) {
            player.displayClientMessage(Component.literal("Voting is currently disabled by an administrator.")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)), false);
            return;
        }

        UUID uuid = player.getUUID();

        // check eligibility
        if (!eligibility.getOrDefault(uuid, false)) {
            player.displayClientMessage(Component.literal("You are not eligible to vote (insufficient playtime).")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)), false);
            return;
        }

        // already in-progress
        if (sessions.containsKey(uuid)) {
            player.displayClientMessage(Component.literal("You are already voting or finished voting!")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)), false);
            return;
        }

        // already voted (results file exists) — prevent another vote
        Path resultFile = RESULTS_DIR.resolve(uuid + ".json");
        if (Files.exists(resultFile)) {
            player.displayClientMessage(Component.literal("You have already submitted your vote and cannot start another one.")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)), false);
            return;
        }

        VoteSession session = new VoteSession(player, ballot);
        sessions.put(uuid, session);
        session.sendNextQuestion();
    }

    private static boolean isSessionInvalid(ServerPlayer player, VoteSession session) {
        if (session == null) {
            player.displayClientMessage(Component.literal("You have no active vote session. Use /vote to start.").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)), false);
            return true;
        }
        if (session.isExpired()) {
            sessions.remove(player.getUUID());
            player.displayClientMessage(Component.literal("Vote session timed out. Use /vote to try again.").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)), false);
            return true;
        }

        return false;
    }

    public static void handleAnswer(ServerPlayer player, int choice) {
        UUID uuid = player.getUUID();
        VoteSession session = sessions.get(uuid);
        if (isSessionInvalid(player, session)) return;

        session.recordAnswer(choice);
        if (session.hasNextQuestion()) {
            session.sendNextQuestion();
        }
        else {
            session.finish();
            sessions.remove(uuid);
        }
    }

    public static void handleSkip(ServerPlayer player) {
        UUID uuid = player.getUUID();
        VoteSession session = sessions.get(uuid);
        if (isSessionInvalid(player, session)) return;

        session.skipQuestion();
        if (session.hasNextQuestion()) {
            session.sendNextQuestion();
        }
        else {
            session.finish();
            sessions.remove(uuid);
        }
    }

    public static void cancelSession(UUID playerUuid) {
        sessions.remove(playerUuid);
    }

    public static boolean isVotingEnabled() {
        return votingEnabled;
    }

    public static void setVotingEnabled(boolean votingEnabled) {
        VoteManager.votingEnabled = votingEnabled;
    }
}
