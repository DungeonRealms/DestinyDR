package net.dungeonrealms.game.command.donation;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.punishment.PunishAPI;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.PendingPurchaseable;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.Purchaseables;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;


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
            String transactionID = args[2];

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
                    GameAPI.sendNetworkMessage("donation", uid.toString());
                    return;
                });
            });

            return true;
        } else if(action.equalsIgnoreCase("chargeback")) {
            if(args.length < 3) {
                sender.sendMessage("Please use /drdonation chargeback <uuid> <transactionID>");
                return true;
            }
            UUID playerId;

            try {
                playerId = UUID.fromString(args[1]);
            } catch(Exception e) {
                sender.sendMessage("Invalid uuid specified!");
                Constants.log.info("/drdonation chargeback was used with an invalid UUID. The UUID: " + args[1]);
                return true;
            }

            String transactionID = args[2];

            PlayerWrapper.getPlayerWrapper(playerId, false, true, (wrapper) -> {
                if(wrapper == null) {
                    Constants.log.info("/drdonation chargeback was used with a UUID that has never logged in. The UUID: " + args[1]);
                    sender.sendMessage("/drdonation chargeback was used with a UUID that has never logged in. The UUID: " + args[1]);
                    return;
                }


                for(int k = 0; k < wrapper.getPendingPurchaseablesUnlocked().size(); k++) {
                    PendingPurchaseable pending = wrapper.getPendingPurchaseablesUnlocked().get(k);
                    if(pending == null) continue;
                    if(pending.getTransactionId() == transactionID) {
                        Constants.log.info("The UUID '" + args[1] + "' charged back a pending purchase so we didn't punish him!");
                        sender.sendMessage("The UUID '" + args[1] + "' charged back a pending purchase so we didn't punish him!");
                        wrapper.getPendingPurchaseablesUnlocked().remove(k);
                        wrapper.saveData(true,null);
                        return;
                    }
                }

                StringBuilder discordMessage = new StringBuilder(wrapper.getPlayerName());
                discordMessage.append(" has been perm banned for charging back!");

                //It's not in his pending so he must of claimed it so ban him.
                GameAPI.sendNetworkMessage("BanMessage", sender.getName() + ": " + discordMessage);

                Player online = Bukkit.getPlayer(playerId);
                if (online != null) {
                    CombatLog.removeFromCombat(online);
                    CombatLog.removeFromPVP(online);
                }

                sender.sendMessage(discordMessage.toString());
                PunishAPI.ban(playerId, wrapper.getPlayerName(), 0, 0, "Charge Back", null);

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

            String transactionID = "-1";

            if(isAdd && fromPending) {
                if(args.length < 6) {
                    sender.sendMessage("No transaction ID specified. Using -1 as a default.");
                    sender.sendMessage("To specify a transaction ID use '/drdonation add <player> <purchaseable> <amount> <fromPending> <transactionID>'");
                } else {
                    transactionID = args[5];
                }
            }

            final String realTransactionID = transactionID;

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
                            GameAPI.sendNetworkMessage("donation", uuid.toString());
                        } else {
                            sender.sendMessage("Unknown return code!");
                        }
                    } else if(!isAdd && fromPending) {
                        int returnCode = item.removeNumberPending(wrapper,amount,true);
                        if (returnCode == Purchaseables.NONE_OWNED) {
                            sender.sendMessage(playerName + " did not have any " + item.name());
                        } else if (returnCode == Purchaseables.SUCCESS) {
                            sender.sendMessage("Success! Removed " + amount + " " + item.name() + " to " + playerName + "!");
                            GameAPI.sendNetworkMessage("donation", uuid.toString());
                        } else if (returnCode == Purchaseables.SUCESS_REMOVED_ALL) {
                            sender.sendMessage("Success! Removed " + amount + " " + item.name() + " to " + playerName + "! They no longer have any!");
                            GameAPI.sendNetworkMessage("donation", uuid.toString());
                        } else {
                            sender.sendMessage("Unknown return code!");
                        }
                    } else if(isAdd && fromPending) {
                        int returnCode = item.addNumberPending(wrapper,amount,sender.getName(),Utils.getDateString(),realTransactionID,true);
                        String uuidString =((sender instanceof Player) ? ((Player)sender).getUniqueId().toString() : "-1");
                        //-1 for ^ if console
                        wrapper.updatePurchaseLog("addedPending", realTransactionID, System.currentTimeMillis(), uuidString);
                        if (returnCode == Purchaseables.NO_MULTIPLES) {
                            sender.sendMessage("This player already has this item unlocked and they can not have multiples!");
                        } else if (returnCode == Purchaseables.SUCCESS) {
                            sender.sendMessage("Success! Added " + amount + " " + item.name() + " to " + playerName + "!");
                            GameAPI.sendNetworkMessage("donation", uuid.toString());
                        } else {
                            sender.sendMessage("Unknown return code!");
                        }
                    } else if(!isAdd && !fromPending) {
                        int returnCode = item.removeNumberUnlocked(wrapper,amount);
                        if (returnCode == Purchaseables.NONE_OWNED) {
                            sender.sendMessage(playerName + " did not have any " + item.name());
                        } else if (returnCode == Purchaseables.SUCCESS) {
                            sender.sendMessage("Success! Removed " + amount + " " + item.name() + " to " + playerName + "!");
                            GameAPI.sendNetworkMessage("donation", uuid.toString());
                        } else if (returnCode == Purchaseables.SUCESS_REMOVED_ALL) {
                            sender.sendMessage("Success! Removed " + amount + " " + item.name() + " to " + playerName + "! They no longer have any!");
                            GameAPI.sendNetworkMessage("donation", uuid.toString());
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
