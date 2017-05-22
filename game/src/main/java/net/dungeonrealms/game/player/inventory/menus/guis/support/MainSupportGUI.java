package net.dungeonrealms.game.player.inventory.menus.guis.support;

import net.dungeonrealms.common.game.database.player.PlayerRank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.database.rank.Subscription;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/**
 * Created by Rar349 on 5/19/2017.
 */
public class MainSupportGUI extends SupportGUI {

    public MainSupportGUI(Player player, String viewing) {
        super(player,viewing,45,"Support Tools");
    }

    @Override
    protected void setItems() {
        PlayerRank playerRank = getWrapper().getRank();

        setItem(19, new GUIItem(Material.DIAMOND).setName(ChatColor.GOLD + "Rank Manager").setLore(ChatColor.WHITE + "Modify the rank of " + getOtherName(), ChatColor.WHITE + "Current rank: " + playerRank.getPrefix()).setClick((evt) -> new RankSupportGUI(player,getOtherName()).open(player,evt.getAction())));

        setItem(28, new GUIItem(Material.EMERALD).setName(ChatColor.GOLD + "Gem Balance Manager").setLore(ChatColor.WHITE + "Manage the gem balance of " + getOtherName(), ChatColor.WHITE + "Current bank balance: " + getWrapper().getGems(), "", "Click here to set their gems!").setClick((evt) -> {
            setShouldOpenPreviousOnClose(false);
            player.closeInventory();
            player.sendMessage("Enter the amount of gems to set");
            Chat.listenForMessage(player, customAmount -> {
                if (!customAmount.getMessage().equalsIgnoreCase("cancel") && !customAmount.getMessage().equalsIgnoreCase("exit")) {
                    try {
                        int amount = Integer.parseInt(customAmount.getMessage());
                        getWrapper().setGems(amount);
                        getWrapper().runQuery(QueryType.SET_GEMS, getWrapper().getGems(), getWrapper().getCharacterID());
                        player.sendMessage(ChatColor.GREEN + "Successfully set " + getOtherName() + "'s gems to " + customAmount.getMessage());
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + customAmount.getMessage() + " is not a valid number.");
                    }
                }
            });
        }));

