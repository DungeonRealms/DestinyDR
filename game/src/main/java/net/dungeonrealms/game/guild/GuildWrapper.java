package net.dungeonrealms.game.guild;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Rar349 on 4/23/2017.
 */
public class GuildWrapper {


    @Getter
    @Setter
    private int guildID;

    @Getter
    @Setter
    private String name, displayName, tag, motd, banner;

    @Getter
    private HashMap<Integer, GuildMember> members = new HashMap<>();

    public GuildWrapper(int guildID) {
        this.guildID = guildID;
    }

    private void modifyRank(UUID uuid, GuildMember.GuildRanks toSet) {
        GuildMember member = members.get(SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid));
        if(member == null) return;
        member.setRank(toSet);
    }

    public GuildMember.GuildRanks getRank(UUID uuid) {
        GuildMember member = getMembers().get(SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid));
        if(member == null) return null;
        return member.getRank();
    }

    public boolean removePlayer(UUID player) {
        int accountID = SQLDatabaseAPI.getInstance().getAccountIdFromUUID(player);
        if(getMembers().remove(accountID) != null) return true;
        return false;

    }

    public boolean isMember(UUID uuid) {
        return members.containsKey(SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid));
    }

    public boolean isOwner(UUID uuid) {
        GuildMember member = getMembers().get(SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid));
        return member != null && member.getRank().equals(GuildMember.GuildRanks.OWNER);
    }

    private List<GuildMember> getList(String guildName, GuildMember.GuildRanks data) {
        return getMembers().values().stream().filter(mem -> data == null || mem.getRank() == data).collect(Collectors.toList());

    }

    public void loadData(boolean async, Consumer<Boolean> callback) {
        if (async && Bukkit.isPrimaryThread()) {
            CompletableFuture.runAsync(() -> loadData(false, callback), SQLDatabaseAPI.SERVER_EXECUTOR_SERVICE);
            return;
        }

        try {

            final long start = System.currentTimeMillis();
            @Cleanup PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement(
                    "SELECT * FROM `guilds` WHERE `guild_id` = ?;");
            statement.setInt(1, guildID);

            ResultSet result = statement.getResultSet();
            if (result.first()) {

                this.setName(result.getString("name"));
                this.setDisplayName(result.getString("displayname"));
                this.setTag(result.getString("tag"));
                this.setMotd(result.getString("motd"));
                this.setBanner(result.getString("banner"));

            }

            @Cleanup PreparedStatement statement2 = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement(
                    "SELECT * FROM `guild_members` WHERE `guild_id` = ?;");
            statement2.setInt(1, guildID);

            ResultSet result2 = statement.getResultSet();
            while (result2.next()) {

                int accountID = result2.getInt("account_id");

                GuildMember newMember = new GuildMember(accountID, guildID);
                GuildMember.GuildRanks rank = GuildMember.GuildRanks.getRankFromName(result2.getString("rank"));
                newMember.setRank(rank == null ? GuildMember.GuildRanks.MEMBER : rank);
                newMember.setAccepted(result2.getBoolean("accepted"));
                newMember.setWhenJoined(result2.getLong("joined"));
                members.put(accountID, newMember);

                if (Constants.debug)
                    Bukkit.getLogger().info("Loaded " + this.getName() + "'s [" + getTag() + "] Guild data in " + (System.currentTimeMillis() - start) + "ms.");

            }

            if (Constants.debug)
                Bukkit.getLogger().info("Loaded " + this.getName() + "'s [" + getTag() + "] Guild data in " + (System.currentTimeMillis() - start) + "ms.");

            if (callback != null) {
                callback.accept(true);
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (callback != null)
            callback.accept(false);
    }

    public void saveData(boolean async, Consumer<Boolean> callback) {
        if (async && Bukkit.isPrimaryThread()) {
            CompletableFuture.runAsync(() -> saveData(false, callback), SQLDatabaseAPI.SERVER_EXECUTOR_SERVICE);
            return;
        }

        SQLDatabaseAPI.getInstance().executeQuery("UPDATE guilds SET name = '%s', displayname = '%s', tag = '%s', motd = '%s', banner = '%s';", (set) -> {
            callback.accept(true);
        });
    }

    @SneakyThrows
    public void insertIntoDatabase(boolean async, Consumer<Integer> callback) {
        if (async && Bukkit.isPrimaryThread()) {
            CompletableFuture.runAsync(() -> insertIntoDatabase(false, callback), SQLDatabaseAPI.SERVER_EXECUTOR_SERVICE);
            return;
        }

        @Cleanup PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement("INSERT IGNORE INTO guilds(name, displayname, tag, motd, banner) VALUES (?, ?, ?, ?, ?)");
        statement.setString(1, this.getName());
        statement.setString(2, this.getDisplayName());
        statement.setString(3, this.getTag());
        statement.setString(4, this.getMotd());
        statement.setString(5, this.getBanner());
        statement.executeUpdate();

        SQLDatabaseAPI.getInstance().executeQuery("SELECT guild_id FROM guild WHERE `name` = '" + this.getName() + "' AND `displayname` = '" + this.getDisplayName() + "' AND `tag` = '" + getTag() + "' AND `motd` = '" + getMotd() + "' AND `banner` = '" + banner + "';", false, rs -> {
            int guildID = rs.getInt("guild_id");
            this.setGuildID(guildID);
            try {

                @Cleanup PreparedStatement statement2 = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement("INSERT IGNORE INTO guild_members(account_id, guild_id, rank, joined, accepted) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE");
                GuildMember ownerMember = members.values().stream().findFirst().get();
                statement2.setInt(1, ownerMember.getAccountID());
                statement2.setInt(2, getGuildID());
                statement2.setString(3, ownerMember.getRank().getName());
                statement2.setLong(4, ownerMember.getWhenJoined());
                statement2.setBoolean(5, ownerMember.isAccepted());
                statement2.executeUpdate();

                callback.accept(guildID);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });


        if (callback != null)
            callback.accept(-1);
    }


}
