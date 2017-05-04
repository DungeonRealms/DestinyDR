package net.dungeonrealms.game.listener.mechanic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanic.DungeonManager;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.DungeonManager.DungeonObject;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.world.entity.type.monster.boss.DungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumDungeonBoss;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by Chase on Oct 18, 2015
 */
public class BossListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBossDeath(EntityDeathEvent event) {
		if (event.getEntity().hasMetadata("boss")) {
			event.getEntity().removeMetadata("boss", DungeonRealms.getInstance());
			if (event.getEntity() instanceof CraftLivingEntity) {
				DungeonBoss b = (DungeonBoss) ((CraftLivingEntity) event.getEntity()).getHandle();
				if (!b.getEnumBoss().isFinalBoss()) return;
				if (DungeonManager.getInstance().getDungeon(event.getEntity().getWorld()) != null) {
					DungeonObject dungeon = DungeonManager.getInstance().getDungeon(event.getEntity().getWorld());
					dungeon.teleportPlayersOut(false);
					dungeon.giveShards();
				}
				b.say(b.getEnumBoss().getDeathMessage());
				b.onBossDeath();
				
				//Remove Nearby Fire
				b.getNearbyBlocks(event.getEntity().getLocation(), 10).stream().filter(bk -> bk.getType() == Material.FIRE).forEach(bk -> bk.setType(Material.AIR));
				
				try {
					Random random = new Random();
		            ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.FIREWORKS_SPARK, event.getEntity().getLocation().add(0, 2, 0), random.nextFloat(), random.nextFloat(), random.nextFloat(), 0.2F, 200);
		        } catch (Exception err) {
		            err.printStackTrace();
		        }
				
				//Give out boss drops
				Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
					handleDrops(b);
					b.dropMount(b.getBukkitEntity(), b.getEnumBoss().getDungeonType());
				}, 5L);
				
				for(Player player : event.getEntity().getWorld().getPlayers()){
					if (player.getGameMode() != GameMode.SURVIVAL) 
						continue;
					
	            	PlayerWrapper gp = PlayerWrapper.getPlayerWrapper(player);
	            	if (gp != null)
	            		b.addKillStat(gp);
				}
			}
		}
	}
	
	private void handleDrops(DungeonBoss boss){
		LivingEntity livingEntity = (LivingEntity)boss.getBukkitEntity();
		Random random = new Random();
		int drop = random.nextInt(100);
		if (drop < 80) { // 80% chance!
            List<ItemStack> possible_drops = new ArrayList<>();
            for (ItemStack is : livingEntity.getEquipment().getArmorContents()) {
                if (is == null || is.getType() == Material.AIR || is.getTypeId() == 144 || is.getTypeId() == 397) {
                    continue;
                }
                ItemMeta im = is.getItemMeta();
                is.getEnchantments().keySet().forEach(is::removeEnchantment);
                is.setItemMeta(im);
                possible_drops.add(is);
            }
            ItemStack weapon = livingEntity.getEquipment().getItemInMainHand();
            ItemMeta im = weapon.getItemMeta();
            weapon.getEnchantments().keySet().forEach(weapon::removeEnchantment);
            weapon.setItemMeta(im);
            possible_drops.add(weapon);

            ItemStack reward = ItemManager.makeSoulBound(possible_drops.get(random.nextInt(possible_drops.size())));
            reward = ItemManager.addPartyMemberSoulboundBypass(reward, 60 * 5, livingEntity.getWorld().getPlayers());
            livingEntity.getWorld().dropItem(livingEntity.getLocation(), reward);

            List<String> hoveredChat = new ArrayList<>();
            ItemMeta meta = reward.getItemMeta();
            hoveredChat.add((meta.hasDisplayName() ? meta.getDisplayName() : reward.getType().name()));
            if (meta.hasLore())
                hoveredChat.addAll(meta.getLore());
           
            final JSONMessage normal = new JSONMessage(ChatColor.DARK_PURPLE + "The boss has dropped: ", ChatColor.DARK_PURPLE);
            normal.addHoverText(hoveredChat, ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "SHOW");
            livingEntity.getWorld().getPlayers().forEach(normal::sendToPlayer);
        }
		
		int gemDrop = boss.getGemDrop();
        int groupSize = (int) livingEntity.getWorld().getPlayers().stream().filter(p -> p.getGameMode() == GameMode.SURVIVAL).count();

        int perPlayerDrop = groupSize == 0 ? 1 : Math.round(gemDrop / groupSize);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            for (Player player : livingEntity.getWorld().getPlayers()) {
                player.sendMessage(ChatColor.DARK_PURPLE + "The boss has dropped " + ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + gemDrop + ChatColor.DARK_PURPLE + " gems.");
                player.sendMessage(ChatColor.DARK_PURPLE + "Each player receives " + ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + perPlayerDrop + ChatColor.DARK_PURPLE + " gems!");
            }
            
            String partyMembers = "";
            for (Player player : livingEntity.getWorld().getPlayers()) {
                if (player.getGameMode() != GameMode.SURVIVAL && !DungeonManager.getInstance().isAllOppedPlayers(livingEntity.getWorld()))
                    continue;
                
                partyMembers += player.getName() + ", ";

                if (groupSize > 0) {
                    ItemStack banknote = BankMechanics.createBankNote(perPlayerDrop, boss.getEnumBoss().getName());
                    if (player.getInventory().firstEmpty() == -1) {
                        player.getWorld().dropItem(player.getLocation(), banknote);
                        player.sendMessage(ChatColor.RED + "Because you had no room in your inventory, your new bank note has been placed at your character's feet.");
                    } else {
                        player.getInventory().addItem(banknote);
                    }
                    GameAPI.getGamePlayer(player).addExperience(boss.getXPDrop(), false, true);
                }
            }
            
            final String adventurers = partyMembers.substring(0, partyMembers.length() - 2);
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                Bukkit.broadcastMessage(ChatColor.GOLD.toString() + ChatColor.BOLD + ">> " + ChatColor.GOLD + (boss.getEnumBoss().getPrefix().length() > 0 ? "The " + boss.getEnumBoss().getPrefix() + " " : "") + ChatColor.UNDERLINE + boss.getEnumBoss().getName() + ChatColor.RESET + ChatColor.GOLD + " has been slain by a group of adventurers!");
                Bukkit.broadcastMessage(ChatColor.GRAY + "Group: " + adventurers);
            }, 60L);
        }, 5L);
	}
}
