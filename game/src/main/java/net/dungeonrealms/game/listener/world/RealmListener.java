package net.dungeonrealms.game.listener.world;

import edu.umd.cs.findbugs.ba.BlockType;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.updater.UpdateEvent;
import net.dungeonrealms.common.game.updater.UpdateType;
import net.dungeonrealms.common.game.util.Cooldown;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.handler.FriendHandler;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.repairing.RepairAPI;
import net.dungeonrealms.game.world.realms.Realm;
import net.dungeonrealms.game.world.realms.RealmMaterialFactory;
import net.dungeonrealms.game.world.realms.RealmProperty;
import net.dungeonrealms.game.world.realms.RealmState;
import net.dungeonrealms.game.world.realms.RealmTier;
import net.dungeonrealms.game.world.realms.Realms;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/21/2016
 */
public class RealmListener implements Listener {

    @EventHandler
    public void onWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        player.closeInventory();
        if (EntityAPI.hasPetOut(player.getUniqueId())) {
            net.minecraft.server.v1_9_R2.Entity pet = EntityMechanics.PLAYER_PETS.get(player.getUniqueId());
            pet.dead = true;
            EntityAPI.removePlayerPetList(player.getUniqueId());
        }

        if (EntityAPI.hasMountOut(player.getUniqueId())) {
            net.minecraft.server.v1_9_R2.Entity mount = EntityMechanics.PLAYER_MOUNTS.get(player.getUniqueId());
            mount.dead = true;
            EntityAPI.removePlayerMountList(player.getUniqueId());
        }

        World to = player.getWorld();
        
        Realm realm = Realms.getInstance().getRealm(to);
        if (realm != null) {
        	
            if (!player.getUniqueId().equals(realm.getOwner()))
                player.sendMessage(ChatColor.LIGHT_PURPLE + "You have entered " + ChatColor.BOLD + realm.getName() + "'s" + ChatColor.LIGHT_PURPLE + " realm.");
            else
                player.sendMessage(ChatColor.LIGHT_PURPLE + "You have returned to " + ChatColor.BOLD + "YOUR" + ChatColor.LIGHT_PURPLE + " realm.");
            
            //Toggle flight.
            if (realm.canBuild(player) && realm.getProperty("flight")) {
                player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "FLYING ENABLED");
                player.setAllowFlight(true);
            }
            
            //Show title.
            if (realm.getTitle() != null)
                player.sendMessage(realm.getTitle());

