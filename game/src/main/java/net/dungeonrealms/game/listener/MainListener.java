package net.dungeonrealms.game.listener;

import com.vexsoftware.votifier.model.VotifierEvent;
import io.netty.util.internal.ConcurrentSet;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.punishment.PunishAPI;
import net.dungeonrealms.common.game.util.Cooldown;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.event.PlayerEnterRegionEvent;
import net.dungeonrealms.game.event.PlayerMessagePlayerEvent;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.CrashDetector;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.PlayerManager;
import net.dungeonrealms.game.mechanic.TutorialIsland;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.duel.DuelOffer;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.player.inventory.NPCMenus;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.player.trade.Trade;
import net.dungeonrealms.game.player.trade.TradeManager;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.quests.QuestPlayerData;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveCatchFish;
import net.dungeonrealms.game.title.TitleAPI;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.item.repairing.RepairAPI;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_9_R2.EntityArmorStand;
import net.minecraft.server.v1_9_R2.PacketPlayOutMount;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityUnleashEvent.UnleashReason;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Nick on 9/17/2015.
 */
public class MainListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandWhilstSharding(PlayerCommandPreprocessEvent event) {
        GameAPI.runAsSpectators(event.getPlayer(), p -> p.sendMessage(ChatColor.RED + event.getPlayer().getName() + "> " + event.getMessage()));
        if (event.getPlayer().hasMetadata("sharding")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot perform commands whilst sharding!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(EntityTeleportEvent event) {
        if (event.getEntity().getType() == EntityType.ENDERMAN) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onVote(VotifierEvent event) {
        // No votes on the event shard.
        if (DungeonRealms.getInstance().isEventShard)
            return;

        if (Bukkit.getPlayer(event.getVote().getUsername()) != null) {
            Player player = Bukkit.getPlayer(event.getVote().getUsername());

            // Handle the experience calculations.
            GamePlayer gamePlayer = GameAPI.getGamePlayer(player);
            int expToLevel = gamePlayer.getEXPNeeded(gamePlayer.getLevel());
            int expToGive = expToLevel / 20;
            expToGive += 100;

            // Prepare the message.
            TextComponent bungeeMessage = new TextComponent(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE");
            bungeeMessage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://dungeonrealms.net/vote"));
            bungeeMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to vote!").create()));

            // Handle reward calculations & achievements.
            Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.VOTE);
            int ecashReward = 15;
            if (Rank.isSubscriber(player)) {
                ecashReward = 20;
                Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.VOTE_AS_SUB);
                // Now let's check if we should reward them for being a SUB+/++.
                if (Rank.isSubscriberPlus(player)) {
                    ecashReward = 25;
                    Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.VOTE_AS_SUB_PLUS);
                }
            }

            // Update the database with the new E-Cash reward!
            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$INC, EnumData.ECASH, ecashReward, true);
            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.LAST_VOTE, System.currentTimeMillis(), true);

            // Reward to player with their EXP increase.
            if (GameAPI.getGamePlayer(player) == null) {
                return;
            }
            gamePlayer.addExperience(expToGive, false, true);

