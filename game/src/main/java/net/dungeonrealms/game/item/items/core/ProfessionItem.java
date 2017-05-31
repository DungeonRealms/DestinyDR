package net.dungeonrealms.game.item.items.core;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.functional.ItemEnchantFishingRod;
import net.dungeonrealms.game.item.items.functional.ItemEnchantPickaxe;
import net.dungeonrealms.game.item.items.functional.ItemEnchantProfession;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.data.EnumBuff;
import net.dungeonrealms.game.mechanic.data.ProfessionTier;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.item.Item.ProfessionAttribute;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents profession items.
 *
 * @author Kneesnap
 */
@Getter
public abstract class ProfessionItem extends ItemGear {

    private int level;

    @Setter
    private int XP;

    public ProfessionItem(ItemStack item) {
        super(item);
    }

    public ProfessionItem(ItemType type) {
        this(type, 1);
    }

    public ProfessionItem(ItemType type, int level) {
        super(type);
        setLevel(level);
    }

    @Override
    public void loadItem() {
        setLevel(getTagInt("level"));
        setXP(getTagInt("xp"));
        super.loadItem();
    }

    @Override
    public void updateItem() {
        setTagInt("level", getLevel());
        setTagInt("xp", getXP());
        addLore();
        super.updateItem();
    }

    @Override
    protected String generateItemName() {
        return getTier().getColor() + (getLevel() == 100 ? "Grand " : "") + getProfessionTier().getItemName();
    }

    @Override
    public void onItemBreak(Player player) {
        player.sendMessage(ChatColor.RED + "Your tool bursts into white light, leaving behind its kin.");
        setLevel(1);
        setXP(0);
        setDestroyed(false);//TEST
        player.getInventory().addItem(generateItem());
    }

    public ProfessionItem setLevel(int i) {
        this.level = i;
        return this;
    }

    //Adds the level lore.
    private void addLore() {
        // Generate XP bar
        String expBar = "||||||||||||||||||||||||||||||||||||||||||||||||||";
        double percentDone = (100.0 * getXP()) / getNeededXP();
        int display = (int) (percentDone / 2D);
        display = Math.max(1, Math.min(display, 50));
        String formattedXPBar;
        if (percentDone == 0) {
            formattedXPBar = ChatColor.RED + expBar;
        } else {
            formattedXPBar = ChatColor.GREEN + expBar.substring(0, display) + ChatColor.RED + expBar.substring(display, expBar.length());
        }
        // Add Lore
        addLore("Level: " + getTier().getColor() + getLevel());
        addLore(getXP() + " / " + getNeededXP());
        addLore("EXP: " + formattedXPBar);
    }

    /**
     * Gets an enchant for this item. (Uses special cases, so it needs to be updated for every new profession item.)
     * We can modularize this later.
     */
    public ItemEnchantProfession getEnchant() {
        if (this instanceof ItemPickaxe) {
            return new ItemEnchantPickaxe((ItemPickaxe) this);
        } else if (this instanceof ItemFishingPole) {
            return new ItemEnchantFishingRod((ItemFishingPole) this);
        }
        Utils.log.info("Couldn't create enchant for profession item - " + getClass().getName());
        return null;
    }

