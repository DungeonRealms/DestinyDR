package net.dungeonrealms.handlers;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.API;
import net.dungeonrealms.mastery.ItemSerialization;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumOperators;
import net.dungeonrealms.network.NetworkAPI;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;

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
     * Will give the player the item they have clicked on.
     *
     * @param item   serialized mail item.
     * @param player who to give item to?
     * @since 1.0
     */
    public void giveItemToPlayer(ItemStack item, Player player) {
        if (isMailItem(item)) {
            player.closeInventory();
            String serializedItem = CraftItemStack.asNMSCopy(item).getTag().getString("item");
            String from = serializedItem.split(",")[0];
            long unix = Long.valueOf(serializedItem.split(",")[1]);
            String rawItem = serializedItem.split(",")[2];

            ItemStack actualItem = ItemSerialization.itemStackFromBase64(rawItem);


            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, "notices.mailbox", from + "," + String.valueOf(unix) + "," + rawItem, true);
            player.getInventory().addItem(actualItem);
            sendMailMessage(player, ChatColor.GREEN + "You opened mail from " + ChatColor.AQUA + from + ChatColor.GREEN + "!");
            player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 1f, 63f);
        }
    }

    /**
     * Checks of the item that is specified is a mail item containing NBT.
     *
     * @param item specify if the item is of the serialized item type.
     * @return boolean
     * @since 1.0
     */
    private boolean isMailItem(ItemStack item) {
        if (item == null || item.getType() == null || item.getType().equals(Material.AIR)) return false;
        return CraftItemStack.asNMSCopy(item).hasTag() && CraftItemStack.asNMSCopy(item).getTag().hasKey("item");
    }

    /**
     * Will apply the hidden data to an item.
     *
     * @param itemStack              item applying hidden date to..
     * @param base64SerializedString The encrypted string.
     * @return
     * @since 1.0
     */
    public ItemStack setItemAsMail(ItemStack itemStack, String base64SerializedString) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("mail"));
        tag.set("item", new NBTTagString(base64SerializedString));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
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
