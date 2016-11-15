package net.dungeonrealms.old.game.command.menu;

import net.dungeonrealms.common.frontend.command.BaseCommand;
import net.dungeonrealms.common.old.game.database.DatabaseAPI;
import net.dungeonrealms.common.old.game.database.data.EnumData;
import net.dungeonrealms.old.game.donation.DonationEffects;
import net.dungeonrealms.old.game.player.inventory.PlayerMenus;
import net.dungeonrealms.old.game.player.menu.Profile;
import net.dungeonrealms.old.game.world.entity.type.pet.EnumPets;
import net.dungeonrealms.old.game.world.entity.util.EntityAPI;
import net.dungeonrealms.old.game.world.entity.util.PetUtils;
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

            String petType = (String) DatabaseAPI.getInstance().getData(EnumData.ACTIVE_PET, player.getUniqueId());
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
                Profile.addPetItem(player);
                return true;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
}
