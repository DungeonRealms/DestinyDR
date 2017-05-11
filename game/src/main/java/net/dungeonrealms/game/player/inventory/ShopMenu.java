package net.dungeonrealms.game.player.inventory;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.item.items.core.ShopItem;
import net.dungeonrealms.game.item.items.core.ShopItem.ShopItemClick;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.listener.NPCMenu;
import net.dungeonrealms.game.mastery.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A menu that is used to purchase items from the server using portal shards or eCash.
 * <p>
 * Created April 10th, 2017.
 *
 * @author Kneesnap
 */
public abstract class ShopMenu {

    @Getter
    @Setter
    protected Inventory inventory;

    @Getter
    protected Map<Integer, ShopItem> items = new HashMap<>();

    @Getter
    protected int index = 0, size;

    @Getter
    protected Player player;

    @Getter
    protected Consumer<Player> closeCallback;

    @Getter
    protected String title;

    protected static ShopItem BACK;

    static {
        createStaticItems();
    }

    public ShopMenu(Player player, String title, int rows) {
        this.title = title;
        this.size = rows * 9;
        this.inventory = Bukkit.createInventory(null, rows * 9, title);
        open(player, null);
    }

    public ShopMenu(Player player, int size, String title) {
        this.title = title;
        this.size = size;
        this.inventory = Bukkit.createInventory(null, this.size, title);
    }

    protected NPCMenu getLastMenu() {
        return null; //NPCMenu.ECASH_VENDOR;
    }

    protected void skipSlot() {
        skipSlot(1);
    }

    protected void skipSlot(int i) {
        this.index += i;
    }

    protected ShopItem addItem(ShopItem item) {
        if (this.index >= getInventory().getSize()) {
            GameAPI.sendDevMessage("Attempted to place more items than " + getInventory().getTitle() + " can hold.");
            Utils.printTrace();
            return item;
        }
        getItems().put(this.index, item);
        this.index += 1;
        return item;
    }

    protected ShopItem addItem(ItemStack item) {
        return addItem(new VanillaItem(item));
    }

    protected ShopItem addItem(ItemGeneric item) {
        return addItem(new ShopItem(item));
    }

    protected ShopItem addItem(ItemGeneric item, ShopItemClick cb) {
        return addItem(new ShopItem(item, cb));
    }

    /**
     * Fills empty items with glass.
     */
    protected void bloat() {
        ItemStack glass = new ItemStack(Material.THIN_GLASS);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);

        for (int i = 0; i < getInventory().getContents().length; i++) {
            ItemStack item = getInventory().getContents()[i];
            if (item == null || item.getType().equals(Material.AIR))
                getInventory().setItem(i, glass.clone());
        }
    }

    protected void setIndex(int i) {
        this.index = i;
    }

    /**
     * Places the items in the shop.
     */
    protected abstract void setItems();

    /**
     * Closes this inventory safely and removes all items.
     */
    public void destroy() {
        Lists.newArrayList(getInventory().getViewers()).forEach(HumanEntity::closeInventory);
        getInventory().clear();
    }

    /**
     * Opens this gui for the specified player.
     */
    public void open(Player player, InventoryAction action) {
        if (player == null)
            return;
        this.player = player;
        this.index = 0;
        this.inventory.clear();
        setItems();
        for (int i : getItems().keySet())
            getInventory().setItem(i, getItems().get(i).generateItem());


        ShopMenu menu = ShopMenuListener.getMenus().get(player);
        if(menu != null){
            if(menu.getTitle() != null && menu.getTitle().equals(getTitle()) && menu.getSize() == getSize()){
                setItems();
                return;
            }
            //Delay the next inventory click by 1.
            if(action != null && action.name().startsWith("PICKUP_")){
                //CAnt close the inventory on a pickup_all action etc otherwise throws exceptions.
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    player.closeInventory();
                    reopenWithDelay(player);
                });
                return;
            }
            reopenWithDelay(player);
            return;
        }
        player.openInventory(getInventory());
        ShopMenuListener.getMenus().put(player, this);
    }

    public void reopenWithDelay(Player player){
        player.closeInventory();
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            player.openInventory(getInventory());
            ShopMenuListener.getMenus().put(player, this);
        }, 1);
    }
    protected static ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();

        // Name
        if (name != null)
            meta.setDisplayName(name);

        // Lore
        List<String> l = new ArrayList<>();
        if (lore != null)
            for (String s : lore)
                l.add(ChatColor.GRAY + s);
        meta.setLore(l);


        stack.setItemMeta(meta);
        return stack;
    }

    public void onRemove() {

    }

    private static void createStaticItems() {
        BACK = new ShopItem(new VanillaItem(createItem(Material.BARRIER, ChatColor.GREEN + "Return")), (player, item) -> {
            ShopMenu menu = ShopMenuListener.getMenu(player);
            if (menu.getLastMenu() != null)
                menu.getLastMenu().open(player);
            return false;
        });
    }

    public static int fitSize(int size) {
    	return Math.min(6, 1 + ((size - 1) / 9)) * 9;
    }
}
