package net.dungeonrealms.frontend.vgame.world;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.common.awt.frame.save.EnumSaveFlag;
import net.dungeonrealms.frontend.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * Created by Giovanni on 1-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class GameWorld implements IWorld {

    @Getter
    private World bukkitWorld;

    @Getter
    private transient List<World> recentUnloadedWorlds;

    @Getter
    @Setter
    private String identifier;

    @Getter
    @Setter
    private List<Double> spawnAxisValues;

    public GameWorld() {
        this.recentUnloadedWorlds = Lists.newArrayList();
        Game.getGame().getServer().getPluginManager().registerEvents(this, Game.getGame());
    }

    /**
     * Change the current game world to another bukkit world
     *
     * @param world The bukkit world to change to
     */
    public void update(World world) {
        Bukkit.getOnlinePlayers().stream().filter(player -> player.getWorld() == this.bukkitWorld).forEach(player -> {
            player.teleport(new Location(world, this.spawnAxisValues.get(0), this.spawnAxisValues.get(1), this.spawnAxisValues.get(2)));
            player.sendMessage(ChatColor.RED + "The world you were in has been unloaded");
        });
        this.bukkitWorld = world;
    }

    /**
     * Save all the current game world generic
     *
     * @param saveFlag The type of save
     */
    public void save(EnumSaveFlag saveFlag) {
        if (saveFlag == EnumSaveFlag.SAVE) {
            this.writeSave();
        } else {
            this.recentUnloadedWorlds.clear(); // We don't want to store unloaded worlds
            this.writeSave();
        }
    }

    private void writeSave() {
        try (FileWriter fileWriter = new FileWriter(Game.getGame().getDataFolder() + File.separator + "world" + File.separator + "gameWorld.json")) {
            fileWriter.write(new Gson().toJson(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (this.recentUnloadedWorlds.contains(event.getPlayer().getWorld())) {
            event.getPlayer().teleport(new Location(this.bukkitWorld, this.spawnAxisValues.get(0), this.spawnAxisValues.get(1), this.spawnAxisValues.get(2)));
            event.getPlayer().sendMessage(ChatColor.RED + "The world you tried to access is unloaded");
        }
    }
}
