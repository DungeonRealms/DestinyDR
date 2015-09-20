/**
 * 
 */
package net.dungeonrealms.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.party.PartyMechanics;

public class CommandParty implements CommandExecutor {

   @Override
   public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {

       if (s instanceof ConsoleCommandSender) {
           return false;
       }

       Player player = (Player) s;
       UUID uuid = player.getUniqueId();

       if (args.length > 0) {
           String command = args[0].toLowerCase();
           switch (command) {
               case "create":
                   PartyMechanics.getInstance().createParty(uuid);
                   break;
               case "invite":
                   PartyMechanics.getInstance().inviteToParty(PartyMechanics.getInstance().getParty(uuid), Bukkit.getPlayer(args[1]).getUniqueId());
                   break;
               case "disband":
                   PartyMechanics.getInstance().disbandParty(PartyMechanics.getInstance().getParty(uuid));
                   break;
               case "kick":
                   PartyMechanics.getInstance().kickPlayer(PartyMechanics.getInstance().getParty(uuid), Bukkit.getPlayer(args[1]).getUniqueId());
                   break;
               default:
                   player.sendMessage("ERROR DEFAULT CALLED()..");
           }
       }
       return false;
   }

}