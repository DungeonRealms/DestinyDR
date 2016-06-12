package net.dungeonrealms.game.guild.banner.menus;

import net.dungeonrealms.game.gui.GUIButtonClickEvent;
import net.dungeonrealms.game.gui.item.GUIButton;
import net.dungeonrealms.game.gui.item.GUIDisplayer;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.guild.banner.AbstractMenu;
import net.dungeonrealms.game.guild.banner.menus.selectors.ColorSelectorMenu;
import net.dungeonrealms.game.guild.banner.menus.selectors.PatternSelectorMenu;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.Collections;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/11/2016
 */


public class BannerCreatorMenu extends AbstractMenu {


    public BannerCreatorMenu(Player player, GuildMechanics.GuildCreateInfo info) {
        super("Create a banner for your guild!", 45, player.getUniqueId());
        setDestroyOnExit(true);

        final ItemStack currentBanner = info.getCurrentBanner();
        BannerMeta bannerMeta = (BannerMeta) currentBanner.getItemMeta();


        GUIDisplayer bannerPreview = new GUIDisplayer(currentBanner);
        bannerPreview.getItemStack().setItemMeta(bannerMeta);
        bannerPreview.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Preview");
        bannerPreview.setLore(Collections.singletonList("&7This is how your banner will appear."));

        set(4, bannerPreview);

        GUIButton setBaseColor = new GUIButton(Material.INK_SACK, (byte) 1) {
            @Override
            public void action(GUIButtonClickEvent event) throws Exception {
                new ColorSelectorMenu(event.getWhoClicked(), BannerCreatorMenu.this, null).open(event.getWhoClicked());
            }
        };

        setBaseColor.setDisplayName(ChatColor.GREEN + "Set base color.");
        setBaseColor.setLore(Collections.singletonList("&7Click here to set your desired base color."));
        set(20, setBaseColor);

        GUIButton addPattern = new GUIButton(Material.WORKBENCH) {
            @Override
            public void action(GUIButtonClickEvent event) throws Exception {
                new PatternSelectorMenu(event.getWhoClicked()).open(event.getWhoClicked());
            }
        };

        addPattern.setDisplayName(ChatColor.GREEN + "Add a pattern.");
        addPattern.setLore(Collections.singletonList("&7Click here to add pattern to your banner."));
        set(22, addPattern);


        GUIButton reset = new GUIButton(Material.PAPER) {
            @Override
            public void action(GUIButtonClickEvent event) throws Exception {
                for (int i = 0; i < bannerMeta.getPatterns().size(); i++)
                    bannerMeta.removePattern(i);

                bannerMeta.setBaseColor(DyeColor.WHITE);
                currentBanner.setItemMeta(bannerMeta);
                set(4, bannerPreview);
            }
        };

        reset.setDisplayName(ChatColor.RED + "Reset.");
        reset.setLore(Collections.singletonList("&7Click here to start over again."));
        set(24, reset);


        GUIButton createBanner = new GUIButton(Material.INK_SACK, (byte) 10) {
            @Override
            public void action(GUIButtonClickEvent event) throws Exception {
                GuildMechanics.getInstance().createGuild(player, info);
            }
        };

        createBanner.setDisplayName(ChatColor.GREEN + "Create Banner");
        createBanner.setLore(Collections.singletonList("&7Click here to create your guild"));
        set(40, createBanner);

        fillEmptySpaces(getSpaceFillerItem());
    }

    @Override
    public void open(Player player) {
        player.openInventory(inventory);
    }

}
