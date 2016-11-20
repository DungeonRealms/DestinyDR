package net.dungeonrealms.vgame.security;

import net.dungeonrealms.vgame.security.exception.CompoundException;
import net.minecraft.server.v1_9_R2.ItemStack;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;

import java.util.UUID;

/**
 * Created by Giovanni on 17-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class UAI
{
    private UUID uniqueId;

    public UAI()
    {
        this.uniqueId = UUID.randomUUID();
    }

    public org.bukkit.inventory.ItemStack attachTo(ItemStack itemStack) throws CompoundException
    {
        NBTTagCompound tagCompound = itemStack.getTag();
        if (tagCompound != null)
        {
            if (!tagCompound.hasKey("atomic"))
            {
                tagCompound.set("atomic", new NBTTagString(this.uniqueId.toString()));
                itemStack.setTag(tagCompound);
                return CraftItemStack.asBukkitCopy(itemStack);
            } else
            {
                throw new CompoundException(itemStack);
            }
        } else
        {
            throw new CompoundException(itemStack);
        }
    }
}
