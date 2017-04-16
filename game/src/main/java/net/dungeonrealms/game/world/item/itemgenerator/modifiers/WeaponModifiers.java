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

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 2, 5)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 3, 5, 8)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 6, 9, 23)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 9, 15, 24)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 10, 12, 17)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 16, 18, 24)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 20, 30, 65)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 22, 35, 70)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 25, 30, 45)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 30, 35, 70)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 50, 90, 150)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 60, 100, 160)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 65, 80, 125)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 70, 85, 155)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 90, 110, 220)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 110, 150, 240)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 130, 140, 210)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 150, 160, 260)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 160, 230, 407)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 190, 250, 450)));
		}
		
	}
	
	public class AxeDamage extends ItemModifier {

        public AxeDamage() {
            super(WeaponAttributeType.DAMAGE, ItemType.AXE);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 2, 5)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 3, 5, 8)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 6, 9, 23)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 9, 15, 24)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 10, 12, 17)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 16, 18, 24)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 20, 30, 65)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 22, 35, 70)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 25, 30, 45)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 30, 35, 70)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 50, 90, 150)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 60, 100, 160)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 65, 80, 125)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 70, 85, 155)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 90, 110, 220)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 110, 150, 240)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 130, 140, 210)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 150, 160, 260)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 160, 230, 407)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 200, 300, 550)));
        }
        
    }
	
	public class StaffDamage extends ItemModifier {

        public StaffDamage() {
            super(WeaponAttributeType.DAMAGE, ItemType.STAFF);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 1, 3)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 1, 3, 5)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 1, 5, 7)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 2, 7, 11)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 5, 6, 9)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 8, 9, 12)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 10, 15, 30)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 11, 25, 35)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 12, 15, 23)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 15, 12, 35)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 25, 45, 60)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 30, 50, 80)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 32, 40, 63)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 35, 45, 70)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 45, 60, 110)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 55, 76, 120)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 65, 70, 115)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 75, 80, 130)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 90, 115, 200)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 100, 140, 270)));
		}
        
	}

	public class PolearmDamage extends ItemModifier {

	    public PolearmDamage() {
	        super(WeaponAttributeType.DAMAGE, ItemType.POLEARM);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 1, 3)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 1, 3, 5)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 1, 5, 7)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 2, 7, 11)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 5, 6, 9)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 8, 9, 12)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 10, 15, 30)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 11, 25, 35)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 12, 15, 23)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 15, 12, 35)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 25, 45, 60)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 30, 50, 80)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 32, 40, 63)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 35, 45, 70)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 45, 60, 110)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 55, 76, 120)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 65, 70, 115)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 75, 80, 130)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 90, 115, 200)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 100, 140, 270)));
	    }

	}

	public class BowDamage extends ItemModifier {

	    public BowDamage() {
	        super(WeaponAttributeType.DAMAGE, ItemType.BOW);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 2, 5)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 3, 5, 8)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 6, 9, 23)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 9, 15, 24)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 10, 12, 17)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 16, 18, 24)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 20, 30, 65)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 22, 35, 70)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 25, 30, 45)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 30, 35, 70)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 50, 90, 150)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 60, 100, 160)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 65, 80, 125)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 70, 85, 155)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 90, 110, 220)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 110, 150, 240)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 130, 140, 210)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 150, 160, 260)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 160, 230, 407)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 190, 250, 450)));
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
