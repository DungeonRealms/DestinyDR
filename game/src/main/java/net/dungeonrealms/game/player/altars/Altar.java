package net.dungeonrealms.game.player.altars;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.altars.recipes.AbstractRecipe;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
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
            if(!(recipeTicks >= foundRecipe.getRitualTime()-1)) {
                for (int x = 1; x < altarType.getNodeSize(); x++) {
                    Altar altar = AltarManager.getAltar(altarType);
                    if (altar.hasActiveStack(x)) {
                        playEffect(x);
                    }
                }
            }
            recipeTicks++;
            lastRecipeTick = System.currentTimeMillis();
        }

        if(recipeTicks >= foundRecipe.getRitualTime()) {
            foundRecipe.giveReward(using);
            using.playSound(using.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1f, 1f);
            using.playSound(using.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
            using.sendMessage(ChatColor.GREEN + "You have received a " + foundRecipe.getRewardDisplayName());
            AltarManager.removeAltar(this, false);
        }
    }

    public void handleFoundRecipe(AbstractRecipe recipe) {
        foundRecipe = recipe;
    }

    public void giveDebuff() {

    }

    public void playEffect(int nodeIndex){
        //System.out.println("Playing Effect at Node " + nodeIndex);
        Location nodeLoc = altarType.getNode(nodeIndex);
        Location centerLoc = new Location(altarType.getWorld(), (Math.abs(altarType.getNode(0).getX()) / altarType.getNode(0).getX()) * (Math.abs(altarType.getNode(0).getX()) - 0.5), altarType.getNode(0).getY(), (Math.abs(altarType.getNode(0).getZ()) / altarType.getNode(0).getZ()) * (Math.abs(altarType.getNode(0).getZ()) + 0.5));
        new BukkitRunnable() {
            double o = 5.951; //For Odd
            double e = 4.05; //For Even
            @Override
            public void run() {
                double x, y, z;
                x = 0;
                y = 0;
                z = 0;
                o -= 0.1;
                e -= 0.1;
                //TODO: Find a way to simplify this headache
                if(nodeIndex % 2 != 0) {
                    for (double i = 5.951; i >= o; i -= 0.1) {
                        if (nodeIndex == 1 || nodeIndex == 5) {
                            if (nodeIndex == 1) {
                                x = 0;
                                z = -i;
                                y = -0.15 * Math.pow((z + 2.3), 2) + 2;
                            } else if (nodeIndex == 5) {
                                x = 0;
                                z = i;
                                y = -0.15 * Math.pow((z - 2.3), 2) + 2;
                            }
                            centerLoc.add(x, y, z);
                            altarType.getWorld().spigot().playEffect(centerLoc, Effect.FLYING_GLYPH, 0, 0, 0, 0, 0, 0, 1, 25);
                            centerLoc.subtract(x, y, z);
                            if (i <= 0.1) {
                                this.cancel();
                            }
                        }
                        if (nodeIndex == 3 || nodeIndex == 7) {
                            if (nodeIndex == 3) {
                                x = i;
                                z = 0;
                                y = -0.15 * Math.pow((x - 2.3), 2) + 2;
                            } else if (nodeIndex == 7) {
                                x = -i;
                                z = 0;
                                y = -0.15 * Math.pow((x + 2.3), 2) + 2;
                            }
                            centerLoc.add(x, y, z);
                            altarType.getWorld().spigot().playEffect(centerLoc, Effect.FLYING_GLYPH, 0, 0, 0, 0, 0, 0, 1, 25);
                            centerLoc.subtract(x, y, z);
                            if (i <= 0.1) {
                                this.cancel();
                            }
                        }
                    }
                }
                else{
                    for (double i = 4.05; i >= e; i -= 0.1) {
                        if ((nodeIndex % 2) == 0) {
                            if (nodeIndex == 2) {
                                x = i;
                                z = i;
                                y = -0.33 * Math.pow((x - 1.6), 2) + 2;
                            }
                            else if (nodeIndex == 4) {
                                x = i;
                                z = -i;
                                y = -0.33 * Math.pow((x - 1.6), 2) + 2;
                            }
                            else if (nodeIndex == 6) {
                                x = -i;
                                z = i;
                                y = -0.33 * Math.pow((x + 1.6), 2) + 2;
                            }
                            else if (nodeIndex == 8) {
                                x = -i;
                                z = -i;
                                y = -0.33 * Math.pow((x + 1.6), 2) + 2;
                            }
                            centerLoc.add(x, y, z);
                            altarType.getWorld().spigot().playEffect(centerLoc, Effect.FLYING_GLYPH, 0, 0, 0, 0, 0, 0, 1, 25);
                            centerLoc.subtract(x, y, z);
                            if (i <= 0.1) {
                                this.cancel();
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(DungeonRealms.getInstance(), 0, 1);
    }



}
