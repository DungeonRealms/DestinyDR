package net.dungeonrealms.game.guild;

import net.dungeonrealms.core.Core;
import net.dungeonrealms.game.mastery.UUIDFetcher;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

    public static HashMap<UUID, ArrayList<String>> invitations = new HashMap<>();

    public boolean isGuildNull(UUID uuid) {
        return getGuildOf(uuid) == null;
    }

    public void doLogin(Player player) {
        if (!isGuildNull(player.getUniqueId())) {
            String guildName = getGuildOf(player.getUniqueId());
            if (!guilds.containsKey(guildName)) {
                doesGuildNameExist(guildName, exist -> {
                    if (!exist) {
                        player.sendMessage(ChatColor.RED + "Your guild no longer exist in the database, this could be a result in it being disbanded.");
                        setGuild(player.getUniqueId(), null);
                        return;
                    }
                    fetchOfflineGuild(guildName, jsonObject -> {
                        if (jsonObject != null) {
                            guilds.put(guildName, jsonObject);
                            if (!((ArrayList<String>) guilds.get(guildName).get("members")).contains(player.getUniqueId().toString()) && !((ArrayList<String>) guilds.get(guildName).get("officers")).contains(player.getUniqueId().toString()) && !guilds.get(guildName).get("owner").toString().equals(player.getUniqueId().toString())) {
                                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.GUILD, "", true);
                                player.sendMessage(ChatColor.RED + "Apparently you were removed from your guild.");
                                return;
                            }
                            player.sendMessage(getClanTagOf(guildName).toUpperCase() + " " + ChatColor.GRAY + getMotdOf(guildName));
                        }
                    });
                });
            } else {
                if (!((ArrayList<String>) guilds.get(guildName).get("members")).contains(player.getUniqueId().toString()) && !((ArrayList<String>) guilds.get(guildName).get("officers")).contains(player.getUniqueId().toString()) && !guilds.get(guildName).get("owner").toString().equals(player.getUniqueId().toString())) {
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.GUILD, "", true);
                    player.sendMessage(ChatColor.RED + "Apparently you were removed from your guild.");
                    return;
                }
                sendAlert(guildName, ChatColor.GREEN + player.getName() + " " + ChatColor.GRAY + "is now online!");
            }
        } else {
            System.out.println("\n PLAYER IS NOT IN GUILD\n ");
        }
    }

    /**
     * Gets the guild of a player.
     *
     * @param uuid
     * @return
     */
    public String getGuildOf(UUID uuid) {
        Future<?> guildName = Executors.newSingleThreadExecutor().submit(() -> {
            String rname = "";
            try (
                    PreparedStatement statement = Core.getInstance().connection.prepareStatement("SELECT guild FROM `players` WHERE uuid='" + uuid.toString() + "';");
                    ResultSet resultSet = statement.executeQuery();
            ) {
                if (resultSet.next()) {
                    rname = resultSet.getString("guild");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return rname;
        });
        try {
            return (String) guildName.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Demotes a player from [OFFICER] to [MEMBER]
     *
     * @param guildName
     * @param uuid
     */
    public void demotePlayer(String guildName, UUID uuid) {

        ((ArrayList<String>) guilds.get(guildName).get("officers")).remove(uuid.toString());
        ((ArrayList<String>) guilds.get(guildName).get("member")).add(uuid.toString());

        sendAlert(guildName, ChatColor.RED + Core.getInstance().getNameFromUUID(uuid) + " has been demoted to [MEMBER]!");

        Executors.newSingleThreadExecutor().submit(() -> {
            try (
                    PreparedStatement statement = Core.getInstance().connection.prepareStatement("UPDATE `players` SET guild=" + null + " WHERE uuid='" + uuid.toString() + "';");
            ) {
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

    }

    /**
     * Promotes a player, like [MEMBER] to [OFFICER]
     *
     * @param uuid
     */
    public void promotePlayer(String guildName, UUID uuid) {
        ((ArrayList<String>) guilds.get(guildName).get("members")).remove(uuid.toString());
        ((ArrayList<String>) guilds.get(guildName).get("officers")).add(uuid.toString());

        sendAlert(guildName, ChatColor.RED + Core.getInstance().getNameFromUUID(uuid) + " has been promoted to [OFFICER]!");

    }

    /**
     * @param executor
     * @param guildName
     * @param uuid
     */
    public void kickFrom(UUID executor, String guildName, UUID uuid) {

        if (isOwner(executor, guildName) && isOfficer(uuid, guildName)) {
            ((ArrayList<String>) guilds.get(guildName).get("officers")).remove(uuid.toString());
            sendAlert(guildName, ChatColor.RED + Core.getInstance().getNameFromUUID(executor) + " " + "has removed " + Core.getInstance().getNameFromUUID(uuid) + " from the guild!");
        }

        if (isOfficer(executor, guildName) || isOwner(executor, guildName) && isMember(uuid, guildName)) {
            ((ArrayList<String>) guilds.get(guildName).get("members")).remove(uuid.toString());
            sendAlert(guildName, ChatColor.RED + Core.getInstance().getNameFromUUID(executor) + " " + "has removed " + Core.getInstance().getNameFromUUID(uuid) + " from the guild!");
        }

        if (Bukkit.getPlayer(uuid) != null) {
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.GUILD, "", true);
            Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED + "You have been removed from the guild!");
        } else {
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.GUILD, "", false);
        }

    }

    /**
     * @param uuid
     * @param guildName
     * @return
     */
    public boolean isOwner(UUID uuid, String guildName) {
        return guilds.get(guildName).get("owner").toString().equals(uuid.toString());
    }

    /**
     * @param uuid
     * @param guildName
     * @return
     */
    public boolean isMember(UUID uuid, String guildName) {
        return ((ArrayList<String>) guilds.get(guildName).get("members")).contains(uuid.toString());
    }

    /**
     * @param uuid
     * @param guildName
     * @return
     */
    public boolean isOfficer(UUID uuid, String guildName) {
        return ((ArrayList<String>) guilds.get(guildName).get("officers")).contains(uuid.toString());
    }

    /**
     * Saves all guilds.
     */
    public void saveAllGuilds() {
        guilds.entrySet().stream().forEach(e -> {
            saveGuild(e.getKey());
        });
    }

    /**
     * @param guildName name of guild.
     * @param motd      the motd
     */
    public void setMotdOf(String guildName, String motd) {
        guilds.get(guildName).put("motd", motd);
        sendAlert(guildName, ChatColor.YELLOW + motd);
    }

    /**
     * @param player target player
     * @return boolean
     */
    public boolean isOwnerOfGuild(Player player) {
        if (!isGuildNull(player.getUniqueId())) {
            String guildName = DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()).toString();
            return guilds.get(guildName).get("owner").toString().equals(player.getUniqueId().toString());
        }
        return false;
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
     */
    public void saveGuild(String guildName) {
        try (
                PreparedStatement statement = Core.getInstance().connection.prepareStatement("UPDATE `guilds` SET data='" + guilds.get(guildName).toString() + "' WHERE guildName='" + guildName + "';");
        ) {
            statement.executeUpdate();

            Utils.log.info("DR | Saved Guild = " + guildName);

        } catch (SQLException e) {
            e.printStackTrace();
        }
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
     * Sets the players guild.
     *
     * @param uuid
     * @param guildName
     */
    public void setGuild(UUID uuid, String guildName) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try (
                    PreparedStatement statement = Core.getInstance().connection.prepareStatement("UPDATE `players` SET guild='" + guildName == null ? null : guildName + "' WHERE uuid='" + uuid.toString() + "';");
            ) {
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
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
        setGuild(owner, guildName);
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
        temp.put("banner", "");
        temp.put("color", "");
        return temp;
    }
}
