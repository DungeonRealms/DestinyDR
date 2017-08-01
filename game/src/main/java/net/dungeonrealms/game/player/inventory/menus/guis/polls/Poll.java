package net.dungeonrealms.game.player.inventory.menus.guis.polls;

import lombok.Getter;
import lombok.SneakyThrows;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;

import java.util.*;

/**
 * Created by Rar349 on 8/1/2017.
 */
public class Poll {

    private int pollID = -1;
    @Getter
    private String question;
    private Map<Integer, Integer> votes = new HashMap<>();
    public Map<Integer,PollOption> options = new HashMap<>();

    public Poll(int pollID, String question) {
        this.pollID = pollID;
        this.question = question;
    }

    public void addOption(PollOption option) {
        this.options.put(option.getOptionID(), option);
    }

    public void addAnswer(int accountID, int optionID) {
        if(!options.containsKey(optionID)) return;
        votes.put(accountID, optionID);
    }

    public int getNumberOfOptions() {
        return options.size();
    }

    public boolean hasVotes(int accountID) {
        return votes.containsKey(accountID);
    }

    public boolean hasVoted(int accountID) {
        return getVotedOption(accountID) != null;
    }

    public PollOption getVotedOption(int accountID) {
        Integer pollID = votes.get(accountID);
        if(pollID == null) return null;
        return getPollOption(pollID);
    }

    public PollOption getPollOption(int pollID) {
        return options.get(pollID);
    }

    public void populateOptions(Runnable callback) {
        SQLDatabaseAPI.getInstance().executeQuery(QueryType.SELECT_ALL_POLL_QUESTIONS.getQuery(pollID), (set) -> {
            try {
                while (set.next()) {
                    int optionID = set.getInt("option_id");
                    String answer = set.getString("answer");
                    PollOption option = new PollOption(optionID, answer);
                    addOption(option);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }

            if(callback != null)callback.run();
        });
    }

    public void populateAnswers(Runnable callback) {
        SQLDatabaseAPI.getInstance().executeQuery(QueryType.SELECT_ALL_POLL_ANSWERS.getQuery(pollID), (set) -> {
            try {
                while (set.next()) {
                    int accountID = set.getInt("account_id");
                    int optionID = set.getInt("option_id");
                    addAnswer(accountID, optionID);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }

            if(callback != null)callback.run();
        });
    }


}