        setItem(22, new GUIItem(Material.EXP_BOTTLE).setName(ChatColor.GOLD + "Level Manager").setLore(ChatColor.WHITE + "Manage the level/exp of " + getOtherName(), ChatColor.WHITE + "Current level: " + getWrapper().getLevel(), ChatColor.WHITE + "Current EXP: " + getWrapper().getExperience(), "", "Right click to set level", "Left click to set exp").setClick((evt) -> {
            //new LevelSupportGUI(player,getOtherName()).open(player,evt.getAction())
            if(evt.getClick().equals(ClickType.LEFT)) {
                player.sendMessage(ChatColor.YELLOW + "Please enter the xp amount you would to set");
                Chat.listenForMessage(player, customAmount -> {
                    if (!customAmount.getMessage().equalsIgnoreCase("cancel") && !customAmount.getMessage().equalsIgnoreCase("exit")) {
                        try {
                            int amount = Integer.parseInt(customAmount.getMessage());
                            getWrapper().setExperience(amount);
                            getWrapper().saveData(true,null);
                            player.sendMessage("Success! Set the exp to: " + amount);
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + customAmount.getMessage() + " is not a valid number.");
                        }
                    }
                });

            } else if(evt.getClick().equals(ClickType.RIGHT)) {
                player.sendMessage(ChatColor.YELLOW + "Please enter the level amount you would to set");
                Chat.listenForMessage(player, customAmount -> {
                    if (!customAmount.getMessage().equalsIgnoreCase("cancel") && !customAmount.getMessage().equalsIgnoreCase("exit")) {
                        try {
                            int amount = Integer.parseInt(customAmount.getMessage());
                            getWrapper().setLevel(amount);
                            getWrapper().saveData(true,null);
                            player.sendMessage("Success! Set the level to: " + amount);
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + customAmount.getMessage() + " is not a valid number.");
                        }
                    }
                });
            }
        }));

        setItem(31, new GUIItem(Material.QUARTZ).setName(ChatColor.GOLD + "Hearthstone Manager").setLore(ChatColor.WHITE + "Modify the hearthstone location of " + getOtherName(), ChatColor.WHITE + "Current location: " + getWrapper().getHearthstone().getDisplayName()).setClick((evt) -> {
            new HearthstoneSupportGUI(player,getOtherName()).open(player,evt.getAction());
        }));

        setItem(25, new GUIItem(Material.GOLDEN_CARROT).setName(ChatColor.GOLD + "E-cash Manager").setLore(ChatColor.WHITE + "Modify the e-cash of " + getOtherName(), ChatColor.WHITE + "Current E-Cash: " + getWrapper().getEcash(), "", "Click to set e-cash").setClick((evt) -> {
            //new EcashSupportGUI(player,getOtherName()).open(player,evt.getAction())
            player.sendMessage(ChatColor.YELLOW + "Please enter the ecash amount you would to set");
            Chat.listenForMessage(player, customAmount -> {
                if (!customAmount.getMessage().equalsIgnoreCase("cancel") && !customAmount.getMessage().equalsIgnoreCase("exit")) {
                    try {
                        int amount = Integer.parseInt(customAmount.getMessage());
                        getWrapper().setEcash(amount);
                        getWrapper().saveData(true,null);
                        player.sendMessage("Success! Set the ecash to: " + amount);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + customAmount.getMessage() + " is not a valid number.");
                    }
                }
            });
        }));
        setItem(34, new GUIItem(Material.NAME_TAG).setName(ChatColor.GOLD + "Pet Manager").setLore(ChatColor.WHITE + "Modify pets of " + getOtherName()).setClick((evt) -> new PetSupportGUI(player,getOtherName()).open(player,evt.getAction())));

        setItem(35, new GUIItem(Material.EYE_OF_ENDER).setName(ChatColor.GOLD + "Trail Manager").setLore(ChatColor.WHITE + "Modify trails of " + getOtherName()).setClick((evt) -> new TrailSupportGUI(player,getOtherName()).open(player,evt.getAction())));

        setItem(4, new GUIItem(Material.SKULL_ITEM).setSkullOwner(getOtherName()).setName(ChatColor.GREEN + getOtherName() + ChatColor.WHITE + "(" + getWrapper().getUuid() + ")").setLore(
                ChatColor.WHITE + "Rank: " + playerRank.getPrefix() +
                        (playerRank.isSUB() ?
                                ChatColor.WHITE + " (" + Subscription.getInstance().checkSubscription(getWrapper().getUuid(), getWrapper().getRankExpiration()) + " days remaining)" : ""),
                ChatColor.WHITE + "Level: " + getWrapper().getLevel(),
                ChatColor.WHITE + "Experience: " + getWrapper().getExperience(),
                ChatColor.WHITE + "E-Cash: " + getWrapper().getEcash(),
                ChatColor.WHITE + "Bank Balance: " + getWrapper().getGems(),
                ChatColor.WHITE + "Hearthstone Location: " + getWrapper().getHearthstone().getDisplayName(),
                ChatColor.WHITE + "Alignment: " + Utils.ucfirst(getWrapper().getAlignment().name()),
                //ChatColor.WHITE + "Last Logout: " + Utils.formatTimeAgo((int) (System.currentTimeMillis() / 1000) - Integer.valueOf(DatabaseAPI.getInstance().getData(EnumData.LAST_LOGOUT, uuid).toString())) + " ago", @todo: Fix a bug with this.
                ChatColor.WHITE + "Join Date: " + Utils.getDate(getWrapper().getFirstLogin() * 1000)
        ));
    }
}
