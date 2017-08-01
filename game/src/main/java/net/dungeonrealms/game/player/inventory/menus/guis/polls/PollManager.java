package net.dungeonrealms.game.player.inventory.menus.guis.polls;

import lombok.SneakyThrows;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.game.util.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Created by Rar349 on 8/1/2017.
 */
public class PollManager {

    public static ConcurrentHashMap<Integer, Poll> polls = new ConcurrentHashMap<>();

    public static void loadAllPolls(Consumer<Integer> callback) {
        SQLDatabaseAPI.getInstance().executeQuery(QueryType.SELECT_ALL_POLLS.getQuery(), (set) -> {
            try {
                while (set.next()) {
                    int pollID = set.getInt("poll_id");
                    String question = set.getString("question");
                    Poll poll = new Poll(pollID, question);
                    poll.populateOptions(() -> {
                        poll.populateAnswers(null);
                    });
                }
            } catch(Exception e) {
                e.printStackTrace();
            }

            if(callback != null) callback.accept(1);
        });
    }

    public static int getNumberOfPolls() {
        return polls.size();
    }

    public static void openPollBooth(Player player) {
        if(polls.isEmpty()) {
            player.sendMessage(ChatColor.RED + "There does not seem to be active polls to vote on!");
            return;
        }
        new PollSelectionGUI(player).open(player,null);
    }
}
