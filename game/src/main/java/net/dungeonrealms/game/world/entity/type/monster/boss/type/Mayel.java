package net.dungeonrealms.game.world.entity.type.monster.boss.type;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.enchantments.EnchantmentAPI;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.DungeonManager;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.boss.DungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumDungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.RangedWitherSkeleton;
import net.dungeonrealms.game.world.entity.util.EntityStats;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Chase on Oct 18, 2015
 */
public class Mayel extends RangedWitherSkeleton implements DungeonBoss {

    /**
     * @param world
     */
    public Mayel(World world) {
        super(world);
    }

    public Location loc;

    public Mayel(World world, Location loc) {
        super(world);
        this.loc = loc;
        this.setSize(0.7F, 2.4F);
        this.fireProof = true;
        createEntity(100);
    }
    
    public String[] getItems(){
    	return new String[] {"mayelbow", "mayelhelmet", "mayelchest", "mayelpants", "mayelboot"};
    }

    /**
     * Called when entity fires a projectile.
     */
    @Override
    public void a(EntityLiving entityliving, float f) {
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = this.getEquipment(EnumItemSlot.MAINHAND);
        NBTTagCompound tag = nmsItem.getTag();
        DamageAPI.fireArrowFromMob((CraftLivingEntity) this.getBukkitEntity(), tag, (CraftLivingEntity) entityliving.getBukkitEntity());
    }

    @Override
    public void onBossDeath() {
        
    }

    private boolean canSpawn = false;

    @Override
    public void onBossAttack(EntityDamageByEntityEvent event) {
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        if (canSpawnMobs(livingEntity)) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> canSpawn = false, 100L);
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SPELL, loc, random.nextFloat(), random.nextFloat(), random.nextFloat(), 1F, 100);
            } catch (Exception err) {
                err.printStackTrace();
            }
            for (int i = 0; i < 4; i++) {
                Entity entity = SpawningMechanics.getMob(world, 1, EnumMonster.MayelPirate);
                int level = Utils.getRandomFromTier(2, "high");
                String newLevelName = ChatColor.AQUA.toString() + "[Lvl. " + level + "] ";
                EntityStats.createDungeonMob(entity, level, 1);
                SpawningMechanics.rollElement(entity, EnumMonster.MayelPirate);
                if (entity == null) {
                    return; //WTF?? UH OH BOYS WE GOT ISSUES
                }
                entity.getBukkitEntity().setMetadata("dungeon", new FixedMetadataValue(DungeonRealms.getInstance(), true));
                entity.setCustomName(newLevelName + GameAPI.getTierColor(1).toString() + ChatColor.BOLD + "Mayels Crew");
                entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + GameAPI.getTierColor(1).toString() + ChatColor.BOLD + "Mayels Crew"));
                Location location = new Location(world.getWorld(), getBukkitEntity().getLocation().getX() + random.nextInt(3), getBukkitEntity().getLocation().getY(), getBukkitEntity().getLocation().getZ() + random.nextInt(3));
                entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                ((EntityInsentient) entity).persistent = true;
                ((LivingEntity) entity.getBukkitEntity()).setRemoveWhenFarAway(false);
                world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
            }
            say("Come to my call, brothers!");
        }
    }
    
    public int getXPDrop(){
    	return 5000;
    }
    
    public int getGemDrop(){
    	return random.nextInt(250 - 100) + 100;
    }
    
    private boolean canSpawnMobs(LivingEntity livingEntity) {
        int maxHP = HealthHandler.getInstance().getMonsterMaxHPLive(livingEntity);
        int currentHP = HealthHandler.getInstance().getMonsterHPLive(livingEntity);
        
        if (currentHP <= maxHP * 0.9 && !canSpawn) {
        	canSpawn = true;
        	return true;
        }
        
        return false;
    }

    @Override
    public EnumDungeonBoss getEnumBoss() {
        return EnumDungeonBoss.Mayel;
    }

	@Override
	public void addKillStat(GamePlayer gp) {
		gp.getPlayerStatistics().setMayelKills(gp.getPlayerStatistics().getMayelKills() + 1);
	}
}
