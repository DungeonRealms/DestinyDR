package net.dungeonrealms.old.game.world.entity.type.monster.boss.type.world;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.old.game.miscellaneous.SkullTextures;
import net.dungeonrealms.old.game.world.entity.type.monster.base.DRPigman;
import net.dungeonrealms.old.game.world.entity.type.monster.boss.WorldBoss;
import net.dungeonrealms.old.game.world.item.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/22/2016
 */

public class Albranir extends DRPigman implements WorldBoss {

    @Getter
    private int phase;

    @Getter
    private final Location eventLocation;

    @Getter
    protected Map<String, Integer[]> attributes = new HashMap<>();

    public Albranir(World world, Location eventLocation) {
        super(world);
        this.phase = 1;
        this.eventLocation = eventLocation;
        ///setArmor(4);

        this.angerLevel = 30000;
        this.setCustomName(ChatColor.AQUA + "Albranir The Bitter");
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), ChatColor.AQUA + "Albranir The Bitter"));
        this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.ICE_BOSS.getSkull()));
        //EntityStats.setBossRandomStats(this, 100, 4);
        this.noDamageTicks = 0;
        this.maxNoDamageTicks = 0;

        for (Player p : this.getBukkitEntity().getWorld().getPlayers())
            p.sendMessage(ChatColor.RED.toString() + "Albranir " + ChatColor.RESET.toString() + ": " + "Are these minor annoyances your pathetic excuses for attacksss?");

        this.setSize(0.7F, 2.4F);

        clearGoalSelectors();

        this.fireProof = true;
        this.goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F));
        freeze();
    }

    @Override
    public void setArmor(int tier) {
        ItemStack weapon = ItemGenerator.getNamedItem("albranir_sword");
        ItemStack boots = ItemGenerator.getNamedItem("albranir_boots");
        ItemStack legs = ItemGenerator.getNamedItem("albranir_legs");
        ItemStack chest = ItemGenerator.getNamedItem("albranir_chest");
        ItemStack head = ItemGenerator.getNamedItem("albranir_helmet");
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
    }

    @Override
    public void onBossDeath() {

    }

    @Override
    public void onBossAttack(EntityDamageByEntityEvent event) {

    }

    public static ItemStack createEgg() {
        return null;
    }

    public static void checkRequirements(Player player) {


    }

    private void freeze() {
        navigation = new NavigationAbstract(this, world) {
            @Override
            protected Pathfinder a() {
                return null;
            }

            @Override
            protected Vec3D c() {
                return null;
            }

            @Override
            protected boolean b() {
                return false;
            }

            @Override
            protected boolean a(Vec3D vec3D, Vec3D vec3D1, int i, int i1, int i2) {
                return false;
            }
        };

    }

    private void clearGoalSelectors() {
        try {
            Field a = PathfinderGoalSelector.class.getDeclaredField("b");
            Field b = PathfinderGoalSelector.class.getDeclaredField("c");
            a.setAccessible(true);
            b.setAccessible(true);
            ((LinkedHashSet) a.get(this.goalSelector)).clear();
            ((LinkedHashSet) b.get(this.goalSelector)).clear();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void switchPhase(int phase) {
        // SWITCH BOSS'S PHASE //
    }
}
