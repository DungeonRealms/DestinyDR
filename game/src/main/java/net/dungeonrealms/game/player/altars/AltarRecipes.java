package net.dungeonrealms.game.player.altars;

import lombok.SneakyThrows;
import net.dungeonrealms.game.player.altars.recipes.AbstractRecipe;
import net.dungeonrealms.game.player.altars.recipes.impl.*;
import org.bukkit.inventory.Recipe;

/**
 * Created by Rar349 on 8/3/2017.
 */
public enum AltarRecipes {
    T3_DPS(T3RecipeDPS.class),
    T4_DPS(T4RecipeDPS.class),
    T5_DPS(T5RecipeDPS.class),

    T3_ARMOR(T3RecipeArmor.class),
    T4_ARMOR(T4RecipeArmor.class),
    T5_ARMOR(T5RecipeArmor.class),

    FIRE_RESIST(RecipeFireResist.class),
    SPEED(RecipeSpeed.class),
    HASTE(RecipeHaste.class),
    WATER_BREATHING(RecipeWaterBreathing.class),
    NIGHT_VISION(RecipeNightVision.class);

    Class clas;
    AltarRecipes(Class<? extends AbstractRecipe> clas) {
        this.clas = clas;
    }

    @SneakyThrows
    public AbstractRecipe getRecipe() {
        return (AbstractRecipe) clas.newInstance();
    }
}
