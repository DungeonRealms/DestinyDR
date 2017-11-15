package net.dungeonrealms.game.world.item.itemgenerator.modifiers;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.item.Item.WeaponAttributeType;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ItemModifier;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierCondition;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierRange;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierType;

public class WeaponModifiers {

	private static ItemType[] SEMI_MELEE = new ItemType[] {ItemType.AXE, ItemType.POLEARM, ItemType.STAFF, ItemType.SWORD};
	
	private static WeaponAttributeType[] ELEMENTS = new WeaponAttributeType[] {WeaponAttributeType.FIRE_DAMAGE, WeaponAttributeType.ICE_DAMAGE, WeaponAttributeType.POISON_DAMAGE};

	public class SwordDamage extends ItemModifier {

		public SwordDamage() {
			super(WeaponAttributeType.DAMAGE, ItemType.SWORD);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 1, 2, 4, 6)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 4, 6, 8, 12)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 8, 13, 14, 18)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 12, 14, 20, 24)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 8, 10, 14, 18)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 15, 21, 28, 35)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 22, 29, 40, 50)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 30, 35, 55, 65)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 22, 29, 40, 50)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 35, 40, 60, 70)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 60, 70, 80, 100)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 70, 80, 120, 140)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 60, 70, 80, 100)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 90, 115, 120, 160)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 130, 150, 180, 200)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 160, 170, 210, 235)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 130, 150, 180, 200)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 180, 210, 230, 290)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 250, 290, 330, 370)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 300, 350, 400, 450)));
		}

	}

	public class AxeDamage extends ItemModifier {

		public AxeDamage() {
			super(WeaponAttributeType.DAMAGE, ItemType.AXE);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 1, 2, 4, 6)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 5, 7, 10, 14)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 10, 12, 17, 22)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 14, 17, 24, 29)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 10, 12, 17, 22)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 18, 34, 31, 42)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 26, 35, 48, 60)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 36, 42, 66, 78)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 26, 35, 48, 60)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 42, 48, 72, 84)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 72, 84, 96, 120)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 84, 96, 138, 156)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 72, 84, 96, 120)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 108, 138, 144, 192)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 156, 180, 216, 240)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 192, 204, 252, 282)));

			//
