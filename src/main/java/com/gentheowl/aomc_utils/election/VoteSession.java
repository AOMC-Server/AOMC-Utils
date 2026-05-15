package com.gentheowl.aomc_utils.election;

import com.gentheowl.aomc_utils.election.data.Answer;
import com.gentheowl.aomc_utils.election.data.BallotConfig;
import com.gentheowl.aomc_utils.election.data.QuestionConfig;
import com.gentheowl.aomc_utils.election.data.VoteResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

public class VoteSession {
    private static final int TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes

    private final ServerPlayer player;
    private final List<QuestionConfig> questions;
    private int currentIndex = 0;
    private final long startTime;
    private final List<Answer> answers = new ArrayList<>();
    private List<Integer> currentRanking = null;

    public VoteSession(ServerPlayer player, BallotConfig ballot) {
        this.player = player;
        this.questions = ballot.getQuestions();
        this.startTime = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - startTime > VoteSession.TIMEOUT_MS;
    }

    public void sendNextQuestion () {
        QuestionConfig question = questions.get(currentIndex);
        boolean condorcet = question.isCondorcet();

        // initialize ranking collector for condorcet questions
        if (condorcet && currentRanking == null) currentRanking = new ArrayList<>();

        Component message = Component.literal("Item " + (currentIndex+1) + ": " + question.getText() + (condorcet ? " (Click options in the order you prefer to rank them)" : ""))
                .setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA));
        player.displayClientMessage(message, false);

        // list options
        int optionCount = question.getOptions().size();
        for (int i = 0; i < optionCount; i++) {
            String optionText = question.getOptions().get(i);
            int displayIndex = i + 1;
            MutableComponent optionLine = Component.literal("  [" + displayIndex + "] " + optionText);

            if (condorcet) {
                int rankPos = currentRanking.indexOf(i);
                if (rankPos >= 0) {
                    // already chosen — show chosen rank and don't attach click event
                    optionLine = optionLine.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))
                            .append(Component.literal(" (Chosen as #" + (rankPos + 1) + ")"));
                } else {
                    // not yet chosen — clickable to pick next rank
                    optionLine = optionLine.setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)
                                    .withClickEvent(new ClickEvent.RunCommand("/vote answer " + displayIndex)))
                            .append(Component.literal(" (Click to pick next)"));
                }
            } else {
                // single-choice behavior as before
                optionLine = optionLine.setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)
                                .withClickEvent(new ClickEvent.RunCommand("/vote answer " + displayIndex)))
                        .append(Component.literal(" (Click to choose)"));
            }

            player.displayClientMessage(optionLine, false);
        }

        // show skip option (works for both types; skips whole question)
        Component skipLine = Component.literal("  [S] Skip")
                .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)
                        .withClickEvent(new ClickEvent.RunCommand("/vote skip")))
                .append(Component.literal(" (Click to skip question)"));
        player.displayClientMessage(skipLine, false);

        // condorcet progress message
        if (condorcet) {
            player.displayClientMessage(Component.literal("Progress: " + currentRanking.size() + " / " + optionCount + " picked.")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)), false);
        }
    }

    public void skipQuestion() {
        QuestionConfig question = questions.get(currentIndex);
        // clear any partial condorcet ranking when skipping
        currentRanking = null;
        answers.add(new Answer(question.getText(), true, question.isCondorcet(), null, null));
        currentIndex++;
    }

    public boolean hasNextQuestion() {
        return currentIndex < questions.size();
    }

    public void recordAnswer(int choice) {
        QuestionConfig question = questions.get(currentIndex);
        int optionCount = question.getOptions().size();
        int idx = choice - 1;

        // validate choice index
        if (idx < 0 || idx >= optionCount) {
            player.displayClientMessage(Component.literal("Invalid choice number.").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)), false);
            return;
        }

        if (question.isCondorcet()) {
            // ensure collector exists
            if (currentRanking == null) currentRanking = new ArrayList<>();

            // prevent picking the same option twice
            if (currentRanking.contains(idx)) {
                player.displayClientMessage(Component.literal("That option is already chosen — pick a different one.").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)), false);
                return;
            }

            // add picked option as the next rank
            currentRanking.add(idx);
            player.displayClientMessage(Component.literal("Picked \"" + question.getOptions().get(idx) + "\" as #" + currentRanking.size()).setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)), false);

            // if we've picked all options, finalize the ranking and advance
            if (currentRanking.size() == optionCount) {
                // store a copy of the ranking (0-based indices)
                List<Integer> finalRanking = new ArrayList<>(currentRanking);
                answers.add(new Answer(question.getText(), false, true, null, finalRanking));
                // clear collector and move to next question
                currentRanking = null;
                currentIndex++;
            }
        }
        else {
            // single-choice behavior (unchanged)
            answers.add(new Answer(question.getText(), false, question.isCondorcet(), choice-1, null));
            currentIndex++;
        }
    }

    public void finish() {
        player.displayClientMessage(Component.literal("Thank you for voting! Your responses have been recorded")
                .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)), false);
        saveResults();
    }

    private void saveResults() {
        VoteResult result = new VoteResult(player.getName().getString(), answers);
        Path resultFile = VoteManager.RESULTS_DIR.resolve(player.getUUID() + ".json");
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.writeString(resultFile, gson.toJson(result));
        } catch (IOException e) {
            System.err.println("Failed to save vote results for " + player.getName().getString() + ": " + e.getMessage());
        }
    }
}
