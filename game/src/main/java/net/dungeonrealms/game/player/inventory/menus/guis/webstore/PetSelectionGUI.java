package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import com.google.common.collect.Lists;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.items.functional.ecash.ItemPet;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.dungeonrealms.game.world.entity.util.PetUtils;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PetSelectionGUI extends GUIMenu {
    public PetSelectionGUI(Player player, GUIMenu gui) {
        super(player, fitSize(EnumPets.values().length + 2), "Pet Selection", gui);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void setItems() {

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return;

        if (this.previousGUI != null)
            setItem(getSize() - 2, getBackButton());
        setItem(getSize() - 1, new GUIItem(ItemManager.createItem(Material.LEASH, ChatColor.GREEN + "Dismiss Pet"))
                .setClick(e -> PetUtils.removePet(player)));

        for(Purchaseables purchase : Purchaseables.values()) {
            if(purchase.getCategory() == null || !purchase.getCategory().equals(WebstoreCategories.PETS)) continue;

            if(!purchase.isEnabled() && !Rank.isGM(player)) continue;

            EnumPets pets = (EnumPets) purchase.getSpecialArgs()[0];
            String name = pets.getPlayersPetName(wrapper);

            AtomicBoolean isLocked = new AtomicBoolean(!purchase.isUnlocked(wrapper));
            if ((pets.isSubGetsFree() && wrapper.getRank().isSUB()) || (pets.isSubPlusPlusGetsFree() && wrapper.getRank().isLifetimeSUB()))
                isLocked.set(false);

            List<String> lore = Lists.newArrayList(
                    ChatColor.GRAY + "Left Click: " + ChatColor.WHITE + "Summon Pet",
                    ChatColor.GRAY + "Right Click: " + ChatColor.WHITE + "Rename Pet",
                    "");
            if(pets.getDescription() != null){
                lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString() + pets.getDescription());
                lore.add("");
            }
            lore.add(ChatColor.GREEN + "Name: " + ChatColor.WHITE + (name));
            lore.add((isLocked.get() ? ChatColor.RED : ChatColor.GREEN) + "" + ChatColor.BOLD + (isLocked.get() ? "" : "UN") + "LOCKED");

            NBTTagCompound compound = new NBTTagCompound();
            compound.setString("id", pets.getEntityType().getName());
            setItem(purchase.getGuiSlot(), new GUIItem(new NBTWrapper(ItemManager.createItem(purchase.getItemType(), ChatColor.GREEN + pets.getDisplayName(),(short) purchase.getMeta())).set("EntityTag", compound).build())
                    .setLore(lore).setClick(e -> {

                        if (isLocked.get()) {
                            player.sendMessage(ChatColor.RED + "You do " + ChatColor.BOLD + "NOT" + ChatColor.RED + " have access to this pet!");
                            player.sendMessage(ChatColor.GRAY + "You can unlock access at " + ChatColor.UNDERLINE + Constants.SHOP_URL + ChatColor.GRAY + "!");
                            return;
                        }

                        if (e.getClick() == ClickType.LEFT) {
                            PetUtils.removePet(player);
                            PlayerWrapper pw = PlayerWrapper.getWrapper(player);
                            pw.setActivePet(pets);
                            PetUtils.spawnPet(player, pets, pw.getPetName(pets));
                        } else if (e.getClick() == ClickType.RIGHT) {
                            ItemPet.renamePet(player, pets);
                        }
                    }));

        }
    }

}
