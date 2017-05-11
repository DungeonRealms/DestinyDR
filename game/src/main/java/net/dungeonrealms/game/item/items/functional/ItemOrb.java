package net.dungeonrealms.game.item.items.functional;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.database.PlayerGameStats.StatColumn;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent.ItemInventoryListener;
import net.dungeonrealms.game.item.items.core.CombatItem;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class ItemOrb extends FunctionalItem implements ItemInventoryListener {

    private static final String LAST_ORB = "lastOrbUsed";

    public ItemOrb() {
        super(ItemType.ORB_OF_ALTERATION);
    }

    public ItemOrb(ItemStack item) {
        super(item);
    }

    @Override
    public void onInventoryClick(ItemInventoryEvent evt) {
        if (!CombatItem.isCombatItem(evt.getSwappedItem()))
            return;

        evt.setCancelled(true);

        Player player = evt.getPlayer();
        PlayerWrapper pw = PlayerWrapper.getWrapper(player);

        //Cooldown has not expired.
        if (player.hasMetadata(LAST_ORB) && System.currentTimeMillis() - player.getMetadata(LAST_ORB).get(0).asLong() < 500)
            return;

        player.setMetadata(LAST_ORB, new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
        evt.setUsed(true);

        ItemStack oldItem = evt.getSwappedItem().clone();

        ItemGear gear = (ItemGear) PersistentItem.constructItem(oldItem);
        int oldOrbSize = gear.getAttributes().getAttributes().size();
        gear.rollStats(true);
        ItemStack newItem = gear.generateItem();
        evt.setSwappedItem(newItem);

        //  ORB PARTICLES  //

        pw.getPlayerGameStats().addStat(StatColumn.ORBS_USED);
        if (oldOrbSize < gear.getAttributes().getAttributes().size()) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.25F);
            ParticleAPI.spawnParticle(Particle.FIREWORKS_SPARK, player.getLocation().add(0, 2.5, 0), 100, .75F);
            Firework fw = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
            FireworkMeta fwm = fw.getFireworkMeta();
            FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).withFade(Color.YELLOW).with(FireworkEffect.Type.BURST).trail(true).build();
            fwm.addEffect(effect);
            fwm.setPower(0);
            fw.setFireworkMeta(fwm);
        } else {
            // Orb failed. (Enchant had less than or equal to the number of ones it had before.)
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2.0F, 1.25F);
            ParticleAPI.spawnParticle(Particle.LAVA, player.getLocation().add(0, 2.5, 0), 75, 1F);
        }

        player.updateInventory();
    }

    @Override
    protected String getDisplayName() {
        return ChatColor.LIGHT_PURPLE + "Orb of Alteration";
    }

    @Override
    protected String[] getLore() {
        return new String[]{"Randomizes bonus stats of selected equipment"};
    }

    @Override
    protected ItemUsage[] getUsage() {
        return new ItemUsage[]{ItemUsage.INVENTORY_SWAP_PLACE};
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.MAGMA_CREAM);
    }

    public static boolean isOrb(ItemStack item) {
        return isType(item, ItemType.ORB_OF_ALTERATION);
    }
}
