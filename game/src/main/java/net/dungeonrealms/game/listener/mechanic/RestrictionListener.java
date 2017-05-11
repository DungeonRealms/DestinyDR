package net.dungeonrealms.game.listener.mechanic;

import com.google.common.collect.Lists;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.util.CooldownProvider;
import net.dungeonrealms.database.PlayerToggles.Toggles;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import net.dungeonrealms.game.handler.EnergyHandler;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.dungeonrealms.game.mechanic.CrashDetector;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.duel.DuelOffer;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.shops.Shop;
import net.dungeonrealms.game.world.shops.ShopMechanics;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Kieran Quigley (Proxying) on 16-Jun-16.
 */
public class RestrictionListener implements Listener {

    private static CooldownProvider ANTI_COMMAND_SPAM = new CooldownProvider();
    
    public static boolean canPlayerUseItem(Player p, ItemStack item) {
    	if(!ItemGear.isCustomTool(item))
    		return true;
    	ItemGear gear = (ItemGear)PersistentItem.constructItem(item);
    	
    	if(!canPlayerUseTier(p, gear.getTier())) {
    		p.sendMessage(ChatColor.RED + "You must to be " + ChatColor.UNDERLINE + "at least" + ChatColor.RED + " level "
                    + gear.getTier().getLevelRequirement() + " to use this weapon.");
    		return false;
    	}
    	
    	return true;
    }

    public static boolean canPlayerUseTier(Player p, ItemTier tier) {
        return PlayerWrapper.getWrapper(p).getLevel() >= tier.getLevelRequirement();
    }

    //Illegal item check.
    private void checkForIllegalItems(Player p) {
        for (ItemStack is : p.getInventory().getContents()) {
            if (is == null || is.getType() == Material.AIR)
                continue;

            if (is.getType() == Material.SKULL_ITEM) {
                //Dragon skull.
                if (is.getDurability() == (short) 5 && !is.hasItemMeta()) {
                    p.getInventory().remove(is);
                    Bukkit.getLogger().info("Removed illegal Dragon head from " + p.getName());
                }

                continue;
            }

            if (!p.isOnline()) return;
            if (!is.hasItemMeta()) continue;
            if (!is.getItemMeta().hasLore()) continue;
            for (String s : is.getItemMeta().getLore()) {
                if (s.equals(ChatColor.GRAY + "Display Item")) {
                    p.getInventory().remove(is);
                    break;
                }
            }
        }
    }

