package net.dungeonrealms.lobby.characters;


import com.mysql.jdbc.Statement;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.player.PlayerRank;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.game.menu.AbstractMenu;
import net.dungeonrealms.common.game.menu.gui.GUIButtonClickEvent;
import net.dungeonrealms.common.game.menu.item.GUIButton;
import net.dungeonrealms.common.util.CharacterData;
import net.dungeonrealms.common.util.CharacterType;
import net.dungeonrealms.common.util.ChatUtil;
import net.dungeonrealms.lobby.Lobby;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;


public class CharacterSelector extends AbstractMenu {

    //    private Set<UUID> accepted = new HashSet<>();
    public CharacterSelector(Player player, int accountID, Integer selectedCharacterID, List<CharacterData> characters, Integer maxPurchasedCharacterSlots, int invSize) {
        super(Lobby.getInstance(), "Select your character", AbstractMenu.round(invSize + 1), player.getUniqueId());
        setDestroyOnExit(true);

        if (characters.isEmpty() || selectedCharacterID == null || maxPurchasedCharacterSlots == null) {
            player.sendMessage(ChatColor.RED + "It seems you are a new user and we have not finished creating your data yet!");
            player.sendMessage(ChatColor.YELLOW + "Please try again!");
            return;
        }

        PlayerRank hisRank = Rank.getPlayerRank(player.getUniqueId());
        HashMap<CharacterType, AtomicInteger> usedCharacterSlots = new HashMap<>();
        for(CharacterType type : CharacterType.values())usedCharacterSlots.put(type, new AtomicInteger(0));
        for (int k = 0; k < characters.size(); k++) {
            CharacterData nameObject = characters.get(k);
            int characterID = nameObject.getCharacterID();
            CharacterType type = CharacterType.getCharacterType(nameObject.getCharacterType());
            boolean isLocked = nameObject.isManuallyLocked() || isLocked(type, player);
            String characterTitle = nameObject.getCharacterName();
            if (characterTitle == null) characterTitle = k == 0 ? "Default Character" : "Character " + k;
            AtomicInteger val = usedCharacterSlots.get(type);
            val.incrementAndGet();
            boolean isSelected = selectedCharacterID.intValue() == characterID;
            short durability = isLocked ? DyeColor.RED.getWoolData() : isSelected ? DyeColor.LIME.getWoolData() : DyeColor.YELLOW.getWoolData();
            ItemStack buttonStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, durability);
            final String finalTile = characterTitle;
            GUIButton button = new GUIButton(buttonStack) {

                @Override
                public void action(GUIButtonClickEvent event) throws Exception {
                    Player player = event.getWhoClicked();
                    if (isLocked) {
                        player.sendMessage(ChatColor.RED + "This character is locked!");
                        return;
                    }
                    if(player.hasMetadata("savingData")) {
                        player.sendMessage(ChatColor.RED + "Please wait!");
                        return;
                    }
                    player.closeInventory();
                    applySavingMeta(player);
                    if (event.getClickEvent().getClick().equals(ClickType.LEFT)) {
                        if (isSelected) {
                            removeSavingMeta(player);
                            return;
                        }

                        SQLDatabaseAPI.getInstance().executeUpdate((rows) -> {
                            if (rows == null) {
                                player.sendMessage(ChatColor.RED + "Something went wrong! Please try again");
                                removeSavingMeta(player);
                                return;
                            }
                            removeSavingMeta(player);
                            //new ShardSelector(player).open(player);
                            player.sendMessage(ChatColor.GREEN + "Your active character is now " + ChatColor.YELLOW + ChatColor.BOLD.toString() + finalTile);
                        }, QueryType.UPDATE_SELECTED_CHARACTER.getQuery(characterID, accountID));
                    } else if (event.getClickEvent().getClick().equals(ClickType.RIGHT)) {
                        player.sendMessage(ChatColor.GREEN + "Please type in your new character name!");
                        Lobby.chatCallbacks.put(player.getUniqueId(), chatEvent -> {
                            String msg = chatEvent.getMessage();
                            Lobby.chatCallbacks.remove(player.getUniqueId());
                            applySavingMeta(player);
                            if (msg.length() > 1 && msg.length() <= 16 && StringUtils.isAlphanumericSpace(msg) && !ChatUtil.containsBannedWords(msg)) {
                                SQLDatabaseAPI.getInstance().executeUpdate((rows) -> {
                                    if (rows == null || rows <= 0) {
                                        player.sendMessage(ChatColor.RED + "Something went wrong! Please try again!");
                                        removeSavingMeta(player);
                                        return;
                                    }
                                    player.sendMessage(ChatColor.GREEN + "Success! Your character is now named: " + msg);
                                    removeSavingMeta(player);
                                }, QueryType.UPDATE_CHARACTER_DISPLAY.getQuery(SQLDatabaseAPI.escape(msg), characterID));
                            } else {
                                player.sendMessage(ChatColor.RED + "You entered an INVALID character display name");
                                player.sendMessage(ChatColor.GRAY + "Please try again! This can only be a max of 16 characters! Alphanumeric only!");
                                removeSavingMeta(player);
                            }
                        });
                    }
                }
            };

            List<String> lore = new ArrayList<>();


            //final int slot = getServerType(shardID).equals("") ? getSize() : Math.min(getInventorySize(), getInventorySize() - 1) - (getSize() - getNormalServers());

            lore.add(" ");
            lore.add(ChatColor.GOLD + ChatColor.BOLD.toString() + "Character Info");
            //lore.add(" ");
            lore.add(ChatColor.YELLOW + ChatColor.BOLD.toString() + "  Type: " + ChatColor.GRAY + type.getDisplayName());
            lore.add(ChatColor.YELLOW + ChatColor.BOLD.toString() + "  Level: " + ChatColor.GRAY + nameObject.getLevel());
            lore.add(ChatColor.YELLOW + ChatColor.BOLD.toString() + "  Health: " + ChatColor.GRAY + nameObject.getCurrentHP());
            lore.add(ChatColor.YELLOW + ChatColor.BOLD.toString() + "  Alignment: " + ChatColor.GRAY + nameObject.getAlignmentString());
            //lore.add(" ");
            lore.add(ChatColor.YELLOW + ChatColor.BOLD.toString() + "  Created: " + ChatColor.GRAY + nameObject.getTimeCreatedString());
            lore.add(" ");
            lore.add(ChatColor.RED + ChatColor.BOLD.toString() + "Left Click:" + ChatColor.GRAY + " Load this character");
            lore.add(ChatColor.RED + ChatColor.BOLD.toString() + "Right Click:" + ChatColor.GRAY + " Rename this character");
            if(isLocked) {
                lore.add(" ");
                lore.add(ChatColor.RED + ChatColor.BOLD.toString() + "LOCKED");
            }

            ChatColor nameColor = ChatColor.GREEN;
            button.setDisplayName(nameColor + ChatColor.BOLD.toString() + characterTitle);
            button.setLore(lore);

            set(getSize(), button);
        }

