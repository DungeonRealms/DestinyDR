package net.dungeonrealms.game.guild;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.game.mastery.ItemSerialization;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
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
    private String name, displayName, tag;

    @Setter
    private String motd = "Default MOTD";

    @Getter
    private HashMap<Integer, GuildMember> members = new HashMap<>();

    @Getter
    @Setter
    private ItemStack banner = new ItemStack(Material.BANNER, 1, (byte) 15);

    public GuildWrapper(int guildID) {
        this.guildID = guildID;
    }

    public String getMotd() {
        if(this.motd == null) return "Default MOTD";
        return this.motd;
    }

    private void modifyRank(UUID uuid, GuildMember.GuildRanks toSet) {
        GuildMember member = members.get(SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid));
        if (member == null) return;
        member.setRank(toSet);
    }

    public GuildMember.GuildRanks getRank(UUID uuid) {
        GuildMember member = getMembers().get(SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid));
        if (member == null) return null;
        return member.getRank();
    }

    public boolean removePlayer(UUID player) {
        int accountID = SQLDatabaseAPI.getInstance().getAccountIdFromUUID(player);
        if (getMembers().remove(accountID) != null) return true;
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
                this.setBannerFromString(result.getString("banner"));

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

        @Cleanup PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement("INSERT INTO guilds(name, displayname, tag, motd, banner) VALUES (?, ?, ?, ?, ?)");
        statement.setString(1, this.getName());
        statement.setString(2, this.getDisplayName());
        statement.setString(3, this.getTag());
        statement.setString(4, this.getMotd());
        statement.setString(5, this.getBannerString());
        statement.executeUpdate();

        SQLDatabaseAPI.getInstance().executeQuery("SELECT guild_id FROM guilds WHERE `name` = '" + this.getName() + "' AND `displayname` = '" + this.getDisplayName() + "' AND `tag` = '" + getTag() + "' AND `motd` = '" + getMotd() + "' AND `banner` = '" + getBannerString() + "';", false, rs -> {
            try {
                if(rs.first()) {
                    int guildID = rs.getInt("guild_id");
                    this.setGuildID(guildID);
                    @Cleanup PreparedStatement statement2 = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement("INSERT INTO guild_members(account_id, guild_id, rank, joined, accepted) VALUES (?, ?, ?, ?, ?);");
                    GuildMember ownerMember = members.values().stream().findFirst().get();
                    statement2.setInt(1, ownerMember.getAccountID());
                    statement2.setInt(2, getGuildID());
                    statement2.setString(3, ownerMember.getRank().getName());
                    statement2.setLong(4, ownerMember.getWhenJoined());
                    statement2.setBoolean(5, ownerMember.isAccepted());
                    statement2.executeUpdate();

                    callback.accept(guildID);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });


        if (callback != null)
            callback.accept(-1);
    }

    public void sendGuildMessage(String message, boolean thisShard, GuildMember.GuildRanks rank) {
        for (GuildMember member : getMembers().values()) {
            if(!rank.isThisRankOrHigher(member.getRank())) continue;
            Player player = Bukkit.getPlayer(member.getUUID());
            if (player != null) {
                player.sendMessage(message);
            }
        }

        if(!thisShard){
            //Send network message..
            GameAPI.sendNetworkMessage("Guilds", "message", String.valueOf(this.getGuildID()),message, rank.getName());
        }
    }

    public void sendGuildMessage(String message, boolean thisShard) {
        this.sendGuildMessage(message, thisShard, GuildMember.GuildRanks.MEMBER);
    }

    public void sendGuildMessage(String message) {
        this.sendGuildMessage(message, false);
    }

    public int getNumberOfGuildMembersOnThisShard() {
        return (int) getMembers().keySet().stream().mapToInt(memberID -> memberID).mapToObj(memberID -> SQLDatabaseAPI.getInstance().getUUIDFromAccountID(memberID)).filter(Objects::nonNull).map(Bukkit::getPlayer).filter(Objects::nonNull).count();
    }

    public GuildMember getOwner() {
        return getMembers().values().stream().filter(Objects::nonNull).filter(member -> member.getRank().equals(GuildMember.GuildRanks.OWNER)).findFirst().orElse(null);

    }

    public String getNamesForInfo(GuildMember.GuildRanks rank) {
        StringBuilder toReturn = new StringBuilder("");
        for (GuildMember member : getMembers().values()) {
            if (member.getRank().equals(rank)) {
                boolean isOnline = Bukkit.getPlayer(SQLDatabaseAPI.getInstance().getUsernameFromAccountID(member.getAccountID())) != null;
                ChatColor color = isOnline ? ChatColor.GREEN : ChatColor.GRAY;
                toReturn.append(color.toString());
                toReturn.append(member.getPlayerName());
                toReturn.append(',');
            }
        }

        if (toReturn.toString().isEmpty()) return "None";
        return toReturn.toString().substring(0, toReturn.length() - 1);
    }

    private String getBannerString() {
        if(banner == null)banner = new ItemStack(Material.BANNER, 1, (byte) 15);
        return ItemSerialization.itemStackToBase64(banner);
    }

    private void setBannerFromString(String banner) {
        if(banner == null || banner.isEmpty()) {
            this.banner = new ItemStack(Material.BANNER, 1, (byte) 15);
            return;
        }

        try {
            this.banner = ItemSerialization.itemStackFromBase64(banner);
        } catch(Exception e) {
            e.printStackTrace();
            this.banner = new ItemStack(Material.BANNER, 1, (byte) 15);
        }
    }


}
