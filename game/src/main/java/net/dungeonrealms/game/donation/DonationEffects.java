package net.dungeonrealms.game.donation;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.game.util.StringUtils;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.donation.buffs.Buff;
import net.dungeonrealms.game.donation.buffs.LevelBuff;
import net.dungeonrealms.game.donation.buffs.LootBuff;
import net.dungeonrealms.game.donation.buffs.ProfessionBuff;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.world.entity.type.pet.Creeper;
import net.minecraft.server.v1_9_R2.Entity;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * Created by Kieran on 10/1/2015.
 */
@Getter
@Setter
public class DonationEffects implements GenericMechanic {

    private static DonationEffects instance = null;

    public static DonationEffects getInstance() {
        if (instance == null) {
            instance = new DonationEffects();
        }
        return instance;
    }

    //CLOSED BETA PAYERS = RED_DUST
    //HALLOWEEN PLAYERS = SMALL_SMOKE
    //CHRISTMAS PLAYERS = SNOW_SHOVEL

    public HashMap<Player, ParticleAPI.ParticleEffect> PLAYER_PARTICLE_EFFECTS = new HashMap<>();
    public HashMap<Entity, ParticleAPI.ParticleEffect> ENTITY_PARTICLE_EFFECTS = new HashMap<>();
    public ConcurrentHashMap<Location, Material> PLAYER_GOLD_BLOCK_TRAIL_INFO = new ConcurrentHashMap<>();
    @Getter
    public Set<Creeper> fireWorkCreepers = new CopyOnWriteArraySet<>();
    public List<Player> PLAYER_GOLD_BLOCK_TRAILS = new ArrayList<>();


    private LootBuff activeLootBuff = null;
    private ProfessionBuff activeProfessionBuff = null;
    private LevelBuff activeLevelBuff = null;
    private Queue<LootBuff> queuedLootBuffs = new LinkedList<>();
    private Queue<ProfessionBuff> queuedProfessionBuffs = new LinkedList<>();
    private Queue<LevelBuff> queuedLevelBuffs = new LinkedList<>();


