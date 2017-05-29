package net.dungeonrealms.game.player.inventory.menus.guis;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.items.functional.ecash.ItemMount;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.data.HorseTier;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.type.mounts.MountData;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class MountSelectionGUI extends GUIMenu {

    public MountSelectionGUI(Player player, GUIMenu previous) {
        super(player, fitSize(EnumMounts.values().length + 2), "Mount Selection", previous);
    }

    @Override
    protected void setItems() {
        int slot = 0;
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return;

        Set<EnumMounts> playerMounts = wrapper.getMountsUnlocked();

        if (this.previousGUI != null)
            setItem(getSize() - 2, getBackButton());

        setItem(getSize() - 1, new GUIItem(ItemManager.createItem(Material.LEASH, ChatColor.GREEN + "Dismiss Mount"))
                .setClick(e -> MountUtils.removeMount(player)));

        for (EnumMounts mounts : EnumMounts.values()) {
            AtomicBoolean unlocked = new AtomicBoolean(playerMounts.contains(mounts));
            setItem(slot++, new GUIItem(getDisplayItem(wrapper, mounts, unlocked.get())).setClick((evt) -> {
                if (!unlocked.get()) {
                    player.sendMessage(ChatColor.RED + "You do " + ChatColor.BOLD + "NOT" + ChatColor.RED + " have access to this mount!");
                    return;
                }

                if (evt.getClick() == ClickType.LEFT) {
                    //Need to go through attemptSummon for proper checks.
                    wrapper.setActiveMount(mounts);

                    String name = mounts == EnumMounts.MULE ? wrapper.getMuleTier().getName() : mounts.getDisplayName();
                    setShouldOpenPreviousOnClose(false);
                    ItemMount.attemptSummonMount(player, name);
                    player.closeInventory();
                }
            }));
        }
    }

    public ItemStack getDisplayItem(PlayerWrapper wrapper, EnumMounts mount, boolean unlocked) {
        MountData data = mount.getMountData();
        Material mat = mount.getSelectionItem().getType();
        String name = mount.getDisplayName();
        ChatColor displayColor = mount.getDisplayColor();

        ItemStack toReturn = new ItemStack(mat);
        ItemMeta stackMeta = toReturn.getItemMeta();
        stackMeta.setDisplayName(displayColor + (mount == EnumMounts.MULE ? wrapper.getMuleTier().getName() : name));
        List<String> lore = new ArrayList<>();
        if (data != null) {
            lore.add(ChatColor.RED + "Speed " + data.getSpeedPercent() + "%");
//            lore.add(ChatColor.RED + "Jump: 100%");
            lore.addAll(data.getLore());
        } else {
            HorseTier tier = HorseTier.getByMount(mount);
            if (tier != null) {
                lore.add(ChatColor.RED + "Speed " + tier.getSpeed() + "%");
                if (tier.getJump() > 100)
                    lore.add(ChatColor.RED + "Jump " + tier.getJump() + "%");
                lore.addAll(tier.getDescription());
            }else{
                //Storage mule I guess?
                lore.addAll(HorseTier.MULE.getDescription(wrapper.getMuleTier()));
            }
        }
        lore.add("");
        lore.add(unlocked ? ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "UNLOCKED" : ChatColor.RED.toString() + ChatColor.BOLD.toString() + "LOCKED");
        stackMeta.setLore(lore);
        toReturn.setItemMeta(stackMeta);
        return toReturn;
    }


}
