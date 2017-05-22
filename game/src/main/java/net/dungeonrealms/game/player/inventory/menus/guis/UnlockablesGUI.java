package net.dungeonrealms.game.player.inventory.menus.guis;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.items.functional.ecash.ItemMount;
import net.dungeonrealms.game.item.items.functional.ecash.ItemMuleMount;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.data.MuleTier;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.entity.util.PetUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Set;

/**
 * Created by Rar349 on 5/18/2017.
 */
public class UnlockablesGUI extends GUIMenu {

    public UnlockablesGUI(Player player, GUIMenu previous) {
        super(player,9,"Unlockables",previous);
        if(previousGUI != null) setShouldOpenPreviousOnClose(true);
    }

    @Override
    protected void setItems() {

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);

        setItem(0, new GUIItem(ItemManager.createItem(Material.SADDLE, ChatColor.GOLD + "Mounts",
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

        setItem(2, new GUIItem(ItemManager.createItem(Material.ARMOR_STAND, ChatColor.GOLD + "Mount Skins",
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Equip your mount with a fancy skin.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View obtained mount skins."
        )).setClick(e -> new MountSkinSelectionGUI(player, this).open(player, e.getAction())));

        setItem(4, new GUIItem(ItemManager.createItem(Material.EYE_OF_ENDER, ChatColor.GOLD + "Effects and Trails",
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Stand out amongst the rest",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "with powerful effects.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View available effects."
        )).setClick(e -> new ParticleEffectGUI(player, this).open(player, e.getAction())));

        setItem(6, new GUIItem(ItemManager.createItem(Material.LEASH, ChatColor.GOLD + "Storage Mule",
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
                    setShouldOpenPreviousOnClose(false);
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
                setShouldOpenPreviousOnClose(false);
                player.closeInventory();
            }
        }));

        setItem(8, new GUIItem(ItemManager.createItem(Material.NAME_TAG, ChatColor.GOLD + "Pets",
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Travel with a cute companion.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View available pets."
        )).setClick(e -> new PetSelectionGUI(player, this).open(player, null)));
    }
}
