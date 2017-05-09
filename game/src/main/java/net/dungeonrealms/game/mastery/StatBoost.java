package net.dungeonrealms.game.mastery;

import lombok.Getter;
import net.dungeonrealms.game.world.item.Item.AttributeType;

public class StatBoost {
		
		@Getter
		private AttributeType type;
		
		@Getter
		private float multiplier;
		
		public StatBoost(AttributeType type, float multiplier) {
			this.type = type;
			this.multiplier = multiplier;
		}
	}