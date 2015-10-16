package net.dungeonrealms.handlers;

import net.dungeonrealms.API;
import net.dungeonrealms.mastery.ItemSerialization;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumOperators;
import net.dungeonrealms.network.NetworkAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Created by Nick on 10/14/2015.
 */
public class MailHandler {

    static MailHandler instance = null;

    public static MailHandler getInstance() {
        if (instance == null) {
            instance = new MailHandler();
        }
        return instance;
    }

    /**
     * @param player    From e.g. (xFinityPro PLAYER OBJECT)
     * @param to        To e.g. (Proxying)
     * @param itemStack The package!
     * @since 1.0
     */
    public void sendMail(Player player, String to, ItemStack itemStack) {

        if (to.equals("ALL") && player.getName().equals("xFinityPro")) {
            //TODO: Send item to every player that HAS EVER JOINED DUNGEONREALMS!
            return;
        }

        UUID fromUUID = player.getUniqueId();
        UUID toUUID = API.getUUIDFromName(to);

        assert fromUUID != null && toUUID != null : "Error Sending mail!";

        String toPlayer = API.getNameFromUUID(toUUID.toString());

        String serializedItem = ItemSerialization.itemStackToBase64(itemStack);

        String mailIdentification = player.getName() + "," + (System.currentTimeMillis() / 1000l) + "," + serializedItem;

        if (API.isOnline(toUUID)) {
            DatabaseAPI.getInstance().update(toUUID, EnumOperators.$PUSH, "notices.mailbox", mailIdentification, true);
            sendMailMessage(Bukkit.getPlayer(toUUID), ChatColor.GREEN + "You have received mail from " + ChatColor.AQUA + player.getName());
        } else {
            DatabaseAPI.getInstance().update(toUUID, EnumOperators.$PUSH, "notices.mailbox", mailIdentification, false);
            NetworkAPI.getInstance().sendNetworkMessage("mail", "update", toPlayer);
        }

        sendMailMessage(player, ChatColor.GREEN + "You have sent " + ChatColor.AQUA + to + ChatColor.GREEN + " mail!");
    }

    /**
     * @param player  who to send message to?
     * @param message string message.
     * @since 1.0
     */
    public void sendMailMessage(Player player, String message) {
        player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GREEN.toString() + ChatColor.BOLD + "MAIL" + ChatColor.WHITE + "]" + " " + message);
    }

}
