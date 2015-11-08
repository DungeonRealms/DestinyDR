package net.dungeonrealms.items;

import net.dungeonrealms.items.armor.Armor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Nick on 9/19/2015.
 */
public class NameGenerator {

    //TODO: Redo this class with proper names, and add options for Armor Sets.

    /**
     * Gets a random item named with prefix of ItemType{Axe, Sword, etc...}
     *
     * @param type
     * @return
     * @since 1.0
     */
    public List<String> next(Item.ItemType type) {
        return getRandomItemName(type);
    }

    /**
     * @param type
     * @return
     */
    public List<String> next(Armor.EquipmentType type) {
        return getRandomItemName(type);
    }

    /**
     * Returns Armor Name
     *
     * @param type
     * @return
     * @since 1.0
     */
    private List<String> getRandomItemName(Armor.EquipmentType type) {
        List<String> name = new ArrayList<>();
        name.add(type.getName());
        name.add(middle.get(new Random().nextInt(middle.size())));
        name.add(last.get(new Random().nextInt(last.size())));
        return name;
    }

    /**
     * Returns Item names
     *
     * @param type
     * @return
     * @since 1.0
     */
    private List<String> getRandomItemName(Item.ItemType type) {
        List<String> name = new ArrayList<>();
        name.add(type.getName());
        name.add(middle.get(new Random().nextInt(middle.size())));
        name.add(last.get(new Random().nextInt(last.size())));
        return name;
    }

    /**
     * Synonyms of
     */
    private List<String> middle = Arrays.asList(
            "Of",
            "Of The"
    );

    /**
     * Suffix words
     */
    private List<String> last = Arrays.asList(
            "Afrit",
            "Alfar",
            "Astomi",
            "Ant-Lion",
            "Anthropomorphous",
            "Barnacle-Goose",
            "Basilisk",
            "Bean Sidh",
            "Blemiyh",
            "Centaur",
            "Cerberus",
            "Ch`i Lin",
            "Cyclops",
            "Dwarf",
            "Camelot",
            "Hood",
            "Robin",
            "Alba",
            "Aang",
            "Katara",
            "Darkness",
            "Light",
            "Balance"
    );

}
