package net.dungeonrealms.game.affair;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;
import net.dungeonrealms.game.affair.party.Party;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.player.json.JSONMessage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Created by Nick on 11/9/2015.
 */
public class Affair implements GenericMechanic {

	@Getter private static Affair instance = new Affair();
	@Getter private static List<UUID> partyChat = new ArrayList<>();
	@Getter private static List<Party> parties = new CopyOnWriteArrayList<>();
	@Getter private static Map<Player, Party> invitations = new ConcurrentHashMap<>();

	@Override
	public EnumPriority startPriority() {
		return EnumPriority.CATHOLICS;
	}

	@Override
	public void startInitialization() {

		// Update the party scoreboard.
		Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), () -> getParties().forEach(Party::updateScoreboard), 0, 20L);
	}

    public void sendPartyChat(Player player, String message) {
    	if (!isInParty(player)) {
    		player.sendMessage(ChatColor.RED + "You are not in a party.");
    		return;
    	}

    	getParty(player).sendChat(player, message);
    }

    public static boolean isPartyChat(Player player) {
    	return getPartyChat().contains(player.getUniqueId());
    }

    public static void togglePartyChat(Player player) {
        if (isPartyChat(player)) {
            getPartyChat().remove(player.getUniqueId());
            player.sendMessage(ChatColor.GRAY + "Messages will now be default sent to local chat.");
        } else {
            getPartyChat().add(player.getUniqueId());
            player.sendMessage(ChatColor.DARK_AQUA + "Messages will now be default sent to party chat. Type " + ChatColor.UNDERLINE + "/l <msg>" + ChatColor.DARK_AQUA + " to speak in local.");
            player.sendMessage(ChatColor.GRAY + "To change back to default local, type " + ChatColor.BOLD + "/p" + ChatColor.GRAY + " again.");
        }
    }

    public static boolean areInSameParty(Player player1, Player player2) {
        return isInParty(player1) && isInParty(player2) && getParty(player1) == getParty(player2);
    }

    public static Party getParty(Player player) {
    	for (Party party : getParties())
    		if (party.isMember(player))
    			return party;
    	return null;
    }

    public static boolean isInParty(Player p) {
    	return getParty(p) != null;
    }

    public static void createParty(Player player) {
    	if (DungeonRealms.isEvent()) {
    		player.sendMessage(ChatColor.RED + "You may not create a party on this shard.");
    		return;
    	}

    	getParties().add(new Party(player));
        player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Your party has been created!");
        player.sendMessage(ChatColor.GRAY + "To invite more people to join your party, " + ChatColor.UNDERLINE
        		+ "Left Click" + ChatColor.GRAY + " them with your character journal or use " + ChatColor.BOLD + "/pinvite"
        		+ ChatColor.GRAY + ". To kick, use " + ChatColor.BOLD + "/pkick" + ChatColor.GRAY
        		+ ". To chat with party, use " + ChatColor.BOLD + "/p" + ChatColor.GRAY + ". To change the loot profile, use "
        		+ ChatColor.BOLD + "/ploot");

        Achievements.giveAchievement(player, EnumAchievements.PARTY_MAKER);
    }

    public void handlePartyPickup(PlayerPickupItemEvent event, Party party) {
        ItemStack item = event.getItem().getItemStack();
        //If its gone or something dont pls.
        if (!event.getItem().isValid()) return;

        if (!ItemArmor.isArmor(item) && !ItemWeapon.isWeapon(item)) return;

        int blocks = 40;

        int radius = blocks * blocks;
        Player player = event.getPlayer();
        switch (party.getLootMode()) {
            case KEEP:
                return;
            case LEADER:
                if (party.getOwner() != null && party.getOwner().isOnline() && !party.getOwner().getName().equals(player.getName())) {

                    if (party.getOwner().getWorld() != player.getWorld() || party.getOwner().getLocation().distanceSquared(player.getLocation()) > radius)
                        return;

                    //Send item to leader..

                    if (party.getOwner().getInventory().firstEmpty() == -1) {
                        //FULL INVENTORY!!!!! Let them keep it...
                        return;
                    }

                    event.setCancelled(true);
                    event.getItem().remove();
                    //PLay a noise to indicate what happened.
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1.3F);
                    party.getOwner().getWorld().playSound(party.getOwner().getLocation(), Sound.ENTITY_ITEM_PICKUP, 3, 1.1F);
                    party.getOwner().getInventory().addItem(item);
                }
                break;
            case RANDOM:
                if (party.getMembers().size() > 0 && party.getOwner() != null) {
                    List<Player> allMembers = party.getAllMembers().stream().filter((mem) -> mem.getWorld().equals(player.getWorld()) && mem.getLocation().distanceSquared(player.getLocation()) <= radius).collect(Collectors.toList());

                    //Only us in the list, dont overwrite?
                    if (allMembers.size() == 0 || (allMembers.size() == 1 && allMembers.get(0).getName().equals(player.getName())))
                        return;

                    Player random = allMembers.get(ThreadLocalRandom.current().nextInt(allMembers.size()));

                    if (random.isOnline()) {
                        //We won the roll, dont mess with the item
                        if (random.getName().equals(player.getName())) return;

                        if (random.getInventory().firstEmpty() == -1) {
                            //You do not have the inventory space
                            JSONMessage message = new JSONMessage(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "<P> " + ChatColor.GRAY + "Your inventory is too full to ", ChatColor.GRAY);

                            List<String> hoveredChat = new ArrayList<>();
                            ItemMeta meta = item.getItemMeta();
                            hoveredChat.add((meta.hasDisplayName() ? meta.getDisplayName() : item.getType().name()));
                            if (meta.hasLore())
                                hoveredChat.addAll(meta.getLore());

                            message.addHoverText(hoveredChat, ChatColor.GRAY + "received " + ChatColor.WHITE + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + "SHOW");
                            message.addText(ChatColor.GRAY + " picked up by " + ChatColor.LIGHT_PURPLE + player.getName(), ChatColor.GRAY);
                            message.sendToPlayer(random);
                            return;
                        }

                        event.setCancelled(true);
                        event.getItem().remove();
                        random.getInventory().addItem(item);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1.3F);
                        random.getWorld().playSound(random.getLocation(), Sound.ENTITY_ITEM_PICKUP, 3, 1.1F);
                    }

                }
                break;
        }
    }

    @Override
    public void stopInvocation() {

    }
}


