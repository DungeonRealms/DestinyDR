package net.dungeonrealms.game.world.entity.type.mounts;

import net.minecraft.server.v1_9_R2.EntityHorse;
import net.minecraft.server.v1_9_R2.EnumHorseType;
import net.minecraft.server.v1_9_R2.World;

import org.bukkit.entity.Player;

/**
 * HorseMount - Any type of Horse Mount.
 * 
 * Redone on April 22nd, 2017.
 * @author Kneesnap
 */
public class HorseMount extends EntityHorse {

	public HorseMount(World world) {
		super(world);
	}
	
    public HorseMount(World world, Player player) {
        super(world);
        setTame(true);
        setType(EnumHorseType.HORSE);
        setOwnerUUID(player.getUniqueId());
        setTemper(100);
        this.jumpPower *= 2;
    }
    
    public void setSkin(EnumMountSkins skin) {
    	setType(EnumHorseType.a(skin == null ? 0 : skin.getMeta()));
    	setVariant(skin == null ? 0 : skin.getVariant().ordinal());
    }
}
