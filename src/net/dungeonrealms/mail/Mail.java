package net.dungeonrealms.mail;

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
public class Mail {

    static Mail instance = null;

    public static Mail getInstance() {
        if (instance == null) {
            instance = new Mail();
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
            NetworkAPI.getInstance().sendPlayerMessage(toPlayer, "YOU GOT MAIL!");
            NetworkAPI.getInstance().sendNetworkMessage("mail", "update", API.getNameFromUUID(toUUID.toString()));
        }


        player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GREEN.toString() + ChatColor.BOLD + "MAIL" + ChatColor.WHITE + "]" + " " + ChatColor.GREEN + "You have sent " + ChatColor.AQUA + to + ChatColor.GREEN + " mail!");

    }

    public void sendMailMessage(Player player, String message) {
        player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GREEN.toString() + ChatColor.BOLD + "MAIL" + ChatColor.WHITE + "]" + " " + message);
    }

}
