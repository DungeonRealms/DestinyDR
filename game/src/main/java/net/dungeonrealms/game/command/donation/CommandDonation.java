package net.dungeonrealms.game.command.donation;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.Purchaseables;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;


/**
 * Created by Rar349 on 5/13/2017.
 */
public class CommandDonation extends BaseCommand {
    public CommandDonation() {
        super("drdonation", "/<command> <action> <player> <purchaseable> <amount>", "Add an item to the players pending purchases");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean hasPerms = Rank.isDev(sender) || (sender instanceof ConsoleCommandSender);
        if(!hasPerms) return true;

        if(args.length < 1) return false;

        String action = args[0];
        if(action.equalsIgnoreCase("help")) {
                sender.sendMessage("ACTION: add");
                sender.sendMessage("ACTION: remove");
                sender.sendMessage("ACTION: chargeback");
                sender.sendMessage("");
                for(Purchaseables item : Purchaseables.values()) {
                    if(!item.isShouldStore()) continue;
                    sender.sendMessage("PURCHASEABLE: " + item.name());
                }
                return true;
        }

        if(action.equalsIgnoreCase("removepending")) {
            if(args.length < 3) {
                sender.sendMessage("Use /drdonation removepending <player> <transactionId>");
            }
            String playerName = args[1];
            int transactionID;
            try {
                transactionID = Integer.parseInt(args[2]);
            } catch(Exception e) {
                sender.sendMessage("The transaction id must be an integer!");
                return true;
            }

            SQLDatabaseAPI.getInstance().getUUIDFromName(playerName,false,(uid) -> {
                if(uid == null) {
                    sender.sendMessage("This player has never logged into dungeon realms!");
                    return;
                }
                PlayerWrapper.getPlayerWrapper(uid, (wrapper) -> {
                    if(wrapper == null) {
                        sender.sendMessage("Something went wrong loading the playerwrapper!");
                        return;
               }

                    boolean removed = Purchaseables.removePending(wrapper,transactionID,true);
                    sender.sendMessage(removed ? "Successfully removed the transaction!" : "Could not find any pending purchases with that transaction id");
                    return;
                });
            });

            return true;
        }

        if(args.length < 4) return false;

        String playerName = args[1];
        int amount;
        Purchaseables item;
        try {
            item = Purchaseables.valueOf(args[2]);
            amount = Integer.parseInt(args[3]);
        } catch(NumberFormatException e) {
            sender.sendMessage("Amount must be a number!");
            return true;
        } catch(Exception e) {
            sender.sendMessage("Invalid purchaseable: " + args[2] + "Use '/drdonation help' for help!");
            return true;
        }

        if(item == null) return true;

        if(!item.isShouldStore()) {
            sender.sendMessage("This is a special case item. It can not be used with /drdonation");
            return true;
        }


        if(action.equalsIgnoreCase("add") || action.equalsIgnoreCase("remove")) {
            boolean isAdd = action.equalsIgnoreCase("add");
            if(args.length < 5) {
                sender.sendMessage("You must include a pending boolean at the end of the command for add / remove!");
                sender.sendMessage("/drdonation <action> <player> <purchaseable> <amount> <fromPending>");
                return true;
            }

            boolean fromPending;
            try {
                fromPending = Boolean.valueOf(args[4]);
            } catch(Exception e) {
                sender.sendMessage("fromPending must be a true or false!");
                return true;
            }
            if (isAdd && amount < 1) {
                sender.sendMessage("Use the action 'remove' to remove items!");
                return true;
            } else if(!isAdd && amount < 1 && amount != -1) {
                sender.sendMessage("Please use only positive amounts or -1 to remove all occurances.");
                return true;
            }

            SQLDatabaseAPI.getInstance().getUUIDFromName(playerName,false, (uuid) -> {
                if(uuid == null) {
                    sender.sendMessage("That player has never logged onto Dungeon Realms!");
                    return;
                }
                PlayerWrapper.getPlayerWrapper(uuid, (wrapper) -> {
                    if(wrapper == null) {
                        sender.sendMessage("Something went wrong while fetching the players wrapper!");
                        return;
                    }

                    if(isAdd && !fromPending) {
                        int returnCode = item.addNumberUnlocked(wrapper, amount);
                        if (returnCode == Purchaseables.NO_MULTIPLES) {
                            sender.sendMessage("This player already has this item unlocked and they can not have multiples!");
                        } else if (returnCode == Purchaseables.SUCCESS) {
                            sender.sendMessage("Success! Added " + amount + " " + item.name() + " to " + playerName + "!");
                        } else {
                            sender.sendMessage("Unknown return code!");
                        }
                    } else if(!isAdd && fromPending) {
                        int returnCode = item.removeNumberPending(wrapper,amount,true);
                        if (returnCode == Purchaseables.NONE_OWNED) {
                            sender.sendMessage(playerName + " did not have any " + item.name());
                        } else if (returnCode == Purchaseables.SUCCESS) {
                            sender.sendMessage("Success! Removed " + amount + " " + item.name() + " to " + playerName + "!");
                        } else if (returnCode == Purchaseables.SUCESS_REMOVED_ALL) {
                            sender.sendMessage("Success! Removed " + amount + " " + item.name() + " to " + playerName + "! They no longer have any!");
                        } else {
                            sender.sendMessage("Unknown return code!");
                        }
                    }else if(isAdd && fromPending) {
                        int returnCode = item.addNumberPending(wrapper,amount,sender.getName(),Utils.getDateString(),-1,true);
                        if (returnCode == Purchaseables.NO_MULTIPLES) {
                            sender.sendMessage("This player already has this item unlocked and they can not have multiples!");
                        } else if (returnCode == Purchaseables.SUCCESS) {
                            sender.sendMessage("Success! Added " + amount + " " + item.name() + " to " + playerName + "!");
                        } else {
                            sender.sendMessage("Unknown return code!");
                        }
                    } else if(!isAdd && !fromPending) {
                        int returnCode = item.removeNumberUnlocked(wrapper,amount);
                        if (returnCode == Purchaseables.NONE_OWNED) {
                            sender.sendMessage(playerName + " did not have any " + item.name());
                        } else if (returnCode == Purchaseables.SUCCESS) {
                            sender.sendMessage("Success! Removed " + amount + " " + item.name() + " to " + playerName + "!");
                        } else if (returnCode == Purchaseables.SUCESS_REMOVED_ALL) {
                            sender.sendMessage("Success! Removed " + amount + " " + item.name() + " to " + playerName + "! They no longer have any!");
                        } else {
                            sender.sendMessage("Unknown return code!");
                        }
                    }
                });
            });

        }

        return true;
    }
}
