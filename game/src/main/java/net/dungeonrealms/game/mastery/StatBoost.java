package net.dungeonrealms.game.mastery;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.game.world.item.Item.AttributeType;

@AllArgsConstructor @Getter
public class StatBoost {
	private AttributeType type;
	private float multiplier;
}