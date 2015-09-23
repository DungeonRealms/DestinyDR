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
    List<String> getRandomItemName(Armor.EquipmentType type) {
        List<String> name = new ArrayList<>();
        name.add(type.getName());
        name.add(middle.get(new Random().nextInt(middle.size() - 0) + 0));
        name.add(last.get(new Random().nextInt(last.size() - 0) + 0));
        return name;
    }

    /**
     * Returns Item names
     *
     * @param type
     * @return
     * @since 1.0
     */
    List<String> getRandomItemName(Item.ItemType type) {
        List<String> name = new ArrayList<>();
        name.add(type.getName());
        name.add(middle.get(new Random().nextInt(middle.size() - 0) + 0));
        name.add(last.get(new Random().nextInt(last.size() - 0) + 0));
        return name;
    }

    /**
     * Synonyms of
     */
    public List<String> middle = Arrays.asList(
            "Of",
            "The"
    );

    /**
     * Suffix words
     */
    public List<String> last = Arrays.asList(
            "Afrit",
            "Alfar",
            "Astomi",
            "Ant-Lion",
            "Anthropomorphous",
            "Barnacle-Goose",
            "Basilisk",
            "Bean Sidh",
            "Blemiyh",
            "Xwaffle",
            "Proxying",
            "xFinityPro",
            "Centaur",
            "Cerberus",
            "Ch`i Lin",
            "Cyclops",
            "Dwarf"
    );

}
