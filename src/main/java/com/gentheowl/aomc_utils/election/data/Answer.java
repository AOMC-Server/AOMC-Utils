package com.gentheowl.aomc_utils.election.data;

import java.util.List;

public class Answer {
    String question;
    boolean skipped;
    boolean condorcet;
    Integer choice;
    List<Integer> ranking;

    public Answer(String question, boolean skipped, boolean condorcet, Integer choice, List<Integer> ranking) {
        this.question = question;
        this.skipped = skipped;
        this.condorcet = condorcet;
        this.choice = choice;
        this.ranking = ranking;
    }
}
