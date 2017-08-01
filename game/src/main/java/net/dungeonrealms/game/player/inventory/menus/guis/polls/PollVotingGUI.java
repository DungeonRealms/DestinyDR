package net.dungeonrealms.game.player.inventory.menus.guis.polls;

import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Created by Rar349 on 8/1/2017.
 */
public class PollVotingGUI extends GUIMenu {

    private Poll poll;
    public PollVotingGUI(Player player, Poll poll) {
        super(player, fitSize(poll.getNumberOfOptions()), poll.getQuestion());
        this.poll = poll;
    }

    @Override
    protected void setItems() {
        int index = 0;
        for(PollOption option : poll.options.values()) {
            setItem(index++, new GUIItem(Material.STAINED_GLASS_PANE, DyeColor.LIME.getDyeData()).setName(ChatColor.GREEN + ChatColor.BOLD.toString() + option.getAnswer()).setLore("",ChatColor.GRAY + "Click here to vote for this answer").setClick((evt) -> {
                PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                poll.addAnswer(wrapper.getAccountID(), option.getOptionID());
                player.sendMessage("Clicked the option: " + option.getAnswer());
                player.closeInventory();
                new PollSelectionGUI(player).open(player, evt.getAction());
            }));
        }
    }
}
