package net.dungeonrealms.game.item.items.functional.ecash.jukebox;

import com.google.common.collect.Lists;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MobileJukeboxGUI extends GUIMenu {

    private MobileJukebox jukebox;

    public MobileJukeboxGUI(Player player, MobileJukebox jukebox) {
        super(player, 18, "Mobile Musicbox");
        this.jukebox = jukebox;
    }

    private static Set<Material> records = new LinkedHashSet<>();

    static {
        //Record 3 - 12, gold, green
        records.add(Material.GOLD_RECORD);
        records.add(Material.GREEN_RECORD);
        for (int i = 3; i < 13; i++) {
            records.add(Material.matchMaterial("RECORD_" + i));
        }
    }

    @Override
    protected void setItems() {
        int slot = 0;
        boolean isOwner = jukebox.getUuid().equals(player.getUniqueId());
        for (Material material : records) {
            List<String> lore = Lists.newArrayList();

            lore.add("");
            if (material.equals(jukebox.getRecordPlaying())) {
                lore.add(ChatColor.GREEN + ChatColor.BOLD.toString() + "CURRENTLY PLAYING");
            } else {
                if (isOwner) {
                    lore.add(ChatColor.GRAY + "Click to play this record!");
                }
            }
            if (!isOwner) {
                lore.add(ChatColor.GRAY + "Want to play this record?");
                lore.add(ChatColor.GRAY + "Unlock a " + ChatColor.AQUA + "Mobile Musicbox" + ChatColor.GRAY + " on");
                lore.add(ChatColor.GRAY + "the Dungeon Realms web store!");
            }
            setItem(slot++, new GUIItem(material).setLore(lore).setClick(evt -> {
                if (isOwner) {
                    //Check if someone already occupied this block in this time?
                    MobileJukebox closest = ItemJukebox.getNearbyJukebox(jukebox.getJukebox().getLocation());
                    if (closest != null && !player.isOp()) {
                        player.sendMessage(ChatColor.RED + "You cannot place a Mobile Musicbox so close to another one!");
                        player.sendMessage(ChatColor.GRAY + "Closest Jukebox: " + ChatColor.BOLD + closest.getOwner());
                        return;
                    }

                    player.sendMessage(ChatColor.GREEN + "Now playing record!");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1F);
                    jukebox.setPlaying(material);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.closeInventory(), 1);
                } else {
                    player.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " change the song for " + jukebox.getOwner() + "'s Mobile Musicbox.");
                    player.sendMessage(ChatColor.GRAY + "Unlock your own at " + ChatColor.UNDERLINE + Constants.STORE_URL + ChatColor.GRAY + "!");
                }
            }));
        }
    }
}
