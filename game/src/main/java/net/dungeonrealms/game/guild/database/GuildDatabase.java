package net.dungeonrealms.game.guild.database;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.game.guild.GuildMember;
import net.dungeonrealms.game.guild.GuildWrapper;
import net.dungeonrealms.game.mastery.Utils;
import org.bukkit.Bukkit;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;


public class GuildDatabase {

    private static GuildDatabase instance = null;

    @Setter
    @Getter
    private static Logger logger = null;


    public volatile ConcurrentHashMap<Integer, GuildWrapper> cached_guilds = new ConcurrentHashMap<>();

    public static GuildDatabase getAPI() {
        if (instance == null) instance = new GuildDatabase();
        return instance;
    }

    public void updateCache(GuildWrapper wrapper, boolean async, Consumer<Boolean> callback) {
        if (wrapper == null) return;
        if (Bukkit.isPrimaryThread() && async) {
            if (async && Bukkit.isPrimaryThread()) {
                CompletableFuture.runAsync(() -> updateCache(wrapper, false, callback), SQLDatabaseAPI.SERVER_EXECUTOR_SERVICE);
                return;
            }
        }

        wrapper.loadData(async, callback);
    }

    public GuildWrapper getPlayersGuildWrapper(UUID playerID) {
        return this.getPlayersGuildWrapper(SQLDatabaseAPI.getInstance().getAccountIdFromUUID(playerID));
    }

    public GuildWrapper getPlayersGuildWrapper(int accountID) {
        return cached_guilds.values().stream().filter(Objects::nonNull).filter(wrapper -> wrapper.getMembers().containsKey(accountID)).findFirst().orElse(null);
    }

    public GuildWrapper getGuildWrapper(String guildName) {
        for (GuildWrapper wrapper : cached_guilds.values()) {
            if (wrapper == null) continue;
            if (wrapper.getName().equalsIgnoreCase(guildName)) return wrapper;
        }

        return null;
    }

    public GuildWrapper getGuildWrapper(int guildID) {
        return cached_guilds.get(guildID);
    }

    public void removeFromCache(String guildName) {
        GuildWrapper wrapper = getGuildWrapper(guildName);
        if (wrapper == null) return;
        cached_guilds.remove(wrapper);
    }

    public boolean isGuildCached(String guildName) {
        for (GuildWrapper wrapper : cached_guilds.values()) {
            if (wrapper == null) continue;
            if (wrapper.getName().equalsIgnoreCase(guildName)) return true;
        }

        return false;
    }


    public void createGuild(String guildName, String displayName, String tag, UUID owner, String banner, Consumer<Boolean> callback) {


        GuildWrapper wrapper = new GuildWrapper(-1);
        wrapper.setDisplayName(displayName);
        wrapper.setName(guildName);
        wrapper.setTag(tag);
        wrapper.setBanner(banner);

        GuildMember ownerMember = new GuildMember(SQLDatabaseAPI.getInstance().getAccountIdFromUUID(owner), -1);
        ownerMember.setWhenJoined(System.currentTimeMillis());
        ownerMember.setAccepted(true);
        ownerMember.setRank(GuildMember.GuildRanks.OWNER);
        wrapper.getMembers().put(ownerMember.getAccountID(), ownerMember);

        wrapper.insertIntoDatabase(true, newGuildID -> {
            ownerMember.setGuildID(newGuildID);
            cached_guilds.put(newGuildID, wrapper);
        });

        Utils.log.warning("New guild created: " + guildName);

        if (callback != null)
            callback.accept(true);
    }


    @SneakyThrows
    public void doesGuildNameExist(String guildName, Consumer<Boolean> action) {
        SQLDatabaseAPI.getInstance().executeQuery("SELECT `guild_id` FROM `guilds` WHERE UPPER(`name`) = UPPER('" + guildName + "');", (set) -> {
            if (set == null) action.accept(null);
            if (set.isFirst()) action.accept(true);
            else action.accept(false);
        });
    }


    @SneakyThrows
    public void doesTagExist(String tag, Consumer<Boolean> action) {
        SQLDatabaseAPI.getInstance().executeQuery("SELECT `guild_id` FROM `guilds` WHERE UPPER(`tag`) = UPPER('" + tag + "');", (set) -> {
            if (set == null) action.accept(null);
            if (set.isFirst()) action.accept(true);
            else action.accept(false);
        });
    }


}
