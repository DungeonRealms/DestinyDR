package net.dungeonrealms.game.command.test;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.mastery.NMSUtils;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.world.Albranir;
import net.minecraft.server.v1_9_R2.EntityPigZombie;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/18/2016
 */
public class CommandAlbranir extends BaseCommand {

    public CommandAlbranir(String command, String usage, String description) {
        super(command, usage, description);
        new NMSUtils().registerEntity("Albranir", 57, EntityPigZombie.class, Albranir.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;

        if (!Rank.isDev(player)) return false;

        Albranir albranir = new Albranir(((CraftWorld) player.getWorld()).getHandle(), player.getLocation());
        ((CraftWorld) player.getWorld()).getHandle().addEntity(albranir, CreatureSpawnEvent.SpawnReason.CUSTOM);
        albranir.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 1, 1);

        return true;
    }

}