//          addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 65, 80, 125)));
//          addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 70, 85, 155)));
//          addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 90, 110, 220)));
//          addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 110, 150, 240)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 156, 180, 216, 240)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 216, 252, 276, 348)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 300, 348, 396, 444)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 360, 420, 480, 540)));
		}

	}

	public class StaffDamage extends ItemModifier {

		public StaffDamage() {
			super(WeaponAttributeType.DAMAGE, ItemType.STAFF);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 1, 1, 2, 4)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 2, 4, 5, 7)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 5, 6, 8, 11)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 7, 8, 12, 14)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 5, 6, 8, 11)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 9, 13, 17, 21)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 13, 17, 24, 30)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 18, 21, 33, 39)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 13, 17, 24, 30)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 21, 24, 36, 42)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 36, 42, 48, 60)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 42, 48, 69, 78)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 36, 42, 48, 60)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 54, 69, 72, 96)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 78, 90, 108, 120)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 96, 102, 126, 141)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 78, 90, 108, 120)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 108, 126, 138, 174)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 150, 174, 198, 222)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 180, 210, 240, 270)));
		}

	}

	public class PolearmDamage extends ItemModifier {

		public PolearmDamage() {
			super(WeaponAttributeType.DAMAGE, ItemType.POLEARM);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 1, 1, 2, 3)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 2, 3, 4, 6)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 4, 5, 7, 9)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 6, 7, 10, 12)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 4, 5, 7, 9)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 7, 10, 14, 18)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 11, 15, 20, 25)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 15, 18, 27, 32)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 16, 22, 30, 38)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 26, 30, 45, 52)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 45, 52, 60, 75)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 52, 60, 86, 98)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 45, 52, 60, 75)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 68, 86, 90, 120)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 98, 112, 135, 150)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 120, 128, 158, 190)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 98, 112, 135, 150)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 135, 158, 172, 218)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 188, 218, 172, 218)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 225, 262, 300, 338)));
		}

	}

	public class BowDamage extends ItemModifier {

		public BowDamage() {
			super(WeaponAttributeType.DAMAGE, ItemType.BOW);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 1, 2, 5, 7)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 5, 7, 10, 14)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 10, 12, 17, 22)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 14, 17, 24, 29)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 4, 5, 7, 9)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 7, 10, 14, 18)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 11, 15, 20, 25)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 15, 18, 27, 32)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 26, 35, 48, 60)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 42, 48, 72, 84)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 72, 84, 96, 120)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 84, 96, 138, 156)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 72, 84, 96, 120)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 108, 138, 144, 192)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 156, 180, 216, 240)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 192, 204, 252, 282)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.QUAD, 156, 180, 216, 240)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.QUAD, 216, 252, 276, 348)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.QUAD, 300, 348, 396, 444)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.QUAD, 360, 420, 480, 540)));
		}

	}

	public class Precision extends ItemModifier {
		public Precision(){
			super(WeaponAttributeType.PRECISION, ItemType.STAFF);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 5, 70), 30));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 5, 80), 30));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 10, 95), 30));
		}
	}
	public class Critical extends ItemModifier {

		public Critical() {
			super(WeaponAttributeType.CRITICAL_HIT, ItemWeapon.WEAPONS);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 2), 2));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 4), 5));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 5), 8));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 6), 9));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 10), 7));
		}
		
	}
	
	public class LifeSteal extends ItemModifier {

		public LifeSteal() {
			super(WeaponAttributeType.LIFE_STEAL, ItemWeapon.WEAPONS);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 30), 2));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 15), 4));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 12), 5));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 7), 10));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 8), 8));
		}
		
	}
	
	public class Knockback extends ItemModifier {

		public Knockback() {
			super(WeaponAttributeType.KNOCKBACK, SEMI_MELEE);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 3), 3));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 6), 10));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 12), 13));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 15), 16));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 20), 20));
		}
		
	}
	
	public class Blind extends ItemModifier {

		public Blind() {
			super(WeaponAttributeType.BLIND, ItemWeapon.WEAPONS);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 5), 3));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 7), 5));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 9), 8));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 9), 9));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 11), 11));
		}
		
	}
	
	public class Slow extends ItemModifier {

		public Slow() {
			super(WeaponAttributeType.SLOW, ItemType.BOW);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 3), 3));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 4), 10));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 5), 13));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 7), 16));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 10), 20));
		}
		
	}

	public class Elemental extends ItemModifier {

		public Elemental() {
			super(ELEMENTS, SEMI_MELEE);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 4), 6));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 9), 9));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 15), 10));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 25), 15));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 55), 20));
		}
	}

	public class ElementalBow extends ItemModifier {

		public ElementalBow() {
			super(ELEMENTS, ItemType.BOW);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 8), 6));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 15), 9));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 25), 10));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 45), 15));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 75), 20));
		}
	}
	
	public class Versus extends ItemModifier {

		public Versus() {
			super(new WeaponAttributeType[] {WeaponAttributeType.VS_PLAYER, WeaponAttributeType.VS_MONSTERS}, ItemWeapon.WEAPONS);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 10), 6));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 12), 9));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 15), 10));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 20), 12));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 15), 15));
		}
	}
	
	public class Pure extends ItemModifier {

		public Pure() {
			super(WeaponAttributeType.PURE_DAMAGE, ItemType.AXE);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 5), 6));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 8), 9));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 15), 5));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 25), 5));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 45), 10));
		}
		
	}
	
	public class Accuracy extends ItemModifier {

		public Accuracy() {
			super(WeaponAttributeType.ACCURACY, ItemType.SWORD);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 10), 8));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 12), 12));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 25), 15));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 28), 20));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 35), 15));
		}
		
	}
	
	public class ArmorPenetration extends ItemModifier {

		public ArmorPenetration() {
			super(WeaponAttributeType.ARMOR_PENETRATION, ItemType.AXE);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 1), 20));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 3), 20));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 5), 25));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 8), 20));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 10), 15));
		}
		
	}
}
