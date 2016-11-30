package net.dungeonrealms.common.frontend.menu.construct;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

/**
 * Created by Giovanni on 5-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface IGUI extends Listener {
    Inventory openInventory(Player player);
}
