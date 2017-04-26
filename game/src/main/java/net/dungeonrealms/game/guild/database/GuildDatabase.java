package net.dungeonrealms.game.guild.database;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.game.guild.GuildMember;
import net.dungeonrealms.game.guild.GuildWrapper;
import net.dungeonrealms.game.mastery.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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

    public boolean areInSameGuild(Player player, Player player2) {
        GuildWrapper guild = getPlayersGuildWrapper(player.getUniqueId());
        if (guild != null && guild.isMember(player2.getUniqueId())) return true;
        return false;
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

    @SneakyThrows
    public void doesGuildNameExist(String guildName, Consumer<Integer> action) {
        SQLDatabaseAPI.getInstance().executeQuery("SELECT `guild_id` FROM `guilds` WHERE UPPER(`name`) = UPPER('" + guildName + "');", (set) -> {
            try {
                if (set == null) action.accept(null);
                if (set.isFirst()) {
                    int guildID = set.getInt("guild_id");
                    action.accept(guildID);
                }
                else action.accept(-1);
            } catch (Exception e) {
                e.printStackTrace();
                action.accept(null);
            }
        });
    }


    @SneakyThrows
    public void doesTagExist(String tag, Consumer<Integer> action) {
        SQLDatabaseAPI.getInstance().executeQuery("SELECT `guild_id` FROM `guilds` WHERE UPPER(`tag`) = UPPER('" + tag + "');", (set) -> {
            try {
                if (set == null) action.accept(null);
                if (set.isFirst()) {
                    int guildID = set.getInt("guild_id");
                    if(action != null)action.accept(guildID);
                }
                else action.accept(-1);
            } catch (Exception e) {
                e.printStackTrace();
                action.accept(null);
            }
        });
    }


}
