package net.dungeonrealms.game.listener.inventory;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.affair.party.Party;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.item.items.functional.PotionItem;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.PlayerManager;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.glow.GlowAPI;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by Kieran on 9/18/2015.
 */
public class ItemListener implements Listener {


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        //Party handler here.
        Optional<Party> party = Affair.getInstance().getParty(event.getPlayer());
        if (party != null && party.isPresent()) {
            Party part = party.get();
            Affair.getInstance().handlePartyPickup(event, part);
        }
    }

    /**
     * Makes Uncommon+ Items glow
     */
    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        this.applyRarityGlow(event.getEntity());
    }

    private void applyRarityGlow(Item entity) {
        ItemStack item = entity.getItemStack();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
            return;
        List<String> lore = item.getItemMeta().getLore();
        for (int i = 1; i < ItemRarity.values().length; i++) {
            ItemRarity rarity = ItemRarity.getById(i);
            for (String s : lore) {
                if (s.contains(rarity.getName())) {
                	Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> {
                		//Filter out players who have toggle glow off.
                		List<Player> sendTo = GameAPI.getNearbyPlayers(entity.getLocation(), 100, true).stream().filter(p -> {
                			return (boolean)DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOW, p.getUniqueId());
                		}).collect(Collectors.toList());
                		//Set the item as glowing.
                		GlowAPI.setGlowing(entity, GlowAPI.Color.valueOf(rarity.getColor().name()), sendTo);
                	});
                	return;
                }
            }
        }
    }

    /**
     * Used to handle dropping a soulbound, untradeable, or
     * permanently untradeable item.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDrop(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;
        Player p = event.getPlayer();
        GamePlayer gp = GameAPI.getGamePlayer(p);
        if(gp != null && !gp.isAbleToDrop())
        	return;
        ItemStack item = event.getItemDrop().getItemStack();

        //  SOULBOUND ITEM DESTRUCTION PROMPT  //
        if (ItemManager.isItemSoulbound(item)) {
            //Don't cancel this event inside of here. It keeps the item in the inventory.
            event.getItemDrop().remove();
            p.sendMessage(ChatColor.RED + "Are you sure you want to " + ChatColor.UNDERLINE + "destroy" + ChatColor.RED + " this Soulbound item? ");
            p.sendMessage(ChatColor.GRAY + "Type " + ChatColor.GREEN + ChatColor.BOLD + "Y" + ChatColor.GRAY + " or " + ChatColor.DARK_RED + ChatColor.BOLD + "N" + ChatColor.GRAY + " to confirm.");
            p.playSound(p.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1, 1.2F);
            
            if(p.getItemOnCursor().equals(item))
            	p.setItemOnCursor(null);
            
            Chat.promptPlayerConfirmation(p, () -> {
            	p.sendMessage(ChatColor.RED + "Item " + (item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() + " " : "") + ChatColor.RED + "has been " + ChatColor.UNDERLINE + "destroyed.");
            }, () -> {
            	p.sendMessage(ChatColor.RED + "Soulbound item destruction " + ChatColor.UNDERLINE + "CANCELLED");
                GameAPI.giveOrDropItem(p, item.clone());
            });

            return;
        }
        
        //  PREVENT DROPPING PERMANENTLY UNTRADEABLE ITEMS  //
        if(ItemManager.isItemPermanentlyUntradeable(item)) {
        	event.setCancelled(true);
        	event.getItemDrop().remove();
        	event.getPlayer().sendMessage(ChatColor.GRAY + "This item is " + ChatColor.UNDERLINE + "not" + ChatColor.GRAY + " droppable.");
        	return;
        }

        //  PREVENT DROPPING UNTRADEABLE ITEMS  //
        if (!ItemManager.isItemTradeable(item)) { 
        	event.getItemDrop().remove();
        	p.sendMessage(ChatColor.GRAY + "This item was " + ChatColor.ITALIC + "untradeable" + ChatColor.GRAY + ", " + "so it has " + ChatColor.UNDERLINE + "vanished.");
        	p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.6F, 0.2F);
        	return;
        }
        
        PlayerManager.checkInventory(event.getPlayer());
        
        //  SILENTLY REMOVE UNDROPPABLE ITEMS  //
        if(!ItemManager.isItemDroppable(item)) {
        	event.getItemDrop().remove();
        	event.setCancelled(true);
        	return;
        }
    }

    /**
     * Handles Right Click of Character Journal
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUseMap(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        Player p = event.getPlayer();
        if (p.getEquipment().getItemInMainHand() == null || p.getEquipment().getItemInMainHand().getType() != Material.EMPTY_MAP)
            return;
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(p.getEquipment().getItemInMainHand());
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null) return;
        if (tag.hasKey("type")) {
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onGuildBannerEquip(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;

        Player p = event.getPlayer();
        if (p.getInventory().getItemInMainHand() == null || p.getInventory().getItemInMainHand().getType() != Material.BANNER)
            return;
        if (!p.getInventory().getItemInMainHand().hasItemMeta()) return;
        if (p.getInventory().getItemInMainHand().getItemMeta().getDisplayName() == null) return;
        if (!p.getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains("Guild banner")) return;

        String guildName = p.getInventory().getItemInMainHand().getItemMeta().getDisplayName().substring(2).replace("'s Guild banner", "").replaceAll("\\s", "").toLowerCase();

        final ItemStack banner = p.getInventory().getItemInMainHand();

        p.getInventory().setItemInMainHand(p.getInventory().getHelmet());
        p.getInventory().setHelmet(banner);

        GuildDatabaseAPI.get().doesGuildNameExist(guildName, exists -> {
            if (exists && GuildDatabaseAPI.get().getGuildOf(p.getUniqueId()).equals(guildName)) {
                Achievements.getInstance().giveAchievement(p.getUniqueId(), Achievements.EnumAchievements.GUILD_REPESENT);
                String motd = GuildDatabaseAPI.get().getMotdOf(guildName);

                if (!motd.isEmpty())
                    p.sendMessage(ChatColor.GRAY + "\"" + ChatColor.AQUA + motd + ChatColor.GRAY + "\"");
            }
        });

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerUseMountItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.getType() != Material.AIR) {
            NBTWrapper wrapper = new NBTWrapper(item);
            if (wrapper.hasTag("mount")) {
                String mount = wrapper.getString("mount");

                EnumMounts eMount = EnumMounts.getByName(mount);
                if (eMount == null) return;

                event.setCancelled(true);
                event.setUseItemInHand(Event.Result.DENY);
                event.setUseInteractedBlock(Event.Result.DENY);
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                    Player player = event.getPlayer();
                    if (EntityAPI.hasMountOut(player.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + "You already have a mount currently spawned.");
                        player.sendMessage(ChatColor.GRAY + "Use '/mount' to remove your spawned mount.");
                        return;
                    }

                    if (event.getPlayer().hasMetadata("summoningMount")) {
                        player.sendMessage(ChatColor.RED + "You are already summoning a mount!");
                        return;
                    }

                    Location startingLocation = player.getLocation();

                    AtomicInteger counter = new AtomicInteger(5);
                    player.sendMessage(ChatColor.WHITE.toString() + ChatColor.BOLD + "SUMMONING" + ChatColor.WHITE + " ... " + counter.get() + ChatColor.BOLD + "s");

                    player.setMetadata("summoningMount", new FixedMetadataValue(DungeonRealms.getInstance(), ""));
                    new BukkitRunnable() {
                        public void run() {
                            if (!player.isOnline()) {
                                player.removeMetadata("summoningMount", DungeonRealms.getInstance());
                                cancel();
                                return;
                            }

                            if (player.getLocation().distance(startingLocation) <= 4) {
                                if (!EntityAPI.hasMountOut(player.getUniqueId())) {
                                    if (!CombatLog.isInCombat(player) && !CombatLog.inPVP(player)) {
                                        if (counter.decrementAndGet() > 0) {
                                            player.sendMessage(ChatColor.WHITE.toString() + ChatColor.BOLD + "SUMMONING" + ChatColor.WHITE + " ... " + counter.get() + ChatColor.BOLD + "s");
                                            ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SPELL, player.getLocation().add(0, 0.15, 0),
                                                    ThreadLocalRandom.current().nextFloat(), ThreadLocalRandom.current().nextFloat(), ThreadLocalRandom.current().nextFloat(), 0.5F, 80);
//                                            ParticleAPI.ParticleEffect.sendToLocation(ParticleAPI.ParticleEffect.SPELL, pl.getLocation().add(0, 0.15, 0), new Random().nextFloat(),
//                                                    new Random().nextFloat(), new Random().nextFloat(), 0.5F, 80);
                                        } else {
                                            MountUtils.spawnMount(player.getUniqueId(), eMount.getRawName(), null);
                                            cancel();
                                            player.removeMetadata("summoningMount", DungeonRealms.getInstance());
                                        }
                                    } else {
                                        player.sendMessage(ChatColor.RED + "Combat has cancelled your mount summoning!");
                                        player.removeMetadata("summoningMount", DungeonRealms.getInstance());
                                        cancel();
                                    }
                                } else {
                                    player.sendMessage(ChatColor.RED + "Mount already detected out.");
                                    player.removeMetadata("summoningMount", DungeonRealms.getInstance());
                                    cancel();
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "Movement has cancelled your mount summoning!");
                                player.removeMetadata("summoningMount", DungeonRealms.getInstance());
                                cancel();
                            }
                        }
                    }.runTaskTimer(DungeonRealms.getInstance(), 20, 20);
                }
            }
        }
    }
    
    /**
     * This is a prank for the people who are supposedly using vanilla potions in pvp.
     */
    private void playPotionPrank(PlayerInteractEvent evt) {
    	ItemStack item = evt.getItem();
    	Player player = evt.getPlayer();
    	if(item == null || item.getType() == Material.AIR || PotionItem.isPotion(item) || (item.getType() != Material.POTION && item.getType() != Material.SPLASH_POTION))
    		return;
    	evt.setCancelled(true);
    	player.getInventory().remove(item);
    	GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED + "[ALERT] " + ChatColor.WHITE + "Removing vanilla potion from " + player.getName() + ".");
    	
    	player.sendMessage(ChatColor.GRAY + " *Glug* *Glug* *Glug*");
    	player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1, 1);
    	
    	Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
    		player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 375, 2));
    		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 350, 2));
    		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 350, 2));
    	}, 5);
    	
    	for(int second = 0; second < 15; second++) { 
    		for(int i = 0; i < 4; i++) {
    			final int in = i;
    			Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> { if(player.isOnline()) player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_DEATH, 0.17f + 0.33f * in, 0.25f * in + 0.25f);}, (second * 20) + 1 + i * 5);
    		}
    	}
    	
    	Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
    		player.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.ITALIC + "Regret shoots through you as vomit pours from your mouth.");
    	}, 16);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerDrinkVanillaPotion(PlayerInteractEvent event) {
    	playPotionPrank(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void petRename(PlayerInteractEntityEvent event) {
        if (!event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;
        Player player = event.getPlayer();
        if (event.getRightClicked() instanceof Player) return;
        if (player.getEquipment().getItemInMainHand() == null || player.getEquipment().getItemInMainHand().getType() == Material.AIR)
            return;
        if (player.getEquipment().getItemInMainHand().getType() != Material.NAME_TAG) return;
        event.setCancelled(true);
        player.updateInventory();
        if (!EntityAPI.hasPetOut(player.getUniqueId())) return;
        if (EntityAPI.getPlayerPet(player.getUniqueId()).equals(((CraftEntity) event.getRightClicked()).getHandle())) {
            player.sendMessage(ChatColor.GRAY + "Enter a name for your pet, or type " + ChatColor.RED + ChatColor.UNDERLINE + "cancel" + ChatColor.GRAY + " to end the process.");
            Chat.listenForMessage(player, newPetName -> {
                if (newPetName.getMessage().equalsIgnoreCase("cancel") || newPetName.getMessage().equalsIgnoreCase("exit")) {
                    player.sendMessage(ChatColor.GRAY + "Pet naming " + ChatColor.RED + ChatColor.UNDERLINE + "CANCELLED.");
                    return;
                }
                Entity pet = EntityAPI.getPlayerPet(player.getUniqueId());
                if (pet == null) {
                    return;
                }

                String inputName = newPetName.getMessage();

                // Name must be below 20 characters
                if (inputName.length() > 20) {
                    player.sendMessage(ChatColor.RED + "Your pet name exceeds the maximum length of 20 characters.");
                    player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "You were " + (inputName.length() - 20) + " characters over the limit.");
                    return;
                }

                if (inputName.contains("@")) {
                    inputName = inputName.replaceAll("@", "_");
                }

                String checkedPetName = Chat.getInstance().checkForBannedWords(inputName);

                String activePet = (String) DatabaseAPI.getInstance().getData(EnumData.ACTIVE_PET, player.getUniqueId());
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.PETS, activePet, true);
                if (activePet.contains("@")) {
                    activePet = activePet.split("@")[0];
                }
                String newPet = activePet + "@" + checkedPetName;
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.PETS, activePet, true);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, EnumData.PETS, newPet, true);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_PET, newPet, true);
                ChatColor prefix = ChatColor.WHITE;
                if (Rank.isSubscriber(player)) {
                    String rank = Rank.getInstance().getRank(player.getUniqueId());
                    if (rank.equalsIgnoreCase("sub") || rank.equalsIgnoreCase("hiddenmod")) {
                        prefix = ChatColor.GREEN;
                    } else if (rank.equalsIgnoreCase("sub+")) {
                        prefix = ChatColor.GOLD;
                    } else if (rank.equalsIgnoreCase("sub++")) {
                        prefix = ChatColor.YELLOW;
                    }
                }
                if (Rank.isDev(player)) {
                    prefix = ChatColor.AQUA;
                }
                pet.setCustomName(prefix + checkedPetName);
                player.sendMessage(ChatColor.GRAY + "Your pet's name has been changed to " + ChatColor.GREEN + ChatColor.UNDERLINE + checkedPetName + ChatColor.GRAY + ".");
            }, null);
        }
    }
}
