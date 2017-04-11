package net.dungeonrealms.game.item.items.functional.ecash;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemConsumeEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.player.chat.Chat;
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
            
            if (evt.hasEntity() && evt.getClickedEntity().equals(entity.getBukkitEntity())) {
            	player.sendMessage(ChatColor.GRAY + "Enter a name for your pet, or type " + ChatColor.RED + ChatColor.UNDERLINE + "cancel" + ChatColor.GRAY + ".");
            	Chat.listenForMessage(player, (mess) -> {
            		Entity pet = EntityAPI.getPlayerPet(player.getUniqueId());
            		if (pet == null) { // No Pet?
            			player.sendMessage(ChatColor.RED + "You have no active pet to rename.");
            			return;
            		}
            		// Cancel
            		String name = mess.getMessage().replaceAll("@", "_");
            		if (name.equalsIgnoreCase("cancel") || name.equalsIgnoreCase("exit")) {
            			player.sendMessage(ChatColor.GRAY + "Pet naming " + ChatColor.RED + ChatColor.UNDERLINE + "CANCELLED.");
            			return;
            		}
            		
            		// Too Long.
            		if (name.length() > 20) {
            			player.sendMessage(ChatColor.RED + "Your pet name exceeds the maximum length of 20 characters.");
                        player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Please remove " + (name.length() - 20) + " characters.");
            			return;
            		}
            		
            		String checkedPetName = Chat.getInstance().checkForBannedWords(name);

                    String activePet = (String) DatabaseAPI.getInstance().getData(EnumData.ACTIVE_PET, player.getUniqueId());
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.PETS, activePet, true);
                    if (activePet.contains("@"))
                        activePet = activePet.split("@")[0];
                    
                    // Update DB and entity name.
                    String newPet = activePet + "@" + checkedPetName;
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.PETS, activePet, true);
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, EnumData.PETS, newPet, true);
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_PET, newPet, true);
                    pet.setCustomName(Rank.colorFromRank(Rank.getInstance().getRank(player.getUniqueId())) + checkedPetName);
                    player.sendMessage(ChatColor.GRAY + "Your pet's name has been changed to " + ChatColor.GREEN + ChatColor.UNDERLINE + checkedPetName + ChatColor.GRAY + ".");
            		
            	}, null);
            	// Rename Pet.
            	return;
            }
            // Dismiss Pet
            
            if (entity.isAlive())
                entity.getBukkitEntity().remove();
            
            if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity))
                DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
            
            player.sendMessage(ChatColor.GREEN + "Your pet has been dismissed.");
            EntityAPI.removePlayerPetList(player.getUniqueId());
            return;
        }
        
		String petType = (String) DatabaseAPI.getInstance().getData(EnumData.ACTIVE_PET, player.getUniqueId());
        
		// No Pet.
		if (petType == null || petType.equals("")) {
            player.sendMessage(ChatColor.RED + "You don't have an active pet, please enter the pets section in your profile to set one.");
            player.closeInventory();
            return;
        }
        
		//Either load pet name or get default.
		String petName;
        if (petType.contains("@")) {
            petName = petType.split("@")[1];
            petType = petType.split("@")[0];
        } else {
            petName = EnumPets.getByName(petType).getDisplayName();
        }
        
        // Spawn Pet.
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
