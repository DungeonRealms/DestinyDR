package net.dungeonrealms.game.player.inventory.menus.guis;

import com.google.common.collect.Lists;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMountSkins;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import org.bukkit.ChatColor;
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
                lore.add(ChatColor.GRAY + "Visit the E-Cash Vendor to unlock it!");
            }

            setItem(slot++, new GUIItem(skin.getSelectionItem()).setName(ChatColor.GREEN.toString() + skin.getDisplayName())
                    .setLore(lore).setClick(event -> {
                        if (!wrapper.getMountSkins().contains(skin)) {
                            player.sendMessage(ChatColor.RED + "You do not own this Mount Skin!");
                            player.sendMessage(ChatColor.GRAY + "You can unlock it at " + ChatColor.UNDERLINE + Constants.STORE_URL + ChatColor.GRAY + "!");
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
