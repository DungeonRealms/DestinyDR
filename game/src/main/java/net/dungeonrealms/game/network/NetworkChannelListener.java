package net.dungeonrealms.game.network;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.database.type.EnumData;
import net.dungeonrealms.game.database.type.EnumOperators;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.handlers.ScoreboardHandler;
import net.dungeonrealms.game.mastery.AsyncUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.menu.item.GUIButton;
import net.dungeonrealms.game.player.menu.ShardSwitcher;
import net.dungeonrealms.game.punishment.PunishAPI;
import net.dungeonrealms.game.world.shops.Shop;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import net.dungeonrealms.network.ShardInfo;
import net.dungeonrealms.network.bungeecord.BungeeServerInfo;
import net.dungeonrealms.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.network.bungeecord.BungeeUtils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Nick on 10/12/2015.
 */
@SuppressWarnings("unchecked")
public class NetworkChannelListener implements PluginMessageListener, GenericMechanic {

    static NetworkChannelListener instance = null;

    public static NetworkChannelListener getInstance() {
        if (instance == null) {
            instance = new NetworkChannelListener();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    public void startInitialization() {
        Utils.log.info("[NetworkChannelListener] Registering Outbound/Inbound BungeeCord channels...");
        Bukkit.getMessenger().registerOutgoingPluginChannel(DungeonRealms.getInstance(), "BungeeCord");
        Bukkit.getMessenger().registerIncomingPluginChannel(DungeonRealms.getInstance(), "BungeeCord", this);

        Bukkit.getMessenger().registerOutgoingPluginChannel(DungeonRealms.getInstance(), "DungeonRealms");
        Bukkit.getMessenger().registerIncomingPluginChannel(DungeonRealms.getInstance(), "DungeonRealms", this);

        BungeeServerTracker.startTask(DungeonRealms.getInstance(), 1L);
        Utils.log.info("[NetworkChannelListener] Finished Registering Outbound/Inbound BungeeCord channels ... OKAY!");
    }

    @Override
    public void stopInvocation() {
        Utils.log.info("[NetworkChannelListener] Unregistering Outbound/Inbound BungeeCord channels...");

        Bukkit.getMessenger().unregisterIncomingPluginChannel(DungeonRealms.getInstance(), "BungeeCord");
        Bukkit.getMessenger().unregisterIncomingPluginChannel(DungeonRealms.getInstance(), "BungeeCord", this);

        Bukkit.getMessenger().unregisterIncomingPluginChannel(DungeonRealms.getInstance(), "DungeonRealms");
        Bukkit.getMessenger().unregisterIncomingPluginChannel(DungeonRealms.getInstance(), "DungeonRealms", this);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equalsIgnoreCase("BungeeCord") && !channel.equalsIgnoreCase("DungeonRealms")) return;

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
        try {
            String subChannel = in.readUTF();

            if (channel.equalsIgnoreCase("DungeonRealms")) {

                if (subChannel.equals("Update")) {
                    UUID uuid = UUID.fromString(in.readUTF());
                    Player player1 = Bukkit.getPlayer(uuid);
                    if (player1 != null) {
                        DatabaseAPI.getInstance().requestPlayer(uuid);
                        if (GameAPI.getGamePlayer(player1) != null) {
                            //Updates tab menu, prefixes etc.
                            ScoreboardHandler.getInstance().setPlayerHeadScoreboard(player1, GameAPI.getGamePlayer(player1).getPlayerAlignment().getAlignmentColor(), GameAPI.getGamePlayer(player1).getLevel());
                        }
                    }
                    return;
                }

                if (subChannel.equals("Friends")) {
                    String msg = in.readUTF();
                    if (msg.contains("join:")) {
                        String[] content = msg.split(",");
                        String uuid = content[1];
                        String name = content[2];
                        String shard = content[3];
                        ArrayList<String> list = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.FRIENDS, UUID.fromString(uuid));
                        for (String uuidString : list) {
                            UUID friendUuid = UUID.fromString(uuidString);
                            Player friend = Bukkit.getPlayer(friendUuid);

                            if (friend != null && !friendUuid.toString().equalsIgnoreCase(uuid)) {
                                friend.sendMessage(ChatColor.GRAY + name + " has joined " + ChatColor.AQUA + ChatColor.UNDERLINE + shard + ".");
                                friend.playSound(friend.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 63f);
                            }
                        }
                    } else if (msg.contains("request:")) {
                        String[] content = msg.split(",");
                        String senderUuid = content[1];
                        String senderName = content[2];
                        String friendUUID = content[3];
                        UUID uuid = UUID.fromString(friendUUID);
                        if (Bukkit.getPlayer(uuid) != null) {
                            Player friend = Bukkit.getPlayer(uuid);
                            DatabaseAPI.getInstance().update(friend.getUniqueId(), EnumOperators.$PUSH, EnumData.FRIEND_REQUSTS, senderUuid, true);
                            friend.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + senderName + ChatColor.GREEN + " sent you a friend request.");
                            friend.sendMessage(ChatColor.GREEN + "Use /accept (player) to accept.");

                        }
                    } else if (msg.contains("accept:")) {
                        String[] content = msg.split(",");
                        String senderUuid = content[1];
                        String senderName = content[2];
                        String friendUUID = content[3];
                        UUID uuid = UUID.fromString(friendUUID);
                        if (Bukkit.getPlayer(uuid) != null) {
                            Player friend = Bukkit.getPlayer(uuid);
                            DatabaseAPI.getInstance().update(friend.getUniqueId(), EnumOperators.$PULL, EnumData.FRIEND_REQUSTS, senderUuid, true);
                            DatabaseAPI.getInstance().update(friend.getUniqueId(), EnumOperators.$PUSH, EnumData.FRIENDS, senderUuid, true);
                            friend.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + senderName + ChatColor.GREEN + " accepted your friend request.");
                        }

                    }

                    return;
                } else if (subChannel.equals("Shop")) {
                    String msg = in.readUTF();
                    if (msg.contains("close:")) {
                        String[] content = msg.split(",");
                        String playerName = content[1];
                        Shop shop = ShopMechanics.getShop(playerName);
                        if (shop != null) {
                            shop.deleteShop(false);
                            BungeeUtils.sendPlayerMessage(playerName, ChatColor.YELLOW + "Shop found and removed.");
                        }
                    }
                }

                if (subChannel.equals("Stop")) {
                    GameAPI.stopGame();
                    return;
                }

                if (subChannel.equals("Ping")) {
                    final long currentTime = System.currentTimeMillis();
                    String hostname = in.readUTF();

                    // Make sure server has access to ping //
                    Futures.addCallback(MoreExecutors.listeningDecorator(AsyncUtils.pool).submit(() -> InetAddress.getByName(hostname).isReachable(2000)), new FutureCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean isPinged) {
                            long ping = System.currentTimeMillis() - currentTime;
                            BungeeUtils.sendNetworkMessage("DungeonRealms", "Pinged", hostname, DungeonRealms.getInstance().bungeeName, isPinged ? String.valueOf(ping) : String.valueOf(0));
                        }

                        @ParametersAreNonnullByDefault
                        public void onFailure(Throwable ignored) {
                        }
                    });

