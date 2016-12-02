package net.dungeonrealms.frontend.vgame.item.security;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.common.awt.frame.handler.Handler;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Giovanni on 17-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class NUAIHolder implements Handler {

    @Getter
    private static NUAIHolder holder;

    @Getter
    private final AtomicReference<List<ItemStack>> atomicList = new AtomicReference<>();

    @Getter
    private boolean prepared;

    @Getter
    private UUID uniqueId;

    @Override
    public void prepare() {
        holder = this;
        this.atomicList.set(Lists.newArrayList());

        this.uniqueId = UUID.randomUUID();
        this.prepared = true;
    }

    @Override
    public void disable() {
        this.uniqueId = null;
        this.prepared = false;
    }

    public void flush() {
        this.atomicList.get().clear();
    }
}
