package net.dungeonrealms.game.handlers;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Kieran Quigley (Proxying) on 18-Jun-16.
 */
public class TipHandler implements GenericMechanic {

    private static TipHandler instance = null;

    public static TipHandler getInstance() {
        if (instance == null) {
            instance = new TipHandler();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }

    @Override
    public void startInitialization() {
        loadTips();
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::displayTipToPlayers, 8000L, 6000L);
    }

    @Override
    public void stopInvocation() {

    }

    @Getter
    @Setter
    private List<String> unused_Tips = new ArrayList<>();

    @Getter
    @Setter
    private List<String> used_Tips = new ArrayList<>();

    public void displayTipToPlayers() {
        String tipToDisplay = getRandomUnusedTip();
        if (tipToDisplay.equals("")) {
            return;
        }
        Bukkit.getOnlinePlayers().stream().filter(player -> (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_TIPS, player.getUniqueId())).forEach(player -> player.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + ">>" + ChatColor.YELLOW + " TIP - " + ChatColor.GRAY + tipToDisplay));
    }

    private String getRandomUnusedTip() {
        if (unused_Tips.isEmpty() && used_Tips.isEmpty()) {
            return "";
        }
        if (unused_Tips.isEmpty()) {
            unused_Tips.addAll(used_Tips);
            used_Tips.clear();
        }

        String tipChosen = unused_Tips.get(new Random().nextInt(unused_Tips.size() - 1));

        used_Tips.add(tipChosen);
        unused_Tips.remove(tipChosen);

        return tipChosen;
    }

    private void loadTips() {
        try {
            File file = new File("plugins/DungeonRealms/tips/tips.txt");
            if (!(file.exists())) {
                file.createNewFile();
            }
            int count = 0;
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                for (String line; (line = br.readLine()) != null; ) {
                    if (line.equalsIgnoreCase("null")) {
                        continue;
                    }
                    unused_Tips.add(line);
                    count++;
                }
                br.close();
                DungeonRealms.getInstance().getLogger().info("[TipMechanics] " + count + " Gameplay Tips have been LOADED.");
            } catch (Exception err) {
                err.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
