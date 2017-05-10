/*package net.dungeonrealms.game.listener.mechanic;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.dungeonrealms.game.player.banks.BankMechanics;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class EasterListener implements Listener {

    private ItemStack skullItem;

    private List<EasterBunny> easterRabbits = Lists.newArrayList();

    private String bunnyName = "Rar349";

    public static ItemStack createEasterEgg() {
        return new ItemBuilder().setItem(new ItemStack(Material.EGG, 1))
                .setName(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Easter Egg " + ChatColor.GRAY + "(Right Click)")
                .setLore(Lists.newArrayList(ChatColor.GRAY + "Throw on the ground to", ChatColor.GRAY + "unlock some prizes!"))
                .setNBTString("easterEgg", "").build();
    }

    public EasterListener() {
        this.skullItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta sm = (SkullMeta) this.skullItem.getItemMeta();
        sm.setOwner(bunnyName);
        this.skullItem.setItemMeta(sm);


        List<Material> displayMaterials = Lists.newArrayList(Material.MAGMA_CREAM, Material.EMERALD, Material.EMPTY_MAP, Material.FIREWORK_CHARGE, Material.BREAD);
        //Show 10 items total?
        int maxTicks = 25;
        int realMax = 35;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            Iterator<EasterBunny> bunnies = easterRabbits.iterator();
            while (bunnies.hasNext()) {

                EasterBunny bunny = bunnies.next();
                if (!bunny.getRoller().isOnline()) {
                    bunnies.remove();
                    bunny.destroy();
                    continue;
                }

                if (bunny.getTicks() < maxTicks) {
                    bunny.setTicks(bunny.getTicks() + 1);
                    if (bunny.getTicks() == maxTicks) {
                        //Set the real prize from the chance
                        ItemStack item = getRandomPrizeMaterial();
                        bunny.getItem().setItemStack(item);
                    } else {
                        if (bunny.getTicks() % 2 == 0)
                            bunny.getRoller().playSound(bunny.getRoller().getLocation(), Sound.BLOCK_NOTE_PLING, .8F, 1.1F);

                        //Just display, show orbs like crazy...
                        bunny.getItem().setItemStack(new ItemStack(displayMaterials.get(ThreadLocalRandom.current().nextInt(displayMaterials.size()))));
                    }
                } else if (bunny.getTicks() == maxTicks) {

                    //Give prize?
                    ItemStack prize = createRealPrize(bunny.getPrize());

                    Player player = bunny.getRoller();
                    //Give prize??
                    if (player.getInventory().firstEmpty() == -1) {
                        //Only allow them to pickup...
                        ItemManager.whitelistItemDrop(player, player.getWorld().dropItem(player.getLocation(), prize));
                    } else {
                        player.getInventory().addItem(prize);
                    }

                    ParticleAPI.sendParticleToEntityLocation(ParticleAPI.ParticleEffect.HAPPY_VILLAGER, bunny.getItem(), .5F, .5F, .5F, .1F, 30);
                    String prizeName = prize.getItemMeta().hasDisplayName() ? prize.getItemMeta().getDisplayName() : StringUtils.capitaliseAllWords(prize.getType().name().replace("_", " ").toLowerCase());
                    player.sendMessage(ChatColor.GRAY + "You received " + (prize.getAmount() > 1 ? prize.getAmount() + "x " : "a(n) ") + prizeName + ChatColor.GRAY + " from your " + ChatColor.LIGHT_PURPLE + "Easter Egg" + ChatColor.GRAY + "!");
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 3F, .8F);
                    player.updateInventory();
                    bunny.setTicks(bunny.getTicks() + 1);
                    //Remove the item..
                } else if (bunny.getTicks() > maxTicks) {
                    if (bunny.getTicks() >= realMax) {
                        //Really destroy...
                        ParticleAPI.sendParticleToEntityLocation(ParticleAPI.ParticleEffect.CLOUD, bunny.getArmorStand(), .3F, .25F, .3F, .1F, 10);
                        bunny.destroy();
                        bunnies.remove();
                    } else {
                        bunny.setTicks(bunny.getTicks() + 1);
                    }
                }
            }
        }, 20, 4L);
    }

    private ItemStack getRandomPrizeMaterial() {
        double chance = Math.random();
        //.1%?
        if (chance >= 0.99) return new ItemStack(Material.MAGMA_CREAM, 1);

        if (chance >= .90) return new ItemStack(Material.EMPTY_MAP, 1);

        if (chance >= .6) return new ItemStack(Material.EMERALD, 1);
        if (chance >= .4) return ItemManager.createOrbofFlight(false);

        if (chance >= .35) return ItemManager.createArmorScrap(5);
        if (chance >= .3) return ItemManager.createArmorScrap(4);
        if (chance >= .25) return ItemManager.createArmorScrap(3);
        if (chance >= .20) return ItemManager.createArmorScrap(2);
        if(chance >= .1)return new ItemStack(Material.BREAD, ThreadLocalRandom.current().nextInt(3) + 1);
        return ItemManager.createArmorScrap(1);
    }

    private ItemStack createRealPrize(ItemStack item) {
        Random random = ThreadLocalRandom.current();

        if (item.getType() == Material.MAGMA_CREAM) return ItemManager.createOrbofAlteration();
        if (item.getType() == Material.EMERALD) return BankMechanics.createGems(random.nextInt(50) + 5);
        if (item.getType() == Material.EMPTY_MAP) {
            //White Scroll
            int isWhiteSCrollChance = random.nextInt(100);
            if (isWhiteSCrollChance <= 25) {
                int chance = random.nextInt(1000);
                int tier = chance >= 999 ? 5 : chance >= 800 ? 4 : chance >= 600 ? 3 : chance >= 300 ? 2 : 1;
                return ItemManager.createProtectScroll(tier);
            } else {
                //Scroll?
                int chance = random.nextInt(100);
                int tier = chance >= 98 ? 5 : chance >= 80 ? 4 : chance >= 60 ? 3 : chance >= 30 ? 2 : 1;
                return ItemManager.createArmorEnchant(tier);
            }
        }
        if (item.getType() == Material.FIREWORK_CHARGE) return ItemManager.createOrbofFlight(false);

        if (ItemManager.isScrap(item)) {
            int amount = 3 + ThreadLocalRandom.current().nextInt(5);
            item.setAmount(amount);
        }
        return item;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        Player player = (Player) event.getEntity().getShooter();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && item.getType() == Material.EGG) {
            //Check if egg..
            NBTWrapper wrapper = new NBTWrapper(item);
            if (wrapper.hasTag("easterEgg")) {
                //BOOM!!!
                event.getEntity().setMetadata("easterEgg", new FixedMetadataValue(DungeonRealms.getInstance(), ""));
                //Spawn zombie or some shit after it launches?
            }

        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        //fuck yea
        easterRabbits.removeIf(rab -> rab.getRoller() != null && rab.getRoller().equals(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity().hasMetadata("easter")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileLand(ProjectileHitEvent event) {
        Projectile entity = event.getEntity();
        if (entity.hasMetadata("easterEgg") && entity.getShooter() instanceof Player) {
            //GUCCIIIIII, HOLLLAAAAA
            entity.remove();

            Player shooter = (Player) entity.getShooter();
            if (!shooter.isOnline()) return;

            //SPAWN?
            ParticleAPI.sendParticleToEntityLocation(ParticleAPI.ParticleEffect.CLOUD, entity, 1F, .5F, 1F, .1F, 35);

            Rabbit rabbit = (Rabbit) entity.getWorld().spawnEntity(entity.getLocation(), EntityType.RABBIT);
            rabbit.setInvulnerable(true);
            rabbit.setAI(false);
            rabbit.setMetadata("easter", new FixedMetadataValue(DungeonRealms.getInstance(), ""));

            Zombie zombie = (Zombie) entity.getWorld().spawnEntity(entity.getLocation(), EntityType.ZOMBIE);
            zombie.setBaby(true);
            zombie.setInvulnerable(true);
            zombie.setAI(false);
            zombie.getEquipment().setHelmet(this.skullItem.clone());
            zombie.setMetadata("easter", new FixedMetadataValue(DungeonRealms.getInstance(), ""));

            Item item = entity.getWorld().dropItem(entity.getLocation(), new ItemStack(Material.MAGMA_CREAM));

            item.setMetadata("no_pickup", new FixedMetadataValue(DungeonRealms.getInstance(), ""));
            item.setPickupDelay(Integer.MAX_VALUE);
            item.setInvulnerable(true);
            item.setGlowing(true);
            item.setMetadata("easter", new FixedMetadataValue(DungeonRealms.getInstance(), ""));

            ArmorStand stand = (ArmorStand) entity.getWorld().spawnEntity(entity.getLocation(), EntityType.ARMOR_STAND);
            stand.setInvulnerable(true);
            stand.setVisible(false);
            stand.setPassenger(item);
            stand.setMetadata("easter", new FixedMetadataValue(DungeonRealms.getInstance(), ""));

            zombie.setPassenger(stand);
            rabbit.setPassenger(zombie);
            EasterBunny bunny = new EasterBunny(rabbit, zombie, stand, item, 0, shooter);
            this.easterRabbits.add(bunny);
            shooter.playSound(shooter.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1, 1.5F);
        }
    }


    @AllArgsConstructor
    class EasterBunny {
        private Entity rabbit;
        private Entity zombie;
        @Getter
        private Entity armorStand;
        @Getter
        private Item item;

        @Getter
        @Setter
        private int ticks;
        @Getter
        private Player roller;

        public void destroy() {
            this.destroy(this.rabbit);
        }

        public ItemStack getPrize() {
            return this.item.getItemStack();
        }

        public void destroy(Entity entity) {
            if (entity.getPassenger() != null) {
                destroy(entity.getPassenger());
            }

            if (entity.isValid())
                entity.remove();

            if (this.item != null && this.item.isValid()) {
                this.item.remove();
            }

            if (this.zombie != null) {
                this.zombie.remove();
            }

        }
    }
}*/
