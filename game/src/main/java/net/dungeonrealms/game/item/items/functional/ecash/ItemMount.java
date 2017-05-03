package net.dungeonrealms.game.item.items.functional.ecash;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemClickEvent.ItemClickListener;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMountSkins;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.util.MountUtils;

public class ItemMount extends FunctionalItem implements ItemClickListener {
	
	@Getter @Setter
	private boolean mule;
	
	public ItemMount() {
		super(ItemType.MOUNT);
		setUntradeable(true);
		setMule(false);
	}
	
	public ItemMount(ItemStack item) {
		super(item);
		setMule(getTagBool("mule"));
	}
	
	@Override
	public void updateItem() {
		setTagBool("mule", isMule());
		super.updateItem();
	}
	
	@SuppressWarnings("unchecked")
	public static void attemptSummonMount(Player player) {
		if (!canSummonMount(player))
        	return;
        
		EnumMounts mountType = EnumMounts.getByName((String) DatabaseAPI.getInstance().getData(EnumData.ACTIVE_MOUNT, player.getUniqueId()));
        
        if (mountType == null) {
            player.sendMessage(ChatColor.RED + "You don't have an active mount, please enter the mounts section in your profile to set one.");
            player.closeInventory();
            return;
        }
        
        List<String> playerMounts = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MOUNTS, player.getUniqueId());
        if (!playerMounts.contains(mountType.name())) {
            player.sendMessage(ChatColor.RED + "You do not own this mount.");
            return;
        }
        
        player.sendMessage(ChatColor.GREEN + "Your mount is being summoned into this world!");
        final int[] count = {0};
        Location startingLocation = player.getLocation();
        final boolean[] cancelled = {false};
        int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
        	boolean cancel = !canSummonMount(player);
        	if (player.getLocation().distanceSquared(startingLocation) > 4) {
        		cancel = true;
        		player.sendMessage(ChatColor.RED + "You're too far away.");
        	}
        	
        	if (cancel) {
        		cancelled[0] = true;
        		count[0] = 0;
        	}
        	
        	if (cancelled[0])
        		return;
        	
        	if (count[0] < 3) {
                count[0]++;
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SPELL, player.getLocation(), 1F, 0F, 1F, .1F, 40);
            } else {
            	MountUtils.spawnMount(player, mountType, EnumMountSkins.getByName((String) DatabaseAPI.getInstance().getData(EnumData.ACTIVE_MOUNT_SKIN, player.getUniqueId())));
            }
        	
        }, 0L, 20L);
        Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> Bukkit.getScheduler().cancelTask(taskID), 65L);
	}

	private static boolean canSummonMount(Player player) {
		// Dismiss existing mount.
		if (MountUtils.hasActiveMount(player)) {
			MountUtils.removeMount(player);
			player.sendMessage(ChatColor.GREEN + "Your mount has been dismissed.");
			return false;
		}
		
		if (player.getEyeLocation().getBlock().getType() != Material.AIR) {
            player.sendMessage(ChatColor.RED + "You cannot summon a mount here!");
            return false;
        }
		
		if (CombatLog.isInCombat(player)) {
            player.sendMessage(ChatColor.RED + "You cannot summon a mount while in combat!");
            return false;
        }
		
		return true;
	}
	
	@Override
	public void onClick(ItemClickEvent evt) {
        attemptSummonMount(evt.getPlayer());
	}

	@Override
	protected String getDisplayName() {
		return ChatColor.GREEN + "Mount";
	}

	@Override
	protected String[] getLore() {
		return new String[] { ChatColor.DARK_GRAY + "Summons your active Mount." };
	}

	@Override
	protected ItemUsage[] getUsage() {
		return INTERACT_RIGHT_CLICK;
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.SADDLE);
	}
}
