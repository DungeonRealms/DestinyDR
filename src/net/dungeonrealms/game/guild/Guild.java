package net.dungeonrealms.game.guild;

import net.dungeonrealms.core.Core;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Nick on 9/29/2015.
 */
@SuppressWarnings("unchecked")
public class Guild {

    static Guild instance = null;

    public static Guild getInstance() {
        if (instance == null) {
            instance = new Guild();
        }
        return instance;
    }

    public HashMap<String, JSONObject> guilds = new HashMap<>();

    public static HashMap<Player, ArrayList<String>> invitations = new HashMap<>();

    public boolean isGuildNull(UUID uuid) {
        return DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid).toString().isEmpty();
    }

    public void doLogin(Player player) {
        if (!isGuildNull(player.getUniqueId())) {
            String guildName = DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()).toString();
            if (!guilds.containsKey(guildName)) {
                doesGuildNameExist(guildName, exist -> {
                    if (!exist) {
                        player.sendMessage(ChatColor.RED + "Your guild no longer exist in the database, this could be a result in it being disbanded.");
                        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.GUILD, "", true);
                        return;
                    }
                    fetchOfflineGuild(guildName, jsonObject -> {
                        if (jsonObject != null) {
                            guilds.put(guildName, jsonObject);
                            player.sendMessage(getClanTagOf(guildName) + ChatColor.GRAY + getMotdOf(guildName));
                        }
                    });
                });
            } else {
                sendAlert(guildName, ChatColor.GREEN + player.getName() + " " + ChatColor.GRAY + "is now online!");
            }
        }
    }

    /**
     * @param guildName targeted guild.
     * @return Ally list.
     */
    public List<String> getEnemiesOf(String guildName) {
        return ((ArrayList) guilds.get(guildName).get("enemies"));
    }

    /**
     * @param guildName targeted guild.
     * @return Ally list.
     */
    public List<String> getAlliesOf(String guildName) {
        return ((ArrayList) guilds.get(guildName).get("allies"));
    }

    /**
     * @param guildName targeted guild.
     * @param message   sends all a message.
     */
    public void sendAlert(String guildName, String message) {
        getAllOnlineOf(guildName).stream().forEach(player -> {
            player.sendMessage(
                    ChatColor.GRAY + "<" + ChatColor.DARK_AQUA + getClanTagOf(guildName) + ChatColor.GRAY + ">" + ChatColor.RESET + " " + message);
        });
    }

    /**
     * @param guildName name of guild.
     * @return motd of guild.
     */
    public String getMotdOf(String guildName) {
        return guilds.get(guildName).get("motd").toString();
    }

    /**
     * @param guildName the name of guild.
     * @param action    Returns jsonObject
     */
    public void fetchOfflineGuild(String guildName, Consumer<JSONObject> action) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try (
                    PreparedStatement statement = Core.getInstance().connection.prepareStatement("SELECT data FROM `guilds` WHERE guildName='" + guildName + "';");
                    ResultSet resultSet = statement.executeQuery();
            ) {
                if (resultSet.next()) {
                    action.accept((JSONObject) new JSONParser().parse(resultSet.getString("data")));
                }

            } catch (SQLException | ParseException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * @param guildName targeted guild.
     * @return The clanTag
     */
    public String getClanTagOf(String guildName) {
        if (guilds.containsKey(guildName)) {
            return guilds.get(guildName).get("clanTag").toString();
        } else {
            return "NULL";
        }
    }

    /**
     * @param guildName name wanting the players.
     * @return The offline players of a guild.
     */
    public List<String> getAllOfflineOf(String guildName) {
        List<String> temp = new ArrayList<>();

        JSONObject jsonObject = guilds.get(guildName);

        if (Bukkit.getPlayer(UUID.fromString(jsonObject.get("owner").toString())) == null) {
            temp.add(UUIDFetcher.getName(UUID.fromString(jsonObject.get("owner").toString())));
        }

        ((ArrayList<String>) jsonObject.get("members")).stream().forEach(s1 -> {
            if (Bukkit.getPlayer(UUID.fromString(s1)) == null) {
                temp.add(UUIDFetcher.getName(UUID.fromString(s1)));
            }
        });

        ((ArrayList<String>) jsonObject.get("officers")).stream().forEach(s1 -> {
            if (Bukkit.getPlayer(UUID.fromString(s1)) == null) {
                temp.add(UUIDFetcher.getName(UUID.fromString(s1)));
            }
        });

        return temp;

    }

    /**
     * @param guildName name wanting the players.
     * @return The online players of a guild.
     */
    public List<Player> getAllOnlineOf(String guildName) {
        List<Player> temp = new ArrayList<>();

        JSONObject jsonObject = guilds.get(guildName);

        if (Bukkit.getPlayer(UUID.fromString(jsonObject.get("owner").toString())) != null) {
            temp.add(Bukkit.getPlayer(UUID.fromString(jsonObject.get("owner").toString())));
        }

        temp.addAll(((ArrayList<String>) jsonObject.get("officers")).stream().filter(officer -> Bukkit.getPlayer(UUID.fromString(officer)) != null).map(Bukkit::getPlayer).collect(Collectors.toList()));
        temp.addAll(((ArrayList<String>) jsonObject.get("members")).stream().filter(member -> Bukkit.getPlayer(UUID.fromString(member)) != null).map(Bukkit::getPlayer).collect(Collectors.toList()));

        return temp;

    }

    /**
     * @param guildName Guild Name.
     * @param clanTag   Clan Tag.
     * @param owner     owner UUID
     * @param action    boolean.
     */
    public void createGuild(String guildName, String clanTag, UUID owner, Consumer<Boolean> action) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try (
                    PreparedStatement statement = Core.getInstance().connection.prepareStatement("INSERT INTO guilds VALUES(" + "'" + guildName + "'" + ", " + "'" + clanTag + "'" + ", " + "'" + getRawGuildJson(guildName, clanTag, owner).toJSONString() + "'" + ");");
            ) {
                statement.executeUpdate();
                Utils.log.info("DR | Created new guildName: " + guildName + " | clanTag: " + clanTag + " | ownerUUID: " + owner.toString());
                DatabaseAPI.getInstance().update(owner, EnumOperators.$SET, EnumData.GUILD, guildName, true);
                action.accept(true);
            } catch (SQLException e) {
                e.printStackTrace();
                action.accept(false);
            }
        });
    }

    /**
     * @param clanTag Name of ClanTag.
     * @param action  ASync Callback.
     * @return boolean.
     */
    public boolean doesClanTagExist(String clanTag, Consumer<Boolean> action) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try (
                    PreparedStatement statement = Core.getInstance().connection.prepareStatement("SELECT clanTag FROM `guilds` WHERE clanTag='" + clanTag + "';");
                    ResultSet resultSet = statement.executeQuery();
            ) {
                action.accept(resultSet.next());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        return true;
    }

    /**
     * @param guildName Name of the Guild.
     * @param action    Async Callback.
     * @return boolean.
     */
    public boolean doesGuildNameExist(String guildName, Consumer<Boolean> action) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try (
                    PreparedStatement statement = Core.getInstance().connection.prepareStatement("SELECT guildName FROM `guilds` WHERE guildName='" + guildName + "';");
                    ResultSet resultSet = statement.executeQuery();
            ) {
                action.accept(resultSet.next());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        return true;
    }

    JSONObject getRawGuildJson(String guildName, String clanTag, UUID uuid) {
        JSONObject temp = new JSONObject();
        temp.put("name", guildName);
        temp.put("clanTag", clanTag);
        temp.put("owner", uuid.toString());
        temp.put("officers", new ArrayList<String>());
        temp.put("members", new ArrayList<String>());
        temp.put("level", 1);
        temp.put("experience", 0);
        temp.put("karma", 0);
        temp.put("achievements", new ArrayList<String>());
        temp.put("allies", new ArrayList<String>());
        temp.put("enemies", new ArrayList<String>());
        temp.put("sound", Sound.ARROW_HIT.toString());
        temp.put("origin", (System.currentTimeMillis() / 1000l));
        temp.put("motd", "This is my MOTD, change this.");
        return temp;
    }
}
