package com.gentheowl.aomc_utils.election;

import com.gentheowl.aomc_utils.AomcUtilsMod;
import com.gentheowl.aomc_utils.election.data.BallotConfig;
import com.gentheowl.aomc_utils.election.data.QuestionConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import parliament.api.entity.Player;
import parliament.api.text.Color;
import parliament.api.text.Message;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class VoteManager {
    private static BallotConfig ballot;
    private static final Map<UUID, VoteSession> sessions = new HashMap<>();
    public static final Map<UUID, Boolean> eligibility = new HashMap<>();
    private static boolean votingEnabled = true;

    private static Path configDir;
    private static Path ballotFile;
    public static Path RESULTS_DIR;
    private static Path eligibilityFile;

    public static void loadEligibility() {
        try {
            if (!Files.exists(eligibilityFile)) {
                // create empty eligibility file
                Files.writeString(eligibilityFile, "{}");
            }
            Gson gson = new Gson();
            String json = Files.readString(eligibilityFile);
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

    public static void loadConfig(Path globalConfigDir) {
        configDir = globalConfigDir.resolve(AomcUtilsMod.MOD_ID);
        ballotFile = configDir.resolve("ballot.json");
        RESULTS_DIR = configDir.resolve("results");
        eligibilityFile = configDir.resolve("eligibility.json");

        try {
            Files.createDirectories(configDir);
            Files.createDirectories(RESULTS_DIR);
        } catch (IOException e) {
            System.err.println("Failed to create config directory: " + e.getMessage());
        }

        if (!Files.exists(ballotFile)) {
            writeSampleConfig();
        }

        // read the ballot
        try (var reader = Files.newBufferedReader(ballotFile)) {
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
            Files.writeString(ballotFile, gson.toJson(sample));
        } catch (IOException e) {
            System.err.println("Failed to write sample ballot config: " + e.getMessage());
        }
    }

    public static void startSession(Player player) {
        if (!votingEnabled) {
            player.sendMessage(Message.literal("Voting is currently disabled by an administrator.")
                    .color(Color.RED));
            return;
        }

        UUID uuid = player.uuid();

        // check eligibility
        if (!eligibility.getOrDefault(uuid, false)) {
            player.sendMessage(Message.literal("You are not eligible to vote (insufficient playtime).")
                    .color(Color.RED));
            return;
        }

        // already in-progress
        if (sessions.containsKey(uuid)) {
            player.sendMessage(Message.literal("You are already voting or finished voting!")
                    .color(Color.RED));
            return;
        }

        // already voted (results file exists) — prevent another vote
        if (hasVoted(uuid)) {
            player.sendMessage(Message.literal("You have already submitted your vote and cannot start another one.")
                    .color(Color.RED));
            return;
        }

        VoteSession session = new VoteSession(player, ballot);
        sessions.put(uuid, session);
        session.sendNextQuestion();
    }

    private static boolean isSessionInvalid(Player player, VoteSession session) {
        if (session == null) {
            player.sendMessage(Message.literal("You have no active vote session. Use /vote to start.").color(Color.RED));
            return true;
        }
        if (session.isExpired()) {
            sessions.remove(player.uuid());
            player.sendMessage(Message.literal("Vote session timed out. Use /vote to try again.").color(Color.RED));
            return true;
        }

        return false;
    }

    public static void handleAnswer(Player player, int choice) {
        UUID uuid = player.uuid();
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

    public static void handleSkip(Player player) {
        UUID uuid = player.uuid();
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

    public static boolean hasVoted(UUID uuid) {
        return Files.exists(RESULTS_DIR.resolve(uuid + ".json"));
    }

    public static boolean isVotingEnabled() {
        return votingEnabled;
    }

    public static void setVotingEnabled(boolean votingEnabled) {
        VoteManager.votingEnabled = votingEnabled;
    }
}
