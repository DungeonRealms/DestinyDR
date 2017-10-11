package net.dungeonrealms.game.item.items.functional.ecash;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemClickEvent.ItemClickListener;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.minecraft.server.v1_9_R2.EntityLightning;
import net.minecraft.server.v1_9_R2.PacketPlayOutSpawnEntityWeather;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.Set;

public class ItemLightningRod extends FunctionalItem implements ItemClickListener {

    public ItemLightningRod() {
        super(ItemType.LIGHTNING_ROD);
        setSoulbound(true);
    }

    public ItemLightningRod(ItemStack item) {
        super(item);
        setSoulbound(true);
    }


    @Override
    public void onClick(ItemClickEvent evt) {
        Player player = evt.getPlayer();

        if (evt.isRightClick()) {
            Location lightningLoc = null;
            if (evt.hasEntity()) {
                lightningLoc = evt.getClickedEntity().getLocation();
            } else if (evt.hasBlock()) {
                lightningLoc = evt.getClickedBlock().getLocation();
            } else {
                Block block = player.getTargetBlock((Set<Material>) null, 25);
                if (block != null) lightningLoc = block.getLocation();
            }

            if (lightningLoc != null) {
                if(isOnCD(player)) {
                    player.sendMessage(ChatColor.RED + "Your Lightning Rod is still recharging!");
                    return;
                }

                player.setMetadata("rodCD", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis() + 3000));
                /*EntityLightning el = new EntityLightning((World) Bukkit.getWorld(player.getWorld().toString()), lightningLoc.getX(), lightningLoc.getY(), lightningLoc.getZ(), true, true);
                PacketPlayOutSpawnEntityWeather packet = new PacketPlayOutSpawnEntityWeather(el);

                player.getWorld().playSound(lightningLoc, Sound.ENTITY_LIGHTNING_THUNDER, 1f, 1f);
                for(Player playa : GameAPI.getNearbyPlayers(lightningLoc, 50)) {
                    if(playa == null) continue;
                    ((CraftPlayer) playa).getHandle().playerConnection.sendPacket(packet);
                }*/
                GameAPI.playLightningEffect(player.getWorld(), lightningLoc, 50);
            }
        }
    }

    private boolean isOnCD(Player player) {
        if(!player.hasMetadata("rodCD")) return false;
        MetadataValue value = player.getMetadata("rodCD").get(0);
        Long theTime = value.asLong();
        if(theTime == null) return false;
        return theTime > System.currentTimeMillis();
    }

    @Override
    protected String getDisplayName() {
        return ChatColor.AQUA.toString() + ChatColor.BOLD + "Lightning Rod";
    }

    @Override
    protected String[] getLore() {
        return new String[]{
                " ",
                ChatColor.GREEN + ChatColor.BOLD.toString() + "Right Click: " + ChatColor.GRAY + "Strike lightning"};
    }

    @Override
    protected ItemUsage[] getUsage() {
        return INTERACT;
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.BLAZE_ROD);
    }
}