                    return;
                }

                if (subChannel.equals("Pinged")) {
                    String hostname = in.readUTF();

                    if (!ShardSwitcher.CACHED_PING_SHARD_BUTTONS.containsKey(hostname)) return;

                    String bungeeName = in.readUTF();
                    String ping = in.readUTF();

                    Map<String, GUIButton> map = ShardSwitcher.CACHED_PING_SHARD_BUTTONS.get(hostname);

                    if (!map.containsKey(bungeeName)) return;

                    String shardID = ShardInfo.getByPseudoName(bungeeName).getShardID();
                    map.get(bungeeName).setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + shardID + ChatColor.GRAY + " (" + ping + " ms)");
                }

                if (subChannel.equals("Guilds")) {
                    String command = in.readUTF();

                    if (command.contains("message:")) {
                        String[] commandArray = command.split(":");
                        String[] filter = Arrays.copyOfRange(commandArray, 1, commandArray.length);

                        String guildName = in.readUTF();
                        String msg = in.readUTF();

                        GuildMechanics.getInstance().sendMessageToGuild(guildName, msg, filter);
                        return;
                    }

                    switch (command) {
                        case "message": {
                            String guildName = in.readUTF();
                            String msg = in.readUTF();

                            GuildMechanics.getInstance().sendMessageToGuild(guildName, msg);
                            break;
                        }

                        case "update": {
                            String guildName = in.readUTF();

                            if (GuildDatabaseAPI.get().isGuildCached(guildName))
                                GuildDatabaseAPI.get().updateCache(guildName);
                            break;
                        }
                    }
                }


            } else {
                try {
                    if (subChannel.equals("IP")) {
                        String address = in.readUTF();

                        Document existingDoc = DatabaseAPI.getInstance().getDocumentFromAddress(address);

                        if (existingDoc != null) {
                            UUID uuid = UUID.fromString(((Document) existingDoc.get("info")).get("uuid", String.class));

                            if (PunishAPI.isBanned(uuid)) {
                                String bannedMessage = PunishAPI.getBannedMessage(uuid);
                                PunishAPI.kick(player.getName(), bannedMessage, doBefore -> {
                                });

                                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.BANNED_TIME, DatabaseAPI.getInstance().getValue(uuid, EnumData.BANNED_TIME), true);
                                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.BANNED_REASON, DatabaseAPI.getInstance().getValue(uuid, EnumData.BANNED_REASON), true);
                            }
                        }

                        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.IP_ADDRESS, address, true);
                        return;
                    }

                    if (subChannel.equals("PlayerCount")) {
                        String server = in.readUTF();

                        if (in.available() > 0) {
                            int online = in.readInt();

                            BungeeServerInfo serverInfo = BungeeServerTracker.getOrCreateServerInfo(server);
                            serverInfo.setOnlinePlayers(online);
                        }
                    }

                } catch (EOFException e) {
                    // Do nothing.
                } catch (IOException e) {
                    // This should never happen.
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
