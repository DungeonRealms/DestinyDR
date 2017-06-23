package net.dungeonrealms.game.player.inventory.menus.guis.support;

import net.dungeonrealms.common.game.database.player.PlayerRank;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.game.menu.gui.GUIButtonClickEvent;
import net.dungeonrealms.common.game.menu.item.GUIButton;
import net.dungeonrealms.common.util.CharacterData;
import net.dungeonrealms.common.util.CharacterType;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Created by Rar349 on 6/22/2017.
 */
public class CharacterSelectionGUI extends GUIMenu {

    private Consumer<Integer> clickCallback;
    private int accountID;
    public CharacterSelectionGUI(Player player, int accountID, Consumer<Integer> clickCallback) {
        super(player,54, "Characters");
        this.clickCallback = clickCallback;
        this.accountID = accountID;
    }

    @Override
    protected void setItems() {
        SQLDatabaseAPI.getInstance().executeQuery(QueryType.SELECT_ALL_CHARACTERS.getQuery(accountID), true, (set) -> {
            if (set == null) {
                player.sendMessage(ChatColor.RED + "Something went wrong! Please try again! 2");
                return;
            }
            List<CharacterData> createdCharacterIds = new ArrayList<>();
            Integer selectedCharacterID = null;
            try {
                while (set.next()) {
                    createdCharacterIds.add(new CharacterData(set.getInt("character_id"), set.getString("display_name"), set.getInt("level"), set.getInt("health"), set.getLong("created"), set.getString("alignment"), set.getString("character_type"), set.getBoolean("characters.isLocked")));
                    if (selectedCharacterID == null) selectedCharacterID = set.getInt("selected_character_id");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (selectedCharacterID == null) {
                player.sendMessage(ChatColor.RED + "Something went wrong! Please try again! 3");
                return;
            }

            int slot = 0;

            for(CharacterData nameObject : createdCharacterIds) {
                int characterID = nameObject.getCharacterID();
                CharacterType type = CharacterType.getCharacterType(nameObject.getCharacterType());
                boolean isLocked = nameObject.isManuallyLocked() || isLocked(type, player);
                String characterTitle = nameObject.getCharacterName();
                if (characterTitle == null) characterTitle = "Non Named Character";
                boolean isSelected = selectedCharacterID.intValue() == characterID;
                short durability = isLocked ? DyeColor.RED.getWoolData() : isSelected ? DyeColor.LIME.getWoolData() : DyeColor.YELLOW.getWoolData();
                ItemStack buttonStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, durability);

                List<String> lore = new ArrayList<>();


                //final int slot = getServerType(shardID).equals("") ? getSize() : Math.min(getInventorySize(), getInventorySize() - 1) - (getSize() - getNormalServers());

                lore.add(" ");
                lore.add(ChatColor.GOLD + ChatColor.BOLD.toString() + "Character Info");
                //lore.add(" ");
                lore.add(ChatColor.YELLOW + ChatColor.BOLD.toString() + "  Type: " + ChatColor.GRAY + type.getDisplayName());
                lore.add(ChatColor.YELLOW + ChatColor.BOLD.toString() + "  Level: " + ChatColor.GRAY + nameObject.getLevel());
                lore.add(ChatColor.YELLOW + ChatColor.BOLD.toString() + "  Health: " + ChatColor.GRAY + nameObject.getCurrentHP());
                lore.add(ChatColor.YELLOW + ChatColor.BOLD.toString() + "  Alignment: " + ChatColor.GRAY + nameObject.getAlignmentString());
                //lore.add(" ");
                lore.add(ChatColor.YELLOW + ChatColor.BOLD.toString() + "  Created: " + ChatColor.GRAY + nameObject.getTimeCreatedString());
                lore.add(" ");
                lore.add(ChatColor.RED + ChatColor.BOLD.toString() + "Left Click:" + ChatColor.GRAY + " Load this character");
                //lore.add(ChatColor.RED + ChatColor.BOLD.toString() + "Right Click:" + ChatColor.GRAY + " Rename this character");
                if(isLocked) {
                    lore.add(" ");
                    lore.add(ChatColor.RED + ChatColor.BOLD.toString() + "LOCKED");
                }

                setItem(slot++, new GUIItem(buttonStack).setName(ChatColor.GREEN + ChatColor.BOLD.toString() + characterTitle).setLore(lore).setClick((evt) -> {
                    if (isLocked) {
                        player.sendMessage(ChatColor.RED + "This character is locked!");
                        return;
                    }
                    player.closeInventory();
                    if (evt.getClick().equals(ClickType.LEFT)) {
                        clickCallback.accept(characterID);
                    }
                }));
            }
        });
    }

    private static boolean isLocked(CharacterType type, Player player) {
        if (type == null) return true; //Lock them if it's unknown?
        PlayerRank rank = type.getRank();
        if (!Rank.getPlayerRank(player.getUniqueId()).isAtLeast(rank)) return true;
        return false;
    }
}
