package net.dungeonrealms.game.command;

import com.google.common.collect.Lists;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.tool.PatchTools;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/10/2016
 */
public class CommandPatchNotes extends BaseCommand {

    public CommandPatchNotes() {
        super("patchnotes", "/<command>", "Shows patch for current build", null, Lists.newArrayList("patch"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player)
        	GameAPI.openBook((Player)sender, PatchTools.getPatchBook());
        return true;
    }
}
