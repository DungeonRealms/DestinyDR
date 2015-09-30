package net.dungeonrealms.guild;

import com.mongodb.client.model.Filters;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mongo.Database;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
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

    ArrayList<GuildBlob> GUILDS = new ArrayList<>();


    public void createGuild(String name, String clanTag, UUID owner) {

        Database.guilds.find(Filters.eq("info.name", name)).first((document, throwable) -> {
            Utils.log.info("[GUILD] [ASYNC] Checking for info.name " + name);
            if (document != null) {
                Utils.log.info("[GUILD] [ASYNC] Already exist!? -> " + name);
                Bukkit.getPlayer(owner).sendMessage(ChatColor.RED + "That guild already exist!");
                return;
            }
            Database.guilds.insertOne(
                    new Document("info",
                            new Document("name", name)
                                    .append("clanTag", clanTag)
                                    .append("owner", owner.toString())
                                    .append("officers", new ArrayList<String>())
                                    .append("members", new ArrayList<String>()))
                    , (aVoid, throwable1) -> {
                        Utils.log.info("[GUILD] Creating Guild (" + name + ") w/ tag (" + clanTag + ")");
                        Database.guilds.find(Filters.eq("info.name", name)).first((guild, error) -> {
                            if (guild == null) return;
                            Object info = document.get("info");
                            UUID ownerUUID = (UUID) ((Document) info).get("owner");
                            String guildName = ((Document) info).getString("name");
                            String guildClanTag = ((Document) info).getString("clanTag");
                            List<UUID> guildOfficers = (List<UUID>) ((Document) info).get("officers");
                            List<UUID> guildMembers = (List<UUID>) ((Document) info).get("members");
                            GUILDS.add(new GuildBlob(ownerUUID, guildName, guildClanTag, guildOfficers, guildMembers));
                            Utils.log.info("[GUILD] Cached Guild (" + name + ") w/ tag (" + clanTag + ") in volatile memory!");
                        });
                    });
        });

    }


    class GuildBlob {
        private UUID owner;
        private String name;
        private String clanTag;
        private List<UUID> officers;
        private List<UUID> members;

        public GuildBlob(UUID owner, String name, String clanTag, List<UUID> officers, List<UUID> members) {
            this.owner = owner;
            this.name = name;
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

        public String getClanTag() {
            return clanTag;
        }

        public List<UUID> getOfficers() {
            return officers;
        }

        public List<UUID> getMembers() {
            return members;
        }
    }


}
