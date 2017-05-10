package net.dungeonrealms.game.world.entity.type.pet;

import net.dungeonrealms.game.mastery.Utils;
import net.minecraft.server.v1_9_R2.EntitySheep;
import net.minecraft.server.v1_9_R2.EnumColor;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;

import org.bukkit.DyeColor;

/**
 * Created by Rar349 on 5/2/2017.
 */
public class RainbowSheepPet extends EntitySheep {

    public RainbowSheepPet(World world) {
        super(world);
        setAge(0);
        this.ageLocked = true;
        randomColor();
        getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(.45F);
    }

    @Override //On Tick.
    public void n() {
        super.n();

        if (ticksLived % 10 == 0)
            randomColor();
    }
    
    @SuppressWarnings("deprecation")
	private void randomColor() {
    	EnumColor newColor = EnumColor.fromColorIndex(DyeColor.values()[Utils.randInt(0, DyeColor.values().length - 1)].getWoolData());
    	if (newColor == getColor()) { // Make sure we're changing to a new color.
    		randomColor();
    		return;
    	}
    	
    	setColor(newColor);
    }
}
