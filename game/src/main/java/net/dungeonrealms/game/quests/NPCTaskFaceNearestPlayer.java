package net.dungeonrealms.game.quests;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("FaceNearest")
public class NPCTaskFaceNearestPlayer extends Trait {

	protected NPCTaskFaceNearestPlayer() {
		super("FaceNearest");
	}

	@Override
    public void run() {
		if(!this.getNPC().isSpawned())
			return;
		List<Entity> nearby = this.getNPC().getEntity().getNearbyEntities(5, 5, 5);
		double distance = 99;
		Player toFace = null;
		for(Entity e : nearby){
			double newDis = e.getLocation().distanceSquared(this.getNPC().getEntity().getLocation());
			if(e instanceof Player && newDis < distance && !e.hasMetadata("NPC")){
				distance = newDis;
				toFace = (Player)e;
			}
		}
		if(toFace != null)
			this.getNPC().faceLocation(toFace.getLocation());
    }
}
