package net.dungeonrealms.guild;

import com.mongodb.client.model.Filters;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mongo.*;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

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

    public void addMember(UUID uuid, String guildName) {
        if (!isMember(guildName, uuid)) {
            DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PUSH, "info.members", uuid.toString(), true);
        }
    }

    @SuppressWarnings("unchecked")
    public boolean isMember(String guildName, UUID uuid) {
        if (isInGuild(uuid)) {
            return ((ArrayList<String>) DatabaseAPI.getInstance().getData(EnumGuildData.MEMBERS, guildName)).contains(uuid);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean isOfficer(String guildName, UUID uuid) {
        if (isInGuild(uuid)) {
            return ((ArrayList<String>) DatabaseAPI.getInstance().getData(EnumGuildData.OFFICERS, guildName)).contains(uuid);
        }
        return false;
    }

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
                                    .append("members", new ArrayList<String>()))
                            .append("unixCreation", System.currentTimeMillis() / 1000l)
                    , (aVoid, throwable1) -> {
                        DatabaseAPI.getInstance().requestGuild(name);
                    });
        });

    }

}
