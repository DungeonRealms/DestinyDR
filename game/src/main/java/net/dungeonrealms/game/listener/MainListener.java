package net.dungeonrealms.game.listener;

import com.google.common.collect.Lists;
import com.vexsoftware.votifier.model.VotifierEvent;
import io.netty.util.internal.ConcurrentSet;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.game.util.Cooldown;
import net.dungeonrealms.database.PlayerToggles.Toggles;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;
import net.dungeonrealms.game.command.moderation.CommandMobDebug;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.event.PlayerEnterRegionEvent;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.item.items.functional.ItemGemNote;
import net.dungeonrealms.game.item.items.functional.ItemOrb;
import net.dungeonrealms.game.mastery.DamageTracker;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.CrashDetector;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.duel.DuelOffer;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.player.inventory.NPCMenus;
import net.dungeonrealms.game.player.inventory.menus.guis.SalesManagerGUI;
import net.dungeonrealms.game.player.trade.Trade;
import net.dungeonrealms.game.player.trade.TradeManager;
import net.dungeonrealms.game.title.TitleAPI;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import net.dungeonrealms.game.world.shops.SoldShopItem;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import net.minecraft.server.v1_9_R2.EntityArmorStand;
import net.minecraft.server.v1_9_R2.PacketPlayOutMount;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityUnleashEvent.UnleashReason;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Nick on 9/17/2015.
 */
public class MainListener implements Listener {

    private static List<String> alwaysCancel = Lists.newArrayList("Sales Manager");
    private Set<UUID> kickedIgnore = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        GameAPI.runAsSpectators(event.getPlayer(), p -> p.sendMessage(ChatColor.RED + event.getPlayer().getName() + "> " + event.getMessage()));

