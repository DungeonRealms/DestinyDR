package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.PlayerRank;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.UpdateType;
import net.dungeonrealms.game.item.items.functional.ecash.ItemNameTag;
import net.dungeonrealms.game.item.items.functional.ecash.jukebox.ItemJukebox;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.ParticleAPI.ParticleEffect;
import net.dungeonrealms.game.player.banks.CurrencyTab;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.Purchaseables;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.dungeonrealms.game.world.entity.type.pet.PetData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

/**
 * Created by Kieran on 10/9/2015.
 */
public class CommandEss extends BaseCommand {

    public CommandEss() {
        super("dr", "/<command> [args]", "Developer command with the core essentials.");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            // You must be a support or GM to use this!
            if (!Rank.isGM(player) && !Rank.isSupport(player))
                return false;

            // Support Agents can only use this command on the support shard.
            if (!Rank.isGM(player) && Rank.isSupport(player))
                if (!DungeonRealms.isSupport())
                    return false;

                    // Extended Permission Check
                else if (!Rank.isHeadGM(player) && !DungeonRealms.getInstance().isGMExtendedPermissions) {
                    commandSender.sendMessage(ChatColor.RED + "You don't have permission to execute this command.");
                    return false;
                }
        } else if (!(commandSender instanceof ConsoleCommandSender)) {
            return false;
        }

