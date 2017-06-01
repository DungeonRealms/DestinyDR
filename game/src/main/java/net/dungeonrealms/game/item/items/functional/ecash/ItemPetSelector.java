package net.dungeonrealms.game.item.items.functional.ecash;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.PetSelectionGUI;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class ItemPetSelector extends FunctionalItem implements ItemInventoryEvent.ItemInventoryListener {

    private EnumPets pet;
    private String petName;

    public ItemPetSelector(ItemStack item) {
        super(item);
    }

    public ItemPetSelector() {
        super(ItemType.PET_SELECTOR);
    }

    public ItemPetSelector(EnumPets pet) {
        this();
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
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> new PetSelectionGUI(evt.getPlayer(), null).open(evt.getPlayer(), evt.getEvent().getAction()), 1);
    }
}
