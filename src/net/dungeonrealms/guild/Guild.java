package net.dungeonrealms.guild;

import com.mongodb.client.model.Filters;
import net.dungeonrealms.API;
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
     * Notes:
     * Max Guild Level: 50
     * People in Guild: 5 per Level.
     *
     * After Guild level 50 you start v1,v2,v3,v4,v5
     * 10 invites per v.
     * So when you're level 50 v5 max cap 300 people.
     *
     */

    /**
     * Handles Guild Logs.
     *
     * @param player
     * @since 1.0
     */
    public void doLogin(Player player) {
        if (DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()).equals("")) return;
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId());
        DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PUSH, "logs.playerLogin", player.getName() + "," + (System.currentTimeMillis() / 1000l), true);
    }

    public void removePlayer(Player player, String playerName) {
        if (player.getName().equalsIgnoreCase(playerName)) return;
        UUID uuid = API.getUUIDFromName(playerName);
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId());
        if (API.isOnline(uuid)) {
            if (Guild.getInstance().isInvited(guildName, uuid)) {
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$PULL, "notices.guildInvites", guildName, true);
                DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, "invitations", player.getUniqueId().toString(), true);
                player.sendMessage(ChatColor.GREEN + "You have successfully remove a pending invitation from " + playerName);
            } else {
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, "info.guild", "", true);
                if (Guild.getInstance().isMember(guildName, uuid)) {
                    DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, "info.members", player.getUniqueId().toString(), true);
                    player.sendMessage(ChatColor.GREEN + "You have successfully remove member " + playerName);
                } else if (Guild.getInstance().isOfficer(guildName, uuid)) {
                    DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, "info.officers", player.getUniqueId().toString(), true);
                    player.sendMessage(ChatColor.GREEN + "You have successfully remove officer " + playerName);
                }
            }
        } else {
            //TODO: Relook at this entire method.
            //If the player they're wanting to remove isn't online.. Ohh shit..
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$PULL, "notices.guildInvites", guildName, false);
            DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, "invitations", player.getUniqueId().toString(), true);
        }
    }

    /**
     * This will send the player an invitation.
     *
     * @param player
     * @param playerName
     * @return
     */
    public void invitePlayer(Player player, String playerName) {
        if (player.getName().equalsIgnoreCase(playerName)) return;
        UUID uuid = API.getUUIDFromName(playerName);
        if (API.isOnline(uuid)) {
            if (Guild.getInstance().isGuildNull(uuid)) {
                String guildName = (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId());
                if (Guild.getInstance().isInvited(guildName, player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "That player is already invited to your guild!");
                } else {
                    DatabaseAPI.getInstance().update(uuid, EnumOperators.$PUSH, "notices.guildInvites", guildName + "," + (System.currentTimeMillis() / 1000l), true);
                    DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PUSH, "invitations", player.getUniqueId().toString(), true);
                    player.sendMessage(ChatColor.GREEN + "Player has been invited to a guild!");
                }
            } else {
                player.sendMessage(ChatColor.RED + "That player is inside of a Guild!");
                player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1f, 63f);
            }
        } else {
            player.sendMessage(ChatColor.RED + "That player isn't on your server!");
            player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1f, 63f);
        }
    }

    /**
     * Checks to see if the player has any outstanding notices
     * to join the guild defined.
     *
     * @param guildName
     * @param uuid
     * @return
     * @since 1.0
     */
    public boolean isInvited(String guildName, UUID uuid) {
        ArrayList<String> activeInvitations = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumGuildData.INVITATIONS, guildName);
        if (activeInvitations.contains(uuid.toString())) {
            return true;
        }
        return false;
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
        if (!isGuildNull(uuid)) {
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
        if (!isGuildNull(uuid)) {
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
    public boolean isGuildNull(UUID uuid) {
        return String.valueOf(DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid)).equals("");
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
    //TODO: Redo this!
    public void addGuildExperience(String guildName, double experienceToAdd) {
        int level = (int) DatabaseAPI.getInstance().getData(EnumGuildData.LEVEL, guildName);
        //Guild level CAP 50.
        if (level >= 50) return;
        double experience = (double) DatabaseAPI.getInstance().getData(EnumGuildData.EXPERIENCE, guildName);
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
                                    .append("icon", "DIRT")
                                    .append("owner", owner.toString())
                                    .append("officers", new ArrayList<String>())
                                    .append("members", new ArrayList<String>())
                                    .append("unixCreation", System.currentTimeMillis() / 1000l)
                                    .append("netLevel", 1)
                                    .append("experience", 0)
                                    .append("invitations", new ArrayList<String>())
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
