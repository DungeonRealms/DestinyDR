package net.dungeonrealms.game.player.inventory.menus.guis;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.items.functional.ecash.ItemMount;
import net.dungeonrealms.game.item.items.functional.ecash.ItemMuleMount;
import net.dungeonrealms.game.listener.NPCMenu;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.data.MuleTier;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import net.dungeonrealms.game.player.inventory.menus.ShopECashVendor;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveOpenProfile;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.entity.util.PetUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;

import java.util.Set;

public class PlayerProfileGUI extends GUIMenu {
    public PlayerProfileGUI(Player player, GUIMenu menu) {
        super(player, 27, "Profile (" + player.getName() + ")", menu);
    }

    public PlayerProfileGUI(Player player) {
        this(player, null);
    }

    @Override
    protected void setItems() {

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        setItem(0, new GUIItem(ItemManager.createItem(Material.EXP_BOTTLE, ChatColor.GOLD + "Stat Distribution",
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Place stat points on different attributes. ",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View Attributes."
        )).setClick(e -> wrapper.getPlayerStats().openMenu(player)));

        setItem(1, new GUIItem(ItemManager.createItem(Material.SKULL_ITEM, ChatColor.GOLD + "Friend List", (short) 3,
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Add or remove friends.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View Friend list."
        )).setSkullOwner("Shrek").setClick(e -> new FriendGUI(player, this, false).open(player, e.getAction())));

        setItem(4, new GUIItem(Utils.getPlayerHead(player)).setName(ChatColor.GREEN + "Player Profile"));

        setItem(6, new GUIItem(ItemManager.createItem(Material.EYE_OF_ENDER, ChatColor.GOLD + "Effects and Trails",
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Stand out amongst the rest",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "with powerful effects.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View available effects."
        )).setClick(e -> new ParticleEffectGUI(player, this).open(player, e.getAction())));

        setItem(7, new GUIItem(ItemManager.createItem(Material.SADDLE, ChatColor.GOLD + "Mounts",
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Travel Andalucia quickly.",
                "",
                ChatColor.WHITE + "Left-Click:" + ChatColor.GREEN + " View obtained mounts.",
                ChatColor.WHITE + "Right-Click:" + ChatColor.GREEN + " Receive Saddle."
        )).setClick(e -> {
            if (e.getClick() == ClickType.RIGHT) {
                if (!Utils.hasItem(player, Material.SADDLE)) {
                    if (player.getInventory().firstEmpty() == -1) {
                        player.sendMessage(ChatColor.RED + "You do not have any free inventory space!");
                        return;
                    }


                    EnumMounts highestHorse = wrapper.getHighestHorseUnlocked();
                    if (highestHorse == null) {
                        player.sendMessage(ChatColor.RED + "You do not own a Mount!");
                        player.sendMessage(ChatColor.GRAY + "Visit the Animal Tamer to unlock one!");
                        return;
                    }

                    player.sendMessage(ChatColor.GREEN + highestHorse.getDisplayName() + ChatColor.GREEN + " Saddle added to inventory!");
                    player.getInventory().addItem(new ItemMount(highestHorse.getHorseTier()).generateItem());
                    player.closeInventory();
                } else {
                    player.sendMessage(ChatColor.RED + "There already seems to be a Saddle in your inventory or Bank!");
                }
            } else {
                new MountSelectionGUI(player, this).open(player, e.getAction());
            }
        }));

        setItem(8, new GUIItem(ItemManager.createItem(Material.NAME_TAG, ChatColor.GOLD + "Pets",
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Travel with a cute companion.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View available pets."
        )).setClick(e -> new PetSelectionGUI(player, this).open(player, null)));

        setItem(16, new GUIItem(ItemManager.createItem(Material.LEASH, ChatColor.GOLD + "Storage Mule",
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Inventory getting full on your travels?",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "purchase a Mule from the Animal Tamer.",
                "",
                ChatColor.WHITE + "Left-Click:" + ChatColor.GREEN + " Spawn Storage Mule.",
                ChatColor.WHITE + "Right-Click:" + ChatColor.GREEN + " Receive Mule Leash."
        )).setClick(e -> {
            Set<EnumMounts> playerMounts = wrapper.getMountsUnlocked();
            if (!playerMounts.contains(EnumMounts.MULE)) {
                player.sendMessage(ChatColor.RED + "Purchase a Storage Mule from the Animal Tamer near Cyrennica Fountain!");
                return;
            }
            if (e.getClick() == ClickType.RIGHT) {
                MuleTier tier = MuleTier.getByTier(wrapper.getMuleLevel());
                ItemMuleMount mount = new ItemMuleMount(tier);
                if (!Utils.hasItem(player, Material.LEASH)) {
                    if (player.getInventory().firstEmpty() == -1) {
                        player.sendMessage(ChatColor.RED + "You do not have any free inventory space!");
                        return;
                    }

                    player.sendMessage(ChatColor.GRAY + "Mule Leash added to inventory!");
                    player.getInventory().addItem(mount.generateItem());
                    player.closeInventory();
                } else {
                    player.sendMessage(ChatColor.RED + "There already seems to be a Leash in your inventory or Bank!");
                }
            } else {
                MountUtils.removeMount(player);
                PetUtils.removePet(player);
                if (CombatLog.isInCombat(player)) {
                    player.sendMessage(ChatColor.RED + "You cannot summon a storage mule while in combat!");
                    return;
                }
                wrapper.setActiveMount(EnumMounts.MULE);
                ItemMount.attemptSummonMount(player, null);
                player.closeInventory();
            }
        }));

        setItem(17, new GUIItem(ItemManager.createItem(Material.ARMOR_STAND, ChatColor.GOLD + "Mount Skins",
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Equip your mount with a fancy skin.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View obtained mount skins."
        )).setClick(e -> new MountSkinSelectionGUI(player, this).open(player, e.getAction())));

        setItem(18, new GUIItem(ItemManager.createItem(Material.EMERALD, ChatColor.GOLD + "E-Cash Vendor",
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "E-Cash is obtained by voting and online store purchase.",
                ChatColor.GRAY + "http://dungeonrealms.net/shop",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " Open the E-Cash Vendor."
        )).setClick(e -> {
            player.closeInventory();
            //NPCMenu.ECASH_VENDOR.open(player)
            ShopECashVendor gui = new ShopECashVendor(player);
            gui.setPreviousGUI(this);
            gui.setShouldOpenPreviousOnClose(true);
            gui.open();
        }));

        setItem(24, new GUIItem(ItemManager.createItem(Material.COMPASS, ChatColor.GOLD + "Achievements",
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Check your progress.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View achievements."
        )).setClick(e -> new AchievementGUI(player, this).open(player, e.getAction())));

        setItem(26, new GUIItem(ItemManager.createItem(Material.REDSTONE_COMPARATOR, ChatColor.GOLD + "Toggles",
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Adjust preferences here.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " Open toggles menu."
        )).setClick(e -> new TogglesGUI(player, this).open(player, e.getAction())));
    }

    @Override
    public void open(Player player, InventoryAction action) {
        super.open(player, action);
        Quests.getInstance().triggerObjective(player, ObjectiveOpenProfile.class);
    }
}
