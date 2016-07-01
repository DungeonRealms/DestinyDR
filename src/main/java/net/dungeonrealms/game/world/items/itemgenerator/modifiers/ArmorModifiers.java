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

public class ArmorModifiers {
	
	private static List<ItemType> armor = Arrays.asList(ItemType.BOOTS, ItemType.CHESTPLATE, ItemType.LEGGINGS, ItemType.HELMET);
	private static ChatColor r = ChatColor.RED;
	private static List<String> elements = Arrays.asList("FIRE", "ICE", "POISON");

	public class HPRegen extends ItemModifier {

		public HPRegen() {
			super(armor, 100, "healthRegen", r + "HP REGEN: +", " HP/s", false);
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 20, 35)).setCantContain(ArmorModifiers.EnergyRegen.class));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 45, 60)).setCantContain(ArmorModifiers.EnergyRegen.class));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 65, 80)).setCantContain(ArmorModifiers.EnergyRegen.class));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 110, 130)).setCantContain(ArmorModifiers.EnergyRegen.class));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 250, 300)).setCantContain(ArmorModifiers.EnergyRegen.class));

			setOrderPriority(5);
		}

	}

	public class MainArmor extends ItemModifier {

		public MainArmor() {
			super(Collections.singletonList(ItemType.CHESTPLATE), 100, "armor", r + "ARMOR: ", "%", false); // 50% chance for DPS, 50% for armor

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 1, 1)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 1, 2, 3)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 1, 2, 3)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 1, 2, 3)).setCantContain(MainDPS.class));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 2, 3)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 3, 4, 5)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 5, 6, 7)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 5, 6, 7)).setCantContain(MainDPS.class));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 5, 6, 7)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 6, 9, 10)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 8, 9, 10)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 8, 9, 11)).setCantContain(MainDPS.class));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 8, 9, 10)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 10, 11, 12)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 11, 12, 13)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 12, 13, 14)).setCantContain(MainDPS.class));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 11, 12, 13)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 13, 14, 15)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 16, 17, 18)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 17, 18, 19)).setCantContain(MainDPS.class));

			setOrderPriority(2);
		}

	}

	public class LeggingArmor extends ItemModifier {

		public LeggingArmor() {
			super(Collections.singletonList(ItemType.LEGGINGS), 100, "armor", r + "ARMOR: ", "%", false); // 50% chance for DPS, 50% for armor

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 1, 1)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 1, 2, 3)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 1, 2, 3)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 1, 2, 3)).setCantContain(MainDPS.class));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 2, 3)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 3, 4, 5)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 5, 6, 7)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 5, 6, 7)).setCantContain(MainDPS.class));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 5, 6, 7)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 6, 9, 10)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 8, 9, 10)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 8, 9, 11)).setCantContain(MainDPS.class));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 8, 9, 10)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 10, 11, 12)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 11, 12, 13)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 12, 13, 14)).setCantContain(MainDPS.class));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 11, 12, 13)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 13, 14, 15)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 16, 17, 18)).setCantContain(MainDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 17, 18, 19)).setCantContain(MainDPS.class));

			setOrderPriority(2);
		}

	}

	public class HelmetsArmor extends ItemModifier {

		public HelmetsArmor() {
			super(Collections.singletonList(ItemType.HELMET), 100, "armor", r + "ARMOR: ", "%", false); // 50% chance for DPS, 50% for armor

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 1, 1)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 1, 1, 2)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 1, 1, 2)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 1, 1, 2)).setCantContain(OtherDPS.class));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 1, 2)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 2, 2, 3)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 3, 3, 4)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 3, 3, 4)).setCantContain(OtherDPS.class));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 3, 3, 4)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 3, 5, 5)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 4, 5, 5)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 4, 5, 6)).setCantContain(OtherDPS.class));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 4, 5, 5)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 5, 6, 6)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 6, 6, 7)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 6, 7, 7)).setCantContain(OtherDPS.class));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 6, 6, 7)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 7, 7, 8)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 8, 9, 9)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 9, 9, 10)).setCantContain(OtherDPS.class));

			setOrderPriority(2);
		}

	}

	public class BootsArmor extends ItemModifier {

		public BootsArmor() {
			super(Collections.singletonList(ItemType.BOOTS), 100, "armor", r + "ARMOR: ", "%", false); // 50% chance for DPS, 50% for armor

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 1, 1)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 1, 1, 2)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 1, 1, 2)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 1, 1, 2)).setCantContain(OtherDPS.class));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 1, 2)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 2, 2, 3)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 3, 3, 4)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 3, 3, 4)).setCantContain(OtherDPS.class));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 3, 3, 4)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 3, 5, 5)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 4, 5, 5)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 4, 5, 6)).setCantContain(OtherDPS.class));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 4, 5, 5)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 5, 6, 6)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 6, 6, 7)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 6, 7, 7)).setCantContain(OtherDPS.class));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 6, 6, 7)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 7, 7, 8)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 8, 9, 9)).setCantContain(OtherDPS.class));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 9, 9, 10)).setCantContain(OtherDPS.class));

			setOrderPriority(2);
		}

	}

	public class MainDPS extends ItemModifier {

        public MainDPS() {
            super(Collections.singletonList(ItemType.LEGGINGS), 50, "dps", r + "DPS: ", "%", false); // 50% chance for DPS, 50% for armor

            addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 1, 1)));
            addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 1, 2, 3)));
            addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 1, 2, 3)));
            addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 1, 2, 3)));

            addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 2, 3)));
            addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 3, 4, 5)));
            addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 5, 6, 7)));
            addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 5, 6, 7)));

            addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 5, 6, 7)));
            addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 6, 9, 10)));
            addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 8, 9, 10)));
            addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 8, 9, 11)));

            addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 8, 9, 10)));
            addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 10, 11, 12)));
            addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 11, 12, 13)));
            addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 12, 13, 14)));

            addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 11, 12, 13)));
            addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 13, 14, 15)));
            addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 16, 17, 18)));
            addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 17, 18, 19)));

            setOrderPriority(1);
        }
	}

	public class MainDPS1 extends ItemModifier {

        public MainDPS1() {
            super(Collections.singletonList(ItemType.CHESTPLATE), 50, "dps", r + "DPS: ", "%", false); // 50% chance for DPS, 50% for armor

            addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 1, 1)));
            addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 1, 2, 3)));
            addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 1, 2, 3)));
            addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 1, 2, 3)));

            addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 2, 3)));
            addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 3, 4, 5)));
            addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 5, 6, 7)));
            addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 5, 6, 7)));

            addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 5, 6, 7)));
            addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 6, 9, 10)));
            addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 8, 9, 10)));
            addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 8, 9, 11)));

            addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 8, 9, 10)));
            addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 10, 11, 12)));
            addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 11, 12, 13)));
            addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 12, 13, 14)));

            addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 11, 12, 13)));
            addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 13, 14, 15)));
            addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 16, 17, 18)));
            addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 17, 18, 19)));

            setOrderPriority(1);
        }

    }

	public class OtherDPS extends ItemModifier {

        public OtherDPS() {
            super(Collections.singletonList(ItemType.HELMET), 50, "dps", r + "DPS: ", "%", false); // 50% chance for DPS, 50% for armor

            addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 1, 1)));
            addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 1, 1, 2)));
            addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 1, 1, 2)));
            addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 1, 1, 2)));

            addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 1, 2)));
            addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 2, 2, 3)));
            addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 3, 3, 4)));
            addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 3, 3, 4)));

            addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 3, 3, 4)));
            addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 3, 5, 5)));
            addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 4, 5, 5)));
            addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 4, 5, 6)));

            addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 4, 5, 5)));
            addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 5, 6, 6)));
            addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 6, 6, 7)));
            addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 6, 7, 7)));

            addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 6, 6, 7)));
            addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 7, 7, 8)));
            addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 8, 9, 9)));
            addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 9, 9, 10)));

            setOrderPriority(1);
        }

    }

	public class OtherDPS1 extends ItemModifier {

        public OtherDPS1() {
            super(Collections.singletonList(ItemType.BOOTS), 50, "dps", r + "DPS: ", "%", false); // 50% chance for DPS, 50% for armor

            addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 1, 1)));
            addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 1, 1, 2)));
            addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 1, 1, 2)));
            addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 1, 1, 2)));

            addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 1, 2)));
            addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 2, 2, 3)));
            addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 3, 3, 4)));
            addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 3, 3, 4)));

            addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 3, 3, 4)));
            addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 3, 5, 5)));
            addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 4, 5, 5)));
            addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 4, 5, 6)));

            addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 4, 5, 5)));
            addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 5, 6, 6)));
            addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 6, 6, 7)));
            addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 6, 7, 7)));

            addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 6, 6, 7)));
            addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 7, 7, 8)));
            addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 8, 9, 9)));
            addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 9, 9, 10)));

            setOrderPriority(1);
        }

    }

	public class ChestplateHP extends ItemModifier {

        public ChestplateHP() {
            super(Collections.singletonList(ItemType.CHESTPLATE), 100, "healthPoints", r + "HP: +", null, false);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.STATIC, 10, 90)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.STATIC, 90, 150)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.STATIC, 150, 180)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.STATIC, 180, 240)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.STATIC, 300, 420)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.STATIC, 420, 500)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.STATIC, 500, 560)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.STATIC, 560, 580)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.STATIC, 700, 900)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.STATIC, 900, 1100)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.STATIC, 1100, 1250)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.STATIC, 1250, 1350)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.STATIC, 1800, 2600)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.STATIC, 2600, 3200)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.STATIC, 3200, 3600)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.STATIC, 3600, 3900)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.STATIC, 4200, 5800)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.STATIC, 5800, 6900)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.STATIC, 6900, 7700)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.STATIC, 7700, 8100)));

            setOrderPriority(3);
        }

    }

	public class LeggingsHP extends ItemModifier {

        public LeggingsHP() {
            super(Collections.singletonList(ItemType.LEGGINGS), 100, "healthPoints", r + "HP: +", null, false);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.STATIC, 10, 90)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.STATIC, 90, 150)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.STATIC, 150, 180)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.STATIC, 180, 240)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.STATIC, 300, 420)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.STATIC, 420, 500)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.STATIC, 500, 560)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.STATIC, 560, 580)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.STATIC, 700, 900)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.STATIC, 900, 1100)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.STATIC, 1100, 1250)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.STATIC, 1250, 1350)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.STATIC, 2000, 2900)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.STATIC, 2900, 3500)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.STATIC, 3500, 4000)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.STATIC, 4000, 4300)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.STATIC, 4400, 6000)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.STATIC, 6000, 7120)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.STATIC, 7120, 7920)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.STATIC, 7920, 8480)));

            setOrderPriority(3);
        }

    }

	public class BootsHP extends ItemModifier {

        public BootsHP() {
            super(Collections.singletonList(ItemType.BOOTS), 100, "healthPoints", r + "HP: +", null, false);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.STATIC, 6, 54)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.STATIC, 54, 90)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.STATIC, 90, 108)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.STATIC, 108, 120)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.STATIC, 180, 252)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.STATIC, 252, 300)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.STATIC, 300, 336)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.STATIC, 336, 348)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.STATIC, 420, 540)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.STATIC, 540, 660)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.STATIC, 660, 950)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.STATIC, 750, 810)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.STATIC, 1200, 1740)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.STATIC, 1740, 2100)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.STATIC, 2100, 2400)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.STATIC, 2400, 2580)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.STATIC, 3300, 4500)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.STATIC, 4500, 5340)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.STATIC, 5340, 5940)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.STATIC, 5940, 6360)));

            setOrderPriority(3);
        }
    }

	public class HelmetHP extends ItemModifier {

        public HelmetHP() {
            super(Collections.singletonList(ItemType.HELMET), 100, "healthPoints", r + "HP: +", null, false);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.STATIC, 6, 54)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.STATIC, 54, 90)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.STATIC, 90, 108)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.STATIC, 108, 120)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.STATIC, 180, 252)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.STATIC, 252, 300)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.STATIC, 300, 336)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.STATIC, 336, 348)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.STATIC, 420, 540)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.STATIC, 540, 660)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.STATIC, 660, 950)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.STATIC, 750, 810)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.STATIC, 1200, 1740)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.STATIC, 1740, 2100)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.STATIC, 2100, 2400)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.STATIC, 2400, 2580)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.STATIC, 3300, 4500)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.STATIC, 4500, 5340)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.STATIC, 5340, 5940)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.STATIC, 5940, 6360)));

            setOrderPriority(3);
        }

    }

	public class StrDexVitInt extends ItemModifier {

		public StrDexVitInt() {
			super(armor, -1, null, null, null);
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
			if (chosenStat == null || chosenStat.equalsIgnoreCase("")) {
			    chooseStat();
			}
			return r + chosenStat.substring(0, 3).toUpperCase() + ": +";
		}

		@Override
		public String getNBTName() {
		    if (chosenStat == null || chosenStat.equalsIgnoreCase("")) {
                chooseStat();
            }
		    return chosenStat;
		}
	}

	public class EnergyRegen extends ItemModifier {

		public EnergyRegen() {
			super(armor, 50, "energyRegen", r + "ENERGY REGEN: +", "%", false);
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 5, true)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 3, 7, true)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 5, 9, true)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 7, 12, true)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 7, 12, true)));

			setOrderPriority(4);
		}

	}

	public class Thorns extends ItemModifier {

		public Thorns() {
			super(armor, -1, "thorns", r + "THORNS: +", "% DMG");
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 2), 3));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 3), 5));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 5), 10));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 9), 13));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 9), 20));
		}

	}

	public class Dodge extends ItemModifier {

		public Dodge() {
			super(armor, -1, "dodge", r + "DODGE: ", "%");
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 5), 5));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 8), 9));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 10), 15));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 12), 25));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 12), 30));
		}

	}

	public class Block extends ItemModifier {

		public Block() {
			super(armor, -1, "block", r + "BLOCK: ", "%");
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 5), 5));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 8), 9));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 10), 15));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 12), 25));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 12), 30));
		}

	}

	public class Resistances extends ItemModifier {

		public Resistances() {
			super(armor, -1, null, null, "%");
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 5), 15));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 7), 15));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 20), 25));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 32), 20));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 45), 30));
		}

		@Override
		public void chooseStat() {
		    chosenStat = elements.get(new Random().nextInt(elements.size()));
		}

		@Override
		public String getPrefix(ItemMeta meta){
			if (chosenStat == null || chosenStat == "") chooseStat();
		    return r + chosenStat + " RESISTANCE: ";
		}

		@Override
		public String getNBTName() {
		    return chosenStat.toLowerCase() + "Resistance";
		}
	}

	public class Reflection extends ItemModifier {

		public Reflection() {
			super(armor, -1, "reflection", r + "REFLECTION: ", "%");
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 1), 3));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 2), 5));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 4), 10));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 5), 13));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 5), 15));
		}

	}

	public class GemFind extends ItemModifier {

		public GemFind() {
			super(armor, -1, "gemFind", r + "GEM FIND: ", "%");
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 5), 5));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 8), 5));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 15), 5));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 20), 5));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 20), 5));
		}

	}

	public class ItemFind extends ItemModifier {

		public ItemFind() {
			super(armor, -1, "itemFind", r + "ITEM FIND: +", "%");
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 1), 5));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 2), 5));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 3), 5));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 4), 5));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 4), 5));
		}
		
	}
	
}
