package net.dungeonrealms.items.armor;

import net.dungeonrealms.items.DamageMeta;
import net.dungeonrealms.items.NameGenerator;
import net.dungeonrealms.items.armor.Armor.ArmorModifier;
import net.dungeonrealms.items.armor.Armor.ArmorTier;
import net.dungeonrealms.items.armor.Armor.EquipmentType;
import net.dungeonrealms.items.repairing.RepairAPI;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Created by Nick on 9/21/2015.
 */
public class ArmorGenerator {
	/**
	 * Get a defined ArmorStack.
	 *
	 * @param type
	 * @param tier
	 * @param modifier
	 * @return
	 * @since 1.0
	 */
	public ItemStack getDefinedStack(Armor.EquipmentType type, Armor.ArmorTier tier, Armor.ArmorModifier modifier) {
		return getArmor(type, tier, modifier);
	}

	/**
	 * Gets a random set of armor.
	 *
	 * @return
	 * @since 1.0
	 */
	public ItemStack next() {
		return getArmor(getRandomEquipmentType(), getRandomItemTier(), getRandomItemModifier());
	}

	/**
	 * Used for the next() method above.
	 *
	 * @param tier
	 * @param modifier
	 * @return
	 * @since 1.0
	 */
	private ItemStack getArmor(Armor.EquipmentType type, Armor.ArmorTier tier, Armor.ArmorModifier modifier) {
		ItemStack item = getBaseItem(type, tier);
		ArrayList<Armor.ArmorAttributeType> attributeTypes = getRandomAttributes(new Random().nextInt(tier.getAttributeRange()));
		ItemMeta meta = item.getItemMeta();
		List<String> list = new NameGenerator().next(type);
		meta.setDisplayName(tier.getChatColorOfTier(tier) + list.get(0) + " " + list.get(1) + " " + list.get(2));
		List<String> itemLore = new ArrayList<>();

		HashMap<Armor.ArmorAttributeType, Integer> attributeTypeIntegerHashMap = new HashMap<>();

		attributeTypes.stream().filter(aType -> aType != null).forEach(aType -> {
			int i = new DamageMeta().nextArmor(tier, modifier, aType);
			attributeTypeIntegerHashMap.put(aType, i);
			itemLore.add(setCorrectArmorLore(aType, i));
		});
		itemLore.add(modifier.getChatColorOfModifier(modifier).toString() + modifier.getName());
		meta.setLore(itemLore);
		item.setItemMeta(meta);

		RepairAPI.setCustomItemDurability(item, 1500);

		// Time for some NMS on the item, (Backend attributes for reading).
		net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
		tag.set("type", new NBTTagString("armor"));

		// Settings NBT for the Attribute Class. () -> itemType, itemTier,
		// itemModifier
		tag.set("armorType", new NBTTagInt(type.getId()));
		tag.set("armorTier", new NBTTagInt(tier.getTierId()));
		tag.set("armorModifier", new NBTTagInt(modifier.getId()));

		/*
		 * The line below removes the weapons attributes. E.g. Diamond Sword
		 * says, "+7 Attack Damage"
		 */
		tag.set("AttributeModifiers", new NBTTagList());

		for (Map.Entry<Armor.ArmorAttributeType, Integer> entry : attributeTypeIntegerHashMap.entrySet()) {
			tag.set(entry.getKey().getNBTName(), new NBTTagInt(entry.getValue()));
		}

		nmsStack.setTag(tag);

		return CraftItemStack.asBukkitCopy(nmsStack);
	}

	/**
	 * Gets a random Equipment.
	 *
	 * @return
	 * @since 1.0
	 */
	private Armor.EquipmentType getRandomEquipmentType() {
		return Armor.EquipmentType.getById(new Random().nextInt(Armor.EquipmentType.values().length));
	}

	/**
	 * Gets a random Armor Tier
	 *
	 * @return
	 * @since 1.0
	 */
	public static Armor.ArmorTier getRandomItemTier() {
		return Armor.ArmorTier.getById(new Random().nextInt(Armor.ArmorTier.values().length));
	}

	/**
	 * Gets a random ArmorModifier
	 *
	 * @return
	 * @since 1.0
	 */
	public static Armor.ArmorModifier getRandomItemModifier() {
		return Armor.ArmorModifier.getById(new Random().nextInt(Armor.ArmorModifier.values().length));
	}

	/**
	 * Returns a list of itemAttributes based on the param.
	 *
	 * @param amountOfAttributes
	 * @return ArrayList
	 * @since 1.0
	 */
	private ArrayList<Armor.ArmorAttributeType> getRandomAttributes(int amountOfAttributes) {
		ArrayList<Armor.ArmorAttributeType> attributeList = new ArrayList<>();
		// We always want to add Damage to the Item. Since AttributeModifiers
		// are removed. Completely.
		if (new Random().nextBoolean()) {
			attributeList.add(Armor.ArmorAttributeType.ARMOR);
		} else {
			attributeList.add(Armor.ArmorAttributeType.DAMAGE);
		}
		attributeList.add(Armor.ArmorAttributeType.HEALTH_POINTS);
		if (new Random().nextBoolean()) {
			attributeList.add(Armor.ArmorAttributeType.HEALTH_REGEN);
		} else {
			attributeList.add(Armor.ArmorAttributeType.ENERGY_REGEN);
		}
		for (int i = 0; i < amountOfAttributes; i++) {
			int random = new Random().nextInt(Armor.ArmorAttributeType.values().length);
			if (!attributeList.contains(Armor.ArmorAttributeType.getById(random))
			        && canAddAttribute(Armor.ArmorAttributeType.getById(random), attributeList)) {
				attributeList.add(Armor.ArmorAttributeType.getById(random));
			} else {
				i--;
			}
		}
		return attributeList;
	}

