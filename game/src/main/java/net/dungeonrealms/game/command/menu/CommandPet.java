package net.dungeonrealms.game.command.menu;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import net.dungeonrealms.game.player.menu.CraftingMenu;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.entity.util.PetUtils;
import net.minecraft.server.v1_9_R2.Entity;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Kieran Quigley (Proxying) on 29-May-16.
 */
public class CommandPet extends BaseCommand {

    public CommandPet(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            return false;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
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
                return true;
            }

            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);

            if(wrapper == null) return false;
            String petType = wrapper.getActivePet();
            if (petType == null || petType.equals("")) {
                player.sendMessage(ChatColor.RED + "You currently don't have an active pet. Please select one from your profile.");
                player.closeInventory();
                return true;
            }
            String petName;
            String petToSummon;
            if (petType.contains("@")) {
                petToSummon = petType.split("@")[0];
                petName = petType.split("@")[1];
            } else {
                petToSummon = petType;
                petName = EnumPets.getByName(petToSummon).getDisplayName();
            }
            player.sendMessage(ChatColor.GREEN + "Your pet has been summoned.");
            PetUtils.spawnPet(player.getUniqueId(), petToSummon, petName);
            return true;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("open") || args[0].equalsIgnoreCase("o") || args[0].equalsIgnoreCase("view") || args[0].equalsIgnoreCase("v")) {
                PlayerMenus.openPlayerPetMenu(player);
                return true;
            } else if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("a") || args[0].equalsIgnoreCase("g") || args[0].equalsIgnoreCase("get")) {
                CraftingMenu.addPetItem(player);
                return true;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
}
