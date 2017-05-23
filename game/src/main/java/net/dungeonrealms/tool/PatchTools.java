package net.dungeonrealms.tool;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.minecraft.server.v1_9_R2.ChatBaseComponent;
import net.minecraft.server.v1_9_R2.IChatBaseComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftMetaBook;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/9/2016
 */
public class PatchTools implements GenericMechanic {

    @Getter
    private static ItemStack patchBook;

    public static int getSize() {
        try {
            InputStream fileIn = DungeonRealms.getInstance().getResource("patchnotes.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(fileIn));
            int len = 0;
            while (in.read() != -1)
                len++;
            return len;
        } catch (Exception e) {

        }
        return 0;
    }

    @Override
    public void startInitialization() {

        // BOOK PAGES //
        List<IChatBaseComponent> pages = new ArrayList<>();

        try {
            // READ PATCH NOTE FILE //
            InputStream fileIn = DungeonRealms.getInstance().getResource("patchnotes.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(fileIn));

            String str;
            int curCharacters = 0;

            StringBuilder builder = new StringBuilder();
            JSONMessage message = new JSONMessage(" ", ChatColor.BLACK);
            while ((str = in.readLine()) != null) {
                curCharacters += 2; // line breaks count as 2 characters
                String text = ChatColor.BLACK + ChatColor.translateAlternateColorCodes('&', str.replace("<build>", Constants.BUILD_NUMBER));

                if (curCharacters >= 180) {
                    curCharacters = 0;
                    pages.add(ChatBaseComponent.ChatSerializer.a(message.toString()));
                    message = new JSONMessage(" ", ChatColor.BLACK);
                    builder = new StringBuilder();
                }


                // APPEND BOOK PAGE ///
                if (text.contains("<h>") && text.contains("</h>")) {
                    String[] split = text.split("\\<h>");
                    String hoverText = split[1].split("</h>")[0];
                    text = split[0];
                    System.out.println("Page: " + hoverText + " Text: " + text);
                    message.addHoverText(Lists.newArrayList(hoverText), text + "\n");
                } else {
                    message.addText(text + "\n", ChatColor.BLACK);
                }

                if (text.trim().length() > 0)
                    curCharacters += text.length();

                builder.append(text);
                builder.append("\n");
            }

            if (curCharacters < 180)
                pages.add(ChatBaseComponent.ChatSerializer.a(message.toString()));

            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        patchBook = ItemManager.createItem(Material.WRITTEN_BOOK,
                ChatColor.GOLD.toString() + ChatColor.BOLD + "Patch Notes for Build " + Constants.BUILD_NUMBER, new String[]{});
        BookMeta bm = (BookMeta) patchBook.getItemMeta();

        CraftMetaBook meta = (CraftMetaBook) bm;
        bm.setAuthor("DungeonRealms Development Team");

        meta.pages = pages;
//        bm.setPages(pages);

        patchBook.setItemMeta(meta);
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
