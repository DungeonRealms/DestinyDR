package net.dungeonrealms.game.command;

import com.google.common.collect.Lists;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.quests.QuestPlayerData;
import net.dungeonrealms.game.quests.Quests;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class CommandQObjective extends BaseCommand {
    public CommandQObjective() {
        super("qobjectives", "/<command>", "Objective command", Lists.newArrayList("questobjective", "qobjective", "quest"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        QuestPlayerData data = Quests.getInstance().playerDataMap.get(player);
        if (data != null) {
            int pageLength = 120;
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
            BookMeta im = (BookMeta) book.getItemMeta();

            StringBuilder builder = new StringBuilder();
//            String page = "";
            for (Quest doing : data.getCurrentQuests()) {
                QuestPlayerData.QuestProgress qp = data.getQuestProgress(doing);

                StringBuilder newText = new StringBuilder();
                newText.append(ChatColor.BLACK).append(doing.getQuestName()).append("> ").append(ChatColor.GREEN);
                if (qp.getCurrentStage().getPrevious() == null) {
                    newText.append("Start by talking to ").append(qp.getCurrentStage().getNPC().getName());
                } else {
                    newText.append(qp.getCurrentStage().getPrevious().getObjective().getTaskDescription(player, qp.getCurrentStage()));
                }
                newText.append("\n\n");

                String done = newText.toString();
                String wholePage = builder.toString();
                if (done.length() + wholePage.length() > pageLength) {
                    im.addPage(wholePage);
                    builder = new StringBuilder(done);
                } else {
                    builder.append(done);
                }
            }

            if (builder.length() > 0) {
                im.addPage(builder.toString());
            }

            book.setItemMeta(im);
            GameAPI.openBook(player, book);
            player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, .5F, 1.5F);
        } else {
            player.sendMessage(ChatColor.RED + "You do not have any currently active quests!");
        }
        return false;
    }
}
