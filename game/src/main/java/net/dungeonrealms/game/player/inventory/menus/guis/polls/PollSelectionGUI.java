package net.dungeonrealms.game.player.inventory.menus.guis.polls;

import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rar349 on 8/1/2017.
 */
public class PollSelectionGUI extends GUIMenu {
    public PollSelectionGUI(Player player) {
        super(player, fitSize(PollManager.getNumberOfPolls()), "Dungeon Realms Poll Booth");
    }

    @Override
    protected void setItems() {
        int index = 0;
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        for(Poll poll : PollManager.polls.values()) {
            boolean hasVoted = poll.hasVoted(wrapper.getAccountID());
            List<String> lore = new ArrayList<>();
            lore.add("");
            if(hasVoted) {
                lore.add(ChatColor.GRAY + "VOTED");
            } else lore.add(ChatColor.GRAY + "Click here to vote for this poll!");

            setItem(index++, new GUIItem(Material.STAINED_GLASS_PANE, DyeColor.LIME.getDyeData()).setName(ChatColor.GREEN + ChatColor.BOLD.toString() + poll.getQuestion()).setLore(lore).setClick((evt) -> {
                if(hasVoted) {
                    player.sendMessage(ChatColor.RED + "You have already voted on this poll!");
                    return;
                }
                player.closeInventory();
                new PollVotingGUI(player, poll).open(player, evt.getAction());
            }));
        }
    }
}
