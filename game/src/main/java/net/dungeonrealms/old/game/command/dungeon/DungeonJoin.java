package net.dungeonrealms.old.game.command.dungeon;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.frontend.command.BaseCommand;
import net.dungeonrealms.old.game.mechanic.DungeonManager;
import net.dungeonrealms.old.game.party.Party;
import net.dungeonrealms.old.game.party.PartyMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Kieran Quigley (Proxying) on 20-Jun-16.
 */
public class DungeonJoin extends BaseCommand {
    public DungeonJoin(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        if (args.length != 0) {
            return true;
        }

        Player player = (Player) sender;
        if (player.getWorld().equals(Bukkit.getWorlds().get(0))) {
            if (PartyMechanics.getInstance().isInParty(player)) {
                Party party = PartyMechanics.getInstance().getParty(player).get();
                boolean partyInDungeon = false;
                DungeonManager.DungeonObject dungeonObject = null;
                if (party.getOwner().getWorld().getName().contains("DUNGEON")) {
                    partyInDungeon = true;
                    dungeonObject = DungeonManager.getInstance().getDungeon(party.getOwner().getWorld());
                }
                if (!partyInDungeon) {
                    for (Player player1 : party.getMembers()) {
                        if (player1.getWorld().getName().contains("DUNGEON")) {
                            partyInDungeon = true;
                            dungeonObject = DungeonManager.getInstance().getDungeon(party.getOwner().getWorld());
                            break;
                        }
                    }
                }
                if (dungeonObject == null || !partyInDungeon) {
                    player.sendMessage(ChatColor.RED + "Your party are not in a Dungeon.");
                    return true;
                }
                if (dungeonObject.getPlayerList().containsKey(player) || dungeonObject.getTime() < 600) {
                    dungeonObject.getPlayerList().put(player, true);
                    DungeonManager.getInstance().getPlayers_Entering_Dungeon().put(player.getName(), 100);
                    if (GameAPI.isInSafeRegion(player.getLocation())) {
                        boolean hasTeleported = false;
                        DungeonManager.getInstance().getPlayers_Entering_Dungeon().put(player.getName(), 100);
                        for (Player player1 : dungeonObject.getPlayerList().keySet()) {
                            if (player.getName().equals(player1.getName())) {
                                continue;
                            }
                            if (player1.getWorld().getName().contains("DUNGEON")) {
                                player1.sendMessage(ChatColor.LIGHT_PURPLE + "<" + ChatColor.BOLD + "P" + ChatColor.LIGHT_PURPLE + ">" + ChatColor.GRAY
                                        + " " + player.getName() + " has " + ChatColor.GREEN + ChatColor.UNDERLINE + "joined" + ChatColor.GRAY
                                        + " the dungeon.");
                                if (!hasTeleported) {
                                    player.teleport(player1.getLocation());
                                    player.setFallDistance(0F);
                                    if (dungeonObject.getType() == DungeonManager.DungeonType.THE_INFERNAL_ABYSS) {
                                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                                            DungeonManager.getInstance().sendWorldEnvironment(player, World.Environment.NETHER);
                                        }, 5L);
                                    }
                                    hasTeleported = true;
                                }
                            }
                        }
                        return true;
                    } else {
                        player.sendMessage(ChatColor.RED + "You cannot join the Dungeon from this location!");
                        return true;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "This Dungeon was created before you joined the party, you cannot join this session.");
                    return true;
                }

            } else {
                player.sendMessage(ChatColor.RED + "You are not in a party, so cannot join their Dungeon.");
                return true;
            }
        }

        return true;
    }
}
