package net.dungeonrealms.vgame.security;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.common.awt.handler.SuperHandler;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Giovanni on 17-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class NUAIHolder implements SuperHandler.Handler
{
    @Getter
    private static NUAIHolder holder;

    @Getter
    private final AtomicReference<List<ItemStack>> atomicList = new AtomicReference<>();

    @Override
    public void prepare()
    {
        holder = this;
        this.atomicList.set(Lists.newArrayList());
    }

    public void flush()
    {
        this.atomicList.get().clear();
    }
}