	/**
	 * Returns if the attribute selected can be applied to the armor piece
	 *
	 * @param attributeType
	 * @param attributeList
	 * @return boolean
	 * @since 1.0
	 */
	private static boolean canAddAttribute(Armor.ArmorAttributeType attributeType, ArrayList<Armor.ArmorAttributeType> attributeList) {
		return !(attributeType == Armor.ArmorAttributeType.VITALITY || attributeType == Armor.ArmorAttributeType.DEXTERITY || attributeType == Armor.ArmorAttributeType.INTELLECT || attributeType == Armor.ArmorAttributeType.STRENGTH) || !attributeList.contains(Armor.ArmorAttributeType.VITALITY) && !attributeList.contains(Armor.ArmorAttributeType.DEXTERITY) && !attributeList.contains(Armor.ArmorAttributeType.INTELLECT) && !attributeList.contains(Armor.ArmorAttributeType.STRENGTH);
	}

	/**
	 * Returns Max/Min lore for an armor piece based on the Attribute Type
	 * includes chat colouring
	 *
	 * @param aType
	 * @param i
	 * @return String
	 * @since 1.0
	 */
	public static String setCorrectArmorLore(Armor.ArmorAttributeType aType, int i) {
		switch (aType) {
		case DAMAGE:
			return ChatColor.GREEN + "" + ChatColor.RED + i + "% " + ChatColor.WHITE + aType.getName();
		case ENERGY_REGEN:
			return ChatColor.GREEN + "" + ChatColor.RED + i + "% " + ChatColor.WHITE + aType.getName();
		case ARMOR:
			return ChatColor.GREEN + "" + ChatColor.RED + i + "% " + ChatColor.WHITE + aType.getName();
		case BLOCK:
			return ChatColor.GREEN + "" + ChatColor.RED + i + "% " + ChatColor.WHITE + aType.getName();
		case LUCK:
			return ChatColor.GREEN + "" + ChatColor.RED + i + "% " + ChatColor.WHITE + aType.getName();
		case THORNS:
			return ChatColor.GREEN + "" + ChatColor.RED + i + "% " + ChatColor.WHITE + aType.getName();
		case DODGE:
			return ChatColor.GREEN + "" + ChatColor.RED + i + "% " + ChatColor.WHITE + aType.getName();
		default:
			return ChatColor.GREEN + "" + ChatColor.RED + i + " " + ChatColor.WHITE + aType.getName();
		}
	}

	/**
	 * Returns ItemStack Material based on item type and tier.
	 *
	 * @param tier
	 * @return
	 * @since 1.0
	 */
	private ItemStack getBaseItem(Armor.EquipmentType type, Armor.ArmorTier tier) {
		switch (type) {
		case HELMET:
			switch (tier) {
			case TIER_1:
				return new ItemStack(Material.LEATHER_HELMET);
			case TIER_2:
				return new ItemStack(Material.CHAINMAIL_HELMET);
			case TIER_3:
				return new ItemStack(Material.IRON_HELMET);
			case TIER_4:
				return new ItemStack(Material.DIAMOND_HELMET);
			case TIER_5:
				return new ItemStack(Material.GOLD_HELMET);
			}
		case CHESTPLATE:
			switch (tier) {
			case TIER_1:
				return new ItemStack(Material.LEATHER_CHESTPLATE);
			case TIER_2:
				return new ItemStack(Material.CHAINMAIL_CHESTPLATE);
			case TIER_3:
				return new ItemStack(Material.IRON_CHESTPLATE);
			case TIER_4:
				return new ItemStack(Material.DIAMOND_CHESTPLATE);
			case TIER_5:
				return new ItemStack(Material.GOLD_CHESTPLATE);
			}
		case LEGGINGS:
			switch (tier) {
			case TIER_1:
				return new ItemStack(Material.LEATHER_LEGGINGS);
			case TIER_2:
				return new ItemStack(Material.CHAINMAIL_LEGGINGS);
			case TIER_3:
				return new ItemStack(Material.IRON_LEGGINGS);
			case TIER_4:
				return new ItemStack(Material.DIAMOND_LEGGINGS);
			case TIER_5:
				return new ItemStack(Material.GOLD_LEGGINGS);
			}
		case BOOTS:
			switch (tier) {
			case TIER_1:
				return new ItemStack(Material.LEATHER_BOOTS);
			case TIER_2:
				return new ItemStack(Material.CHAINMAIL_BOOTS);
			case TIER_3:
				return new ItemStack(Material.IRON_BOOTS);
			case TIER_4:
				return new ItemStack(Material.DIAMOND_BOOTS);
			case TIER_5:
				return new ItemStack(Material.GOLD_BOOTS);
			}
		}
		return null;
	}

	/**
	 * return ItemStack Array of random armor based on int tier.
	 * @param tier
	 */
	public ItemStack[] nextTier(int tier) {
		return new ItemStack[]{getArmor(EquipmentType.CHESTPLATE, ArmorTier.getByTier(tier), getRandomItemModifier()),
		getArmor(EquipmentType.LEGGINGS, ArmorTier.getByTier(tier), getRandomItemModifier()),
		getArmor(EquipmentType.BOOTS, ArmorTier.getByTier(tier), getRandomItemModifier())};

	}

	/**
	 * @return
	 */
	public ItemStack[] nextArmor(int tier, ArmorModifier modifier) {
		return new ItemStack[]{getArmor(EquipmentType.CHESTPLATE, ArmorTier.getByTier(tier), modifier),
		getArmor(EquipmentType.LEGGINGS, ArmorTier.getByTier(tier), modifier),
		getArmor(EquipmentType.BOOTS, ArmorTier.getByTier(tier), modifier)};
	}
}
