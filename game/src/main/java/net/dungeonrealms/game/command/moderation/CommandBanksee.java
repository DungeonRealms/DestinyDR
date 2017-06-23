package net.dungeonrealms.game.command.moderation;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabase;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;

import net.dungeonrealms.game.player.inventory.menus.guis.support.CharacterSelectionGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Brad on 25/12/2016.
 */
public class CommandBanksee extends BaseCommand {
    public CommandBanksee() {
        super("banksee", "/<command> [args]", "View a player's bank.", Collections.singletonList("mbs"));
    }

    public static Map<UUID, UUID> offline_bank_watchers = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player sender = (Player) s;

        if (!Rank.isGM(sender)) return false;

        if (args.length == 0) {
            s.sendMessage(usage);
            return true;
        }

        String playerName = args[0];
        if (Bukkit.getPlayer(playerName) != null) {
            Storage storage = BankMechanics.getStorage(Bukkit.getPlayer(playerName).getUniqueId());
            sender.openInventory(storage.inv);
        } else {

            SQLDatabaseAPI.getInstance().getUUIDFromName(playerName, false, (uuid) -> {
                if(uuid == null) {
                    sender.sendMessage(ChatColor.RED + "This player has never logged in with Dungeon Realms");
                    return;
                }

                Integer accountID = SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid);
                if(accountID == null) {
                    sender.sendMessage(ChatColor.RED + "This player has never logged in with Dungeon Realms");
                    return;
                }

                new CharacterSelectionGUI(sender, accountID, (charID) -> {
                    PlayerWrapper.getPlayerWrapper(uuid, charID,false, false, (wrapper) -> {
                        if(wrapper == null) {
                            sender.sendMessage(ChatColor.RED + "Something went wrong while loading the data!");
                            return;
                        }

                        if(wrapper.isPlaying()) {
                            String shard = wrapper.getFormattedShardName();
                            sender.sendMessage(ChatColor.RED + "That player is currently playing on shard " + shard + ". " +
                                    "Please banksee on that shard to avoid concurrent modification.");

                            return;
                        }

                        Storage storage = BankMechanics.getStorage(uuid);
                        if(storage == null) {
                            storage = wrapper.getPendingBankStorage();
                            if(storage == null) {
                                sender.sendMessage(ChatColor.RED + "Something went wrong while loading the bank.");
                                return;
                            }
                        }
                        sender.openInventory(storage.inv);
                        offline_bank_watchers.put(sender.getUniqueId(), uuid);
                    });
                }).open(sender,null);

            });

        }
        return false;
    }
}