            player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "INVINCIBILITY (15s)");
            player.sendMessage(ChatColor.GRAY + "You will " + ChatColor.UNDERLINE + "NOT" + ChatColor.GRAY.toString()
                    + " be flagged as 'combat logged' while invincible.");

            if (GameAPI.getGamePlayer(player) != null)
                GameAPI.getGamePlayer(player).setInvulnerable(true);

            Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
                player.setFireTicks(0);
                player.setFallDistance(0.0F);
                if (GameAPI.getGamePlayer(player) != null)
                    GameAPI.getGamePlayer(player).setInvulnerable(false);
            }, 15 * 20L);

        } else if (Realms.getInstance().getRealm(event.getFrom()) != null) {
            Realm realmFrom = Realms.getInstance().getRealm(event.getFrom());
            
            if (!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR))
                player.setAllowFlight(false);

            if (player.getUniqueId().equals(realmFrom.getOwner()))
                player.sendMessage(ChatColor.LIGHT_PURPLE + "You have left " + ChatColor.BOLD + "YOUR" + ChatColor.LIGHT_PURPLE + " realm.");
            else
                player.sendMessage(ChatColor.LIGHT_PURPLE + "You have left " + ChatColor.BOLD + realmFrom.getName() + "'s" + ChatColor.LIGHT_PURPLE + " realm.");
        }
    }


    @EventHandler
    public void onBlock(BlockPhysicsEvent event) {
        if (GameAPI.isMainWorld(event.getBlock().getLocation())) return;
        
        Realm realm = Realms.getInstance().getRealm(event.getBlock().getWorld());

        if (realm != null && !realm.isSettingSpawn() && event.getBlock().getType().equals(Material.PORTAL))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.hasBlock()) {
                if (player.getGameMode() == GameMode.CREATIVE || event.getClickedBlock().getWorld() == Bukkit.getWorlds().get(0))
                    return;

                if (event.getItem() != null && GameAPI.isWeapon(event.getItem()) && event.getClickedBlock().getType() == Material.GRASS) {
                    event.setCancelled(true);
                    event.setUseInteractedBlock(Event.Result.DENY);
                }
            }
        }
    }

    @EventHandler
    public void RealmBlockProcessor(UpdateEvent e) {
        if (!e.getType().equals(UpdateType.SLOW)) return;

        int blocksPasted = 0;
        
        for(UUID uuid : Realms.getInstance().getProcessingBlocks().keySet()) {
        	
        	//Max blocks allowed to upgrade per second prevents crashing.
        	if(blocksPasted > Realms.SERVER_BLOCK_BUFFER)
        		break;
        	
        	Realm realm = Realms.getInstance().getRealm(uuid);
        	
        	int yMin = Realms.GRASS_POSITION - realm.getTier().getDimensions() + 1;
        	CopyOnWriteArrayList<Location> needToPlace = new CopyOnWriteArrayList<>(Realms.getInstance().getProcessingBlocks().get(uuid));
        	
        	for (Location loc : needToPlace) {
        		if (blocksPasted >= Realms.SERVER_BLOCK_BUFFER)
        			break;
        		
        		//Check that we can replace this block.
        		if (Realms.REPLACEABLE_BLOCKS.contains(loc.getBlock().getType())) {
        			if (loc.getBlock().getY() == Realms.GRASS_POSITION) {
                        loc.getBlock().setType(Material.GRASS);
                    } else if (loc.getBlock().getY() == yMin) {
                        loc.getBlock().setType(Material.BEDROCK);
                    } else {
                        loc.getBlock().setType(Material.DIRT);
                    }
        		}
        		
        		needToPlace.remove(loc);
        		blocksPasted++;
        	}
        	
        	if (needToPlace.isEmpty()) {
        		//Realm upgrade complete.
        		Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    player.sendMessage("");
                    Utils.sendCenteredMessage(player, ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "REALM UPGRADE COMPLETE.");
                    player.sendMessage("");
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
                    realm.setState(RealmState.CLOSED);
                } else {
                	realm.removeRealm(true);
                }

                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.REALM_UPGRADE, false, true, doAfter -> {
                    GameAPI.updatePlayerData(uuid);
                });
                
                Realms.getInstance().getProcessingBlocks().remove(uuid);
                realm.setUpgradeProgress(0);
            } else {
            	RealmTier oldTier = RealmTier.getByTier(realm.getTier().getTier() - 1);
                double totalArea = Math.pow(realm.getTier().getDimensions(), 3) - Math.pow(oldTier.getDimensions(), 3);
                double completeBlocks = totalArea - needToPlace.size();

                double percent = ((completeBlocks / totalArea) * 100.0D);
                realm.setUpgradeProgress(percent);
                Realms.getInstance().getProcessingBlocks().put(uuid, needToPlace);
            }
        }
    }

    @SuppressWarnings("unchecked")
	@EventHandler
    public void updateRealmEvent(UpdateEvent e) {
        if (!e.getType().equals(UpdateType.SEC)) return;
        
        for (Realm realm : Realms.getInstance().getRealms()) {
        	Player owner = Bukkit.getPlayer(realm.getOwner());
            if (owner != null && owner.isOnline()) {
                if (!realm.getProperty("peaceful") && realm.getProperty("flight")) {
                    RealmProperty<Boolean> property = (RealmProperty<Boolean>) realm.getRealmProperties().get("flight");
                    property.setExpiry(System.currentTimeMillis() - 1000L);
                }
                if (!realm.isOpen())
                	continue;

                for (RealmProperty<?> p : realm.getRealmProperties().values()) {
                    if (!(p.hasExpired() && p.isAcknowledgeExpiration())) continue;
                    
                    switch (p.getName()) {
                        case "peaceful":
                            

                            if (owner != null)
                                owner.sendMessage(ChatColor.RED + "Your realm is now once again a " + ChatColor.BOLD + "CHAOTIC" + ChatColor.RED + " zone.");

                            if (realm.getProperty("flight")) {

                                if (owner != null)
                                    owner.sendMessage(ChatColor.GRAY + "Due to this, your " + ChatColor.UNDERLINE + "Orb of Flight" + ChatColor.GRAY
                                            + " has expired.");

                                RealmProperty<Boolean> property = (RealmProperty<Boolean>) realm.getRealmProperties().get("flight");
                                property.setExpiry(System.currentTimeMillis() - 1000L);
                            }

                            break;
                        case "flight":
                            if (owner != null)
                            	owner.sendMessage(ChatColor.RED + "Your " + ChatColor.UNDERLINE + "Orb of Flight" + ChatColor.RED + " effect has expired.");

                            for (Player pl : realm.getWorld().getPlayers())
                            	pl.setAllowFlight(false);
                            break;
                    }
                    realm.updateWGFlags();
                    realm.updateRealmHologram();
                    p.setAcknowledgeExpiration(false);
                }

                if (realm.getPortalLocation() == null || realm.getState() != RealmState.OPENED) continue;

                Location loc = realm.getPortalLocation().clone().add(0, 1, 0);

                if (Rank.isTrialGM(Bukkit.getPlayer(realm.getOwner())) && !DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.containsKey(Bukkit.getPlayer(realm.getOwner())))
                    createDoubleHelix(loc);
                else if (DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.containsKey(Bukkit.getPlayer(realm.getOwner())))
                    DonationEffects.getInstance().spawnPlayerParticleEffects(loc);

                //loc.subtract(.5D, 2D, .5D);
                if (realm.getProperty("peaceful"))
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.HAPPY_VILLAGER, loc.clone().add(0.5, 1.5, 0.5), 0, 0, 0, 0F, 20);

                //loc.subtract(.5D, 1.5D, .5D);
                if (realm.getProperty("flight"))
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CLOUD, loc.clone().add(0.5, 1.5, 0.5), 0, 0, 0, 0F, 20);
            }
        }
    }

    @EventHandler
    public void onIllegalItemDispense(ProjectileLaunchEvent event) {
        if (event.getEntityType() == EntityType.DROPPED_ITEM)
            event.setCancelled(true);
    }
    
    public static boolean isItemBanned(ItemStack item) {
    	if(item == null || item.getType() == Material.AIR)
    		return false;
    	
    	return GameAPI.isArmor(item) || GameAPI.isWeapon(item) || BankMechanics.isMoney(item)
    			|| ItemManager.isEnchantScroll(item) || ItemManager.isProtectScroll(item)
    			|| GameAPI.isOrb(item) || RepairAPI.isItemArmorScrap(item);
    }

    @EventHandler
    public void onHopperMove(InventoryMoveItemEvent event) {
    	if(!Realms.getInstance().isRealm(event.getSource().getLocation()))
    		return;
    		
        if (isItemBanned(event.getItem())) {
        	event.getItem().setType(Material.AIR);
            event.setCancelled(true);
        }
    }

    private void createDoubleHelix(Location loc) {
        double radius;

        for (double y = 0; y < 2; y += 0.007) {
            radius = y / 3;
            double x = radius * Math.cos(3 * y);
            double z = radius * Math.sin(3 * y);

            double y2 = 3 - y;

            final Location loc2 = new Location(loc.getWorld(), loc.getX() + x + 0.5, loc.getY() + y2, loc.getZ() + z + 0.5);

            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(),
                    () -> ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.RED_DUST, loc2, 0, 0, 0, 0, 1), (long) ((y + 1) * 20));
        }

        for (double y = 0; y < 2; y += 0.007) {
            radius = y / 3;
            double x = -(radius * Math.cos(3 * y));
            double z = -(radius * Math.sin(3 * y));

            double y2 = 5 - y;

            final Location loc2 = new Location(loc.getWorld(), loc.getX() + x + 0.5, loc.getY() + y2, loc.getZ() + z + 0.5);

            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(),
                    () -> ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.RED_DUST, loc2, 0, 0, 0, 0, 1), (long) ((y + 1) * 20));
        }
    }

    @SuppressWarnings("unchecked")
	@EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerUseOrb(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        Player p = event.getPlayer();
        if (p.getEquipment().getItemInMainHand() == null)
            return;
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(p.getEquipment().getItemInMainHand());
        if (nmsStack == null) return;
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null) return;
        if (!tag.hasKey("orb")) return;
        if (!tag.getString("orb").equalsIgnoreCase("flight") && !tag.getString("orb").equalsIgnoreCase("peace")) return;

        event.setCancelled(true);
        event.setUseItemInHand(Event.Result.DENY);

        if (Cooldown.hasCooldown(event.getPlayer().getUniqueId())) return;
        Cooldown.addCooldown(event.getPlayer().getUniqueId(), 1000);

        if (!p.getWorld().getName().equalsIgnoreCase(p.getUniqueId().toString()) && !Rank.isTrialGM(p)) {
            // Trying to use in a realm that isn't theirs.
            if (tag.getString("orb").equalsIgnoreCase("flight"))
                p.sendMessage(ChatColor.RED + "You may only use an " + ChatColor.UNDERLINE + "Orb of Flight" + ChatColor.RED + " in your OWN realm.");
            else if (tag.getString("orb").equalsIgnoreCase("peace"))
                p.sendMessage(ChatColor.RED + "You may only use an " + ChatColor.UNDERLINE + "Orb of Peace" + ChatColor.RED + " in your OWN realm.");

            event.setCancelled(true);
            event.setUseItemInHand(Event.Result.DENY);
            return;
        }
        Realm realm = Realms.getInstance().getRealm(p);


        if (tag.getString("orb").equalsIgnoreCase("flight")) {

            if (!realm.getProperty("peaceful")) {
                p.sendMessage(ChatColor.RED + "You can only use an " + ChatColor.UNDERLINE + "Orb of Flight" + ChatColor.RED
                        + " in a peaceful realm.");
                return;
            }

            p.sendMessage("");
            p.sendMessage(ChatColor.AQUA + "Your realm will now be a " + ChatColor.BOLD + "FLY ENABLED ZONE" + ChatColor.AQUA
                    + " for 30 minute(s), or until logout.");
            p.sendMessage(ChatColor.GRAY + "Only YOU and anyone you add to your build list will be able to fly in your realm.");

            for (Player pl : realm.getWorld().getPlayers()) {
                if (pl == null || (!realm.canBuild(pl)))
                    continue;
                pl.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "FLYING ENABLED");
                pl.setAllowFlight(true);
            }

            RealmProperty<Boolean> property = (RealmProperty<Boolean>) realm.getRealmProperties().get("flight");
            property.setExpiry(System.currentTimeMillis() + 1800000L);

            property.setValue(true);
            property.setAcknowledgeExpiration(true);
        } else if (tag.getString("orb").equalsIgnoreCase("peace")) {
            GamePlayer gp = GameAPI.getGamePlayer(p);

            if (gp.getPlayerAlignment() == KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
                p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " use an orb of peace while chaotic.");
                return;
            }

            p.sendMessage("");
            p.sendMessage(ChatColor.GREEN + "Your realm will now be a " + ChatColor.BOLD + "SAFE ZONE" + ChatColor.GREEN + " for 1 hour(s), or until logout.");
            p.sendMessage(ChatColor.GRAY + "All damage in your realm will be disabled for this time period.");
            p.getWorld().playEffect(p.getLocation(), Effect.ENDER_SIGNAL, 10);

            RealmProperty<Boolean> property = (RealmProperty<Boolean>) realm.getRealmProperties().get("peaceful");
            property.setExpiry(System.currentTimeMillis() + 3600000L);

            property.setValue(true);
            property.setAcknowledgeExpiration(true);

            // SET WORLD GUARD FLAG
            realm.updateWGFlags();
            realm.updateRealmHologram();
        }

        int amount = p.getInventory().getItemInMainHand().getAmount();
        ItemStack in_hand = p.getInventory().getItemInMainHand();

        if (amount <= 1) {
            p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        } else if (amount > 1) {
            amount--;
            in_hand.setAmount(amount);
            p.getInventory().setItemInMainHand(in_hand);
        }
        p.updateInventory();
    }


    @EventHandler
    public void onMove(PlayerMoveEvent event) {
    	Realm realm = Realms.getInstance().getRealm(event.getPlayer().getWorld());
        if (realm == null) return;
        Player p = event.getPlayer();

        Location to = event.getTo().clone();
        if (event.getTo().getY() <= 0) {

            Location normalWorld = realm.getPortalLocation().clone().add(0, 1, 0);
            p.teleport(normalWorld);
            //Teleporting twice prevents teleporting far up
            p.teleport(normalWorld);
            return;
        }

        int maxDistance = 150;
        if (!(Rank.isTrialGM(p))) {
            if (to.getZ() >= maxDistance || to.getZ() <= -maxDistance || to.getX() >= maxDistance || to.getX() <= -maxDistance) {
                Location newTo = event.getFrom();
                newTo.setPitch(event.getTo().getPitch());
                newTo.setYaw(event.getTo().getYaw());
                event.setTo(newTo);
            }
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Realm realm = Realms.getInstance().getRealm(event.getPlayer().getWorld());

        Player p = event.getPlayer();

        if (realm == null) return;

        if (event.hasBlock())
            if (event.getClickedBlock().getType().equals(Material.ENCHANTMENT_TABLE)) {
                event.setCancelled(true);
                return;
            }

        if (event.hasItem())
            if (event.getItem().getType().equals(Material.ITEM_FRAME) || event.getItem().getType().equals(Material.END_CRYSTAL)) {
                event.setCancelled(true);
                return;
            }

        if (!realm.canBuild(event.getPlayer())) {
            p.sendMessage(ChatColor.RED + "You aren't authorized to build in " + Bukkit.getPlayer(realm.getOwner()).getName() + "'s realm.");
            p.sendMessage(ChatColor.GRAY + Bukkit.getPlayer(realm.getOwner()).getName() + " will have to " + ChatColor.UNDERLINE + "Sneak Left Click" + ChatColor.GRAY +
                    " you with their Realm Portal Rune to add you to their builder list.");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPortalDestory(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (!GameAPI.isMainWorld(event.getPlayer().getWorld())) return;

        Realm realm = Realms.getInstance().getRealm(event.getClickedBlock().getLocation());
        if (realm == null) return;

        if (event.getClickedBlock().getType().equals(Material.PORTAL) && realm.isOwner(event.getPlayer()) ||
                Rank.isTrialGM(event.getPlayer())) {
        	realm.removePortal(null);
            event.setCancelled(true);
            
            if(Rank.isTrialGM(event.getPlayer()) && Bukkit.getPlayer(realm.getOwner()) == null) {
            	realm.removeRealm(true);
            	Utils.sendCenteredMessage(event.getPlayer(), ChatColor.RED + "" + ChatColor.BOLD + "Saved " + realm.getName() + "'s realm.");
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
        Player p = e.getPlayer();

        if (GameAPI.isMainWorld(e.getBlock().getWorld()))
        	return;
        
        Realm realm = Realms.getInstance().getRealm(e.getBlock().getWorld());

        if (realm == null || p.getGameMode() == GameMode.CREATIVE)
        	return;
        
        if (e.getBlock().getType() == Material.PORTAL) {
            e.setCancelled(true);
            return;
        }
        
        int maxSize = realm.getTier().getDimensions() + 16;
        Block b = e.getBlock();
        if (!(Rank.isTrialGM(p))) {
            if (Math.round(b.getX() - 0.5) > maxSize || Math.round(b.getX() - 0.5) < 16 || Math.round(b.getZ() - 0.5) > maxSize
                    || Math.round(b.getZ() - 0.5) < 16 || (b.getY() > (Realms.GRASS_POSITION + (maxSize) + 1)) || (b.getY() < (Realms.GRASS_POSITION - (maxSize) - 1))) {
                e.setCancelled(true);
                p.updateInventory();
                return;
            }
        }

        if (!Rank.isTrialGM(p) && (e.getBlock().getType() == Material.TRAPPED_CHEST || e.getBlock().getType() == Material.GOLD_BLOCK)) {
        	p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " place this "
        			+ e.getBlock().getType().name().toUpperCase() + " as it is an illegal item.");
            p.updateInventory();
            e.setCancelled(true);
            return;
        }


        if (!realm.canBuild(p)) {
            p.sendMessage(ChatColor.RED + "You aren't authorized to build in " + Bukkit.getPlayer(realm.getOwner()).getName() + "'s realm.");
            p.sendMessage(ChatColor.GRAY + Bukkit.getPlayer(realm.getOwner()).getName() + " will have to " + ChatColor.UNDERLINE + "Sneak Left Click" + ChatColor.GRAY +
                    " you with their Realm Portal Rune to add you to their builder list.");
            e.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAddBuilder(EntityDamageByEntityEvent event) {
        if (!GameAPI.isPlayer(event.getDamager()) || !GameAPI.isPlayer(event.getEntity())) return;

        Player p = (Player) event.getDamager();
        Player target = (Player) event.getEntity();

        if (p.getEquipment().getItemInMainHand() == null || p.getEquipment().getItemInMainHand().getType() != Material.NETHER_STAR)
            return;

        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(p.getEquipment().getItemInMainHand());
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null) return;
        if (tag.hasKey("realmPortalRune") && !(tag.getString("realmPortalRune").equalsIgnoreCase("true"))) return;

        event.setCancelled(true);
        event.setDamage(0);

        if (!p.isSneaking()) return;
        
        Realm realm = Realms.getInstance().getRealm(p.getWorld());
        
        if (realm == null || !realm.isOwner(p)) {
        	p.sendMessage(ChatColor.RED + "You must be in your realm to add builders.");
            return;
        }

        if (!(FriendHandler.getInstance().areFriends(p, target.getUniqueId()))) {
            p.sendMessage(ChatColor.RED + "Cannot add a non-buddy to realm build list.");
            p.sendMessage(ChatColor.GRAY + "Goto your friends list in the character profile to add '" + ChatColor.BOLD + target.getName() + ChatColor.GRAY
                    + "' as friend.");
            return;
        }

        if (!realm.getBuilders().contains(target.getUniqueId())) {
            p.sendMessage(ChatColor.GREEN + "" + ChatColor.UNDERLINE + "ADDED " + ChatColor.RESET + ChatColor.GREEN + "" + ChatColor.BOLD + target.getName()
                    + ChatColor.GREEN + " to your realm builder list.");
            p.sendMessage(ChatColor.GRAY + target.getName()
                    + " can now place/destroy blocks in your realm until you logout of your current game session.");
            target.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "ADDED" + ChatColor.GREEN + " to " + p.getName() + "'s build list.");
            target.sendMessage(ChatColor.GRAY + "You can now place/destroy blocks in their realm until the end of their game session.");

            realm.getBuilders().add(target.getUniqueId());

            if (target.getWorld().getName().equals(p.getUniqueId().toString()) && realm.getProperty("flight")) {
                target.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "FLYING ENABLED");
                target.setAllowFlight(true);
            }

        } else {
            p.sendMessage(ChatColor.RED + "" + ChatColor.UNDERLINE + "REMOVED " + ChatColor.RESET + ChatColor.RED + "" + ChatColor.BOLD + target.getName()
                    + ChatColor.RED + " from your realm builder list.");
            p.sendMessage(ChatColor.GRAY + target.getName() + " can no longer place/destroy blocks in your realm.");
            target.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "REMOVED " + ChatColor.RED + "from " + p.getName() + "'s builder list.");
            target.sendMessage(ChatColor.GRAY + "You can no longer place/destroy blocks/fly in their realm.");

            realm.getBuilders().remove(target.getUniqueId());

            if (target.getWorld().getName().equals(p.getUniqueId().toString()) && realm.getProperty("flight"))
                target.setAllowFlight(false);

        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerBlockBreak(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!e.hasBlock() || e.getAction() != Action.LEFT_CLICK_BLOCK || e.isCancelled()
        		|| p.getGameMode() == GameMode.CREATIVE || GameAPI.isMainWorld(e.getClickedBlock().getWorld()))
            return;

        Realm realm = Realms.getInstance().getRealm(p.getWorld());

        if (realm == null || !realm.isOpen())
        	return;

        if (!realm.canBuild(p)) {
            p.sendMessage(ChatColor.RED + "You aren't authorized to build in " + Bukkit.getPlayer(realm.getOwner()).getName() + "'s realm.");
            p.sendMessage(ChatColor.GRAY + Bukkit.getPlayer(realm.getOwner()).getName() + " will have to " + ChatColor.UNDERLINE + "Sneak Left Click" + ChatColor.GRAY +
                    " you with their Realm Portal Rune to add you to their builder list.");
            e.setCancelled(true);
            return;
        }

        Block b = e.getClickedBlock();
        e.setCancelled(true);

        Material m = b.getType();
        if (m == Material.AIR || m == Material.PORTAL) return;

        ItemStack loot = (new ItemStack(b.getType(), 1, b.getState().getData().getData()));
        
        if (b.getType() == Material.WATER || b.getType() == Material.STATIONARY_WATER || b.getType() == Material.LAVA
                || b.getType() == Material.STATIONARY_LAVA) {
            return;
        }

        if (b.getType() == Material.CHEST) {
            Chest c = (Chest) b.getState();
            Inventory chestInv = c.getInventory();
            deleteIllegalItemsInInventory(chestInv, p);
            int chestItems = getUsedSlots(chestInv);
            int freeSlots = getAvailableSlots(p.getInventory());
            
            if (chestItems > freeSlots) {
                p.sendMessage(ChatColor.RED + "You do not have enough room in your inventory for all the items in this chest.");
                p.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "REQ: " + chestItems + " slots");
                return;
            }
            
            for (ItemStack is : chestInv.getContents())
                if (is != null && is.getType() != Material.AIR)
                    p.getInventory().setItem(p.getInventory().firstEmpty(), is);

            chestInv.clear();
        }

        if (b.getState() instanceof InventoryHolder) {
            deleteIllegalItemsInInventory(((InventoryHolder) b.getState()).getInventory(), p);
        }
        
        if (b.getType() == Material.CHEST)
            loot = ItemManager.createRealmChest();
        
        //Get data from bukkit drops.
        for(ItemStack i : b.getDrops())
        	if(i.getType() == b.getType())
        		loot = i;
        
        if (b.getType() == Material.SKULL) {
        	loot.setType(Material.SKULL_ITEM);
        	if (b.getState() instanceof Skull)
                loot.setDurability((short) ((Skull) b.getState()).getSkullType().ordinal());
        }
        
        b.getLocation().getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getTypeId());
        b.setType(Material.AIR);

        int amount = loot.getAmount();
        int max_stack = loot.getMaxStackSize();
        Inventory i = p.getInventory();
        int slot = -1;

        HashMap<Integer, ? extends ItemStack> invItems = i.all(loot.getType());
        for (Map.Entry<Integer, ? extends ItemStack> entry : invItems.entrySet()) {
            ItemStack item = entry.getValue();
            int stackAmount = item.getAmount();
            if (item.getDurability() != loot.getDurability()) {
                continue;
            }
            if ((stackAmount + amount) <= max_stack) {
                slot = entry.getKey();
                item.setAmount(item.getAmount() + amount);
                b.setType(Material.AIR);
                p.getInventory().setItem(slot, item);
                p.updateInventory();
                break; // Set stack more, no need to add a new item to it.
            }
        }


        if (slot == -1) {
            // We never found a stack to add it to.
            if (p.getInventory().firstEmpty() == -1) {
                // No space!
                p.sendMessage(ChatColor.RED + "No inventory space.");
                e.setCancelled(true);
                b.setType(m); // Revert the block.
                return;
            }

            // There's room!
            p.getInventory().setItem(p.getInventory().firstEmpty(), GameAPI.makeItemUntradeable(loot));
        }

        p.updateInventory();
    }


    public int getAvailableSlots(Inventory i) {
        int count = 0;
        for (ItemStack is : i.getStorageContents())
            if (is == null || is.getType() == Material.AIR)
                count++;
        return count;
    }

    public int getUsedSlots(Inventory i) {
        int count = 0;
        for (ItemStack is : i.getContents())
            if (is != null && is.getType() != Material.AIR)
                count++;
        return count;
    }

    @EventHandler
    public void onRealmChestClose(InventoryCloseEvent event) {
        if (Realms.getInstance().isInRealm((Player)event.getPlayer()) && isRealmInventory(event.getInventory()))
        	deleteIllegalItemsInInventory(event.getInventory(), (Player) event.getPlayer());
    }

    @EventHandler
    public void onRealmContainerOpen(InventoryOpenEvent event) {
    	if (Realms.getInstance().isInRealm((Player)event.getPlayer()) && isRealmInventory(event.getInventory()))
        	deleteIllegalItemsInInventory(event.getInventory(), (Player) event.getPlayer());
    }

    private static int deleteIllegalItemsInInventory(Inventory inv, Player p) {
        int deletedItems = 0;
        for (ItemStack i : inv.getContents()) {
            if (isItemBanned(i)) {
                inv.remove(i);
                deletedItems++;
            }
        }
        if (deletedItems > 0 && p != null) {
            p.sendMessage(ChatColor.RED + "Removed " + ChatColor.BOLD + deletedItems + " illegal item" +
                    (deletedItems > 1 ? "s" : "") + ChatColor.RED + " from your realm container.");
        }
        return deletedItems;
    }

    private boolean isRealmInventory(Inventory inv) {
    	return inv.getName().equalsIgnoreCase("container.chest")
    			|| inv.getName().contains("Realm Chest")
    			|| inv.getName().equalsIgnoreCase("container.chestDouble")
                || inv.getName().equalsIgnoreCase("container.minecart")
                || inv.getName().equalsIgnoreCase("container.dispenser")
                || inv.getName().equalsIgnoreCase("container.hopper")
                || inv.getName().equalsIgnoreCase("container.dropper");
    }
    
    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Player p = (Player) event.getWhoClicked();

        if (!isRealmInventory(event.getInventory()))
            return;

        if (GameAPI.isMainWorld(p.getWorld()))
        	return;

        ItemStack cursor = event.getCursor();
        if (event.isShiftClick() || event.getAction() == InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD)
            cursor = event.getCurrentItem();
        if ((event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD || event.getAction() == InventoryAction.HOTBAR_SWAP) && event.getRawSlot() < event.getInventory().getSize())
            cursor = event.getView().getBottomInventory().getItem(event.getHotbarButton());

        if (event.getRawSlot() < event.getInventory().getSize() && (event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY && event.getAction() != InventoryAction.HOTBAR_SWAP && event.getAction() != InventoryAction.HOTBAR_MOVE_AND_READD && event.getAction() != InventoryAction.PLACE_ONE && event.getAction() != InventoryAction.PLACE_SOME && event.getAction() != InventoryAction.PLACE_ALL) && event.getRawSlot() != -999)
            return;

        if (cursor != null) {
            if (isItemBanned(cursor)) {
                event.setCancelled(true);
                event.setResult(Result.DENY);
                p.updateInventory();
                p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " deposit weapons, armor, or gems in realm blocks.");
                p.sendMessage(ChatColor.GRAY + "Deposit those items in your BANK CHEST.");
            }
        }
    }
    
    @EventHandler
    public void onDrag(InventoryDragEvent evt) {
    	if(isRealmInventory(evt.getInventory()) && isItemBanned(evt.getOldCursor()))
    		evt.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerEnterPortal(PlayerPortalEvent event) {

        if(event.getPlayer().hasMetadata("realmcd")) {
            Long timer = event.getPlayer().getMetadata("realmcd").get(0).asLong();
            if(System.currentTimeMillis() - timer <= TimeUnit.SECONDS.toMillis(3)) {
                event.setCancelled(true);
                return;
            }
        }


    	//Remove Pets
    	if (EntityAPI.hasPetOut(event.getPlayer().getUniqueId())) {
            Entity pet = EntityMechanics.PLAYER_PETS.get(event.getPlayer().getUniqueId());
            pet.dead = true;
            EntityAPI.removePlayerPetList(event.getPlayer().getUniqueId());
        }
    	
    	//Remove Mount
        if (EntityAPI.hasMountOut(event.getPlayer().getUniqueId())) {
            Entity mount = EntityMechanics.PLAYER_MOUNTS.get(event.getPlayer().getUniqueId());
            mount.dead = true;
            EntityAPI.removePlayerMountList(event.getPlayer().getUniqueId());
        }
        
        if (GameAPI.isMainWorld(event.getPlayer().getLocation())) {
        	// Player is entering a realm.
            if (DuelingMechanics.isDueling(event.getPlayer().getUniqueId())) {
                event.getPlayer().sendMessage(ChatColor.RED + "You cannot enter a realm while in a duel!");
                return;
            }

            if (CombatLog.isInCombat(event.getPlayer())) {
            	event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "You cannot enter a realm while in combat!");
            }
            
            // Gets the realm based on the location of the portal entered.
            Realm realm = Realms.getInstance().getRealm(event.getFrom());
            
            if(realm == null || !realm.isOpen())
            	return;
            
            // Saves their location so they don't spawn in the realm if they logout.
            DatabaseAPI.getInstance().update(event.getPlayer().getUniqueId(), EnumOperators.$SET, EnumData.CURRENT_LOCATION, GameAPI.locationToString(event.getFrom()), true);
            // Teleports them inside the realm.
            event.setTo(realm.getWorld().getSpawnLocation().clone().add(0, 2, 0));

            event.getPlayer().setMetadata("realmcd", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
            
        } else if (Realms.getInstance().isInRealm(event.getPlayer())) {
        	// Player is exiting a realm.
            if(event.getPlayer().hasMetadata("realmcd")) {
                Long timer = event.getPlayer().getMetadata("realmcd").get(0).asLong();
                if(System.currentTimeMillis() - timer <= TimeUnit.SECONDS.toMillis(3)) {
                    event.setCancelled(true);
                    return;
                }
            }

            event.getPlayer().setMetadata("realmcd", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
            Realm realm = Realms.getInstance().getRealm(event.getPlayer().getWorld());
            event.setTo(realm.getPortalLocation().clone().add(0, 1, 0));
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerUsePortalRune(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK
                || event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) return;
        Player p = event.getPlayer();
        if (p.getEquipment().getItemInMainHand() == null || p.getEquipment().getItemInMainHand().getType() != Material.NETHER_STAR)
            return;
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(p.getEquipment().getItemInMainHand());
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null) return;
        if (tag.hasKey("realmPortalRune") && !(tag.getString("realmPortalRune").equalsIgnoreCase("true"))) return;
        
        Realm realm = Realms.getInstance().getRealm(event.getPlayer().getWorld());
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {

            if (Cooldown.hasCooldown(event.getPlayer().getUniqueId()))
            	return;
            
            Cooldown.addCooldown(event.getPlayer().getUniqueId(), 1000);

            if (p.isSneaking()) {

                if (DungeonRealms.getInstance().getRebootTime() - System.currentTimeMillis() < 5 * 60 * 1000) {
                    p.sendMessage(ChatColor.RED + "This shard is rebooting in less than 5 minutes, so you cannot upgrade this realm on this shard.");
                    return;
                }

                if (realm == null || !realm.isOwner(p)) {
                    p.sendMessage(ChatColor.RED + "You must be inside your realm to modify its size.");
                    return;
                }
                
                if (realm.getTier().getTier() >= RealmTier.values().length) {
                    p.sendMessage(ChatColor.RED + "You have upgraded your realm to it's final tier");
                    return;
                }
                
                int upgradeCost = RealmTier.getByTier(realm.getTier().getTier() + 1).getPrice();

                p.sendMessage("");
                p.sendMessage(ChatColor.DARK_GRAY + "           *** " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Realm Upgrade Confirmation"
                        + ChatColor.DARK_GRAY + " ***");
                p.sendMessage(ChatColor.DARK_GRAY + "FROM Tier " + ChatColor.LIGHT_PURPLE + realm.getTier().getTier() + ChatColor.DARK_GRAY + " TO " + ChatColor.LIGHT_PURPLE
                        + (realm.getTier().getTier() + 1));
                p.sendMessage(ChatColor.DARK_GRAY + "Upgrade Cost: " + ChatColor.LIGHT_PURPLE + "" + upgradeCost + " Gem(s) from your bank");
                p.sendMessage("");
                p.sendMessage(ChatColor.GRAY + "Enter '" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "CONFIRM" + ChatColor.GRAY + "' to confirm realm upgrade.");
                p.sendMessage("");
                p.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "WARNING:" + ChatColor.RED + " Realm upgrades are " + ChatColor.BOLD + ChatColor.RED + "NOT"
                        + ChatColor.RED + " reversible or refundable. Type 'cancel' to void this upgrade request.");
                p.sendMessage("");
                
                final Realm r = realm;
                Chat.promptPlayerConfirmation(p, () -> {
                	int balance = (Integer) DatabaseAPI.getInstance().getData(EnumData.GEMS, p.getUniqueId());
                    
                    if (balance < upgradeCost) {
                        p.sendMessage(ChatColor.RED + "You do not have enough GEM(s) in your bank to purchase this upgrade. Upgrade cancelled.");
                        p.sendMessage(ChatColor.RED + "COST: " + upgradeCost + " gems");
                        return;
                    }
                    //Take Gems -> Upgrade Realm
                    DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$SET, EnumData.GEMS, balance - upgradeCost, true, doAfter -> 
                    	Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> r.upgradeRealm(p)));
                
                }, () -> p.sendMessage(ChatColor.RED + "Realm upgrade cancelled."));
                
                return;
            }

            if (realm != null && realm.isOwner(p)) {
                if (event.getClickedBlock() != null && event.getClickedBlock().getLocation() != null) {
                    Location newLocation = event.getClickedBlock().getLocation().clone().add(0, 2, 0);

                    if (GameAPI.isMaterialNearby(newLocation.clone().getBlock(), 3, Material.LADDER)
                    		|| GameAPI.isMaterialNearby(newLocation.clone().getBlock(), 5, Material.ENDER_CHEST)
                    		|| newLocation.getBlock().getType() != Material.AIR
                    		|| newLocation.clone().subtract(0, 1, 0).getBlock().getType() != Material.AIR) {
                        event.getPlayer().sendMessage(ChatColor.RED + "You cannot place a realm portal here!");
                        return;
                    }
                    
                    realm.setRealmSpawn(newLocation);
                    event.setCancelled(true);
                    return;
                } // Player is clicking air
            }

            //Open the player's realm.
            if (event.getClickedBlock() != null) {
            	realm = Realms.getInstance().getOrCreateRealm(event.getPlayer());
            	realm.openPortal(p, event.getClickedBlock().getLocation());
            }
            event.setCancelled(true);
        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {

        	if((event.hasBlock() && event.getClickedBlock().getType() == Material.PORTAL) || event.getPlayer().isSneaking())
        		return;

            if (!Realms.getInstance().isInRealm(event.getPlayer())) {
                event.getPlayer().sendMessage(ChatColor.RED + "You must be in your realm to open the realm material store.");
                event.setCancelled(true);
                return;
            }

            // OPENS STORE //
            RealmMaterialFactory.getInstance().openMaterialStore(p, 0);
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onWaterFlow(BlockFromToEvent event) {
        //Dont cancel in main world.
        if (GameAPI.isMainWorld(event.getBlock().getLocation())) return;
        
        event.setCancelled(true);
    }
}
