package net.dungeonrealms.game.item;

import java.util.Random;

import net.dungeonrealms.game.item.items.core.*;
import net.dungeonrealms.game.item.items.functional.*;
import net.dungeonrealms.game.item.items.functional.ecash.*;
import net.dungeonrealms.game.world.item.Item.GeneratedItemType;
import lombok.Getter;

/**
 * A registry of all persistent items.
 * If an item is not registered here, it will NOT be constructable via PersistentItem.constructItem
 * @author Kneesnap
 */
public enum ItemType {

	//  COMBAT  //
	MELEE("melee", ItemWeaponMelee.class, GeneratedItemType.SWORD, GeneratedItemType.AXE),
	POLEARM("polearm", ItemWeaponPolearm.class, GeneratedItemType.POLEARM),
	STAFF("staff", ItemWeaponStaff.class, GeneratedItemType.STAFF),
	BOW("bow", ItemWeaponBow.class, GeneratedItemType.BOW),
	ARMOR("armor", ItemArmor.class, GeneratedItemType.HELMET, GeneratedItemType.CHESTPLATE, GeneratedItemType.LEGGINGS, GeneratedItemType.BOOTS),
	
	//  PROFESSION TOOLS  //
	PICKAXE("pick", ItemPickaxe.class, GeneratedItemType.PICKAXE),
	FISHING_POLE("rod", ItemFishingPole.class, GeneratedItemType.FISHING_ROD),
	
	//  FUNCTIONAL ITEMS  //
	SCRAP("scrap", ItemScrap.class),
	TELEPORT_BOOK("teleport", ItemTeleportBook.class),
	FISH("fish", ItemFish.class),
	FOOD("food", ItemHealingFood.class),
	POTION("potion", PotionItem.class),
	ENCHANT_WEAPON("enchantWeapon", ItemEnchantWeapon.class),
	ENCHANT_ARMOR("enchantArmor", ItemEnchantArmor.class),
	ENCHANT_PICKAXE("enchantPickaxe", ItemEnchantPickaxe.class),
	ENCHANT_FISHING_ROD("enchantFishingRod", ItemEnchantFishingRod.class),
	PROTECTION_SCROLL("protScroll", ItemProtectionScroll.class),
	
	//  ORBS  //
	ORB_OF_ALTERATION("orb", ItemOrb.class),
	ORB_OF_FLIGHT("flightOrb", ItemFlightOrb.class),
	ORB_OF_PEACE("peaceOrb", ItemPeaceOrb.class),
	
	//  MONEY  //
	GEM("gem", ItemGem.class),
	GEM_POUCH("gemPouch", ItemGemPouch.class),
	GEM_NOTE("gemNote", ItemGemNote.class),
	
	//  ECASH  //
	GLOBAL_MESSAGER("globalMessager", ItemGlobalMessager.class),
	RETRAINING_BOOK("retrainingBook", ItemRetrainingBook.class),
	MOUNT("mount", ItemMount.class),
	MULE("mule", ItemMuleMount.class),
	PARTICLE_TRAIL("trail", ItemParticleTrail.class),
	PET("pet", ItemPet.class),
	
	//  DONATION  //
	BUFF("buff", ItemBuff.class),
	
	//  MENU ITEMS  //
	PLAYER_JOURNAL("journal", ItemPlayerJournal.class),
	PORTAL_RUNE("portalRune", ItemPortalRune.class),
	HEARTHSTONE("hearthstone", ItemHearthstone.class),
	OPEN_PROFILE("profile", ItemPlayerProfile.class);
	
	@Getter private final String NBT;
	@Getter private final Class<? extends PersistentItem> itemClass;
	private GeneratedItemType[] generatedType;
	
	ItemType(String nbt, Class<? extends PersistentItem> cls, GeneratedItemType... gType) {
		this.NBT = nbt;
		this.itemClass = cls;
		this.generatedType = gType;
	}
	
	/**
	 * Gets the GeneratedItemType of an item.
	 * Will throw an error if there is no defined GeneratedItemType.
	 */
	public GeneratedItemType getType() {
		assert this.generatedType != null && this.generatedType.length > 0;
		return this.generatedType[new Random().nextInt(this.generatedType.length)];
	}
	
	public static ItemType getType(String type) {
		for (ItemType it : ItemType.values())
			if (it.getNBT().equals(type))
				return it;
		return null;
	}
}
