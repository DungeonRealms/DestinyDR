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
            lore.add(ChatColor.ITALIC + horse.getDescription());

            HorseTier req = horse.getRequirement();

            if (req != null)
                lore.add(ChatColor.RED + "" + ChatColor.BOLD + "REQ: " + ChatColor.RESET + ChatColor.AQUA + req.getName());

            lore.add(ChatColor.GREEN + "Price: " + ChatColor.WHITE + horse.getPrice() + "g");
            setItem(horse == HorseTier.MULE ? 9 : index++, new GUIItem(horse.getMount().getSelectionItem())
                    .setLore(lore).setName(horse.getColor() + horse.getName()).setClick(e -> {
                if (pw.getGems() < horse.getPrice()) {
                    player.sendMessage(ChatColor.RED + "You cannot afford this mount!");
                    return;
                }

                if (pw.getMountsUnlocked().contains(horse.getMount())) {
                    player.sendMessage(ChatColor.RED + "You already own this mount!");
                    return;
                }

                if (!MountUtils.hasMountPrerequisites(horse.getMount(), pw.getMountsUnlocked())) {
                    player.sendMessage(ChatColor.RED + "You must own the previous mount to upgrade.");
                    return;
                }

                pw.withdrawGems(horse.getPrice());
                buyMount(player, horse.getMount());
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
