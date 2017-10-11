package net.dungeonrealms.game.command;

import com.google.common.collect.Lists;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.mechanic.CutSceneMechanic;
import net.dungeonrealms.game.mechanic.cutscenes.CutScene;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandCutscene extends BaseCommand {
    public CommandCutscene() {
        super("cutscene", "/cutscene", "", Lists.newArrayList("cs"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (!Rank.isGM(player)) {
            sender.sendMessage(ChatColor.RED + "Unknown command. View your Character Journal for a list of commands.");
            return true;
        }

        CutSceneMechanic mechanic = CutSceneMechanic.get();
        if (args.length >= 7) {
            if (args[0].equalsIgnoreCase("create")) {
                String name = args[1];

                try {
                    int tickSpeed = Integer.parseInt(args[3]);
                    if (tickSpeed < 1) tickSpeed = 1;
                    double time = Double.parseDouble(args[2]);

                    int titleTicks = Integer.parseInt(args[4]);
                    String title = args[5];

                    String desc = "";
                    for(int i = 6; i < args.length; i++){
                        desc += args[i] + " ";
                    }
                    desc = desc.trim();

                    CutScene scene = new CutScene(name, title, desc, time, System.currentTimeMillis(), tickSpeed, titleTicks, Lists.newArrayList());
                    mechanic.getCreatingCutscene().put(player.getUniqueId(), scene);
                    player.sendMessage(ChatColor.GREEN + "Cutscene started! It will end in " + time + " seconds. All movement will be recorded and saved.");
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Please enter how long you'd wish the Cutscene to last in seconds.");
                }
            }

        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("begin")) {
                String name = args[1];

                CutScene found = mechanic.getCutScenes().get(name.toLowerCase());
                if (found == null) {
                    sender.sendMessage(ChatColor.RED + "Cutscene not found.");
                }

                mechanic.startCutscene(player, found);
                return true;
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                mechanic.getCutScenes().values().forEach((sc) -> player.sendMessage(sc.getName() + " (" + sc.getLocations().size() + ") Locations"));
                return true;
            }
        }
        sender.sendMessage("/custscene create <name> <lengthInSeconds> <tickSpeed> <titleTimeInTicks> <area_title> <Description>");
        sender.sendMessage(CC.Gray + "Enter -1 for <titleTimeInTicks> to disable titles.");
        return false;
    }
}
