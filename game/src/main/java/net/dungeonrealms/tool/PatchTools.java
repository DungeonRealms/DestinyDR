package net.dungeonrealms.tool;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
    
    public int getSize(){
    	try{
    		InputStream fileIn = DungeonRealms.getInstance().getResource("patchnotes.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(fileIn));
            int len = 0;
            while(in.read() != -1)
            	len++;
            return len;
    	}catch(Exception e){
    		
    	}
    	return 0;
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
            int curCharacters = 0;

            StringBuilder builder = new StringBuilder();

            while ((str = in.readLine()) != null) {
                curCharacters += 2; // line breaks count as 2 characters
                String text = ChatColor.translateAlternateColorCodes('&', str.replace("<build>", Constants.BUILD_NUMBER));

                if (text.trim().length() > 0)
                    curCharacters += text.length();

                if (curCharacters >= 180) {
                    curCharacters = 0;
                    pages.add(builder.toString());
                    builder = new StringBuilder();
                }

                // APPEND BOOK PAGE ///
                builder.append(text).append("\n");
            }

            if (curCharacters < 180)
                pages.add(builder.toString());

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ItemStack patchBook = ItemManager.createItem(Material.WRITTEN_BOOK,
                ChatColor.GOLD.toString() + ChatColor.BOLD + "Patch Notes for Build " + Constants.BUILD_NUMBER, new String[]{});
        BookMeta bm = (BookMeta) patchBook.getItemMeta();

        bm.setAuthor("DungeonRealms Development Team");
        bm.setPages(pages);

        patchBook.setItemMeta(bm);

        this.patchBook = patchBook;
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
