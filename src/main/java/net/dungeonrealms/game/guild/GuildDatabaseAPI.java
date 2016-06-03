package net.dungeonrealms.game.guild;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public interface GuildDatabaseAPI {

    void startInitialization();


    /**
     * @param guildName Guild Name.
     * @param clanTag   Clan Tag.
     * @param owner     owner UUID
     * @param action    boolean.
     */

    void createGuild(String guildName, String clanTag, UUID owner, Consumer<Boolean> action);


    boolean isGuildNull(UUID uuid);

    /**
     * Gets the guild of a player.
     *
     * @param uuid Player
     * @return Gets guild that player is in
     */
    String getGuildOf(UUID uuid);

    /**
     * Demotes a player from [OFFICER] to [MEMBER]
     *
     * @param guildName Name of guild
     * @param uuid      Player
     */
    void demotePlayer(String guildName, UUID uuid);

    /**
     * Promotes a player, like [MEMBER] to [OFFICER]
     *
     * @param guildName Name of guild
     * @param uuid      Player
     */
    void promotePlayer(String guildName, UUID uuid);

    /**
     * @param executor  Executor
     * @param guildName Name of guild
     * @param uuid      Player
     */
    void kickFrom(UUID executor, String guildName, UUID uuid);

    /**
     * @param uuid      Player
     * @param guildName Name of guild
     */
    boolean isOwner(UUID uuid, String guildName);

    /**
     * @param uuid      Player
     * @param guildName Name of guild
     */
    boolean isMember(UUID uuid, String guildName);

    /**
     * @param uuid      Player
     * @param guildName Name of guild
     */
    boolean isOfficer(UUID uuid, String guildName);

    /**
     * Saves all guilds.
     */
    void saveAllGuilds();

    /**
     * @param guildName Name of guild.
     * @param motd      the motd
     */
    void setMotdOf(String guildName, String motd);

    /**
     * @param player target player
     * @return boolean
     */
    boolean isOwnerOfGuild(UUID player);

    /**
     * @param guildName targeted guild.
     * @return Ally list.
     */
    List<String> getEnemiesOf(String guildName);

    /**
     * @param guildName targeted guild.
     * @return Ally list.
     */
    List<String> getAlliesOf(String guildName);

    /**
     * @param guildName targeted guild.
     * @param message   sends all a message.
     */
    void sendAlert(String guildName, String message);


    void saveGuild(String guildName);

    /**
     * @param guildName targeted guild.
     * @return The clanTag
     */
    String getClanTagOf(String guildName);

    /**
     * @param guildName name wanting the players.
     * @return The offline players of a guild.
     */
    List<String> getAllOfflineOf(String guildName);

    /**
     * @param guildName name wanting the players.
     * @return The online players of a guild.
     */
    List<String> getAllOnlineNamesOf(String guildName);

    /**
     * @param guildName name wanting the players.
     * @return The online players of a guild.
     */
    List<UUID> getAllOnlineOf(String guildName);

    /**
     * Sets the players guild.
     *
     * @param uuid      Player
     * @param guildName Name of Guild
     */
    void setGuild(UUID uuid, String guildName);


    /**
     * @param clanTag Name of ClanTag.
     * @param action  ASync Callback.
     * @return boolean.
     */
    boolean doesClanTagExist(String clanTag, Consumer<Boolean> action);

    /**
     * @param guildName Name of the Guild.
     * @param action    Async Callback.
     * @return boolean.
     */
    boolean doesGuildNameExist(String guildName, Consumer<Boolean> action);

}