            // Send a message to everyone prompting them that a player has voted & how much they were rewarded for voting.
            final JSONMessage normal = new JSONMessage(ChatColor.AQUA + player.getName() + ChatColor.RESET + ChatColor.GRAY + " voted for " + ecashReward + " ECASH & 5% EXP @ vote ", ChatColor.WHITE);
            normal.addURL(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE", ChatColor.AQUA, "http://dungeonrealms.net/vote");
            GameAPI.sendNetworkMessage("BroadcastRaw", normal.toString());
        }
    }

    @EventHandler
    public void onPm(PlayerMessagePlayerEvent event) {
        if (event.getSender().equals(event.getReceiver())) {
            Achievements.getInstance().giveAchievement(event.getSender().getUniqueId(), Achievements.EnumAchievements.MESSAGE_YOURSELF);
        } else {
            Achievements.getInstance().giveAchievement(event.getSender().getUniqueId(), Achievements.EnumAchievements.SEND_A_PM);
        }
    }

    @EventHandler
    public void onPing(ServerListPingEvent event) {
        if (!DungeonRealms.getInstance().canAcceptPlayers()) event.setMotd("offline");
        else
            event.setMotd(DungeonRealms.getInstance().shardid + "," + GameAPI.getServerLoad() + ChatColor.RESET + "," + Constants.BUILD_NUMBER);
    }

    /**
     * Monitors and checks the players language.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onChat(AsyncPlayerChatEvent event) {
        if (GameAPI.getGamePlayer(event.getPlayer()) == null) { // server is restarting
            event.setCancelled(true);
            return;
        }

        Chat.getInstance().doMessageChatListener(event);

        if (PunishAPI.isMuted(event.getPlayer().getUniqueId()) && !event.isCancelled()) {
            event.getPlayer().sendMessage(PunishAPI.getMutedMessage(event.getPlayer().getUniqueId()));
            event.setCancelled(true);
            return;
        }

        Chat.getInstance().doChat(event);
        Affair.getInstance().doChat(event);
        GuildMechanics.getInstance().doChat(event);
        Chat.getInstance().doLocalChat(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void worldInit(org.bukkit.event.world.WorldInitEvent e) {
        e.getWorld().setKeepSpawnInMemory(false);
    }

    @EventHandler
    public void onAsyncLogin(AsyncPlayerPreLoginEvent event) {

        if (PunishAPI.isBanned(event.getUniqueId()) && DungeonRealms.getInstance().isEventShard) {
            event.disallow(Result.KICK_BANNED, ChatColor.RED + "You have been eliminated from this event!");
            return;
        }

        if (DungeonRealms.getInstance().getLoggingOut().contains(event.getName())) {
            event.disallow(Result.KICK_OTHER, ChatColor.RED + "Please wait while your data syncs.");
            DungeonRealms.getInstance().getLoggingOut().remove(event.getName());
            return;
        }

        if ((Boolean) DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, event.getUniqueId())) {
            String shard = DatabaseAPI.getInstance().getFormattedShardName(event.getUniqueId());
            if (!shard.equals("") && shard != null && !DungeonRealms.getInstance().shardid.equals(shard)) {
                event.disallow(Result.KICK_OTHER, ChatColor.YELLOW.toString() + "The account " + ChatColor.BOLD.toString() + event.getName() + ChatColor.YELLOW.toString()

                        + " is already logged in on " + ChatColor.UNDERLINE.toString() + shard + "." + "\n\n" + ChatColor.GRAY.toString()
                        + "If you have just recently changed servers, your character data is being synced -- " + ChatColor.UNDERLINE.toString()
                        + "wait a few seconds" + ChatColor.GRAY.toString() + " before reconnecting.");
                return;
            }
        }

        DungeonRealms.getInstance().getLoggingIn().add(event.getUniqueId());

        // REQUEST PLAYER'S DATA ASYNC //
        DatabaseAPI.getInstance().requestPlayer(event.getUniqueId(), false);
    }

    @EventHandler
    public void asyncChat(AsyncPlayerChatEvent event) {
        if (event.getMessage() != null) {
            if (event.getMessage().toLowerCase().startsWith("@i")) {
                if (event.getMessage().length() < 1) {
                    // A player types "@i" only, cancel that message
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onShardClick(InventoryClickEvent event) {
        if (event.getInventory().getTitle() != null) {
            if (event.getInventory().getTitle().equalsIgnoreCase("dungeonrealms shards")) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * This event is the main event once the player has actually entered the
     * world! It is now safe to do things to the player e.g TitleAPI or
     * adding PotionEffects.. etc..
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();

        //No left over shit..
        savedOnLogout.remove(player.getUniqueId());
        savedAfterSharding.remove(player.getUniqueId());
        if (!DatabaseAPI.getInstance().PLAYERS.containsKey(player.getUniqueId())) {
            player.kickPlayer(ChatColor.RED + "Unable to load your character.");
            return;
        }

        //GameAPI.SAVE_DATA_COOLDOWN.submitCooldown(player, 2000L);
        TitleAPI.sendTitle(player, 0, 0, 0, "", "");

        CombatLog.checkCombatLog(player.getUniqueId());
        try {
            GameAPI.handleLogin(player.getUniqueId());
        } catch (Exception e) {
            player.kickPlayer(ChatColor.RED + "There was an error loading your character. Staff have been notified.");
            GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED + "[ALERT] " + ChatColor.WHITE + "There was an error loading " + ChatColor.GOLD + player.getName() + "'s " + ChatColor.WHITE + "data on " + DungeonRealms.getShard().getShardID() + ".");
            e.printStackTrace();
            return;
        }

        GameAPI.asyncTracker.add(player);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            if (player.isOnline()) {
                if ((Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.LOGGERDIED, player.getUniqueId()).toString()))) {
                    player.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "You logged out while in combat, your doppelganger was killed and alas your items are gone.");
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.LOGGERDIED, false, true);
                    ItemManager.giveStarter(player);
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoinWhitelistedShard(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() == Result.KICK_WHITELIST) {
            event.setKickMessage(ChatColor.AQUA + "This DungeonRealms shard is on " + ChatColor.UNDERLINE +
                    "maintenance" + ChatColor.AQUA + " mode. Only authorized users can join");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDropEvent(PlayerDropItemEvent event) {
        Player p = event.getPlayer();
        if (GameAPI.getGamePlayer(p) == null) return;
        if (!GameAPI.getGamePlayer(p).isAbleToDrop()) {
            event.setCancelled(true);
        }
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
        if (((CraftEntity) event.getEntity()).getHandle() instanceof DRMonster) {
            event.getEntity().setCustomNameVisible(true);
            event.getEntity().setCollidable(true);
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
        if (!(GameAPI.isPlayer(event.getExited()))) {
            if (event.getExited() instanceof EntityArmorStand) {
                Bukkit.getLogger().info("Removing armor stand...");
                event.getExited().remove();
            }
            return;
        }
        if (EntityAPI.hasMountOut(event.getExited().getUniqueId())) {
            if (event.getVehicle().hasMetadata("type")) {
                String metaValue = event.getVehicle().getMetadata("type").get(0).asString();
                if (metaValue.equalsIgnoreCase("mount")) {
                    event.getVehicle().remove();
                    EntityAPI.removePlayerMountList(event.getExited().getUniqueId());
                    event.getExited().sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "For its own safety, your mount has returned to the stable.");
                }
            }
        }
    }


    private Set<UUID> kickedIgnore = new HashSet<>();

    /**
     * Handles player leaving the server
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        event.setLeaveMessage(null);
        this.kickedIgnore.add(event.getPlayer().getUniqueId());
        onDisconnect(event.getPlayer(), !event.getReason().contains("Appeal at: www.dungeonrealms.net"));
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL && Rank.isSubscriber(event.getPlayer()))
            event.allow();
    }

    private SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");

    @EventHandler(priority = EventPriority.MONITOR)
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

        boolean sharding = player.hasMetadata("sharding");
        if (player.hasMetadata("sharding")) {
            player.removeMetadata("sharding", DungeonRealms.getInstance());
        }

        if (GameAPI.IGNORE_QUIT_EVENT.contains(player.getUniqueId())) {
            //Still remove this shit..
            GameAPI.IGNORE_QUIT_EVENT.remove(player.getUniqueId());
            if (!sharding) {
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

        String name = player.getName();
        UUID uuid = player.getUniqueId();
        GameAPI.handleLogout(uuid, true, (result) -> {
            savedOnLogout.add(uuid);
        });
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
        if (!(DonationEffects.getInstance().PLAYER_GOLD_BLOCK_TRAILS.contains(event.getPlayer())))
            return;
        Player player = event.getPlayer();
        if (!(player.getWorld().equals(Bukkit.getWorlds().get(0))))
            return;
        if (player.getLocation().getBlock().getType() != Material.AIR)
            return;
        Material material = player.getLocation().subtract(0, 1, 0).getBlock().getType();
        if (material == Material.DIRT || material == Material.GRASS || material == Material.STONE
                || material == Material.COBBLESTONE || material == Material.GRAVEL || material == Material.LOG
                || material == Material.SMOOTH_BRICK || material == Material.BEDROCK || material == Material.GLASS
                || material == Material.SANDSTONE || material == Material.SAND || material == Material.BOOKSHELF
                || material == Material.MOSSY_COBBLESTONE || material == Material.OBSIDIAN
                || material == Material.SNOW_BLOCK || material == Material.CLAY || material == Material.STAINED_CLAY
                || material == Material.WOOL) {
            DonationEffects.getInstance().PLAYER_GOLD_BLOCK_TRAIL_INFO
                    .put(player.getLocation().subtract(0, 1, 0).getBlock().getLocation(), material);
            player.getLocation().subtract(0, 1, 0).getBlock().setType(Material.GOLD_BLOCK);
            player.getLocation().subtract(0, 1, 0).getBlock().setMetadata("time",
                    new FixedMetadataValue(DungeonRealms.getInstance(), 10));
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
        GamePlayer gp = GameAPI.getGamePlayer(player);
        if (gp == null) {
            return;
        }
        if (!gp.isPvPTagged() && gp.getPlayerAlignment() != KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
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
            if (gp.getPlayerAlignment() == KarmaHandler.EnumPlayerAlignments.CHAOTIC)
                player.sendMessage(ChatColor.RED + "The guards have kicked you out of this area due to your alignment.");
            else
                player.sendMessage(ChatColor.RED + "The guards have kicked you out of this area due to your PvP tagged status.");
            return;
        }
        if (GameAPI.isInSafeRegion(event.getTo()) || GameAPI.isNonPvPRegion(event.getTo())) {
            event.setCancelled(true);
            player.teleport(new Location(player.getWorld(), event.getFrom().getX(), event.getFrom().getY(), event.getFrom().getZ(), player.getLocation().getPitch() * -1, player.getLocation().getPitch() * -1));
            if (gp.getPlayerAlignment() == KarmaHandler.EnumPlayerAlignments.CHAOTIC)
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

        // Event NPCs and Restrictions
        if (DungeonRealms.getInstance().isEventShard) {
            if (npcNameStripped.equalsIgnoreCase("E-Cash Vendor") || npcNameStripped.equalsIgnoreCase("Skill Trainer")
                    || npcNameStripped.equalsIgnoreCase("Item Vendor") || npcNameStripped.equalsIgnoreCase("Merchant")
                    || npcNameStripped.equalsIgnoreCase("Animal Tamer") || npcNameStripped.equalsIgnoreCase("Innkeeper")) {
                event.getPlayer().sendMessage(ChatColor.RED + "You cannot talk to this NPC on this shard.");
                return;
            }
        }

        if (npcNameStripped.equalsIgnoreCase("Animal Tamer")) {
            NPCMenus.openMountPurchaseMenu(event.getPlayer());
            return;
        }
        if (npcNameStripped.equalsIgnoreCase("Merchant")) {
            NPCMenus.openMerchantMenu(event.getPlayer());
            return;
        }
        if (npcNameStripped.equalsIgnoreCase("E-Cash Vendor")) {
            NPCMenus.openECashPurchaseMenu(event.getPlayer());
            return;
        }
        if (npcNameStripped.equalsIgnoreCase("Wizard")) {
            NPCMenus.openWizardMenu(event.getPlayer());
            return;
        }
        if (npcNameStripped.equalsIgnoreCase("Dungeoneer")) {
            NPCMenus.openDungeoneerMenu(event.getPlayer());
            return;
        }
        if (npcNameStripped.equalsIgnoreCase("Skill Trainer")) {
            NPCMenus.openProfessionPurchaseMenu(event.getPlayer());
            return;
        }
        if (npcNameStripped.equalsIgnoreCase("Food Vendor")) {
            NPCMenus.openFoodVendorMenu(event.getPlayer());
            return;
        }
        if (npcNameStripped.equalsIgnoreCase("Item Vendor")) {
            NPCMenus.openItemVendorMenu(event.getPlayer());
            return;
        }
        if (npcNameStripped.equalsIgnoreCase("Guild Registrar")) {
            GuildMechanics.getInstance().startGuildCreationDialogue(event.getPlayer());
            return;
        }
        if (npcNameStripped.equalsIgnoreCase("Innkeeper")) {
            NPCMenus.openHearthstoneRelocateMenu(event.getPlayer());
            return;
        }
        if (npcNameStripped.equalsIgnoreCase("Banker") || npcNameStripped.equalsIgnoreCase("Roaming Banker")
                || npcNameStripped.equalsIgnoreCase("Wandering Banker") || npcNameStripped.equalsIgnoreCase("Hallen")
                || npcNameStripped.equalsIgnoreCase("Shakhtan") || npcNameStripped.equalsIgnoreCase("Lakhtar")
                || npcNameStripped.equalsIgnoreCase("Aeylah")) {
            Storage storage = BankMechanics.getInstance().getStorage(event.getPlayer().getUniqueId());
            if (storage == null) {
                event.getPlayer().sendMessage(ChatColor.RED + "Please wait while your Bank is being loaded...");
                return;
            }
            storage.openBank(event.getPlayer());
        }
        /* if (npcNameStripped.equalsIgnoreCase("Ship Captain")) {
            if (GameAPI.getRegionName(event.getRightClicked().getLocation()).contains("tutorial")) {
                event.getPlayer().sendMessage("");
                event.getPlayer().sendMessage(ChatColor.GRAY + "Ship Captain: " + ChatColor.WHITE + "Are you ready to start ye adventure " + event.getPlayer().getName() + "?"); //+ " " + ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Y " + ChatColor.GRAY.toString() + "/" + ChatColor.RED.toString() + ChatColor.BOLD.toString() + " N");
                event.getPlayer().sendMessage(ChatColor.GRAY + "Type either '" + ChatColor.GREEN + "Y" + ChatColor.GRAY + "' or '" + ChatColor.RED + "N" + ChatColor.GRAY + "' -- Yes or No; Once you leave this island you can never come back, your epic adventure in the lands of Andalucia will begin!");
                event.getPlayer().sendMessage("");
                Chat.listenForMessage(event.getPlayer(), e -> Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    if (e.getMessage().equalsIgnoreCase("y")) {
                        event.getPlayer().sendMessage(ChatColor.GRAY + "Ship Captain: " + ChatColor.WHITE + "Argh! We'll be casting off in a few moments!");
                        event.getPlayer().teleport(new Location(Bukkit.getWorlds().get(0), -600 + .5, 60 + 1.5, 473 + .5, -1F, 2.5F));
                        ItemManager.giveStarter(event.getPlayer());

                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                            final JSONMessage normal = new JSONMessage(ChatColor.GOLD + " â�¢ " + ChatColor.YELLOW + "Need more information? Visit our wiki " + ChatColor.WHITE);
                            normal.addURL(ChatColor.GOLD.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE!", ChatColor.GREEN, "http://dungeonrealms.wikia.com/wiki/Main_Page");
                            normal.addSuggestCommand(ChatColor.YELLOW.toString() + " or for any questions. Click " + ChatColor.GOLD.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE!", ChatColor.GREEN, "/ask ");
                            normal.addText(ChatColor.GOLD + " â�¢ ");

                            event.getPlayer().sendMessage("");
                            normal.sendToPlayer(event.getPlayer());
                            event.getPlayer().sendMessage("");

                            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 0.5F);
                        }, 40);
                    }
                }), pl -> pl.sendMessage(ChatColor.GRAY + "Ship Captain: " + ChatColor.WHITE + "Argh! Speak to me when ye ready to leave!"));
            }
        }*/
    }

    /*
    * Prevents fishing bug in Dungeons/Realms
     */
    @EventHandler
    public void onPlayerInteractFishingRod(PlayerInteractEvent event) {
        final Player pl = event.getPlayer();
        if (pl.getEquipment().getItemInMainHand() != null)
            if (!(Fishing.isDRFishingPole(pl.getEquipment().getItemInMainHand()))) {
                return; // Get out of here.
            }
        if (!pl.getWorld().equals(Bukkit.getWorlds().get(0))) {
            event.getPlayer().sendMessage(ChatColor.RED + "There are " + ChatColor.UNDERLINE + "no" + ChatColor.RED + " populated fishing spots near this location.");
            event.getPlayer().sendMessage(ChatColor.GRAY + "Look for particles above water blocks to signify active fishing spots.");
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
        }
    }


    @EventHandler
    public void onPlayerFish(PlayerFishEvent e) {
        final Player pl = e.getPlayer();
        if (!pl.getWorld().equals(Bukkit.getWorlds().get(0))) {
            e.getPlayer().sendMessage(ChatColor.RED + "There are " + ChatColor.UNDERLINE + "no" + ChatColor.RED + " populated fishing spots near this location.");
            e.getPlayer().sendMessage(ChatColor.GRAY + "Look for particles above water blocks to signify active fishing spots.");
            e.setCancelled(true);
            return;
        }

        e.setExpToDrop(0);

        if (!(Fishing.isDRFishingPole(pl.getEquipment().getItemInMainHand()))) {
            e.setCancelled(true);
            return; // Get out of here.
        }

        if (e.getState().equals(State.FISHING)) {
            Location loc = Fishing.getInstance().getFishingSpot(e.getPlayer().getLocation());
            if (loc == null) {
                e.getPlayer().sendMessage(ChatColor.RED + "There are " + ChatColor.UNDERLINE + "no" + ChatColor.RED + " populated fishing spots near this location.");
                e.getPlayer().sendMessage(ChatColor.GRAY + "Look for particles above water blocks to signify active fishing spots.");
                e.setCancelled(true);
                return;
            }
            int rodTier = Fishing.getRodTier(pl.getEquipment().getItemInMainHand());
            int areaTier = Fishing.getInstance().getFishingSpotTier(loc);
            if (areaTier > rodTier) {
                e.getPlayer().sendMessage(ChatColor.RED + "This area is a Tier " + areaTier + " fishing zone.");
                e.getPlayer().sendMessage(ChatColor.RED + "Your current pole is too weak to catch any fish here.");
            }
        }

        if (e.getState() == State.CAUGHT_FISH) {
            Random random = new Random();
            final Location fish_loc = Fishing.getInstance().getFishingSpot(pl.getLocation());
            final int spot_tier = Fishing.getInstance().getFishingSpotTier(pl.getLocation());
            if (e.getCaught() != null)
                e.getCaught().remove();

            if (fish_loc == null || spot_tier == -1) {
                pl.sendMessage(ChatColor.RED + "You must be near a Fishing Location to catch fish!");
                return;
            }

            int duraBuff = Fishing.getDurabilityBuff(pl.getEquipment().getItemInMainHand());

            pl.sendMessage(ChatColor.GRAY + "You examine your catch... ");
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                int do_i_get_fish = new Random().nextInt(100);

                int item_tier = Fishing.getRodTier(pl.getEquipment().getItemInMainHand());
                int success_rate = 0;

                if (item_tier > spot_tier) {
                    success_rate = 100;
                }
                if (item_tier == spot_tier) {
                    int lvl = CraftItemStack.asNMSCopy(pl.getEquipment().getItemInMainHand()).getTag().getInt("level");
                    success_rate = 50 + (2 * (20 - Math.abs((Fishing.getNextLevelUp(item_tier) - lvl))));
                }

                int success_mod = Fishing.getSuccessChance(pl.getEquipment().getItemInMainHand());
                success_rate += success_mod; // %CHANCE

                if (TutorialIsland.onTutorialIsland(pl.getUniqueId())) {
                    QuestPlayerData quests = Quests.getInstance().playerDataMap.get(pl);
                    if (quests != null) {
                        for (Quest q : quests.getCurrentQuests()) {
                            if (q != null && q.getQuestName().equalsIgnoreCase("Tutorial Island")) {
                                QuestPlayerData.QuestProgress qp = quests.getQuestProgress(q);
                                if (qp != null && qp.getCurrentStage() != null && qp.getCurrentStage().getObjective() != null) {
                                    if (qp.getCurrentStage().getObjective().getClass().equals(ObjectiveCatchFish.class)) {
                                        success_rate = 100;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                if (success_rate <= do_i_get_fish) {
                    pl.sendMessage(ChatColor.RED + "It got away..");
                    if (new Random().nextInt(100) > duraBuff) {
                        RepairAPI.subtractCustomDurability(pl, pl.getEquipment().getItemInMainHand(), 1);
                    }
                    return;
                }

                if (Fishing.isDRFishingPole(pl.getEquipment().getItemInMainHand())) {
                    // They get fish!
                    ItemStack fish = Fishing.getFishDrop(spot_tier);
                    if (pl.getInventory().firstEmpty() != -1) {
                        pl.getInventory().setItem(pl.getInventory().firstEmpty(), fish);
                    } else {
                        // Full inventory!
                        pl.getWorld().dropItem(pl.getLocation(), fish);
                    }
                    if (new Random().nextInt(100) > duraBuff) {
                        RepairAPI.subtractCustomDurability(pl, pl.getEquipment().getItemInMainHand(), 2);
                    }
                    pl.sendMessage(ChatColor.GREEN + "... you caught some " + fish.getItemMeta().getDisplayName() + ChatColor.GREEN + "!");

                    int exp = Fishing.getFishEXP(Fishing.getFishTier(fish));
                    Fishing.gainExp(pl.getEquipment().getItemInMainHand(), pl, exp);
                    GamePlayer gamePlayer = GameAPI.getGamePlayer(pl);
                    if (gamePlayer == null) return;
                    gamePlayer.addExperience(exp / 8, false, true);
                    gamePlayer.getPlayerStatistics().setFishCaught(gamePlayer.getPlayerStatistics().getFishCaught() + 1);
                    int doi_double_drop = new Random().nextInt(100) + 1;
                    if (Fishing.getDoubleDropChance(pl.getEquipment().getItemInMainHand()) >= doi_double_drop) {
                        fish = Fishing.getFishDrop(spot_tier);
                        if (pl.getInventory().firstEmpty() != -1) {
                            pl.getInventory().setItem(pl.getInventory().firstEmpty(), fish);
                        } else {
                            // Full inventory!
                            pl.getWorld().dropItem(pl.getLocation(), fish);
                        }
                        if ((boolean) DatabaseAPI.getInstance().getData(PlayerManager.PlayerToggles.DEBUG.getDbField(), pl.getUniqueId())) {
                            pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "          DOUBLE FISH CATCH" + ChatColor.YELLOW + " (2x)");
                        }
                        gamePlayer.getPlayerStatistics().setFishCaught(gamePlayer.getPlayerStatistics().getFishCaught() + 1);
                    }

                    int doi_triple_drop = new Random().nextInt(100) + 1;
                    if (Fishing.getTripleDropChance(pl.getEquipment().getItemInMainHand()) >= doi_triple_drop) {
                        fish = Fishing.getFishDrop(spot_tier);
                        if (pl.getInventory().firstEmpty() != -1) {
                            pl.getInventory().setItem(pl.getInventory().firstEmpty(), fish);
                        } else {
                            // Full inventory!
                            pl.getWorld().dropItem(pl.getLocation(), fish);
                        }

                        fish = Fishing.getFishDrop(spot_tier);
                        if (pl.getInventory().firstEmpty() != -1) {
                            pl.getInventory().setItem(pl.getInventory().firstEmpty(), fish);
                        } else {
                            // Full inventory!
                            pl.getWorld().dropItem(pl.getLocation(), fish);
                        }
                        if ((boolean) DatabaseAPI.getInstance().getData(PlayerManager.PlayerToggles.DEBUG.getDbField(), pl.getUniqueId())) {
                            pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "          TRIPLE FISH CATCH" + ChatColor.YELLOW + " (3x)");
                        }
                        gamePlayer.getPlayerStatistics().setFishCaught(gamePlayer.getPlayerStatistics().getFishCaught() + 2);
                    }

                    int junk_chance = Fishing.getJunkFindChance(pl.getEquipment().getItemInMainHand());
                    if (junk_chance >= (new Random().nextInt(100) + 1)) {
                        int junk_type = new Random().nextInt(100) + 1; // 0, 1, 2
                        ItemStack junk = null;

                        if (junk_type < 70) {
                            if (spot_tier == 1) {
                                junk = ItemManager.createHealthPotion(1, false, false);
                                junk.setAmount(5 + new Random().nextInt(3));
                            }
                            if (spot_tier == 2) {
                                junk = ItemManager.createHealthPotion(2, false, false);
                                junk.setAmount(4 + new Random().nextInt(3));
                            }
                            if (spot_tier == 3) {
                                junk = ItemManager.createHealthPotion(3, false, false);
                                junk.setAmount(2 + new Random().nextInt(3));
                            }
                            if (spot_tier == 4) {
                                junk = ItemManager.createHealthPotion(4, false, false);
                                junk.setAmount(1 + new Random().nextInt(3));
                            }
                            if (spot_tier == 5) {
                                junk = ItemManager.createHealthPotion(5, false, false);
                                junk.setAmount(1 + new Random().nextInt(3));
                            }
                        }

                        if (junk_type > 70 && junk_type < 95) {
                            if (spot_tier == 1) {
                                junk = ItemManager.createArmorScrap(1);
                                junk.setAmount(20 + new Random().nextInt(7));
                            }
                            if (spot_tier == 2) {
                                junk = ItemManager.createArmorScrap(2);
                                junk.setAmount(15 + new Random().nextInt(7));
                            }
                            if (spot_tier == 3) {
                                junk = ItemManager.createArmorScrap(3);
                                junk.setAmount(10 + new Random().nextInt(7));
                            }
                            if (spot_tier == 4) {
                                junk = ItemManager.createArmorScrap(4);
                                junk.setAmount(5 + new Random().nextInt(7));
                            }
                            if (spot_tier == 5) {
                                junk = ItemManager.createArmorScrap(5);
                                junk.setAmount(2 + new Random().nextInt(6));
                            }
                        }

                        if (junk_type >= 95) {
                            int tier = spot_tier;
                            if (spot_tier == 4) {
                                int tierChance = random.nextInt(100);
                                if (tierChance <= 70) {
                                    tier = 3;
                                }
                            } else if (spot_tier == 5) {
                                int tierChance = random.nextInt(100);
                                if (tierChance <= 70) {
                                    tier = 3;
                                } else if (tierChance <= 95) {
                                    tier = 4;
                                } else {
                                    tier = 5;
                                }
                            }
                            junk = new ItemGenerator().setTier(net.dungeonrealms.game.world.item.Item.ItemTier.getByTier(tier))
                                    .setType(random.nextBoolean() ? net.dungeonrealms.game.world.item.Item.ItemType.getRandomArmor() :
                                            net.dungeonrealms.game.world.item.Item.ItemType.getRandomWeapon())
                                    .setRarity(net.dungeonrealms.game.world.item.Item.ItemRarity.COMMON).generateItem().getItem();
                        }

                        if (junk != null) {
                            int item_count = junk.getAmount();
                            if (junk.getType() == Material.POTION) {
                                // Not stackable.
                                int amount = junk.getAmount();
                                junk.setAmount(1);
                                while (amount > 0) {
                                    amount--;
                                    if (pl.getInventory().firstEmpty() != -1) {
                                        pl.getInventory().setItem(pl.getInventory().firstEmpty(), junk);
                                    } else {
                                        // Full inventory!
                                        pl.getWorld().dropItem(pl.getLocation(), junk);
                                    }
                                }
                            } else {
                                if (pl.getInventory().firstEmpty() != -1) {
                                    pl.getInventory().setItem(pl.getInventory().firstEmpty(), junk);
                                } else {
                                    // Full inventory!
                                    pl.getWorld().dropItem(pl.getLocation(), junk);
                                }
                            }

                            pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "  YOU FOUND SOME JUNK! -- " + item_count + "x "
                                    + junk.getItemMeta().getDisplayName());
                        }
                    }

                    int treasure_chance = Fishing.getTreasureFindChance(pl.getEquipment().getItemInMainHand());
                    if (treasure_chance >= (new Random().nextInt(300) + 1)) {
                        // Give em treasure!
                        int treasure_type = new Random().nextInt(3); // 0, 1
                        ItemStack treasure = null;
                        if (treasure_type == 0) {
                            // OOA
                            treasure = CraftItemStack.asCraftCopy(ItemManager.createOrbofAlteration());
                        } else if (treasure_type == 1) {
                            int tier = spot_tier;
                            if (spot_tier == 4) {
                                int tierChance = random.nextInt(100);
                                if (tierChance <= 70) {
                                    tier = 3;
                                }
                            } else if (spot_tier == 5) {
                                int tierChance = random.nextInt(100);
                                if (tierChance <= 70) {
                                    tier = 3;
                                } else if (tierChance <= 95) {
                                    tier = 4;
                                } else {
                                    tier = 5;
                                }
                            }
                            net.dungeonrealms.game.world.item.Item.ItemRarity rarity =
                                    random.nextInt(100) <= 75 ? net.dungeonrealms.game.world.item.Item.ItemRarity.UNCOMMON : net.dungeonrealms.game.world.item.Item.ItemRarity.RARE;
                            treasure = new ItemGenerator().setTier(net.dungeonrealms.game.world.item.Item.ItemTier.getByTier(tier))
                                    .setType(random.nextInt(100) <= 75 ? net.dungeonrealms.game.world.item.Item.ItemType.getRandomArmor() :
                                            net.dungeonrealms.game.world.item.Item.ItemType.getRandomWeapon())
                                    .setRarity(rarity).generateItem().getItem();
                        } else if (treasure_type == 2) {
                            treasure = ItemManager.createOrbofFlight(true);
                        }

                        if (treasure != null) {

                            if (pl.getInventory().firstEmpty() != -1) {
                                pl.getInventory().setItem(pl.getInventory().firstEmpty(), treasure);
                            } else {
                                // Full inventory!
                                pl.getWorld().dropItem(pl.getLocation(), treasure);
                            }

                            pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "  YOU FOUND SOME TREASURE! -- a(n) "
                                    + treasure.getItemMeta().getDisplayName());
                        }
                    }
                }

                Quests.getInstance().triggerObjective(pl, ObjectiveCatchFish.class);
            }, 10L);
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
                itemStack = BankMechanics.createBankNote(itemStack.getAmount(), player);
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

    /**
     * Checks for player punching a map on a wall
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerHitMap(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player && event.getEntity() instanceof ItemFrame) {
            Player player = (Player) event.getRemover();
            ItemFrame itemFrame = (ItemFrame) event.getEntity();
            if (player.getInventory().firstEmpty() != -1 && (itemFrame.getItem().getType() == Material.MAP)) {
                ItemStack map = itemFrame.getItem();
                if (!(player.getInventory().contains(map))) {
                    player.getInventory().addItem(map);
                    player.updateInventory();
                    player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1F, 0.8F);
                    Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.CARTOGRAPHER);
                }
            }
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

    /**
     * Checks for player punching a map on a wall
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerHitMapItemFrame(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() == EntityType.ITEM_FRAME) {
            ItemFrame is = (ItemFrame) event.getEntity();
            is.setItem(is.getItem());
            is.setRotation(Rotation.NONE);
            event.setCancelled(true);
            if (event.getDamager() instanceof Player) {
                if (is.getItem().getType() != Material.MAP) return;
                Player plr = (Player) event.getDamager();
                if (plr.getInventory().contains(is.getItem())) {
                    return;
                }
                plr.getInventory().addItem(is.getItem());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (GameAPI.getGamePlayer(player).isSharding() || player.hasMetadata("sharding")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        GamePlayer gp = GameAPI.getGamePlayer(player);
        if (player.hasMetadata("sharding") || !gp.isAbleToOpenInventory() || gp.isSharding()) {
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

    /**
     * Prevents players from shearing sheep etc.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerShearEntityEvent(PlayerShearEntityEvent event) {
        if (event.getPlayer().isOp()) {
            return;
        }
        event.setCancelled(true);
    }

    /**
     * Prevents players from dropping maps
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMapDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().hasMetadata("sharding")) event.setCancelled(true);
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(event.getItemDrop().getItemStack());
        if (!(event.isCancelled())) {
            Player pl = event.getPlayer();
            // The maps gonna drop! DESTROY IT!
            if (event.getItemDrop().getItemStack().getType() == Material.MAP) {
                event.getItemDrop().remove();
                if (pl.getEquipment().getItemInMainHand().getType() == Material.MAP) {
                    pl.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
                } else if (pl.getItemOnCursor().getType() == Material.MAP) {
                    pl.setItemOnCursor(new ItemStack(Material.AIR));
                }
                pl.playSound(pl.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1F, 2F);
                pl.updateInventory();
                return;
            }
            if (nms == null || !nms.hasTag())
                return;
            if (nms.getTag().hasKey("subtype")) event.getItemDrop().remove();
            if (nms.getTag().hasKey("dataType")) event.getItemDrop().remove();
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void playerEnchant(EnchantItemEvent event) {
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void playerAttemptTrade(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;
        if (!GameAPI.isItemDroppable(event.getItemDrop().getItemStack())) return;

        Player pl = event.getPlayer();

        Player trader = TradeManager.getTarget(pl);
        if (trader == null)
            return;

        if (GameAPI._hiddenPlayers.contains(trader) || trader.getGameMode() == GameMode.SPECTATOR) return;

        if (!(boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_TRADE, trader.getUniqueId()) && !Rank.isTrialGM(pl)) {
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

        if (pl.getItemOnCursor() != null) {
            pl.setItemOnCursor(new ItemStack(Material.AIR));
        }

        Cooldown.addCooldown(event.getPlayer().getUniqueId(), 20 * 5);
        TradeManager.startTrade(pl, trader);
        Trade trade = TradeManager.getTrade(pl.getUniqueId());
        if (trade == null) {
            return;
        }
        trader.playSound(trader.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1F, 0.8F);
        pl.playSound(pl.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1F, 0.8F);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void chunkUnload(ChunkUnloadEvent event) {
        if (event.getWorld() == Bukkit.getWorlds().get(0)) {
            if (event.getChunk().getEntities().length > 0) {
                for (Entity entity : event.getChunk().getEntities()) {
                    if (!(entity instanceof org.bukkit.entity.Item) && !(entity instanceof Player)) {
                        if (!(entity instanceof ItemFrame) && !(entity instanceof Painting) && !(entity instanceof Hanging)) {
                            entity.remove();
                        }
                    }
                }
            }
        } else if (event.getWorld().getName().contains("DUNGEON")) {
            event.setCancelled(true);
        } else {
            if (event.getChunk().getEntities().length > 0) {
                for (Entity entity : event.getChunk().getEntities()) {
                    if (!(entity instanceof Player)) {
                        if (!(entity instanceof ItemFrame) && !(entity instanceof Painting) && !(entity instanceof Hanging)) {
                            entity.remove();
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void chunkLoad(ChunkLoadEvent event) {
        if (event.getWorld() == Bukkit.getWorlds().get(0)) {
            if (event.getChunk().getEntities().length > 0) {
                for (Entity entity : event.getChunk().getEntities()) {
                    if (!(entity instanceof org.bukkit.entity.Item) && !(entity instanceof Player)) {
                        if (!(entity instanceof ItemFrame) && !(entity instanceof Painting) && !(entity instanceof Hanging)) {
                            entity.remove();
                        }
                    }
                }
            }
        } else {
            if (event.getChunk().getEntities().length > 0) {
                for (Entity entity : event.getChunk().getEntities()) {
                    if (!(entity instanceof Player)) {
                        if (!(entity instanceof ItemFrame) && !(entity instanceof Painting) && !(entity instanceof Hanging)) {
                            entity.remove();
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        if (event.getItem().getItemStack().getAmount() <= 0) {
            event.setCancelled(true);
            event.getItem().remove();
            return;
            //Prevent weird MC glitch.
        }
        if (event.getItem().getItemStack().getType() == Material.ARROW) {
            event.setCancelled(true);
            event.getItem().remove();
            return;
        }
        if (event.getItem().getItemStack().getType() != Material.EMERALD) {
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void avalonTP(PlayerEnterRegionEvent event) {
        if (event.getRegion().equalsIgnoreCase("teleport_underworld")) {
            event.getPlayer().teleport(Teleportation.Underworld);
        } else if (event.getRegion().equalsIgnoreCase("teleport_overworld")) {
            event.getPlayer().teleport(Teleportation.Overworld);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void playerInteractMule(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Horse)) return;
        Horse horse = (Horse) event.getRightClicked();
        event.setCancelled(true);
        if (horse.getVariant() != Variant.MULE) return;
        if (horse.getOwner() == null) {
            horse.remove();
            return;
        }
        Player p = event.getPlayer();
        if (horse.getOwner().getUniqueId().toString().equalsIgnoreCase(event.getPlayer().getUniqueId().toString())) {
            horse.setLeashHolder(p);
            Inventory inv = MountUtils.inventories.get(p.getUniqueId());
            p.openInventory(inv);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void unLeashMule(EntityUnleashEvent event) {
        if (!(event.getEntity() instanceof Horse)) return;
        Horse horse = (Horse) event.getEntity();
        if (horse.getVariant() != Variant.MULE) return;
        if (!event.getReason().equals(UnleashReason.PLAYER_UNLEASH)) {
            horse.remove();
            return;
        }
        if (horse.getOwner() == null) {
            horse.remove();
            return;
        }
        horse.setLeashHolder((Player) horse.getOwner());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void chickenLayEgg(ItemSpawnEvent event) {
        if (event.getEntityType() != EntityType.EGG) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDragonEggMove(BlockFromToEvent event) {
        if (event.getBlock().getType() == Material.DRAGON_EGG) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void entityTarget(EntityTargetEvent event) {
        if (event.getTarget() != null) {
            if (!(event.getTarget() instanceof Player)) {
                event.setCancelled(true);
            } else if (GameAPI.isInSafeRegion(event.getTarget().getLocation())) {
                event.setCancelled(true);
            } else {
                GamePlayer gp = GameAPI.getGamePlayer((Player) event.getTarget());
                if (gp != null && !gp.isTargettable()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void explosionDungeon(EntityExplodeEvent event) {
        if (event.getEntity().getWorld().getName().contains("DUNGEON")) {
            event.blockList().forEach(block -> block.setType(Material.AIR));
            event.setYield(0);
            event.blockList().clear();
            event.getEntity().remove();
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void characterJournalPartyInvite(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            if (!GameAPI.isPlayer(event.getEntity())) return;
            if (((Player) event.getDamager()).getEquipment().getItemInMainHand() != null) {
                ItemStack stack = ((Player) event.getDamager()).getEquipment().getItemInMainHand();
                if (stack.getType() == Material.WRITTEN_BOOK) {
                    event.setCancelled(true);
                    event.setDamage(0);
                    Player player = (Player) event.getDamager();
                    Player invite = (Player) event.getEntity();
                    if (Affair.getInstance().isInParty(invite)) {
                        player.sendMessage(ChatColor.RED + "That player is already in a party!");
                    } else {
                        if (Affair.getInstance().isInParty(player)) {
                            if (Affair.getInstance().isOwner(player)) {
                                if (Affair.getInstance().getParty(player).get().getMembers().size() >= 7) {
                                    player.sendMessage(ChatColor.RED + "Your party has reached the max player count!");
                                    return;
                                }
                                Affair.getInstance().invitePlayer(invite, player);
                            } else {
                                player.sendMessage(new String[]{
                                        ChatColor.RED + "You are NOT the leader of your party.",
                                        ChatColor.GRAY + "Type " + ChatColor.BOLD + "/pquit" + ChatColor.GRAY + " to quit your current party."
                                });
                            }
                        } else {
                            Affair.getInstance().createParty(player);
                            Affair.getInstance().invitePlayer(invite, player);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void preventNaturalHealthRegen(EntityRegainHealthEvent e) {
        e.setCancelled(true);
    }
}
