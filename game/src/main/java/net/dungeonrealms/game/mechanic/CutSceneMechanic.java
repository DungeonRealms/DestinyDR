package net.dungeonrealms.game.mechanic;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.listener.world.CutSceneListener;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.cutscenes.CutScene;
import net.dungeonrealms.game.mechanic.cutscenes.GsonLocation;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.title.TitleAPI;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CutSceneMechanic implements GenericMechanic {

    @Getter
    private Map<String, CutScene> cutScenes = new ConcurrentHashMap<>();

    @Getter
    private Map<UUID, CutScene> creatingCutscene = new HashMap<>();

    @Getter
    private Map<Player, PlayerCutScene> playerCutScenes = new ConcurrentHashMap<>();

    private static CutSceneMechanic mechanic;

    public static CutSceneMechanic get() {
        if (mechanic == null)
            mechanic = new CutSceneMechanic();
        return mechanic;
    }

    Type token = new TypeToken<CutScene>() {
    }.getType();

    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }


    @Override
    public void startInitialization() {
        File cutSceneFolder = new File(DungeonRealms.getInstance().getDataFolder(), "cutscenes");
        try {
            if (!cutSceneFolder.exists())
                cutSceneFolder.createNewFile();
            if (cutSceneFolder.exists()) {
                for (File inside : cutSceneFolder.listFiles()) {
                    if (inside == null) continue;
                    try {
                        CutScene cutScene = gson.fromJson(new FileReader(inside), token);
                        if (cutScene != null) {
                            this.cutScenes.put(cutScene.getName().toLowerCase(), cutScene);
                            Bukkit.getLogger().info("Loaded " + cutScene.getName() + " Cutscene with " + (cutScene.getLocations() != null ? cutScene.getLocations().size() : 0) + " locations!");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Bukkit.getLogger().info("Unable to parse custscene for file: " + inside.getName() + " Exception: " + ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bukkit.getPluginManager().registerEvents(new CutSceneListener(), DungeonRealms.getInstance());

        new BukkitRunnable() {
            @Override
            public void run() {
                playerCutScenes.forEach((p, scene) -> {
                    if (scene.done || !p.isOnline()) {
                        playerCutScenes.remove(p);
                        return;
                    }
                    scene.processTick(p);
                });
            }
        }.runTaskTimer(DungeonRealms.getInstance(), 1, 1);
    }

    @SneakyThrows
    public void saveCutscene(CutScene scene) {
        File custsene = new File(DungeonRealms.getInstance().getDataFolder(), "cutscenes/" + scene.getName().toLowerCase() + ".cutscene");

        if (!custsene.exists())
            custsene.createNewFile();

        this.cutScenes.put(scene.getName().toLowerCase(), scene);
        String toSet = gson.toJson(scene, token);

        FileWriter writer = new FileWriter(custsene);
        writer.write(toSet);
        writer.flush();
        writer.close();
    }

    public void startCutscene(Player player, CutScene scene) {
        PlayerCutScene pScene = new PlayerCutScene(scene);
        pScene.startCutScene(player);
        playerCutScenes.put(player, pScene);
    }

    @Override
    public void stopInvocation() {

    }

    public static class PlayerCutScene {

        private CutScene scene;
        private int index;

        private ArmorStand stand;

        public PlayerCutScene(CutScene scene) {
            this.scene = scene;
        }

        private Location start;

        private int tick = 0;
        private boolean done = false;

        public void startCutScene(Player player) {
            this.start = player.getLocation().clone();
            player.setGameMode(GameMode.SPECTATOR);
            GsonLocation start = scene.getStart();

            Location loc = new Location(Bukkit.getWorld(start.getWorldName()), start.getX(), start.getY(), start.getZ(), start.getYaw(), start.getPitch());
            this.stand = (ArmorStand) player.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);

            player.teleport(stand.getLocation());

            if (scene.getTitleLength() > 0) {
                TitleAPI.sendTitle(player, 4, scene.getTitleLength(), 10, Utils.translate(scene.getTitle()).replace("_", " "), Utils.translate(scene.getDescription()));
            }

            this.sendUpdatePacket(player, stand.getLocation().getChunk());

            this.attachToStand(player);
        }

        public void processTick(Player player) {
            if (done) return;
            tick++;

            //Skip first few ticks to allow to be updated.
            if (tick < 2) return;
            if (tick != 1 && tick % scene.getTickSpeed() != 0) return;

            if (player.getVehicle() == null || !player.getVehicle().equals(stand)) {
                if (player.getVehicle() != null)
                    player.getVehicle().eject();

                player.teleport(stand.getLocation());
                stand.setPassenger(player);
                return;
            }

            EntityArmorStand nms = (EntityArmorStand) ((CraftEntity) stand).getHandle();

            if (index >= scene.getLocations().size()) {
                endScene(player);
            } else {
                GsonLocation next = scene.getLocations().get(index);

                nms.locX = next.getX();
                nms.locY = next.getY();
                nms.locZ = next.getZ();
                nms.yaw = next.getYaw();
                nms.pitch = next.getPitch();

                Location current = player.getLocation();
                if (current.getYaw() != next.getYaw() || current.getPitch() != next.getPitch()) {
                    EntityPlayer pl = ((CraftPlayer) player).getHandle();
                    pl.yaw = next.getYaw();
                    pl.pitch = next.getPitch();
//
//                    int l = MathHelper.d(next.getYaw() * 256.0F / 360.0F);
//                    int i1 = MathHelper.d(next.getPitch() * 256.0F / 360.0F);
//                    PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook look = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(pl.getId(),
//                            (long) (current.getX() - next.getX()), (long) (current.getY() - next.getY()),
//                            (long) (current.getZ() - next.getZ()), (byte) l, (byte) i1, false);
//                    pl.playerConnection.sendPacket(look);
                }
                index++;
            }
        }

        private void attachToStand(Player player) {
            player.teleport(stand);
            player.setSpectatorTarget(stand);
        }

        private void sendUpdatePacket(Player player, Chunk chunk) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), 65535));
        }

        public void endScene(Player player) {
            done = true;

            player.leaveVehicle();
            //Return to our old target of ourselves.
            player.setSpectatorTarget(player);

            if (this.stand != null) {
                this.stand.eject();
                this.stand.remove();
            }

            player.teleport(start);
            player.setSpectatorTarget(player);
            player.setGameMode(GameMode.SURVIVAL);

            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                this.sendUpdatePacket(player, start.getChunk());
            }, 1);
        }
    }
}
