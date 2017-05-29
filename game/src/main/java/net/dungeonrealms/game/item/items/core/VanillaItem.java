package net.dungeonrealms.game.item.items.core;

import lombok.Setter;
import net.dungeonrealms.game.item.ItemType;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A generic item that can be constructed with any bukkit itemstack.
 *
 * @author Kneesnap
 */
public class VanillaItem extends ItemGeneric {
    @Setter
    private String displayName;
    private boolean empty;

    public VanillaItem(ItemStack item) {
        super(item);
        if (item == null || item.getType() == Material.AIR) {
            empty = true;
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta.hasLore())
            meta.getLore().forEach(this::addLore); // Lore gets wiped when an item is generated, don't let that happen.
        if (meta.hasDisplayName())
            setDisplayName(meta.getDisplayName());
    }

    // Change visibility.
    public void addLore(String lore) {
        super.addLore(lore);
    }

    @Override
    public void updateItem() {
        if (displayName != null)
            getMeta().setDisplayName(displayName);
        super.updateItem();
    }

    @Override
    public ItemStack generateItem() {
        ItemStack generated = super.generateItem();
        return generated;
    }

    @Override
    protected void saveMeta() {
        ItemStack item = getStack().clone();
        if (item.getType() == Material.AIR) return;
        getMeta().setLore(this.lore);
        item.setItemMeta(getMeta());
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
        if (!nms.hasTag())
            return;

        if (nms.getTag().hasKey("display"))
            getTag().set("display", nms.getTag().get("display"));
    }

    @Override
    protected ItemStack getStack() {
        return empty ? new ItemStack(Material.AIR) : getItem();
    }

    @Override
    public ItemType getItemType() {
        return null;
    }
}