    private void checkPlayersArmorIsValid(Player p) {
        boolean hadIllegalArmor = false;
        for (ItemStack is : p.getInventory().getArmorContents()) {
            if (is == null || is.getType() == Material.AIR || is.getType() == Material.SKULL_ITEM)
                continue;
         
            if (!p.isOnline())
            	return;
            
            if (!canPlayerUseItem(p, is)) {
                hadIllegalArmor = true;
                GameAPI.giveOrDropItem(p, GameAPI.removeArmor(p, is));
            }
        }
        if (hadIllegalArmor) {
            p.updateInventory();
            HealthHandler.updatePlayerHP(p);
            p.sendMessage(ChatColor.RED + "You were found with armor that is not wearable at your level.");
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();

        // Servers that can bypass these limits altogether.
        if (DungeonRealms.isMaster() || DungeonRealms.isSupport())
            return;

        // Commands that can bypass the cooldown restriction.
        switch (command.toLowerCase().substring(1).split(" ")[0]) {
            case "g": // Guild Chat
            case "gl": // Global Chat
            case "l": // Local Chat
            case "p":
            case "pchat": // Party Chat
            case "w":
            case "message":
            case "m":
            case "whisper":
            case "msg":
            case "tell":
            case "t": // Private Message
            case "r":
            case "reply": // Reply (to Private Message)
            case "pinvite": // Party Invite
            case "premove":
            case "pkick": // Party Kick
            case "pleave":
            case "pquit": // Party Leave
            case "ginvite": // Guild Invite
            case "gkick": // Guild Kick
            case "toggles":
            case "toggle": // Toggle Menu
            case "toggledebug":
            case "debug": // Toggle Debug
            case "togglechaos": // Toggle Chaos
            case "toggleglobalchat": // Toggle Global Chat
            case "togglepvp": // Toggle PvP
            case "toggletells":
            case "dnd": // Toggle (Non-Bud) PMs
            case "toggletrade": // Toggle Trading
            case "toggletradechat": // Toggle Trade Chat
            case "toggleduel": // Toggle Duel
            case "toggletips": // Toggle Tips
            case "staffchat":
            case "sc":
            case "s": // Staff Chat
            case "answer": // Answer
                return;
        }

        if (ANTI_COMMAND_SPAM.isCooldown(event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage(ChatColor.RED + "You can only execute a command every 5 seconds!");
            event.setCancelled(true);
            return;
        }

        if (!Rank.isTrialGM(event.getPlayer()) && !event.getPlayer().isOp())
            ANTI_COMMAND_SPAM.submitCooldown(event.getPlayer(), 5000L);

        if ((command.toLowerCase().startsWith("/me") || command.toLowerCase().startsWith("/minecraft:me")) && !Rank.isDev(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityHangingBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player) || 
        		(GameAPI.isMainWorld(event.getEntity().getWorld()) && !Rank.isTrialGM((Player) event.getRemover())))
        	event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getPlayer().getOpenInventory() == null) return;
        if (!GameAPI.isShop(event.getPlayer().getOpenInventory())) return;
        if (GameAPI.isInSafeRegion(event.getPlayer().getLocation())) {
            String ownerName = event.getPlayer().getOpenInventory().getTitle().split("@")[1];
            if (ownerName == null) return;
            Shop shop = ShopMechanics.getShop(ownerName);
            if (shop == null) return;
            event.setCancelled(true);
        } else {
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot access a shop in a " + ChatColor.BOLD + "CHAOTIC" + ChatColor.RED + " region!");
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();

        if (result.getType() == Material.WHEAT || result.getType() == Material.BREAD || result.getType() == Material.WOOD_SWORD || result.getType() == Material.BUCKET || result.getType() == Material.FURNACE
                || result.getType() == Material.ARMOR_STAND || result.getType() == Material.ENDER_CHEST || result.getType() == Material.SHIELD
                || result.getType() == Material.STONE_SWORD || result.getType() == Material.IRON_SWORD || result.getType() == Material.DIAMOND_SWORD
                || result.getType() == Material.GOLD_SWORD || result.getType() == Material.BOW || result.getType() == Material.WOOD_AXE
                || result.getType() == Material.STONE_AXE || result.getType() == Material.IRON_AXE || result.getType() == Material.DIAMOND_AXE
                || result.getType() == Material.GOLD_AXE || result.getType() == Material.WOOD_SPADE || result.getType() == Material.STONE_SPADE
                || result.getType() == Material.IRON_SPADE || result.getType() == Material.DIAMOND_SPADE || result.getType() == Material.GOLD_SPADE
                || result.getType() == Material.WOOD_PICKAXE || result.getType() == Material.STONE_PICKAXE || result.getType() == Material.IRON_PICKAXE
                || result.getType() == Material.DIAMOND_PICKAXE || result.getType() == Material.GOLD_PICKAXE || result.getType() == Material.WOOD_HOE
                || result.getType() == Material.STONE_HOE || result.getType() == Material.IRON_HOE || result.getType() == Material.DIAMOND_HOE
                || result.getType() == Material.GOLD_HOE || result.getType() == Material.LEATHER_HELMET || result.getType() == Material.LEATHER_CHESTPLATE
                || result.getType() == Material.LEATHER_LEGGINGS || result.getType() == Material.LEATHER_BOOTS || result.getType() == Material.CHAINMAIL_HELMET
                || result.getType() == Material.CHAINMAIL_CHESTPLATE || result.getType() == Material.CHAINMAIL_LEGGINGS
                || result.getType() == Material.CHAINMAIL_BOOTS || result.getType() == Material.IRON_HELMET || result.getType() == Material.IRON_CHESTPLATE
                || result.getType() == Material.IRON_LEGGINGS || result.getType() == Material.IRON_BOOTS || result.getType() == Material.DIAMOND_HELMET
                || result.getType() == Material.DIAMOND_CHESTPLATE || result.getType() == Material.DIAMOND_LEGGINGS
                || result.getType() == Material.DIAMOND_BOOTS || result.getType() == Material.GOLD_HELMET || result.getType() == Material.GOLD_CHESTPLATE
                || result.getType() == Material.GOLD_LEGGINGS || result.getType() == Material.GOLD_BOOTS || result.getType() == Material.EMERALD_BLOCK
                || result.getType() == Material.EMERALD || result.getType() == Material.PAPER || result.getType() == Material.ANVIL
                || result.getType() == Material.CHEST || result.getType() == Material.FURNACE || result.getType() == Material.BEACON
                || result.getType() == Material.JUKEBOX || result.getType() == Material.ITEM_FRAME || result.getType() == Material.HOPPER
                || result.getType() == Material.TRAPPED_CHEST || result.getType() == Material.DROPPER || result.getType() == Material.FISHING_ROD
                || result.getType() == Material.DISPENSER || result.getType() == Material.INK_SACK || result.getType() == Material.IRON_FENCE
                || result.getType() == Material.MAP || result.getType() == Material.EMPTY_MAP || result.getType() == Material.BOOK
                || result.getType() == Material.ENCHANTMENT_TABLE || result.getType() == Material.BREWING_STAND || result.getType() == Material.JUKEBOX
                || result.getType() == Material.RAILS || result.getType() == Material.ACTIVATOR_RAIL || result.getType() == Material.POWERED_RAIL
                || result.getType() == Material.MINECART || result.getType() == Material.GOLD_INGOT || result.getType() == Material.GOLD_ORE
                || result.getType() == Material.GOLDEN_APPLE || result.getType() == Material.STORAGE_MINECART || result.getType() == Material.PISTON_BASE
                || result.getType() == Material.PISTON_STICKY_BASE || result.getType() == Material.CARROT_STICK || result.getType() == Material.LEASH
                || result.getType() == Material.NAME_TAG || result.getTypeId() == 417 || result.getTypeId() == 418 || result.getTypeId() == 419 || result.getType().name().startsWith("BOAT")) {

            Player p = ((Player) event.getWhoClicked());
            if (p.isOp()) {
                return;
            }
            event.setCancelled(true);

            String item = result.getType().name();
            item = item.replaceAll("_", " ");
            item = item.replaceAll("WOOD", "WOODEN");

            item = item.substring(0, 1).toUpperCase() + item.substring(1, item.length()).toLowerCase();
            p.sendMessage(ChatColor.RED + "You cannot craft a(n) " + ChatColor.BOLD + item + ChatColor.RED + "");

        }
    }


    @EventHandler
    public void onCropGrowth(BlockGrowEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void playerWeaponSwitch(PlayerItemHeldEvent event) {
        Player p = event.getPlayer();
        ItemStack i = p.getInventory().getItem(event.getNewSlot());
        if (!ItemWeapon.isWeapon(i))
        	return;
        
        if(!canPlayerUseItem(p, i)) {
        	event.setCancelled(true);
        	p.updateInventory();
        	return;
        }
        
        // Play the noise.
        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.4F);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        checkPlayersArmorIsValid((Player) event.getPlayer());
        checkForIllegalItems((Player) event.getPlayer());

        if (event.getInventory() instanceof MerchantInventory) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        checkPlayersArmorIsValid((Player) event.getPlayer());
        checkForIllegalItems((Player) event.getPlayer());
    }

    // Level restrictions on equipment removed on 7/18/16 Build#131
    //Level restrictions added back on 2/2/2017
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            ItemStack held = player.getEquipment().getItemInMainHand();
            if(ItemWeapon.isWeapon(held)) {
            	if (!canPlayerUseItem(player, held)) {
                    event.setCancelled(true);
                    event.setDamage(0);
                    EnergyHandler.removeEnergyFromPlayerAndUpdate(player.getUniqueId(), 1F);
                }
            }
        }
    }

    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent event) {
        if ((event.getBucket() == Material.WATER_BUCKET || event.getBucket() == Material.WATER
                || event.getBucket() == Material.LAVA || event.getBucket() == Material.LAVA_BUCKET)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (event.getClickedBlock().getType() == Material.FIRE) {
                event.setCancelled(true);
                event.getClickedBlock().setType(Material.FIRE);
                return;
            }
        }


        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.hasBlock()) {
                if (event.getClickedBlock().getType() == Material.CAKE_BLOCK) {
                    event.setCancelled(true);
                    event.setUseInteractedBlock(Event.Result.DENY);
                }
            }
        }
    }

    @EventHandler
    public void playerClickHorse(InventoryClickEvent event) {
        final Player p = (Player) event.getWhoClicked();

        if (!p.isInsideVehicle()) return;
        if (!(p.getVehicle() instanceof Horse)) return;
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getType().toString().contains("BARDING") || event.getCurrentItem().getType() == Material.SADDLE) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerJoinEventDelayed(PlayerJoinEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            if (event.getPlayer() != null && event.getPlayer().isOnline())
                checkPlayersArmorIsValid(event.getPlayer());
        }, 150L);
    }

    private List<UUID> loggedOutCombat = Lists.newArrayList();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent event) {
        loggedOutCombat.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void shardingExtraSafetyCheckDrop(PlayerDropItemEvent event) {
        if (shouldBlock(event.getPlayer()))
            event.setCancelled(true);
    }

    /**
     * Prevents players from picking up items when they're sharding, can't pickup items, or if those items can't be picked up.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void checkPickup(PlayerPickupItemEvent event) {
    	Player p = event.getPlayer();
        GamePlayer gp = GameAPI.getGamePlayer(p);
        if ((gp != null && !gp.isAbleToDrop()) || shouldBlock(p) || Metadata.NO_PICKUP.has(event.getItem()))
            event.setCancelled(true);
    }
    
    /**
     * Prevents items that can't be picked up from going into hoppers.
     */
    @EventHandler
    public void onHopperPickup(InventoryPickupItemEvent event) {
        if(event.getItem() != null && Metadata.NO_PICKUP.has(event.getItem()))
            event.setCancelled(true);
    }
    
    private boolean shouldBlock(Player p) {
    	return Metadata.SHARDING.has(p) || DungeonRealms.getInstance().getLoggingOut().contains(p.getName()) || CrashDetector.crashDetected;
    }

    @EventHandler
    public void onEntityTargetUntargettablePlayer(EntityTargetLivingEntityEvent event) {
        if (!GameAPI.isPlayer(event.getTarget())) return;
        PlayerWrapper pw = PlayerWrapper.getWrapper((Player) event.getTarget());
        GamePlayer gamePlayer = GameAPI.getGamePlayer((Player) event.getTarget());
        if (gamePlayer == null) return;
        if (Metadata.TIER.has(event.getEntity())) {
            if (GameAPI.getTierFromLevel(pw.getLevel()) > EntityAPI.getTier(event.getEntity()) + 2) {
                if (!CombatLog.isInCombat((Player) event.getTarget()) && event.getTarget().getWorld().equals(Bukkit.getWorlds().get(0))) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        if (gamePlayer.isTargettable()) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInvulnerablePlayerDamage(EntityDamageEvent event) {
        if (!GameAPI.isPlayer(event.getEntity())) return;
        if (GameAPI.getGamePlayer((Player) event.getEntity()) == null) return;
        if (!GameAPI.getGamePlayer((Player) event.getEntity()).isInvulnerable()) return;

        event.setDamage(0);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAttackHorse(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Horse)) return;
        if (GameAPI.isInSafeRegion(event.getEntity().getLocation()) && (event.getDamager() instanceof Player || event.getDamager() instanceof Projectile))
            return;
        Horse horse = (Horse) event.getEntity();
        LivingEntity passenger = (LivingEntity) horse.getPassenger();
        horse.eject();
        if (passenger != null) Bukkit.getServer().getPluginManager().callEvent(new VehicleExitEvent(horse, passenger));
    }

    /**
     * Checks if a player can be damaged and if they can damage.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onAttemptAttackEntity(EntityDamageByEntityEvent event) {
    	if (!(event.getEntity() instanceof LivingEntity))
    		return;
    	
        Entity damager = event.getDamager();
        Entity receiver = event.getEntity();
        boolean isAttackerPlayer = false;
        boolean isDefenderPlayer = false;
        Player pDamager = null;
        Player pReceiver = null;

        if (GameAPI.isPlayer(damager)) {
            isAttackerPlayer = true;
            pDamager = (Player) event.getDamager();
        } else if ((damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Player && (DamageAPI.isStaffProjectile(damager) ||
                DamageAPI.isBowProjectile(damager)))) {
            isAttackerPlayer = true;
            pDamager = (Player) ((Projectile) damager).getShooter();
        }

        if (GameAPI.isPlayer(receiver)) {
            isDefenderPlayer = true;
            pReceiver = (Player) event.getEntity();
        }
        if (!isAttackerPlayer && !isDefenderPlayer) {
            return;
        }

        if (!isAttackerPlayer || !isDefenderPlayer) {
            if (GameAPI.isInSafeRegion(damager.getLocation()) || GameAPI.isInSafeRegion(receiver.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }

        if (isAttackerPlayer) {
            if (GameAPI.getGamePlayer(pDamager) == null) {
                event.setCancelled(true);
                event.setDamage(0);
                pDamager.updateInventory();
                return;
            }
            if (pDamager.hasMetadata("last_Attack")) {
                if (System.currentTimeMillis() - pDamager.getMetadata("last_Attack").get(0).asLong() < 80) {
                    event.setCancelled(true);
                    event.setDamage(0);
                    pDamager.updateInventory();
                    return;
                }
            }
            pDamager.setMetadata("last_Attack", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
            if (pDamager.hasPotionEffect(PotionEffectType.SLOW_DIGGING) || EnergyHandler.getPlayerCurrentEnergy(pDamager) <= 0) {
                event.setCancelled(true);
                event.setDamage(0);
                pDamager.playSound(pDamager.getLocation(), Sound.ENTITY_WOLF_PANT, 12F, 1.5F);
                pDamager.updateInventory();
                try {
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CRIT, event.getEntity().getLocation().add(0, 1, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.75F, 40);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return;
            }
        }

        if (isDefenderPlayer) {
            if (GameAPI.getGamePlayer(pReceiver) == null) {
                event.setCancelled(true);
                event.setDamage(0);
                pReceiver.updateInventory();
                return;
            }
            if (GameAPI.getGamePlayer(pReceiver).isInvulnerable() || !GameAPI.getGamePlayer(pReceiver).isTargettable()) {
                event.setCancelled(true);
                event.setDamage(0);
                return;
            }
        }

        if (isAttackerPlayer && isDefenderPlayer) {
            if (GameAPI.isNonPvPRegion(pDamager.getLocation()) || GameAPI.isNonPvPRegion(pReceiver.getLocation())) {
                if (DuelingMechanics.isDueling(pDamager.getUniqueId())) { //TODO: Check if you can attack players that are dueling.
                    if (DuelingMechanics.isDueling(pReceiver.getUniqueId())) {

                        if (!DuelingMechanics.isDuelPartner(pDamager.getUniqueId(), pReceiver.getUniqueId())) {
                            event.setDamage(0);
                            event.setCancelled(true);
                            pDamager.updateInventory();
                            pReceiver.updateInventory();
                        } else {
                            DuelOffer offer = DuelingMechanics.getOffer(pDamager.getUniqueId());
                            if (offer != null && !offer.canFight) {
                                event.setCancelled(true);
                                event.setDamage(0D);
                                return;
                            }
                        }
                    } else {
                        event.setCancelled(true);
                        event.setDamage(0);
                    }
                } else {
                    event.setDamage(0);
                    event.setCancelled(true);
                    pDamager.updateInventory();
                    pReceiver.updateInventory();
                }
                return;
            }

            PlayerWrapper damagerWrapper = PlayerWrapper.getPlayerWrapper(pDamager);
            if (!damagerWrapper.getToggles().getState(Toggles.PVP)) {
            	damagerWrapper.sendDebug(ChatColor.YELLOW + "You have toggle PvP disabled. You currently cannot attack players.");
                event.setCancelled(true);
                event.setDamage(0);
                pDamager.updateInventory();
                pReceiver.updateInventory();
                return;
            }

            if (Affair.areInSameParty(pDamager, pReceiver)) {
                event.setCancelled(true);
                event.setDamage(0);
                pDamager.updateInventory();
                pReceiver.updateInventory();
                return;
            }
            
            if (GuildDatabase.getAPI().areInSameGuild(pDamager, pReceiver)) {
                event.setCancelled(true);
                event.setDamage(0);
                pDamager.updateInventory();
                pReceiver.updateInventory();
            }
        }
    }
}
