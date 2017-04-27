package net.dungeonrealms.game.guild;


import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Created by Rar349 on 4/23/2017.
 */
public class GuildMember {

    @Getter
    private final int accountID;

    @Getter
    @Setter
    private int guildID;

    @Getter
    @Setter
    private GuildRanks rank;

    @Getter
    @Setter
    private long whenJoined;

    @Getter
    @Setter
    private boolean accepted;

    private UUID cachedUUID;

    public GuildMember(int accountID, int guildID) {
        this.accountID = accountID;
        this.guildID = guildID;
    }

    public UUID getUUID() {
        if (this.cachedUUID == null) {
            this.cachedUUID = SQLDatabaseAPI.getInstance().getUUIDFromAccountID(this.accountID);
        }
        return this.cachedUUID;
    }

    public void saveData(boolean async, Consumer<Boolean> callback) {

        if (async && Bukkit.isPrimaryThread()) {
            CompletableFuture.runAsync(() -> saveData(false, callback), SQLDatabaseAPI.SERVER_EXECUTOR_SERVICE);
            return;
        }

        try {
            @Cleanup PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement(getUpdateQuery());

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        if (callback != null)
            callback.accept(true);
    }

    public String getUpdateQuery() {
        String original = "UPDATE guild_members SET rank = '%s', joined = '%s', accepted = '%s' WHERE account_id = '%s';";
        return String.format(original, this.getRank().getName(), this.getWhenJoined(), this.isAccepted() ? 1 : 0, this.getAccountID());
    }

    public String getPlayerName() {
        return SQLDatabaseAPI.getInstance().getUsernameFromAccountID(this.accountID);
    }


    public enum GuildRanks {

        MEMBER("member", 3),
        OFFICER("officer",2),
        OWNER("owner",1);

        private String name;
        private int order;

        GuildRanks(String name, int order) {

            this.name = name;
            this.order = order;
        }

        public String getName() {
            return this.name;
        }
        public int getOrder() { return this.order; }

        public static GuildRanks getRankFromName(String name) {
            for (GuildRanks rank : GuildRanks.values()) {
                if (rank.getName().equalsIgnoreCase(name)) return rank;
            }

            return null;
        }

        public boolean isThisRankOrHigher(GuildRanks other) {
            if(other == null) return false;
            if(this.getOrder() > other.getOrder()) return false;
            return true;
        }


    }
}
