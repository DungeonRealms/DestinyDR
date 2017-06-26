package net.dungeonrealms.game.item.items.core;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.database.PlayerGameStats.StatColumn;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.data.ScrapTier;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.Item.AttributeType;
import net.dungeonrealms.game.world.item.Item.GeneratedItemType;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ItemModifier;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierCondition;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierRange;
import net.minecraft.server.v1_9_R2.NBTTagList;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ItemGear - Contains shared methods for gear that can have attributes, be
 * orbed, etc.
 * <p>
 * Created March 28th, 2017.
 *
 * @author Kneesnap
 */
@Getter
public abstract class ItemGear extends ItemGeneric {

    @Setter
    private boolean Protected; // Uppercast to avoid being the keyword "protected".
    private ItemTier tier;
    private ItemRarity rarity;

    //If we want some randomness to their gear allow for a min amount of uniques if thats desired.
    @Getter
    private ItemRarity maxRarity;
    @Getter
    private int minRarityItems;
    @Setter
    private int enchantCount;

    protected int durability;
    private AttributeList attributes;
    private boolean rollStats;


    public static final int MAX_DURABILITY = 1500;
    private static final int[] SUCCESS_CHANCE = {100, 100, 100, 70, 60, 50, 35, 25, 20, 15, 10, 5};
    private static final int[] DURABILITY_WARNINGS = {2, 5, 10, 30};

    public ItemGear(ItemType... types) {
        this(types[ThreadLocalRandom.current().nextInt(types.length)]);
    }

    // Used for item generators usually.
    public ItemGear(ItemType type) {
        super(type);
        setType(type);
        setAntiDupe(true);
        setTier(ItemTier.getRandomTier());
        setRarity(ItemRarity.getRandomRarity());
        this.durability = MAX_DURABILITY;
        rollStats = true;
    }

    // Used for loading existing items usually.
    public ItemGear(ItemStack item) {
        super(item);
        setAntiDupe(true);
    }

    @Override
    protected void loadItem() {
        super.loadItem();

        this.attributes = new AttributeList();

        // LOAD GENERAL DATA //
        setTier(ItemTier.getByTier(getTagInt(TIER)));
        setProtected(getTagBool("protected"));
        setEnchantCount(getTagInt("enchant"));

        if (hasTag("itemRarity"))
            this.setRarity(ItemRarity.valueOf(getTagString("itemRarity")));

        // LOAD DURABILITY //
        if (hasTag("RepairCost")) {
            this.durability = getTagInt("RepairCost");
        } else {
            double percent = (Math.max(1, (getItem().getType().getMaxDurability() - getItem().getDurability())) / Math.max(1, getItem().getType().getMaxDurability()));
            // We don't just multiply by MAX_DURABILITY because that results in
            // rounding at the wrong decimal place.
            this.durability = (int) (Math.round(percent * 100) * (MAX_DURABILITY / 100));
        }

        // LOAD ATTRIBUTES //
        if (getGeneratedItemType() != null)
            getAttributes().load(getTag(),
                    getGeneratedItemType().getAttributeBank().getAttributes());
    }

    @Override
    public void updateItem() {
        if (isRollStats()) {
            rollStats(false);
            this.rollStats = false;
        }

        // SAVE GENERAL DATA //
        if (getRarity() != null)
            setTagString("itemRarity", getRarity().name());

        getAttributes().save(this);

        setTagInt(TIER, getTier().getId());
        setTagInt("enchant", getEnchantCount());
        setTagBool("protected", isProtected());
        setTagInt("RepairCost", getDurability());

        // Removes the extra tag on gear, Ie: Diamond Sword - "+7 Attack Damage"
        getTag().set("AttributeModifiers", new NBTTagList());

        double percent = getDurabilityPercent() / 100D;
        getItem().setDurability((short) (getItem().getType().getMaxDurability() - percent * getItem().getType().getMaxDurability()));

        if (getEnchantCount() > 3)
            setGlowing(true);
        else if (getMeta().hasEnchant(Enchantment.ARROW_INFINITE))
            getMeta().removeEnchant(Enchantment.ARROW_INFINITE);

        super.updateItem();
    }

    /**
     * Change the item type of this item. Can be dangerous if used improperly.
     */
    public ItemGear setType(ItemType type) {
        super.setType(type);
        return this;
    }

    public ItemGear setMaxRarity(ItemRarity rarity, int minRequired) {
        this.maxRarity = rarity;
        this.minRarityItems = minRequired;
        return this;
    }

    public GeneratedItemType getGeneratedItemType() {
        return getItemType().getType();
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(getGeneratedItemType().getTier(getTier()));
    }

    /**
     * Called when an item's durability runs out. Calls functions such as
     * respawn a new pickaxe or recalculate player armor stats.
     */
    protected abstract void onItemBreak(Player player);

    /**
     * Gets the repair cost for one percent of this item.
     */
    protected abstract double getBaseRepairCost();

    /**
     * Called to give each item its own attribute upgrades when enchanted.
     */
    protected abstract void applyEnchantStats();

