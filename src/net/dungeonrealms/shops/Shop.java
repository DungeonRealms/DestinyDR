package net.dungeonrealms.shops;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mastery.ItemSerialization;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumOperators;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

/**
 * Created by Chase on Sep 23, 2015
 */
public class Shop {

    private UUID owner;
    private String shopName;
    public boolean isopen;
    public Inventory inventory;
    public Block block;
    Hologram hologram = null;

    public Shop(UUID owner, String shopName, Block block) {
        this.owner = owner;
        this.shopName = shopName.replace("@", "%");
        this.isopen = false;
        this.inventory = createNewInv();
        this.block = block;
        Location loc = block.getLocation();

        loc.add(.5, 1.5, .5);
        hologram = HologramsAPI.createHologram(DungeonRealms.getInstance(), loc);
        hologram.appendTextLine(ChatColor.RED.toString() + ChatColor.BOLD + shopName);
        hologram.getVisibilityManager().setVisibleByDefault(true);
    }

    /*
     * toggles hologram name green and red for on and off.
     */
    public void toggleHologram() {
        hologram.clearLines();
        if (isopen)
            hologram.appendTextLine(ChatColor.GREEN.toString() + ChatColor.BOLD + shopName);
        else
            hologram.appendTextLine(ChatColor.RED.toString() + ChatColor.BOLD + shopName);
    }

    /**
     * Returns block representing this shop
     *
     * @since 1.0
     */
    public Block getBlock() {
        return block;
    }

    /**
     * Creates new inventory.
     *
     * @since 1.0
     */
    private Inventory createNewInv() {
        Inventory inv = Bukkit.createInventory(null, getSize(owner), shopName + " - @" + Bukkit.getPlayer(owner).getName());
        ItemStack button = new ItemStack(Material.INK_SACK, 1, DyeColor.GRAY.getDyeData());
        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW.toString() + "Open Shop");
        button.setItemMeta(meta);
        net.minecraft.server.v1_8_R3.ItemStack nmsButton = CraftItemStack.asNMSCopy(button);
        nmsButton.getTag().setString("status", "off");
        inv.setItem(8, CraftItemStack.asBukkitCopy(nmsButton));
        return inv;
    }

    /**
     * Get current inventory
     *
     * @since 1.0
     */
    public Inventory getInv() {
        return inventory;
    }

    /**
     * getOwner of Shop instance
     *
     * @since 1.0
     */
    public Player getOwner() {
        return Bukkit.getPlayer(owner);
    }

    /**
     * Deletes block, and unregisters all things for shop.
     *
     * @since 1.0
     */
    public void deleteShop() {
        hologram.delete();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack current = inventory.getItem(i);
            if (current == null) continue;
            net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(current);
            if (nms.hasTag()) {
                if (nms.getTag().hasKey("status")) continue;
            }
            inventory.setItem(i, null);
            ItemMeta meta = current.getItemMeta();
            List<String> lore = meta.getLore();
            for (int j = 0; j < lore.size(); j++) {
                String currentStr = lore.get(j);
                if (currentStr.contains("Price")) {
                    lore.remove(j);
                    break;
                }
            }
            nms.getTag().remove("worth");
            meta.setLore(lore);
            current.setItemMeta(meta);
            if (getOwner() != null) {
                getOwner().getInventory().addItem(current);
            } else {
                // Collection Bin
            }
        }
        Block chest = getBlock();
        block.setType(Material.AIR);
        chest.setType(Material.AIR);
        chest.getWorld().playSound(chest.getLocation(), Sound.PISTON_RETRACT, 1, 1);
        
        // TODO: WTF ARE YOU DOING CHASE WE HAVE A PARTICLE API. DON'T CALL THIS
        // SHIT. GOOD BOY
        /*
         * Packet b_particles = new PacketPlayOutWorldEvent(2001, new
		 * BlockPosition((int) Math.round(chest.getLocation().getX()), (int)
		 * Math.round(chest.getLocation().getY()), (int)
		 * Math.round(chest.getLocation().getZ())), 54, false); ((CraftServer)
		 * DungeonRealms.getInstance().getServer()).getServer().getPlayerList().
		 * sendPacketNearby( chest.getLocation().getX(),
		 * chest.getLocation().getY(), chest.getLocation().getZ(), 24,
		 * ((CraftWorld) chest.getWorld()).getHandle().dimension, b_particles);
		 */

        // TODO FUCKIN Broken Shit Particles

        // try {
        // ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.TOWN_AURA,
        // chest.getLocation(),
        // new Random().nextFloat(), new Random().nextFloat(), new
        // Random().nextFloat(), 0.6F, 150);
        // } catch (Exception ex) {
        // ex.printStackTrace();
        // }
        getBlock().setType(Material.AIR);
        ShopMechanics.PLAYER_SHOPS.remove(owner);
    }

    /**
     * returns size of players shop they can make.
     *
     * @param uniqueId
     * @since 1.0
     */
    private static int getSize(UUID uniqueId) {
    	//TODO MAKE SHOPS UPGRADE
//    	int lvl = (Integer) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_LEVEL, Bukkit.getPlayer(uniqueId).getUniqueId());
        return 9;
    }

	/**
	 * 
	 */
	public void saveCollectionBin() {
		Inventory inv = getInv();
		String invString = "";
		for(ItemStack stack : inv.getContents()){
			if(stack != null && stack.getType() != Material.AIR){
				invString = ItemSerialization.toString(inv);
				break;
			}
		}
		DatabaseAPI.getInstance().update(owner, EnumOperators.$SET, "info.collection_bin", invString, false);
	}
}