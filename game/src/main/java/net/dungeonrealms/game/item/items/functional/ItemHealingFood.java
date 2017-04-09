package net.dungeonrealms.game.item.items.functional;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemConsumeEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.Item.ItemTier;

public class ItemHealingFood extends FunctionalItem {
	
	private EnumHealingFood foodType;
	
	private int healAmount;

	public ItemHealingFood(EnumHealingFood foodType) {
		super(ItemType.FOOD);
		this.foodType = foodType;
	}
	
	@SuppressWarnings("deprecation")
	private void startHealing(Player player, int healAmount) {     
		Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), new BukkitRunnable() {
			int secondsHealed = 0;
			
			public void run() {
				secondsHealed++;
				if(secondsHealed >= 15)
					cancel();
				
				if(player.isSprinting() || CombatLog.isInCombat(player)) {
					player.sendMessage(ChatColor.RED + "In the commotion, you have dropped your food.");
					cancel();
					return;
				}
				
				if (HealthHandler.getPlayerHP(player) < HealthHandler.getPlayerMaxHP(player))
					HealthHandler.healPlayer(player, healAmount);
			}
		}, 0, 20);
	}
	
	@Override
	public void loadItem() {
		this.healAmount = getTagInt("healAmount");
	}
	
	@Override
	public void updateItem() {
		setTagInt("healAmount", this.healAmount);
	}
	
	@Override
	public void onClick(ItemClickEvent evt) {}

	@Override
	public void onConsume(ItemConsumeEvent evt) {
		Player player = evt.getPlayer();
        
		evt.setUsed(true);
        player.setFoodLevel(player.getFoodLevel() + 6);
        if (HealthHandler.getPlayerHP(player) < HealthHandler.getPlayerMaxHP(player)) {
            player.sendMessage(ChatColor.GREEN + "Healing " + ChatColor.BOLD + healAmount + ChatColor.GREEN + "HP/s for 15 seconds!");
            startHealing(player, healAmount);
        } else {
            player.sendMessage(ChatColor.YELLOW + "You are already at full HP, however, your hunger has been satisfied.");
        }
	}

	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {}

	@Override
	protected ItemUsage[] getUsage() {
		return new ItemUsage [] { ItemUsage.CONSUME_ITEM };
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(this.foodType.getMaterial());
	}

	@Override
	protected String getDisplayName() {
		return this.foodType.getItemTier().getColor() + this.foodType.getDisplayName();
	}

	@Override
	protected String[] getLore() {
		return new String[] {
				ChatColor.RED + "+" + ChatColor.BOLD + foodType.getHealAmount() + "HP/s" + ChatColor.RED + " for " + ChatColor.BOLD + "15 " + ChatColor.RED + "seconds.",
				ChatColor.RED.toString() + ChatColor.BOLD + "Sprinting will cancel the effect!",
				foodType.getDescription(),
				foodType.getRarity().getName()
			};
	}
	
	public enum EnumHealingFood {
		
		// Tier 1
		POTATO(1, ItemRarity.COMMON, Material.POTATO, 9, 2, "Plowed Potato", "The staple crop of Andulucia."),
		POTATO_SKIN(1, ItemRarity.RARE, Material.BAKED_POTATO, 16, 4, "Loaded Potato Skin", "Extremely Tasty."),
		APPLE(1, ItemRarity.UNIQUE, Material.APPLE, 25, 8, "Fresh Apple", "Fresh from the local apple tree."),
		// Tier 2
		RAW_CHICKEN(2, ItemRarity.COMMON, Material.RAW_CHICKEN, 42, 10, "Uncooked Chicken", "This may or may not be safe to eat..."),
		ROAST_CHICKEN(2, ItemRarity.RARE, Material.COOKED_CHICKEN, 55, 14, "Roast Chicken", "Warm and toasty. Delicious too."),
		PUMPKIN_PIE(2, ItemRarity.UNIQUE, Material.PUMPKIN_PIE, 70, 18, "Pumpkin Pie", "The spookiest meal you'll ever eat."),
		// Tier 3
		SALTED_PORK(3, ItemRarity.COMMON, Material.PORK, 90, 20, "Salted Pork", "Bringing in the bacon."),
		SEASONED_PORK(3, ItemRarity.RARE, Material.GRILLED_PORK, 150, 25, "Seasoned Pork", "Bacon. Except tastier (is that possible?)."),
		MUSHROOM_SOUP(3, ItemRarity.UNIQUE, Material.MUSHROOM_SOUP, 190, 30, "Mushroom Soup", "I hope these are the correct mushrooms."),
		// Tier 4
		FROZEN_STEAK(4, ItemRarity.COMMON, Material.RAW_BEEF, 300, 35, "Frozen Steak", "Stop complaining. Your dog would love this."),
		WARM_STEAK(4, ItemRarity.RARE, Material.COOKED_BEEF, 400, 45, "Rare Warm Steak", "Real men take their steaks rare."),
		GRILLED_RABBIT(4, ItemRarity.UNIQUE, Material.COOKED_MUTTON, 500, 55, "Grilled Rabbit", "Aww, look at the cute little bunny."),
		// Tier 5
		KINGS_APPLE(4, ItemRarity.COMMON, Material.GOLDEN_APPLE, 700, 95, "King's Apple", "A meal fit for a King."),
		ENCH_KINGS_APPLE(4, ItemRarity.RARE, Material.GOLDEN_APPLE, 1, 1000, 100, "Enchanted King's Apple", "A powerful King's battle snack."),
		GOLD_CARROT(4, ItemRarity.UNIQUE, Material.GOLDEN_CARROT, 1350, 128, "Golden Carrot", "Now this is just a waste of useful gold ore.");
		
		@Getter private final String displayName;
		@Getter private final ItemRarity rarity;
		@Getter private final String description;
		@Getter private final int healAmount;
		@Getter private final Material material;
		@Getter private final int meta;
		@Getter private final int tier;
		@Getter private final int price;
		
		EnumHealingFood(int tier, ItemRarity rarity, Material mat, int health, int price, String name, String desc) {
			this(tier, rarity, mat, 0, health, price, name, desc);
		}
		
		EnumHealingFood(int tier, ItemRarity rarity, Material mat, int meta, int health, int price, String name, String description) {
			this.tier = tier;
			this.displayName = name;
			this.material = mat;
			this.healAmount = health;
			this.description = ItemTier.getByTier(tier).getColor() + description;
			this.meta = meta;
			this.rarity = rarity;
			this.price = price;
		}

		public static EnumHealingFood get(int tier, ItemRarity rare) {
			for(EnumHealingFood food : values())
				if(tier == food.getTier() && rare == food.getRarity())
					return food;
			return EnumHealingFood.POTATO;
		}
		
		public ItemTier getItemTier() {
			return ItemTier.getByTier(this.tier);
		}
	}
}
