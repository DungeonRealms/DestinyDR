package net.dungeonrealms.game.guild;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

public class GuildMechanics implements GenericMechanic {

    private static GuildMechanics instance = null;

    public static GuildMechanics getInstance() {
        if (instance == null) {
            instance = new GuildMechanics();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.ARCHBISHOPS;
    }


    @Override
    public void startInitialization() {


    }

    @Override
    public void stopInvocation() {

    }

    public void doLogin(Player player) {


    }


    public void sendGuildMessage(Player player, String[] message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Guilds");

        Arrays.asList(message).stream().forEach(out::writeUTF);

        player.sendPluginMessage(DungeonRealms.getInstance(), "DungeonRealms", out.toByteArray());
    }


}
