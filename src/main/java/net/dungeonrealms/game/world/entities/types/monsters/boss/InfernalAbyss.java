package net.dungeonrealms.game.world.entities.types.monsters.boss;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumBoss;
import net.dungeonrealms.game.world.entities.types.monsters.MeleeMobs.MeleeWitherSkeleton;
import net.dungeonrealms.game.world.entities.types.monsters.boss.subboss.InfernalGhast;
import net.dungeonrealms.game.world.entities.types.monsters.boss.subboss.InfernalLordsGuard;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.dungeonrealms.game.world.items.Item.ItemRarity;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.DamageSource;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Created by Chase on Oct 21, 2015
 */
public class InfernalAbyss extends MeleeWitherSkeleton implements Boss {

    public InfernalGhast ghast;
    public InfernalLordsGuard guard;

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
        this.setOnFire(Integer.MAX_VALUE);
        this.getBukkitEntity().setCustomNameVisible(true);
        int level = 50;
        MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, getEnumBoss().tier, level);
        this.getBukkitEntity().setMetadata("boss", new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().nameid));
        EntityStats.setBossRandomStats(this, level, getEnumBoss().tier);
        this.getBukkitEntity().setCustomName(ChatColor.RED.toString() + ChatColor.UNDERLINE.toString() + getEnumBoss().name);
        for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
            p.sendMessage(ChatColor.RED.toString() + "The Infernal Abyss" + ChatColor.RESET.toString() + ": " + "I have nothing to say to you foolish mortals, except for this: Burn.");
        }
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            if (!this.getBukkitEntity().isDead()) {
                this.getBukkitEntity().getLocation().add(0, 1, 0).getBlock().setType(Material.FIRE);
            }
        }, 0, 20L);
        ghast = new InfernalGhast(this);
        guard = new InfernalLordsGuard(this);
        guard.isInvulnerable(DamageSource.FALL);
        guard.setLocation(locX, locY, locZ, 1, 1);
        this.setSize(0.7F, 2.4F);
        this.fireProof = true;
        this.setSkeletonType(1);
        setArmor(getEnumBoss().tier);
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


    @Override
    public void onBossHit(EntityDamageByEntityEvent event) {
        if (!finalForm)
            if (this.ghast.isAlive() || this.guard.isAlive()) {
                for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
                    p.sendMessage(ChatColor.RED.toString() + "The Infernal Abyss" + ChatColor.RESET.toString() + ": " + "Hah! You must take out my minions.");
                }
                event.setDamage(0);
                event.setCancelled(true);
                return;
            }

        LivingEntity en = (LivingEntity) event.getEntity();
        double seventyFivePercent = HealthHandler.getInstance().getMonsterMaxHPLive(en) * 0.75;

        if (HealthHandler.getInstance().getMonsterHPLive(en) <= seventyFivePercent && !hasFiredGhast) {
            for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
                p.sendMessage(ChatColor.RED.toString() + "The Infernal Abyss" + ChatColor.RESET.toString() + ": " + "Taste FIRE!");
            }
            ghast.setLocation(this.locX, this.locY + 4, this.locZ, 1, 1);
            this.getWorld().addEntity(ghast, SpawnReason.CUSTOM);
            ghast.init();
            this.isInvulnerable(DamageSource.STUCK);
            this.setLocation(locX, locY + 100, locZ, 1, 1);
            hasFiredGhast = true;
        }
    }

}
