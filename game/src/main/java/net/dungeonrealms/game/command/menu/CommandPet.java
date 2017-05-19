package net.dungeonrealms.game.command.menu;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.item.items.functional.ecash.ItemPet;
import net.dungeonrealms.game.world.entity.type.pet.CustomPet;
import net.minecraft.server.v1_9_R2.WorldServer;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CommandPet extends BaseCommand {

    public CommandPet() {
        super("pet", "/<command>", "Opens the player pets menu.", "pets");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player))
            return false;
        
        Player player = (Player) sender;
        if (args.length == 0) {
            ItemPet.spawnPet(player);
        }else if(args.length == 1 && args[0].equals("custom") && player.isOp()){

            WorldServer world = ((CraftWorld)player.getWorld()).getHandle();

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
