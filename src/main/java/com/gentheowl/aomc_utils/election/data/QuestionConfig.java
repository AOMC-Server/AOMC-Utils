package com.gentheowl.aomc_utils.election;

import java.util.List;

public class QuestionConfig {
    String text;
    boolean condorcet;
    List<String> options;

    public QuestionConfig(String text, boolean condorcet, List<String> options) {
        this.text = text;
        this.condorcet = condorcet;
        this.options = options;
    }

    public String getText() { return text;}
    public boolean isCondorcet() { return condorcet; }
    public List<String> getOptions() { return options; }
}
