package net.dungeonrealms.listeners;

import com.connorlinfoot.bountifulapi.BountifulAPI;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.chat.Chat;
import net.dungeonrealms.core.Callback;
import net.dungeonrealms.core.CoreAPI;
import net.dungeonrealms.core.reply.BanReply;
import net.dungeonrealms.core.reply.ProxyReply;
import net.dungeonrealms.donate.DonationEffects;
import net.dungeonrealms.duel.DuelMechanics;
import net.dungeonrealms.duel.DuelWager;
import net.dungeonrealms.entities.utils.EntityAPI;
import net.dungeonrealms.handlers.KarmaHandler;
import net.dungeonrealms.handlers.TradeHandler;
import net.dungeonrealms.inventory.GUI;
import net.dungeonrealms.inventory.NPCMenus;
import net.dungeonrealms.mastery.GamePlayer;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.WebAPI;
import net.dungeonrealms.mongo.DatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;
import java.util.Random;

/**
 * Created by Nick on 9/17/2015.
 */
public class MainListener implements Listener {

    /**
     * Monitors and checks the players language.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onChat(AsyncPlayerChatEvent event) {
        Chat.getInstance().doChat(event);
    }

    /**
     * This event is used for the Database.
     *
     * @param event the event.
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onAsyncJoin(AsyncPlayerPreLoginEvent event) {
        CoreAPI.getInstance().findBan(event.getName(), new Callback<BanReply>(BanReply.class) {
            @Override
            public void callback(Throwable failCause, BanReply result) {
                switch (result.getResult()) {
                    case YES:
                        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ChatColor.RED + "You are banned!" + ChatColor.BLUE + " Reason: " + ChatColor.AQUA + result.getReason().getName());
                        break;
                    case NO:
                        DatabaseAPI.getInstance().requestPlayer(event.getUniqueId());
                        break;
                    case TEMP_BANNED:
                        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "You are temp banned for: " + result.getReason());
                        break;
                    default:
                        Utils.log.warning("[BAN] [ASYNC] Unable to parse data from findBan() in onAsyncJoin().");
                }
            }
        });
        CoreAPI.getInstance().isProxying(event.getAddress(), new Callback<ProxyReply>(ProxyReply.class) {
            @Override
            public void callback(Throwable failCause, ProxyReply result) {
                switch (result.getResult()) {
                    case YES:
                        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ChatColor.RED + "Socks 4/5 Proxies & VPNs are NOT allowed!");
                        break;
                    case NO:
                        break;
                }
            }
        });
    }


    /**
     * This event is the main event once the player has actually entered the
     * world! It is now safe to do things to the player e.g BountifulAPI or
     * adding PotionEffects.. etc..
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (WebAPI.ANNOUNCEMENTS != null && WebAPI.ANNOUNCEMENTS.size() > 0) {
            Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
                for (Map.Entry<String, Integer> e : WebAPI.ANNOUNCEMENTS.entrySet()) {
                    BountifulAPI.sendTitle(player, 1, e.getValue(), 1, ChatColor.translateAlternateColorCodes('&', e.getKey().split("@")[0]),
                            ChatColor.translateAlternateColorCodes('&', e.getKey().split("@")[1].split(",")[0]));
                }
            }, 5l);
        }
        for (String s : WebAPI.JOIN_INFORMATION) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
        }
        player.getInventory().clear();
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> API.handleLogin(player.getUniqueId()), 20L);
    }

    /**
     * Cancel spawning unless it's CUSTOM. So we don't have RANDOM SHEEP. We
     * have.. CUSTOM SHEEP. RAWR SHEEP EAT ME>.. AH RUN!
     *
     * @param event
     * @WARNING: THIS EVENT IS VERY INTENSIVE!
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            event.setCancelled(true);
        }
    }

    /**
     * Makes sure to despawn mounts on dismount and remove from hashmap
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMountDismount(VehicleExitEvent event) {
        if (!(API.isPlayer(event.getExited()))) return;
        if (EntityAPI.hasMountOut(event.getExited().getUniqueId())) {
            if (event.getVehicle().hasMetadata("type")) {
                String metaValue = event.getVehicle().getMetadata("type").get(0).asString();
                if (metaValue.equalsIgnoreCase("mount")) {
                    event.getVehicle().remove();
                    EntityAPI.removePlayerMountList(event.getExited().getUniqueId());
                    event.getExited().sendMessage("For it's own safety, your mount has returned to the stable.");
                }
            }
        }
    }

    /**
     * Handles player leaving the server
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (EntityAPI.hasPetOut(player.getUniqueId())) {
            net.minecraft.server.v1_8_R3.Entity playerPet = EntityAPI.getPlayerPet(player.getUniqueId());
            if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(playerPet)) {
                DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(playerPet);
            }
            if (playerPet.isAlive()) { // Safety check
                playerPet.dead = true;
            }
            //.damageEntity(DamageSource.GENERIC, 20);
            EntityAPI.removePlayerPetList(player.getUniqueId());
        }

        if (EntityAPI.hasMountOut(player.getUniqueId())) {
            net.minecraft.server.v1_8_R3.Entity playerMount = EntityAPI.getPlayerMount(player.getUniqueId());
            if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(playerMount)) {
                DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(playerMount);
            }
            if (playerMount.isAlive()) { // Safety check
                if (playerMount.passenger != null) {
                    playerMount.passenger = null;
                }
                playerMount.dead = true;
            }
            EntityAPI.removePlayerMountList(player.getUniqueId());
        }

        // Player leaves while in duel
        if (DuelMechanics.isDueling(player.getUniqueId())) {
            DuelMechanics.getWager(player.getUniqueId()).handleLogOut(player.getUniqueId());
        }
        API.handleLogout(player.getUniqueId());
    }

    /**
     * Handles players bringing up commands for a specific player.
     *
     * @param theevent
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerShiftRightClickPlayer(PlayerInteractEntityEvent theevent) {
        if (API.isPlayer(theevent.getRightClicked())) {
            if (theevent.getPlayer().isSneaking()) {
                Player playerClicked = (Player) theevent.getRightClicked();
                GUI gui = new GUI(playerClicked.getName(), 27, event -> {
                    if (event.getPosition() < 27) {
                        int slot = event.getPosition();
                        if (event.getInventory().getItem(slot) != null && event.getInventory().getItem(slot).getType() != Material.AIR) {
                            ItemStack item = event.getInventory().getItem(slot);
                            // Duel Request
                            if (item.getType() == Material.IRON_SWORD) {
                                event.setWillClose(true);
                                event.setWillDestroy(true);
                                theevent.getPlayer().closeInventory();
                                Player p1 = theevent.getPlayer();
                                Player p2 = playerClicked;
                                if (API.isInSafeRegion(p1.getLocation()) && API.isInSafeRegion(p2.getLocation())) {
                                    if (DuelMechanics.isDueling(p2.getUniqueId())) {
                                        return;
                                    } else {
                                        if (DuelMechanics.isPendingDuel(p1.getUniqueId())) {
                                            if (DuelMechanics.isPendingDuelPartner(p1.getUniqueId(), p2.getUniqueId())) {
                                                Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> DuelMechanics.launchWager(p1, p2), 10l);
                                                // Remove from pending
//						                        DuelMechanics.cancelRequestedDuel(p1.getUniqueId());
                                            } else {
                                                if (!DuelMechanics.isOnCooldown(p1.getUniqueId())) {
                                                    DuelMechanics.cancelRequestedDuel(p1.getUniqueId());
                                                    DuelMechanics.sendDuelRequest(p1.getUniqueId(), p2.getUniqueId());
                                                } else {
                                                    p1.sendMessage(ChatColor.RED + "You must wait to send another Duel Request");
                                                }

                                            }
                                        } else {
                                            if (DuelMechanics.isOnCooldown(p1.getUniqueId())) {
                                                p1.sendMessage(ChatColor.RED + "You must wait to send another Duel Request");
                                                return;
                                            }
                                            if (DuelMechanics.isPendingDuel(p2.getUniqueId())) {
                                                DuelMechanics.cancelRequestedDuel(p2.getUniqueId());
                                            }
                                            DuelMechanics.sendDuelRequest(p1.getUniqueId(), p2.getUniqueId());
                                        }
                                    }
                                }
                            } else if (item.getType() == Material.EMERALD) {
                                event.setWillClose(true);
                                event.setWillDestroy(true);
                                event.willDestroy();
                                TradeHandler trade = new TradeHandler(theevent.getPlayer(), playerClicked);
                                Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> trade.launchTradeWindow(), 10l);
                            } else if (item.getType() == Material.PAPER) {
                                theevent.getPlayer().closeInventory();
                                theevent.getPlayer().chat("/tell " + playerClicked.getName() + " ");
                            }
                        }
                    }
                }, DungeonRealms.getInstance());

                gui.setOption(4, Utils.getPlayerHead(playerClicked), ChatColor.AQUA.toString() + playerClicked.getName(), new String[]{});
                gui.setOption(8, new ItemStack(Material.IRON_SWORD), "Challenge to duel", new String[]{"Challenges " + playerClicked.getName() + " to a battle!"});
                gui.setOption(17, new ItemStack(Material.PAPER), "Private Message", new String[]{"Privately message " + playerClicked.getName()});
                gui.setOption(26, new ItemStack(Material.EMERALD), "Trade", new String[]{"Send a trade request to " + playerClicked.getName()});
                gui.open(theevent.getPlayer());
            }
        }
    }


    /**
     * Handling Duels. When a player punches another player.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void playerPunchPlayer(EntityDamageByEntityEvent event) {
        if (!(API.isPlayer(event.getEntity()) && API.isPlayer(event.getDamager()))) return;
        Player p1 = (Player) event.getDamager();
        Player p2 = (Player) event.getEntity();
        if (API.isInSafeRegion(p1.getLocation()) && API.isInSafeRegion(p2.getLocation())) {
            if (DuelMechanics.isDueling(p2.getUniqueId())) {
                // If player they're punching is their duel partner
                if (DuelMechanics.isDuelPartner(p1.getUniqueId(), p2.getUniqueId())) {
                    if (p2.getHealth() - event.getDamage() <= 0) {
                        // if they're gonna die this hit end duel
                        DuelWager wager = DuelMechanics.getWager(p1.getUniqueId());
                        if (wager != null) {
                            event.setCancelled(true);
                            p2.setHealth(0.5);
                            wager.endDuel(p1, p2);
                        }
                    }
                } else
                    p1.sendMessage("That's not you're dueling partner!");
            }
        }

    }

    /**
     * Checks player movement, adds a trail of gold blocks if they have
     * the perk and the situation is correct.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!API.isPlayer(event.getPlayer())) return;
        if (!(DonationEffects.getInstance().PLAYER_GOLD_BLOCK_TRAILS.contains(event.getPlayer()))) return;
        Player player = event.getPlayer();
        if (!(player.getWorld().getName().equalsIgnoreCase(Bukkit.getWorlds().get(0).getName()))) return;
        if (player.getLocation().getBlock().getType() != Material.AIR) return;
        Material material = player.getLocation().subtract(0, 1, 0).getBlock().getType();
        if (material == Material.DIRT || material == Material.GRASS || material == Material.STONE || material == Material.COBBLESTONE || material == Material.GRAVEL
                || material == Material.LOG || material == Material.SMOOTH_BRICK || material == Material.BEDROCK || material == Material.GLASS
                || material == Material.SANDSTONE || material == Material.SAND || material == Material.BOOKSHELF || material == Material.MOSSY_COBBLESTONE || material == Material.OBSIDIAN
                || material == Material.SNOW_BLOCK || material == Material.CLAY || material == Material.STAINED_CLAY || material == Material.WOOL) {
            DonationEffects.getInstance().PLAYER_GOLD_BLOCK_TRAIL_INFO.put(player.getLocation().subtract(0, 1, 0).getBlock().getLocation(), material);
            player.getLocation().subtract(0, 1, 0).getBlock().setType(Material.GOLD_BLOCK);
            player.getLocation().subtract(0, 1, 0).getBlock().setMetadata("time", new FixedMetadataValue(DungeonRealms.getInstance(), 10));
        }
    }

    /**
     * Checks player movement, if they are chaotic and
     * entering or currently in a Non-PvP zone, remove
     * them from it.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMoveWhileChaotic(PlayerMoveEvent event) {
        if (!API.isPlayer(event.getPlayer())) return;
        Player player = event.getPlayer();
        if (new GamePlayer(player).getPlayerAlignment() != KarmaHandler.EnumPlayerAlignments.CHAOTIC) return;
        if (!(player.getWorld().getName().equalsIgnoreCase(Bukkit.getWorlds().get(0).getName()))) return;
        if (API.isInSafeRegion(event.getFrom()) && !API.isInSafeRegion(event.getTo())) {
            player.teleport(KarmaHandler.CHAOTIC_RESPAWNS.get(new Random().nextInt(KarmaHandler.CHAOTIC_RESPAWNS.size() - 1)));
            player.sendMessage(ChatColor.RED + "The guards have kicked you out of the " + ChatColor.UNDERLINE + "protected area" + ChatColor.RED + " due to your Chaotic alignment.");
        }
        if (API.isInSafeRegion(event.getTo())) {
            //Might not cancel it as it could look buggy. May have to force TP to a Chaotic spawn from KarmaHandler.
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " enter " + ChatColor.BOLD.toString() + "NON-PVP" + ChatColor.RED + " zones with a Chaotic alignment.");
        }
    }

    /**
     * Checks for player interacting with
     * NPC Players, opens an inventory
     * if they have one.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void playerInteractWithNPC(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) return;
        if (API.isPlayer(event.getRightClicked())) return;
        if (event.getRightClicked().getName().equalsIgnoreCase("")) return;
        if (event.getRightClicked().getName().equalsIgnoreCase("Animal Tamer")) {
            NPCMenus.openMountPurchaseMenu(event.getPlayer());
        }
        if (event.getRightClicked().getName().equalsIgnoreCase("Merchant")) {
            //TODO: Open Merchant Menu
        }
        if (event.getRightClicked().getName().equalsIgnoreCase("E-Cash Vendor")) {
            //TODO: Open E-Cash Menu
        }
        if (event.getRightClicked().getName().equalsIgnoreCase("Wizard")) {
            //TODO: Open Attributes Reset Menu
        }
        if (event.getRightClicked().getName().equalsIgnoreCase("Dungeoneer")) {
            //TODO: Open Dungeoneer Menu
        }
        if (event.getRightClicked().getName().equalsIgnoreCase("Skill Trainer")) {
            //TODO: Open Profession Item Menu
        }
        if (event.getRightClicked().getName().equalsIgnoreCase("Food Vendor")) {
            //TODO: Open Food Menu
        }
        if (event.getRightClicked().getName().equalsIgnoreCase("Item Vendor")) {
            //TODO: Open Item Vendor
        }
        if (event.getRightClicked().getName().equalsIgnoreCase("Guild Registrar")) {
            //TODO: Open Guild Registrar
        }
        if (event.getRightClicked().getName().equalsIgnoreCase("Innkeeper")) {
            //TODO: Open Hearthstone Guy (Anvil API)
        }
    }
}
