package net.dungeonrealms.old.game.donation;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.old.game.donation.buffs.Buff;
import net.dungeonrealms.old.game.donation.buffs.LevelBuff;
import net.dungeonrealms.old.game.donation.buffs.LootBuff;
import net.dungeonrealms.old.game.donation.buffs.ProfessionBuff;
import net.dungeonrealms.old.game.mastery.GamePlayer;
import net.dungeonrealms.old.game.mastery.Utils;
import net.dungeonrealms.old.game.mechanic.ParticleAPI;
import net.dungeonrealms.old.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.old.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.old.game.world.entity.type.pet.Creeper;
import net.dungeonrealms.vgame.Game;
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
public class DonationEffects implements GenericMechanic
{

    private static DonationEffects instance = null;

    public static DonationEffects getInstance()
    {
        if (instance == null)
        {
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
    public EnumPriority startPriority()
    {
        return EnumPriority.CATHOLICS;
    }

    @Override
    public void startInitialization()
    {
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::spawnPlayerParticleEffects, 40L, 2L);
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::spawnEntityParticleEffects, 40L, 2L);
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::removeGoldBlockTrails, 40L, 4L);
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::handleCreeperFireworks, 40L, 100L);

        // check if there are available buffs (will be null if not)
        activeLootBuff = (LootBuff) Buff.deserialize((String) DatabaseAPI.getInstance().getShardData(Game.getGame().getGameShard().getBungeeIdentifier()
                , "buffs.activeLootBuff"), LootBuff.class);
        final ArrayList<String> queuedLootBuffData = (ArrayList<String>) DatabaseAPI.getInstance().getShardData(Game.getGame().getGameShard().getBungeeIdentifier(), "" +
                "buffs.queuedLootBuffs");
        if (queuedLootBuffData != null && !queuedLootBuffData.isEmpty())
        {
            queuedLootBuffs.addAll(queuedLootBuffData.stream().map(s -> (LootBuff) Buff.deserialize(s, LootBuff.class)).collect(Collectors.toList()));
        }

        activeProfessionBuff = (ProfessionBuff) Buff.deserialize((String) DatabaseAPI.getInstance().getShardData
                (Game.getGame().getGameShard().getBungeeIdentifier(), "buffs.activeProfessionBuff"), ProfessionBuff.class);
        final ArrayList<String> queuedProfessionBuffData = (ArrayList<String>) DatabaseAPI.getInstance().getShardData(Game.getGame().getGameShard().getBungeeIdentifier(),
                "buffs.queuedProfessionBuffs");
        if (queuedProfessionBuffData != null && !queuedProfessionBuffData.isEmpty())
        {
            queuedProfessionBuffs.addAll(queuedProfessionBuffData.stream().map(s -> (ProfessionBuff) Buff.deserialize(s, ProfessionBuff.class)).collect(Collectors.toList()));
        }


        activeLevelBuff = (LevelBuff) Buff.deserialize((String) DatabaseAPI.getInstance().getShardData(Game.getGame().getGameShard().getBungeeIdentifier(), "buffs.activeLevelBuff"), LevelBuff.class);
        final ArrayList<String> queuedLevelBuffData = (ArrayList<String>) DatabaseAPI.getInstance().getShardData(Game.getGame().getGameShard().getBungeeIdentifier(),
                "buffs.queuedLevelBuffs");
        if (queuedLevelBuffData != null && !queuedLevelBuffData.isEmpty())
        {
            queuedLevelBuffs.addAll(queuedLevelBuffData.stream().map(s -> (LevelBuff) Buff.deserialize(s, LevelBuff.class)).collect(Collectors.toList()));
        }

        // expired while we were offline, RIP
        if (activeLootBuff != null && System.currentTimeMillis() > activeLootBuff.getTimeUntilExpiry())
        {
            DatabaseAPI.getInstance().updateShardCollection(Game.getGame().getGameShard().getBungeeIdentifier(), EnumOperators.$UNSET,
                    "buffs.activeLootBuff", "", true);
            activeLootBuff.deactivateBuff();
        }
        if (activeProfessionBuff != null && System.currentTimeMillis() > activeProfessionBuff.getTimeUntilExpiry())
        {
            DatabaseAPI.getInstance().updateShardCollection(Game.getGame().getGameShard().getBungeeIdentifier(), EnumOperators.$UNSET,
                    "buffs.activeProfessionBuff", "", true);
            activeProfessionBuff.deactivateBuff();
        }
        if (activeLevelBuff != null && System.currentTimeMillis() > activeLevelBuff.getTimeUntilExpiry())
        {
            DatabaseAPI.getInstance().updateShardCollection(Game.getGame().getGameShard().getBungeeIdentifier(), EnumOperators.$UNSET,
                    "buffs.activeLevelBuff", "", true);
            activeLevelBuff.deactivateBuff();
        }

        if (activeLootBuff != null)
        {
            Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> activeLootBuff.deactivateBuff(), (
                    (activeLootBuff.getTimeUntilExpiry() - System.currentTimeMillis()) / 1000) * 20L);
        }
        if (activeLevelBuff != null)
        {
            Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> activeLevelBuff.deactivateBuff(), (
                    (activeLevelBuff.getTimeUntilExpiry() - System.currentTimeMillis()) / 1000) * 20L);
        }
        if (activeProfessionBuff != null)
        {
            Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> activeProfessionBuff.deactivateBuff(), (
                    (activeProfessionBuff.getTimeUntilExpiry() - System.currentTimeMillis()) / 1000) * 20L);
        }
    }

    @Override
    public void stopInvocation()
    {
        // update database of active buffs (queued buffs should already be there)
        if (this.activeLootBuff != null)
            DatabaseAPI.getInstance().updateShardCollection(Game.getGame().getGameShard().getBungeeIdentifier(), EnumOperators.$SET,
                    "buffs.activeLootBuff", activeLootBuff.serialize(), true);
        if (this.activeProfessionBuff != null)
            DatabaseAPI.getInstance().updateShardCollection(Game.getGame().getGameShard().getBungeeIdentifier(), EnumOperators.$SET,
                    "buffs.activeProfessionBuff", activeProfessionBuff.serialize(), true);
        if (this.activeLevelBuff != null)
            DatabaseAPI.getInstance().updateShardCollection(Game.getGame().getGameShard().getBungeeIdentifier(), EnumOperators.$SET,
                    "buffs.activeLevelBuff", activeLevelBuff.serialize(), true);
    }

    private void handleCreeperFireworks()
    {
        if (fireWorkCreepers.isEmpty()) return;
        FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.BLUE, Color.RED, Color.WHITE).withFade(Color.BLUE, Color.RED, Color.WHITE).with(FireworkEffect.Type.STAR).trail(true).build();
        for (Creeper creeper : fireWorkCreepers)
        {
            if (!creeper.isAlive())
            {
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

    public void spawnPlayerParticleEffects(Location location)
    {
        if (PLAYER_PARTICLE_EFFECTS.isEmpty()) return;
        for (Player player : PLAYER_PARTICLE_EFFECTS.keySet())
        {
            if (!player.isOnline())
            {
                PLAYER_PARTICLE_EFFECTS.remove(player);
                continue;
            }
            float moveSpeed = 0.02F;
            ParticleAPI.ParticleEffect particleEffect = PLAYER_PARTICLE_EFFECTS.get(player);
            if (particleEffect == ParticleAPI.ParticleEffect.RED_DUST || particleEffect == ParticleAPI.ParticleEffect.NOTE)
            {
                moveSpeed = -1F;
            }
            try
            {
                ParticleAPI.sendParticleToLocation(particleEffect, location.clone().add(0, 0.22, 0), (random.nextFloat()) - 0.4F, (random.nextFloat()) - 0.5F, (random.nextFloat()) - 0.5F, moveSpeed, 6);
            } catch (Exception e)
            {
                e.printStackTrace();
                Utils.log.warning("[Donations] Could not spawn donation particle " + particleEffect.name() + " for player " + player.getName());
            }
        }
    }

    public void doLogin(Player p)
    {
        if (this.activeLootBuff != null)
        {
            int minutesLeft = (int) (((activeLootBuff.getTimeUntilExpiry() - System.currentTimeMillis()) / 1000.0D) / 60.0D);
            int bonusAmount = (int) activeLootBuff.getBonusAmount();
            String playerName = activeLootBuff.getActivatingPlayer();
            if (p != null)
            {
                p.sendMessage("");
                p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + ">> " + playerName + "'s " + ChatColor.GOLD.toString() + ChatColor.UNDERLINE + "+" + bonusAmount + "% Global Drop Rates" + ChatColor.GOLD
                        + " is active for " + ChatColor.UNDERLINE + minutesLeft + ChatColor.RESET + ChatColor.GOLD + " more minute(s)!");
                p.sendMessage("");
            }
        }
        if (this.activeProfessionBuff != null)
        {
            int minutesLeft = (int) (((activeProfessionBuff.getTimeUntilExpiry() - System.currentTimeMillis()) / 1000.0D) / 60.0D);
            int bonusAmount = (int) activeProfessionBuff.getBonusAmount();
            String playerName = activeProfessionBuff.getActivatingPlayer();
            if (p != null)
            {
                p.sendMessage("");
                p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + ">> " + playerName + "'s " + ChatColor.GOLD.toString() + ChatColor.UNDERLINE + "+" + bonusAmount + "% Global Profession EXP Rates" + ChatColor.GOLD
                        + " is active for " + ChatColor.UNDERLINE + minutesLeft + ChatColor.RESET + ChatColor.GOLD + " more minute(s)!");
                p.sendMessage("");
            }
        }
        if (this.activeLevelBuff != null)
        {
            int minutesLeft = (int) (((activeLevelBuff.getTimeUntilExpiry() - System.currentTimeMillis()) / 1000.0D) / 60.0D);
            int bonusAmount = (int) activeLevelBuff.getBonusAmount();
            String playerName = activeLevelBuff.getActivatingPlayer();
            if (p != null)
            {
                p.sendMessage("");
                p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + ">> " + playerName + "'s " + ChatColor.GOLD.toString() + ChatColor.UNDERLINE + "+" + bonusAmount + "% Global Character Level EXP Rates" + ChatColor.GOLD
                        + " is active for " + ChatColor.UNDERLINE + minutesLeft + ChatColor.RESET + ChatColor.GOLD + " more minute(s)!");
                p.sendMessage("");
            }
        }
    }

    public void activateNewLootBuffOnThisShard(int duration, float bonusAmount, String activatingPlayer, String shard)
    {
        final LootBuff newLootBuff = new LootBuff(duration, bonusAmount, activatingPlayer, shard);

        // check if there is already one active
        if (this.activeLootBuff != null)
        {
            // queue a new buff
            this.queuedLootBuffs.add(newLootBuff);
            DatabaseAPI.getInstance().updateShardCollection(Game.getGame().getGameShard().getBungeeIdentifier(), EnumOperators.$PUSH,
                    "buffs.queuedLootBuffs", newLootBuff.serialize(), true);
            Bukkit.broadcastMessage(ChatColor.GOLD + ">> Player " + newLootBuff.getActivatingPlayer() + ChatColor
                    .GOLD + " has queued a Global Loot Buff set for activation after the current one expires.");
            Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_EGG_THROW, 1f, 1f));
        } else
        {
            newLootBuff.activateBuff();
        }
    }

    public void activateNewLevelBuffOnThisShard(int duration, float bonusAmount, String activatingPlayer, String shard)
    {
        final LevelBuff newLevelBuff = new LevelBuff(duration, bonusAmount, activatingPlayer, shard);

        // check if there is already one active
        if (this.activeLevelBuff != null)
        {
            // queue a new buff
            this.queuedLevelBuffs.add(newLevelBuff);
            DatabaseAPI.getInstance().updateShardCollection(Game.getGame().getGameShard().getBungeeIdentifier(), EnumOperators.$PUSH,
                    "buffs.queuedLevelBuffs", newLevelBuff.serialize(), true);
            Bukkit.broadcastMessage(ChatColor.GOLD + ">> Player " + newLevelBuff.getActivatingPlayer() + ChatColor
                    .GOLD + " has queued a Global Level Buff set for activation after the current one expires.");
            Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_EGG_THROW, 1f, 1f));
        } else
        {
            newLevelBuff.activateBuff();
        }
    }

    public void activateNewProfessionBuffOnThisShard(int duration, float bonusAmount, String activatingPlayer, String shard)
    {
        final ProfessionBuff newProfessionBuff = new ProfessionBuff(duration, bonusAmount, activatingPlayer, shard);

        // check if there is already one active
        if (this.activeProfessionBuff != null)
        {
            // queue a new buff
            this.queuedProfessionBuffs.add(newProfessionBuff);
            DatabaseAPI.getInstance().updateShardCollection(Game.getGame().getGameShard().getBungeeIdentifier(), EnumOperators.$PUSH,
                    "buffs.queuedProfessionBuffs", newProfessionBuff.serialize(), true);
            Bukkit.broadcastMessage(ChatColor.GOLD + ">> Player " + newProfessionBuff.getActivatingPlayer() + ChatColor
                    .GOLD + " has queued a Global Profession Buff set for activation after the current one expires.");
            Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_EGG_THROW, 1f, 1f));
        } else
        {
            newProfessionBuff.activateBuff();
        }
    }

    private void spawnPlayerParticleEffects()
    {
        if (PLAYER_PARTICLE_EFFECTS.isEmpty()) return;
        for (Player player : PLAYER_PARTICLE_EFFECTS.keySet())
        {
            if (!player.isOnline())
            {
                PLAYER_PARTICLE_EFFECTS.remove(player);
                continue;
            }
            float moveSpeed = 0.02F;
            ParticleAPI.ParticleEffect particleEffect = PLAYER_PARTICLE_EFFECTS.get(player);
            if (particleEffect == ParticleAPI.ParticleEffect.RED_DUST || particleEffect == ParticleAPI.ParticleEffect.NOTE)
            {
                moveSpeed = -1F;
            }
            try
            {
                ParticleAPI.sendParticleToLocation(particleEffect, player.getLocation().add(0, 0.22, 0), (random.nextFloat()) - 0.4F, (random.nextFloat()) - 0.5F, (random.nextFloat()) - 0.5F, moveSpeed, 6);
            } catch (Exception e)
            {
                e.printStackTrace();
                Utils.log.warning("[Donations] Could not spawn donation particle " + particleEffect.name() + " for player " + player.getName());
            }
        }
    }

    private void removeGoldBlockTrails()
    {
        if (PLAYER_GOLD_BLOCK_TRAIL_INFO.isEmpty()) return;
        for (Map.Entry<Location, Material> goldTrails : PLAYER_GOLD_BLOCK_TRAIL_INFO.entrySet())
        {
            Location location = goldTrails.getKey();
            int timeRemaining = location.getBlock().getMetadata("time").get(0).asInt();
            timeRemaining--;
            if (timeRemaining <= 0)
            {
                Material material = goldTrails.getValue();
                location.getBlock().setType(material);
                PLAYER_GOLD_BLOCK_TRAIL_INFO.remove(location);
            } else
            {
                location.getBlock().setMetadata("time", new FixedMetadataValue(DungeonRealms.getInstance(), timeRemaining));
            }
        }
    }

    private void spawnEntityParticleEffects()
    {
        if (ENTITY_PARTICLE_EFFECTS.isEmpty()) return;
        for (Entity entity : ENTITY_PARTICLE_EFFECTS.keySet())
        {
            if (!entity.isAlive())
            {
                ENTITY_PARTICLE_EFFECTS.remove(entity);
                continue;
            }
            float moveSpeed = 0.02F;
            ParticleAPI.ParticleEffect particleEffect = ENTITY_PARTICLE_EFFECTS.get(entity);
            if (particleEffect == ParticleAPI.ParticleEffect.RED_DUST || particleEffect == ParticleAPI.ParticleEffect.NOTE)
            {
                moveSpeed = -1F;
            }
            Location location = new Location(Bukkit.getWorlds().get(0), entity.locX, entity.locY, entity.locZ);
            try
            {
                ParticleAPI.sendParticleToLocation(particleEffect, location.add(0, 0.22, 0), (random.nextFloat()) - 0.4F, (random.nextFloat()) - 0.5F, (random.nextFloat()) - 0.5F, moveSpeed, 6);
            } catch (Exception e)
            {
                e.printStackTrace();
                Utils.log.warning("[Donations] Could not spawn donation particle " + particleEffect.name() + " for entity " + entity.getName());
            }
        }
    }

    public boolean removeECashFromPlayer(Player player, int amount)
    {
        if (amount <= 0)
        {
            return true;
            //Someone done fucked up and made it remove a negative amount. Probably Chase.
        }
        int playerEcash = (int) DatabaseAPI.getInstance().getData(EnumData.ECASH, player.getUniqueId());
        if (playerEcash <= 0)
        {
            return false;
        }
        if (playerEcash - amount >= 0)
        {
            GamePlayer gamePlayer = GameAPI.getGamePlayer(player);
            if (gamePlayer == null) return false;
            gamePlayer.getPlayerStatistics().setEcashSpent(gamePlayer.getPlayerStatistics().getEcashSpent() + amount);
            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$INC, EnumData.ECASH, (amount * -1), true);
            return true;
        } else
        {
            return false;
        }
    }
}
