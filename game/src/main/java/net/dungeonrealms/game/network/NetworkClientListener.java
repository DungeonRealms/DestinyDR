package net.dungeonrealms.game.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.database.type.EnumData;
import net.dungeonrealms.game.database.type.EnumOperators;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.handlers.ScoreboardHandler;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.world.shops.Shop;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import net.dungeonrealms.network.bungeecord.BungeeUtils;
import net.dungeonrealms.network.packet.type.BasicMessagePacket;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/15/2016
 */
public class NetworkClientListener extends Listener implements GenericMechanic {

    static NetworkClientListener instance = null;

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
        return EnumPriority.BISHOPS;
    }


    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof BasicMessagePacket) {
            BasicMessagePacket packet = (BasicMessagePacket) object;

            byte[] data = packet.data;
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));

            try {
                String task = in.readUTF();

                if (task.equals("LoginRequestToken")) {
                    UUID uuid = UUID.fromString(in.readUTF());
                    String shard = in.readUTF();

                    if (!DungeonRealms.getShard().getPseudoName().equals(shard)) return;
                    GameAPI.submitAsyncCallback(() -> DatabaseAPI.getInstance().requestPlayer(uuid), after -> {
                        if (DungeonRealms.getInstance().hasFinishedSetup())
                            GameAPI.sendNetworkMessage("AcceptLoginToken", uuid.toString(), shard);
                        else
                            GameAPI.sendNetworkMessage("RefuseLoginToken", uuid.toString(), "This server is still setting up..");
                    });
                    return;
                }

                if (task.equals("Update")) {
                    UUID uuid = UUID.fromString(in.readUTF());
                    Player player1 = Bukkit.getPlayer(uuid);

                    if (player1 != null) {

                        GameAPI.submitAsyncCallback(() -> DatabaseAPI.getInstance().requestPlayer(uuid), consumer -> Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                            if (GameAPI.getGamePlayer(player1) != null)
                                ScoreboardHandler.getInstance().setPlayerHeadScoreboard(player1, GameAPI.getGamePlayer(player1).getPlayerAlignment().getAlignmentColor(), GameAPI.getGamePlayer(player1).getLevel());
                        }));
                    }
                    return;
                }

                // Handle packet sync //
                Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
                    try {
                        if (task.equals("Friends")) {
                            String msg = in.readUTF();
                            if (msg.contains("join:")) {
                                String[] content = msg.split(",");
                                String uuidString = content[1];
                                String name = content[2];
                                String shard = content[3];
                                UUID uuid = UUID.fromString(uuidString);

                                if (Bukkit.getPlayer(uuid) != null) {
                                    ArrayList<String> list = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.FRIENDS, uuid);

                                    for (String s : list) {
                                        UUID friendUuid = UUID.fromString(s);
                                        Player friend = Bukkit.getPlayer(friendUuid);

                                        if (friend != null && !friendUuid.toString().equalsIgnoreCase(s)) {
                                            friend.sendMessage(ChatColor.GRAY + name + " has joined " + ChatColor.AQUA + ChatColor.UNDERLINE + shard + ".");
                                            friend.playSound(friend.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 63f);
                                        }
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
                                    DatabaseAPI.getInstance().update(friend.getUniqueId(), EnumOperators.$PUSH, EnumData.FRIEND_REQUSTS, senderUuid, true, true);
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
                                    DatabaseAPI.getInstance().update(friend.getUniqueId(), EnumOperators.$PULL, EnumData.FRIEND_REQUSTS, senderUuid, true, true);
                                    DatabaseAPI.getInstance().update(friend.getUniqueId(), EnumOperators.$PUSH, EnumData.FRIENDS, senderUuid, true, true);
                                    friend.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + senderName + ChatColor.GREEN + " accepted your friend request.");
                                }

                            }

                            return;
                        } else if (task.equals("Shop")) {
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
                        } else if (task.equals("Broadcast")) {
                            String message = ChatColor.translateAlternateColorCodes('&', in.readUTF());
                            Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(message));
                        } else if (task.equals("Stop")) {

                            if (DungeonRealms.getInstance().hasFinishedSetup())
                                GameAPI.stopGame();

                            return;
                        } else if (task.equals("Guilds")) {
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
                                        GameAPI.submitAsyncCallback(() -> GuildDatabaseAPI.get().updateCache(guildName), null);
                                    break;
                                }
                            }
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