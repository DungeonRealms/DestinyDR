package net.dungeonrealms.game.player.inventory.menus.guis.support;

import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.Purchaseables;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.WebstoreCategories;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Rar349 on 5/19/2017.
 */
public class PetSupportGUI extends SupportGUI {

    public PetSupportGUI(Player viewer, String other) {
        super(viewer,other,45,other + "'s Ecash Management");
    }

    @Override
    protected void setItems() {
        int i = 0;
        Map<EnumPets, String> playerPets = getWrapper().getPetsUnlocked();
        for(Purchaseables purchase : Purchaseables.values()) {
            if(!purchase.getCategory().equals(WebstoreCategories.PETS)) continue;

            EnumPets pets = (EnumPets) purchase.getSpecialArgs()[0];

            AtomicBoolean isLocked = new AtomicBoolean(!purchase.isUnlocked(getWrapper()));

            NBTTagCompound compound = new NBTTagCompound();
            compound.setString("id", pets.getEntityType().getName());
            setItem(i++, new GUIItem(new NBTWrapper(ItemManager.createItem(Material.MONSTER_EGG, ChatColor.WHITE + pets.getDisplayName(), (short) pets.getEggShortData(),"Click to toggle this pet for " + getOtherName(),"",
                    (isLocked.get() ? ChatColor.RED : ChatColor.GREEN) + "" + ChatColor.BOLD + (isLocked.get() ? "" : "UN") + "LOCKED")).set("EntityTag", compound).build()).setClick(e -> {

                purchase.setNumberOwned(getWrapper(), isLocked.get() ? 1 : 0);
                saveData();
                player.sendMessage("Success!");
            }));
        }

    }
}
