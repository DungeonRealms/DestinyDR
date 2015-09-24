/**
 * 
 */
package net.dungeonrealms.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mastery.ItemSerialization;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumOperators;

/**
 * Created by Chase on Sep 22, 2015
 */
public class CommandSet implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
		if (s instanceof ConsoleCommandSender)
			return false;
		Player player = (Player) s;
		if (args.length > 0) {
			switch (args[0]) {
			case "level":
			int lvl = Integer.parseInt(args[1]);
			DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "info.netLevel", lvl);
			s.sendMessage("Level set to " + lvl);
			case "gems":
			int gems = Integer.parseInt(args[1]);
			DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "info.gems", gems);
			s.sendMessage("Gems set to " + gems);
			case "save":
			if (player.getItemInHand() != null) {
				ArrayList<ItemStack> items = new ArrayList<>();
				items.add(player.getItemInHand());

				InputStream inputStream = null;
				OutputStream outputStream = null;

				try {
					File file = new File(DungeonRealms.getInstance().getDataFolder() + "//file.json");
					file.createNewFile();
					String text = ItemSerialization.serialize(items);
					inputStream = IOUtils.toInputStream(text, "UTF-8");
					// read this file into InputStream
					outputStream = new FileOutputStream(file);

					int read = 0;
					byte[] bytes = new byte[1024];

					while ((read = inputStream.read(bytes)) != -1) {
						outputStream.write(bytes, 0, read);
					}

					System.out.println("Done!");

				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					player.setItemInHand(null);
					if (inputStream != null) {
						try {
						inputStream.close();
						} catch (IOException e) {
						e.printStackTrace();
						}
					}
					if (outputStream != null) {
						try {
						// outputStream.flush();
						outputStream.close();
						} catch (IOException e) {
						e.printStackTrace();
						}

					}
				}

			}
			case "load":
			File file = new File(DungeonRealms.getInstance().getDataFolder() + "//file.json");
			try {
				InputStream inputStream = new FileInputStream(file);
				String source = IOUtils.toString(inputStream, "UTF-8");
				List<ItemStack> items = ItemSerialization.deserialize(source);
				for (int i = 0; i < items.size(); i++) {
					if (items.get(i) != null) {
						player.getInventory().addItem(items.get(i));
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
		}
		return true;
	}
}
