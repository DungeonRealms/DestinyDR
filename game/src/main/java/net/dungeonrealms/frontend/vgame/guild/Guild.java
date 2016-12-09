package net.dungeonrealms.frontend.vgame.guild;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.frontend.Game;
import net.dungeonrealms.frontend.vgame.guild.exception.GuildExistsException;
import net.dungeonrealms.frontend.vgame.guild.exception.InvalidNameException;
import net.dungeonrealms.frontend.vgame.guild.profile.GuildProfile;
import net.dungeonrealms.frontend.vgame.guild.role.EnumGuildRole;
import net.dungeonrealms.frontend.vgame.player.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Giovanni on 9-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class Guild {

    @Getter
    private UUID ownerId;

    @Getter
    private String name;

    @Getter
    @Setter
    private String motd = "";

    // Online members
    @Getter
    private transient ConcurrentHashMap<UUID, GuildProfile> members;

    // Raw members, online & offline, used to store in mongo
    @Getter
    private List<UUID> rawMembers;

    public Guild(String name, UUID ownerId) throws GuildExistsException, InvalidNameException {
        // Construct the guild
        if (!Game.getGame().getRegistryRegistry().getGuildRegistry().getGuildCache().containsKey(name)) {
            if (name.length() < 4) {
                this.name = name;
                this.ownerId = ownerId;
                this.members = new ConcurrentHashMap<>();
                this.rawMembers = Lists.newArrayList();
                // Add the owner to the raw member map
                this.rawMembers.add(ownerId);
                // Update the owner's profile
                GuildProfile guildProfile = Game.getGame().getRegistryRegistry().getPlayerRegistry().getPlayer(ownerId).getGuildProfile();
                guildProfile.updateTo(this, EnumGuildRole.OWNER);
                // Update the online member list
                this.members.put(ownerId, guildProfile);
            } else throw new InvalidNameException(name, "name size is above 4");
        } else throw new GuildExistsException(name);
    }

    /**
     * Hands over the leader-ship or disbands the guild
     */
    public void disband() {
        // Are there any officers which can take the leader task?
        if (this.getMembersByRole(EnumGuildRole.OFFICER).size() > 0) {
            this.setOwner(Game.getGame().getRegistryRegistry()
                    .getPlayerRegistry().getPlayer(this.getMemberByRole(EnumGuildRole.OFFICER).getOwner()));
        } else {
            for (UUID uuid : this.members.keySet()) {
                // Flush all profiles, also the owner's profile
                this.members.get(uuid).flush();
                String ownerName = Bukkit.getPlayer(this.ownerId).getName();
                // Clear the members
                this.members.clear();
                this.rawMembers.clear();
                this.notify(new String[]{ChatColor.RED + ownerName + " has disbanded the guild"});
            }
            Game.getGame().getRegistryRegistry().getGuildRegistry().getGuildCache().remove(this.name);
        }
    }

    /**
     * Hands over the leader-ship to another player
     *
     * @param gamePlayer The new owner
     */
    public void setOwner(GamePlayer gamePlayer) {
        // Remove the old owner, keep them as a member
        this.getMemberByRole(EnumGuildRole.OWNER).updateRole(EnumGuildRole.MEMBER);
        // Set the new owner
        gamePlayer.getGuildProfile().updateRole(EnumGuildRole.OWNER);
        this.ownerId = gamePlayer.getData().getUniqueId();
        this.notify(new String[]{gamePlayer.getPlayer().getName() + " has been selected as the new " + ChatColor.UNDERLINE + "GUILD LEADER"});
    }

    /**
     * Get a guild member by UUID
     *
     * @param uniqueId The unique id
     * @return The guild profile of the member
     */
    public GuildProfile getMember(UUID uniqueId) {
        return this.members.get(uniqueId);
    }

    /**
     * Get the first member with a specific role
     *
     * @param guildRole Target role
     * @return The first member in the map with that role
     */
    public GuildProfile getMemberByRole(EnumGuildRole guildRole) {
        for (GuildProfile guildProfile : this.members.values()) {
            if (guildProfile.getGuildRole() == guildRole) {
                return guildProfile;
            }
        }
        return null;
    }

    /**
     * Get all members with a specific role
     *
     * @param guildRole Target role
     * @return The members with the specific role
     */
    public List<GuildProfile> getMembersByRole(EnumGuildRole guildRole) {
        List<GuildProfile> profiles = Lists.newArrayList();
        profiles.addAll(this.members.values().stream().filter(guildProfile -> guildProfile.getGuildRole() == guildRole).collect(Collectors.toList()));
        return profiles;
    }

    /**
     * Remove a player from the guild
     *
     * @param gamePlayer The target
     */
    public void removeMember(GamePlayer gamePlayer) {
        if (this.members.containsKey(gamePlayer.getData().getUniqueId())) {
            this.members.get(gamePlayer.getData().getUniqueId()).flush();
            // Remove from maps
            this.members.remove(gamePlayer.getData().getUniqueId());
            rawMembers.remove(gamePlayer.getPlayer().getUniqueId());
        }
    }

    /**
     * Add a player to the guild
     *
     * @param gamePlayer The target
     */
    public void addMember(GamePlayer gamePlayer) {
        if (!this.members.containsKey(gamePlayer.getData().getUniqueId())) {
            // Add to maps
            this.members.put(gamePlayer.getData().getUniqueId(), gamePlayer.getGuildProfile());
            this.rawMembers.add(gamePlayer.getData().getUniqueId());
            // Update profile
            gamePlayer.getGuildProfile().updateTo(this, EnumGuildRole.MEMBER);
            // Notify all members
            this.notify(new String[]{ChatColor.GRAY + gamePlayer.getPlayer().getName() + " has " + ChatColor.UNDERLINE + "joined" + ChatColor.GRAY + " your guild"});
        }
    }

    /**
     * Send a message to all guild members
     *
     * @param message The message
     */
    public void notify(String[] message) {
        String prefix = ChatColor.DARK_AQUA + "<" + ChatColor.BOLD + this.name + ChatColor.DARK_AQUA + "> " + ChatColor.DARK_AQUA;
        for (UUID uuid : this.members.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            player.sendMessage(prefix + Arrays.toString(message));
        }

        // TODO send to all shards
    }

    /**
     * Get the raw member count
     *
     * @return Raw member count
     */
    public int members() {
        return this.rawMembers.size();
    }
}