        for(CharacterType type : CharacterType.values()) {
            if(!hisRank.isAtLeast(type.getRank())) continue;
            int usedSlots = usedCharacterSlots.get(type).get();
            int maxSlots = type.equals(CharacterType.PURCHASED) ? maxPurchasedCharacterSlots : type.getDefaultSlots();
            int numberOfUnusedSlots = maxSlots - usedSlots;


            for (int k = 0; k < numberOfUnusedSlots; k++) {
                short durability = DyeColor.WHITE.getWoolData();
                ItemStack buttonStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, durability);
                GUIButton button = new GUIButton(buttonStack) {

                    @Override
                    public void action(GUIButtonClickEvent event) throws Exception {
                        Player player = event.getWhoClicked();
                        player.closeInventory();
                        if(player.hasMetadata("savingData")) {
                            player.sendMessage(ChatColor.RED + "Please wait!");
                            return;
                        }
                        applySavingMeta(player);
                        //player.sendMessage(ChatColor.GREEN + "We clicked the un created character slot");
                        CompletableFuture.runAsync(() -> {

                            /*String queryString =
                                    "START TRANSACTION;"
                                    + "SET @account_id = " + accountID + ";"
                                    + String.format("INSERT INTO characters(account_id, created, character_type) VALUES (@account_id, '%s', '%s');", System.currentTimeMillis(), type.getInternalName())
                                    + "SET @character_id = LAST_INSERT_ID();"
                                    + "INSERT INTO `statistics`(character_id) VALUES (@character_id);"
                                    + "INSERT INTO `realm`(character_id) VALUES (@character_id);"
                                    + "INSERT INTO `attributes`(character_id) VALUES (@character_id);"
                                    + "COMMIT;";*/


                            //System.out.println(queryString);

                            String query = String.format("INSERT IGNORE INTO characters(account_id, created, character_type) VALUES ('%s', '%s', '%s');", accountID, System.currentTimeMillis(), type.getInternalName());
                            try {
                                PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

                                if (Constants.debug)
                                    Constants.log.info("Updating database with query: " + query);

                                statement.executeUpdate();

                                ResultSet keys = statement.getGeneratedKeys();
                                if(keys.first()) {
                                    int charID = keys.getInt(1);
                                    PreparedStatement statement2 = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement("");
                                    statement2.addBatch(String.format("INSERT INTO `statistics`(character_id) VALUES (%s);", charID));
                                    statement2.addBatch(String.format("INSERT INTO `realm`(character_id) VALUES (%s);", charID));
                                    statement2.addBatch(String.format("INSERT INTO `attributes`(character_id) VALUES (%s);", charID));
                                    statement2.executeBatch();
                                    statement2.close();
                                }

                                player.closeInventory();

                                removeSavingMeta(player);
                                openCharacterSelector(player);
                                player.sendMessage(ChatColor.GREEN + "Success! You have created a new character!");

                                //Just manually close statement?
                                statement.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }, SQLDatabaseAPI.getSERVER_EXECUTOR_SERVICE());
                    }
                };

                List<String> lore = new ArrayList<>();


                //final int slot = getServerType(shardID).equals("") ? getSize() : Math.min(getInventorySize(), getInventorySize() - 1) - (getSize() - getNormalServers());

                lore.add(" ");
                lore.add(ChatColor.GOLD + ChatColor.BOLD.toString() + "Character Info");
                lore.add(ChatColor.YELLOW + ChatColor.BOLD.toString() + "  Type: " + ChatColor.GRAY + type.getDisplayName());
                lore.add(" ");
                lore.add(ChatColor.RED + ChatColor.BOLD.toString() + "Left Click: " + ChatColor.GRAY + "Create this character");

                button.setDisplayName(ChatColor.WHITE + ChatColor.BOLD.toString() + "Empty Character Slot");
                button.setLore(lore);

                set(getSize(), button);
            }
        }

