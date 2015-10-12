package net.dungeonrealms.handlers;

import com.minebone.anvilapi.core.AnvilApi;
import com.minebone.anvilapi.nms.anvil.AnvilGUIInterface;
import com.minebone.anvilapi.nms.anvil.AnvilSlot;
import net.dungeonrealms.guild.Guild;
import net.dungeonrealms.inventory.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Nick on 10/2/2015.
 */
public class ClickHandler {

    static ClickHandler instance = null;

    public static ClickHandler getInstance() {
        if (instance == null) {
            instance = new ClickHandler();
        }
        return instance;
    }

    public void doGuildClick(InventoryClickEvent event) {
        String name = event.getInventory().getName();
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if (slot == -999) return;
        if (name.equals("Guild Management")) {
            event.setCancelled(true);
            switch (slot) {
                case 0:
                    Menu.openPlayerGuildInventory(player);
                    break;
                case 10:
                    AnvilGUIInterface invitePlayerGUI = AnvilApi.createNewGUI(player, anvilClick -> {
                        switch (anvilClick.getSlot()) {
                            case OUTPUT:
                                anvilClick.setWillClose(true);
                                anvilClick.setWillDestroy(true);
                                Guild.getInstance().invitePlayer(player, anvilClick.getName());
                                break;
                        }
                    });
                    invitePlayerGUI.setSlot(AnvilSlot.INPUT_LEFT, Menu.editItem(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), "Type name here..", new String[]{}));
                    invitePlayerGUI.open();
                    break;
                case 11:
                    AnvilGUIInterface removePlayerGUI = AnvilApi.createNewGUI(player, anvilClick -> {
                        switch (anvilClick.getSlot()) {
                            case OUTPUT:
                                Guild.getInstance().removePlayer(player, anvilClick.getName());
                                break;
                        }
                    });
                    removePlayerGUI.setSlot(AnvilSlot.INPUT_LEFT, Menu.editItem(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), "Type name here..", new String[]{}));
                    removePlayerGUI.open();
                    break;
            }
        }
        if (name.endsWith("- Officers")) {
            event.setCancelled(true);
            if (slot == 0) {
                Menu.openPlayerGuildInventory(player);
            }
        } else if (name.endsWith("- Members")) {
            event.setCancelled(true);
            if (slot == 0) {
                Menu.openPlayerGuildInventory(player);
            }
        } else if (name.endsWith(" - (Bank Logs)")) {
            event.setCancelled(true);
            if (slot == 0) {
                Menu.openPlayerGuildLog(player);
            }
        } else if (name.endsWith("- (Invite Logs)")) {
            event.setCancelled(true);
            if (slot == 0) {
                Menu.openPlayerGuildLog(player);
            }
        } else if (name.endsWith(" - (Login Logs)")) {
            event.setCancelled(true);
            if (slot == 0) {
                Menu.openPlayerGuildLog(player);
            }
        } else if (name.endsWith("- (Logs)")) {
            event.setCancelled(true);
            if (slot > 18) return;
            switch (slot) {
                case 0:
                    Menu.openPlayerGuildInventory(player);
                    break;
                case 12:
                    Menu.openPlayerGuildLogLogins(player);
                    break;
                case 13:
                    Menu.openPlayerGuildLogInvitations(player);
                    break;
                case 14:
                    Menu.openPlayerGuildLogBankClicks(player);
                    break;

            }
        } else if (name.equals("Top Guilds")) {
            event.setCancelled(true);
        } else if (name.equals("Guild Management")) {
            event.setCancelled(true);
        } else if (name.startsWith("Guild - ")) {
            event.setCancelled(true);
            if (slot > 54) return;
            switch (slot) {
                case 0:
                    Menu.openPlayerGuildLog(player);
                    break;
                case 1:
                    Menu.openGuildManagement(player);
                    break;
                case 17:
                    Menu.openGuildRankingBoard(player);
                    break;
                case 18:
                    Menu.openGuildOfficers(player);
                    break;
                case 27:
                    Menu.openGuildMembers(player);
                    break;
            }
        }
    }
}
