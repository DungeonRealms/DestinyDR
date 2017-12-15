package net.dungeonrealms.game.command.test;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandTestParticles extends BaseCommand{

    public CommandTestParticles(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;

        if (!Rank.isDev(player)) return false;
        Location location = player.getLocation().clone();

        new BukkitRunnable() {
            @Override
            public void run() {
                playExclamationPoint(location, 7, 0.35);
            }
        }.runTaskTimerAsynchronously(DungeonRealms.getInstance(), 0, 3);
        return true;
    }



    public void playExclamationPoint(Location location, int particleAmount ,double height){

        drawSphere(location, particleAmount, 0.01);
        drawOval(location.add(0, 1, 0), particleAmount, 0.0375, height);
        location.subtract(0, 1, 0);

    }

    public void drawSphere(Location loc, int particleAmount ,double r){
        for (double i = 0; i <= Math.PI; i += Math.PI / particleAmount) {
            double radius = Math.sin(i)*r;
            double y = Math.cos(i)*r;
            for (double a = 0; a < Math.PI * 2; a+= Math.PI / particleAmount) {
                double x = Math.cos(a) * radius;
                double z = Math.sin(a) * radius;
                loc.add(x, y, z);
                float red = 0x42;
                float green = 0xf4;
                float blue = 0x42;
                loc.getWorld().spigot().playEffect(loc, Effect.COLOURED_DUST, 0, 0, red/255, green/255, blue/255, 1, 0, 25);
                loc.subtract(x, y, z);
            }
        }
    }

    public void drawOval(Location loc, int particleAmount ,double r, double h){
        for (double i = 0; i <= Math.PI; i += Math.PI / particleAmount) {
            double radius = Math.sin(i)*r;
            double y = Math.cos(i)*h;
            for (double a = 0; a < Math.PI * 2; a+= Math.PI / particleAmount) {
                double x = Math.cos(a) * radius;
                double z = Math.sin(a) * radius;
                float red = 0x42;
                float green = 0xf4;
                float blue = 0x42;
                loc.add(x, y, z);
                loc.getWorld().spigot().playEffect(loc, Effect.COLOURED_DUST, 0, 0, red/255, green/255, blue/255, 1, 0, 25);
                loc.subtract(x, y, z);
            }
        }
    }
}
