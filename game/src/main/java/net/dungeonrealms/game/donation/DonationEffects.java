package net.dungeonrealms.game.donation;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.game.util.StringUtils;
import net.dungeonrealms.database.PlayerGameStats.StatColumn;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.ParticleAPI.ParticleEffect;
import net.dungeonrealms.game.mechanic.data.EnumBuff;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class DonationEffects implements GenericMechanic {

    @Getter
    private static DonationEffects instance = new DonationEffects();

    public HashMap<Player, ParticleAPI.ParticleEffect> PLAYER_PARTICLE_EFFECTS = new HashMap<>();
    public ConcurrentHashMap<Location, Material> PLAYER_GOLD_BLOCK_TRAIL_INFO = new ConcurrentHashMap<>();
    private Map<EnumBuff, LinkedList<Buff>> buffMap = new HashMap<>();
    private static String buffDelimeter = "@#$%";

    private static Random random = new Random();

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
    public void startInitialization() {
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::spawnPlayerParticleEffects, 40L, 2L);
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::removeGoldBlockTrails, 40L, 4L);

        SQLDatabaseAPI.getInstance().executeQuery("SELECT * FROM buffs LIMIT 1;", rs -> {
            try {
                if (rs.first()) {

                    for (EnumBuff buffType : EnumBuff.values()) {
                        LinkedList<Buff> buffs = new LinkedList<>();
                        this.buffMap.put(buffType, buffs);
                        List<String> queuedBuffs = StringUtils.deserializeList(rs.getString(buffType.getColumnName()), buffDelimeter);
                        queuedBuffs.forEach(s -> {
                            Buff buff = Buff.deserialize(s);
                            buff.setType(buffType);
                            buffs.add(buff);
                        });
                    }
                }

                rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            handleExpiry();
        });
    }

    private void handleExpiry() {
        boolean changed = false;
        for (EnumBuff buffType : EnumBuff.values()) {
            if (!hasBuff(buffType))
                continue;

            Buff buff = getBuff(buffType);

            //  Expired while offline D:
            if (System.currentTimeMillis() > buff.getTimeUntilExpiry()) {
                buff.deactivate();
                changed = true;
                continue;
            }

            //Set this buff to expire.
            Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), buff::deactivate,
                    (buff.getTimeUntilExpiry() - System.currentTimeMillis()) / 50);
        }

        if (changed)
            saveBuffData();
    }

    public void saveBuffData() {
        for (EnumBuff buffType : EnumBuff.values())
            updateBuff(buffType, serializeQueuedBuffs(getQueuedBuffs(buffType)));
    }

    private String serializeQueuedBuffs(Queue<? extends Buff> buffs) {
        if (buffs == null || buffs.isEmpty())
            return null;
        List<String> list = new ArrayList<>();
        buffs.forEach(b -> list.add(b.serialize()));
        return StringUtils.serializeList(list, buffDelimeter);
    }

    private void updateBuff(EnumBuff buffType, String value) {
        SQLDatabaseAPI.getInstance().executeUpdate(updates -> {
            Bukkit.getLogger().info("SET " + buffType.getColumnName() + " to " + value + " for loot buff.");
        }, "UPDATE buffs SET " + buffType.getColumnName() + " = " + SQLDatabaseAPI.escape(value) + ";");
    }

    @Override
    public void stopInvocation() {
        saveBuffData();
    }

    /**
     * Gets all the queued buffs.
     */
    public Queue<Buff> getQueuedBuffs(EnumBuff type) {
        return buffMap.get(type);
    }

    /**
     * Gets the active buff of this type.
     */
    public Buff getBuff(EnumBuff buffType) {
        return hasBuff(buffType) ? buffMap.get(buffType).getFirst() : null;
    }

    /**
     * Returns if there is at least one buff of this type queued / active.
     */
    public boolean hasBuff(EnumBuff buffType) {
        LinkedList<Buff> buffs = buffMap.get(buffType);
        return buffs != null && !buffs.isEmpty();
    }

    public void doLogin(Player p) {
        for (EnumBuff buffType : EnumBuff.values()) {
            if (!hasBuff(buffType))
                continue;
            Buff buff = getBuff(buffType);
            int minutesLeft = (int) (((buff.getTimeUntilExpiry() - System.currentTimeMillis()) / 1000.0D) / 60.0D);

            p.sendMessage("");
            p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + ">> " + buff.getActivatingPlayer() + "'s " + ChatColor.GOLD.toString() + ChatColor.UNDERLINE + "+" + buff.getBonusAmount() + "% "
                    + ChatColor.stripColor(buff.getType().getDescription()) + ChatColor.GOLD + " is active for " + ChatColor.UNDERLINE + minutesLeft + ChatColor.RESET + ChatColor.GOLD + " more minute(s)!");
            p.sendMessage("");
        }
    }

    public void activateLocalBuff(Buff buff) {
        boolean existingBuff = hasBuff(buff.getType());
        this.buffMap.get(buff.getType()).add(buff);
        saveBuffData();

        if (existingBuff) {
            Bukkit.broadcastMessage(ChatColor.GOLD + ">> Player " + buff.getActivatingPlayer() + ChatColor
                    .GOLD + " has queued a " + buff.getType().getItemName() + " set for activation after the current one expires.");
            Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_EGG_THROW, 1f, 1f));
            return;
        }

        buff.activate();
    }

    private void spawnPlayerParticleEffects() {
        for (Map.Entry<Player, ParticleEffect> entry : PLAYER_PARTICLE_EFFECTS.entrySet()) {
            if (!entry.getKey().isOnline()) {
                PLAYER_PARTICLE_EFFECTS.remove(entry.getKey());
                continue;
            }

            ParticleEffect effect = entry.getValue();
            if (effect == ParticleEffect.GOLD_BLOCK) continue;
            ParticleAPI.sendParticleToLocation(effect, entry.getKey().getLocation().add(0, 0.22, 0), random.nextFloat() - 0.4F, random.nextFloat() - 0.5F, random.nextFloat() - 0.5F,
                    effect == ParticleEffect.REDSTONE || effect == ParticleEffect.NOTE ? -1F : 0.02F, 6);
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

    public boolean removeECashFromPlayer(Player player, int amount) {
        if (amount <= 0)
            return true;

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null)
            return false;

        int playerEcash = wrapper.getEcash();
        if (playerEcash <= 0)
            return false;

        if (playerEcash - amount >= 0) {
            wrapper.getPlayerGameStats().addStat(StatColumn.ECASH_SPENT, amount);

            wrapper.setEcash(wrapper.getEcash() - amount);
            return true;
        }
        return false;
    }

    public static boolean isGoldenCursable(Material material) {
        return material == Material.DIRT || material == Material.GRASS || material == Material.STONE
                || material == Material.COBBLESTONE || material == Material.GRAVEL || material == Material.LOG
                || material == Material.SMOOTH_BRICK || material == Material.BEDROCK || material == Material.GLASS
                || material == Material.SANDSTONE || material == Material.SAND || material == Material.BOOKSHELF
                || material == Material.MOSSY_COBBLESTONE || material == Material.OBSIDIAN
                || material == Material.SNOW_BLOCK || material == Material.CLAY || material == Material.STAINED_CLAY
                || material == Material.WOOL;
    }
}