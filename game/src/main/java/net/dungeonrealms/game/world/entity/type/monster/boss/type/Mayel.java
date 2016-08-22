package net.dungeonrealms.game.world.entity.type.monster.boss.type;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.enchantments.EnchantmentAPI;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.RangedWitherSkeleton;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.boss.Boss;
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
public class Mayel extends RangedWitherSkeleton implements Boss {

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
        setArmor(getEnumBoss().tier);
        this.getBukkitEntity().setCustomNameVisible(true);
        int level = 100;
        MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, getEnumBoss().tier, level);
        this.getBukkitEntity().setMetadata("boss", new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().nameid));
        EntityStats.setBossRandomStats(this, level, getEnumBoss().tier);
        this.getBukkitEntity().setCustomName(ChatColor.RED.toString() + "Mayel The Cruel");
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().name));
        for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
            p.sendMessage(ChatColor.RED.toString() + "Mayel The Cruel" + ChatColor.RESET.toString() + ": " + getEnumBoss().greeting);
        }
        this.setSize(0.7F, 2.4F);
        this.fireProof = true;
        this.setSkeletonType(1);
    }

    @Override
    public void setArmor(int tier) {
        // weapon, boots, legs, chest, helmet/head
        ItemStack weapon = getWeapon();
        ItemStack boots = ItemGenerator.getNamedItem("mayelboot");
        ItemStack legs = ItemGenerator.getNamedItem("mayelpants");
        ItemStack chest = ItemGenerator.getNamedItem("mayelchest");
        ItemStack head = ItemGenerator.getNamedItem("mayelhelmet");
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

    /**
     * @return
     */
    private ItemStack getWeapon() {
        return ItemGenerator.getNamedItem("mayelbow");
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
        try {
            ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.FIREWORKS_SPARK, this.getBukkitEntity().getLocation().add(0, 2, 0), random.nextFloat(), random.nextFloat(), random.nextFloat(), 0.2F, 200);
        } catch (Exception err) {
            err.printStackTrace();
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), this::doBossDrops, 5L);
        for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
            if (p.getGameMode() != GameMode.SURVIVAL) {
                continue;
            }
            GamePlayer gp = GameAPI.getGamePlayer(p);
            if (gp != null) {
                gp.getPlayerStatistics().setMayelKills(gp.getPlayerStatistics().getMayelKills() + 1);
            }
            p.sendMessage(ChatColor.RED.toString() + "Mayel The Cruel" + ChatColor.RESET.toString() + ": " + "No... how could it be?");
        }
    }

    private boolean canSpawn = false;

    @Override
    public void onBossHit(EntityDamageByEntityEvent event) {
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
            for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
                p.sendMessage(ChatColor.RED.toString() + "Mayel The Cruel" + ChatColor.RESET.toString() + ": " + "Come to my call, brothers!");
            }
        }
    }


    private void doBossDrops() {
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        List<ItemStack> possible_drops = new ArrayList<>();
        for (ItemStack is : livingEntity.getEquipment().getArmorContents()) {
            if (is == null || is.getType() == org.bukkit.Material.AIR || is.getTypeId() == 144 || is.getTypeId() == 397) {
                continue;
            }
            ItemMeta im = is.getItemMeta();
            if (im.hasEnchants()) {
                for (Map.Entry<org.bukkit.enchantments.Enchantment, Integer> data : im.getEnchants().entrySet()) {
                    is.removeEnchantment(data.getKey());
                }
            }
            is.removeEnchantment(org.bukkit.enchantments.Enchantment.LOOT_BONUS_MOBS);
            is.removeEnchantment(org.bukkit.enchantments.Enchantment.KNOCKBACK);
            is.removeEnchantment(EnchantmentAPI.getGlowEnchant());
            is.setItemMeta(im);
            possible_drops.add(is);
        }
        ItemStack weapon = livingEntity.getEquipment().getItemInMainHand();
        ItemMeta im = weapon.getItemMeta();
        if (im.hasEnchants()) {
            for (Map.Entry<org.bukkit.enchantments.Enchantment, Integer> data : im.getEnchants().entrySet()) {
                im.removeEnchant(data.getKey());
            }
        }
        weapon.removeEnchantment(org.bukkit.enchantments.Enchantment.LOOT_BONUS_MOBS);
        weapon.removeEnchantment(org.bukkit.enchantments.Enchantment.KNOCKBACK);
        weapon.removeEnchantment(EnchantmentAPI.getGlowEnchant());
        weapon.setItemMeta(im);
        possible_drops.add(weapon);

        ItemStack reward = ItemManager.makeSoulBound(possible_drops.get(random.nextInt(possible_drops.size())));
        livingEntity.getWorld().dropItem(livingEntity.getLocation(), reward);
        List<String> hoveredChat = new ArrayList<>();
        ItemMeta meta = reward.getItemMeta();
        hoveredChat.add((meta.hasDisplayName() ? meta.getDisplayName() : reward.getType().name()));
        if (meta.hasLore()) {
            hoveredChat.addAll(meta.getLore());
        }
        final JSONMessage normal = new JSONMessage(ChatColor.DARK_PURPLE + "The boss has dropped: ", ChatColor.DARK_PURPLE);
        normal.addHoverText(hoveredChat, ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "SHOW");
        livingEntity.getWorld().getPlayers().forEach(normal::sendToPlayer);

        int gemDrop = random.nextInt(250 - 100) + 100;
        int groupSize = 0;
        for (Player player : livingEntity.getWorld().getPlayers()) {
            if (player.getGameMode() != GameMode.SURVIVAL) {
                continue;
            }
            groupSize++;
        }
        int perPlayerDrop = Math.round(gemDrop / groupSize);
        ItemStack banknote = BankMechanics.createBankNote(perPlayerDrop);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            for (Player player : livingEntity.getWorld().getPlayers()) {
                player.sendMessage(ChatColor.DARK_PURPLE + "The boss has dropped " + ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + gemDrop + ChatColor.DARK_PURPLE + " gems.");
                player.sendMessage(ChatColor.DARK_PURPLE + "Each player receives " + ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + perPlayerDrop + ChatColor.DARK_PURPLE + " gems!");
            }
        }, 5L);
        String partyMembers = "";
        for (Player player : livingEntity.getWorld().getPlayers()) {
            if (player.getGameMode() != GameMode.SURVIVAL) {
                continue;
            }
            partyMembers += player.getName() + ", ";
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItem(player.getLocation(), banknote);
                player.sendMessage(ChatColor.RED + "Because you had no room in your inventory, your new bank note has been placed at your character's feet.");
            } else {
                player.getInventory().addItem(banknote);
            }
            GameAPI.getGamePlayer(player).addExperience(5000, false, true);
        }
        final String adventurers = partyMembers.substring(0, partyMembers.length() - 2);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            Bukkit.broadcastMessage(ChatColor.GOLD.toString() + ChatColor.BOLD + ">> " + ChatColor.GOLD + "The cunning bandit lord " + ChatColor.UNDERLINE + "Mayel The Cruel" + ChatColor.RESET + ChatColor.GOLD + " has been slain by a group of adventurers!");
            Bukkit.broadcastMessage(ChatColor.GRAY + "Group: " + adventurers);
        }, 60L);
    }


    private boolean canSpawnMobs(LivingEntity livingEntity) {
        int maxHP = HealthHandler.getInstance().getMonsterMaxHPLive(livingEntity);
        int currentHP = HealthHandler.getInstance().getMonsterHPLive(livingEntity);
        if (currentHP <= (maxHP * 0.9)) {
            if (!canSpawn) {
                canSpawn = true;
                return true;
            }
        } else if (currentHP <= (maxHP * 0.8)) {
            if (!canSpawn) {
                canSpawn = true;
                return true;
            }
        } else if (currentHP <= (maxHP * 0.8)) {
            if (!canSpawn) {
                canSpawn = true;
                return true;
            }
        } else if (currentHP <= (maxHP * 0.7)) {
            if (!canSpawn) {
                canSpawn = true;
                return true;
            }
        } else if (currentHP <= (maxHP * 0.6)) {
            if (!canSpawn) {
                canSpawn = true;
                return true;
            }
        } else if (currentHP <= (maxHP * 0.5)) {
            if (!canSpawn) {
                canSpawn = true;
                return true;
            }
        } else if (currentHP <= (maxHP * 0.4)) {
            if (!canSpawn) {
                canSpawn = true;
                return true;
            }
        } else if (currentHP < (maxHP * 0.3)) {
            if (!canSpawn) {
                canSpawn = true;
                return true;
            }
        } else if (currentHP <= (maxHP * 0.2)) {
            if (!canSpawn) {
                canSpawn = true;
                return true;
            }
        } else if (currentHP <= (maxHP * 0.1)) {
            if (!canSpawn) {
                canSpawn = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public EnumBoss getEnumBoss() {
        return EnumBoss.Mayel;
    }
}
