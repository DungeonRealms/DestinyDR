package net.dungeonrealms.game.item.items.core.setbonus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.common.game.util.ChatColor;

import java.util.Arrays;

@AllArgsConstructor
public enum SetBonuses {

    CARROT_FARMER("old_carrot_farmer", "Carrot Farmer", ChatColor.GREEN + "Green Thumb", new AbstractSetBonus("Farmers food is twice as effective.")),
    WHEAT_FARMER("old_wheat_farmer", "Wheat Farmer", ChatColor.GREEN + "Bakers Boy", new AbstractSetBonus("Bread is twice as filling.")),
    QUEEN_OF_THE_NEST("queen_of_the_nest", "Queen of the Nest", ChatColor.DARK_GREEN + "Poison Fang", new AbstractEnergyBonus("3% Energy + Randomly Apply Poison DOT", 3)),
    REDEYE_THE_CRUEL("redeye_the_cruel", "Redeye The Cruel", ChatColor.RED + "Bloodshed", new AbstractSetBonus("Randomly apply bleed to Monsters")),
    PYRO_BANDIT("pyro_bandit", "Pyro Bandit", ChatColor.RED + "Massacre", new AbstractEnergyBonus("5% Energy + Chance to apply AOE on hit", 5)),
    BLOOD_BUTCHER("blood_butcher", "Blood Butcher", ChatColor.RED + "Blood Frenzy", new AbstractEnergyBonus("4% Energy + When less then 20% HP, gain 20% Lifesteal", 4)),
    LIBRARIAN("librarian", "Librarian", ChatColor.YELLOW + "Book Finder", new AbstractSetBonus("Increased TP Book Drop Chance"));

    @Getter
    String customID;

    @Getter
    private String displayName;

    @Getter
    private String bonusName;

    @Getter
    SetBonus setBonus;


    public static SetBonuses getFromCustomID(String name) {
        return Arrays.stream(values()).filter(e -> e.getCustomID().equals(name)).findFirst().orElse(null);
    }
}
