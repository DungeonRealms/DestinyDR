package net.dungeonrealms.game.command;

import com.google.common.collect.Lists;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.quests.QuestPlayerData;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandQStop extends BaseCommand {
    public CommandQStop() {
        super("qstop", "/queststop", "Stop active quests.", Lists.newArrayList("queststop", "stopquest"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        Player player = (Player) sender;
        QuestPlayerData data = Quests.getInstance().playerDataMap.get(player);
        if (data.getCurrentQuests() != null && data.getCurrentQuests().size() > 0) {
            Quest quest = data.getCurrentQuests().get(data.getCurrentQuests().size() - 1);

            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3, .5F);

            player.sendMessage("");
            Utils.sendCenteredMessage(player, CC.GreenB + "Quest Cancellation");
            Utils.sendCenteredMessage(player, CC.Gray + "Quest: " + CC.GreenB + quest.getQuestName());
            Utils.sendCenteredMessage(player, CC.Gray + "Enter " + CC.GreenB + "Y" + CC.Gray + " to " + CC.GreenU + "confirm" + CC.Gray + " or anything else to " + CC.RedU + "cancel.");
            player.sendMessage("");

            Chat.listenForMessage(player, e -> {
                player.sendMessage(ChatColor.RED + "Quest has been removed from your Active Quests!");
                if (data.isDoingQuest(quest)) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 4, .9F);
                }
                data.removeQuest(quest);
            }, cancel -> player.sendMessage(ChatColor.RED + "Quest not cancelled."));
            data.removeQuest(quest);
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have any currently active quests!");
        }
        return false;
    }
}