    public ItemGear setRarity(ItemRarity rarity) {
        this.rarity = rarity;
        return this;
    }

    public ItemGear setTier(int tier) {
        return setTier(ItemTier.getByTier(tier));
    }

    public ItemGear setTier(ItemTier tier) {
        this.tier = tier;
        return this;
    }

    public void updateLore() {
        // Save attributes
//        getAttributes().save(this);

        // Show rarity.
        if (getRarity() != null)
            addLore(getRarity().getName());

        // Show protection status.
        if (isProtected())
            addLore(ChatColor.WHITE + ChatColor.BOLD.toString() + "Protected");

        // UPDATE DISPLAY NAME //
        getMeta().setDisplayName(generateItemName());
    }

    protected String generateItemName() {
        String name = getTier().getColor().toString();

        List<AttributeType> sorted = new ArrayList<>(attributes.getAttributes());
        sorted.sort((a1, a2) -> a2.getDisplayPriority()
                - a1.getDisplayPriority());

        GeneratedItemType itemType = this.getGeneratedItemType();
        String rawItemName = itemType.getTierName(getTier());

        // ADD PREFIXES //
        for (AttributeType type : sorted)
            if (!type.getDisplayPrefix().equals(""))
                name += type.getDisplayPrefix() + " ";

        // ADD SUFFIXES //
        for (AttributeType type : sorted) {
            boolean contains = name.contains(rawItemName);
            String suffix = type.getDisplaySuffix(contains);
            if (!suffix.equals(""))
                name += (contains ? "" : rawItemName + " of") + " " + suffix;
        }

        if (!name.contains(rawItemName))
            name += rawItemName;

        if (getEnchantCount() > 0)
            name = ChatColor.RED + "[+" + getEnchantCount() + "] " + name;
        return name;
    }

    @Override // Shows enchant count for EC-named items.
    public String getCustomName() {
        String o = super.getCustomName();
        return o != null ? (getEnchantCount() > 0 ? ChatColor.RED + "[+" + getEnchantCount() + "] " : "") + getTier().getColor() + o : null;
    }

    /**
     * Enchants this item.
     */
    public void enchantItem(Player p) {
        boolean success = ThreadLocalRandom.current().nextInt(100) <= SUCCESS_CHANCE[enchantCount];
        PlayerWrapper pw = PlayerWrapper.getWrapper(p);

        if (!success) {
            pw.getPlayerGameStats().addStat(StatColumn.FAILED_ENCHANTS);

            if (enchantCount <= 8 && isProtected()) {
                setProtected(false);
                p.sendMessage(ChatColor.RED + "Your enchantment scroll " + ChatColor.UNDERLINE + "FAILED" + ChatColor.RED
                        + " but since you had white scroll protection, your item did not vanish.");
                return;
            }

            p.getWorld().playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2F, 1.25F);
            ParticleAPI.spawnParticle(Particle.LAVA, p.getLocation().add(0, 2.5, 0), 75, 1F);
            p.sendMessage(ChatColor.RED + "While dealing with magical enchants, your item VANISHED.");
            setDestroyed(true);
            return;
        }

        applyEnchantStats();

        this.enchantCount++;
        setProtected(false);

