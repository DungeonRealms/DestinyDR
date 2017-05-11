package net.dungeonrealms.game.world.entity.type.mounts;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.item.items.functional.ecash.ItemMount;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.minecraft.server.v1_9_R2.NBTTagInt;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MountData {

    @Getter
    String name;

    @Getter
    ChatColor nameColor;

    @Getter
    float speed;

    @Getter
    private int speedPercent;

    @Getter
    List<String> lore = Lists.newArrayList(ChatColor.GRAY + ChatColor.ITALIC.toString() + "A Mystical being, ready to ride into battle.");

    public MountData(String name, ChatColor nameColor, float speed, int speedPercent, String... lore) {
        this.name = name;
        this.nameColor = nameColor;
        this.speed = speed;

        this.speedPercent = speedPercent;
        if (lore != null && lore.length > 0) {
        	this.lore.clear();
        	for (String s : lore)
        		this.lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + s);
        }

    }

    public ItemStack createMountItem(EnumMounts mount) {

    	//TODO: Make item
        List<String> lore = Lists.newArrayList();

        lore.add(ChatColor.RED + "Speed: " + ChatColor.BOLD + speedPercent + "%");

        lore.add("");
        lore.addAll(getLore());

        lore.add(ChatColor.DARK_RED + "Soulbound");
        ItemMount saddle = new ItemMount(AntiDuplication.getInstance().applyAntiDupe(new NBTWrapper(new ItemBuilder().setItem(mount.getSelectionItem().clone())
                .setName(getNameColor() + ChatColor.BOLD.toString() + getName() + " Mount").setLore(lore).build())
                .setString("mount", mount.name())
                .setString("speed", "" + getSpeed())
                .set("soulbound", new NBTTagInt(1))
                .set("untradeable", new NBTTagInt(1))
                .set("puntradeable", new NBTTagInt(1)).set("type", new NBTTagString("mount")).build()));
        return saddle.generateItem();
    }
}
