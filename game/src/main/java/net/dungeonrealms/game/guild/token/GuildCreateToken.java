package net.dungeonrealms.game.guild.token;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/25/2016
 */

public class GuildCreateToken {

    @Getter
    @Setter
    private UUID owner;

    @Getter
    @Setter
    private String guildName, displayName, tag;

    @Getter
    @Setter
    private ItemStack currentBanner = new ItemStack(Material.BANNER, 1, (byte) 15);

}