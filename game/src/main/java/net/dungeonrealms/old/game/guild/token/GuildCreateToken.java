package net.dungeonrealms.old.game.guild.token;

import lombok.Data;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/25/2016
 */
@Data
public class GuildCreateToken {

    private UUID owner;

    private String guildName, displayName, tag;

    private ItemStack currentBanner = new ItemStack(Material.BANNER, 1, (byte) 15);

}