    /**
     * Level up this item.
     */
    public void levelUp(Player p) {
        if (getLevel() >= 100)
            return;
        int newLevel = getLevel() + 1;

        if (newLevel == getNextTierLevel())
            Achievements.giveAchievement(p, getProfessionTier().getAchievement());

        ItemTier oldTier = getTier();
        setLevel(newLevel);
        setXP(0);

        //Apply new stat.
        if (getTier() != oldTier) {
            ProfessionAttribute[] attributes = (ProfessionAttribute[]) getGeneratedItemType().getAttributeBank().getAttributes();
            ProfessionAttribute pa = attributes[ThreadLocalRandom.current().nextInt(attributes.length)];
            int currentAmount = getAttributes().hasAttribute(pa) ? getAttributes().get(pa).getValue() : 0;
            int newAmount = pa.getRandomValueFromTier(getTier());
            int toUse = currentAmount >= newAmount ? currentAmount + 1 : newAmount;
            int maxStat = pa.getMaxFromTier(getTier());
            if(toUse > maxStat)toUse = maxStat;
            getAttributes().setStat(pa, toUse);
        }

        p.sendMessage(ChatColor.YELLOW + "Your " + getItemType().getNBT() + " has increased to level " + ChatColor.AQUA + getLevel());

        if (newLevel == 100) {
            p.sendMessage(ChatColor.YELLOW + "Congratulations! Your " + getItemType().getNBT() + " has reached " + ChatColor.UNDERLINE + "LVL 100"
                    + ChatColor.YELLOW + " this means you can no longer repair it. You now have TWO options.");
            p.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "(1) " + ChatColor.YELLOW + "You can exchange it at the merchant for an enchant scroll that will hold all the custom stats of your item and may be applied to another one.");
            p.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "(2) " + ChatColor.YELLOW + "If you continue to use this"
                    + " until it runs out of durability, it will become LVL 1 again"
                    + ", but it will retain all its custom stats.");
            p.sendMessage("");
        }

        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.25F);
        Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).withFade(Color.YELLOW).with(FireworkEffect.Type.BURST).trail(true).build();
        fwm.addEffect(effect);
        fwm.setPower(0);
        fw.setFireworkMeta(fwm);
    }

    public int getNextTierLevel() {
        return Math.min(((getLevel() / 20) + 1) * 20, 100);
    }

    @Override
    public ItemTier getTier() {
        return ItemTier.getByTier(getProfessionTier().getTier());
    }

    /**
     * Gives XP to this item.
     */
    public void addExperience(Player p, int xpGain) {
        int professionBuffBonus = 0;
        if (DonationEffects.getInstance().hasBuff(EnumBuff.PROFESSION)) {
            professionBuffBonus = Math.round(xpGain * DonationEffects.getInstance().getBuff(EnumBuff.PROFESSION).getBonusAmount() / 100f);
            xpGain += professionBuffBonus;
        }
        setXP(getXP() + xpGain);

        PlayerWrapper pw = PlayerWrapper.getWrapper(p);

        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

        pw.sendDebug(ChatColor.YELLOW.toString() + ChatColor.BOLD + "          +" + ChatColor.YELLOW + Math.round(xpGain - professionBuffBonus) + ChatColor.BOLD + " EXP"
                + ChatColor.YELLOW + ChatColor.GRAY + " [" + Math.round(getXP() - professionBuffBonus) + ChatColor.BOLD + "/" + ChatColor.GRAY + getNeededXP() + " EXP]");

        if (professionBuffBonus > 0) {
            pw.sendDebug(ChatColor.YELLOW.toString() + ChatColor.BOLD + "        " + ChatColor.GOLD
                    .toString() + ChatColor.BOLD + "PROF. BUFF >> " + ChatColor.YELLOW.toString() + ChatColor.BOLD
                    + "+" + ChatColor.YELLOW + Math.round(professionBuffBonus) + ChatColor.BOLD + " EXP " +
                    ChatColor.GRAY + "[" + getXP() + ChatColor.BOLD + "/" + ChatColor.GRAY + getNeededXP() + " EXP]");
        }

        if (getNeededXP() > 0 && getXP() > getNeededXP()) {
            int extraXP = getXP() - getNeededXP();
            levelUp(p);

            if (extraXP > 0 && getNeededXP() > 0)
                addExperience(p, extraXP);
        }

//        p.getEquipment().setItemInMainHand(generateItem());
    }

    /**
     * Return the XP needed to level up.
     */
    public int getNeededXP() {
        return getNeededXP(getLevel());
    }

    private static int getNeededXP(int level) {
        if (level <= 1)
            return 176;

        if (level == 100)
            return 0;

        int lastLevel = level - 1;
        return (int) (Math.pow(lastLevel, 2) + (lastLevel * 20) + 150 + (lastLevel * 4) + getNeededXP(lastLevel));
    }

    @Override //Profession items don't have rarity.
    public ItemRarity getRarity() {
        return null;
    }

    @Override
    protected double getBaseRepairCost() {
        return Math.pow(getLevel(), 2) / 1000;
    }

    public static boolean isProfessionItem(ItemStack item) {
        return ItemFishingPole.isFishingPole(item) || ItemPickaxe.isPickaxe(item);
    }

    @Override
    protected void applyEnchantStats() {
        //The stats are applied by the enchants themselves since they contain the data.
    }

    /**
     * Calls when this item levels up.
     */
    public abstract ProfessionTier getProfessionTier();
}
