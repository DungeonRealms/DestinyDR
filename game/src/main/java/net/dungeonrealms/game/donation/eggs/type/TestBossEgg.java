package net.dungeonrealms.game.donation.eggs.type;

import net.dungeonrealms.game.donation.eggs.BossEgg;
import net.dungeonrealms.game.mechanic.ItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/22/2016
 */

public class TestBossEgg extends BossEgg {

    public TestBossEgg() {
        super("test", "Test Boss", 1);
    }


    public ItemStack create(){
        ItemStack bossEgg = ItemManager.createItem(Material.EGG, ChatColor.GOLD.toString() + "Test",
                Arrays.asList((ChatColor.GRAY.toString() + "Right click to " + ChatColor.UNDERLINE + "UNVEIL" + ChatColor.GRAY + " the beastly"),
                        (ChatColor.GRAY.toString() + "test boss."),
                        (ChatColor.RED.toString() + ChatColor.BOLD.toString() + "REQ:" + ChatColor.RED.toString() + " Level 1")).toArray(new String[3]));

        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(bossEgg);
        nms.getTag().setString("bossegg", getID());

        return bossEgg;
    }

}
