package net.dungeonrealms.mastery;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

/**
 * Created by Nick on 9/24/2015.
 */
public class ItemSerialization {

    /**
     * Converts an Inventory to a string
     *
     * @param i
     * @return String
     * @since 1.0
     */

    public static String toString(Inventory i) {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("Title", i.getTitle());
        configuration.set("Size", i.getSize());
        for (int a = 0; a < i.getSize(); a++) {
            ItemStack s = i.getItem(a);
            if (s == null) {
                s = new ItemStack(Material.AIR, 1);
            }
            configuration.set("Contents." + a, s);
        }
        return Base64Coder.encodeString(configuration.saveToString());
    }


    /**
     * Converts String to an Inventory.
     *
     * @param s
     * @return Inventory
     * @since 1.0
     */
    public static Inventory fromString(String s) {
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.loadFromString(Base64Coder.decodeString(s));
            Inventory i = Bukkit.createInventory(null, configuration.getInt("Size"), configuration.getString("Title"));
            ConfigurationSection contents = configuration.getConfigurationSection("Contents");
            contents.getKeys(false).stream().filter(index -> contents.getItemStack(index) != null).forEach(index -> {
                i.setItem(Integer.parseInt(index), contents.getItemStack(index));
            });
            return i;
        } catch (InvalidConfigurationException e) {
            return null;
        }
    }

}