        for(int k = 0; k < inventory.getSize(); k++) {
            //This is used to fill the rest of the gui.
            if(inventory.getContents()[k] != null) continue;
            short durability = DyeColor.GRAY.getWoolData();
            ItemStack buttonStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, durability);
            GUIButton button = new GUIButton(buttonStack) {

                @Override
                public void action(GUIButtonClickEvent event) throws Exception {
                    Player player = event.getWhoClicked();
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + "This character is " + ChatColor.BOLD + "LOCKED!");
                    player.sendMessage(ChatColor.GRAY + "You can unlock it at " + ChatColor.UNDERLINE + Constants.SHOP_URL);
                }
            };

            List<String> lore = new ArrayList<>();


            //final int slot = getServerType(shardID).equals("") ? getSize() : Math.min(getInventorySize(), getInventorySize() - 1) - (getSize() - getNormalServers());

            lore.add(" ");
            lore.add(ChatColor.RED + ChatColor.BOLD.toString() + "Left Click: " + ChatColor.GRAY + "Create this character");
            lore.add(" ");
            lore.add(ChatColor.RED + ChatColor.BOLD.toString() + "LOCKED");

            button.setDisplayName(ChatColor.WHITE + ChatColor.BOLD.toString() + "Empty Character Slot");
            button.setLore(lore);

            set(k, button);
        }

    }

    public static boolean isLocked(CharacterType type, Player player) {
        if (type == null) return true; //Lock them if it's unknown?
        PlayerRank rank = type.getRank();
        if (!Rank.getPlayerRank(player.getUniqueId()).isAtLeast(rank)) return true;
        return false;
    }

    public static void applySavingMeta(Player p) {
        p.setMetadata("savingData", new FixedMetadataValue(Lobby.getInstance(), true));
    }

    public static void removeSavingMeta(Player p) {
        p.removeMetadata("savingData", Lobby.getInstance());
    }

    public static void openCharacterSelector(Player player) {
        Integer accountID = SQLDatabaseAPI.getInstance().getAccountIdFromUUID(player.getUniqueId());
        if (accountID == null) {
            player.sendMessage(ChatColor.RED + "It seems you are a new user and we have not finished creating your data yet!");
            player.sendMessage(ChatColor.YELLOW + "Please try again! 1");
            return;
        }
        SQLDatabaseAPI.getInstance().executeQuery(QueryType.SELECT_ALL_CHARACTERS.getQuery(accountID), true, (set) -> {
            if (set == null) {
                player.sendMessage(ChatColor.RED + "Something went wrong! Please try again! 2");
                return;
            }
            List<CharacterData> createdCharacterIds = new ArrayList<>();
            Integer selectedCharacterID = null;
            Integer maxCharacterSlots = null;
            try {
                while (set.next()) {
                    createdCharacterIds.add(new CharacterData(set.getInt("character_id"), set.getString("display_name"), set.getInt("level"), set.getInt("health"), set.getLong("created"), set.getString("alignment"), set.getString("character_type"), set.getBoolean("characters.isLocked")));
                    if (selectedCharacterID == null) selectedCharacterID = set.getInt("selected_character_id");
                    if (maxCharacterSlots == null) maxCharacterSlots = set.getInt("character_slots");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (selectedCharacterID == null) {
                player.sendMessage(ChatColor.RED + "Something went wrong! Please try again! 3");
                return;
            }

            if (maxCharacterSlots == null) {
                player.sendMessage(ChatColor.RED + "Something went wrong! Please try again! 4");
                return;
            }


            int invSize = CharacterType.getDefaultSlots(player) + maxCharacterSlots;

            //Its possible they are no longer GM or sub or something and have more characters than they technically are supposed to have. This makes sure the inventory is large enough.
            if(invSize < createdCharacterIds.size()) invSize = createdCharacterIds.size();



            new CharacterSelector(player, accountID, selectedCharacterID, createdCharacterIds, maxCharacterSlots,invSize).open(player);
        });
    }

    @Override
    public void open(Player player) {
        if (getSize() == 0) {
            player.sendMessage(ChatColor.RED + "Unable to find an available character for you.");
            return;
        }

        try {
            AtomicInteger secondsLeft = Lobby.getInstance().getRecentLogouts().getIfPresent(player.getUniqueId());

            if (secondsLeft != null) {
                if (secondsLeft.get() > 0 && !Rank.isTrialGM(player)) {
                    int left = secondsLeft.get();
                    player.sendMessage(ChatColor.RED + "You must wait " + left + " second(s) before you can transfer characters.");
                    return;
                } else {
                    Lobby.getInstance().getRecentLogouts().invalidate(player.getUniqueId());
                }
            }
        } catch (Exception e) {
            //Catches an NPE relating to if a player has a last shard transfer time
            e.printStackTrace();
        }

        player.openInventory(inventory);
    }

}
