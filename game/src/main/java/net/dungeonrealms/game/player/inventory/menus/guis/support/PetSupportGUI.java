package net.dungeonrealms.game.player.inventory.menus.guis.support;

import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.items.functional.ecash.ItemPet;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.guis.PetSelectionGUI;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.dungeonrealms.game.world.entity.type.pet.PetData;
import net.dungeonrealms.game.world.entity.util.PetUtils;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

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
        Map<EnumPets, PetData> playerPets = getWrapper().getPetsUnlocked();
        for (EnumPets pets : EnumPets.values()) {

            PetData hisData = playerPets.get(pets);

            AtomicBoolean isLocked = new AtomicBoolean(hisData == null || !hisData.isUnlocked());

            NBTTagCompound compound = new NBTTagCompound();
            compound.setString("id", pets.getEntityType().getName());
            setItem(i++, new GUIItem(new NBTWrapper(ItemManager.createItem(Material.MONSTER_EGG, ChatColor.WHITE + pets.getDisplayName(), (short) pets.getEggShortData(),"Click to toggle this pet for " + getOtherName(),"",
                    (isLocked.get() ? ChatColor.RED : ChatColor.GREEN) + "" + ChatColor.BOLD + (isLocked.get() ? "" : "UN") + "LOCKED")).set("EntityTag", compound).build()).setClick(e -> {

                getWrapper().getPetsUnlocked().put(pets, new PetData(null, !isLocked.get()));
                getWrapper().saveData(true,null);
                player.sendMessage("Success!");
            }));
        }

    }
}
