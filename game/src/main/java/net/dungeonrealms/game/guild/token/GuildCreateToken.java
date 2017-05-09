package net.dungeonrealms.game.guild.token;

import lombok.Data;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/25/2016
 */
@Data
public class GuildCreateToken {

    private UUID owner;

    private String guildName, displayName, tag;

    private ItemStack currentBanner = null;
}