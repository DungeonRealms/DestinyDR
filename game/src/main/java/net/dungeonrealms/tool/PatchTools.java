package net.dungeonrealms.tool;

import lombok.Getter;
import net.dungeonrealms.Constants;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.player.json.JSONMessage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/9/2016
 */

public class PatchTools implements GenericMechanic {

    @Getter
    private ItemStack patchBook;

    private static PatchTools instance;

    public static PatchTools getInstance() {
        if (instance == null) {
            instance = new PatchTools();
        }
        return instance;
    }

    @Override
    public void startInitialization() {

        // BOOK PAGES //
        List<String> pages = new ArrayList<>();

        try {
            // READ PATCH NOTE FILE //
            InputStream fileIn = DungeonRealms.getInstance().getResource("patchnotes.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(fileIn));

            String str;
            int line = 0;

            StringBuilder builder = new StringBuilder();

            while ((str = in.readLine()) != null) {

                if (line >= 12) {
                    line = 0;
                    pages.add(builder.toString());
                }

                // APPEND BOOK PAGE ///
                builder.append(ChatColor.translateAlternateColorCodes('&', str.replace("<build>", Constants.BUILD_NUMBER))).append("\n");
                line++;
            }

            if (line < 12)
                pages.add(builder.toString());

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ItemStack patchBook = ItemManager.createItem(Material.WRITTEN_BOOK,
                ChatColor.GOLD.toString() + ChatColor.BOLD + "Patch Notes for " + Constants.BUILD_VERSION + " Build " + Constants.BUILD_NUMBER, new String[]{});
        BookMeta bm = (BookMeta) patchBook.getItemMeta();

        bm.setAuthor("DungeonRealms Development Team");
        bm.setPages(pages);

        patchBook.setItemMeta(bm);

        this.patchBook = patchBook;
    }

    public void doLogin(Player player) {
        final JSONMessage normal = new JSONMessage(ChatColor.GREEN + "*" + ChatColor.GOLD + " Patch notes available! " + ChatColor.GRAY +
                "To view patch notes click ", ChatColor.WHITE);
        normal.addRunCommand(ChatColor.GREEN.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE!", ChatColor.GREEN, "/patch");
        normal.addText(ChatColor.GREEN + "*");
        normal.sendToPlayer(player);
    }


    @Override
    public void stopInvocation() {
        // DO NOTHING
    }


    // WE WANT THIS TO LOAD FIRST //
    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }

}
