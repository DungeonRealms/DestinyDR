package net.dungeonrealms.updated.connection.player;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.player.rank.Subscription;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.game.achievements.AchievementManager;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.handler.EnergyHandler;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.DungeonManager;
import net.dungeonrealms.game.mechanic.PlayerManager;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.player.notice.Notice;
import net.dungeonrealms.game.world.entity.type.mounts.mule.MuleTier;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.updated.connection.pipeline.DataPipeline;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Giovanni on 23-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class PlayerJoinPipeline extends DataPipeline {

    private ConcurrentHashMap<UUID, GamePlayer> processing;

    public PlayerJoinPipeline() {
        this.processing = new ConcurrentHashMap<>();

        // Check if a gameplayer is processing, and if not allow them to interact again
        DungeonRealms.getInstance().getServer().getScheduler().scheduleAsyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            // Is the player not processing & is not allowed to interact?
            GameAPI.GAMEPLAYERS.values().stream().filter(gamePlayer -> !this.processing.containsKey(gamePlayer.getPlayer().getUniqueId()))
                    .filter(gamePlayer -> !gamePlayer.isAbleToDrop() && !gamePlayer.isAbleToSuicide()).forEach(gamePlayer -> {
                gamePlayer.setAbleToDrop(true);
                gamePlayer.setAbleToSuicide(true);
            });
        }, 0L, 25);
    }


    @Override
    public void handle(UUID uniqueId) {
        if (Bukkit.getPlayer(uniqueId) != null && Bukkit.getPlayer(uniqueId).isOnline()) {
            Player player = Bukkit.getPlayer(uniqueId);
            // Can the pipeline accept this player?
            if (this.canAccept(uniqueId)) {
                // Login
                Utils.sendCenteredMessage(player, ChatColor.GREEN + "PLEASE WAIT WHILST YOUR DATA IS BEING CONVERTED");
                DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$SET, EnumData.FIRST_LOGIN, System.currentTimeMillis(), true);
                // Cache the gameplayer
                GameAPI.GAMEPLAYERS.put(player.getName(), new GamePlayer(player));
                this.processing.put(uniqueId, GameAPI.getGamePlayer(player));
                // Prevent the player from doing things
                this.preventInteraction(uniqueId);
                // Load the inventory data
                this.loadInventory(uniqueId);
                player.setGameMode(GameMode.SURVIVAL);
                // Achievements
                this.checkAchievements(uniqueId);
                // Handlers
                this.loadHandlers(uniqueId);
                // Finished
                this.processing.remove(uniqueId);
                Utils.sendCenteredMessage(player, ChatColor.GREEN + "DATA CONVERSION FINISHED");
                // Server info
                this.sendServerData(uniqueId);
                // Done after 3s to prevent chat spamming
                DungeonRealms.getInstance().getServer().getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> finish(uniqueId), 60);
            }
        }
    }

    /**
     * Let all game handlers listen for a player
     *
     * @param uniqueId The unique id
     */
    private void loadHandlers(UUID uniqueId) {
        Player player = Bukkit.getPlayer(uniqueId);
        EnergyHandler.getInstance().handleLoginEvents(player);
        KarmaHandler.getInstance().handleLoginEvents(player);
        Subscription.getInstance().handleLogin(player);
        GuildMechanics.getInstance().doLogin(player);
        Notice.getInstance().doLogin(player);
    }

    /**
     * Finish a player's login process
     *
     * @param uniqueId The unique id of the player
     */
    private void finish(UUID uniqueId) {
        Player player = Bukkit.getPlayer(uniqueId);
        GamePlayer gamePlayer = GameAPI.getGamePlayer(player);
        int freeEcash = (int) (Long.valueOf(DatabaseAPI.getInstance().getData(EnumData.FREE_ECASH, uniqueId).toString()) / 1000);
        int currentTime = (int) (System.currentTimeMillis() / 1000);
        if (currentTime - freeEcash >= 86400) {
            int ecashReward = Utils.randInt(10, 15);
            DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$SET, EnumData.FREE_ECASH, System.currentTimeMillis(), true);
            DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$INC, EnumData.ECASH, ecashReward, true);
            player.sendMessage(new String[]{"",
                    ChatColor.GOLD + "You have gained " + ChatColor.BOLD + ecashReward + "EC" + ChatColor.GOLD + " for logging into DungeonRealms today!",
                    ChatColor.GRAY + "Use /ecash to spend your EC, you can obtain more e-cash by logging in daily or by visiting " + ChatColor.GOLD + ChatColor.UNDERLINE + "http://www.dungeonrealms.net/shop"
            });
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
        }

        DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$SET, EnumData.USERNAME, player.getName().toLowerCase(), true);
        DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$SET, EnumData.CURRENTSERVER, DungeonRealms.getInstance().bungeeName, true);
        DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$SET, EnumData.IS_PLAYING, true, true);

        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("IP");

            player.sendPluginMessage(DungeonRealms.getInstance(), "BungeeCord", out.toByteArray());
        });

        GameAPI.sendNetworkMessage("Friends", "join:" + " ," + player.getUniqueId().toString() + "," + player.getName() + "," + DungeonRealms.getInstance().shardid);

        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> AchievementManager.getInstance().handleLogin(player.getUniqueId()), 70L);
        // Allow citizens conversations
        player.addAttachment(DungeonRealms.getInstance()).setPermission("citizens.npc.talk", true);
        // Remove minecraft swing
        AttributeInstance instance = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        instance.setBaseValue(1024.0D);
        // Permissions
        if (!player.isOp() && !Rank.isDev(player)) {
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.plugins", false);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.version", false);
        }

        if (Rank.isPMOD(player)) {
            player.addAttachment(DungeonRealms.getInstance()).setPermission("nocheatplus.notify", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("nocheatplus.command.notify", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("nocheatplus.command.info", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("nocheatplus.command.inspect", true);
        }

        if (Rank.isGM(player)) {
            player.addAttachment(DungeonRealms.getInstance()).setPermission("essentials.*", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("citizens.*", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("worldedit.*", true);

            player.addAttachment(DungeonRealms.getInstance()).setPermission("nocheatplus.checks", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("nocheatplus.bypass.denylogin", true);

            //Don't think these will work as they default to Operators in MC.
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.gamemode", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("minecraft.command.gamemode", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("bukkit.command.teleport", true);
            player.addAttachment(DungeonRealms.getInstance()).setPermission("minecraft.command.tp", true);
        }

        // calculate attributes and check inventory
        Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
            PlayerManager.checkInventory(uniqueId);
            GameAPI.calculateAllAttributes(player);
        }, 40);

        if (gamePlayer.getPlayer() != null) {
            Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
                if (gamePlayer.getStats().freePoints > 0) {
                    player.sendMessage("");
                    final JSONMessage normal = new JSONMessage(ChatColor.GREEN + "*" + ChatColor.GRAY + "You have available " + ChatColor.GREEN + "stat points. " + ChatColor.GRAY +
                            "To allocate click ", ChatColor.WHITE);
                    normal.addRunCommand(ChatColor.GREEN.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE!", ChatColor.GREEN, "/stats");
                    normal.addText(ChatColor.GREEN + "*");
                    normal.sendToPlayer(gamePlayer.getPlayer());
                }
            }, 100);
        }

        if (Rank.isGM(player)) {
            gamePlayer.setInvulnerable(true);
            gamePlayer.setTargettable(false);
            player.sendMessage("");

            Utils.sendCenteredMessage(player, ChatColor.AQUA + ChatColor.BOLD.toString() + "GM INVINCIBILITY");

            // check vanish
            final Object isVanished = DatabaseAPI.getInstance().getData(EnumData.TOGGLE_VANISH, player.getUniqueId());
            if (isVanished != null && (Boolean) isVanished) {
                GameAPI._hiddenPlayers.add(player);
                player.setCustomNameVisible(false);
                Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(player));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
                Utils.sendCenteredMessage(player, ChatColor.AQUA + ChatColor.BOLD.toString() + "GM VANISH");
                player.setGameMode(GameMode.SPECTATOR);
            } else {
                player.setGameMode(GameMode.CREATIVE);
            }
        }
    }

    /**
     * Notify a player about the current server
     *
     * @param uniqueId The unique id of the player
     */
    private void sendServerData(UUID uniqueId) {
        Player player = Bukkit.getPlayer(uniqueId);
        // Clear chat
        for (int i = 0; i < 20; i++) {
            player.sendMessage("");
        }
        // Send information
        if (!DungeonRealms.getInstance().isMasterShard) {
            Utils.sendCenteredMessage(player, ChatColor.RED.toString() + ChatColor.BOLD + "-> NOTIFICATION");
            player.sendMessage(new String[]{
                    ChatColor.RED + "You are playing on an unstable version of Dungeon Realms -",
                    ChatColor.RED + "This server is running fixed 2016 code, everything is",
                    "being rewritten as we speak - www.dungeonrealms.net"});
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1f, 1f);
        } else {
            Utils.sendCenteredMessage(player, ChatColor.AQUA.toString() + ChatColor.BOLD + "DEVELOPMENT SERVER");
        }
    }

    /**
     * Check if a player has a specific set of achievements
     *
     * @param uniqueId The unique id of the player
     */
    private void checkAchievements(UUID uniqueId) {
        Player player = Bukkit.getPlayer(uniqueId);
        if (Rank.isDev(player)) {
            Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.DEVELOPER);
            Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.INFECTED);
        }

        if (Rank.isGM(player)) {
            Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.GAME_MASTER);
        }

        if (Rank.isSupport(player)) {
            Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.SUPPORT_AGENT);
        }

        if (Rank.isPMOD(player)) {
            Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.PLAYER_MOD);
        }
        if (Rank.isSubscriber(player)) {
            String rank = Rank.getInstance().getRank(player.getUniqueId()).toLowerCase();
            if (!rank.equals("pmod")) {
                Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.SUBSCRIBER);
                if (!rank.equals("sub")) {
                    Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.SUBSCRIBER_PLUS);
                    if (!rank.equals("sub+")) {
                        Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.SUBSCRIBER_PLUS_PLUS);
                    }
                }
            }
        }
    }

    /**
     * Prevent a player from interacting
     *
     * @param uniqueId The unique id of the player
     */
    private void preventInteraction(UUID uniqueId) {
        Player player = Bukkit.getPlayer(uniqueId);
        if (GameAPI.GAMEPLAYERS.containsKey(player.getName())) {
            GamePlayer gamePlayer = GameAPI.getGamePlayer(player);
            gamePlayer.setAbleToDrop(false);
            gamePlayer.setAbleToSuicide(false);
            DungeonManager.getInstance().getPlayers_Entering_Dungeon().put(player.getName(), 60);
            // Allow them to interact again when the process is finished, auto-checked
        } else {
            player.kickPlayer(ChatColor.RED + "Failed to load your data - Please reconnect");
        }
    }

    /**
     * Load a player's inventory data
     *
     * @param uniqueId The unique id of the player
     */
    private void loadInventory(UUID uniqueId) {
        Player player = Bukkit.getPlayer(uniqueId);
        // Load
        String playerInv = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY, uniqueId);
        if (playerInv != null && playerInv.length() > 0 && !playerInv.equalsIgnoreCase("null")) {
            ItemStack[] items = ItemSerialization.fromString(playerInv, 36).getContents();
            player.getInventory().setContents(items);
            player.updateInventory();
        }
        List<String> playerArmor = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.ARMOR, player.getUniqueId());
        int i = -1;
        ItemStack[] armorContents = new ItemStack[4];
        ItemStack offHand = new ItemStack(Material.AIR);
        for (String armor : playerArmor) {
            i++;
            if (i <= 3) { //Normal armor piece
                if (armor.equals("null") || armor.equals("")) {
                    armorContents[i] = new ItemStack(Material.AIR);
                } else {
                    armorContents[i] = ItemSerialization.itemStackFromBase64(armor);
                }
            } else {
                if (armor.equals("null") || armor.equals("")) {
                    offHand = new ItemStack(Material.AIR);
                } else {
                    offHand = ItemSerialization.itemStackFromBase64(armor);
                }
            }
        }
        player.getEquipment().setArmorContents(armorContents);
        player.getEquipment().setItemInOffHand(offHand);

        player.updateInventory();
        String source = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_STORAGE, uniqueId);
        if (source != null && source.length() > 0 && !source.equalsIgnoreCase("null")) {
            Inventory inv = ItemSerialization.fromString(source);
            Storage storageTemp = new Storage(uniqueId, inv);
            BankMechanics.storage.put(uniqueId, storageTemp);
        } else {
            Storage storageTemp = new Storage(uniqueId);
            BankMechanics.storage.put(uniqueId, storageTemp);
        }
        String invString = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_MULE, player.getUniqueId());
        int muleLevel = (int) DatabaseAPI.getInstance().getData(EnumData.MULELEVEL, player.getUniqueId());
        if (muleLevel > 3) {
            muleLevel = 3;
        }
        MuleTier tier = MuleTier.getByTier(muleLevel);
        Inventory muleInv = null;
        if (tier != null) {
            muleInv = Bukkit.createInventory(player, tier.getSize(), "Mule Storage");
            if (!invString.equalsIgnoreCase("") && !invString.equalsIgnoreCase("empty") && invString.length() > 4) {
                //Make sure the inventory is as big as we need
                muleInv = ItemSerialization.fromString(invString, tier.getSize());
            }
        }
        if (!invString.equalsIgnoreCase("") && !invString.equalsIgnoreCase("empty") && invString.length() > 4 && muleInv != null) {
            MountUtils.inventories.put(player.getUniqueId(), muleInv);
        }
    }

    /**
     * Check if a player's connection can be accepted
     *
     * @param uniqueId The unique id of the player
     * @return Boolean
     */
    private boolean canAccept(UUID uniqueId) {
        Player player = Bukkit.getPlayer(uniqueId);
        // Player is cached?
        if (DatabaseAPI.getInstance().PLAYERS.containsKey(uniqueId)) {
            return true;
        } else player.kickPlayer(ChatColor.RED + "Failed to load your data - Please reconnect");
        // Server is not finished yet, but the player is a developer?
        if (!DungeonRealms.getInstance().canAcceptPlayers() && Rank.isDev(Bukkit.getPlayer(uniqueId))) {
            return true;
        } else player.kickPlayer(ChatColor.RED + "This server is not prepared yet, please wait");
        // Server is up?
        if (DungeonRealms.getInstance().canAcceptPlayers()) {
            return true;
        } else player.kickPlayer(ChatColor.RED + "This server is not prepared yet, please wait");
        // Player is allowed to join this server?
        if ((DungeonRealms.getInstance().isYouTubeShard && Rank.isYouTuber(player)) || (DungeonRealms.getInstance().isSupportShard && Rank.isSupport(player))) {
            return true;
        } else
            player.kickPlayer(ChatColor.RED + "You are " + ChatColor.UNDERLINE + "not" + ChatColor.RED + " authorized to connect to this shard.");
        // Player has a premium rank for this server?
        if (DungeonRealms.getInstance().isSubscriberShard && !Rank.getInstance().getRank(player.getUniqueId()).equalsIgnoreCase("default")) {
            return true;
        } else
            player.kickPlayer(ChatColor.RED + "You are " + ChatColor.UNDERLINE + "not" + ChatColor.RED + " authorized to connect to a subscriber only shard.\n\n" +
                    ChatColor.GRAY + "Subscriber at http://www.dungeonrealms.net/shop to gain instant access!");
        // Is the player combat logged?
        if (!this.combatLogged(uniqueId)) {
            return true;
        }
        return false;
    }

    /**
     * Check if a player is combat logged
     *
     * @param uniqueId The unique id of the player
     * @return Boolean
     */
    private boolean combatLogged(UUID uniqueId) {
        Player player = Bukkit.getPlayer(uniqueId);
        if ((Boolean) DatabaseAPI.getInstance().getData(EnumData.IS_COMBAT_LOGGED, uniqueId)) {
            if (!DatabaseAPI.getInstance().getData(EnumData.CURRENTSERVER, uniqueId).equals(DungeonRealms.getShard().getPseudoName())) {
                String lastShard = ShardInfo.getByPseudoName((String) DatabaseAPI.getInstance().getData(EnumData.CURRENTSERVER, uniqueId)).getShardID();
                player.kickPlayer(ChatColor.RED + "You have been combat logged. Please connect to Shard " + lastShard);
                return true;
            }
        }
        return false;
    }
}
