package com.gentheowl.aomc_utils.election.data;

import java.util.List;

public class VoteResult {
    String player;
    List<Answer> answers;

    public VoteResult(String player, List<Answer> answers) {
        this.player = player;
        this.answers = answers;
    }
}