    private static Random random = new Random();

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    private static String buffDelimeter = "@#$%";
    @Override
    public void startInitialization() {
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::spawnPlayerParticleEffects, 40L, 2L);
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::spawnEntityParticleEffects, 40L, 2L);
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::removeGoldBlockTrails, 40L, 4L);
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::handleCreeperFireworks, 40L, 100L);

        //Pull the loot buff column we have stored.. should only be 1.
        SQLDatabaseAPI.getInstance().executeQuery("SELECT * FROM buffs LIMIT 1;", rs -> {
            try {
                if (rs.first()) {
                    //Found buff row.
                    this.activeLootBuff = (LootBuff) Buff.deserialize(rs.getString("activeLootBuff"), LootBuff.class);
                    this.queuedLootBuffs = deserialize(StringUtils.deserializeList(rs.getString("queuedLootBuffs"), buffDelimeter), LootBuff.class);

                    this.activeProfessionBuff = (ProfessionBuff) Buff.deserialize(rs.getString("activeProfessionBuff"), ProfessionBuff.class);
                    this.queuedProfessionBuffs = deserialize(StringUtils.deserializeList(rs.getString("queuedProfessionBuffs"), buffDelimeter), ProfessionBuff.class);

                    this.activeLevelBuff = (LevelBuff)Buff.deserialize(rs.getString("activeLevelBuff"), LevelBuff.class);
                    this.queuedProfessionBuffs = deserialize(StringUtils.deserializeList(rs.getString("queuedLevelBuffs"), buffDelimeter), LevelBuff.class);
                }else{
                    //No table there?
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
//        // check if there are available buffs (will be null if not)
//        activeLootBuff = (LootBuff) Buff.deserialize((String) DatabaseAPI.getInstance().getShardData(DungeonRealms
//                .getInstance().bungeeName, "buffs.activeLootBuff"), LootBuff.class);
//        final ArrayList<String> queuedLootBuffData = (ArrayList<String>) DatabaseAPI.getInstance().getShardData(DungeonRealms
//                .getInstance().bungeeName, "buffs.queuedLootBuffs");
//        if (queuedLootBuffData != null && !queuedLootBuffData.isEmpty()) {
//            for (String s : queuedLootBuffData) {
//                queuedLootBuffs.add((LootBuff) Buff.deserialize(s, LootBuff.class));
//            }
//        }
//
//        activeProfessionBuff = (ProfessionBuff) Buff.deserialize((String) DatabaseAPI.getInstance().getShardData
//                (DungeonRealms.getInstance().bungeeName, "buffs.activeProfessionBuff"), ProfessionBuff.class);
//        final ArrayList<String> queuedProfessionBuffData = (ArrayList<String>) DatabaseAPI.getInstance().getShardData(DungeonRealms
//                .getInstance().bungeeName, "buffs.queuedProfessionBuffs");
//        if (queuedProfessionBuffData != null && !queuedProfessionBuffData.isEmpty()) {
//            for (String s : queuedProfessionBuffData) {
//                queuedProfessionBuffs.add((ProfessionBuff) Buff.deserialize(s, ProfessionBuff.class));
//            }
//        }
//
//
//        activeLevelBuff = (LevelBuff) Buff.deserialize((String) DatabaseAPI.getInstance().getShardData(DungeonRealms
//                .getInstance().bungeeName, "buffs.activeLevelBuff"), LevelBuff.class);
//        final ArrayList<String> queuedLevelBuffData = (ArrayList<String>) DatabaseAPI.getInstance().getShardData(DungeonRealms
//                .getInstance().bungeeName, "buffs.queuedLevelBuffs");
//        if (queuedLevelBuffData != null && !queuedLevelBuffData.isEmpty()) {
//            for (String s : queuedLevelBuffData) {
//                queuedLevelBuffs.add((LevelBuff) Buff.deserialize(s, LevelBuff.class));
//            }
//        }

        // expired while we were offline, RIP
        if (activeLootBuff != null && System.currentTimeMillis() > activeLootBuff.getTimeUntilExpiry()) {
//            DatabaseAPI.getInstance().updateShardCollection(DungeonRealms.getInstance().bungeeName, EnumOperators.$UNSET,
//                    "buffs.activeLootBuff", "", true);
            updateLootBuff("activeLootBuff", null);
            activeLootBuff.deactivateBuff();
        }
        if (activeProfessionBuff != null && System.currentTimeMillis() > activeProfessionBuff.getTimeUntilExpiry()) {
//            DatabaseAPI.getInstance().updateShardCollection(DungeonRealms.getInstance().bungeeName, EnumOperators.$UNSET,
//                    "buffs.activeProfessionBuff", "", true);
            updateLootBuff("activeProfessionBuff", null);
            activeProfessionBuff.deactivateBuff();
        }
        if (activeLevelBuff != null && System.currentTimeMillis() > activeLevelBuff.getTimeUntilExpiry()) {
//            DatabaseAPI.getInstance().updateShardCollection(DungeonRealms.getInstance().bungeeName, EnumOperators.$UNSET,
//                    "buffs.activeLevelBuff", "", true);
            updateLootBuff("activeLevelBuff", null);
            activeLevelBuff.deactivateBuff();
        }

        if (activeLootBuff != null) {
            Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> activeLootBuff.deactivateBuff(), (
                    (activeLootBuff.getTimeUntilExpiry() - System.currentTimeMillis()) / 1000) * 20L);
        }
        if (activeLevelBuff != null) {
            Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> activeLevelBuff.deactivateBuff(), (
                    (activeLevelBuff.getTimeUntilExpiry() - System.currentTimeMillis()) / 1000) * 20L);
        }
        if (activeProfessionBuff != null) {
            Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> activeProfessionBuff.deactivateBuff(), (
                    (activeProfessionBuff.getTimeUntilExpiry() - System.currentTimeMillis()) / 1000) * 20L);
        }
    }

    public <T extends Buff> Queue<T> deserialize(List<String> list, Class<T> clazz) {
        Queue<T> buff = new LinkedList<>();
        if (list != null && !list.isEmpty()) {
            return list.stream().map(s -> (T) Buff.deserialize(s, clazz)).collect(Collectors.toCollection(LinkedList::new));
        }
        return null;
    }


    public void updateLootBuff(String lootColumnName, String value){
        SQLDatabaseAPI.getInstance().executeUpdate(updates -> {
            Bukkit.getLogger().info("SET " + lootColumnName + " to " + value + " for loot buff.");
        }, "UPDATE buffs SET " + lootColumnName + " = " + (value == null ? null : "'" + value + "'") + ";");
    }
    @Override
    public void stopInvocation() {
        // update database of active buffs (queued buffs should already be there)
        if (this.activeLootBuff != null)
            DatabaseAPI.getInstance().updateShardCollection(DungeonRealms.getInstance().bungeeName, EnumOperators.$SET,
                    "buffs.activeLootBuff", activeLootBuff.serialize(), true);
        if (this.activeProfessionBuff != null)
            DatabaseAPI.getInstance().updateShardCollection(DungeonRealms.getInstance().bungeeName, EnumOperators.$SET,
                    "buffs.activeProfessionBuff", activeProfessionBuff.serialize(), true);
        if (this.activeLevelBuff != null)
            DatabaseAPI.getInstance().updateShardCollection(DungeonRealms.getInstance().bungeeName, EnumOperators.$SET,
                    "buffs.activeLevelBuff", activeLevelBuff.serialize(), true);
    }

    private void handleCreeperFireworks() {
        if (fireWorkCreepers.isEmpty()) return;
        FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.BLUE, Color.RED, Color.WHITE).withFade(Color.BLUE, Color.RED, Color.WHITE).with(FireworkEffect.Type.STAR).trail(true).build();
        for (Creeper creeper : fireWorkCreepers) {
            if (!creeper.isAlive()) {
                fireWorkCreepers.remove(creeper);
                continue;
            }
            Firework fw = (Firework) creeper.getBukkitEntity().getWorld().spawnEntity(creeper.getBukkitEntity().getLocation(), EntityType.FIREWORK);
            FireworkMeta fwm = fw.getFireworkMeta();
            fwm.addEffect(effect);
            fwm.setPower(1); // 0.5 seconds
            fw.setFireworkMeta(fwm);
        }
    }

    public void spawnPlayerParticleEffects(Location location) {
        if (PLAYER_PARTICLE_EFFECTS.isEmpty()) return;
        for (Player player : PLAYER_PARTICLE_EFFECTS.keySet()) {
            if (!player.isOnline()) {
                PLAYER_PARTICLE_EFFECTS.remove(player);
                continue;
            }
            float moveSpeed = 0.02F;
            ParticleAPI.ParticleEffect particleEffect = PLAYER_PARTICLE_EFFECTS.get(player);
            if (particleEffect == ParticleAPI.ParticleEffect.RED_DUST || particleEffect == ParticleAPI.ParticleEffect.NOTE) {
                moveSpeed = -1F;
            }
            try {
                ParticleAPI.sendParticleToLocation(particleEffect, location.clone().add(0, 0.22, 0), (random.nextFloat()) - 0.4F, (random.nextFloat()) - 0.5F, (random.nextFloat()) - 0.5F, moveSpeed, 6);
            } catch (Exception e) {
                e.printStackTrace();
                Utils.log.warning("[Donations] Could not spawn donation particle " + particleEffect.name() + " for player " + player.getName());
            }
        }
    }

    public void doLogin(Player p) {
        if (this.activeLootBuff != null) {
            int minutesLeft = (int) (((activeLootBuff.getTimeUntilExpiry() - System.currentTimeMillis()) / 1000.0D) / 60.0D);
            int bonusAmount = (int) activeLootBuff.getBonusAmount();
            String playerName = activeLootBuff.getActivatingPlayer();
            if (p != null) {
                p.sendMessage("");
                p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + ">> " + playerName + "'s " + ChatColor.GOLD.toString() + ChatColor.UNDERLINE + "+" + bonusAmount + "% Global Drop Rates" + ChatColor.GOLD
                        + " is active for " + ChatColor.UNDERLINE + minutesLeft + ChatColor.RESET + ChatColor.GOLD + " more minute(s)!");
                p.sendMessage("");
            }
        }
        if (this.activeProfessionBuff != null) {
            int minutesLeft = (int) (((activeProfessionBuff.getTimeUntilExpiry() - System.currentTimeMillis()) / 1000.0D) / 60.0D);
            int bonusAmount = (int) activeProfessionBuff.getBonusAmount();
            String playerName = activeProfessionBuff.getActivatingPlayer();
            if (p != null) {
                p.sendMessage("");
                p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + ">> " + playerName + "'s " + ChatColor.GOLD.toString() + ChatColor.UNDERLINE + "+" + bonusAmount + "% Global Profession EXP Rates" + ChatColor.GOLD
                        + " is active for " + ChatColor.UNDERLINE + minutesLeft + ChatColor.RESET + ChatColor.GOLD + " more minute(s)!");
                p.sendMessage("");
            }
        }
        if (this.activeLevelBuff != null) {
            int minutesLeft = (int) (((activeLevelBuff.getTimeUntilExpiry() - System.currentTimeMillis()) / 1000.0D) / 60.0D);
            int bonusAmount = (int) activeLevelBuff.getBonusAmount();
            String playerName = activeLevelBuff.getActivatingPlayer();
            if (p != null) {
                p.sendMessage("");
                p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + ">> " + playerName + "'s " + ChatColor.GOLD.toString() + ChatColor.UNDERLINE + "+" + bonusAmount + "% Global Character Level EXP Rates" + ChatColor.GOLD
                        + " is active for " + ChatColor.UNDERLINE + minutesLeft + ChatColor.RESET + ChatColor.GOLD + " more minute(s)!");
                p.sendMessage("");
            }
        }
    }

    public void activateNewLootBuffOnThisShard(int duration, float bonusAmount, String activatingPlayer, String shard) {
        final LootBuff newLootBuff = new LootBuff(duration, bonusAmount, activatingPlayer, shard);

        // check if there is already one active
        if (this.activeLootBuff != null) {
            // queue a new buff
            this.queuedLootBuffs.add(newLootBuff);
            DatabaseAPI.getInstance().updateShardCollection(DungeonRealms.getInstance().bungeeName, EnumOperators.$PUSH,
                    "buffs.queuedLootBuffs", newLootBuff.serialize(), true);
            Bukkit.broadcastMessage(ChatColor.GOLD + ">> Player " + newLootBuff.getActivatingPlayer() + ChatColor
                    .GOLD + " has queued a Global Loot Buff set for activation after the current one expires.");
            Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_EGG_THROW, 1f, 1f));
        } else {
            newLootBuff.activateBuff();
        }
    }

    public void activateNewLevelBuffOnThisShard(int duration, float bonusAmount, String activatingPlayer, String shard) {
        final LevelBuff newLevelBuff = new LevelBuff(duration, bonusAmount, activatingPlayer, shard);

        // check if there is already one active
        if (this.activeLevelBuff != null) {
            // queue a new buff
            this.queuedLevelBuffs.add(newLevelBuff);
            DatabaseAPI.getInstance().updateShardCollection(DungeonRealms.getInstance().bungeeName, EnumOperators.$PUSH,
                    "buffs.queuedLevelBuffs", newLevelBuff.serialize(), true);
            Bukkit.broadcastMessage(ChatColor.GOLD + ">> Player " + newLevelBuff.getActivatingPlayer() + ChatColor
                    .GOLD + " has queued a Global Level Buff set for activation after the current one expires.");
            Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_EGG_THROW, 1f, 1f));
        } else {
            newLevelBuff.activateBuff();
        }
    }

    public void activateNewProfessionBuffOnThisShard(int duration, float bonusAmount, String activatingPlayer, String shard) {
        final ProfessionBuff newProfessionBuff = new ProfessionBuff(duration, bonusAmount, activatingPlayer, shard);

        // check if there is already one active
        if (this.activeProfessionBuff != null) {
            // queue a new buff
            this.queuedProfessionBuffs.add(newProfessionBuff);
            DatabaseAPI.getInstance().updateShardCollection(DungeonRealms.getInstance().bungeeName, EnumOperators.$PUSH,
                    "buffs.queuedProfessionBuffs", newProfessionBuff.serialize(), true);
            Bukkit.broadcastMessage(ChatColor.GOLD + ">> Player " + newProfessionBuff.getActivatingPlayer() + ChatColor
                    .GOLD + " has queued a Global Profession Buff set for activation after the current one expires.");
            Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_EGG_THROW, 1f, 1f));
        } else {
            newProfessionBuff.activateBuff();
        }
    }

    private void spawnPlayerParticleEffects() {
        if (PLAYER_PARTICLE_EFFECTS.isEmpty()) return;
        for (Player player : PLAYER_PARTICLE_EFFECTS.keySet()) {
            if (!player.isOnline()) {
                PLAYER_PARTICLE_EFFECTS.remove(player);
                continue;
            }
            float moveSpeed = 0.02F;
            ParticleAPI.ParticleEffect particleEffect = PLAYER_PARTICLE_EFFECTS.get(player);
            if (particleEffect == ParticleAPI.ParticleEffect.RED_DUST || particleEffect == ParticleAPI.ParticleEffect.NOTE) {
                moveSpeed = -1F;
            }
            try {
                ParticleAPI.sendParticleToLocation(particleEffect, player.getLocation().add(0, 0.22, 0), (random.nextFloat()) - 0.4F, (random.nextFloat()) - 0.5F, (random.nextFloat()) - 0.5F, moveSpeed, 6);
            } catch (Exception e) {
                e.printStackTrace();
                Utils.log.warning("[Donations] Could not spawn donation particle " + particleEffect.name() + " for player " + player.getName());
            }
        }
    }

    private void removeGoldBlockTrails() {
        if (PLAYER_GOLD_BLOCK_TRAIL_INFO.isEmpty()) return;
        for (Map.Entry<Location, Material> goldTrails : PLAYER_GOLD_BLOCK_TRAIL_INFO.entrySet()) {
            Location location = goldTrails.getKey();
            int timeRemaining = location.getBlock().getMetadata("time").get(0).asInt();
            timeRemaining--;
            if (timeRemaining <= 0) {
                Material material = goldTrails.getValue();
                location.getBlock().setType(material);
                PLAYER_GOLD_BLOCK_TRAIL_INFO.remove(location);
            } else {
                location.getBlock().setMetadata("time", new FixedMetadataValue(DungeonRealms.getInstance(), timeRemaining));
            }
        }
    }

    private void spawnEntityParticleEffects() {
        if (ENTITY_PARTICLE_EFFECTS.isEmpty()) return;
        for (Entity entity : ENTITY_PARTICLE_EFFECTS.keySet()) {
            if (!entity.isAlive()) {
                ENTITY_PARTICLE_EFFECTS.remove(entity);
                continue;
            }
            float moveSpeed = 0.02F;
            ParticleAPI.ParticleEffect particleEffect = ENTITY_PARTICLE_EFFECTS.get(entity);
            if (particleEffect == ParticleAPI.ParticleEffect.RED_DUST || particleEffect == ParticleAPI.ParticleEffect.NOTE) {
                moveSpeed = -1F;
            }
            Location location = new Location(Bukkit.getWorlds().get(0), entity.locX, entity.locY, entity.locZ);
            try {
                ParticleAPI.sendParticleToLocation(particleEffect, location.add(0, 0.22, 0), (random.nextFloat()) - 0.4F, (random.nextFloat()) - 0.5F, (random.nextFloat()) - 0.5F, moveSpeed, 6);
            } catch (Exception e) {
                e.printStackTrace();
                Utils.log.warning("[Donations] Could not spawn donation particle " + particleEffect.name() + " for entity " + entity.getName());
            }
        }
    }

    public boolean removeECashFromPlayer(Player player, int amount) {
        if (amount <= 0) {
            return true;
            //Someone done fucked up and made it remove a negative amount. Probably Chase.
        }
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return false;

        int playerEcash = wrapper.getEcash();
        if (playerEcash <= 0) {
            return false;
        }
        if (playerEcash - amount >= 0) {

            wrapper.getPlayerGameStats().setEcashSpent(wrapper.getPlayerGameStats().getEcashSpent() + amount);

            wrapper.setEcash(wrapper.getEcash() - amount);
//            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$INC, EnumData.ECASH, (amount * -1), true);
            return true;
        } else {
            return false;
        }
    }
}
