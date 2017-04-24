package net.dungeonrealms.game.guild;


import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.game.util.StringUtils;
import net.dungeonrealms.database.PlayerGameStats;
import net.dungeonrealms.database.PlayerToggles;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.banks.CurrencyTab;
import net.dungeonrealms.game.player.stats.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public GuildMember(int accountID, int guildID) {
        this.accountID = accountID;
        this.guildID = guildID;
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
        String original = "UPDATE guild_members SET rank = '%s', joined = '%s', accepted = '%s', guild_id = '%s' WHERE account_id = '%s';";
        return String.format(original, this.getRank().getName(), this.getWhenJoined(), this.isAccepted(), this.getGuildID(), this.getAccountID());
    }


    public enum GuildRanks {

        MEMBER("member"),
        OFFICER("officer"),
        OWNER("owner");

        private String name;

        GuildRanks(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public static GuildRanks getRankFromName(String name) {
            for(GuildRanks rank : GuildRanks.values()) {
                if(rank.getName().equalsIgnoreCase(name)) return rank;
            }

            return null;
        }


    }
}
