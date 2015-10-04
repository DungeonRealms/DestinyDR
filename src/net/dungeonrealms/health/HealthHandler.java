package net.dungeonrealms.health;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.duel.DuelMechanics;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

/**
 * Created by Kieran on 10/3/2015.
 */
public class HealthHandler {

    private static HealthHandler instance = null;

    public static HealthHandler getInstance() {
        if (instance == null) {
            instance = new HealthHandler();
        }
        return instance;
    }

    public void startInitialization() {
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::updatePlayerHPBars, 40, 5L);
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::regenerateHealth, 40, 20L);
    }

    public static void handleLoginEvents(Player player) {
        setPlayerMaxHPLive(player, getPlayerMaxHPOnLogin(player.getUniqueId()));
        setPlayerHPLive(player, getPlayerMaxHPOnLogin(player.getUniqueId()));
        setPlayerHPRegenLive(player, getPlayerHPRegenLive(player));
    }

    public static void handleLogoutEvents(Player player) {
        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "info.health", getPlayerMaxHPLive(player), false);
    }

    private void updatePlayerHPBars() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (getPlayerHPLive(player) > 0) {
                setPlayerOverheadHP(uuid, getPlayerHPLive(player));
            }
        }
    }

    public static int getPlayerHPLive(Player player) {
        if (player.hasMetadata("currentHP")) {
            return player.getMetadata("currentHP").get(0).asInt();
        } else {
            return 50; //This shouldn't happen but safety return. Probably kick them or something if their data cannot be loaded.
        }
    }

    public static void setPlayerOverheadHP(UUID uuid, int hp) {
        //Check their Max HP from wherever we decide to store it, get it as a percentage.
        //Update BarAPI thing with it.
    }

    public static void setPlayerHPLive(Player player, int hp) {
        player.setMetadata("currentHP", new FixedMetadataValue(DungeonRealms.getInstance(), hp));
    }

    public static int getPlayerMaxHPOnLogin(UUID uuid) {
        if ((DatabaseAPI.getInstance().getData(EnumData.HEALTH, uuid) != null) && ((int) DatabaseAPI.getInstance().getData(EnumData.HEALTH, uuid) > 0)) {
            return (int) DatabaseAPI.getInstance().getData(EnumData.HEALTH, uuid);
        } else {
            return generateMaxHPFromItems(Bukkit.getPlayer(uuid)); //This shouldn't happen but safety return. Probably kick them or something if their data cannot be loaded.
        }
    }

    public static int getPlayerMaxHPLive(Player player) {
        if (player.hasMetadata("maxHP")) {
            return player.getMetadata("maxHP").get(0).asInt();
        } else {
            return generateMaxHPFromItems(player);
        }
    }

    public static void setPlayerMaxHPLive(Player player, int maxHP) {
        player.setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), maxHP));
    }

    private void regenerateHealth() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (getPlayerHPLive(player) <= 0 && player.getHealth() <= 0) {
                continue;
            }
            if (player.hasMetadata("starving")) {
                continue;
            }
            if (CombatLog.isInCombat(uuid)) {
                continue;
            }
            //Check their Max HP from wherever we decide to store it.
            int currentHP = getPlayerHPLive(player);
            int amountToHealPlayer = getPlayerHPRegenLive(player);
            int maxHP = getPlayerMaxHPLive(player);
            if (currentHP + 1 > maxHP) {
                if (player.getHealth() != 20) {
                    player.setHealth(20);
                }
                continue;
            }
            amountToHealPlayer += getHealthRegenFromItems(player);

            if ((currentHP + amountToHealPlayer) >= maxHP) {
                player.setHealth(20);
                setPlayerHPLive(player, maxHP);
            } else if (player.getHealth() <= 19 && ((currentHP + amountToHealPlayer) < maxHP)) {
                setPlayerHPLive(player, (getPlayerHPLive(player) + amountToHealPlayer));
                double playerHPPercent = (getPlayerHPLive(player) + amountToHealPlayer) / maxHP;
                double newPlayerHP = playerHPPercent * 20;
                if (newPlayerHP >= 19.5D) {
                    if (playerHPPercent >= 1.0D) {
                        newPlayerHP = 20;
                    } else {
                        newPlayerHP = 19;
                    }
                }
                if (newPlayerHP < 1) {
                    newPlayerHP = 1;
                }
                player.setHealth(newPlayerHP);
            }

        }
    }

    public static void handlePlayerBeingDamaged(Player player, double damage) {
        double maxHP = getPlayerMaxHPLive(player);
        double currentHP = getPlayerHPLive(player);
        double newHP = currentHP - damage;

        if (newHP <= 0 && DuelMechanics.isDueling(player.getUniqueId())) {
            newHP = 1;
        }

        if (newHP <= 0) {
            player.setHealth(0);
            return;
        }

        setPlayerHPLive(player, (int) newHP);
        Bukkit.broadcastMessage(String.valueOf(newHP));
        double playerHPPercent = (newHP / maxHP);
        double newPlayerHPToDisplay = playerHPPercent * 20;
        int convHPToDisplay = (int) newPlayerHPToDisplay;
        if (convHPToDisplay <= 0) {
            convHPToDisplay = 1;
        }
        if (convHPToDisplay > 20) {
            convHPToDisplay = 20;
        }
        player.setHealth(convHPToDisplay);
        setPlayerHPLive(player, (int) newHP);
    }

    public static int generateMaxHPFromItems(Player player) {
        ItemStack[] playerArmor = player.getInventory().getArmorContents();
        double totalHP = 0;
        for (ItemStack itemStack : playerArmor) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            totalHP += getHealthValueOfItem(itemStack);
        }

        if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
            totalHP += getHealthValueOfItem(player.getItemInHand());
        }

        totalHP += 50;

        return (int) totalHP;
    }

    private static int getHealthValueOfItem(ItemStack itemStack) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(itemStack));
        int healthValue = 0;
        if (nmsItem == null || nmsItem.getTag() == null) {
            return 0;
        }
        if (!(nmsItem.getTag().getString("type").equalsIgnoreCase("armor") && nmsItem.getTag().getString("type").equalsIgnoreCase("weapon"))) {
            return 0;
        }
        if (nmsItem.getTag().getInt("health") > 0) {
            healthValue += nmsItem.getTag().getInt("healthPoints");
        }
        if (nmsItem.getTag().getInt("vitality") > 0) {
            healthValue +=  healthValue * ((nmsItem.getTag().getInt("vitality") * 0.034D) / 100.0D);
        }
        return healthValue;
    }

    private static int getHealthRegenFromItems(Player player) {
        ItemStack[] playerArmor = player.getInventory().getArmorContents();
        double totalHPRegen = 0;
        for (ItemStack itemStack : playerArmor) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            totalHPRegen += getHealthRegenValueOfItem(itemStack);
        }

        if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
            totalHPRegen += getHealthRegenValueOfItem(player.getItemInHand());
        }

        totalHPRegen += 50;

        return (int) totalHPRegen;
    }

    private static int getHealthRegenValueOfItem(ItemStack itemStack) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(itemStack));
        int healthRegen = 0;
        if (nmsItem == null || nmsItem.getTag() == null) {
            return 0;
        }
        if (!(nmsItem.getTag().getString("type").equalsIgnoreCase("armor") && nmsItem.getTag().getString("type").equalsIgnoreCase("weapon"))) {
            return 0;
        }
        if (nmsItem.getTag().getInt("healthRegen") > 0) {
            healthRegen += nmsItem.getTag().getInt("healthPoints");
        }
        if (nmsItem.getTag().getInt("vitality") > 0) {
            healthRegen +=  healthRegen * ((nmsItem.getTag().getInt("vitality") * 0.3D) / 100.0D);
        }
        return healthRegen;
    }

    public static void setPlayerHPRegenLive(Player player, int regenAmount) {
        player.setMetadata("regenHP", new FixedMetadataValue(DungeonRealms.getInstance(), regenAmount));
    }

    public static int getPlayerHPRegenLive(Player player) {
        if (player.hasMetadata("regenHP")) {
            return player.getMetadata("regenHP").get(0).asInt();
        } else {
            return generateMaxHPFromItems(player);
        }
    }
}
