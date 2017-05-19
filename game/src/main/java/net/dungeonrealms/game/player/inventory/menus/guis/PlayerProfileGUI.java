package net.dungeonrealms.game.player.inventory.menus.guis;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.items.functional.ecash.ItemMount;
import net.dungeonrealms.game.item.items.functional.ecash.ItemMuleMount;
import net.dungeonrealms.game.listener.NPCMenu;
import net.dungeonrealms.game.mastery.StatBoost;
import net.dungeonrealms.game.mastery.Stats;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.data.MuleTier;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import net.dungeonrealms.game.player.inventory.menus.ShopECashVendor;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.CategoryGUI;
import net.dungeonrealms.game.player.stats.PlayerStats;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveOpenProfile;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.entity.util.PetUtils;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class PlayerProfileGUI extends GUIMenu {

    private static DecimalFormat df = new DecimalFormat("##.###");

    public PlayerProfileGUI(Player player, GUIMenu menu) {
        super(player, 27, "Profile (" + player.getName() + ")", menu);
    }

    public PlayerProfileGUI(Player player) {
        this(player, null);
    }

    @Override
    protected void setItems() {

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);

        setItem(17, new GUIItem(ItemManager.createItem(Material.BOOK_AND_QUILL, ChatColor.YELLOW + ChatColor.BOLD.toString() + "Friend List",
                ChatColor.GRAY.toString() + "Add or remove friends."
        )).setClick(e -> new FriendGUI(player, this, false).open(player, e.getAction())));

        setItem(0, new GUIItem(ItemManager.createItem(Material.CHEST, ChatColor.YELLOW + ChatColor.BOLD.toString() + "Unlockables",
                ChatColor.GRAY.toString() + "Make yourself unique",
                ChatColor.GRAY.toString() + "with awesome unlockables"
        )).setClick(e -> new UnlockablesGUI(player, this).open(player, e.getAction())));

        setItem(18, new GUIItem(ItemManager.createItem(Material.ENDER_CHEST, ChatColor.YELLOW + ChatColor.BOLD.toString() + "Purchasables",
                ChatColor.GRAY.toString() + "Swag yourself out with",
                ChatColor.GRAY.toString() + "some dank purchasables"
        )).setClick(e -> new CategoryGUI(player).setPreviousGUI(this).setShouldOpenPreviousOnClose(true).open(player, e.getAction())));







        setItem(9, new GUIItem(ItemManager.createItem(Material.EMERALD, ChatColor.YELLOW + ChatColor.BOLD.toString() + "E-Cash Vendor",
                ChatColor.GRAY.toString() + "E-Cash is obtained by voting",
                "",
                ChatColor.RED + "Use /vote"
        )).setClick(e -> {
            player.closeInventory();
            //NPCMenu.ECASH_VENDOR.open(player)
            ShopECashVendor gui = new ShopECashVendor(player);
            gui.setPreviousGUI(this);
            gui.setShouldOpenPreviousOnClose(true);
            gui.open();
        }));

        setItem(8, new GUIItem(ItemManager.createItem(Material.COMPASS, ChatColor.YELLOW + ChatColor.BOLD.toString() + "Achievements",
                ChatColor.GRAY.toString() + "Check your progress."
        )).setClick(e -> new AchievementGUI(player, this).open(player, e.getAction())));

        setItem(26, new GUIItem(ItemManager.createItem(Material.REDSTONE_COMPARATOR, ChatColor.YELLOW + ChatColor.BOLD.toString() + "Toggles",
                ChatColor.GRAY.toString() + "Adjust preferences here."
        )).setClick(e -> new TogglesGUI(player, this).open(player, e.getAction())));

        PlayerStats stats = wrapper.getPlayerStats();
        for(Stats stat : Stats.values()) {



            //Maps
            List<String> lore = new ArrayList<>();
            lore.addAll(Arrays.asList(stat.getDescription()));
            lore.add("");
            Utils.addChatColor(lore,ChatColor.GRAY);
            lore.add(ChatColor.AQUA + "Allocated Points: " + stats.getStat(stat) + (stats.getTempStat(stat) > 0 ? ChatColor.GREEN + " [+" + stats.getTempStat(stat) + "]" : ""));
            lore.add(ChatColor.RED + "Free Points: " + stats.getFreePoints());
            lore.add("");

            for(StatBoost boost : stat.getStatBoosts()) {
                String prefix = ChatColor.stripColor(boost.getType().getPrefix());
                lore.add(ChatColor.GOLD + prefix + ChatColor.AQUA + df.format(stats.getStat(stat) * boost.getMultiplier()) + boost.getType().getSuffix() + " " + (stats.getTempStat(stat) > 0 ? ChatColor.GREEN + "[+" + df.format(stats.getTempStat(stat) * boost.getMultiplier()) + "]" : ""));
            }

            if(stat.equals(Stats.STRENGTH)) {
                lore.add(ChatColor.GOLD + "AXE DMG: +" + ChatColor.AQUA + df.format(stats.getAxeDMG(false)) + "% " + (stats.getTempStat(stat) > 0 ? ChatColor.GREEN + "[+" + df.format(stats.getAxeDMG(true)) + "%]" : ""));
                lore.add(ChatColor.GOLD + "POLEARM DMG: +" + ChatColor.AQUA + df.format(stats.getPolearmDMG(false)) + "% " + (stats.getTempStat(stat) > 0 ? ChatColor.GREEN + "[+" + df.format(stats.getPolearmDMG(true)) + "%]" : ""));
            } else if(stat.equals(Stats.VITALITY)) {
                lore.add(ChatColor.GOLD + "SWORD DMG: +" + ChatColor.AQUA + df.format(stats.getSwordDMG(false)) + "% " + (stats.getTempStat(stat) > 0 ? ChatColor.GREEN + "[+" + df.format(stats.getSwordDMG(true)) + "%]" : ""));
            } else if(stat.equals(Stats.DEXTERITY)) {
                lore.add(ChatColor.GOLD + "BOW DMG: +" + ChatColor.AQUA + df.format(stats.getBowDMG(false)) + "% " + (stats.getTempStat(stat) > 0 ? ChatColor.GREEN + "[+" + df.format(stats.getBowDMG(true)) + "%]" : ""));
            } else if(stat.equals(Stats.INTELLECT)) {
                lore.add(ChatColor.GOLD + "STAFF DMG: +" + ChatColor.AQUA + df.format(stats.getStaffDMG(false)) + "% " + (stats.getTempStat(stat) > 0 ? ChatColor.GREEN + "[+" + df.format(stats.getStaffDMG(true)) + "%]" : ""));
            }

            setItem(stat.getGuiSlot(), new GUIItem(Material.BOOK).setName(ChatColor.DARK_PURPLE + ChatColor.BOLD.toString() + stat.getDisplayName()).setLore(lore).setClick((evt) -> {
                        if (evt.isRightClick())
                            stats.removePoint(stat);
                        if (evt.isLeftClick())
                            stats.allocatePoint(stat);

                    reconstructGUI(player);
            }));
        }

        setItem(15, new GUIItem(Material.INK_SACK).setDurability(DyeColor.LIME.getDyeData()).setName(ChatColor.GREEN + ChatColor.BOLD.toString() + "Confirm").setLore(ChatColor.GRAY + "Click to confirm your stat ", ChatColor.GRAY + "point allocation.  If you ", ChatColor.GRAY + "want to undo your changes, ", ChatColor.GRAY + "press escape.").setClick((evt) -> {
            stats.confirmStats();
            player.closeInventory();
        }));
    }

    @Override
    public void onRemove() {
        super.onRemove();
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper != null) wrapper.getPlayerStats().resetTemp();
    }

    @Override
    public void open(Player player, InventoryAction action) {
        super.open(player, action);
        Quests.getInstance().triggerObjective(player, ObjectiveOpenProfile.class);
    }
}
