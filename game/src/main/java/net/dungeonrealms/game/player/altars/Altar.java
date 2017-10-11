package net.dungeonrealms.game.player.altars;

import lombok.Getter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.player.altars.recipes.AbstractRecipe;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * Created by Rar349 on 8/3/2017.
 */
public class Altar {

    @Getter
    private Player using;
    @Getter
    private Altars altarType;
    @Getter
    private ItemStack[] nodeStacks;
    @Getter
    private Item[] nodeDisplayStacks;

    private AbstractRecipe foundRecipe = null;
    private long lastRecipeTick = 0L;
    private int recipeTicks = 0;

    public Altar(Player using, Altars altar) {
        this.using = using;
        this.altarType = altar;
        this.nodeStacks = new ItemStack[altarType.getNodeSize()];
        this.nodeDisplayStacks = new Item[altarType.getNodeSize()];
    }

    public void setItemStack(int index, ItemStack stack) {
        nodeStacks[index] = stack;
        Location loc = altarType.getNode(index);
        Item displayStack = altarType.getWorld().dropItem(loc.clone().add(0.5,1,0.5), new ItemStack(stack.getType(), stack.getAmount(), stack.getDurability()));
        displayStack.setPickupDelay(Integer.MAX_VALUE);
        displayStack.teleport(loc.clone().add(0.5,1,0.5));
        displayStack.setVelocity(new Vector());
        nodeDisplayStacks[index] = displayStack;
        if(index == 0) checkRecipes();
    }


    public boolean hasActiveStack(int index) {
        return getItemStack(index) != null;
    }

    public ItemStack getItemStack(int nodeIndex) {
        return nodeStacks[nodeIndex];
    }


    public void checkRecipes() {
        for(AltarRecipes recipes : AltarRecipes.values()) {
            AbstractRecipe recipe = recipes.getRecipe();
            if(recipe.checkRecipe(getAltarType())) {
                //altarType.getWorld().strikeLightningEffect(getAltarType().getCenterLocation());
                //recipe.giveReward(using);
                //using.sendMessage(ChatColor.GREEN + "You have received a " + recipe.getRewardDisplayName());
                //AltarManager.removeAltar(this, false);
                handleFoundRecipe(recipe);
                return;
            }
        }

        giveDebuff();
        AltarManager.removeAltar(this, true);

    }

    public void tick() {
        if(foundRecipe == null) return;
        if(System.currentTimeMillis() - lastRecipeTick > 5000) {
            GameAPI.playLightningEffect(altarType.getWorld(), altarType.getNode(0), 250);
            recipeTicks++;
            lastRecipeTick = System.currentTimeMillis();
        }

        if(recipeTicks >= foundRecipe.getRitualTime()) {
            foundRecipe.giveReward(using);
            using.sendMessage(ChatColor.GREEN + "You have received a " + foundRecipe.getRewardDisplayName());
            AltarManager.removeAltar(this, false);
        }
    }

    public void handleFoundRecipe(AbstractRecipe recipe) {
        foundRecipe = recipe;
    }

    public void giveDebuff() {

    }



}
