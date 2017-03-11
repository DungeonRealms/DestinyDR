package net.dungeonrealms.game.world.entity.type.monster.boss;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.DungeonManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.title.TitleAPI;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.base.DRWitherSkeleton;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumDungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.util.EntityStats;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.minecraft.server.v1_9_R2.EntityInsentient;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Created by Chase on Oct 18, 2015
 */

public interface DungeonBoss extends Boss {

    EnumDungeonBoss getEnumBoss();
    
    public int getGemDrop();
    
    public int getXPDrop();
    
    public Entity getBukkitEntity();
    
    public String[] getItems();
    
    public void addKillStat(GamePlayer gp);
    
    default net.minecraft.server.v1_9_R2.Entity spawnMinion(EnumMonster monsterType, String mobName, int tier) {
    	return spawnMinion(monsterType, mobName, tier, true);
    }
    
    default net.minecraft.server.v1_9_R2.Entity spawnMinion(EnumMonster monsterType, String mobName, int tier, boolean highPower) {
    	Random random = new Random();
    	
    	net.minecraft.server.v1_9_R2.World world = ((CraftWorld)getBukkitEntity().getWorld()).getHandle();
    	net.minecraft.server.v1_9_R2.Entity entity = SpawningMechanics.getMob(world, tier, monsterType);
        int level = Utils.getRandomFromTier(tier, highPower ? "high" : "low");
        String newLevelName = ChatColor.AQUA + "[Lvl. " + level + "] ";
        EntityStats.createDungeonMob(entity, level, tier);
        SpawningMechanics.rollElement(entity, monsterType);
        if (entity == null)
            return null;
        entity.getBukkitEntity().setMetadata("dungeon", new FixedMetadataValue(DungeonRealms.getInstance(), true));
        String displayName = newLevelName + GameAPI.getTierColor(tier).toString() + ChatColor.BOLD + mobName;
        entity.setCustomName(displayName);
        entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), displayName));
        Location location = new Location(world.getWorld(), getBukkitEntity().getLocation().getX() + random.nextInt(3), getBukkitEntity().getLocation().getY(), getBukkitEntity().getLocation().getZ() + random.nextInt(3));
        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
        ((EntityInsentient) entity).persistent = true;
        ((LivingEntity) entity.getBukkitEntity()).setRemoveWhenFarAway(false);
        world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
        return entity;
    }
    
    default ItemStack getWeapon(){
    	return ItemGenerator.getNamedItem(getItems()[0]);
    }
    
    default int getTier(){
    	return getEnumBoss().getDungeonType().getTier();
    }
    
    default void setArmor(){
    	ItemStack weapon = getWeapon();
    	ItemStack head = ItemGenerator.getNamedItem(getItems()[1]);
    	ItemStack chest = ItemGenerator.getNamedItem(getItems()[2]);
    	ItemStack legs = ItemGenerator.getNamedItem(getItems()[3]);
        ItemStack boots = ItemGenerator.getNamedItem(getItems()[4]);
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        livingEntity.getEquipment().setItemInMainHand(weapon);
        livingEntity.getEquipment().setBoots(boots);
        livingEntity.getEquipment().setLeggings(legs);
        livingEntity.getEquipment().setChestplate(chest);
        livingEntity.getEquipment().setHelmet(head);
    }

    default void say(String msg) {
        for (Player p : getBukkitEntity().getWorld().getPlayers())
            p.sendMessage(ChatColor.RED + this.getEnumBoss().getName() + ": " + ChatColor.WHITE + msg);
    }
    
    default void createEntity(int level){
    	if(getItems() != null)
    		setArmor();
    	if(this instanceof DRWitherSkeleton){
    		DRWitherSkeleton monster = (DRWitherSkeleton)this;
    		monster.setSkeletonType(1);
    		monster.setSize(0.7F, 2.4F);
    	}
    	getBukkitEntity().setCustomNameVisible(true);
    	MetadataUtils.registerEntityMetadata((net.minecraft.server.v1_9_R2.Entity)this, EnumEntityType.HOSTILE_MOB, getTier(), level);
        this.getBukkitEntity().setMetadata("boss", new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().getNameID()));
        EntityStats.setBossRandomStats((net.minecraft.server.v1_9_R2.Entity)this, level, getTier());
        this.getBukkitEntity().setCustomName(ChatColor.RED + getEnumBoss().getName());
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), ChatColor.RED + getEnumBoss().getName()));
        say(getEnumBoss().getGreeting());
    }

    boolean enabled = true;
    boolean debug = false;
    
    default List<Block> getNearbyBlocks(Location loc, int maxradius) {
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

    default void dropMount(Entity entity, DungeonManager.DungeonType dungeonType) {
        if (!enabled) return;
        Random random = ThreadLocalRandom.current();
        if(random.nextInt(1000) < dungeonType.getMount().getChance() || debug){
            ItemStack mountItem = dungeonType.getMount().getMountData().createMountItem(dungeonType.getMount());
            if (mountItem != null) {
                //GIVE IT TO SOMEONE!!!

                List<Player> partyMembers = entity.getWorld().getPlayers().stream().filter((pl) -> pl.getGameMode() == GameMode.SURVIVAL).collect(Collectors.toList());

                if (partyMembers != null && !partyMembers.isEmpty()) {
                    Collections.shuffle(partyMembers);

                    Player winner = null;
                    for (Player win : partyMembers) {
                        //Its shuffled so first player can just be the winner if they have space.
                        //kys.
                        if (win.getInventory().firstEmpty() == -1) {
                            Bukkit.getLogger().info("Not giving mount to " + win.getName() + " due to full inventory.");
                            continue;
                        }

                        winner = win;
                        break;
                    }

                    if (winner != null) {
                        winner.playSound(winner.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
                        winner.getInventory().addItem(mountItem);

                        winner.getWorld().playSound(winner.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, .3F);

                        ParticleAPI.sendParticleToEntityLocation(ParticleAPI.ParticleEffect.HAPPY_VILLAGER, winner, 1, 1, 1, 0.03F, 50);

                        Bukkit.broadcastMessage(ChatColor.GOLD.toString() + ChatColor.BOLD + "** " + winner.getName() + ChatColor.GOLD + " has received a " +
                                mountItem.getItemMeta().getDisplayName() + ChatColor.GOLD + " from the " + dungeonType.getDungeonName() +
                                ChatColor.GOLD + " Dungeon as a rare drop! " + ChatColor.GOLD + ChatColor.BOLD + "**");
                        TitleAPI.sendActionBar(winner, ChatColor.GREEN.toString() + ChatColor.BOLD + "You have received a " +
                                mountItem.getItemMeta().getDisplayName() + ChatColor.GREEN + "!", 20 * 5);

                    }
                }
            }
        }
    }
}
