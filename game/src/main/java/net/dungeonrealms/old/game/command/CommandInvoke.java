package net.dungeonrealms.old.game.command;

import net.dungeonrealms.common.frontend.command.BaseCommand;
import net.dungeonrealms.common.old.game.database.player.rank.Rank;
import net.dungeonrealms.old.game.mechanic.DungeonManager;
import net.dungeonrealms.old.game.party.PartyMechanics;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nick on 10/20/2015.
 */
public class CommandInvoke extends BaseCommand {
    public CommandInvoke(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {

        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;
        if (!Rank.isGM(player)) {
            return false;
        }
        if (args.length > 0) {
            if (DungeonManager.getInstance().canCreateInstance()) {
                switch (args[0].toLowerCase()) {
                    case "bandittrove":
                    case "t1dungeon":
                        if (PartyMechanics.getInstance().isInParty(player)) {
                            Map<Player, Boolean> partyList = new HashMap<>();
                            for (Player player1 : PartyMechanics.getInstance().getParty(player).get().getMembers()) {
                                if (player1.getLocation().distanceSquared(player.getLocation()) <= 200) {
                                    partyList.put(player1, true);
                                } else {
                                    partyList.put(player1, false);
                                }
                            }
                            partyList.put(player, true);
                            DungeonManager.getInstance().createNewInstance(DungeonManager.DungeonType.BANDIT_TROVE, partyList, "T1Dungeon");
                        } else {
                            PartyMechanics.getInstance().createParty(player);
                            Map<Player, Boolean> partyList = new HashMap<>();
                            for (Player player1 : PartyMechanics.getInstance().getParty(player).get().getMembers()) {
                                if (player1.getLocation().distanceSquared(player.getLocation()) <= 200) {
                                    partyList.put(player1, true);
                                } else {
                                    partyList.put(player1, false);
                                }
                            }
                            partyList.put(player, true);
                            DungeonManager.getInstance().createNewInstance(DungeonManager.DungeonType.BANDIT_TROVE, partyList, "T1Dungeon");
                        }
                        break;
                    case "varenglade":
                    case "dodungeon":
                        if (PartyMechanics.getInstance().isInParty(player)) {
                            Map<Player, Boolean> partyList = new HashMap<>();
                            for (Player player1 : PartyMechanics.getInstance().getParty(player).get().getMembers()) {
                                if (player1.getLocation().distanceSquared(player.getLocation()) <= 200) {
                                    partyList.put(player1, true);
                                } else {
                                    partyList.put(player1, false);
                                }
                            }
                            partyList.put(player, true);
                            DungeonManager.getInstance().createNewInstance(DungeonManager.DungeonType.VARENGLADE, partyList, "DODungeon");
                        } else {
                            PartyMechanics.getInstance().createParty(player);
                            Map<Player, Boolean> partyList = new HashMap<>();
                            for (Player player1 : PartyMechanics.getInstance().getParty(player).get().getMembers()) {
                                if (player1.getLocation().distanceSquared(player.getLocation()) <= 200) {
                                    partyList.put(player1, true);
                                } else {
                                    partyList.put(player1, false);
                                }
                            }
                            partyList.put(player, true);
                            DungeonManager.getInstance().createNewInstance(DungeonManager.DungeonType.VARENGLADE, partyList, "DODungeon");
                        }
                        break;
                    case "infernal_abyss":
                    case "infernalabyss":
                    case "fireydungeon":
                        if (PartyMechanics.getInstance().isInParty(player)) {
                            Map<Player, Boolean> partyList = new HashMap<>();
                            for (Player player1 : PartyMechanics.getInstance().getParty(player).get().getMembers()) {
                                if (player1.getLocation().distanceSquared(player.getLocation()) <= 400) {
                                    partyList.put(player1, true);
                                } else {
                                    partyList.put(player1, false);
                                }
                            }
                            partyList.put(player, true);
                            DungeonManager.getInstance().createNewInstance(DungeonManager.DungeonType.THE_INFERNAL_ABYSS, partyList, "fireydungeon");
                        } else {
                            PartyMechanics.getInstance().createParty(player);
                            Map<Player, Boolean> partyList = new HashMap<>();
                            for (Player player1 : PartyMechanics.getInstance().getParty(player).get().getMembers()) {
                                if (player1.getLocation().distanceSquared(player.getLocation()) <= 400) {
                                    partyList.put(player1, true);
                                } else {
                                    partyList.put(player1, false);
                                }
                            }
                            partyList.put(player, true);
                            DungeonManager.getInstance().createNewInstance(DungeonManager.DungeonType.THE_INFERNAL_ABYSS, partyList, "fireydungeon");
                        }
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + "Unknown instance " + args[0] + "!");
                        break;
                }
            } else {
                player.sendMessage(ChatColor.RED + "There are no dungeons available at this time.");
            }
        }

        return false;
    }
}
