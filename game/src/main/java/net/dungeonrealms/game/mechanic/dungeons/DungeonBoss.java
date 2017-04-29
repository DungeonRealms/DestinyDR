package net.dungeonrealms.game.mechanic.dungeons;

import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.base.DRWitherSkeleton;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Redone in April 2017.
 * @author Kneesnap
 */
public interface DungeonBoss extends DRMonster {
	
	BossType getBossType();
	
	public int getGemDrop();
	
	public int getXPDrop();
	
	public String[] getItems();
	
	public void addKillStat(GamePlayer gp);
	
	default void onBossDeath(Player player) {
		
	}
	
	default void onBossAttack(EntityDamageByEntityEvent event) {
		
	}
	
	//  OVERRIDDEN STUFF  //
	@Override
	default void setupMonster(int tier) {
		setupNMS();
	}
	
	@Override
	default void onMonsterAttack(Player p) {}
	
	@Override
	default void onMonsterDeath(Player killer) {
		say(getBossType().getDeathMessage());
		onBossDeath(killer);
		
		if (!getBossType().isFinalBoss())
			return;
		
		//Remove Nearby Fire
		getNearbyBlocks(getBukkit().getLocation(), 10).stream().filter(bk -> bk.getType() == Material.FIRE).forEach(bk -> bk.setType(Material.AIR));
		
		Random random = new Random();
		ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.FIREWORKS_SPARK, getBukkit().getLocation().add(0, 2, 0), random.nextFloat(), random.nextFloat(), random.nextFloat(), 0.2F, 200);
		
		getDungeon().completeDungeon();
	}
	
	@Override
	default ItemStack getWeapon(){
		return ItemGenerator.getNamedItem(getItems()[0]);
	}
	
	@Override
	default int getTier(){
		return getBossType().getType().getTier();
	}
	
	default Entity spawnMinion(EnumMonster monsterType, String mobName, int tier) {
		return spawnMinion(monsterType, mobName, tier, true);
	}
	
	default Entity spawnMinion(EnumMonster monsterType, String mobName, int tier, boolean highPower) {
		Location loc = getBukkit().getLocation().clone().add(Utils.randInt(0, 6) - 3, 0, Utils.randInt(0, 6) - 3);
		LivingEntity le = (LivingEntity) EntityAPI.spawnCustomMonster(loc, monsterType, Utils.getRandomFromTier(tier, highPower ? "high" : "low"), tier, null);
		le.setRemoveWhenFarAway(false);
		return le;
	}
	
	default Dungeon getDungeon() {
		return DungeonManager.getDungeon(getBukkit().getWorld());
	}
	
	default void setArmor(){
		// Set armor.
		EntityEquipment e = getBukkit().getEquipment();
		ItemStack[] armor = e.getArmorContents();
		for (int i = 1; i < getItems().length; i++)
			armor[i - 1] = ItemGenerator.getNamedItem(getItems()[i]);
		
		e.setArmorContents(armor);
		e.setItemInMainHand(getWeapon());
	}
	
	default void say(String msg) {
		getDungeon().announce(ChatColor.RED + getBossType().getName() + "> " + ChatColor.RESET + msg);
	}	
	
	default void createEntity(int level){
		if(getItems() != null)
			setArmor();
		if(this instanceof DRWitherSkeleton){
			DRWitherSkeleton monster = (DRWitherSkeleton)this;
			monster.setSkeletonType(1);
			monster.setSize(0.7F, 2.4F);
		}
		getBukkit().setRemoveWhenFarAway(false);
		EntityAPI.registerBoss(this, 100, getTier());
        say(getBossType().getGreeting());
	}
    
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
}
