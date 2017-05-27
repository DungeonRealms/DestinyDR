package net.dungeonrealms.game.item.items.functional;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemClickEvent.ItemClickListener;
import net.dungeonrealms.game.mechanic.data.FishingTier;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Fishing.EnumFish;
import net.dungeonrealms.game.profession.fishing.FishBuff;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ItemFish extends FunctionalItem implements ItemClickListener {

    @Getter
    @Setter
    private boolean cooked;

    @Getter
    @Setter
    private EnumFish fishType;

    @Getter
    @Setter
    private FishBuff fishBuff;

    @Getter
    @Setter
    private FishingTier tier;

    public ItemFish(FishingTier tier, EnumFish fishType) {
        super(ItemType.FISH);
        setTier(tier);
        setFishType(fishType);
        setTagString("fishType", fishType.name());

        if (getTier().getBuffChance() >= ThreadLocalRandom.current().nextInt(100))
            setFishBuff(Fishing.getRandomBuff(getTier()));

        setAntiDupe(false);
    }

    public ItemFish(ItemStack item) {
        super(item);

        setCooked(getTagBool("cooked"));
        setTier(FishingTier.values()[getTagInt(TIER) - 1]);
        if (hasTag("buffType"))
            setFishBuff(Fishing.loadBuff(getTag()));

        if (hasTag("fishType"))
            setFishType(EnumFish.valueOf(getTagString("fishType")));

        setAntiDupe(false);
    }

    @Override
    public void updateItem() {
        setTagBool("cooked", isCooked());
        setTagInt(TIER, getTier().getTier());

        if (getFishType() != null)
            setTagString("fishType", getFishType().name());

        if (getFishBuff() != null)
            getFishBuff().save(getTag());
        super.updateItem();
    }

    @Override
    public void onClick(ItemClickEvent evt) {
        Player p = evt.getPlayer();

        //  Cook.
        if (!isCooked() && evt.hasBlock() && isCookable(evt.getClickedBlock().getType())) {
            if (evt.getClickedBlock().getState() instanceof Furnace) //Activate the furnace. (Cosmetic)
                ((Furnace) evt.getClickedBlock().getState()).setBurnTime((short) 20);
            p.playSound(p.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1, 1);
            setCooked(true);
            //Dont wipe the stack plzzzzzzz
            int stackAmount = evt.getItem().getItem().getAmount();
            ItemStack stack = generateItem();
            stack.setAmount(stackAmount);
            evt.setResultItem(stack);
            return;
        }

        if (!isCooked()) {
            p.sendMessage(ChatColor.RED + "You must cook this fish before you can eat it!");
            return;
        }

        if (p.getFoodLevel() >= 20) {
            //Dont let them eat!
            if (getFishBuff() == null) {
                evt.setCancelled(true);
                p.sendMessage(ChatColor.RED + "You must be hungry to eat this!");
                return;
            }
        }
        evt.setUsed(true);
        if (getFishBuff() != null)
            getFishBuff().applyBuff(evt.getPlayer());

        p.setFoodLevel(p.getFoodLevel() + getTier().getHungerAmount() / 5);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 1F, 1F);
        Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 1F, 1.5F), 4L);
    }

    public boolean isCookable(Material m) {
        return m == Material.FURNACE || m == Material.BURNING_FURNACE
                || m == Material.FIRE || m == Material.LAVA
                || m == Material.STATIONARY_LAVA || m == Material.TORCH;
    }

    @Override
    protected String getDisplayName() {
        FishBuff buff = getFishBuff();
        String name = buff != null ? buff.getItemName(getFishType()) : " " + getFishType().getName();
        return getTier().getColor() + (isCooked() ? "Grilled" : "Raw") + name;
    }

    @Override
    protected String[] getLore() {
        List<String> lore = new ArrayList<>();
        if (getFishBuff() != null)
            lore.add(getFishBuff().getLore());
        lore.add(ChatColor.RED + "-" + getTier().getHungerAmount() + "% HUNGER");
        lore.add(ChatColor.ITALIC + (isCooked() ? getFishType().getDesciption() : "A freshly caught fish."));
        return lore.toArray(new String[lore.size()]);
    }

    @Override
    protected ItemUsage[] getUsage() {
        return INTERACT_RIGHT_CLICK;
    }

    @Override
    protected ItemStack getStack() {
        ItemStack fish = new ItemStack(isCooked() ? Material.COOKED_FISH : Material.RAW_FISH);
        if (getFishBuff() != null)
            fish.setDurability((short) getFishBuff().getBuffType().getFishMeta());
        return fish;
    }
}
