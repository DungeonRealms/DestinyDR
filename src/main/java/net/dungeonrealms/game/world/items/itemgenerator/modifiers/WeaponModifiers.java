package net.dungeonrealms.game.world.items.itemgenerator.modifiers;

import net.dungeonrealms.game.world.items.Item.ItemRarity;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.itemgenerator.engine.ItemModifier;
import net.dungeonrealms.game.world.items.itemgenerator.engine.ModifierCondition;
import net.dungeonrealms.game.world.items.itemgenerator.engine.ModifierRange;
import net.dungeonrealms.game.world.items.itemgenerator.engine.ModifierType;
import org.bukkit.ChatColor;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WeaponModifiers {

	public static List<ItemType> weapons = Arrays.asList(ItemType.AXE, ItemType.BOW, ItemType.POLEARM, ItemType.STAFF, ItemType.SWORD);
	
	public static List<String> elements = Arrays.asList("FIRE", "ICE", "POISON");
	public static List<Double> elementMultipliers = Arrays.asList(0.15D, 1D, 0.10D);

	public static List<String> versus = Arrays.asList("Monsters", "Players");
	
	public static ChatColor r = ChatColor.RED;

	public class SwordDamage extends ItemModifier {

		public SwordDamage() {
			super(Collections.singletonList(ItemType.SWORD), 100, "damage", r + "DMG: ", null, false);
			
			setOrderPriority(0);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 3, 7)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 8, 10, 14)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 15, 17, 21)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 22, 24, 28)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 30, 34, 40)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 41, 45, 51)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 52, 56, 62)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 63, 67, 73)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 90, 98, 110)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 111, 119, 131)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 132, 140, 152)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 153, 161, 173)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 210, 222, 242)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 243, 255, 275)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 276, 288, 308)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 309, 321, 341)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 400, 416, 456)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 457, 473, 513)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 514, 530, 570)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 571, 587, 627)));
		}
		
	}
	
	public class AxeDamage extends ItemModifier {

        public AxeDamage() {
            super(Collections.singletonList(ItemType.AXE), 100, "damage", r + "DMG: ", null, false);
            
            setOrderPriority(0);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 4, 10)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 11, 14, 20)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 21, 24, 30)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 31, 34, 40)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 40, 46, 56)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 57, 63, 73)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 74, 80, 90)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 91, 97, 107)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 115, 124, 144)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 145, 154, 174)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 175, 184, 204)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 205, 214, 234)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 240, 252, 292)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 293, 305, 345)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 346, 358, 398)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 399, 411, 451)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 470, 486, 536)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 537, 553, 603)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 604, 620, 670)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 671, 687, 737)));
        }
        
    }
	
	public class StaffDamage extends ItemModifier {

        public StaffDamage() {
            super(Collections.singletonList(ItemType.STAFF), 100, "damage", r + "DMG: ", null, false);
            
            setOrderPriority(0);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 2, 5)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 6, 7, 10)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 11, 12, 15)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 16, 17, 20)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 25, 27, 33)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 34, 36, 42)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 43, 45, 51)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 52, 54, 60)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 80, 84, 96)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 97, 99, 105)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 106, 108, 114)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 115, 117, 123)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 140, 148, 172)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 173, 181, 205)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 206, 214, 238)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 239, 247, 271)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 300, 316, 346)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 347, 363, 393)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 394, 410, 440)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 441, 457, 487)));
        }
        
	}

	public class PolearmDamage extends ItemModifier {

	    public PolearmDamage() {
	        super(Collections.singletonList(ItemType.POLEARM), 100, "damage", r + "DMG: ", null, false);

	        setOrderPriority(0);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 3, 7)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 8, 10, 14)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 15, 17, 21)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 22, 24, 28)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 35, 39, 46)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 47, 51, 58)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 59, 63, 70)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 71, 75, 82)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 95, 101, 114)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 115, 121, 134)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 135, 141, 154)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 155, 161, 174)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 190, 199, 224)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 225, 234, 259)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 260, 269, 294)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 295, 304, 329)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 350, 365, 400)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 401, 416, 451)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 452, 467, 502)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 503, 518, 553)));
	    }

	}

	public class BowDamage extends ItemModifier {

	    public BowDamage() {
	        super(Collections.singletonList(ItemType.BOW), 100, "damage", r + "DMG: ", null, false);

			setOrderPriority(0);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 3, 7)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 8, 10, 14)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 15, 17, 21)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 22, 24, 28)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 35, 39, 46)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 47, 51, 58)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 59, 63, 70)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 71, 75, 82)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 95, 101, 114)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 115, 121, 134)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 135, 141, 154)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 155, 161, 174)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 190, 199, 224)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 225, 234, 259)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 260, 269, 294)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 295, 304, 329)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 350, 365, 400)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 401, 416, 451)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 452, 467, 502)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 503, 518, 553)));
	    }

	}

	public class StrDexVitInt extends ItemModifier {

		public StrDexVitInt() {
			super(weapons, -1, null, null, null);
			setOrderPriority(11);
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 15), 25));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 35), 20).setBonus(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 35), 5).setReplacement(StrDexVitInt.class)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 75), 15).setBonus(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 75), 5).setReplacement(StrDexVitInt.class).setBonus(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 75), 1).setReplacement(StrDexVitInt.class))));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 115), 15).setBonus(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 115), 9).setReplacement(StrDexVitInt.class).setBonus(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 115), 4).setReplacement(StrDexVitInt.class))));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 315), 20).setBonus(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 315), 10).setReplacement(StrDexVitInt.class).setBonus(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 315), 5).setReplacement(StrDexVitInt.class))));
		}
		
		@Override
        public void chooseStat() {
            List<String> allStats = Arrays.asList("strength", "vitality", "intellect", "dexterity");
            chosenStat = allStats.get(new Random().nextInt(allStats.size()));
        }
        
        @Override
        public String getPrefix(ItemMeta meta){
            if (chosenStat == null || chosenStat.equals("")) {
                chooseStat();
            }
            return r + chosenStat.substring(0, 3).toUpperCase() + ": +";
        }
        
        @Override
        public String getNBTName() {
            if (chosenStat == null || chosenStat.equals("")) {
                chooseStat();
            }
            return chosenStat;
        }
	}

	
	public class Critical extends ItemModifier {

		public Critical() {
			super(weapons, -1, "criticalHit", r + "CRITICAL HIT: ", "%");
			setOrderPriority(4);
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 2), 2));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 4), 5));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 5), 8));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 6), 9));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 10), 7));
		}
		
	}
	
	public class LifeSteal extends ItemModifier {

		public LifeSteal() {
			super(weapons, -1, "lifesteal", r + "LIFE STEAL: ", "%");
			setOrderPriority(2);
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 30), 2));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 15), 4));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 12), 5));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 7), 10));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 8), 8));
		}
		
	}
	
	public class Knockback extends ItemModifier {

		public Knockback() {
			super(Arrays.asList(ItemType.AXE, ItemType.POLEARM, ItemType.STAFF, ItemType.SWORD), -1, "knockback", r + "KNOCKBACK: ", "%");
			setOrderPriority(7);
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 3), 3));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 6), 10));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 12), 13));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 15), 16));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 20), 20));
		}
		
	}
	
	public class Blind extends ItemModifier {

		public Blind() {
			super(weapons, -1, "blind", r + "BLIND: ", "%");
			setOrderPriority(8);
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 5), 3));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 7), 5));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 9), 8));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 9), 9));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 11), 11));
		}
		
	}
	
	public class Slow extends ItemModifier {

		public Slow() {
			super(Collections.singletonList(ItemType.BOW), -1, "slow", r + "SLOW: ", "%");
			setOrderPriority(6);
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 3), 3));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 4), 10));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 5), 13));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 7), 16));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 10), 20));
		}
		
	}

	public class Elemental extends ItemModifier {

		public Elemental() {
			super(Arrays.asList(ItemType.AXE, ItemType.POLEARM, ItemType.STAFF, ItemType.SWORD), -1, null, null, null);
			setOrderPriority(9);
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 4), 6));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 9), 9));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 15), 10));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 25), 15));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 55), 20));
		}
		
		@Override
        public void chooseStat() {
            chosenStat = elements.get(new Random().nextInt(elements.size()));
        }
        
        @Override
        public String getPrefix(ItemMeta meta){
            if (chosenStat == null || chosenStat.equals("")) chooseStat();
            return r + chosenStat + " DMG: +";
        }
        
        @Override
        public String getNBTName() {
            return chosenStat.toLowerCase() + "Damage";
        }
	}

	public class ElementalBow extends ItemModifier {

		public ElementalBow() {
			super(Collections.singletonList(ItemType.BOW), -1, null, null, null);
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 8), 6));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 15), 9));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 25), 10));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 45), 15));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 75), 20));
		}
		
		@Override
        public void chooseStat() {
            chosenStat = elements.get(new Random().nextInt(elements.size()));
        }
        
        @Override
        public String getPrefix(ItemMeta meta){
            if (chosenStat == null || chosenStat.equals("")) chooseStat();
            return r + chosenStat + " DMG: +";
        }
        
        @Override
        public String getNBTName() {
            return chosenStat.toLowerCase() + "Damage";
        }
	}
	
	public class Versus extends ItemModifier {

		public Versus() {
			super(weapons, -1, null, null, "% DMG");
			setOrderPriority(1);
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 10), 6));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 12), 9));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 15), 10));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 20), 12));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 15), 15));
		}
		
		@Override
		public void chooseStat() {
		    chosenStat = versus.get(new Random().nextInt(versus.size()));
		}
		
		@Override
		public String getPrefix(ItemMeta meta){
			if (chosenStat == null || chosenStat.equals("")) {
			    chooseStat();
			}
		    return r + "vs. " + chosenStat.toUpperCase() + ": +";
		}
		
		@Override
		public String getNBTName() {
		    return "vs" + chosenStat;
		}
	}
	
	public class Pure extends ItemModifier {

		public Pure() {
			super(Collections.singletonList(ItemType.AXE), -1, "pureDamage", r + "PURE DMG: +", null);
			setOrderPriority(10);
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 5), 6));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 8), 9));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 15), 5));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 25), 5));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 45), 10));
		}
		
	}
	
	public class Accuracy extends ItemModifier {

		public Accuracy() {
			super(Collections.singletonList(ItemType.SWORD), -1, "accuracy", r + "ACCURACY: ", "%");
			setOrderPriority(3);
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 10), 8));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 12), 12));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 25), 15));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 28), 20));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 35), 15));
		}
		
	}
	
	public class ArmorPenetration extends ItemModifier {

		public ArmorPenetration() {
			super(Collections.singletonList(ItemType.AXE), -1, "armorPenetration", r + "ARMOR PENETRATION: ", "%");
			setOrderPriority(5);
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 1), 20));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 3), 20));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 5), 25));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 8), 20));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 10), 15));
		}
		
	}
	
}
