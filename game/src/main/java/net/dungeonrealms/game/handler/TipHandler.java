package net.dungeonrealms.game.handler;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.PlayerToggles.Toggles;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * TipHandler - Announces tips regularly.
 * 
 * Redone May 8th, 2017.
 * @author Kneesnap
 */
public class TipHandler implements GenericMechanic {
	private static List<String> tips = new ArrayList<>();
	
	@Override
	public EnumPriority startPriority() {
		return EnumPriority.POPE;
	}
	
	@Override
	public void startInitialization() {
		Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), TipHandler::showTip, 8000L, 6000L);
	}
	
	@Override
	public void stopInvocation() {
		
	}
	
	public static void showTip() {
		String tip = getRandomTip();
		
		Bukkit.getOnlinePlayers().stream().filter(player -> {
			PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
			return wrapper != null && wrapper.getToggles().getState(Toggles.TIPS);
		}).forEach(player -> player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + ">>" + ChatColor.YELLOW + " TIP - " + ChatColor.GRAY + tip));
	}
	
	private static String getRandomTip() {
		if (tips.isEmpty()) // We've cycled through them all, reload them.
			loadTips();
		
		return tips.remove(new Random().nextInt(tips.size()));
	}
	
	private static void loadTips() {
		try {
			File file = new File("plugins/DungeonRealms/tips/tips.txt");
			if (!file.exists())
				file.createNewFile();
			
			tips = Files.readAllLines(file.toPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}