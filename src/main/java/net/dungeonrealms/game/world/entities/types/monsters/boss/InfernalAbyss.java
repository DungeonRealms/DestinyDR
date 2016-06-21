package net.dungeonrealms.game.world.entities.types.monsters.boss;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.DungeonManager;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumBoss;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.MeleeMobs.MeleeWitherSkeleton;
import net.dungeonrealms.game.world.entities.types.monsters.boss.subboss.InfernalGhast;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.dungeonrealms.game.world.items.Item.ItemRarity;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.minecraft.server.v1_9_R2.DamageSource;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

/**
 * Created by Chase on Oct 21, 2015
 */
public class InfernalAbyss extends MeleeWitherSkeleton implements Boss {

    public InfernalGhast ghast;

    public InfernalAbyss(World world) {
        super(world);
    }

    /**
     * @param world
     */
    public InfernalAbyss(World world, Location loc) {
        super(world);
        this.setSkeletonType(1);
        this.fireProof = true;
        this.getBukkitEntity().setCustomNameVisible(true);
        int bossLevel = 50;
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.29D);
        MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, getEnumBoss().tier, bossLevel);
        this.getBukkitEntity().setMetadata("boss", new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().nameid));
        EntityStats.setBossRandomStats(this, bossLevel, getEnumBoss().tier);
        this.getBukkitEntity().setCustomName(ChatColor.RED.toString() + ChatColor.UNDERLINE + "The Infernal Abyss");
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), ChatColor.RED.toString() + ChatColor.UNDERLINE + "The Infernal Abyss"));
        for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
            p.sendMessage(ChatColor.RED.toString() + "The Infernal Abyss" + ChatColor.RESET.toString() + ": " + "I have nothing to say to you foolish mortals, except for this: Burn.");
        }
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            if (!this.getBukkitEntity().isDead()) {
                this.getBukkitEntity().getLocation().add(0, 1, 0).getBlock().setType(Material.FIRE);
            }
        }, 0, 15L);
        ghast = new InfernalGhast(this);
        this.setSize(0.7F, 2.4F);
        this.fireProof = true;
        this.setSkeletonType(1);
        setArmor(getEnumBoss().tier);
        DungeonManager.getInstance().getFireUnderEntity().add(this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            if (!this.ghast.isAlive()) {
                if (random.nextInt(30) <= 4) {
                    return;
                }
                Location hit_loc = this.getBukkitEntity().getLocation();
                int numToSpawn = random.nextInt(2) + 1;
                if (random.nextBoolean()) {
                } else {
                    for (int i = 0; i <= numToSpawn; i++) {
                        net.minecraft.server.v1_9_R2.Entity entity = SpawningMechanics.getMob(world, 2, EnumMonster.MagmaCube);
                        int level = Utils.getRandomFromTier(4, "low");
                        String newLevelName = org.bukkit.ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] ";
                        MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, 4, level);
                        EntityStats.createDungeonMob(entity, level, 4);
                        if (entity == null) {
                            return; //WTF?? UH OH BOYS WE GOT ISSUES
                        }
                        entity.setCustomName(newLevelName + API.getTierColor(4).toString() + "Demonic Spawn of Inferno");
                        entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + API.getTierColor(4).toString() + "Demonic Spawn of Inferno"));
                        Location location = new Location(world.getWorld(), hit_loc.getX() + new Random().nextInt(3), hit_loc.getY(), hit_loc.getZ() + new Random().nextInt(3));
                        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                        world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    }
                }
            }
        }, 150L, 300L);
    }

    public void setArmor(int tier) {
        ItemStack weapon = getWeapon();
        ItemStack boots = ItemGenerator.getNamedItem("infernalboot");
        ItemStack legs = ItemGenerator.getNamedItem("infernallegging");
        ItemStack chest = ItemGenerator.getNamedItem("infernalchest");
        ItemStack head = ItemGenerator.getNamedItem("infernalhelmet");
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
        this.setEquipment(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(boots));
        this.setEquipment(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(legs));
        this.setEquipment(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(chest));
        this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(head));
        livingEntity.getEquipment().setItemInMainHand(weapon);
        livingEntity.getEquipment().setBoots(boots);
        livingEntity.getEquipment().setLeggings(legs);
        livingEntity.getEquipment().setChestplate(chest);
        livingEntity.getEquipment().setHelmet(head);
        ghast.setArmor(new ItemGenerator().setTier(ItemTier.getByTier(tier)).setRarity(ItemRarity.UNIQUE).getArmorSet(),
                new ItemGenerator().setTier(ItemTier.getByTier(tier)).setRarity(ItemRarity.UNIQUE)
                        .setType(ItemType.getRandomWeapon()).generateItem().getItem());

    }

    private ItemStack getWeapon() {
        //TODO: Probably make him a melee boss. As he was always glitching due to being a staff mob previously.
        return ItemGenerator.getNamedItem("infernalstaff");
    }

    @Override
    public EnumBoss getEnumBoss() {
        return EnumBoss.InfernalAbyss;
    }

    @Override
    public void onBossDeath() {
        // Giant Explosion that deals massive damage
        if (hasFiredGhast) {
            for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
                p.sendMessage(ChatColor.RED.toString() + "The Infernal Abyss" + ChatColor.RESET.toString() + ": " + "You have defeated me. ARGHGHHG!");
            }
        }
    }

    private boolean hasFiredGhast = false;
    public boolean finalForm = false;

    public void doFinalForm(double hp) {
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        livingEntity.setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), (int) hp));
        HealthHandler.getInstance().setMonsterHPLive(livingEntity, (int) hp);
        livingEntity.setMaximumNoDamageTicks(0);
        livingEntity.setNoDamageTicks(0);
        livingEntity.removePotionEffect(PotionEffectType.INVISIBILITY);
        finalForm = true;
        for (Player pl : livingEntity.getWorld().getPlayers()) {
            pl.sendMessage(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "The Infernal Abyss: " + ChatColor.WHITE
                    + "You... cannot... kill me IN MY OWN DOMAIN, FOOLISH MORTALS!");
            pl.sendMessage(ChatColor.GRAY + "The Infernal Abyss has become enraged! " + ChatColor.UNDERLINE + "+50% DMG!");
            pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERDRAGON_GROWL, 2F, 0.85F);
            pl.playSound(pl.getLocation(), Sound.ENTITY_GHAST_DEATH, 2F, 0.85F);
        }
        Location hit_loc = livingEntity.getLocation();
        for (int i = 0; i < 4; i++) {
            net.minecraft.server.v1_9_R2.Entity entity = SpawningMechanics.getMob(world, 3, EnumMonster.MagmaCube);
            int level = Utils.getRandomFromTier(3, "low");
            String newLevelName = org.bukkit.ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] ";
            MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, 3, level);
            EntityStats.createDungeonMob(entity, level, 3);
            if (entity == null) {
                return; //WTF?? UH OH BOYS WE GOT ISSUES
            }
            entity.setCustomName(newLevelName + API.getTierColor(3).toString() + "Demonic Spawn of Inferno");
            entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + API.getTierColor(3).toString() + "Demonic Spawn of Inferno"));
            Location location = new Location(world.getWorld(), hit_loc.getX() + random.nextInt(3), hit_loc.getY(), hit_loc.getZ() + random.nextInt(3));
            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
            world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
        }
        //TODO: Enable double damage from attacks somehow (insert into list or apply metadata).
        //TODO: Enable double armor (takes half damage from attacks) [same as above].
    }

    private void pushAwayPlayer(Entity entity, Player p, double speed) {
        org.bukkit.util.Vector unitVector = p.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize();
        double e_y = entity.getLocation().getY();
        double p_y = p.getLocation().getY();

        Material m = p.getLocation().subtract(0, 1, 0).getBlock().getType();

        if ((p_y - 1) <= e_y || m == Material.AIR) {
            p.setVelocity(unitVector.multiply((speed)));
        }
    }

    @Override
    public void onBossHit(EntityDamageByEntityEvent event) {
        LivingEntity en = (LivingEntity) event.getEntity();
        if (event.getDamager() instanceof Player) {
            Player p_attacker = (Player) event.getDamager();
            if (p_attacker.getLocation().distanceSquared(en.getLocation()) <= 16) {
                pushAwayPlayer(en, p_attacker, 2.5F);
                p_attacker.setFireTicks(80);
                p_attacker.playSound(p_attacker.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1F, 1F);
            }
        }
        if (!finalForm) {
            if (hasFiredGhast) {
                if (this.ghast.isAlive()) {
                    event.setDamage(0);
                    event.setCancelled(true);
                    return;
                } else {
                    en.setMaximumNoDamageTicks(0);
                    en.setNoDamageTicks(0);
                    en.removePotionEffect(PotionEffectType.INVISIBILITY);
                }
            }
        }

        double halfHP = HealthHandler.getInstance().getMonsterMaxHPLive(en) * 0.5;
        if (HealthHandler.getInstance().getMonsterHPLive(en) <= halfHP && !hasFiredGhast) {
            for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
                p.sendMessage(ChatColor.RED.toString() + "The Infernal Abyss" + ChatColor.RESET.toString() + ": " + "Behold, the powers of the inferno.");
            }
            ghast.setLocation(this.locX, this.locY + 4, this.locZ, 1, 1);
            this.getWorld().addEntity(ghast, SpawnReason.CUSTOM);
            ghast.init(HealthHandler.getInstance().getMonsterHPLive(en));
            this.isInvulnerable(DamageSource.STUCK);
            hasFiredGhast = true;
            en.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15));
            en.setMaximumNoDamageTicks(Integer.MAX_VALUE);
            en.setNoDamageTicks(Integer.MAX_VALUE);
        }

        if (!this.ghast.isAlive()) {
            if (random.nextInt(15) == 1) {
                Location hit_loc = this.getBukkitEntity().getLocation();
                int minionType = random.nextInt(2);
                try {
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.LARGE_SMOKE, hit_loc.add(0, 0.5, 0), random.nextFloat(), random.nextFloat(), random.nextFloat(), 1F, 100);
                } catch (Exception err) {
                    err.printStackTrace();
                }
                if (minionType == 0) {
                    if (finalForm) {
                        for (int i = 0; i < 4; i++) {
                            net.minecraft.server.v1_9_R2.Entity entity = SpawningMechanics.getMob(world, 3, EnumMonster.Silverfish);
                            int level = Utils.getRandomFromTier(3, "low");
                            String newLevelName = org.bukkit.ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] ";
                            MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, 3, level);
                            EntityStats.createDungeonMob(entity, level, 3);
                            if (entity == null) {
                                return; //WTF?? UH OH BOYS WE GOT ISSUES
                            }
                            entity.setCustomName(newLevelName + API.getTierColor(3).toString() + "Abyssal Demon");
                            entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + API.getTierColor(3).toString() + "Abyssal Demon"));
                            Location location = new Location(world.getWorld(), hit_loc.getX() + random.nextInt(3), hit_loc.getY(), hit_loc.getZ() + random.nextInt(3));
                            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                            world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                        }
                    } else {
                        for (int i = 0; i < 4; i++) {
                            net.minecraft.server.v1_9_R2.Entity entity = SpawningMechanics.getMob(world, 3, EnumMonster.MagmaCube);
                            int level = Utils.getRandomFromTier(3, "low");
                            String newLevelName = org.bukkit.ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] ";
                            MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, 3, level);
                            EntityStats.createDungeonMob(entity, level, 3);
                            if (entity == null) {
                                return; //WTF?? UH OH BOYS WE GOT ISSUES
                            }
                            entity.setCustomName(newLevelName + API.getTierColor(3).toString() + "Demonic Spawn of Inferno");
                            entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + API.getTierColor(3).toString() + "Demonic Spawn of Inferno"));
                            Location location = new Location(world.getWorld(), hit_loc.getX() + random.nextInt(3), hit_loc.getY(), hit_loc.getZ() + random.nextInt(3));
                            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                            world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                        }
                    }
                } else if (minionType == 1) {
                    if (finalForm) {
                        for (int i = 0; i < 2; i++) {
                            net.minecraft.server.v1_9_R2.Entity entity = SpawningMechanics.getMob(world, 4, EnumMonster.Silverfish);
                            int level = Utils.getRandomFromTier(4, "low");
                            String newLevelName = org.bukkit.ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] ";
                            MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, 4, level);
                            EntityStats.createDungeonMob(entity, level, 4);
                            if (entity == null) {
                                return; //WTF?? UH OH BOYS WE GOT ISSUES
                            }
                            entity.setCustomName(newLevelName + API.getTierColor(4).toString() + "Greater Abyssal Demon");
                            entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + API.getTierColor(4).toString() + "Greater Abyssal Demon"));
                            Location location = new Location(world.getWorld(), hit_loc.getX() + random.nextInt(3), hit_loc.getY(), hit_loc.getZ() + random.nextInt(3));
                            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                            world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                        }
                    } else {
                        for (int i = 0; i < 2; i++) {
                            net.minecraft.server.v1_9_R2.Entity entity = SpawningMechanics.getMob(world, 4, EnumMonster.MagmaCube);
                            int level = Utils.getRandomFromTier(4, "low");
                            String newLevelName = org.bukkit.ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] ";
                            MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, 4, level);
                            EntityStats.createDungeonMob(entity, level, 4);
                            if (entity == null) {
                                return; //WTF?? UH OH BOYS WE GOT ISSUES
                            }
                            entity.setCustomName(newLevelName + API.getTierColor(4).toString() + "Spawn of Inferno");
                            entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + API.getTierColor(4).toString() + "Demonic Spawn of Inferno"));
                            Location location = new Location(world.getWorld(), hit_loc.getX() + random.nextInt(3), hit_loc.getY(), hit_loc.getZ() + random.nextInt(3));
                            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                            world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                        }
                    }
                }
            }
        }
    }
}
