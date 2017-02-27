package net.dungeonrealms.game.affair.party;

import com.google.common.collect.Lists;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.menu.AbstractMenu;
import net.dungeonrealms.common.game.menu.gui.GUIButtonClickEvent;
import net.dungeonrealms.common.game.menu.gui.VolatileGUI;
import net.dungeonrealms.common.game.menu.item.GUIButton;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PLootMenu extends AbstractMenu implements VolatileGUI {

    private Party party;

    public PLootMenu(Player player, Party party) {
        super(DungeonRealms.getInstance(), "Party Loot Selection", 9);
        setDestroyOnExit(true);

        this.party = party;
        this.updateInventory();
    }


    public void updateInventory() {
        int slot = 3;

        for (LootMode mode : LootMode.values()) {

            List<String> lore = Lists.newArrayList();
            for (String line : mode.getLore()) {
                lore.add(line.replace("{LEADER}", party.getOwner().getName()));
            }

            if (mode == party.getLootMode()) {
                lore.add("");
                lore.add(ChatColor.GREEN.toString() + ChatColor.BOLD + "SELECTED");
            }

            ItemStack item = new ItemBuilder().setItem(new ItemStack(mode.getMaterial(), 1))
                    .setName(mode.getColor() + ChatColor.BOLD.toString() + mode.getName()).setLore(lore).build();

            if (party.getLootMode() == mode) {
                item = new NBTWrapper(item).setString("ench", "").build();
            }

            GUIButton button = new GUIButton(item) {
                @Override
                public void action(GUIButtonClickEvent event) throws Exception {
                    Player pl = event.getWhoClicked();
                    event.setCancelled(true);
                    if (party != null && party.getOwner() != null && party.getOwner().getName().equals(pl.getName())) {
                        party.setLootMode(mode);
//                        updateInventory();
                        pl.closeInventory();
                        pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    } else {
                        pl.sendMessage(ChatColor.RED + "Only " + party.getOwner().getName() + " can edit the loot mode.");
                        pl.closeInventory();
                    }
                }
            };

            set(slot++, button);
        }
    }

    @Override
    public void onDestroy(Event event) {

    }

    @Override
    public void open(Player player) throws Exception {

        player.openInventory(inventory);
    }
}
