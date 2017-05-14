package net.dungeonrealms.game.player.inventory.menus;

import com.google.common.collect.Lists;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;
import net.dungeonrealms.game.mechanic.data.HorseTier;
import net.dungeonrealms.game.player.menu.CraftingMenu;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class ShopAnimalTamer extends GUIMenu {

    public ShopAnimalTamer(Player player) {
        super(player, 18, "Animal Tamer");
        open(player, null);
    }

    @Override
    protected void setItems() {
        PlayerWrapper pw = PlayerWrapper.getWrapper(player);
        for (HorseTier horse : HorseTier.values()) {
            List<String> lore = Lists.newArrayList();
            EnumMounts mount = horse == HorseTier.MULE ? EnumMounts.MULE : horse.getMount();
            if (mount.name().contains("HORSE")) {
                lore.add(ChatColor.RED + "Speed: " + horse.getSpeed() + "%");
                if (horse.getJump() > 100)
                    lore.add(ChatColor.RED + "Jump: " + horse.getJump() + "%");
            }

            lore.add("");
            lore.addAll(horse.getDescription());

            HorseTier req = horse.getRequirement();

            if (req != null)
                lore.add(ChatColor.RED + "" + ChatColor.BOLD + "REQ: " + ChatColor.RESET + ChatColor.AQUA + req.getNameWithColor());

            lore.add("");
            if (pw.getMountsUnlocked().contains(mount)) {
                lore.add(ChatColor.GREEN + ChatColor.BOLD.toString() + "UNLOCKED");
            } else {
                lore.add(ChatColor.GREEN + "Price: " + ChatColor.WHITE + horse.getPrice() + "g");
            }
            setItem(horse == HorseTier.MULE ? 9 : index++, new GUIItem(mount.getSelectionItem())
                    .setLore(lore).setName(horse.getNameWithColor()).setClick(e -> {

                        if (pw.getMountsUnlocked().contains(mount)) {
                            player.sendMessage(ChatColor.RED + "You already own this mount!");
                            return;
                        }


                        if (pw.getGems() < horse.getPrice()) {
                            player.sendMessage(ChatColor.RED + "You cannot afford this mount!");
                            return;
                        }
                        if (!MountUtils.hasMountPrerequisites(mount, pw.getMountsUnlocked())) {
                            player.sendMessage(ChatColor.RED + "You must own the previous mount to upgrade.");
                            return;
                        }

                        pw.withdrawGems(horse.getPrice());
                        buyMount(player, mount);
                    }));
        }
    }

    private boolean buyMount(Player player, EnumMounts mount) {
        PlayerWrapper pw = PlayerWrapper.getWrapper(player);

        pw.getMountsUnlocked().add(mount);

        if (mount != EnumMounts.MULE) {
            pw.setActiveMount(mount);
            Achievements.giveAchievement(player, EnumAchievements.MOUNT_OWNER);
            CraftingMenu.addMountItem(player);
        } else {
            CraftingMenu.addMuleItem(player);
        }

        player.sendMessage(ChatColor.GREEN + "You have purchased the " + mount.getDisplayName() + ChatColor.GREEN + " mount.");
        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), player::closeInventory);

        return true;
    }
}
