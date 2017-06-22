package net.dungeonrealms.game.player.inventory.menus.guis;

import com.google.common.collect.Lists;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMountSkins;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

import java.util.List;

public class MountSkinSelectionGUI extends GUIMenu {
    public MountSkinSelectionGUI(Player player, GUIMenu previous) {
        super(player, 9, "Mount Skin Selection", previous);
    }

    @Override
    protected void setItems() {

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        int slot = 0;
        for (EnumMountSkins skin : EnumMountSkins.values()) {
            List<String> lore = Lists.newArrayList();
            lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString() + skin.getDescription());
            lore.add("");
            if (wrapper.getMountSkins().contains(skin)) {
                if (wrapper.getActiveMountSkin() != null && wrapper.getActiveMountSkin().equals(skin)) {
                    lore.add(ChatColor.GREEN.toString() + ChatColor.BOLD + "SELECTED");
                    lore.add(ChatColor.GRAY + "Click to deactivate this skin!");
                } else {
                    lore.add(ChatColor.GREEN + ChatColor.BOLD.toString() + "UNLOCKED");
                    lore.add(ChatColor.GRAY + "Click to apply this skin!");
                }
            } else {
                lore.add(ChatColor.RED + ChatColor.BOLD.toString() + "LOCKED");
                lore.add(ChatColor.WHITE.toString() + skin.getEcashPrice() + " " + ChatColor.GREEN + "E-Cash");
                lore.add(ChatColor.GRAY + "Click to unlock this skin!");
            }

            setItem(slot++, new GUIItem(skin.getSelectionItem()).setName(ChatColor.GREEN.toString() + skin.getDisplayName())
                    .setLore(lore).setClick(event -> {
                        if (!wrapper.getMountSkins().contains(skin)) {
                            player.sendMessage(ChatColor.RED + "You do not own this Mount Skin!");
                            if (wrapper.hasEcash(skin.getEcashPrice())) {
                                Utils.sendCenteredMessage(player, ChatColor.GREEN + "Are you sure you want to unlock this Mount Skin?");
                                Utils.sendCenteredMessage(player, ChatColor.GRAY + "Please enter " + ChatColor.GREEN + ChatColor.BOLD + "Y" + ChatColor.GRAY + " to confirm this purchase.");
                                Chat.promptPlayerConfirmation(player, () -> {
                                    if (wrapper.hasEcash(skin.getEcashPrice()) && !wrapper.getMountSkins().contains(skin)) {
                                        wrapper.withdrawEcash(skin.getEcashPrice());
                                        wrapper.getMountSkins().add(skin);
                                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.4F);
                                        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
                                            player.sendMessage(ChatColor.GREEN + "You have purchased the " + skin.getDisplayName() + " mount skin!");
                                            //Redraw.
                                            open(player, null);
                                        });
                                    } else {
                                        player.sendMessage(ChatColor.RED + "You do not have enough E-Cash anymore!");
                                    }
                                }, () -> player.sendMessage(ChatColor.RED + "Mount Skin Purchase - " + ChatColor.BOLD + "CANCELLED"));
                            } else {
                                player.sendMessage(ChatColor.RED + "This Mount Skin requires " + ChatColor.UNDERLINE + skin.getEcashPrice() + ChatColor.RED + " E-Cash!");
                                player.sendMessage(ChatColor.GRAY + "You can also unlock this Mount Skin at " + ChatColor.UNDERLINE + Constants.SHOP_URL + ChatColor.GRAY + "!");
                            }
                            return;
                        }

                        if (skin.equals(wrapper.getActiveMountSkin())) {
                            player.sendMessage(ChatColor.RED + "Mount skin deactivated!");
                            wrapper.setActiveMountSkin(null);
                            setItems();
                            return;
                        } else {
                            wrapper.setActiveMountSkin(skin);
                            player.sendMessage(ChatColor.GREEN + "Your mount skin has been changed!");
                            setItems();
                        }
                        Entity activeMount = MountUtils.getMounts().get(player);
                        if (activeMount != null && activeMount instanceof Horse) {
                            Horse horse = (Horse) activeMount;
                            horse.setVariant(wrapper.getActiveMountSkin() == null ? Horse.Variant.HORSE : skin.getVariant());
                        }
                    }));
        }

        setItem(getSize() - 1, getBackButton(ChatColor.GRAY + "Return to Profile Menu"));
    }
}