        if (Metadata.SHARDING.has(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot perform commands whilst sharding!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(EntityTeleportEvent event) {
        if (event.getEntity().getType() == EntityType.ENDERMAN) {

            MetadataValue value = MetadataUtils.Metadata.ENTITY_TYPE.get(event.getEntity());
            if (value == null) System.out.println("The value is null on enderman teleport!");
            else System.out.println("The value on enderman teleport is: " + value.asString());
            if (value != null && value.asString().equalsIgnoreCase("pet")) return;

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onVote(VotifierEvent event) {
        // No votes on the event shard.
        if (!DungeonRealms.isEvent())
            GameAPI.announceVote(Bukkit.getPlayer(event.getVote().getUsername()));
    }

    @EventHandler
    public void onPing(ServerListPingEvent event) {
        event.setMotd(DungeonRealms.getInstance().canAcceptPlayers() ? DungeonRealms.getShard().getShardID() + "," + GameAPI.getServerLoad() + ChatColor.RESET + "," + Constants.BUILD_NUMBER : "offline");
    }

    @EventHandler
    public void onPlayerChatTabCompleteEvent(PlayerChatTabCompleteEvent e) {

        if ((e.getChatMessage().startsWith("/") && !Rank.isTrialGM(e.getPlayer())) || e.getChatMessage().startsWith("@")) {
            e.getTabCompletions().clear();
            return;
        }

        if (e.getChatMessage().length() > 200)
            return;

        int index = e.getChatMessage().indexOf("/");
        if (index > 0 && index < 3 && Rank.isTrialGM(e.getPlayer())) {
            e.getPlayer().sendMessage(ChatColor.RED + "Woah there! You sure you want to send that in global?");
            return;
        }

        if (index != 0) {
            Chat.sendChatMessage(e.getPlayer(), e.getChatMessage(), true);
            e.getPlayer().closeInventory(); // Closes the chat after it grabs it!
        }
    }

    /**
     * Monitors and checks the players language.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onChat(AsyncPlayerChatEvent event) {
        if (GameAPI.getGamePlayer(event.getPlayer()) == null) { // server is restarting
            event.setCancelled(true);
            return;
        }

        if (Chat.containsIllegal(event.getMessage())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Message contains illegal characters.");
            return;
        }

        Chat.getInstance().doMessageChatListener(event);

        //Check if we cancelled it..
        if (!event.isCancelled()) {
            Chat.sendChatMessage(event.getPlayer(), event.getMessage(), false);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void worldInit(org.bukkit.event.world.WorldInitEvent e) {
        e.getWorld().setKeepSpawnInMemory(false);
    }

    /**
     * This event is the main event once the player has actually entered the
     * world! It is now safe to do things to the player e.g TitleAPI or
     * adding PotionEffects.. etc..
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();
        player.removeMetadata("saved", DungeonRealms.getInstance());

        //GameAPI.SAVE_DATA_COOLDOWN.submitCooldown(player, 2000L);
        TitleAPI.sendTitle(player, 0, 0, 0, "", "");

        CombatLog.checkCombatLog(player.getUniqueId());
        try {
            GameAPI.handleLogin(player);
        } catch (Exception e) {
            player.kickPlayer(ChatColor.RED + "There was an error loading your character. Staff have been notified.");
            PlayerWrapper.getPlayerWrappers().remove(player.getUniqueId());
//            GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED + "[ALERT] " + ChatColor.WHITE + "There was an error loading " + ChatColor.GOLD + player.getName() + "'s " + ChatColor.WHITE + "data on " + DungeonRealms.getShard().getShardID() + ".");
            e.printStackTrace();
            return;
        }

        GameAPI.asyncTracker.add(player);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            if (player.isOnline()) {
                PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                if (wrapper == null) return;
                if (wrapper.isLoggerDied()) {
                    player.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "You logged out while in combat, your doppelganger was killed and alas your items are gone.");
                    wrapper.setLoggerDied(false);
                    ItemManager.giveStarter(player);
                }
            }
        });


    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoinWhitelistedShard(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() == Result.KICK_WHITELIST)
            event.setKickMessage(ChatColor.AQUA + "This DungeonRealms shard is on " + ChatColor.UNDERLINE +
                    "maintenance" + ChatColor.AQUA + " mode. Only authorized users can join");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDropEvent(PlayerDropItemEvent event) {
        GamePlayer gp = GameAPI.getGamePlayer(event.getPlayer());
        if (gp != null && !gp.isAbleToDrop())
            event.setCancelled(true);
    }

    /**
     * Prevent vanilla mob spawns.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM)
            event.setCancelled(true);
    }

    /**
     * Makes sure to despawn mounts on dismount.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMountDismount(VehicleExitEvent event) {
        if (!GameAPI.isPlayer(event.getExited())) {
            if (event.getExited() instanceof EntityArmorStand)
                event.getExited().remove();
            return;
        }

        Player p = (Player) event.getExited();
        if (MountUtils.hasActiveMount(p)) {
            event.getVehicle().remove();
            MountUtils.removeMount(p);
            event.getExited().sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "For its own safety, your mount has returned to the stable.");
        }
    }

    /**
     * Handles a player getting kicked from the server.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        event.setLeaveMessage(null);
        this.kickedIgnore.add(event.getPlayer().getUniqueId());
        onDisconnect(event.getPlayer(), !event.getReason().contains("Appeal at: www.dungeonrealms.net"));
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL && Rank.isSUB(event.getPlayer()))
            event.allow();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        GameAPI.asyncTracker.remove(event.getPlayer());
        onDisconnect(event.getPlayer(), true);

        //Send this logout to the lobby / master server..
        if (!DungeonRealms.getInstance().isAlmostRestarting() && !CrashDetector.crashDetected) {
            Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> GameAPI.sendNetworkMessage("playerLogout", event.getPlayer().getUniqueId().toString(), 30 + ""));
        }
    }

    public static volatile Set<UUID> savedAfterSharding = new ConcurrentSet<>();
    public static volatile Set<UUID> savedOnLogout = new ConcurrentSet<>();


    private void onDisconnect(Player player, boolean performChecks) {

        if (GameAPI.IGNORE_QUIT_EVENT.contains(player.getUniqueId())) {
            //Still remove this shit..
            GameAPI.IGNORE_QUIT_EVENT.remove(player.getUniqueId());
            if (!Metadata.SHARDING.has(player)) {
                Utils.log.info("Ignored quit event for player " + player.getName());
                return;
            }
        }

        if (savedAfterSharding.contains(player.getUniqueId())) {
            //Dont save, was already saved through the shard process.
            Utils.log.info("Not re-saving data for " + player.getName() + " because /shard already saved it correctly.");
            return;
        }

        if (performChecks) {
            boolean ignoreCombat = this.kickedIgnore.remove(player.getUniqueId());
            // Handle combat log before data save so we overwrite the logger's inventory data
            if (CombatLog.inPVP(player) && !ignoreCombat) {
                // Woo oh, he logged out in PVP
                player.getWorld().strikeLightningEffect(player.getLocation());
                player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SCREAM, 5f, 1f);

                CombatLog.getInstance().handleCombatLog(player);

                // Remove from pvplog
                CombatLog.removeFromPVP(player);
            }

            // Player leaves while in duel
            DuelOffer offer = DuelingMechanics.getOffer(player.getUniqueId());
            if (offer != null)
                offer.handleLogOut(player);
        }

        player.updateInventory();
        // Good to go lads

        //So only remove tehm from the list on logout..
        GameAPI.handleLogout(player, true, savedData -> {
            PlayerWrapper.getPlayerWrappers().remove(player.getUniqueId());
            GameAPI.GAMEPLAYERS.remove(player.getName());
        }, false);
    }

    /**
     * Checks player movement, adds a trail of gold blocks if they have the perk
     * and the situation is correct.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!GameAPI.isPlayer(event.getPlayer()))
            return;

        //Only check blocks that change to save on cpu checks.
        if (event.getTo().getBlock() != event.getFrom().getBlock()) {
            DuelOffer offer = DuelingMechanics.getOffer(event.getPlayer().getUniqueId());
            if (offer != null) {
                Player player = event.getPlayer();
//                if (!offer.canFight) return;
                if (event.getTo().getWorld() != offer.getCenterPoint().getWorld()) {
                    Player winner = offer.player1 == player.getUniqueId() ? offer.getPlayer2() : offer.getPlayer1();
//                                Player loser = offer.player1 == player.getUniqueId() ?  : offer.getPlayer1();
                    offer.endDuel(winner, player);
                    return;
                }

                if (event.getTo().distanceSquared(offer.centerPoint) >= 300) {
                    event.setCancelled(true);
                    player.teleport(event.getFrom());
                    player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "WARNING:" + ChatColor.RED
                            + " You are too far from the DUEL START POINT, please turn back or you will "
                            + ChatColor.UNDERLINE + "FORFEIT.");

                    Integer attempts = offer.getLeaveAttempts().get(player.getUniqueId());
                    if (attempts != null) {

                        long lastAttempt = player.hasMetadata("duel_leave_attempt") ? player.getMetadata("duel_leave_attempt").get(0).asLong() : 0;

                        if (System.currentTimeMillis() - lastAttempt <= 5000) {
                            //Trying within 5 seconds.
                            if (attempts >= 5) {
                                //NO
                                Player winner = offer.player1 == player.getUniqueId() ? offer.getPlayer2() : offer.getPlayer1();
//                                Player loser = offer.player1 == player.getUniqueId() ?  : offer.getPlayer1();
                                offer.endDuel(winner, player);
                                player.sendMessage(ChatColor.RED + "You attempted to leave the duel area and forfeit the duel.");
                            }
                        } else {
                            attempts = 0;
                        }

                        offer.getLeaveAttempts().put(player.getUniqueId(), ++attempts);
                    } else {
                        offer.getLeaveAttempts().put(player.getUniqueId(), 1);
                    }

                    player.setMetadata("duel_leave_attempt", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
                }
            }
        }

        Player player = event.getPlayer();
        ParticleAPI.ParticleEffect effect = DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.get(player);
        if (effect == null || effect != ParticleAPI.ParticleEffect.GOLD_BLOCK) return;
        if (!player.getWorld().equals(Bukkit.getWorlds().get(0)) || player.getLocation().getBlock().getType() != Material.AIR)
            return;
        Material material = player.getLocation().subtract(0, 1, 0).getBlock().getType();
        if (DonationEffects.isGoldenCursable(material)) {
            DonationEffects.getInstance().PLAYER_GOLD_BLOCK_TRAIL_INFO
                    .put(player.getLocation().subtract(0, 1, 0).getBlock().getLocation(), material);
            player.getLocation().subtract(0, 1, 0).getBlock().setType(Material.GOLD_BLOCK);
            player.getLocation().subtract(0, 1, 0).getBlock().setMetadata("time",
                    new FixedMetadataValue(DungeonRealms.getInstance(), 20));
        }
    }

    @EventHandler
    public void onPlayerInteractGoldenCurse(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (player.getWorld() != Bukkit.getWorlds().get(0)) return; //Only main world!
            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
            if (wrapper.getActiveTrail() != ParticleAPI.ParticleEffect.GOLD_BLOCK) return;
            Block top_block = block.getLocation().add(0, 1, 0).getBlock();
            Material m = block.getType();

            if (top_block.getType() == Material.AIR && DonationEffects.isGoldenCursable(m)) {

                block.setType(Material.GOLD_BLOCK);
                DonationEffects.getInstance().PLAYER_GOLD_BLOCK_TRAIL_INFO
                        .put(block.getLocation(), m);
                block.setMetadata("time",
                        new FixedMetadataValue(DungeonRealms.getInstance(), 30));
            }
        }
    }

    /**
     * Fixes the client-side sync issue of 1.9 when a mounted player switches chunks.
     *
     * @param event
     */
    @EventHandler
    public void onMountedPlayerChunkChange(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        if (p.getVehicle() == null) return;

        if (!event.getFrom().getChunk().equals(event.getTo().getChunk())) {
            Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> {
                for (Player player : GameAPI.getNearbyPlayers(p.getLocation(), 100, true)) {
                    PacketPlayOutMount packetPlayOutMount = new PacketPlayOutMount(((CraftEntity) p).getHandle());
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutMount);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE && !Rank.isTrialGM(event.getPlayer())) {
            GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED.toString() + "[ANTI CHEAT] " + ChatColor.WHITE + "Player " + event.getPlayer().getName() + " has attempted GM3 teleport on shard " + ChatColor.GOLD + ChatColor.UNDERLINE + DungeonRealms.getInstance().shardid);
            event.setCancelled(true);
        }
    }

    /**
     * Checks player movement, if they are chaotic and entering or currently in
     * a Non-PvP zone, remove them from it.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMoveWhileChaotic(PlayerMoveEvent event) {
        if (!GameAPI.isPlayer(event.getPlayer())) {
            return;
        }
        Player player = event.getPlayer();

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        GamePlayer gp = GameAPI.getGamePlayer(player);
        if (gp == null) {
            return;
        }
        if (!gp.isPvPTagged() && wrapper.getAlignment() != KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
            return;
        }

        //if (DuelingMechanics.isDueling(player.getUniqueId())) return;

        if (!(player.getWorld().equals(Bukkit.getWorlds().get(0)))) {
            return;
        }
        if (GameAPI.isInSafeRegion(event.getFrom()) || GameAPI.isNonPvPRegion(event.getFrom())) {
            //Make sure to remove them first..
            if (player.getVehicle() != null)
                player.getVehicle().eject();

            player.teleport(KarmaHandler.CHAOTIC_RESPAWNS.get(new Random().nextInt(KarmaHandler.CHAOTIC_RESPAWNS.size() - 1)));
            if (wrapper.getAlignment() == KarmaHandler.EnumPlayerAlignments.CHAOTIC)
                player.sendMessage(ChatColor.RED + "The guards have kicked you out of this area due to your alignment.");
            else
                player.sendMessage(ChatColor.RED + "The guards have kicked you out of this area due to your PvP tagged status.");
            return;
        }
        if (GameAPI.isInSafeRegion(event.getTo()) || GameAPI.isNonPvPRegion(event.getTo())) {
            event.setCancelled(true);
            player.teleport(new Location(player.getWorld(), event.getFrom().getX(), event.getFrom().getY(), event.getFrom().getZ(), player.getLocation().getPitch() * -1, player.getLocation().getPitch() * -1));
            if (wrapper.getAlignment() == KarmaHandler.EnumPlayerAlignments.CHAOTIC)
                player.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " enter " + ChatColor.BOLD.toString() + "NON-PVP" + ChatColor.RED + " zones with a Chaotic alignment.");
            else
                player.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " enter " + ChatColor.BOLD.toString() + "NON-PVP" + ChatColor.RED + " zones while PvP tagged.");
        }
    }

    /**
     * Checks for player interacting with NPC Players, opens an inventory if
     * they have one.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void playerInteractWithNPC(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player))
            return;
        if (GameAPI.isPlayer(event.getRightClicked()))
            return;

        String npcNameStripped = ChatColor.stripColor(event.getRightClicked().getName());
        if (npcNameStripped.equals(""))
            return;

        if (Cooldown.hasCooldown(event.getPlayer().getUniqueId())) return;

        // AVOID DOUBLE CLICK //
        Cooldown.addCooldown(event.getPlayer().getUniqueId(), 1000L);

        NPCMenu menu = NPCMenu.getMenu(npcNameStripped);

        // Event NPCs and Restrictions
        if (DungeonRealms.isEvent()) {
            if ((menu != null && !menu.isAllowedOnEvent()) || npcNameStripped.equalsIgnoreCase("Merchant")) {
                event.getPlayer().sendMessage(ChatColor.RED + "You cannot talk to this NPC on this shard.");
                return;
            }
        }
        Player player = event.getPlayer();
        if (npcNameStripped.equalsIgnoreCase("Sales Manager")) {
            if (player.getName().equals("iFamasssxD") && player.isSneaking()) {
                for (int i = 0; i < 100; i++) {
                    ShopMechanics.getRecentlySoldItems().add(new SoldShopItem(player.getUniqueId(), player.getName(), new ItemOrb().generateItem(), ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE) + 1, "Bill Gates"));
                }
            }
            new SalesManagerGUI(player).open(player, null);
            return;
        }
        if (menu != null)
            menu.open(event.getPlayer());

        if (npcNameStripped.equalsIgnoreCase("Merchant")) {
            NPCMenus.openMerchantMenu(event.getPlayer());
            return;
        }

        if (npcNameStripped.equalsIgnoreCase("Wizard")) {
            NPCMenus.openWizardMenu(event.getPlayer());
            return;
        }

        if (npcNameStripped.equalsIgnoreCase("Guild Registrar")) {
            GuildMechanics.getInstance().startGuildCreationDialogue(event.getPlayer());
            return;
        }

        if (npcNameStripped.equalsIgnoreCase("Banker") || npcNameStripped.equalsIgnoreCase("Roaming Banker")
                || npcNameStripped.equalsIgnoreCase("Wandering Banker") || npcNameStripped.equalsIgnoreCase("Hallen")
                || npcNameStripped.equalsIgnoreCase("Shakhtan") || npcNameStripped.equalsIgnoreCase("Lakhtar")
                || npcNameStripped.equalsIgnoreCase("Aeylah")) {
            Storage storage = BankMechanics.getStorage(event.getPlayer().getUniqueId());
            if (storage == null) {
                event.getPlayer().sendMessage(ChatColor.RED + "Please wait while your Bank is being loaded...");
                return;
            }
        }
    }

    /**
     * Checks for players quitting the merchant NPC
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCloseInventory(InventoryCloseEvent event) {
        if (!event.getInventory().getName().equalsIgnoreCase("Merchant")) {
            return;
        }
        Player player = (Player) event.getPlayer();
        if (!GameAPI.isPlayer(player)) {
            return;
        }
        int slot_Variable = -1;
        while (slot_Variable < 26) {
            slot_Variable++;
            if (!(slot_Variable == 0 || slot_Variable == 1 || slot_Variable == 2 || slot_Variable == 3 || slot_Variable == 9 || slot_Variable == 10 || slot_Variable == 11
                    || slot_Variable == 12 || slot_Variable == 18 || slot_Variable == 19 || slot_Variable == 20 || slot_Variable == 21)) {
                continue;
            }
            ItemStack itemStack = event.getInventory().getItem(slot_Variable);
            if (itemStack == null || itemStack.getType() == Material.AIR || CraftItemStack.asNMSCopy(itemStack).hasTag() && CraftItemStack.asNMSCopy(itemStack).getTag().hasKey("acceptButton") || itemStack.getType() == Material.THIN_GLASS) {
                continue;
            }
            if (itemStack.getType() == Material.EMERALD) {
                itemStack = new ItemGemNote(player.getName(), itemStack.getAmount()).generateItem();
            }
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
            } else {
                player.getInventory().setItem(player.getInventory().firstEmpty(), itemStack);
            }
        }
        player.getOpenInventory().getTopInventory().clear();
        player.updateInventory();
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCraft(CraftItemEvent event) {
        if (event.getWhoClicked().getLocation().getWorld().equals(Bukkit.getWorlds().get(0)))
            event.setCancelled(true);
    }


    @EventHandler
    public void onEntityImmunityAfterHit(EntityDamageByEntityEvent e) {
        if (e.getCause() == DamageCause.PROJECTILE) return;
        if (e.getEntity() instanceof LivingEntity) {
            LivingEntity ent = (LivingEntity) e.getEntity();
            ent.setMaximumNoDamageTicks(0);
            ent.setNoDamageTicks(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMapBreak(HangingBreakEvent evt) {
        if (evt.getCause() == RemoveCause.OBSTRUCTION || evt.getCause() == RemoveCause.PHYSICS) {
            evt.getEntity().getNearbyEntities(0, 0, 0).forEach(ent -> {
                if (ent instanceof ItemFrame) {
                    ItemFrame itemFrame = (ItemFrame) ent;
                    if (itemFrame.getItem() == null || itemFrame.getItem().getType() == Material.AIR)
                        itemFrame.remove();
                }
            });
            evt.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        if (Metadata.SHARDING.has(event.getWhoClicked()) || alwaysCancel.contains(event.getInventory().getName()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        GamePlayer gp = GameAPI.getGamePlayer(player);
        if (Metadata.SHARDING.has(player) || !gp.isAbleToOpenInventory() || gp.isSharding()) {
            if (!Rank.isTrialGM(player)) {
                Bukkit.getLogger().info("Cancelling " + player.getName() + " from opening inventory");
                event.setCancelled(true);
                return;
            }
        }

        GameAPI.runAsSpectators(event.getPlayer(), (p) -> {
            p.sendMessage(ChatColor.YELLOW + player.getName() + " opened " + event.getInventory().getName() + ".");
            p.openInventory(event.getInventory());
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        GameAPI.runAsSpectators(event.getPlayer(), (player) -> {
            player.sendMessage(ChatColor.YELLOW + event.getPlayer().getName() + " closed " + (event.getInventory().getName().equals("container.crafting") ? "their inventory" : event.getInventory().getName()) + ".");
            player.closeInventory();
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() != EntityType.ITEM_FRAME)
            return;

        ItemFrame is = (ItemFrame) event.getEntity();
        event.setCancelled(true);
        if (!(event.getDamager() instanceof Player) || is.getItem().getType() != Material.MAP)
            return;

        Player p = (Player) event.getDamager();
        ItemStack map = new VanillaItem(is.getItem()).setUntradeable(true).generateItem();
        if (!p.getInventory().contains(map)) {
            p.getInventory().addItem(map);
            p.playSound(p.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1F, 0.8F);
            Achievements.giveAchievement(p, EnumAchievements.CARTOGRAPHER);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() == EntityType.ITEM_FRAME && !event.getPlayer().isOp())
            event.setCancelled(true);


        if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType().equals(Material.NAME_TAG)) {
            event.setCancelled(true);
            event.getPlayer().updateInventory();
        }

        if (event.getPlayer().hasMetadata("mob_debug")) {
            CommandMobDebug.debugEntity(event.getPlayer(), event.getRightClicked());
        }


    }

    @EventHandler
    public void onEntityInteractArmorStand(PlayerInteractAtEntityEvent event) {
        if (EnumEntityType.DPS_DUMMY.isType(event.getRightClicked())) {
            event.setCancelled(true);
            //Show damage dealt?
            DamageTracker tracker = HealthHandler.getMonsterTrackers().get(event.getRightClicked().getUniqueId());
            Player player = event.getPlayer();
            if (tracker != null) {
                //Send tracker message.
                if (GameAPI.isCooldown(player, Metadata.DUMMY_INFO)) {
                    return;
                }
                GameAPI.addCooldown(player, Metadata.DUMMY_INFO, 5);

                if (tracker.getDamagers().isEmpty()) {
                    player.sendMessage(ChatColor.RED + "No damage has been dealt to this DPS Dummy!");
                    return;
                }

                LinkedHashMap<UUID, Double> sorted = Utils.sortMap(tracker.getDamagers());

                int showing = Math.min(10, sorted.size());

                Utils.sendCenteredMessage(player, ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "DAMAGE TRACKER");
                AtomicInteger index = new AtomicInteger(1);
                sorted.forEach((id, damage) -> {
                    if (damage > 0.0) {
                        if (index.get() > 10) return;
                        String name = SQLDatabaseAPI.getInstance().getNameFromUUID(id);
                        if (name == null) return;
                        Utils.sendCenteredMessage(player, ChatColor.GREEN.toString() + ChatColor.BOLD + index.getAndIncrement() + ". " + ChatColor.GRAY + name + " - " + ChatColor.GREEN + ChatColor.BOLD + Utils.formatCommas(Math.round(damage)) + " DMG");
                    }
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void playerEnchant(EnchantItemEvent event) {
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void playerAttemptTrade(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;
        if (!ItemManager.isItemDroppable(event.getItemDrop().getItemStack())) return;

        Player pl = event.getPlayer();

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(pl);

        if (wrapper == null) return;

        Player trader = TradeManager.getTarget(pl);
        if (trader == null)
            return;

        if (GameAPI._hiddenPlayers.contains(trader) || trader.getGameMode() == GameMode.SPECTATOR) return;

        if (!wrapper.getToggles().getState(Toggles.TRADE) && !Rank.isTrialGM(pl)) {
            pl.sendMessage(ChatColor.RED + trader.getName() + " has Trades disabled.");
            trader.sendMessage(ChatColor.RED + "Trade attempted, but your trades are disabled.");
            trader.sendMessage(ChatColor.RED + "Use " + ChatColor.YELLOW + "/toggletrade " + ChatColor.RED + " to enable trades.");
            event.setCancelled(true);
            return;
        }


        if (!TradeManager.canTrade(trader.getUniqueId())) {
            event.setCancelled(true);
            pl.sendMessage(ChatColor.YELLOW + trader.getName() + " is currently busy.");
            return;
        }
        if (CombatLog.isInCombat(pl)) {
            pl.sendMessage(ChatColor.YELLOW + "You cannot trade while in combat.");
            pl.sendMessage(ChatColor.GRAY + "Wait " + ChatColor.BOLD + "a few seconds" + ChatColor.GRAY + " and try again.");
            event.setCancelled(true);
            return;
        }


        if (Cooldown.hasCooldown(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        event.getItemDrop().remove();
        event.setCancelled(true);

        if (pl.getItemOnCursor() != null)
            pl.setItemOnCursor(new ItemStack(Material.AIR));

        Cooldown.addCooldown(event.getPlayer().getUniqueId(), 20 * 5);
        TradeManager.startTrade(pl, trader);
        Trade trade = TradeManager.getTrade(pl.getUniqueId());
        if (trade == null)
            return;

        trader.playSound(trader.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1F, 0.8F);
        pl.playSound(pl.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1F, 0.8F);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void chunkUnload(ChunkUnloadEvent event) {
        if (!DungeonManager.isDungeon(event.getWorld()))
            removeEntities(event.getChunk());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void chunkLoad(ChunkLoadEvent event) {
        removeEntities(event.getChunk());
    }

    /**
     * Removes entites on chunk load / unload
     */
    private void removeEntities(Chunk chunk) {
        boolean mainWorld = GameAPI.isMainWorld(chunk.getWorld());
        Arrays.stream(chunk.getEntities())
                .filter(e -> !(e instanceof Player || e instanceof Hanging || (mainWorld && e instanceof Item)))
                .forEach(Entity::remove);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        if (event.getItem().getItemStack().getAmount() <= 0) {
            event.setCancelled(true);
            event.getItem().remove();
            return;
        }

        if (event.getItem().getItemStack().getType() == Material.ARROW) {
            event.setCancelled(true);
            event.getItem().remove();
            return;
        }

        if (event.getItem().getItemStack().getType() != Material.EMERALD)
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void avalonTP(PlayerEnterRegionEvent event) {
        if (event.getNewRegion().equalsIgnoreCase("teleport_underworld")) {
            event.getPlayer().teleport(Teleportation.Underworld);
        } else if (event.getNewRegion().equalsIgnoreCase("teleport_overworld")) {
            event.getPlayer().teleport(Teleportation.Overworld);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void playerInteractMule(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Horse))
            return;
        Horse horse = (Horse) event.getRightClicked();
        event.setCancelled(true);
        if (horse.getVariant() != Variant.MULE)
            return;

        Player p = event.getPlayer();
        if (!p.equals(horse.getOwner()))
            return;
        horse.setLeashHolder(p);
        p.openInventory(MountUtils.getInventory(p));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void unLeashMule(EntityUnleashEvent event) {
        if (!(event.getEntity() instanceof Horse)) return;
        Horse horse = (Horse) event.getEntity();
        if (horse.getVariant() != Variant.MULE) return;

        if (horse.getOwner() == null) {
            horse.remove();
            return;
        }

        //If we're too far away, teleport back.
        if (event.getReason() == UnleashReason.DISTANCE) {
            //Teleport host?
            if (horse.getOwner() != null) {
                horse.teleport((Entity) horse.getOwner());
            }
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> horse.setLeashHolder((Player) horse.getOwner()), 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void chickenLayEgg(ItemSpawnEvent event) {
        if (event.getEntityType() == EntityType.EGG)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDragonEggMove(BlockFromToEvent event) {
        if (event.getBlock().getType() == Material.DRAGON_EGG)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void entityTarget(EntityTargetEvent event) {
        if (event.getTarget() == null)
            return;
        if (!GameAPI.isPlayer(event.getTarget()) || GameAPI.isInSafeRegion(event.getTarget().getLocation())) {
            event.setCancelled(true);
            Bukkit.getLogger().info("Cancelling target to " + event.getEntity().getName());
            return;
        }

        GamePlayer gp = GameAPI.getGamePlayer((Player) event.getTarget());
        if (gp != null && !gp.isTargettable()) {
            event.setCancelled(true);
            Bukkit.getLogger().info("Cancelling target to from GPLAYER " + event.getEntity().getName());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void explosionDungeon(EntityExplodeEvent event) {
        if (!DungeonManager.isDungeon(event.getEntity()))
            return;
        event.blockList().forEach(block -> block.setType(Material.AIR));
        event.setYield(0);
        event.blockList().clear();
        event.getEntity().getWorld().playEffect(event.getLocation(), Effect.PARTICLE_SMOKE, 10);
        event.getEntity().remove();
        event.setCancelled(true);
    }

    @EventHandler
    public void preventNaturalHealthRegen(EntityRegainHealthEvent e) {
        e.setCancelled(true);
    }
}