        if (args.length > 0) {

            switch (args[0]) {
                case "pack":
                    ((Player) commandSender).setResourcePack(Constants.RESOURCE_PACK);
                    break;
                case "currencytab":
                case "scraptab":

                    if (args.length != 3) {
                        commandSender.sendMessage(ChatColor.RED + "/dr currencytab <name> true/false");
                        return true;
                    }

                    SQLDatabaseAPI.getInstance().getUUIDFromName(args[1], true, (id) -> {

                        if (id == null) {
                            commandSender.sendMessage(ChatColor.RED + "Failed to find a user with the name " + ChatColor.UNDERLINE + args[1] + ChatColor.RED + ".");
                            return;
                        }

                        boolean access;
                        try {
                            access = Boolean.parseBoolean(args[2]);
                        } catch (Exception e) {
                            commandSender.sendMessage(ChatColor.RED + "Invalid: " + args[2]);
                            return;
                        }

                        Player online = Bukkit.getPlayer(id);
                        if (online != null) {
                            if (access) {
                                online.sendMessage(ChatColor.GREEN + "You now have access to the Scrap Tab!");
                            }

                            PlayerWrapper.getPlayerWrapper(online.getUniqueId(), false, true, (wrapper) -> {
                                CurrencyTab tab = wrapper.getCurrencyTab();
                                if (tab != null) {
                                    tab.hasAccess = access;
                                } else if (access) {
                                    tab = new CurrencyTab(online.getUniqueId());
                                    tab.hasAccess = true;
                                    wrapper.setCurrencyTab(tab);
                                }
                                commandSender.sendMessage(ChatColor.RED + "Scrap tab set to " + access + " for " + args[1]);

                            });
                        }
                    });
                    return true;
                case "hearthstone":
                    commandSender.sendMessage("Disabled.");
                    break;
                case "pet":
                    if (args.length == 3) {
                        String playerName = args[1];
                        String petType = args[2];
                        String petName;
                        petName = petType;
                        String petNameFriendly = petName.toUpperCase().replace("_", " ");

                        if (!GameAPI.isStringPet(petName)) {
                            commandSender.sendMessage(ChatColor.RED + "The pet " + ChatColor.BOLD + ChatColor.UNDERLINE + petNameFriendly + ChatColor.RED + " does not exist.");
                            return false;
                        }

                        SQLDatabaseAPI.getInstance().getUUIDFromName(playerName, false, (uuid) -> {
                            if (uuid == null) {
                                commandSender.sendMessage(ChatColor.RED + "This person has never logged into Dungeon Realms");
                                return;
                            }
                            PlayerWrapper.getPlayerWrapper(uuid, false, true, (wrapper) -> {
                                if (wrapper == null) {
                                    commandSender.sendMessage(ChatColor.RED + "Something went wrong when loading the data!");
                                    return;
                                }
                                EnumPets pet = EnumPets.getByName(petType);
                                wrapper.setActivePet(pet);
                                wrapper.getPetsUnlocked().put(pet, new PetData(null, true));

                                SQLDatabaseAPI.getInstance().executeQuery(QueryType.SET_PETS.getQuery(wrapper.getSerializePetString(), wrapper.getAccountID()), cb -> {
                                    commandSender.sendMessage(ChatColor.GREEN + "Successfully added the " + ChatColor.BOLD + ChatColor.UNDERLINE + petNameFriendly + ChatColor.GREEN + " pet to " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
                                    GameAPI.updatePlayerData(uuid, UpdateType.UNLOCKABLES);
                                });
                            });
                        });
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Invalid usage! /dr pet <player> <pet>");
                        return false;
                    }
                    break;
                case "namtag":
                case "storeitems":
                case "store":
                    GameAPI.giveOrDropItem((Player) commandSender, new ItemNameTag().generateItem());
                    GameAPI.giveOrDropItem((Player) commandSender, new ItemJukebox().generateItem());
                    return true;
                case "mount":
                    if (args.length == 3) {
                        String playerName = args[1];
                        String mountType = args[2];

                        String mountFriendly = mountType.toUpperCase().replace("_", " ");

                        EnumMounts mount = EnumMounts.getByName(mountType);
                        if (!GameAPI.isStringMount(mountType) || mount == null) {
                            commandSender.sendMessage(ChatColor.RED + "The mount " + ChatColor.BOLD + ChatColor.UNDERLINE + mountFriendly + ChatColor.RED + " does not exist.");
                            return false;
                        }

                        SQLDatabaseAPI.getInstance().getUUIDFromName(playerName, false, uuid -> {
                            if (uuid == null) {
                                commandSender.sendMessage(ChatColor.RED + "This player has never logged into Dungeon Realms");
                                return;
                            }

                            PlayerWrapper.getPlayerWrapper(uuid, false, true, wrapper -> {
                                if (wrapper == null) {
                                    commandSender.sendMessage(ChatColor.RED + "Something went wrong loading the data");
                                    return;
                                }
                                Set<EnumMounts> playerMounts = wrapper.getMountsUnlocked();
//                                Player found = Bukkit.getPlayer(uuid);
//                                if (found != null) {
//                                    if (mount.getMountData() != null) {
//                                        found.getInventory().addItem(mount.getMountData().createMountItem(mount));
//                                        commandSender.sendMessage(ChatColor.RED + "Mount given to " + found.getName());
//                                        return;
//                                    }
//                                }
                                if (playerMounts.contains(mount)) {
                                    commandSender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + playerName + ChatColor.RED + " already has the " + ChatColor.BOLD + ChatColor.UNDERLINE + mountFriendly + ChatColor.RED + " mount.");
                                    return;
                                }

                                playerMounts.add(mount);
                                wrapper.setActiveMount(mount);


                                SQLDatabaseAPI.getInstance().executeQuery(wrapper.getQuery(QueryType.SET_MOUNTS, wrapper.getMountsUnlocked(), wrapper.getAccountID()), cb -> {
                                    commandSender.sendMessage(ChatColor.GREEN + "Successfully added the " + ChatColor.BOLD + ChatColor.UNDERLINE + mountFriendly + ChatColor.GREEN + " mount to " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
                                    GameAPI.updatePlayerData(uuid, UpdateType.UNLOCKABLES);
                                });
                            });
                        });

                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Invalid usage! /dr mount <player> <mount>");
                        return false;
                    }
                    break;
                case "trail":
                case "playertrail":
                    if (args.length == 3) {
                        String playerName = args[1];
                        String trailType = args[2];
                        ParticleEffect trail = ParticleEffect.getByName(trailType);
                        String trailFriendly = trailType.toUpperCase().replace("_", " ");

                        if (ParticleAPI.ParticleEffect.getByName(trailType) == null) {
                            commandSender.sendMessage(ChatColor.RED + "The trail " + ChatColor.BOLD + ChatColor.UNDERLINE + trailFriendly + ChatColor.RED + " does not exist.");
                            return false;
                        }

                        SQLDatabaseAPI.getInstance().getUUIDFromName(playerName, false, (uuid) -> {
                            if (uuid == null) {
                                commandSender.sendMessage(ChatColor.RED + "This player has never logged into Dungeon Realms!");
                                return;
                            }

                            PlayerWrapper.getPlayerWrapper(uuid, false, true, (wrapper) -> {
                                if (wrapper == null) {
                                    commandSender.sendMessage(ChatColor.RED + "Could not load player data!");
                                    return;
                                }
                                Set<ParticleEffect> playerTrails = wrapper.getParticles();
                                if (!playerTrails.isEmpty()) {
//                                    if (playerTrails.contains(trail)) {
//                                        commandSender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + playerName + ChatColor.RED + " already has the " + ChatColor.BOLD + ChatColor.UNDERLINE + trailFriendly + ChatColor.RED + " trail.");
//                                        return;
//                                    }
                                }

                                if (trail == ParticleEffect.GOLD_BLOCK) {
                                    Purchaseables.GOLDEN_CURSE.setNumberOwned(wrapper, 1);
                                }
                                wrapper.getParticles().add(trail);
                                wrapper.setActiveTrail(trail);
                                commandSender.sendMessage(ChatColor.GREEN + "Successfully added the " + ChatColor.BOLD + ChatColor.UNDERLINE + trailFriendly + ChatColor.GREEN + " trail to " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
                                GameAPI.updatePlayerData(uuid, UpdateType.UNLOCKABLES);
                            });
                        });

                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Invalid usage! /dr trail <player> <trail>");
                        return false;
                    }
                    break;
                case "ecash":
                    if (args.length == 4) {
                        String playerName = args[1];


                        switch (args[2]) {
                            case "set":
                                SQLDatabaseAPI.getInstance().getUUIDFromName(playerName, false, uuid -> {
                                    if (uuid == null) {
                                        commandSender.sendMessage(ChatColor.RED + "This player has never logged into Dungeon Realms");
                                        return;
                                    }

                                    PlayerWrapper.getPlayerWrapper(uuid, false, true, wrapper -> {
                                        if (uuid == null) {
                                            commandSender.sendMessage(ChatColor.RED + "Ingot > iFamasssxD");
                                            return;
                                        }

                                        int amount = Math.abs(Integer.parseInt(args[3]));

                                        SQLDatabaseAPI.getInstance().executeUpdate((rows) -> {
                                            commandSender.sendMessage(ChatColor.GREEN + "Successfully set the E-Cash of " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + " to " + ChatColor.BOLD + ChatColor.UNDERLINE + amount + ChatColor.GREEN + ".");
                                            GameAPI.updatePlayerData(uuid, UpdateType.ECASH);
                                        }, QueryType.SET_ECASH.getQuery(amount, wrapper.getAccountID()));
                                    });
                                });
                                break;
                            default:
                                commandSender.sendMessage(ChatColor.RED + "Invalid modification type, please use: SET");
                                return false;
                        }
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Invalid usage! /dr ecash <player> <add|set|remove> <amount>");
                        return false;
                    }
                    break;
                case "sub":
                case "subscription":
                    if (args.length == 5) {
                        String playerName = args[1];
                        String rankName = args[2].toUpperCase();
                        String modifyType = args[3].toLowerCase();
                        SQLDatabaseAPI.getInstance().getUUIDFromName(playerName, false, (uuid) -> {
                            if (uuid == null) {
                                commandSender.sendMessage(ChatColor.RED + "This player has never logged into Dungeon Realms");
                                return;
                            }

                            PlayerWrapper.getPlayerWrapper(uuid, false, true, (wrapper) -> {
                                if (wrapper == null) {
                                    commandSender.sendMessage(ChatColor.RED + "Could not load player data!");
                                    return;
                                }

                                PlayerRank currentRank = wrapper.getRank();

                                int days = Integer.parseInt(args[4]) * 86400;
                                int subscriptionLength = wrapper.getRankExpiration();

                                if (rankName.equalsIgnoreCase("sub") || rankName.equalsIgnoreCase("sub+")) {
                                    if (currentRank.isAtLeast(PlayerRank.BUILDER)) {
                                        commandSender.sendMessage(ChatColor.RED + "Cannot change the rank of " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.RED + ", they're currently " + ChatColor.BOLD + ChatColor.UNDERLINE + currentRank.getPrefix() + ChatColor.RED + "!");
                                        return;
                                    }

                                    if (modifyType.equalsIgnoreCase("add") && subscriptionLength > 0) {
                                        subscriptionLength = subscriptionLength + days;
                                    } else if (modifyType.equalsIgnoreCase("set") || (modifyType.equalsIgnoreCase("add") && subscriptionLength <= 0)) {
                                        subscriptionLength = (int) (System.currentTimeMillis() / 1000) + days;
                                    } else if (modifyType.equalsIgnoreCase("remove")) {
                                        subscriptionLength = subscriptionLength - days;
                                        if (subscriptionLength < 0) subscriptionLength = 0;
                                    } else {
                                        commandSender.sendMessage(ChatColor.RED + "Invalid modification type, please use: ADD | SET | REMOVE");
                                        return;
                                    }
                                    int finalSubLength = subscriptionLength;
                                    wrapper.setRank(PlayerRank.getFromPrefix(rankName));
                                    wrapper.setRankExpiration(finalSubLength);
                                    SQLDatabaseAPI.getInstance().executeQuery(wrapper.getQuery(QueryType.UPDATE_RANK, PlayerRank.getFromPrefix(rankName).getInternalName(), finalSubLength, wrapper.getAccountID()), cb -> {
                                        GameAPI.updatePlayerData(uuid, UpdateType.RANK);
                                        commandSender.sendMessage(ChatColor.GREEN + "Successfully updated the subscription of " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
                                    });
                                } else {
                                    commandSender.sendMessage(ChatColor.RED + "Invalid rank, please use: SUB | SUB+");
                                    return;
                                }

                            });
                        });
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Invalid usage! /dr subscription <name> <rank> <add|set|remove> <days>");
                        return false;
                    }
                    break;
                case "purchase":
                    if (args.length >= 4) {
                        try {
                            String playerName = args[1];
                            UUID uuid;
                            String type = args[2].toLowerCase();
                            String rankName = args[3].toUpperCase();
                            Player online = Bukkit.getPlayer(playerName);
                            if (online != null) {
                                uuid = online.getUniqueId();
                            } else {
                                SQLDatabaseAPI.getInstance().getUUIDFromName(playerName, false, id -> {
                                    setRank(commandSender, id, playerName, type, rankName);
                                });
                                return true;
                            }
                            setRank(commandSender, uuid, type, playerName, rankName);
                        } catch (IllegalArgumentException ex) {
                            commandSender.sendMessage(ChatColor.RED + "I couldn't find the  user " + ChatColor.BOLD + ChatColor.UNDERLINE + args[1] + ChatColor.RED + ", maybe they've not played Dungeon Realms before?");
                            return false;
                        }
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Invalid usage /dr purchase <player> <type> [args]");
                        return false;
                    }
                    break;
                case "resetmule":
                    PlayerWrapper.getPlayerWrapper((Player) commandSender).setMuleLevel(1);
                    commandSender.sendMessage(ChatColor.GREEN + "Your mule level has been reset.");
                    break;
                case "buff":
                    if (args.length != 4) {
                        commandSender.sendMessage(ChatColor.RED + "Syntax: /dr buff <level|loot|profession> <duration in s> <bonusAmount>");
                        break;
                    }
                    String buffType = args[1].toLowerCase();
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        Integer.parseInt(args[2]);
                        //noinspection ResultOfMethodCallIgnored
                        Float.parseFloat(args[3]);
                    } catch (NumberFormatException ex) {
                        commandSender.sendMessage(ChatColor.RED + "Invalid duration or bonus amount! Syntax: /dr buff <level|loot|profession> <duration in s> <bonusAmount>");
                        break;
                    }
                    String duration = args[2];
                    String bonusAmount = args[3];
                    GameAPI.sendNetworkMessage("buff", buffType.toUpperCase(), duration, bonusAmount, commandSender.getName(), DungeonRealms.getShard().getShardID());
                    break;
                default:
                    commandSender.sendMessage(ChatColor.RED + "The command " + ChatColor.BOLD + ChatColor.UNDERLINE + args[0].toUpperCase() + ChatColor.RED + " does not exist.");
                    break;
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "Invalid usage! /dr <command> [args]");
        }
        return true;
    }

    public boolean setRank(CommandSender commandSender, UUID uuid, String playerName, String type, String rankName) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(uuid);
        PlayerRank newRank = PlayerRank.getFromPrefix(rankName);
        if (wrapper == null) {
            return false;
        }

        switch (type) {
            case "rank":
                PlayerRank currentRank = wrapper.getRank();
                if (newRank.isAtLeast(currentRank)) {
                    if (newRank == PlayerRank.SUB_PLUS_PLUS) {
                        Rank.setRank(uuid, rankName, done -> GameAPI.updatePlayerData(uuid, UpdateType.RANK));
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "The rank " + ChatColor.BOLD + ChatColor.UNDERLINE + type + ChatColor.RED + " is invalid or unsupported through this command.");
                        return false;
                    }
                } else {
                    commandSender.sendMessage(ChatColor.RED + "Failed to update the rank of " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.RED + " because they're " + ChatColor.BOLD + ChatColor.UNDERLINE + currentRank + ChatColor.RED + ".");
                    return false;
                }
                break;

            default:
                commandSender.sendMessage(ChatColor.RED + "Invalid purchase type: " + type + ".");
                return false;
        }
        return true;
    }
}