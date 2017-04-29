package net.dungeonrealms.game.listener.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import lombok.SneakyThrows;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.player.PlayerToken;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.game.database.sql.UUIDName;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.UpdateType;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.guild.GuildMember;
import net.dungeonrealms.game.guild.GuildWrapper;
import net.dungeonrealms.game.guild.database.GuildDatabase;
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
    @SneakyThrows
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

            String task = in.readUTF();


            switch (task) {
                case "CreateAccount":
                    UUID newUUID = UUID.fromString(in.readUTF());
                    String username = in.readUTF();
                    Integer accountID = Integer.parseInt(in.readUTF());
                    SQLDatabaseAPI.getInstance().getAccountIdNames().put(accountID, new UUIDName(newUUID, username));
                    System.out.println("Registered user " + username + " with accountID: " + accountID);
                    break;
                case "WipePlayer":
                    int idToWipe = Integer.parseInt(in.readUTF());
                    if (idToWipe <= 0) return;
                    UUID uuidToWipe = UUID.fromString(in.readUTF());
//                    if(SQLDatabaseAPI.getInstance().getAccountIdNames().containsKey())
                    UUIDName removed = SQLDatabaseAPI.getInstance().getAccountIdNames().remove(idToWipe);
                    if (removed != null) {
                        Constants.log.info("Received WipePlayer packet for " + idToWipe + " (" + uuidToWipe + ")");
                    }
                    PlayerWrapper foundWrapper = PlayerWrapper.getPlayerWrapper(uuidToWipe);
                    if(foundWrapper != null){
                        PlayerWrapper.getPlayerWrappers().remove(uuidToWipe);
                    }
                    break;
                case "Update": {
                    if (DungeonRealms.getInstance().isAlmostRestarting()) return;
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                        try {
                            UUID uuid = UUID.fromString(in.readUTF());
                            Player player1 = Bukkit.getPlayer(uuid);

                            String updateField = in.readUTF();
                            if (player1 != null) {

                                PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player1);
                                String storedRank = wrapper.getRank();
                                if (updateField.equals("mute")) {
                                    long previousMute = wrapper.getMuteExpire();
                                    wrapper.loadPunishment(true, expire -> {
                                        if (expire != previousMute) {
                                            //Something updated??
                                            if (previousMute > 0 && expire <= 0) {
                                                //Unmuted??
                                                player1.sendMessage(ChatColor.RED + "You are no longer muted.");
                                            }
                                        }
                                    });
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

                                            rs.close();
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
                                                    if (type == UpdateType.GEMS) {
                                                        wrapper.setGems((int) obj);
                                                    } else {
                                                        field.set(wrapper, obj);
                                                    }
                                                    if (type == UpdateType.RANK) {
                                                        //Need to update cache..
                                                        Rank.PlayerRank foundRank = Rank.PlayerRank.getFromInternalName(wrapper.getRank());
                                                        Rank.getInstance().getCachedRanks().put(player1.getUniqueId(), foundRank);
                                                        if (!wrapper.getRank().equals(storedRank)) {
                                                            player1.sendMessage("                 " + ChatColor.YELLOW + "Your rank is now: " + foundRank.getPrefix());
                                                            player1.playSound(player1.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 63f);
                                                        }
                                                    }
                                                    Bukkit.getLogger().info("Updating " + type.getFieldName() + " to " + obj + " for " + wrapper.getUsername());
                                                }
                                                rs.close();
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
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                    return;
                }
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
                                    if (wrapper != null) {
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
                            return;
                        }


                        case "PrivateMessage": {
                            String fromPlayer = in.readUTF();
                            UUID uuid = UUID.fromString(in.readUTF());
                            String playerName = in.readUTF();
                            String msg = Chat.getInstance().checkForBannedWords(in.readUTF());
                            Player player = Bukkit.getPlayer(playerName);
                            if (player != null) {
                                PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                                GamePlayer gp = GameAPI.getGamePlayer(player);
                                if (gp != null) {
                                    gp.setLastMessager(fromPlayer);
                                }

                                if (wrapper != null && wrapper.getIgnoredFriends().containsKey(uuid)) {
                                    return;
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

                            handleGuildMessage(command, in);
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        }
    }

    public void handleGuildMessage(String command, DataInputStream stream) {
        if (command == null || command.isEmpty() || stream == null) return;
        try {

            String shard = stream.readUTF();
            if (DungeonRealms.getShard().getPseudoName().equals(shard))
                return; //We dont want to receive the messages on the same shard we sent them.

            if (command.equals("message")) {
                int guildID = Integer.parseInt(stream.readUTF());
                String msg = stream.readUTF();
                String rankStr = stream.readUTF();
                GuildMember.GuildRanks rank = GuildMember.GuildRanks.getRankFromName(rankStr);

                GuildWrapper wrapper = GuildDatabase.getAPI().getGuildWrapper(guildID);
                if (wrapper != null)
                    wrapper.sendGuildMessage(msg, true, rank);
            } else if (command.equals("setmotd")) {
                int guildID = Integer.parseInt(stream.readUTF());
                String whoSet = stream.readUTF();
                String newMOTD = stream.readUTF();

                GuildWrapper wrapper = GuildDatabase.getAPI().getGuildWrapper(guildID);
                if (wrapper != null) {
                    wrapper.setMotd(newMOTD);
                    wrapper.sendGuildMessage(ChatColor.GRAY + whoSet + " has updated the guild " + ChatColor.BOLD.toString() + ChatColor.DARK_AQUA + "MOTD" + ChatColor.GRAY + " to:", true);
                    wrapper.sendGuildMessage(GuildMechanics.getInstance().getFormatted(wrapper.getTag(), wrapper.getMotd()), true);
                }
            } else if (command.equals("invite")) {

                int guildID = Integer.parseInt(stream.readUTF());
                String guildName = stream.readUTF();
                String username = stream.readUTF();
                int accountID = Integer.parseInt(stream.readUTF());
                String inviter = stream.readUTF();

                GuildWrapper wrapper = GuildDatabase.getAPI().getGuildWrapper(guildID);
                if (wrapper != null) {
                    GuildMember member = new GuildMember(accountID, guildID);
                    member.setAccepted(false);
                    member.setRank(GuildMember.GuildRanks.MEMBER);
                    member.setWhenJoined(System.currentTimeMillis());
                    wrapper.getMembers().put(accountID, member);
                }
                Player online = Bukkit.getPlayer(username);

                String inviteMessage = ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + inviter + ChatColor.GRAY + " has invited you to join their guild, " + ChatColor.DARK_AQUA +
                        guildName + ChatColor.GRAY + ". To accept, type " + ChatColor.DARK_AQUA.toString() + "/gaccept" + ChatColor.GRAY + " to decline, type " + ChatColor.DARK_AQUA.toString() + "/gdecline";
                if (online != null) {
                    PlayerWrapper playerWrapper = PlayerWrapper.getPlayerWrapper(online);
                    playerWrapper.setGuildID(guildID);
                    if (wrapper == null) {
                        //Load guild since it doesnt exist...
                        GuildWrapper newWrapper = new GuildWrapper(guildID);
                        wrapper = newWrapper;
                        wrapper.loadData(true, loaded -> {
                            if (loaded == null || !loaded) {
                                Bukkit.getLogger().info("Unable to load offline guild for " + guildName + "(" + guildID + ")");
                                return;
                            }
                            GuildDatabase.getAPI().cached_guilds.put(guildID, newWrapper);
                            online.sendMessage("");
                            online.sendMessage(inviteMessage);
                            online.sendMessage("");
                        });
                    } else {
                        //Added and shit.. send message..
                        online.sendMessage("");
                        online.sendMessage(inviteMessage);
                        online.sendMessage("");
                    }
                }
            } else if (command.equals("accept")) {
                int guildID = Integer.parseInt(stream.readUTF());
                int accountIdAccepting = Integer.parseInt(stream.readUTF());
                GuildWrapper wrapper = GuildDatabase.getAPI().getGuildWrapper(guildID);
                if (wrapper == null) return;
                GuildMember pending = wrapper.getMembers().get(accountIdAccepting);
                if (pending == null) {
                    Constants.log.info("Could not find pending guild invitation for " + accountIdAccepting + " for the guild " + wrapper.getName());
                    return;
                }
                pending.setAccepted(true);
            } else if (command.equals("deny")) {
                int guildID = Integer.parseInt(stream.readUTF());
                int accountIdDenying = Integer.parseInt(stream.readUTF());
                GuildWrapper wrapper = GuildDatabase.getAPI().getGuildWrapper(guildID);
                if (wrapper == null) return;
                GuildMember pending = wrapper.getMembers().get(accountIdDenying);
                if (pending == null) {
                    Constants.log.info("Could not find pending guild invitation for " + accountIdDenying + " for the guild " + wrapper.getName());
                    return;
                }
                wrapper.getMembers().remove(accountIdDenying);
            } else if (command.equals("kick")) {
                int guildID = Integer.parseInt(stream.readUTF());
                int accountIdKicking = Integer.parseInt(stream.readUTF());
                GuildWrapper wrapper = GuildDatabase.getAPI().getGuildWrapper(guildID);
                if (wrapper == null) return;
                GuildMember kicking = wrapper.getMembers().get(accountIdKicking);
                if (kicking == null) {
                    Constants.log.info("Could not find guild member on kick for " + accountIdKicking + " for the guild " + wrapper.getName());
                    return;
                }
                PlayerWrapper kickingPlayerWrapper = PlayerWrapper.getPlayerWrapper(kicking.getUUID());
                if(kickingPlayerWrapper != null) {
                    kickingPlayerWrapper.setGuildID(0);
                }
                wrapper.getMembers().remove(accountIdKicking);
            } else if (command.equals("leave")) {
                int guildID = Integer.parseInt(stream.readUTF());
                int accountIdKicking = Integer.parseInt(stream.readUTF());
                GuildWrapper wrapper = GuildDatabase.getAPI().getGuildWrapper(guildID);
                if (wrapper == null) return;
                GuildMember leaving = wrapper.getMembers().get(accountIdKicking);
                if (leaving == null) {
                    Constants.log.info("Could not find guild member on leave for " + accountIdKicking + " for the guild " + wrapper.getName());
                    return;
                }
                wrapper.getMembers().remove(accountIdKicking);
            } else if (command.equals("demote")) {
                int guildID = Integer.parseInt(stream.readUTF());
                int accountIdDemoted = Integer.parseInt(stream.readUTF());
                GuildWrapper wrapper = GuildDatabase.getAPI().getGuildWrapper(guildID);
                if (wrapper == null) return;
                GuildMember member = wrapper.getMembers().get(accountIdDemoted);
                if (member == null) {
                    Constants.log.info("Could not find guild member on demote for " + accountIdDemoted + " for the guild " + wrapper.getName());
                    return;
                }
                member.setRank(GuildMember.GuildRanks.MEMBER);
            } else if (command.equals("setrank")) {
                int guildID = Integer.parseInt(stream.readUTF());
                int accountIdDemoted = Integer.parseInt(stream.readUTF());
                int newRankID = Integer.parseInt(stream.readUTF());
                GuildWrapper wrapper = GuildDatabase.getAPI().getGuildWrapper(guildID);
                if (wrapper == null) return;
                GuildMember member = wrapper.getMembers().get(accountIdDemoted);
                if (member == null) {
                    Constants.log.info("Could not guild member for " + accountIdDemoted + " for the guild " + wrapper.getName());
                    return;
                }
                GuildMember.GuildRanks ranks = GuildMember.GuildRanks.getFromIndex(newRankID);
                Bukkit.getLogger().info("Setting Guild Member rank to " + ranks.name() + " for " + newRankID);
                member.setRank(ranks);
            } else if (command.equals("disband")) {
                int guildID = Integer.parseInt(stream.readUTF());
                GuildWrapper wrapper = GuildDatabase.getAPI().getGuildWrapper(guildID);
                if (wrapper == null) return;
                wrapper.sendGuildMessage(ChatColor.RED + "Your guild has been disbanded!", true);
                for(GuildMember member : wrapper.getMembers().values()) {
                    if(member == null) continue;
                    PlayerWrapper playerWrapper = PlayerWrapper.getPlayerWrapper(member.getUUID());
                    if(playerWrapper != null) {
                        playerWrapper.setGuildID(0);
                    }
                }
                GuildDatabase.getAPI().cached_guilds.remove(wrapper.getGuildID());
            } else {
                throw new IllegalArgumentException("UNHANDLED GUILD NETWORK MESSAGE: " + command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}