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
import net.dungeonrealms.game.world.entity.type.monster.type.EnumBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.melee.MeleeWitherSkeleton;
import net.dungeonrealms.game.world.entity.type.monster.boss.Boss;
import net.dungeonrealms.game.world.entity.util.EntityStats;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.minecraft.server.v1_9_R2.*;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Chase on Oct 19, 2015
 */
public class Burick extends MeleeWitherSkeleton implements Boss {

    public Location loc;

    public Burick(World world) {
        super(world);
    }

    public Burick(World world, Location loc) {
        super(world);
        this.loc = loc;
        setArmor(getEnumBoss().tier);
        this.getBukkitEntity().setCustomNameVisible(true);
        int level = 100;
        MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, getEnumBoss().tier, level);
        this.getBukkitEntity().setMetadata("boss", new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().nameid));
        EntityStats.setBossRandomStats(this, level, getEnumBoss().tier);
        this.getBukkitEntity().setCustomName(ChatColor.RED.toString() + "Burick The Fanatic");
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().name));
        for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
            p.sendMessage(ChatColor.RED.toString() + "Burick The Fanatic" + ChatColor.RESET.toString() + ": " + "Ahahaha! You dare try to kill ME?! I am Burick, disciple of Goragath! None of you will leave this place alive!");
        }
        this.setSize(0.7F, 2.4F);
        this.fireProof = true;
        this.setSkeletonType(1);

        collides = true;
    }


    @Override
    public void setArmor(int tier) {
        ItemStack weapon = getWeapon();
        ItemStack boots = ItemGenerator.getNamedItem("up_boots");
        ItemStack legs = ItemGenerator.getNamedItem("up_legs");
        ItemStack chest = ItemGenerator.getNamedItem("up_chest");
        ItemStack head = ItemGenerator.getNamedItem("up_helmet");
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
        return ItemGenerator.getNamedItem("up_axe");
    }

    @Override
    public void onBossDeath() {
        getNearbyBlocks(this.getBukkitEntity().getLocation(), 10).stream().filter(b -> b.getType() == Material.FIRE).forEach(b -> b.setType(Material.AIR));
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
                gp.getPlayerStatistics().setBurickKills(gp.getPlayerStatistics().getBurickKills() + 1);
            }
            p.sendMessage(ChatColor.RED.toString() + "Burick The Fanatic" + ChatColor.RESET.toString() + ": " + "I will have my revenge!");
        }
    }

    private boolean firstHeal = false;
    private boolean isEnraged = false;
    private CopyOnWriteArrayList<Entity> spawnedMobs = new CopyOnWriteArrayList<>();
    private boolean canAddsRespawn = true;
    private boolean hasMessaged = false;

    public void startEnragedMode(LivingEntity en) {
        isEnraged = true;
        for (Player pl : en.getWorld().getPlayers()) {
            pl.sendMessage(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "Burick The Fanatic: " + ChatColor.WHITE
                    + "Pain. Sufferring. Agony. These are the emotions you will be feeling for the rest of eternity!");
            pl.sendMessage(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "Burick The Fanatic " + ChatColor.GOLD + "has become ENRAGED"
                    + ChatColor.GOLD + " 2X DMG, +80% ARMOR, 2x SPEED, KNOCKBACK IMMUNITY!");
            pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERMEN_DEATH, 0.8F, 0.5F);
            pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERMEN_DEATH, 1.2F, 0.2F);
            pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERMEN_DEATH, 0.8F, 1.2F);
        }
        DamageAPI.setDamageBonus(en, 100);
        DamageAPI.setArmorBonus(en, 30);
        this.getAttributeInstance(GenericAttributes.c).setValue(1.00d);
        en.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, true));
    }

    @Override
    public void onBossHit(EntityDamageByEntityEvent event) {
        if (!spawnedMobs.isEmpty()) {
            for (Entity entity : spawnedMobs) {
                if (!entity.isAlive()) {
                    spawnedMobs.remove(entity);
                }
            }
        }
        LivingEntity en = (LivingEntity) event.getEntity();
        if (spawnedMobs.isEmpty()) {
            if (!canAddsRespawn && !hasMessaged) {
                for (Player pl : en.getWorld().getPlayers()) {
                    pl.sendMessage(ChatColor.RED.toString() + "Burick The Fanatic" + ChatColor.RESET.toString() + ": " + "Face me, pathetic creatures!");
                }
                hasMessaged = true;
            }
            if (en.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                en.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
            if (DamageAPI.isInvulnerable(en)) {
                DamageAPI.removeInvulnerable(en);
            }
        }
        int health = HealthHandler.getInstance().getMonsterMaxHPLive(en);
        int hp = HealthHandler.getInstance().getMonsterHPLive(en);
        float tenPercentHP = (float) (health * .10);
        if (hp <= (float) (health * 0.5)) {
            if (canAddsRespawn) {
                spawnWave();
                try {
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SPELL, loc, random.nextFloat(), random.nextFloat(), random.nextFloat(), 1F, 100);
                } catch (Exception err) {
                    err.printStackTrace();
                }
                en.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15));
                DamageAPI.setInvulnerable(en);
                for (Player pl : en.getWorld().getPlayers()) {
                    pl.sendMessage(ChatColor.RED.toString() + "Burick The Fanatic" + ChatColor.RESET.toString() + ": "
                            + "To me, my undead brethren! Rip these Andalucians to pieces!");
                    pl.sendMessage(ChatColor.GRAY + "Burick uses the energy of his minions to create a forcefield around himself -- kill the minions!");
                    pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERMEN_SCREAM, 1F, 0.5F);
                }
                canAddsRespawn = false;
                hasMessaged = false;
            }
        }
        if (hp <= tenPercentHP) {
            if (!firstHeal) {
                HealthHandler.getInstance().healMonsterByAmount(en, HealthHandler.getInstance().getMonsterMaxHPLive(en));
                HealthHandler.getInstance().setMonsterHPLive(en, HealthHandler.getInstance().getMonsterMaxHPLive(en));
                canAddsRespawn = true;
                if (!firstHeal) {
                    firstHeal = true;
                    for (Player pl : en.getWorld().getPlayers()) {
                        pl.sendMessage(ChatColor.RED.toString() + "Burick The Fanatic" + ChatColor.RESET.toString() + ": " + "Let the powers of Maltai channel into me and give me strength!");
                        pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERMEN_DEATH, 1F, 0.5F);
                    }
                }
            } else if (!isEnraged) {
                startEnragedMode(en);
            }
        }
    }

    @Override
    public EnumBoss getEnumBoss() {
        return EnumBoss.Burick;
    }

    private Location toSpawn = new Location(this.getBukkitEntity().getWorld(), -364, 61, -1);

    private void spawnWave() {
        int waveType = random.nextInt(3);
        Location location = new Location(world.getWorld(), toSpawn.getX() + random.nextInt(3), toSpawn.getY(), toSpawn.getZ() + random.nextInt(3));
        switch (waveType) {
            case 0:
                for (int i = 0; i < 4; i++) {
                    Entity entity = SpawningMechanics.getMob(world, 1, EnumMonster.Monk);
                    int level = Utils.getRandomFromTier(3, "high");
                    String newLevelName = ChatColor.AQUA.toString() + "[Lvl. " + level + "] ";
                    EntityStats.createDungeonMob(entity, level, 3);
                    SpawningMechanics.rollElement(entity, EnumMonster.Monk);
                    if (entity == null) {
                        return; //WTF?? UH OH BOYS WE GOT ISSUES
                    }
                    entity.getBukkitEntity().setMetadata("dungeon", new FixedMetadataValue(DungeonRealms.getInstance(), true));
                    entity.setCustomName(newLevelName + GameAPI.getTierColor(3).toString() + ChatColor.BOLD + "Burick's Protector");
                    entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + GameAPI.getTierColor(3).toString() + ChatColor.BOLD + "Burick's Protector"));
                    entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    ((EntityInsentient) entity).persistent = true;
                    ((LivingEntity) entity.getBukkitEntity()).setRemoveWhenFarAway(false);
                    world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                    entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    spawnedMobs.add(entity);
                }
                break;
            case 1:
                for (int i = 0; i <= 4; i++) {
                    Entity entity = SpawningMechanics.getMob(world, 3, EnumMonster.Acolyte);
                    int level = Utils.getRandomFromTier(3, "high");
                    String newLevelName = ChatColor.AQUA.toString() + "[Lvl. " + level + "] ";
                    EntityStats.createDungeonMob(entity, level, 3);
                    SpawningMechanics.rollElement(entity, EnumMonster.Acolyte);
                    if (entity == null) {
                        return; //WTF?? UH OH BOYS WE GOT ISSUES
                    }
                    entity.getBukkitEntity().setMetadata("dungeon", new FixedMetadataValue(DungeonRealms.getInstance(), true));
                    entity.setCustomName(newLevelName + GameAPI.getTierColor(3).toString() + ChatColor.BOLD + "Burick's Acolyte");
                    entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + GameAPI.getTierColor(3).toString() + ChatColor.BOLD + "Burick's Acolyte"));
                    entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    ((EntityInsentient) entity).persistent = true;
                    ((LivingEntity) entity.getBukkitEntity()).setRemoveWhenFarAway(false);
                    world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                    entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    spawnedMobs.add(entity);
                }
                break;
            case 2:
                for (int i = 0; i <= 6; i++) {
                    Entity entity = SpawningMechanics.getMob(world, 2, EnumMonster.Skeleton);
                    int level = Utils.getRandomFromTier(2, "high");
                    String newLevelName = ChatColor.AQUA.toString() + "[Lvl. " + level + "] ";
                    EntityStats.createDungeonMob(entity, level, 2);
                    SpawningMechanics.rollElement(entity, EnumMonster.Skeleton);
                    if (entity == null) {
                        return; //WTF?? UH OH BOYS WE GOT ISSUES
                    }
                    entity.getBukkitEntity().setMetadata("dungeon", new FixedMetadataValue(DungeonRealms.getInstance(), true));
                    entity.setCustomName(newLevelName + GameAPI.getTierColor(2).toString() + ChatColor.BOLD + "Burick's Sacrifice");
                    entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + GameAPI.getTierColor(2).toString() + ChatColor.BOLD + "Burick's Sacrifice"));
                    entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    ((EntityInsentient) entity).persistent = true;
                    ((LivingEntity) entity.getBukkitEntity()).setRemoveWhenFarAway(false);
                    world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                    entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    spawnedMobs.add(entity);
                }
                break;
        }
    }

    private void doBossDrops() {
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        if (random.nextInt(100) < 80) { // 80% chance!
            List<ItemStack> possible_drops = new ArrayList<>();
            for (ItemStack is : livingEntity.getEquipment().getArmorContents()) {
                if (is == null || is.getType() == Material.AIR || is.getTypeId() == 144 || is.getTypeId() == 397) {
                    continue;
                }
                ItemMeta im = is.getItemMeta();
                if (im.hasEnchants()) {
                    for (Map.Entry<Enchantment, Integer> data : im.getEnchants().entrySet()) {
                        is.removeEnchantment(data.getKey());
                    }
                }
                is.removeEnchantment(Enchantment.LOOT_BONUS_MOBS);
                is.removeEnchantment(Enchantment.KNOCKBACK);
                is.removeEnchantment(EnchantmentAPI.getGlowEnchant());
                is.setItemMeta(im);
                possible_drops.add(is);
            }
            ItemStack weapon = livingEntity.getEquipment().getItemInMainHand();
            ItemMeta im = weapon.getItemMeta();
            if (im.hasEnchants()) {
                for (Map.Entry<Enchantment, Integer> data : im.getEnchants().entrySet()) {
                    im.removeEnchant(data.getKey());
                }
            }
            weapon.removeEnchantment(Enchantment.LOOT_BONUS_MOBS);
            weapon.removeEnchantment(Enchantment.KNOCKBACK);
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
        }
        int gemDrop = random.nextInt(2500 - 1000) + 1000;
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
            GameAPI.getGamePlayer(player).addExperience(25000, false, true);
        }
        final String adventurers = partyMembers.substring(0, partyMembers.length() - 2);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            Bukkit.broadcastMessage(ChatColor.GOLD.toString() + ChatColor.BOLD + ">> " + ChatColor.GOLD + "The corrupt Unholy Priest " + ChatColor.UNDERLINE + "Burick The Fanatic" + ChatColor.RESET + ChatColor.GOLD + " has been slain by a group of adventurers!");
            Bukkit.broadcastMessage(ChatColor.GRAY + "Group: " + adventurers);
        }, 60L);
    }

    private List<Block> getNearbyBlocks(Location loc, int maxradius) {
        List<Block> return_list = new ArrayList<>();
        BlockFace[] faces = {BlockFace.UP, BlockFace.NORTH, BlockFace.EAST};
        BlockFace[][] orth = {{BlockFace.NORTH, BlockFace.EAST}, {BlockFace.UP, BlockFace.EAST}, {BlockFace.NORTH, BlockFace.UP}};
        for (int r = 0; r <= maxradius; r++) {
            for (int s = 0; s < 6; s++) {
                BlockFace f = faces[s % 3];
                BlockFace[] o = orth[s % 3];
                if (s >= 3)
                    f = f.getOppositeFace();
                if (!(loc.getBlock().getRelative(f, r) == null)) {
                    Block c = loc.getBlock().getRelative(f, r);

                    for (int x = -r; x <= r; x++) {
                        for (int y = -r; y <= r; y++) {
                            Block a = c.getRelative(o[0], x).getRelative(o[1], y);
                            return_list.add(a);
                        }
                    }
                }
            }
        }
        return return_list;
    }

}
