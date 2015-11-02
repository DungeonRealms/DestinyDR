package net.dungeonrealms.guild;

import com.mongodb.client.model.Filters;
import net.dungeonrealms.API;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mongo.*;
import net.dungeonrealms.network.NetworkAPI;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        if (isGuildNull(player.getUniqueId())) return;
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId());
        DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PUSH, EnumGuildData.PLAYER_LOGINS, player.getName() + "," + (System.currentTimeMillis() / 1000l), true);
        NetworkAPI.getInstance().sendAllGuildMessage(guildName, ChatColor.AQUA + player.getName() + ChatColor.GREEN + " is now online!");
    }

    /**
     * @param player    The player wanting to remove the Guild.
     * @param guildName Name of the guild.
     * @since 1.0
     */
    public void disbandGuild(Player player, String guildName) {
        if (!isOwner(player.getUniqueId(), guildName)) {
            player.sendMessage(ChatColor.RED + "You cannot disband the Guild! You aren't the Owner!");
            NetworkAPI.getInstance().sendNetworkMessage("guild", "message", player.getName() + " tried to disband the guild but was denied because they aren't of the rank [OWNER]!");
            return;
        }

        String OWNER = (String) DatabaseAPI.getInstance().getData(EnumGuildData.OWNER, guildName);
        String CO_OWNER = (String) DatabaseAPI.getInstance().getData(EnumGuildData.CO_OWNER, guildName);
        List<String> OFFICERS = (List<String>) DatabaseAPI.getInstance().getData(EnumGuildData.OFFICERS, guildName);
        List<String> MEMBERS = (List<String>) DatabaseAPI.getInstance().getData(EnumGuildData.MEMBERS, guildName);

        List<String> ENTIRE_GUILD = new ArrayList<>();
        ENTIRE_GUILD.add(OWNER);
        ENTIRE_GUILD.add(CO_OWNER);
        ENTIRE_GUILD.addAll(OFFICERS);
        ENTIRE_GUILD.addAll(MEMBERS);

        NetworkAPI.getInstance().sendNetworkMessage("guild", "message", "Preparing to purge guild!");

        ENTIRE_GUILD.stream().filter(s -> !s.equals("")).forEach(s1 -> {
            DatabaseAPI.getInstance().update(UUID.fromString(s1), EnumOperators.$SET, EnumData.GUILD, "", true);
            NetworkAPI.getInstance().sendNetworkMessage("player", "update", API.getNameFromUUID(s1));
        });


        Database.guilds.deleteOne(Filters.eq(EnumData.GUILD.getKey(), guildName), (deleteResult, throwable) -> Utils.log.info("[GUILD] [ASYNC] PURGED Guild=" + guildName + " ACKNOWLEDGED=" + deleteResult.wasAcknowledged()));

    }

    /**
     * Demotes a player from a guild.
     *
     * @param player     Executing the demotion.
     * @param playerName being demoted.
     * @since 1.0
     */
    public void demotePlayer(Player player, String playerName) {
        if (player.getName().equalsIgnoreCase(playerName)) return;
        UUID uuid = API.getUUIDFromName(playerName);
        assert uuid != null;
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId());

        if (!isOwner(player.getUniqueId(), guildName)) {
            if (!isCoOwner(player.getUniqueId(), guildName)) {
                player.sendMessage(ChatColor.RED + "You cannot demote players, you must be at-least rank [CoOwner]");
                return;
            }
            player.sendMessage(ChatColor.RED + "You cannot demote players, you aren't rank [Owner]");
            return;
        }

        if (isOfficer(guildName, uuid)) {
            DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, EnumGuildData.OFFICERS, uuid.toString(), false);
            DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PUSH, EnumGuildData.MEMBERS, uuid.toString(), true);
            NetworkAPI.getInstance().sendAllGuildMessage(guildName, ChatColor.AQUA + API.getNameFromUUID(uuid.toString()) + " " + ChatColor.GREEN + "has been demoted to member!");
        } else if (isMember(guildName, uuid)) {
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$PULL, EnumData.GUILD, "", true);
            DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, EnumGuildData.MEMBERS, uuid.toString(), true);
            NetworkAPI.getInstance().sendAllGuildMessage(guildName, ChatColor.AQUA + API.getNameFromUUID(uuid.toString()) + " " + ChatColor.GREEN + "has been removed by demotion!");
            player.sendMessage(ChatColor.RED + "As future reference use the 'Remove Player' option in the Guild Management GUI to remove players!");
        } else if (isInvited(guildName, uuid)) {
            DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, EnumGuildData.INVITATIONS, uuid.toString(), true);
            NetworkAPI.getInstance().sendAllGuildMessage(guildName, ChatColor.AQUA + API.getNameFromUUID(uuid.toString()) + " " + ChatColor.GREEN + "'s invitation has been revoked!");
            player.sendMessage(ChatColor.RED + "As future reference use the 'Remove Player' option in the Guild Management GUI to remove players!");
        } else {
            player.sendMessage(ChatColor.RED + "Unable to find player (" + playerName + ")! In your guild..");
        }

    }

    /**
     * Promotes a player of a Guild!
     *
     * @param player
     * @param playerName
     * @since 1.0
     */
    public void promotePlayer(Player player, String playerName) {
        if (player.getName().equalsIgnoreCase(playerName)) return;
        UUID uuid = API.getUUIDFromName(playerName);
        assert uuid != null;
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId());

        if (!isOwner(player.getUniqueId(), guildName)) {
            player.sendMessage(ChatColor.RED + "You cannot promote players, you aren't an Owner");
            if (!isCoOwner(player.getUniqueId(), guildName)) {
                player.sendMessage(ChatColor.RED + "You cannot promote players, you aren't an CoOwner");
                return;
            }
            return;
        }

        if (isOfficer(guildName, uuid)) {
            if (DatabaseAPI.getInstance().getData(EnumGuildData.CO_OWNER, guildName).equals("")) {
                DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$SET, EnumGuildData.CO_OWNER, uuid.toString(), true);
                NetworkAPI.getInstance().sendAllGuildMessage(guildName, ChatColor.AQUA + API.getNameFromUUID(uuid.toString()) + " " + ChatColor.GREEN + "has been promoted to CoOwner!");
            } else {
                player.sendMessage(ChatColor.RED + "You already have someone as an CoOwner!");
            }
        } else if (isMember(guildName, uuid)) {
            DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, EnumGuildData.MEMBERS, uuid.toString(), false);
            DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PUSH, EnumGuildData.OFFICERS, uuid.toString(), true);
            NetworkAPI.getInstance().sendAllGuildMessage(guildName, ChatColor.AQUA + API.getNameFromUUID(uuid.toString()) + " " + ChatColor.GREEN + "has been promoted to an Officer!");
        } else if (isInvited(guildName, uuid)) {
            player.sendMessage(ChatColor.RED + "You cannot promote a player that hasn't accepted the Guild Invitation Yet!");
        } else {
            player.sendMessage(ChatColor.RED + "Unable to find player (" + playerName + ")! In your guild..");
        }

    }

    /**
     * Remove a player from a Guild.
     *
     * @param player     That's in the guild, owner, officer or member.
     * @param playerName That needs to be removed!
     * @since 1.0
     */
    public void removePlayer(Player player, String playerName) {
        if (player.getName().equalsIgnoreCase(playerName)) return;
        UUID uuid = API.getUUIDFromName(playerName);
        assert uuid != null;
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId());
        if (API.isOnline(uuid)) {
            if (Guild.getInstance().isInvited(guildName, uuid)) {
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$PULL, EnumData.GUILD_INVITES, guildName, true);
                DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, EnumGuildData.INVITATIONS, uuid.toString(), true);
                player.sendMessage(ChatColor.GREEN + "You have successfully remove a pending invitation from " + playerName);
            } else {
                //Something wrong here.
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.GUILD, "", true);
                if (Guild.getInstance().isMember(guildName, uuid)) {
                    DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, EnumGuildData.MEMBERS, uuid.toString(), true);
                    player.sendMessage(ChatColor.GREEN + "You have successfully remove member " + playerName);
                    NetworkAPI.getInstance().sendAllGuildMessage(guildName, player.getName() + " has removed Member " + playerName + "!");
                } else if (Guild.getInstance().isOfficer(guildName, uuid)) {
                    DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, EnumGuildData.OFFICERS, uuid.toString(), true);
                    player.sendMessage(ChatColor.GREEN + "You have successfully remove officer " + playerName);
                    NetworkAPI.getInstance().sendAllGuildMessage(guildName, player.getName() + " has removed Officer " + playerName + "!");
                }
            }
        } else {
            Database.collection.find(Filters.eq("info.uuid", uuid.toString())).first((document, throwable) -> {
                if (document == null) {
                    player.sendMessage(ChatColor.RED + "Unable to find " + ChatColor.GREEN + playerName + ChatColor.RED + " in guild! If this is an error please report this to an administrator immediately!");
                    return;
                }
                Object info = document.get("notices");
                ArrayList<String> invitations = (ArrayList<String>) ((Document) info).get("guildInvites");

                for (String s : invitations) {
                    if (s.startsWith(guildName)) {
                        invitations.remove(s);
                        break;
                    }
                }
                DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, EnumGuildData.INVITATIONS, uuid.toString(), true);
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.GUILD_INVITES, invitations, false);
                player.sendMessage(ChatColor.GREEN + "You have successfully removed " + playerName);
            });

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
                    DatabaseAPI.getInstance().update(uuid, EnumOperators.$PUSH, EnumData.GUILD_INVITES, guildName + "," + System.currentTimeMillis(), true);
                    DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PUSH, EnumGuildData.INVITATIONS, uuid.toString(), true);
                    player.sendMessage(ChatColor.GREEN + "Player has been invited to a guild!");
                    Bukkit.getPlayer(uuid).sendMessage(ChatColor.GREEN + "You have been invited to " + ChatColor.AQUA + guildName + ChatColor.GREEN + " type /accept guild " + guildName + " to join!");
                    NetworkAPI.getInstance().sendAllGuildMessage(guildName, player.getName() + " has invited " + playerName + " to your guild!");
                }
            } else {
                player.sendMessage(ChatColor.RED + "That player is already ranked " + ChatColor.AQUA + getGuildName(player));
                player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1f, 63f);
            }
        } else {
            player.sendMessage(ChatColor.RED + "You cannot invite a player that isn't on your shard!");
            player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1f, 63f);
        }
    }

    /**
     * Gets the players GuildName.
     *
     * @param player
     * @return
     * @since 1.0
     */
    public String getGuildName(Player player) {
        if (!isGuildNull(player.getUniqueId())) {
            return (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId());
        }
        return "NULL";
    }

    /**
     * Sets the guild icon (Material)
     *
     * @param guildName
     * @param material
     * @since 1.0
     */
    public void setGuildIcon(String guildName, Material material) {
        DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$SET, EnumGuildData.ICON, material.toString(), true);
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
        return activeInvitations.contains(uuid.toString());
    }

    /**
     * Is OWNER?
     *
     * @param uuid
     * @param guildName
     * @return
     * @since 1.0
     */
    public boolean isOwner(UUID uuid, String guildName) {
        return uuid.equals(UUID.fromString(String.valueOf(DatabaseAPI.getInstance().getData(EnumGuildData.OWNER, guildName))));
    }

    /**
     * Is CO OWNER?
     *
     * @param uuid
     * @param guildName
     * @return
     * @since 1.0
     */
    public boolean isCoOwner(UUID uuid, String guildName) {
        return !DatabaseAPI.getInstance().getData(EnumGuildData.CO_OWNER, guildName).equals("") && uuid.equals(UUID.fromString(String.valueOf(DatabaseAPI.getInstance().getData(EnumGuildData.CO_OWNER, guildName))));
    }

    /**
     * Sets the guild CoOwner.
     *
     * @param uuid
     * @param guildName
     * @since 1.0
     */
    public void setCoOwner(UUID uuid, String guildName) {
        DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$SET, EnumGuildData.CO_OWNER, uuid.toString(), true);
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
            DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, EnumGuildData.MEMBERS, uuid.toString(), true);
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
            DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, EnumGuildData.OFFICERS, uuid.toString(), true);
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
            DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PUSH, EnumGuildData.MEMBERS, uuid.toString(), true);
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
            DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PUSH, EnumGuildData.OFFICERS, uuid.toString(), true);
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
        return ((ArrayList<String>) DatabaseAPI.getInstance().getData(EnumGuildData.MEMBERS, guildName)).contains(uuid.toString());
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
        return ((ArrayList<String>) DatabaseAPI.getInstance().getData(EnumGuildData.OFFICERS, guildName)).contains(uuid.toString());
    }

    /**
     * Checks if player is in a guild.
     *
     * @param uuid
     * @return
     */
    //TODO: Work for offline players!
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
                DatabaseAPI.getInstance().GUILDS.put(guildName, guild);
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
        double experience = (double) DatabaseAPI.getInstance().getData(EnumGuildData.EXPERIENCE, guildName);
        if (level <= 50) {
            if ((level * (experience + experienceToAdd)) > (level * 1500 + (factorial(Math.round(level % 8))))) {
                DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$INC, EnumGuildData.LEVEL, 1, false);
                DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$SET, EnumGuildData.EXPERIENCE, 0, true);
                NetworkAPI.getInstance().sendAllGuildMessage(guildName, "Has leveled to " + level);
            } else {
                DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$INC, EnumGuildData.EXPERIENCE, experienceToAdd, true);
            }
        } else {
            /*
            Here starts Guilds that are at level 50. The V's.
            51 = v1, 52 = v2, etc..
             */
            switch (level) {
                case 51:
                    if ((level * (experience + experienceToAdd)) > (level * 3500 + (factorial(Math.round(level % 6))))) {
                        DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$INC, EnumGuildData.LEVEL, 1, false);
                        DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$SET, EnumGuildData.EXPERIENCE, 0, true);
                        NetworkAPI.getInstance().sendAllGuildMessage(guildName, "Has leveled to V1");
                    } else {
                        DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$INC, EnumGuildData.EXPERIENCE, experienceToAdd, true);
                    }
                    break;
                case 52:
                    if ((level * (experience + experienceToAdd)) > (level * 4500 + (factorial(Math.round(level % 6))))) {
                        DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$INC, EnumGuildData.LEVEL, 1, false);
                        DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$SET, EnumGuildData.EXPERIENCE, 0, true);
                        NetworkAPI.getInstance().sendAllGuildMessage(guildName, "Has leveled to V2");
                    } else {
                        DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$INC, EnumGuildData.EXPERIENCE, experienceToAdd, true);
                    }
                    break;
                case 53:
                    if ((level * (experience + experienceToAdd)) > (level * 5500 + (factorial(Math.round(level % 5))))) {
                        DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$INC, EnumGuildData.LEVEL, 1, false);
                        DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$SET, EnumGuildData.EXPERIENCE, 0, true);
                        NetworkAPI.getInstance().sendAllGuildMessage(guildName, "Has leveled to V3");
                    } else {
                        DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$INC, EnumGuildData.EXPERIENCE, experienceToAdd, true);
                    }
                    break;
                case 54:
                    if ((level * (experience + experienceToAdd)) > (level * 6500 + (factorial(Math.round(level % 4))))) {
                        DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$INC, EnumGuildData.LEVEL, 1, false);
                        DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$SET, EnumGuildData.EXPERIENCE, 0, true);
                        NetworkAPI.getInstance().sendAllGuildMessage(guildName, "Has leveled to V4");
                    } else {
                        DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$INC, EnumGuildData.EXPERIENCE, experienceToAdd, true);
                    }
                    break;
                case 55:
                    if ((level * (experience + experienceToAdd)) > (level * 7500 + (factorial(Math.round(level % 3))))) {
                        DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$INC, EnumGuildData.LEVEL, 1, false);
                        DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$SET, EnumGuildData.EXPERIENCE, 0, true);
                        NetworkAPI.getInstance().sendAllGuildMessage(guildName, "Has leveled to V5");
                    } else {
                        DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$INC, EnumGuildData.EXPERIENCE, experienceToAdd, true);
                    }
                    break;
            }
        }
    }

    /**
     * Get the factorial of a number.
     *
     * @param n
     * @return
     * @since 1.0
     */
    public int factorial(int n) {
        int output;
        if (n == 1) {
            return 1;
        }
        output = factorial(n - 1) * n;
        return output;
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
                                    .append("coOwner", "")
                                    .append("officers", new ArrayList<String>())
                                    .append("members", new ArrayList<String>())
                                    .append("unixCreation", System.currentTimeMillis() / 1000l)
                                    .append("netLevel", 1)
                                    .append("experience", 0)
                                    .append("invitations", new ArrayList<String>()))
                            .append("logs",
                                    new Document("playerLogin", new ArrayList<String>())
                                            .append("playerInvites", new ArrayList<String>())
                                            .append("bankClicks", new ArrayList<String>()))
                    , (aVoid, throwable1) -> {
                        DatabaseAPI.getInstance().requestGuild(name);
                        DatabaseAPI.getInstance().update(owner, EnumOperators.$SET, EnumData.GUILD, name.toUpperCase(), true);
                    });
        });

    }

}
