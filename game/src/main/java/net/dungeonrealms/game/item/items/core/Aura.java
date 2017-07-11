package net.dungeonrealms.game.item.items.core;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.util.TimeUtil;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.RomanNumeralUtils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class Aura {

    private String name;
    private AuraType type;

    private Location center;

    private double mult;

    private int maxTime;

//    @Getter
//    @Setter
//    private int level = 1;

    //    @Getter
    @Setter
    private List<AtomicInteger> seconds;

    private List<Location> particleLocations = Lists.newLinkedList();

    private ArmorStand stand, timerStand, itemStand;
    private Item item;

    @Getter
    private int radius = 20;

    public Aura(Location loc, String name, AuraType type, double mult, int duration) {
        this.name = name;
        this.center = loc;
        this.type = type;
        this.mult = mult;
        this.maxTime = duration;
        this.seconds = Lists.newArrayList(new AtomicInteger(duration));

        int amount = 200, radius = 10;
        World world = center.getWorld();
        double increment = 2 * Math.PI / amount;
        for (int i = 0; i < amount; i++) {
            double angle = i * increment;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            particleLocations.add(new Location(world, x, center.getY() + 1, z));
        }
        secondIndex = this.particleLocations.size() / 2;

        this.stand = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().add(0, 2, 0), EntityType.ARMOR_STAND);
        prepareStand(this.stand);
        this.timerStand = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().add(0, 1.75, 0), EntityType.ARMOR_STAND);
        this.prepareStand(this.timerStand);
        this.itemStand = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().add(0, .95, 0), EntityType.ARMOR_STAND);
        this.prepareStand(this.itemStand);
        this.itemStand.setMarker(false);

        ItemStack dia = new ItemStack(Material.DIAMOND, 1);
        dia.addUnsafeEnchantment(Enchantment.DAMAGE_UNDEAD, 1);

        this.item = this.itemStand.getWorld().dropItem(this.itemStand.getEyeLocation(), dia);
        this.item.setPickupDelay(Integer.MAX_VALUE);
        MetadataUtils.Metadata.NO_PICKUP.set(item, true);
        this.itemStand.setPassenger(this.item);
    }

    public double getMultiplier() {
        int level = getLevel();
        if (level > 1) {
            double mult = this.mult;

            return mult + (level - 1) * Math.max(1.25, mult / 10);
        }
        return mult;
    }

    public int getLevel() {
        return seconds.size();
    }

    private void prepareStand(ArmorStand stand) {
        stand.setGravity(false);
        stand.setMarker(true);
        stand.setInvulnerable(true);
        stand.setCollidable(false);
        stand.setVisible(false);
        stand.setMetadata("stand", new FixedMetadataValue(DungeonRealms.getInstance(), ""));
    }

    public void remove() {
        this.item.remove();
        this.timerStand.remove();
        this.stand.remove();
        this.itemStand.remove();
    }

    public void update() {
        if (this.stand.isDead()) return;
        if (!this.stand.isCustomNameVisible())
            this.stand.setCustomNameVisible(true);

        this.stand.setCustomName(getArmorStandName());

        int seconds = getSecondsRemaining();
        //Never despawn..
        this.item.setTicksLived(10);
        this.timerStand.setCustomName(ChatColor.GREEN + "Expires in " + TimeUtil.formatDifference(seconds));
        if (!this.timerStand.isCustomNameVisible())
            this.timerStand.setCustomNameVisible(true);
    }

    public int getSecondsRemaining() {
        return seconds.size() <= 0 ? 0 : seconds.get(0).get();
    }

    public boolean isArmorStand(Entity entity) {
        return entity.equals(this.stand) || entity.equals(this.timerStand) || entity.equals(this.itemStand);
    }

    //    private
    private String getArmorStandName() {
        return type.getColor() + name + "'s " + type.getName(false) + " Aura (LVL. " + RomanNumeralUtils.numeralOf(getLevel()) + ")";
    }

    private int lastIndex = 0;
    private int secondIndex = 0;


    public void playParticle() {
        if (lastIndex >= particleLocations.size())
            lastIndex = 0;

        if (secondIndex >= particleLocations.size())
            secondIndex = 0;

        Particle particle = type.getParticle();
        ParticleAPI.spawnParticle(particle, particleLocations.get(lastIndex), 1, 0F, 0F);
        ParticleAPI.spawnParticle(particle, particleLocations.get(secondIndex), 1, 0F, 0F);

        lastIndex++;
        secondIndex++;
    }

    public boolean isInsideAura(Location loc) {
        if (!loc.getWorld().equals(center.getWorld())) return false;
        return loc.distanceSquared(center) <= 20 * 20 / 2;
    }
}