        // Play Effect
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.25F);
        Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).withFade(Color.YELLOW).with(FireworkEffect.Type.BURST).trail(true).build();
        fwm.addEffect(effect);
        fwm.setPower(0);
        fw.setFireworkMeta(fwm);
        pw.getPlayerGameStats().addStat(StatColumn.SUCCESSFUL_ENCHANTS);
    }

    /**
     * Repairs this item (DR Custom Durability, not vanilla.)
     */
    public void repair() {
        this.durability = MAX_DURABILITY;
    }

    /**
     * Subtracts durability from this item, and alerts the player if it reaches
     * a certain level. Supplied player is who should receive the damage
     * warning, if any.
     */
    public void damageItem(Player player, int durability) {
        this.durability -= durability;

        if (this.durability <= 1) {
            if (player == null)
                return;
            // Item has broken!
            player.getInventory().remove(getItem());
            setDestroyed(true); //We have to do this after so it can remove LOL
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1F,
                    1F);
            onItemBreak(player);
            player.updateInventory();
            return;
        }

        if (player == null)
            return;

        // Update the item in the player's inventory.
        ItemStack[] items = player.getInventory().getContents();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item != null && item.equals(getItem())) {
                //Damage?
                items[i] = generateItem();
                player.getInventory().setItem(i, generateItem());
            }
        }
        /*Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            player.updateInventory();
            OverrideListener.updatePlayersHatLocally(player);
        } , 2);*/

        double duraPercent = getDurabilityPercent();
        // Durability warnings.
        for (int i : DURABILITY_WARNINGS) {
            if (duraPercent <= i) {
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1F, 1F);
                player.sendMessage(ChatColor.RED + " **" + ChatColor.BOLD + (int) Math.ceil(duraPercent) + "% DURABILITY " + ChatColor.RED + "left on " + getItem().getItemMeta().getDisplayName() + ChatColor.RED + "**");
                break;
            }
        }
    }

    /**
     * Gets the gem cost of this item to repair via anvil.
     */
    public int getRepairCost() {
        double totalCost = (100 - getDurabilityPercent()) * getBaseRepairCost(); // Percentage Broken * Cost of 1 percentage
        totalCost *= getGeneratedItemType().getAttributeBank().getRepairMultiplier(getTier()); // Multiplier for this tier.
        totalCost *= getGeneratedItemType().getAttributeBank().getGlobalRepairMultiplier(); // Multiplier for this item type.
        return Math.max((int) Math.round(totalCost), 10); // Don't allow prices under 10 gems.
    }

    /**
     * Gets the durability value as a percent
     */
    public double getDurabilityPercent() {
        return (getDurability() / (double) MAX_DURABILITY) * 100D;
    }

    /**
     * Can this item be repaired right now?
     */
    public boolean canRepair() {
        return getDurability() < MAX_DURABILITY;
    }

    /**
     * Rolls the stats for this item.
     */
    public void rollStats(boolean isReroll) {
        if (this.attributes == null)
            this.attributes = new AttributeList();
        // Simulate random order.
        Collections.shuffle(ItemGenerator.modifierObjects);
        ItemMeta meta = getItem().getItemMeta();

        Map<ModifierCondition, ItemModifier> conditionMap = new HashMap<>();
        Random rand = ThreadLocalRandom.current();

        // ROLL STATS //
        for (ItemModifier im : ItemGenerator.modifierObjects) {
            // Is this applicable to the current item material?
            // In the future if we add t6 the generatedItemType system will need
            // to be changed.
            if (im.canApply(getItemType())) {
                ModifierCondition mc = im.tryModifier(meta, getTier(), getRarity());

                if (mc != null) {
                    attemptAddModifier(conditionMap, mc, im, rand, isReroll);
                }
            }
        }

        List<ModifierCondition> sortedStats = new ArrayList<>(conditionMap.keySet());

        for (ItemModifier modifier : conditionMap.values())
            for (ModifierCondition mc : conditionMap.keySet())
                if (!mc.checkCantContain(modifier.getClass()))
                    sortedStats.remove(mc);

        // Sort stats by priority
        Collections.sort(sortedStats, (mc1, mc2) -> conditionMap.get(mc1).compareTo(conditionMap.get(mc2)));

        Map<AttributeType, ModifierRange> keptAttributes = new HashMap<>();

        if (isReroll)
            for (AttributeType attribute : this.attributes.getAttributes())
                if (attribute.isIncludeOnReroll()) {
                    keptAttributes.put(attribute, this.attributes.get(attribute));
                    Bukkit.getLogger().info("Attribute kept: " + attribute.getNBTName());
                }
        this.attributes.clear();

        for (ModifierCondition mc : sortedStats) {
            ItemModifier im = conditionMap.get(mc);
            im.rollAttribute();

            // No duplicate attributes.
            if (this.attributes.containsKey(im.getCurrentAttribute()))
                continue;

            // GENERATE NEW STAT VALUE //
            ModifierRange range = mc.getRange();
            range.generateRandom();
            // Keep the old one if it's not supposed to get rerolled.
            if (keptAttributes.containsKey(im.getCurrentAttribute())) {
                Bukkit.getLogger().info("Keeping: " + im.getCurrentAttribute());
                range = keptAttributes.get(im.getCurrentAttribute());
            }

            // SAVE NEW STAT //
            this.attributes.put(im.getCurrentAttribute(), range.clone());
        }

    }

    private void attemptAddModifier(Map<ModifierCondition, ItemModifier> conditions, ModifierCondition mc, ItemModifier im, Random rand, boolean reRoll) {
        int belowChance = mc.getChance() < 0 ? im.getCurrentAttribute().getChance() : mc.getChance();

        boolean isHPRegen = this.attributes != null && this.attributes.hasAttribute(Item.ArmorAttributeType.HEALTH_REGEN);
        // Randomly add bonus.
        boolean hpConflict = isHPRegen && im.getCurrentAttribute() == Item.ArmorAttributeType.ENERGY_REGEN;
        if (im.getCurrentAttribute().isIncludeOnReroll() && reRoll && !hpConflict)
            conditions.put(mc, im);
        else if (rand.nextInt(100) < belowChance && !hpConflict)
            conditions.put(mc, im);


        if (mc.getBonus() != null)
            attemptAddModifier(conditions, mc.getBonus(), im, rand, reRoll);
    }

    public static boolean isCustomTool(ItemStack item) {
        return CombatItem.isCombatItem(item) || ProfessionItem.isProfessionItem(item);
    }

    /**
     * Handles adding durability back when a scrap is used.
     */
    public void scrapRepair() {
        double newDura = (double) MAX_DURABILITY * 0.03D;
        this.durability = Math.min(this.durability + (int) newDura, MAX_DURABILITY);
    }

    /**
     * Returns the repair particle id for being repaired by scrap.
     */
    public int getRepairParticle(ScrapTier tier) {
        return tier.getParticleId();
    }
}
