package net.dungeonrealms.game.guild;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import net.dungeonrealms.game.mastery.ItemSerialization;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Created by Rar349 on 4/23/2017.
 */
public class GuildWrapper {

    @Getter @Setter
    private int guildID;

    @Getter @Setter
    private String name, tag;

    @Setter
    private String displayName;

    private String motd = "Default MOTD";

    @Getter
    private volatile Map<Integer, GuildMember> members = new ConcurrentHashMap<>();

    @Getter @Setter
    private ItemStack banner = new ItemStack(Material.BANNER, 1, (byte) 15);

    public GuildWrapper(int guildID) {
        this.guildID = guildID;
    }

    public String getMotd() {
        if (this.motd == null || this.motd.isEmpty()) return "Default MOTD";
        return this.motd;
    }

    public void setMotd(String newMotd) {
        this.motd = newMotd.replace("'","");
    }


    public Map<Integer, GuildMember> getMembersNoPending() {
        Map<Integer, GuildMember> toReturn = new HashMap<>();
        this.members.forEach((id, member) -> {
            if (member.isAccepted())
            	toReturn.put(id, member);
        });

        return toReturn;
    }

    public GuildMember.GuildRanks getRank(UUID uuid) {
        GuildMember member = getMembers().get(SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid));
        if (member == null) return null;
        return member.getRank();
    }

    public boolean removePlayer(UUID player) {
        int accountID = SQLDatabaseAPI.getInstance().getAccountIdFromUUID(player);
        return removePlayer(accountID);
    }

    public boolean removePlayer(int accountID) {
        if (getMembers().remove(accountID) != null) {
            if (getNumberOfGuildMembersOnThisShard() <= 0) {
                saveData(true, (bool) -> {
                    if (getNumberOfGuildMembersOnThisShard() > 0) return;
                    System.out.println("Removing from guild: " + accountID);
                    GuildDatabase.getAPI().cached_guilds.remove(getGuildID());
                });
            }
            return true;
        }
        return false;
    }

    public boolean isMember(UUID uuid) {
        return members.containsKey(SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid));
    }

    public String getDisplayName() {
        return displayName == null || displayName.isEmpty() ? name : displayName;
    }

    public GuildMember getMember(UUID uuid) {
        return members.values().stream().filter(mem -> uuid.equals(mem.getUUID())).findFirst().orElse(null);
    }

    public boolean isOwner(UUID uuid) {
        GuildMember member = getMembers().get(SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid));
        return member != null && member.getRank().equals(GuildMember.GuildRanks.OWNER);
    }

    public void loadData(boolean async, Consumer<Boolean> callback) {
        if (async && Bukkit.isPrimaryThread()) {
            CompletableFuture.runAsync(() -> loadData(false, callback), SQLDatabaseAPI.SERVER_EXECUTOR_SERVICE);
            return;
        }

        try {

            final long start = System.currentTimeMillis();
            @Cleanup PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement(
                    "SELECT name, displayname, tag, motd, banner FROM `guilds` WHERE `guild_id` = ?;");
            statement.setInt(1, guildID);

            ResultSet result = statement.executeQuery();
            if (result.first()) {

                this.setName(result.getString("name"));
                this.setDisplayName(result.getString("displayname"));
                this.setTag(result.getString("tag"));
                this.setMotd(result.getString("motd"));
                this.setBannerFromString(result.getString("banner"));

            }

            @Cleanup PreparedStatement statement2 = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement(
                    "SELECT account_id, rank, accepted, joined FROM `guild_members` WHERE `guild_id` = ?;");
            statement2.setInt(1, guildID);

            ResultSet result2 = statement2.executeQuery();
            while (result2.next()) {

                int accountID = result2.getInt("account_id");

                GuildMember newMember = new GuildMember(accountID, guildID);
                GuildMember.GuildRanks rank = GuildMember.GuildRanks.getRankFromName(result2.getString("rank"));
                newMember.setRank(rank == null ? GuildMember.GuildRanks.MEMBER : rank);
                newMember.setAccepted(result2.getBoolean("accepted"));
                newMember.setWhenJoined(result2.getLong("joined"));
                members.put(accountID, newMember);
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

        SQLDatabaseAPI.getInstance().executeUpdate((rows) -> {
            callback.accept(true);
        }, "UPDATE guilds SET name = '" + getName() + "', displayname = '" + getDisplayName() + "', tag = '" + getTag() + "', motd = '" + getMotd() + "', banner = '" + getBannerString() + "' WHERE `guild_id` = '" + getGuildID() + "';");
    }

    @SneakyThrows
    public void insertIntoDatabase(boolean async, Consumer<Integer> callback) {
        if (async && Bukkit.isPrimaryThread()) {
            CompletableFuture.runAsync(() -> insertIntoDatabase(false, callback), SQLDatabaseAPI.SERVER_EXECUTOR_SERVICE);
            return;
        }

        PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement("INSERT IGNORE INTO guilds(name, displayname, tag, motd, banner) VALUES (?, ?, ?, ?, ?)");
        statement.setString(1, this.getName());
        statement.setString(2, this.getDisplayName());
        statement.setString(3, this.getTag());
        statement.setString(4, this.getMotd());
        statement.setString(5, this.getBannerString());
        statement.executeUpdate();
        statement.close();

        SQLDatabaseAPI.getInstance().executeQuery("SELECT guild_id FROM guilds WHERE `name` = '" + this.getName() + "' AND `displayname` = '" + this.getDisplayName() + "' AND `tag` = '" + getTag() + "' AND `motd` = '" + getMotd() + "' AND `banner` = '" + getBannerString() + "';", false, rs -> {
            try {
                if (rs.first()) {
                    int guildID = rs.getInt("guild_id");
                    setGuildID(guildID);
                    PreparedStatement statement2 = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement("INSERT IGNORE INTO guild_members(account_id, guild_id, rank, joined, accepted) VALUES (?, ?, ?, ?, ?);");
                    GuildMember ownerMember = members.values().stream().findFirst().get();
                    statement2.setInt(1, ownerMember.getAccountID());
                    statement2.setInt(2, guildID);
                    statement2.setString(3, ownerMember.getRank().getName());
                    statement2.setLong(4, ownerMember.getWhenJoined());
                    statement2.setBoolean(5, ownerMember.isAccepted());
                    statement2.executeUpdate();

                    statement2.close();
                    callback.accept(guildID);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (callback != null)
                    callback.accept(-1);
            }
        });


    }

    public void sendGuildMessage(String message, boolean thisShard, GuildMember.GuildRanks rank) {
        for (GuildMember member : getMembers().values()) {
            if (!member.isAccepted()) continue;
            if (!member.getRank().isThisRankOrHigher(rank)) {
                continue;
            }
            Player player = Bukkit.getPlayer(member.getUUID());
            if (player != null) {
                player.sendMessage(message);
            }
        }

        if (!thisShard) {
            //Send network message..
            GameAPI.sendNetworkMessage("Guilds", "message", DungeonRealms.getShard().getPseudoName(), String.valueOf(this.getGuildID()), message, rank.getName());
        }
    }

    public void sendGuildMessage(String message, boolean thisShard) {
        this.sendGuildMessage(message, thisShard, GuildMember.GuildRanks.MEMBER);
    }

    public void sendGuildMessage(String message) {
        this.sendGuildMessage(message, false);
    }

    public int getNumberOfGuildMembersOnThisShard() {
        return (int) getMembers().values().stream().map(GuildMember::getUUID).filter(Objects::nonNull).map(Bukkit::getPlayer).filter(Objects::nonNull).count();
    }

    public void promotePlayer(Player promoter, String name, GuildMember member) {
        if (member == null) {
            Bukkit.getLogger().info("Unable to find guild member with name: " + name);
            return;
        }

        SQLDatabaseAPI.getInstance().addQuery(QueryType.SET_GUILD_RANK, "OFFICER", member.getAccountID());
        member.setRank(GuildMember.GuildRanks.OFFICER);
        promoter.sendMessage(ChatColor.DARK_AQUA + "You have " + ChatColor.UNDERLINE + "promoted" + ChatColor.DARK_AQUA + " " + name + " to the rank of " + ChatColor.BOLD + "GUILD OFFICER" + ChatColor.GREEN + ".");
        sendGuildMessage(ChatColor.GREEN + " " + name + " has been " + ChatColor.UNDERLINE + "promoted" + ChatColor.GREEN + " to the rank of " + ChatColor.BOLD + "GUILD OFFICER" + ChatColor.GREEN + ".", false);
        GameAPI.sendNetworkMessage("Guilds", "setrank", DungeonRealms.getShard().getPseudoName(),String.valueOf(getGuildID()), member.getAccountID() + "", GuildMember.GuildRanks.OFFICER.getOrder() + "");
    }

    public GuildMember getOwner() {
        return getMembers().values().stream().filter(Objects::nonNull).filter(member -> member.getRank().equals(GuildMember.GuildRanks.OWNER)).findFirst().orElse(null);

    }

    public String getNamesForInfo(GuildMember.GuildRanks rank) {
        StringBuilder toReturn = new StringBuilder("");
        for (GuildMember member : getMembers().values()) {
            if (member == null) continue;
            if (!member.isAccepted()) continue;
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
        if (banner == null) banner = new ItemStack(Material.BANNER, 1, (byte) 15);
        return ItemSerialization.itemStackToBase64(banner);
    }

    private void setBannerFromString(String banner) {
        if (banner == null || banner.isEmpty()) {
            this.banner = new ItemStack(Material.BANNER, 1, (byte) 15);
            return;
        }

        try {
            this.banner = ItemSerialization.itemStackFromBase64(banner);
        } catch (Exception e) {
            e.printStackTrace();
            this.banner = new ItemStack(Material.BANNER, 1, (byte) 15);
        }
    }


    public String getChatPrefix() {
    	return ChatColor.WHITE + "[" + getTag() + "] ";
    }
}
