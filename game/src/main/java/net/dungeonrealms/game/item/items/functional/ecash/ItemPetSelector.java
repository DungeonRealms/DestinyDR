package net.dungeonrealms.game.item.items.functional.ecash;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.player.inventory.menus.guis.PetSelectionGUI;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class ItemPetSelector extends FunctionalItem implements ItemInventoryEvent.ItemInventoryListener {

    private EnumPets pet;
    private String petName;

    public ItemPetSelector(ItemStack item){
        super(ItemType.PET_SELECTOR);
    }
    public ItemPetSelector(Player player) {
        super(ItemType.PET_SELECTOR);
    }

    public ItemPetSelector(EnumPets pet) {
        super(ItemType.PET_SELECTOR);
        this.pet = pet;
        this.petName = getPet().getDisplayName();
    }

    @Override
    protected String getDisplayName() {
        return ChatColor.GREEN + ChatColor.BOLD.toString() + "Pet Menu";
    }

    @Override
    protected String[] getLore() {
        return new String[]{ChatColor.GRAY + "Click to access the pet selection menu!"};
    }

    @Override
    protected ItemUsage[] getUsage() {
        return INVENTORY_PICKUP;
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.NAME_TAG, 1);
    }

    @Override
    public void onInventoryClick(ItemInventoryEvent evt) {
        evt.setCancelled(true);
        new PetSelectionGUI(evt.getPlayer()).open(evt.getPlayer(), evt.getEvent().getAction());
    }
}
