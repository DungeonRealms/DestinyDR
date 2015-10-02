package net.dungeonrealms.anticheat;

import net.dungeonrealms.mastery.AsyncUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Nick on 10/1/2015.
 */
public class AntiCheat {

    static AntiCheat instance = null;

    public static AntiCheat getInstance() {
        if (instance == null) {
            instance = new AntiCheat();
        }
        return instance;
    }

    /**
     * Because Kayaba and Red are going to be such bitches.
     * ANd we all know that negros like them will follow
     * the path of niggerish nigger. We'll build in a automagically
     * proxyifier detector to detect dem negro from 2 farm fields
     * away.
     *
     * @param uuid
     * @param ip
     * @return
     * @since 1.0
     */
    public boolean isProxying(UUID uuid, InetAddress ip) {

        Future<Boolean> isProxy = AsyncUtils.pool.submit(() -> {
            Utils.log.info("[ANTI-PROXY] [ASYNC] Checking player " + uuid.toString() + " w/ ip " + ip.toString().replace("/", ""));
            try {

                URL url = new URL("http://www.shroomery.org/ythan/proxycheck.php?ip=" + ip.toString().replace("/", ""));
                URLConnection connection = url.openConnection();
                connection.connect();

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine = in.readLine();
                in.close();
                if (inputLine == null) return false;
                switch (inputLine) {
                    case "Y":
                        return true;
                    case "N":
                        return false;
                    case "X":
                        Utils.log.warning("Unable to check ifIs Proxy for user " + uuid);
                        return false;
                    default:
                        Utils.log.warning("DEFAULT FIRED SWITCH() " + uuid);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        });

        try {
            return isProxy.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Will be placed inside an eventlistener to make sure
     * the player isn't duplicating.
     *
     * @param event
     * @return
     * @since 1.0
     */
    public boolean watchForDupes(InventoryClickEvent event) {
        ItemStack checkItem = event.getCursor();
        if (checkItem == null) return false;
        if (!isRegistered(checkItem)) return false;
        String check = getUniqueEpochIdentifier(checkItem);
        for (ItemStack item : event.getInventory().getContents()) {
            if (item == null || item.getType() == null || item.getType().equals(Material.AIR)) continue;
            if (check.equals(getUniqueEpochIdentifier(item))) return true;
        }
        return false;
    }

    /**
     * Returns the actual Epoch Unix String Identifier
     *
     * @param item
     * @return
     * @since 1.0
     */
    public String getUniqueEpochIdentifier(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null || !tag.hasKey("u")) return null;
        return tag.getString("u");
    }

     /**
     * Check to see if item contains 'u' field.
     *
     * @param item
     * @return
     * @since 1.0
     */
    public boolean isRegistered(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        return !(nmsStack == null || nmsStack.getTag() == null) && nmsStack.getTag().hasKey("u");
    }

    /**
     * Adds a (u) to the item. (u) -> UNIQUE IDENTIFIER
     *
     * @param item
     * @return
     * @since 1.0
     */
    public ItemStack applyAntiDupe(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null || tag.hasKey("u")) return item;
        tag.set("u", new NBTTagString(System.currentTimeMillis() + item.getType().toString()));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

}
