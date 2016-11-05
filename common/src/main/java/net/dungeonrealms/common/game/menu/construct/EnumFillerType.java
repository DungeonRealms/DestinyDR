package net.dungeonrealms.common.game.menu.construct;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by Giovanni on 5-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public enum EnumFillerType
{
    YELLOW((byte) 4),
    ORANGE((byte) 1),
    GREEN((byte) 13),
    LIME((byte) 5),
    RED((byte) 14),
    BLACK((byte) 15),
    LIGHT_GRAY((byte) 8);

    @Getter
    private ItemStack itemStack;

    EnumFillerType(byte _byte)
    {
        this.itemStack = new ItemStack(Material.THIN_GLASS, _byte);
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        itemMeta.setDisplayName("");
        this.itemStack.setItemMeta(itemMeta);
    }
}
