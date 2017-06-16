package net.dungeonrealms.game.command.dungeon;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.affair.party.Party;
import net.dungeonrealms.game.item.items.functional.accessories.Trinket;
import net.dungeonrealms.game.mechanic.dungeons.rifts.EliteRift;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Kieran Quigley (Proxying) on 20-Jun-16.
 */
public class DungeonJoin extends BaseCommand {
    public DungeonJoin() {
        super("djoin", "/<command>", "Dungeon Join command");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;

        if (!GameAPI.isMainWorld(player)) {
            player.sendMessage(ChatColor.RED + "Dungeons can only be joined from Andalucia.");
            return true;
        }

        if (!Affair.isInParty(player)) {
            player.sendMessage(ChatColor.RED + "You must be in a party to use this command.");
            return true;
        }

        Party p = Affair.getParty(player);
        if (!p.isDungeon()) {
            player.sendMessage(ChatColor.RED + "Your party is not in a dungeon.");
            return true;
        }

        if (p.getDungeon() != null && p.getDungeon() instanceof EliteRift) {
            player.sendMessage(ChatColor.RED + "Your party is not in a dungeon.");
            return true;
        }

        if (!GameAPI.isInSafeRegion(player.getLocation())) {
            player.sendMessage(ChatColor.RED + "You cannot join a dungeon from this location.");
            return true;
        }

//        if (!p.getDungeon().getAllowedPlayers().contains(player)) {
//            player.sendMessage(ChatColor.RED + "You cannot join a dungeon you did not start.");
//            return true;
//        }

        if (!p.getDungeon().getStartingMembers().contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You cannot join the dungeon if you were not in the dungeon when it started!");
            return true;
        }

        p.announce(player.getName() + " has re-entered the dungeon.");

        if (Trinket.hasActiveTrinket(player, Trinket.DUNGEON_TELEPORT, true) &&
                p.getDungeon().getBoss() != null && p.getDungeon().hasSpawned(p.getDungeon().getBoss().getBossType())) {
            Player teleportTp = p.getOwner() != null && p.getOwner().getWorld().equals(p.getDungeon().getWorld()) ? p.getOwner() : p.getAllMembers().stream().filter(pl -> !pl.equals(player) && pl.getWorld().equals(p.getDungeon().getWorld())).findFirst().orElse(null);
            if (teleportTp != null) {
                player.teleport(teleportTp);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1.4F);
            } else {
                player.teleport(p.getDungeon().getWorld().getSpawnLocation());
                player.sendMessage(ChatColor.RED + "Unable to find a Party Member to teleport to.");
            }
        } else {
            player.teleport(p.getDungeon().getWorld().getSpawnLocation());
        }
        player.setFallDistance(0F);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 3, 1));
        return true;
    }
}
