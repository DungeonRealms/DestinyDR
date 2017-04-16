package net.dungeonrealms.game.item.items.functional;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemConsumeEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.items.core.CombatItem;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanic.ParticleAPI;

public class ItemOrb extends FunctionalItem {

	private final String LAST_ORB = "lastOrbUsed";
	
	public ItemOrb() {
		super(ItemType.ORB_OF_ALTERATION);
	}
	
	public ItemOrb(ItemStack item) {
		super(item);
	}
	
	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {
        if(!CombatItem.isCombatItem(evt.getSwappedItem()))
        	return;
        
        evt.setCancelled(true);
        
        Player player = evt.getPlayer();
        GamePlayer gp = GameAPI.getGamePlayer(player);
        if (gp == null)
            return;
        
        //Cooldown has not expired.
        if (player.hasMetadata(LAST_ORB) && System.currentTimeMillis() - player.getMetadata(LAST_ORB).get(0).asLong() < 500)
        	return;
        
        player.setMetadata(LAST_ORB, new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
        evt.setUsed(true);

        ItemStack oldItem = evt.getSwappedItem().clone();
        
        ItemGear gear = (ItemGear)PersistentItem.constructItem(oldItem);
        int oldOrbSize = gear.getAttributes().getAttributes().size();
        gear.rollStats(true);
        ItemStack newItem = gear.generateItem();
        evt.setSwappedItem(newItem);

        //  ORB PARTICLES  //
        
        gp.getPlayerStatistics().setOrbsUsed(gp.getPlayerStatistics().getOrbsUsed() + 1);
        if (oldOrbSize < gear.getAttributes().getAttributes().size()) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.25F);
            ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.FIREWORKS_SPARK, player.getLocation().add(0, 2.5, 0),
            		new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.75F, 100);
            Firework fw = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
            FireworkMeta fwm = fw.getFireworkMeta();
            FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).withFade(Color.YELLOW).with(FireworkEffect.Type.BURST).trail(true).build();
            fwm.addEffect(effect);
            fwm.setPower(0);
            fw.setFireworkMeta(fwm);
        } else {
            // Orb failed. (Enchant had less than or equal to the number of ones it had before.)
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2.0F, 1.25F);
            ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.LAVA, player.getLocation().add(0, 2.5, 0),
            		new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 1F, 75);
        }
        
        player.updateInventory();
	}

	@Override
	protected String getDisplayName() {
		return ChatColor.LIGHT_PURPLE + "Orb of Alteration";
	}

	@Override
	protected String[] getLore() {
		return new String[] { "Randomizes bonus stats of selected equipment" };
	}

	@Override
	public void onClick(ItemClickEvent evt) {}

	@Override
	public void onConsume(ItemConsumeEvent evt) {}

	@Override
	protected ItemUsage[] getUsage() {
		return new ItemUsage[] {ItemUsage.INVENTORY_SWAP_PLACE};
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.MAGMA_CREAM);
	}
	
	public static boolean isOrb(ItemStack item) {
		return isType(item, ItemType.ORB_OF_ALTERATION);
	}
}
