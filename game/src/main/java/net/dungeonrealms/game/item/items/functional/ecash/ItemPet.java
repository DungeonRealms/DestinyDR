package net.dungeonrealms.game.item.items.functional.ecash;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemConsumeEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.entity.util.PetUtils;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_9_R2.Entity;

public class ItemPet extends FunctionalItem {

	public ItemPet() {
		super(ItemType.PET);
		setUntradeable(true);
	}
	
	public ItemPet(ItemStack item) {
		super(item);
	}

	@Override
	public void onClick(ItemClickEvent evt) {
		Player player = evt.getPlayer();
		
		if (EntityAPI.hasPetOut(player.getUniqueId())) {
            Entity entity = EntityAPI.getPlayerPet(player.getUniqueId());
            if (entity.isAlive()) {
                entity.getBukkitEntity().remove();
            }
            if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
            }
            player.sendMessage(ChatColor.GREEN + "Your pet has been dismissed.");
            EntityAPI.removePlayerPetList(player.getUniqueId());
            return;
        }
        
		String petType = (String) DatabaseAPI.getInstance().getData(EnumData.ACTIVE_PET, player.getUniqueId());
        
		if (petType == null || petType.equals("")) {
            player.sendMessage(ChatColor.RED + "You don't have an active pet, please enter the pets section in your profile to set one.");
            player.closeInventory();
            return;
        }
        
		String petName;
        if (petType.contains("@")) {
            petName = petType.split("@")[1];
            petType = petType.split("@")[0];
        } else {
            petName = EnumPets.getByName(petType).getDisplayName();
        }
        
        PetUtils.spawnPet(player.getUniqueId(), petType, petName);
        player.sendMessage(ChatColor.GREEN + "Your pet has been summoned.");
	}

	@Override
	public void onConsume(ItemConsumeEvent evt) {}

	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {}

	@Override
	protected String getDisplayName() {
		return ChatColor.GREEN + "Pet";
	}

	@Override
	protected String[] getLore() {
		return new String[] { ChatColor.DARK_GRAY + "Summons your active Pet." };
	}

	@Override
	protected ItemUsage[] getUsage() {
		return INTERACT_RIGHT_CLICK;
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.NAME_TAG);
	}
}
