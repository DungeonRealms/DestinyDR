package net.dungeonrealms.game.command.menu;

import com.google.common.collect.Lists;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.item.items.functional.ecash.ItemPet;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.PetSelectionGUI;
import net.dungeonrealms.game.world.entity.type.pet.CustomPet;
import net.dungeonrealms.game.world.entity.util.PetUtils;
import net.minecraft.server.v1_9_R2.WorldServer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CommandPet extends BaseCommand {

    public CommandPet() {
        super("pet", "/<command>", "Opens the player pets menu.", null, Lists.newArrayList("pets"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;
        if (label.equals("pets")) {
            if (CombatLog.isInCombat(player)) {
                player.sendMessage(ChatColor.RED + "You cannot do this while in combat!");
                return true;
            }
            new PetSelectionGUI(player, null).open(player, null);
            return true;
        }
        if (args.length == 0) {
            if (PetUtils.hasActivePet(player)) {
                PetUtils.removePet(player, false);
            } else {
                ItemPet.spawnPet(player);
            }
        } else if (args.length == 1 && args[0].equals("custom") && player.isOp()) {

            WorldServer world = ((CraftWorld) player.getWorld()).getHandle();

            CustomPet pet = new CustomPet(world, player);
            Location l = player.getLocation();
            pet.setLocation(l.getX(), l.getY(), l.getZ(), 0, 0);
            world.addEntity(pet, CreatureSpawnEvent.SpawnReason.CUSTOM);
            pet.setLocation(l.getX(), l.getY(), l.getZ(), 0, 0);

            pet.setupArmorStands();
        }

        return true;
    }
}
