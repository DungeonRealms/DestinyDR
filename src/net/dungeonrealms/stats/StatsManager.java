package net.dungeonrealms.stats;

import java.util.UUID;

import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;

/**
 * Created by Chase on Nov 1, 2015
 */
public class StatsManager {

	public class PlayerStats {
		private int freePoints;
		private int strPoints;
		private int tempstrPoints;
		private int dexPoints;
		private int tempdexPoints;
		private int vitPoints;
		private int tempvitPoints;
		private int intPoints;
		private int tempintPoints;
		private int tempFreePoints;
		private int level;
		private UUID playerUUID;
		public final static int POINTS_PER_LEVEL = 6;

		public PlayerStats(UUID playerUUID) {
			this.playerUUID = playerUUID;
			this.freePoints = 0;
			this.tempFreePoints = 0;
			this.strPoints = 0;
			this.dexPoints = 0;
			this.vitPoints = 0;
			this.intPoints = 0;
			this.level = 1;
			this.tempstrPoints = 0;
			this.tempdexPoints = 0;
			this.tempvitPoints = 0;
			this.tempintPoints = 0;
			loadPlayerStats();
		}

		/**
		 * gets stat points from the database for UUID
		 * 
		 * @since 1.0;
		 */
		private void loadPlayerStats() {
			this.freePoints = (int) DatabaseAPI.getInstance().getData(EnumData.BUFFER_POINTS, playerUUID);
			this.intPoints = (int) DatabaseAPI.getInstance().getData(EnumData.INTELLECT, playerUUID);
			this.dexPoints = (int) DatabaseAPI.getInstance().getData(EnumData.DEXTERITY, playerUUID);
			this.strPoints = (int) DatabaseAPI.getInstance().getData(EnumData.STRENGTH, playerUUID);
			this.vitPoints = (int) DatabaseAPI.getInstance().getData(EnumData.VITALITY, playerUUID);
			this.level = (int) DatabaseAPI.getInstance().getData(EnumData.LEVEL, playerUUID);
		}

		private void lvlUp() {
			this.freePoints += POINTS_PER_LEVEL * level;
			this.level += 1;
		}
		
		private void onLogOff(){
			DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$SET, EnumData.INTELLECT, intPoints, false);
		}
	}

}
