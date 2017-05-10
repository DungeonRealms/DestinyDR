package net.dungeonrealms.game.player.inventory.menus.guis;

import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.items.functional.ecash.ItemPet;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.dungeonrealms.game.world.entity.type.pet.PetData;
import net.dungeonrealms.game.world.entity.util.PetUtils;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class PetSelectionGUI extends GUIMenu {
    public PetSelectionGUI(Player player) {
//        int size = EnumPets.values().length + 2; //Add 2 for the buttons
//        size -= size % 9;
        super(player, fitSize(EnumPets.values().length + 2), "Pet Selection");
    }

    @Override
    protected void setItems() {

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return;

        Map<EnumPets, PetData> playerPets = wrapper.getPetsUnlocked();

        setItem(getSize() - 2, new GUIItem(ItemManager.createItem(Material.BARRIER, ChatColor.GREEN + "Back"))
                .setClick(e -> PlayerMenus.openPlayerProfileMenu(player)));
        setItem(getSize() - 1, new GUIItem(ItemManager.createItem(Material.LEASH, ChatColor.GREEN + "Dismiss Pet"))
                .setClick(e -> PetUtils.removePet(player)));

        int i = 0;
        for (EnumPets pets : EnumPets.values()) {
            if (!pets.isShowInGui() && !Rank.isGM(player) || pets.isSpecial())
                continue;

            PetData hisData = playerPets.get(pets);
            ItemStack itemStack = new ItemStack(Material.MONSTER_EGG, 1, (short) pets.getEggShortData());
            AtomicBoolean isLocked = new AtomicBoolean(hisData == null || !hisData.isUnlocked());
            if (pets.isSubGetsFree() && wrapper.getRank().isSUB())
                isLocked.set(false);

            NBTTagCompound compound = new NBTTagCompound();
            compound.setString("id", EntityType.fromId(pets.getEggShortData()).getName());
            setItem(i++, new GUIItem(new NBTWrapper(ItemManager.createItem(Material.MONSTER_EGG, ChatColor.WHITE + pets.getDisplayName(), (short) pets.getEggShortData(),
                    ChatColor.GREEN + "Left Click: " + ChatColor.WHITE + "Summon Pet",
                    ChatColor.GREEN + "Right Click: " + ChatColor.WHITE + "Rename Pet",
                    "",
                    ChatColor.GREEN + "Name: " + ChatColor.WHITE + (hisData != null && hisData.getPetName() != null ? hisData.getPetName() : pets.getDisplayName()),
                    (isLocked.get() ? ChatColor.RED : ChatColor.GREEN) + "" + ChatColor.BOLD + (isLocked.get() ? "" : "UN") + "LOCKED")).set("EntityTag", compound).build()).setClick(e -> {

                if (isLocked.get()) {
                    player.sendMessage(ChatColor.RED + "You do " + ChatColor.BOLD + "NOT" + ChatColor.RED + " have access to this pet!");
                    player.sendMessage(ChatColor.GRAY + "You can unlock access at " + ChatColor.UNDERLINE + Constants.STORE_URL + ChatColor.GRAY + "!");
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
