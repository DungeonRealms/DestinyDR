package net.dungeonrealms.game.world.entity.type.pet;

import org.bukkit.entity.Player;

/**
 * Represents an entity (Pet) that needs to know its owner.
 * 
 * Created May 5th, 2017.
 * @author Kneesnap
 */
public interface Ownable {
	public void setOwner(Player player);
}
