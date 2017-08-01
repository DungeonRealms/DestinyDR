package net.dungeonrealms.game.player.inventory.menus.guis.polls;

import lombok.Getter;

/**
 * Created by Rar349 on 8/1/2017.
 */
@Getter
public class PollOption {

    private int optionID;
    private String answer;

    public PollOption(int optionID, String answer) {
        this.optionID = optionID;
        this.answer = answer;
    }
}
