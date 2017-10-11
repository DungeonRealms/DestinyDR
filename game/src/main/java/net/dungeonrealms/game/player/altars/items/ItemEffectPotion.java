package net.dungeonrealms.game.player.altars.items;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.item.items.functional.cluescrolls.ClueDifficulty;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Rar349 on 8/4/2017.
 */
public abstract class ItemEffectPotion extends FunctionalItem implements ItemClickEvent.ItemClickListener {

    protected PotionEffectType effect;
    protected int time;
    protected int weight;

    public ItemEffectPotion(PotionEffectType effect, int time, int weight, ItemType type) {
        super(type);
        this.effect = effect;
        this.time = time;
        this.weight = weight;
    }

    public ItemEffectPotion(ItemStack stack) {
        super(stack);

        if (hasTag("effect")) this.effect = PotionEffectType.getByName(getTagString("effect"));

        if (hasTag("time")) this.time = getTagInt("time");

        if (hasTag("weight")) this.weight = getTagInt("weight");
    }

    public void onClick(ItemClickEvent evt) {
            evt.setUsed(true);
            evt.getPlayer().addPotionEffect(effect.createEffect(time,weight));
    }


    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.POTION);
    }

    @Override
    protected ItemUsage[] getUsage() {
        return INTERACT_RIGHT_CLICK;
    }
}
