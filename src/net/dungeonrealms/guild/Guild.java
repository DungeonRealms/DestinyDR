package net.dungeonrealms.guild;

import com.mongodb.client.model.Filters;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mongo.*;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Nick on 9/29/2015.
 */
public class Guild {

    static Guild instance = null;

    public static Guild getInstance() {
        if (instance == null) {
            instance = new Guild();
        }
        return instance;
    }

    /**
     * Handles GUild Logs.
     *
     * @param player
     * @since 1.0
     */
    public void handleLogin(Player player) {
        if (DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()).equals("")) return;
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId());
        DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PUSH, "logs.playerLogin", player.getName() + "," + (System.currentTimeMillis() / 1000l), true);
    }

    /**
     * Removes a member from a Guild.
     *
     * @param uuid
     * @param guildName
     * @since 1.0
     */
    public void removeMember(UUID uuid, String guildName) {
        if (isMember(guildName, uuid)) {
            DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, "info.members", uuid.toString(), true);
        }
    }

    /**
     * Remove an officer of a guild.
     *
     * @param uuid
     * @param guildName
     * @since 1.0
     */
    public void removeOfficer(UUID uuid, String guildName) {
        if (isOfficer(guildName, uuid)) {
            DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, "info.officers", uuid.toString(), true);
        }
    }

    /**
     * Adds a member to a Guild.
     *
     * @param uuid
     * @param guildName
     * @since 1.0
     */
    public void addMember(UUID uuid, String guildName) {
        if (!isMember(guildName, uuid)) {
            DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PUSH, "info.members", uuid.toString(), true);
        }
    }

    /**
     * Adds the player to the Officer position in the Guild.
     *
     * @param uuid
     * @param guildName
     */
    public void addOfficer(UUID uuid, String guildName) {
        if (!isMember(guildName, uuid) && !isOfficer(guildName, uuid)) {
            DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PUSH, "info.officers", uuid.toString(), true);
        }
    }

    /**
     * Checks if player is a member of said guild.
     *
     * @param guildName
     * @param uuid
     * @return
     * @since 1.0
     */
    @SuppressWarnings({"unchecked", "negrotasticness"})
    public boolean isMember(String guildName, UUID uuid) {
        if (isInGuild(uuid)) {
            return ((ArrayList<String>) DatabaseAPI.getInstance().getData(EnumGuildData.MEMBERS, guildName)).contains(uuid);
        }
        return false;
    }

    /**
     * Checks if player is an Officer of said Guild.
     *
     * @param guildName
     * @param uuid
     * @return
     * @since 1.0
     */
    @SuppressWarnings({"unchecked", "negrotasticness"})
    public boolean isOfficer(String guildName, UUID uuid) {
        if (isInGuild(uuid)) {
            return ((ArrayList<String>) DatabaseAPI.getInstance().getData(EnumGuildData.OFFICERS, guildName)).contains(uuid);
        }
        return false;
    }

    /**
     * Checks if player is in a guild.
     *
     * @param uuid
     * @return
     */
    public boolean isInGuild(UUID uuid) {
        return DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid) != null;
    }

    /**
     * Gets the guild of a player, it doesn't exist. MEH.
     *
     * @param uuid
     * @since 1.0
     */
    public void doGet(UUID uuid) {
        String rawGuildName = (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid);
        if (rawGuildName.equals("")) {
            return;
        }
        Database.guilds.find(Filters.eq("info.name", rawGuildName)).first((guild, error) -> {
            if (error == null) {
                Object info = guild.get("info");
                String guildName = ((Document) info).getString("name");
                DatabaseAPI.GUILDS.put(guildName, guild);
                Bukkit.getOnlinePlayers().stream().filter(p -> DatabaseAPI.getInstance().getData(EnumData.GUILD, p.getUniqueId()).equals(guildName)).forEach(p -> {
                    p.sendMessage(ChatColor.WHITE + "[" + ChatColor.BLUE.toString() + ChatColor.BOLD + "GUILD" + ChatColor.WHITE + "] " + ChatColor.GREEN + p.getName() + ChatColor.YELLOW + " has joined your server!");
                    p.playSound(p.getLocation(), Sound.NOTE_PLING, 1f, 63f);
                });
            } else {
                Utils.log.warning("[GUILD] [ASYNC] Unable to retrieve Guild=(" + rawGuildName + ")");
            }
        });
    }

    /**
     * Adds experience to a Guild also watches for the global guild level cap.
     *
     * @param guildName
     * @param experienceToAdd
     * @since 1.0
     */
    public void addGuildExperience(String guildName, double experienceToAdd) {
        int level = (int) DatabaseAPI.getInstance().getData(EnumGuildData.LEVEL, guildName);
        //Guild level CAP 50.
        if (level >= 50) return;
        double experience = (double) DatabaseAPI.getInstance().getData(EnumGuildData.LEVEL, guildName);
        if (((level * experience) + experienceToAdd) > (level * 1500)) {
            DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$INC, "info.netLevel", 1, true);
        } else {
            DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$INC, "info.experience", experienceToAdd, true);
        }
    }

    /**
     * Creates a guild also checks to make sure it doesn't
     * exist.
     *
     * @param name
     * @param clanTag
     * @param owner
     * @since 1.0
     */
    public void createGuild(String name, String clanTag, UUID owner) {
        Database.guilds.find(Filters.eq("info.name", name.toUpperCase())).first((document, throwable) -> {
            if (document != null) {
                Utils.log.info("[GUILD] [ASYNC] Already exist!? -> " + name);
                Bukkit.getPlayer(owner).sendMessage(ChatColor.RED + "That guild already exist!");
                return;
            }
            Database.guilds.insertOne(
                    new Document("info",
                            new Document("name", name.toUpperCase())
                                    .append("motd", "")
                                    .append("clanTag", clanTag)
                                    .append("owner", owner.toString())
                                    .append("officers", new ArrayList<String>())
                                    .append("members", new ArrayList<String>())
                                    .append("unixCreation", System.currentTimeMillis() / 1000l)
                                    .append("netLevel", 1)
                                    .append("experience", 0)
                    )
                            .append("logs",
                                    new Document("playerLogin", new ArrayList<String>())
                                            .append("playerInvites", new ArrayList<String>())
                                            .append("bankClicks", new ArrayList<String>())
                            )
                    , (aVoid, throwable1) -> {
                        DatabaseAPI.getInstance().requestGuild(name);
                        DatabaseAPI.getInstance().update(owner, EnumOperators.$SET, "info.guild", name, true);
                    });
        });

    }

}
