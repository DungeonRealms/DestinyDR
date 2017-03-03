package net.dungeonrealms.game.world.entity.type.mounts;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.minecraft.server.v1_9_R2.Entity;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran on 10/16/2015.
 */
public enum EnumMounts {
    TIER1_HORSE(0, "T1HORSE", new ItemStack(Material.SADDLE, 1), 0, "Old Horse"),
    TIER2_HORSE(1, "T2HORSE", new ItemStack(Material.IRON_BARDING, 1), 0, "Squire's Horse"),
    TIER3_HORSE(2, "T3HORSE", new ItemStack(Material.DIAMOND_BARDING, 1), 0, "Traveler's Horse"),
    TIER4_HORSE(3, "T4HORSE", new ItemStack(Material.GOLD_BARDING, 1), 0, "Knight's Horse"),
    MULE(4, "MULE", new ItemStack(Material.CHEST, 1), 0, "Mule"),

    WOLF(6, "WOLF", new ItemStack(Material.BONE, 1), 0, "Wolf Mount",
            new MountData("Wolf", ChatColor.WHITE, 0.245F, "140%",
                    Lists.newArrayList(
                            ChatColor.GRAY + ChatColor.ITALIC.toString() + "A ferocious beast, said to have",
                            ChatColor.GRAY + ChatColor.ITALIC.toString() + "slept at the side of Mayel The Cruel.")), 5),
                            
    SLIME(7, "SLIME", new ItemStack(Material.SLIME_BALL, 1), 0, "Slime Mount", new MountData("Slime", ChatColor.GREEN, 0.35F, "170%",
            Lists.newArrayList(
                    ChatColor.GRAY + ChatColor.ITALIC.toString() + "A quick slime found deep",
                    ChatColor.GRAY + ChatColor.ITALIC.toString() + "in the Varenglade Ruins")), 3),
                    
    SPIDER(5, "SPIDER", new ItemStack(Material.STRING, 1), 0, "Spider Mount", new MountData("Spider", ChatColor.LIGHT_PURPLE, 0.31F, "190%", null), 5);

    private int id;
    private String name;
    private ItemStack selectionItem;
    private int shortID;
    private String displayName;
    @Getter private int chance;
    @Getter
    private MountData mountData;

    public int getId() {
        return id;
    }

    public String getRawName() {
        return name;
    }

    public ItemStack getSelectionItem() {
        return selectionItem;
    }

    public short getShortID() {
        return (short) shortID;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    EnumMounts(int id, String name, ItemStack selectionItem, int shortID, String displayName) {
    	this.id = id;
        this.name = name;
        this.selectionItem = selectionItem;
        this.shortID = shortID;
        this.displayName = displayName;
    }

    EnumMounts(int id, String name, ItemStack selectionItem, int shortID, String displayName, MountData data, int chance) {
	    this(id, name, selectionItem, shortID, displayName);
        this.mountData = data;
        this.chance = chance;
    }

    public static EnumMounts getById(int id) {
        for (EnumMounts em : values()) {
            if (em.getId() == id) {
                return em;
            }
        }
        return null;
    }

    public static EnumMounts getByName(String rawName) {
        for (EnumMounts em : values()) {
            if (em.getRawName().equalsIgnoreCase(rawName)) {
                return em;
            }
        }
        return null;
    }
}
