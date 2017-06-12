package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import com.google.common.collect.Lists;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.items.functional.ecash.ItemDPSDummy;
import net.dungeonrealms.game.item.items.functional.ecash.ItemNameTag;
import net.dungeonrealms.game.item.items.functional.ecash.jukebox.ItemJukebox;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

import java.util.List;


/**
 * Created by Rar349 on 5/10/2017.
 */
public class MiscGUI extends GUIMenu implements WebstoreGUI {


    public MiscGUI(Player player) {
        super(player, 9, "Miscellaneous");
        setShouldOpenPreviousOnClose(true);
    }

    @Override
    protected void setItems() {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return;


        for (Purchaseables webItem : Purchaseables.values()) {
            if (webItem.getCategory() != getCategory()) continue;
            List<String> lore = Lists.newArrayList();
            lore.addAll(webItem.getDescription(true));
            lore.add("");
            lore.add(webItem.getOwnedDisplayString(wrapper));
            setItem(webItem.getGuiSlot(), new GUIItem(webItem.getItemType()).setName(webItem.getName())
                    .setLore(lore).setDurability(webItem.equals(Purchaseables.SCRAP_TAB) ? DyeColor.YELLOW.getDyeData() : 0)
                    .setClick(evt -> {
                        boolean unlocked = webItem.isUnlocked(wrapper);
                        if (!webItem.isCanHaveMultiple() && !webItem.isShouldStore()) {
                            player.sendMessage(ChatColor.GRAY + "You can purchase this item at " + ChatColor.UNDERLINE + Constants.STORE_URL);
                            this.setShouldOpenPreviousOnClose(false);
                            player.closeInventory();
                        } else if (webItem.equals(Purchaseables.JUKEBOX)) {
                            if (unlocked) {
                                ItemStack juke = new ItemJukebox().generateItem();
                                boolean hasJuke = Utils.hasItem(player, new ItemJukebox().generateItem().getItemMeta().getDisplayName());
                                if (hasJuke) {
                                    player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You already have a Music Box!");
                                    return;
                                }

                                player.getInventory().addItem(juke);
                                player.sendMessage(ChatColor.GREEN + "Enjoy your Music Box!");

                            } else {
                                sendNotUnlocked(player);
                            }

                            setShouldOpenPreviousOnClose(false);
                            player.closeInventory();
                        } else if (webItem == Purchaseables.GOLDEN_CURSE) {
                            if (unlocked) {
                                //Activate / de-activate
                                if (wrapper.getActiveTrail() == ParticleAPI.ParticleEffect.GOLD_BLOCK) {
                                    // Turn it OFF.
                                    player.sendMessage(ChatColor.RED + "Golden Curse - " + ChatColor.BOLD + "DISABLED");
                                    wrapper.setActiveTrail(null);
                                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 0.5F);
                                } else {
                                    // Turn it ON.
                                    player.sendMessage(ChatColor.GREEN + "Golden Curse - " + ChatColor.BOLD + "ENABLED");
                                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 3F);
                                    wrapper.setActiveTrail(ParticleAPI.ParticleEffect.GOLD_BLOCK);
                                }
                                setItems();
                            } else {
                                sendNotUnlocked(player);
                            }
                        } else if (webItem == Purchaseables.DPS_DUMMY) {
                            if (unlocked) {
                                //Activate / de-activate
                                if (Utils.hasItem(player, Material.ARMOR_STAND)) {
                                    // Turn it OFF.
                                    player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You already have a DPS Dummy!");
                                    return;
                                }

                                player.getInventory().addItem(new ItemDPSDummy(null).generateItem());
                                player.sendMessage(ChatColor.GREEN + "DPS Dummy has been added to your inventory.");
                            } else {
                                sendNotUnlocked(player);
                            }
                        } else if (webItem == Purchaseables.ITEM_NAME_TAG) {
                            if (unlocked) {
                                if(player.getInventory().firstEmpty() == -1) {
                                    player.sendMessage(ChatColor.RED + "You have no inventory space to claim this!");
                                    return;
                                }
                                webItem.removeNumberUnlocked(wrapper,1,(rows) -> {
                                    if(rows == null || rows < 1) {
                                        player.sendMessage(ChatColor.RED + "We are sorry! An error occurred while claiming your name tag!");
                                        player.sendMessage(ChatColor.GRAY + "Please try again later or contact an administrator if the problem persists!");
                                        return;
                                    }
                                    player.getInventory().addItem(new ItemNameTag().generateItem());
                                    player.sendMessage(ChatColor.GREEN + "An item name tag has been added to your inventory.");
                                    reconstructGUI(player);
                                });
                            } else {
                                sendNotUnlocked(player);
                            }
                        }
                    }));
        }
    }


    @Override
    public GUIMenu getPreviousGUI() {
        return new CategoryGUI(player);
    }

    @Override
    public void open(Player player, InventoryAction action) {
        super.open(player, action);
    }

    public WebstoreCategories getCategory() {
        return WebstoreCategories.MISCELLANEOUS;
    }

}
