package net.dungeonrealms.game.command.test;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/2/2016
 */
public class CommandTestAntidupe extends BaseCommand {

    public CommandTestAntidupe(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp() && !(sender instanceof ConsoleCommandSender)) return false;

        Player p = (Player) sender;
        Inventory muleInv = MountUtils.inventories.get(p.getUniqueId());
        Storage storage = BankMechanics.getInstance().getStorage(p.getUniqueId());

        AntiDuplication.checkForSuspiciousDupedItems(p, new HashSet<>(Arrays.asList(p.getInventory(), storage.inv, storage.collection_bin, muleInv)));
        return false;
    }


}