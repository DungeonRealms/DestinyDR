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
import net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss.InfernalGhast;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumDungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.staff.StaffWitherSkeleton;
import net.dungeonrealms.game.world.entity.util.EntityStats;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Chase on Oct 21, 2015
 */
public class InfernalAbyss extends StaffWitherSkeleton implements DungeonBoss {

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
        MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, getEnumBoss().tier, bossLevel);
        this.getBukkitEntity().setMetadata("boss", new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().nameid));
        this.getBukkitEntity().setCustomName(ChatColor.RED.toString() + ChatColor.UNDERLINE + "The Infernal Abyss");
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), ChatColor.RED.toString() + ChatColor.UNDERLINE + "The Infernal Abyss"));
        for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
            p.sendMessage(ChatColor.RED.toString() + "The Infernal Abyss" + ChatColor.RESET.toString() + ": " + "I have nothing to say to you foolish mortals, except for this: Burn.");
        }
        this.setSize(0.7F, 2.4F);
        this.fireProof = true;
        this.setSkeletonType(1);
        this.persistent = true;
        DungeonManager.getInstance().getFireUnderEntity().add(this);
        ghast = new InfernalGhast(this);
        setArmor(4);
        EntityStats.setBossRandomStats(this, bossLevel, 4);
        EntityStats.setBossRandomStats(this.ghast, 100, 4);
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
        ghast.setArmor(new ItemGenerator().setTier(Item.ItemTier.getByTier(tier)).setRarity(Item.ItemRarity.UNIQUE).getArmorSet(),
                new ItemGenerator().setTier(Item.ItemTier.getByTier(tier)).setRarity(Item.ItemRarity.UNIQUE)
                        .setType(Item.ItemType.getRandomWeapon()).generateItem().getItem());
    }

    private ItemStack getWeapon() {
        //TODO: Probably make him a melee boss. As he was always glitching due to being a staff mob previously.
        return ItemGenerator.getNamedItem("infernalstaff");
    }

    @Override
    public EnumDungeonBoss getEnumBoss() {
        return EnumDungeonBoss.InfernalAbyss;
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
        DamageAPI.setDamageBonus(livingEntity, 50);
        for (Player pl : livingEntity.getWorld().getPlayers()) {
            pl.sendMessage(ChatColor.RED.toString() + ChatColor.UNDERLINE + "The Infernal Abyss: " + ChatColor.WHITE
                    + "You... cannot... kill me IN MY OWN DOMAIN, FOOLISH MORTALS!");
            pl.sendMessage(ChatColor.GRAY + "The Infernal Abyss has become enraged! " + ChatColor.UNDERLINE + "+50% DMG!");
            pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERDRAGON_GROWL, 2F, 0.85F);
            pl.playSound(pl.getLocation(), Sound.ENTITY_GHAST_DEATH, 2F, 0.85F);
        }
        Location hit_loc = livingEntity.getLocation();
        for (int i = 0; i < 4; i++) {
            net.minecraft.server.v1_9_R2.Entity entity = SpawningMechanics.getMob(world, 3, EnumMonster.MagmaCube);
            int level = Utils.getRandomFromTier(3, "low");
            String newLevelName = ChatColor.AQUA + "[Lvl. " + level + "] ";
            EntityStats.createDungeonMob(entity, level, 3);
            SpawningMechanics.rollElement(entity, EnumMonster.MagmaCube);
            if (entity == null) {
                return; //WTF?? UH OH BOYS WE GOT ISSUES
            }
            entity.setCustomName(newLevelName + GameAPI.getTierColor(3).toString() + "Demonic Spawn of Inferno");
            entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + GameAPI.getTierColor(3).toString() + "Demonic Spawn of Inferno"));
            Location location = new Location(world.getWorld(), hit_loc.getX() + random.nextInt(3), hit_loc.getY(), hit_loc.getZ() + random.nextInt(3));
            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
            world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
        }
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
    public void onBossAttack(EntityDamageByEntityEvent event) {
        LivingEntity en = (LivingEntity) event.getEntity();
        if (event.getDamager() instanceof Player) {
            Player p_attacker = (Player) event.getDamager();
            if (p_attacker.getLocation().distanceSquared(en.getLocation()) <= 16) {
                pushAwayPlayer(en, p_attacker, 2F);
                p_attacker.setFireTicks(80);
                p_attacker.playSound(p_attacker.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1F, 1F);
            }
        }
        if (hasFiredGhast) {
            if (!this.ghast.isAlive()) {
                if (en.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    en.removePotionEffect(PotionEffectType.INVISIBILITY);
                }
                if (DamageAPI.isInvulnerable(en)) {
                    DamageAPI.removeInvulnerable(en);
                }
            }
        }

        double halfHP = HealthHandler.getInstance().getMonsterMaxHPLive(en) * 0.5;
        if (HealthHandler.getInstance().getMonsterHPLive(en) <= halfHP && !hasFiredGhast) {
            for (Player p : this.getBukkitEntity().getWorld().getPlayers()) {
                p.sendMessage(ChatColor.RED.toString() + ChatColor.UNDERLINE + "The Infernal Abyss" + ChatColor.RESET.toString() + ": " + "Behold, the powers of the inferno.");
            }
            ghast.setLocation(this.locX, this.locY + 7, this.locZ, 1, 1);
            ghast.init(HealthHandler.getInstance().getMonsterHPLive(en));
            this.getWorld().addEntity(ghast, SpawnReason.CUSTOM);
            ghast.init(HealthHandler.getInstance().getMonsterHPLive(en));
            for (Player pl : this.getBukkitEntity().getWorld().getPlayers()) {
                pl.sendMessage(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "The Infernal Abyss: " + ChatColor.WHITE + "The inferno will devour you!");
                pl.sendMessage(ChatColor.GRAY + "The Infernal Abyss has armored up! " + ChatColor.UNDERLINE + "+50% ARMOR!");
                pl.playSound(pl.getLocation(), Sound.ENTITY_GHAST_WARN, 2F, 0.35F);
                pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERDRAGON_GROWL, 2F, 0.85F);
            }
            DamageAPI.setArmorBonus(ghast.getBukkitEntity(), 50);
            hasFiredGhast = true;
            en.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15));
            DamageAPI.setInvulnerable(en);
            DamageAPI.setArmorBonus(en, 50);
        }

        if (hasFiredGhast) {
            if (this.ghast.isAlive()) {
                return;
            }
        }
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
                    for (int i = 0; i < 3; i++) {
                        net.minecraft.server.v1_9_R2.Entity entity = SpawningMechanics.getMob(world, 3, EnumMonster.Silverfish);
                        int level = Utils.getRandomFromTier(3, "low");
                        String newLevelName = ChatColor.AQUA + "[Lvl. " + level + "] ";
                        EntityStats.createDungeonMob(entity, level, 3);
                        SpawningMechanics.rollElement(entity, EnumMonster.Silverfish);
                        if (entity == null) {
                            return; //WTF?? UH OH BOYS WE GOT ISSUES
                        }
                        entity.setCustomName(newLevelName + GameAPI.getTierColor(3).toString() + "Abyssal Demon");
                        entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + GameAPI.getTierColor(3).toString() + "Abyssal Demon"));
                        Location location = new Location(world.getWorld(), hit_loc.getX() + random.nextInt(3), hit_loc.getY(), hit_loc.getZ() + random.nextInt(3));
                        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                        world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    }
                } else {
                    for (int i = 0; i < 3; i++) {
                        net.minecraft.server.v1_9_R2.Entity entity = SpawningMechanics.getMob(world, 3, EnumMonster.MagmaCube);
                        int level = Utils.getRandomFromTier(3, "low");
                        String newLevelName = ChatColor.AQUA + "[Lvl. " + level + "] ";
                        EntityStats.createDungeonMob(entity, level, 3);
                        SpawningMechanics.rollElement(entity, EnumMonster.MagmaCube);
                        if (entity == null) {
                            return; //WTF?? UH OH BOYS WE GOT ISSUES
                        }
                        entity.setCustomName(newLevelName + GameAPI.getTierColor(3).toString() + "Spawn of Inferno");
                        entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + GameAPI.getTierColor(3).toString() + "Spawn of Inferno"));
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
                        String newLevelName = ChatColor.AQUA + "[Lvl. " + level + "] ";
                        EntityStats.createDungeonMob(entity, level, 4);
                        SpawningMechanics.rollElement(entity, EnumMonster.Silverfish);
                        if (entity == null) {
                            return; //WTF?? UH OH BOYS WE GOT ISSUES
                        }
                        entity.setCustomName(newLevelName + GameAPI.getTierColor(4).toString() + "Greater Abyssal Demon");
                        entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + GameAPI.getTierColor(4).toString() + "Greater Abyssal Demon"));
                        Location location = new Location(world.getWorld(), hit_loc.getX() + random.nextInt(3), hit_loc.getY(), hit_loc.getZ() + random.nextInt(3));
                        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                        world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    }
                } else {
                    for (int i = 0; i < 2; i++) {
                        net.minecraft.server.v1_9_R2.Entity entity = SpawningMechanics.getMob(world, 4, EnumMonster.MagmaCube);
                        int level = Utils.getRandomFromTier(4, "low");
                        String newLevelName = ChatColor.AQUA + "[Lvl. " + level + "] ";
                        EntityStats.createDungeonMob(entity, level, 4);
                        SpawningMechanics.rollElement(entity, EnumMonster.MagmaCube);
                        if (entity == null) {
                            return; //WTF?? UH OH BOYS WE GOT ISSUES
                        }
                        entity.setCustomName(newLevelName + GameAPI.getTierColor(4).toString() + "Demonic Spawn of Inferno");
                        entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + GameAPI.getTierColor(4).toString() + "Demonic Spawn of Inferno"));
                        Location location = new Location(world.getWorld(), hit_loc.getX() + random.nextInt(3), hit_loc.getY(), hit_loc.getZ() + random.nextInt(3));
                        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                        world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    }
                }
            }
        }
    }

    private void doBossDrops() {
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        for (Player pl : livingEntity.getWorld().getPlayers()) {
            if (pl.getGameMode() != GameMode.SURVIVAL) {
                continue;
            }
            GamePlayer gp = GameAPI.getGamePlayer(pl);
            if (gp != null) {
                gp.getPlayerStatistics().setInfernalAbyssKills(gp.getPlayerStatistics().getInfernalAbyssKills() + 1);
            }
            pl.sendMessage(ChatColor.RED.toString() + ChatColor.UNDERLINE + "The Infernal Abyss: " + ChatColor.WHITE
                    + "You...have... defeated me...ARGHHHH!!!!!");
            pushAwayPlayer(livingEntity, pl, 6.0F);
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F);
            pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 2F, 2F);
        }
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
            reward = ItemManager.addPartyMemberSoulboundBypass(reward, 60 * 5, livingEntity.getWorld().getPlayers());
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
        int gemDrop = random.nextInt(2000) + 10000;
        int groupSize = 0;
        for (Player player : livingEntity.getWorld().getPlayers()) {
            if (player.getGameMode() != GameMode.SURVIVAL) {
                continue;
            }
            groupSize++;
        }
        int perPlayerDrop = Math.round(gemDrop / groupSize);
        ItemStack banknote = BankMechanics.createBankNote(perPlayerDrop, "Infernal Abyss");
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
            GameAPI.getGamePlayer(player).addExperience(50000, false, true);
        }
        final String adventurers = partyMembers.substring(0, partyMembers.length() - 2);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            Bukkit.broadcastMessage(ChatColor.GOLD.toString() + ChatColor.BOLD + ">> " + ChatColor.GOLD + "The evil fire demon known as " + ChatColor.UNDERLINE + "The Infernal Abyss" + ChatColor.RESET + ChatColor.GOLD + " has been slain by a group of adventurers!");
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
