package net.dungeonrealms.game.player.combat;

import net.dungeonrealms.game.handlers.KarmaHandler;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import org.bukkit.Location;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Kieran Quigley (Proxying) on 11-Jun-16.
 */
public class CombatLogger {

    private UUID playerUUID;
    private Zombie loggerNPC;
    private List<ItemStack> itemsToDrop;
    private List<ItemStack> armorToDrop;
    private List<ItemStack> itemsToSave;
    private List<ItemStack> armorToSave;
    private KarmaHandler.EnumPlayerAlignments playerAlignment;

    public CombatLogger(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public void handleNPCDeath() {
        setRespawnLocation();
        DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$SET, EnumData.LOGGERDIED, true, true);
        CombatLog.getInstance().getCOMBAT_LOGGERS().remove(playerUUID, this);
    }

    public void handleTimeOut() {
        if (CombatLog.getInstance().getCOMBAT_LOGGERS().containsKey(playerUUID)) {
            loggerNPC.remove();
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$SET, EnumData.LOGGERDIED, false, true);
            CombatLog.getInstance().getCOMBAT_LOGGERS().remove(playerUUID, this);
        }
    }

    private void setRespawnLocation() {
        if (playerAlignment == KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
            Location loc =  KarmaHandler.CHAOTIC_RESPAWNS.get(new Random().nextInt(KarmaHandler.CHAOTIC_RESPAWNS.size() - 1));
            if (loc != null) {
                String locString = loc.getBlockX() +"," + loc.getBlockY() + 3 + "," + loc.getBlockZ() + "," + "0,0";
                DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$SET, EnumData.CURRENT_LOCATION, locString, false);
            } else {
                DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$SET, EnumData.CURRENT_LOCATION, "-367,90,390,0,0", false);
            }
        } else {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$SET, EnumData.CURRENT_LOCATION, "-367,90,390,0,0", false);
        }
    }

    public KarmaHandler.EnumPlayerAlignments getPlayerAlignment() {
        return playerAlignment;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public Zombie getLoggerNPC() {
        return loggerNPC;
    }

    public List<ItemStack> getItemsToDrop() {
        return itemsToDrop;
    }

    public List<ItemStack> getArmorToDrop() {
        return armorToDrop;
    }

    public List<ItemStack> getItemsToSave() {
        return itemsToSave;
    }

    public List<ItemStack> getArmorToSave() {
        return armorToSave;
    }

    public void setPlayerAlignment(KarmaHandler.EnumPlayerAlignments playerAlignment) {
        this.playerAlignment = playerAlignment;
    }

    public void setArmorToDrop(List<ItemStack> armorToDrop) {
        this.armorToDrop = armorToDrop;
    }

    public void setItemsToDrop(List<ItemStack> toDrop) {
        this.itemsToDrop = toDrop;
    }

    public void setLoggerNPC(Zombie loggerNPC) {
        this.loggerNPC = loggerNPC;
    }

    public void setItemsToSave(List<ItemStack> itemsToSave) {
        this.itemsToSave = itemsToSave;
    }

    public void setArmorToSave(List<ItemStack> armorToSave) {
        this.armorToSave = armorToSave;
    }
}
