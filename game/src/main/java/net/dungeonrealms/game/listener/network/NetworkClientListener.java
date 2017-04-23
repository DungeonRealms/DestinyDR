package net.dungeonrealms.game.listener.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.PlayerToken;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.UpdateType;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.world.shops.Shop;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import net.dungeonrealms.network.packet.type.BasicMessagePacket;
import net.dungeonrealms.network.packet.type.ServerListPacket;
import net.minecraft.server.v1_9_R2.IChatBaseComponent;
import net.minecraft.server.v1_9_R2.PacketPlayOutChat;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/15/2016
 */
public class NetworkClientListener extends Listener implements GenericMechanic {

    static NetworkClientListener instance = null;

    private Map<String, Field> cachedPlayerWrapperFields = new HashMap<>();

    public Field getField(String s) {
        Field field = cachedPlayerWrapperFields.get(s);
        if (field == null) {
            try {
                field = PlayerWrapper.class.getDeclaredField(s);
                field.setAccessible(true);
                this.cachedPlayerWrapperFields.put(s, field);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return field;
    }

    public static NetworkClientListener getInstance() {
        if (instance == null) {
            instance = new NetworkClientListener();
        }
        return instance;
    }

    @Override
    public void startInitialization() {
        if (GameAPI.getClient() == null) return;

        Utils.log.info("[NetworkClientListener] Registering client packet listener...");
        GameAPI.getClient().registerListener(this);
    }

    @Override
    public void stopInvocation() {
        if (GameAPI.getClient() == null) return;

        Utils.log.info("[NetworkClientListener] Unregistering client packet listener...");
        GameAPI.getClient().removeListener(this);
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }


    @Override
    public void received(Connection connection, Object object) {

        if (object instanceof ServerListPacket) {
            ServerListPacket packet = (ServerListPacket) object;

            ShardInfo target = packet.target;
            PlayerToken[] tokens = packet.tokens;

            BungeeServerInfo info = BungeeServerTracker.getOrCreateServerInfo(target.getPseudoName());
            info.setPlayers(Arrays.asList(tokens));
            info.updateLastRequest();

        } else if (object instanceof BasicMessagePacket) {
            BasicMessagePacket packet = (BasicMessagePacket) object;

            byte[] data = packet.data;
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));

            try {
                String task = in.readUTF();

                switch (task) {
                    case "Update":
                        UUID uuid = UUID.fromString(in.readUTF());
                        Player player1 = Bukkit.getPlayer(uuid);

                        String updateField = in.readUTF();
                        if (player1 != null) {

                            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player1);
                            if (updateField.equals("mute")) {
                                wrapper.loadPunishment(true);
                            } else if (updateField.equals("unlockables")) {
                                //Reload their collection bin data...
                                SQLDatabaseAPI.getInstance().executeQuery(QueryType.SELECT_UNLOCKABLES.getQuery(wrapper.getAccountID()), rs -> {
                                    if (rs == null) {
                                        Bukkit.getLogger().info("Unable to get result set on unlockable upgrade, account ID: " + wrapper.getAccountID());
                                        return;
                                    }
                                    try {
                                        if (rs.first())
                                            wrapper.loadUnlockables(rs);
                                        Bukkit.getLogger().info("Reloading unlockables for " + player1.getName());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                            } else {
                                UpdateType type = UpdateType.getFromName(updateField);
                                if (type != null) {
                                    //Pull this data..
                                    SQLDatabaseAPI.getInstance().executeQuery(type.getQuery(wrapper), rs -> {
                                        if (rs == null)
                                            return;
                                        try {
                                            if (rs.first()) {
                                                Object obj = rs.getObject(type.getColumnName());
                                                Field field = getField(type.getFieldName());
                                                field.set(wrapper, obj);
                                            }
                                        } catch (SQLException | IllegalAccessException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                }
                            }
//                            DatabaseAPI.getInstance().requestPlayer(uuid, true, () -> Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
//                                if (GameAPI.getGamePlayer(player1) != null)
//                                    ScoreboardHandler.getInstance().setPlayerHeadScoreboard(player1, GameAPI.getGamePlayer(player1).getPlayerAlignment().getAlignmentColor(), GameAPI.getGamePlayer(player1).getLevel());
//                            }));
                        }
                        return;
                    case "IGN_GMMessage":
                        //This GMMessage will only show in-game and skip being show in discord.
                    case "GMMessage": {
                        String msg = ChatColor.translateAlternateColorCodes('&', in.readUTF());
                        Bukkit.getOnlinePlayers().forEach(p -> {
                            if (Rank.isTrialGM(p)) {
                                GamePlayer gp = GameAPI.getGamePlayer(p);
                                if (gp != null && !gp.isStreamMode()) {
                                    p.sendMessage(msg);
                                }
                            }
                        });
                        break;
                    }
                    case "DEVMessage": {
                        String msg = ChatColor.translateAlternateColorCodes('&', in.readUTF());
                        Bukkit.getOnlinePlayers().forEach(p -> {
                            if (Rank.isDev(p)) {
                                GamePlayer gp = GameAPI.getGamePlayer(p);
                                if (gp != null && !gp.isStreamMode()) {
                                    p.sendMessage(msg);
                                }
                            }
                        });
                        break;
                    }
                    case "StaffMessage": {
                        String msg = ChatColor.translateAlternateColorCodes('&', in.readUTF());
                        Bukkit.getOnlinePlayers().forEach(p -> {
                            if (Rank.isPMOD(p) || Rank.isSupport(p)) {
                                GamePlayer gp = GameAPI.getGamePlayer(p);
                                if (gp != null && !gp.isStreamMode()) {
                                    p.sendMessage(msg);
                                }
                            }
                        });
                        break;
                    }
                }

                // Handle packet sync //
                Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
                    try {
                        switch (task) {
                            case "Friends": {
                                String msg = in.readUTF();
                                if (msg.contains("join:")) {
                                    String[] content = msg.split(",");
                                    String uuidString = content[1];
                                    String name = content[2];
                                    String shard = content[3];
                                    UUID uuid = UUID.fromString(uuidString);

                                    if (Bukkit.getPlayer(uuid) != null) {
                                        /*Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
                                            ArrayList<String> list = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.FRIENDS, uuid);
                                            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                                                for (String s : list) {
                                                    UUID friendUuid = UUID.fromString(s);
                                                    Player friend = Bukkit.getPlayer(friendUuid);

                                                    if (friend != null && !friendUuid.toString().equalsIgnoreCase(s)) {
                                                        friend.sendMessage(ChatColor.GRAY + name + " has joined " + ChatColor.AQUA + ChatColor.UNDERLINE + shard + ".");
                                                        friend.playSound(friend.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 63f);
                                                    }
                                                }
                                            });
                                        });*/

                                        PlayerWrapper personWrapper = PlayerWrapper.getPlayerWrapper(uuid);
                                        if (personWrapper != null) {
                                            for (UUID friend : personWrapper.getFriendsList().keySet()) {
                                                Player friendPlayer = Bukkit.getPlayer(friend);
                                                if (friendPlayer == null) continue;
                                                friendPlayer.sendMessage(ChatColor.GRAY + name + " has joined " + ChatColor.AQUA + ChatColor.UNDERLINE + shard + ".");
                                                friendPlayer.playSound(friendPlayer.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 63f);
                                            }
                                        }

                                    }
                                } else if (msg.contains("request:")) {
                                    String[] content = msg.split(",");
//                                    String senderUuid = content[1];
                                    UUID senderUUID = UUID.fromString(content[1]);
                                    String senderName = content[2];
                                    String friendUUID = content[3];
                                    int accountID = content.length == 5 && StringUtils.isNumeric(content[4]) ? Integer.parseInt(content[4]) : SQLDatabaseAPI.getInstance().getAccountIdFromUUID(senderUUID);
                                    UUID uuid = UUID.fromString(friendUUID);
                                    Player friend = Bukkit.getPlayer(uuid);
                                    if (friend != null) {
//                                        DatabaseAPI.getInstance().update(friend.getUniqueId(), EnumOperators.$PUSH, EnumData.FRIEND_REQUESTS, senderUuid, true);

                                        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(friend);
                                        if(wrapper != null) {
                                            wrapper.getPendingFriends().put(senderUUID, accountID);
                                            friend.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + senderName + ChatColor.GREEN + " sent you a friend request.");
                                            friend.sendMessage(ChatColor.GREEN + "Use /accept (player) to accept.");
                                        }

                                    }
                                } else if (msg.contains("accept:")) {
                                    String[] content = msg.split(",");
                                    String senderUuid = content[1];
                                    String senderName = content[2];
                                    String friendUUID = content[3];

                                    UUID senderUUID = UUID.fromString(senderUuid);
                                    UUID uuid = UUID.fromString(friendUUID);
                                    int friendID = content.length == 3 && StringUtils.isNumeric(content[4]) ? Integer.parseInt(content[4]) : SQLDatabaseAPI.getInstance().getAccountIdFromUUID(senderUUID);
                                    Player friend = Bukkit.getPlayer(uuid);
                                    if (friend != null) {
                                        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(uuid);
                                        wrapper.getFriendsList().put(senderUUID, friendID);
                                        wrapper.getPendingFriends().remove(senderUUID);
                                        friend.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + senderName + ChatColor.GREEN + " accepted your friend request.");
                                    }

                                } else if (msg.contains("removeFriend:")) {
                                    String[] content = msg.split(":");
                                    if (content.length == 4) {
                                        String playerToFindID = content[3];
                                        UUID uuid = UUID.fromString(playerToFindID);
                                        Player pl = Bukkit.getPlayer(uuid);
                                        if (pl != null) {
                                            //Remove friend..
                                            UUID whoRemoved = UUID.fromString(content[1]);
                                            String name = content[2];
                                            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(pl);
                                            if (wrapper != null && wrapper.getFriendsList().containsKey(whoRemoved)) {
                                                //Remove them..
                                                wrapper.getFriendsList().remove(whoRemoved);
                                                if (wrapper.getPendingFriends().remove(whoRemoved) == null)
                                                    pl.sendMessage(ChatColor.RED + name + " has removed you from their friends list.");
                                            }
                                        }
                                    }
                                }
                            }
                            return;
                            case "PrivateMessage": {
                                String fromPlayer = in.readUTF();
                                String playerName = in.readUTF();
                                String msg = Chat.getInstance().checkForBannedWords(in.readUTF());
                                Player player = Bukkit.getPlayer(playerName);
                                if (player != null) {
                                    GamePlayer gp = GameAPI.getGamePlayer(player);
                                    if (gp != null) {
                                        gp.setLastMessager(fromPlayer);
                                    }
                                    player.sendMessage(msg);
                                    GameAPI.runAsSpectators(player, (spectator) -> spectator.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "(AS " + player.getName() + ") " + msg));
                                }
                                break;
                            }
                            case "Shop": {
                                String msg = in.readUTF();
                                if (msg.contains("close:")) {
                                    String[] content = msg.split(",");
                                    String playerName = content[1];
                                    Shop shop = ShopMechanics.getShop(playerName);
                                    if (shop != null) {
                                        shop.deleteShop(false, null);
                                        BungeeUtils.sendPlayerMessage(playerName, ChatColor.YELLOW + "Shop found and removed.");
                                    }
                                }
                                break;
                            }
                            case "Broadcast":
                                String message = ChatColor.translateAlternateColorCodes('&', in.readUTF());
                                Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(message));
                                break;
                            case "BroadcastRaw":
                                String rawMessage = in.readUTF();
                                Bukkit.getOnlinePlayers().forEach(player -> ((CraftPlayer) player).getHandle().playerConnection.sendPacket(
                                        new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(rawMessage), (byte) 1)));
                                break;
                            case "BroadcastSound": {
                                String name = in.readUTF();
                                Float volume = 10f;
                                Float pitch = 1f;
                                if (in.available() > 0) {
                                    try {
                                        volume = Float.valueOf(in.readUTF());
                                    } catch (NumberFormatException e) {
                                        volume = 10f;
                                    }
                                }
                                if (in.available() > 0) {
                                    try {
                                        pitch = Float.valueOf(in.readUTF());
                                    } catch (NumberFormatException e) {
                                        pitch = 1f;
                                    }
                                }
                                Sound sound = null;
                                for (Sound s : Sound.values()) {
                                    if (Sound.valueOf(name).equals(s)) {
                                        sound = s;
                                    }
                                }
                                if (sound != null)
                                    for (Player p : Bukkit.getOnlinePlayers()) {
                                        p.playSound(p.getLocation(), sound, volume, pitch);
                                    }
                                break;
                            }
                            case "BroadcastSoundPlayer": {
                                String playerName = in.readUTF();
                                Player player = Bukkit.getPlayer(playerName);
                                if (player == null) return;
                                String name = in.readUTF();
                                Float volume = 10f;
                                Float pitch = 1f;
                                if (in.available() > 0) {
                                    try {
                                        volume = Float.valueOf(in.readUTF());
                                    } catch (NumberFormatException e) {
                                        volume = 10f;
                                    }
                                }
                                if (in.available() > 0) {
                                    try {
                                        pitch = Float.valueOf(in.readUTF());
                                    } catch (NumberFormatException e) {
                                        pitch = 1f;
                                    }
                                }
                                Sound sound = null;
                                for (Sound s : Sound.values()) {
                                    if (Sound.valueOf(name).equals(s)) {
                                        sound = s;
                                    }
                                }
                                if (sound != null)
                                    player.playSound(player.getLocation(), sound, volume, pitch);
                                break;
                            }
                            case "buff":
                                // No buffs are allowed on this shard.
                                if (DungeonRealms.getInstance().isEventShard)
                                    break;

                                String type = in.readUTF();
                                int duration = Integer.parseInt(in.readUTF());
                                int bonusAmount = Integer.parseInt(in.readUTF());
                                String player_string = in.readUTF();
                                String from_server = in.readUTF();
                                switch (type) {
                                    case "loot":
                                        DonationEffects.getInstance().activateNewLootBuffOnThisShard(duration, bonusAmount, player_string, from_server);
                                        break;
                                    case "profession":
                                        DonationEffects.getInstance().activateNewProfessionBuffOnThisShard(duration, bonusAmount, player_string, from_server);
                                        break;
                                    case "level":
                                        DonationEffects.getInstance().activateNewLevelBuffOnThisShard(duration, bonusAmount, player_string, from_server);
                                        break;
                                }
                                break;
                            case "Stop":
                                DungeonRealms.getInstance().isDrStopAll = true;

                                if (DungeonRealms.getInstance().canAcceptPlayers())
                                    GameAPI.stopGame();
                                return;
                            case "Guilds":
                                String command = in.readUTF();

                                if (command.contains("message:")) {
                                    String[] commandArray = command.split(":");
                                    String[] filter = Arrays.copyOfRange(commandArray, 1, commandArray.length);

                                    String guildName = in.readUTF();
                                    String msg = in.readUTF();

                                    if (GuildDatabaseAPI.get().isGuildCached(guildName))
                                        GuildMechanics.getInstance().sendMessageToGuild(guildName, msg, filter);
                                    return;
                                }

                                switch (command) {
                                    case "message": {
                                        String guildName = in.readUTF();
                                        String msg = in.readUTF();

                                        if (GuildDatabaseAPI.get().isGuildCached(guildName))
                                            GuildMechanics.getInstance().sendMessageToGuild(guildName, msg);
                                        break;
                                    }

                                    case "update": {
                                        String guildName = in.readUTF();

                                        if (GuildDatabaseAPI.get().isGuildCached(guildName))
                                            GuildDatabaseAPI.get().updateCache(guildName, true);
                                        break;
                                    }
                                }
                                break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}