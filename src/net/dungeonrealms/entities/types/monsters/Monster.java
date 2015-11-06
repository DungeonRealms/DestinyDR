package net.dungeonrealms.entities.types.monsters;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.banks.BankMechanics;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 21, 2015
 */
public interface Monster {

	public void onMonsterAttack(Player p);

	public void onMonsterDeath();

	public EnumMonster getEnum();
	

	public default void checkItemDrop(int tier, EnumMonster monter, Location loc){
		World world = ((CraftWorld) loc.getWorld()).getHandle();
    	if(world.random.nextInt(100) < 33){
            ItemStack item = BankMechanics.gem.clone();
            item.setAmount(new Random().nextInt(5));
            world.getWorld().dropItemNaturally(loc.add(0, 1, 0), item);
        }
	}}
