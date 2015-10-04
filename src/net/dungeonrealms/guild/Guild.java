package net.dungeonrealms.guild;

import com.mongodb.client.model.Filters;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mongo.Database;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.*;

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

    HashMap<String, GuildBlob> GUILDS = new HashMap<>();

    /**
     * Gets the guild of a player.
     *
     * @param uuid
     * @return
     * @since 1.0
     */
    public GuildBlob getGuild(UUID uuid) {
        for (Map.Entry<String, GuildBlob> entry : GUILDS.entrySet()) {
            if (entry.getValue().getName().equalsIgnoreCase(((String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid)))) {
                return entry.getValue();
            }
        }
        return null;
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
            Object info = guild.get("info");
            String guildName = ((Document) info).getString("name");
            String guildMotd = ((Document) info).getString("motd");
            String guildClanTag = ((Document) info).getString("clanTag");
            UUID ownerUUID = UUID.fromString((String) ((Document) info).get("owner"));
            List<String> guildOfficers = (List<String>) ((Document) info).get("officers");
            List<String> guildMembers = (List<String>) ((Document) info).get("members");
            GUILDS.put(guildName.toUpperCase(), new GuildBlob(ownerUUID, guildName.toUpperCase(), guildMotd, guildClanTag, guildOfficers, guildMembers));
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
            Utils.log.info("[GUILD] [ASYNC] Checking for info.name " + name);
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
                    , (aVoid, throwable1) -> {
                        Utils.log.info("[GUILD] Creating Guild (" + name + ") w/ tag (" + clanTag + ")");
                        DatabaseAPI.getInstance().update(owner, EnumOperators.$SET, "info.guild", name.toUpperCase(), true);
                        Database.guilds.find(Filters.eq("info.name", name.toUpperCase())).first((guild, error) -> {
                            if (guild == null) return;
                            Object info = document.get("info");
                            UUID ownerUUID = (UUID) ((Document) info).get("owner");
                            String guildName = ((Document) info).getString("name");
                            String guildMotd = ((Document) info).getString("motd");
                            String guildClanTag = ((Document) info).getString("clanTag");
                            List<String> guildOfficers = (ArrayList<String>) ((Document) info).get("officers");
                            List<String> guildMembers = (ArrayList<String>) ((Document) info).get("members");
                            GUILDS.put(guildName.toUpperCase(), new GuildBlob(ownerUUID, guildName, guildMotd, guildClanTag, guildOfficers, guildMembers));
                            Utils.log.info("[GUILD] Cached Guild (" + name + ") w/ tag (" + clanTag + ") in volatile memory!");
                        });
                    });
        });

    }

    public class GuildBlob {
        private UUID owner;
        private String name;
        private String motd;
        private String clanTag;
        private List<String> officers;
        private List<String> members;

        public GuildBlob(UUID owner, String name, String motd, String clanTag, List<String> officers, List<String> members) {
            this.owner = owner;
            this.name = name;
            this.motd = motd;
            this.clanTag = clanTag;
            this.officers = officers;
            this.members = members;
        }

        public UUID getOwner() {
            return owner;
        }

        public String getName() {
            return name;
        }

        public String getMotd() {
            return motd;
        }

        public String getClanTag() {
            return clanTag;
        }

        public List<String> getOfficers() {
            return officers;
        }

        public List<String> getMembers() {
            return members;
        }
    }


}
