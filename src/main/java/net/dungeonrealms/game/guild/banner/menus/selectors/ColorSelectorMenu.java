package net.dungeonrealms.game.guild.banner.menus.selectors;

import net.dungeonrealms.game.gui.item.GUIButton;
import net.dungeonrealms.game.gui.GUIButtonClickEvent;
import net.dungeonrealms.game.guild.banner.AbstractMenu;
import net.dungeonrealms.game.miscellaneous.ColorConverter;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.function.Consumer;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/11/2016
 */

public class ColorSelectorMenu extends AbstractMenu {

    public ColorSelectorMenu(Player player, Consumer<DyeColor> callback) {
        super("Select a color", 27, player.getUniqueId());
        setDestroyOnExit(true);

        for (DyeColor dye : DyeColor.values()) {
            GUIButton button = new GUIButton(Material.INK_SACK, dye.getDyeData()) {
                @Override
                public void action(GUIButtonClickEvent event) throws Exception {
                    Bukkit.getScheduler().runTask(plugin, () -> callback.accept(dye));
                }
            };

            button.setDisplayName(ColorConverter.dyeToChat(dye) + WordUtils.capitalizeFully(dye.name().replace("_", " ")));
            button.setLore(Collections.singletonList("&7Click here to select this color."));

            set(getSize(), button);
        }


    }


    @Override
    public void open(Player player)  {
        player.openInventory(inventory);
    }


}
