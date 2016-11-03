package net.dungeonrealms.vgame.world.dungeon.type;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.util.AsyncUtils;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.old.game.achievements.Achievements;
import net.dungeonrealms.old.game.mastery.GamePlayer;
import net.dungeonrealms.old.game.mastery.MetadataUtils;
import net.dungeonrealms.old.game.mastery.Utils;
import net.dungeonrealms.old.game.party.Party;
import net.dungeonrealms.old.game.party.PartyMechanics;
import net.dungeonrealms.old.game.title.TitleAPI;
import net.dungeonrealms.old.game.world.entity.EnumEntityType;
import net.dungeonrealms.old.game.world.entity.type.monster.boss.type.Burick;
import net.dungeonrealms.old.game.world.entity.util.EntityStats;
import net.dungeonrealms.old.game.world.teleportation.Teleportation;
import net.dungeonrealms.vgame.Game;
import net.dungeonrealms.vgame.world.dungeon.EnumDungeon;
import net.dungeonrealms.vgame.world.dungeon.EnumDungeonEndReason;
import net.dungeonrealms.vgame.world.dungeon.EnumDungeonStage;
import net.dungeonrealms.vgame.world.dungeon.IDungeon;
import net.minecraft.server.v1_9_R2.Entity;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.ZipFile;

/**
 * Copyright © 2016 Matthew E Development - All Rights Reserved
 * You may NOT use, distribute and modify this code.
 * <p>
 * Created by Matthew E on 11/1/2016 at 2:35 PM.
 */
public class VarengladeDungeon implements IDungeon {

    private Party party;
    private String name;
    private EnumDungeon dungeonEnum;
    private World world;
    private File worldZip;
    private int time;
    private int aliveMobs;
    private int maxAlive;
    private EnumDungeonStage dungeonStage;
    private HashMap<String, HashMap<Location, String>> instance_mob_spawns = new HashMap<>();

    public VarengladeDungeon(Party party) {
        this.dungeonStage = EnumDungeonStage.SETUP;
        this.dungeonEnum = EnumDungeon.VARENGLADE;
        this.name = dungeonEnum.getName();
        this.time = 0;
        this.aliveMobs = 0;
        this.maxAlive = 0;
        this.party = party;
        this.worldZip = new File(Game.getGame().getDataFolder() + File.separator + "dungeons" + File.separator + name + ".zip");
        setupInstance();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Game.getGame(), this::actionBar, 5L, 5L);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Game.getGame(), this::timeTask, 20L, 20L);
    }


    private void timeTask() {
        time++;
        switch (time) {
            case 7200:
                endDungeon(EnumDungeonEndReason.LOSE);
                break;

        }
    }

    public VarengladeDungeon(Player player) {
        GamePlayer gp = GameAPI.getGamePlayer(player);
        if (!gp.isInParty()) {
            PartyMechanics.getInstance().createParty(player);
        }
        new VarengladeDungeon(PartyMechanics.getInstance().getParty(player).get());
    }

    private void setupInstance() {
        String worldName = worldZip.getName().split(".zip")[0];
        AsyncUtils.pool.submit(() -> {
            try {
                unZip(new ZipFile(worldZip), worldName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        AsyncUtils.pool.submit(() -> {
            if (new File(worldName + "/" + "uid.dat").exists()) {
                // Delete that shit.
                new File(worldName + "/" + "uid.dat").delete();
            }
            try {
                FileUtils.forceDelete(new File(worldName + "/players"));
                FileUtils.copyDirectory(new File("plugins/WorldGuard/worlds/" + worldName), new File("plugins/WorldGuard/worlds/varenglade"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            Utils.log.info("Completed setup of Dungeon: " + worldName);
        });
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            WorldCreator worldCreator = new WorldCreator(worldName);
            worldCreator.generateStructures(false);
            World world = Bukkit.getServer().createWorld(worldCreator);
            world.setStorm(false);
            world.setAutoSave(false);
            world.setKeepSpawnInMemory(false);
            world.setPVP(false);
            world.setGameRuleValue("randomTickSpeed", "0");
            Bukkit.getWorlds().add(world);
            this.world = world;
        }, 60L);
        startDungeon();
    }

    @Override
    public Party getParty() {
        return party;
    }

    @Override
    public void startDungeon() {
        this.dungeonStage = EnumDungeonStage.STARTED;
    }

    @Override
    public void endDungeon(EnumDungeonEndReason dungeonEndReason) {
        switch (dungeonEndReason) {
            case COMPLETE:
                getParty().getMembers().forEach(player -> {
                    Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.VARENGLADE);
                });
                break;
            case LOSE:
                break;
        }
    }

    @Override
    public void teleportOut() {
        party.getMembers().forEach(player -> player.teleport(Teleportation.Cyrennica));
    }

    @Override
    public void spawnBoss(Location location) {
        Entity burick = new Burick(((CraftWorld) location.getWorld()).getHandle(), location);
        MetadataUtils.registerEntityMetadata(burick, EnumEntityType.HOSTILE_MOB, 1, 100);
        EntityStats.setBossRandomStats(burick, 100, 3);
        burick.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
        ((CraftWorld) location.getWorld()).getHandle().addEntity(burick, CreatureSpawnEvent.SpawnReason.CUSTOM);
        burick.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
        location.getWorld().playSound(location, Sound.ENTITY_ENDERDRAGON_HURT, 4F, 0.5F);
        return;
    }

    @Override
    public EnumDungeon getDungeonEnum() {
        return dungeonEnum;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void enterDungeon(Player player) {
        player.sendMessage(ChatColor.RED.toString() + "Burick The Fanatic" + ChatColor.RESET + ": How dare you enter my domain!");
        player.teleport(getDungeonWorld().getSpawnLocation());
    }

    @Override
    public World getDungeonWorld() {
        return world;
    }

    @Override
    public void spawnInMobs() {

    }

    public void loadMobs() {
        for (File file : new File("plugins/DungeonRealms/dungeonSpawns/").listFiles()) {
            String fileName = file.getName().replaceAll(".dat", "");
            if (fileName.equalsIgnoreCase(name)) {
                DungeonRealms.getInstance().getLogger().info("Found Dungeon Spawn Template for " + name);
                HashMap<Location, String> dungeonMobData = new HashMap<>();
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    for (String line; (line = br.readLine()) != null; ) {
                        if (line.equalsIgnoreCase("null")) {
                            continue;
                        }
                        if (line.contains("=")) {
                            String[] coordinates = line.split("=")[0].split(",");
                            Location location = new Location(Bukkit.getWorlds().get(0), Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]),
                                    Double.parseDouble(coordinates[2]));
                            String spawnData = line.split("=")[1];
                            dungeonMobData.put(location, spawnData);
                        }
                    }
                    br.close();
                    instance_mob_spawns.put(name, dungeonMobData);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    public void actionBar() {
        if ((dungeonStage == EnumDungeonStage.STARTED) || (dungeonStage == EnumDungeonStage.BOSS)) {
            party.getMembers().forEach(player -> {
                TitleAPI.sendActionBar(player, ChatColor.AQUA + "Time: " + ChatColor.WHITE + ChatColor.GOLD
                        + (time / 60) + "/120" + " " + ChatColor.AQUA + "Alive: " + ChatColor.WHITE + (aliveMobs) + ChatColor.GRAY
                        + "/" + ChatColor.RED + maxAlive);
            });
        }
    }
}
