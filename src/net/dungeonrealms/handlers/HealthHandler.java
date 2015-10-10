package net.dungeonrealms.handlers;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.duel.DuelMechanics;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumOperators;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;

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

    /**
     * Handles players logging in,
     * sets their metadata to
     * their correct HP values.
     *
     * @param player
     * @since 1.0
     */
    public void handleLoginEvents(Player player) {
        setPlayerMaxHPLive(player, getPlayerMaxHPOnLogin(player.getUniqueId()));
        setPlayerHPLive(player, getPlayerMaxHPOnLogin(player.getUniqueId()));
        setPlayerHPRegenLive(player, getPlayerHPRegenLive(player));
        player.setMetadata("last_death_time", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
    }

    /**
     * Handles players logging out,
     * removes potion effects and
     * updates mongo for web usage.
     *
     * @param player
     * @since 1.0
     */
    public void handleLogoutEvents(Player player) {
        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "info.health", getPlayerMaxHPLive(player), false);
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
    }

    /**
     * Updates players "HP Bars"
     * using the bossbar API
     *
     *
     * @since 1.0
     */
    private void updatePlayerHPBars() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (getPlayerHPLive(player) > 0) {
                setPlayerOverheadHP(uuid, getPlayerHPLive(player));
            }
        }
    }

    /**
     * Returns the players current HP
     *
     * @param player
     * @return int
     * @since 1.0
     */
    public static int getPlayerHPLive(Player player) {
        if (player.hasMetadata("currentHP")) {
            return player.getMetadata("currentHP").get(0).asInt();
        } else {
            return 50; //This shouldn't happen but safety return. Probably kick them or something if their data cannot be loaded.
        }
    }

    /**
     * Sets the players HP bar
     * Called in "updatePlayerHPBars"
     *
     * @param uuid
     * @param hp
     * @since 1.0
     */
    public static void setPlayerOverheadHP(UUID uuid, int hp) {
        //Check their Max HP from wherever we decide to store it, get it as a percentage.
        //Update BarAPI thing with it.
    }

    /**
     * Sets the players HP metadata
     * to the given value.
     *
     * @param player
     * @param hp
     * @since 1.0
     */
    public static void setPlayerHPLive(Player player, int hp) {
        player.setMetadata("currentHP", new FixedMetadataValue(DungeonRealms.getInstance(), hp));
    }

    /**
     * Returns the players max HP
     * Called on login (calculates it from items
     * in their inventory)
     * Pretty expensive check.
     *
     * @param uuid
     * @return int
     * @since 1.0
     */
    public static int getPlayerMaxHPOnLogin(UUID uuid) {
        return calculateMaxHPFromItems(Bukkit.getPlayer(uuid)); //This shouldn't happen but safety return. Probably kick them or something if their data cannot be loaded.
    }

    /**
     * Returns the players current MaximumHP
     *
     * @param player
     * @return int
     * @since 1.0
     */
    public static int getPlayerMaxHPLive(Player player) {
        if (player.hasMetadata("maxHP")) {
            return player.getMetadata("maxHP").get(0).asInt();
        } else {
            return calculateMaxHPFromItems(player);
        }
    }

    /**
     * Sets the players MaximumHP metadata
     * to the given value.
     *
     * @param player
     * @param maxHP
     * @since 1.0
     */
    public static void setPlayerMaxHPLive(Player player, int maxHP) {
        player.setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), maxHP));
    }

    /**
     * Handles all players regenerating
     * their health.
     *
     * @since 1.0
     */
    private void regenerateHealth() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getPlayerHPLive(player) <= 0 && player.getHealth() <= 0) {
                continue;
            }
            if (player.hasMetadata("starving")) {
                continue;
            }
            if (CombatLog.isInCombat(player)) {
                continue;
            }
            //Check their Max HP from wherever we decide to store it.
            if (!CombatLog.isInCombat(player)) {
                double currentHP = getPlayerHPLive(player);
                double amountToHealPlayer = getPlayerHPRegenLive(player);
                double maxHP = getPlayerMaxHPLive(player);
                if (currentHP + 1 > maxHP) {
                    if (player.getHealth() != 20) {
                        player.setHealth(20);
                    }
                    continue;
                }

                if ((currentHP + amountToHealPlayer) >= maxHP) {
                    player.setHealth(20);
                    setPlayerHPLive(player, (int) maxHP);
                } else if (player.getHealth() <= 19 && ((currentHP + amountToHealPlayer) < maxHP)) {
                    setPlayerHPLive(player, (int) (getPlayerHPLive(player) + amountToHealPlayer));
                    double playerHPPercent = (getPlayerHPLive(player) + amountToHealPlayer) / maxHP;
                    double newPlayerHP = playerHPPercent * 20;
                    if (newPlayerHP >= 19.50D) {
                        if (playerHPPercent >= 1.0D) {
                            newPlayerHP = 20;
                        } else {
                            newPlayerHP = 19;
                        }
                    }
                    if (newPlayerHP < 1) {
                        newPlayerHP = 1;
                    }
                    player.setHealth((int) newPlayerHP);
                }
            }
        }
    }

    public void healPlayerByAmount(Player player, int amount) {
        double currentHP = getPlayerHPLive(player);
        double maxHP = getPlayerMaxHPLive(player);
        if (currentHP + 1 > maxHP) {
            if (player.getHealth() != 20) {
                player.setHealth(20);
            }
        }

        if ((currentHP + (double) amount) >= maxHP) {
            player.setHealth(20);
            setPlayerHPLive(player, (int) maxHP);
        } else if (player.getHealth() <= 19 && ((currentHP + (double) amount) < maxHP)) {
            setPlayerHPLive(player, (int) (getPlayerHPLive(player) + (double) amount));
            double playerHPPercent = (getPlayerHPLive(player) + (double) amount) / maxHP;
            double newPlayerHP = playerHPPercent * 20;
            if (newPlayerHP >= 19.50D) {
                if (playerHPPercent >= 1.0D) {
                    newPlayerHP = 20;
                } else {
                    newPlayerHP = 19;
                }
            }
            if (newPlayerHP < 1) {
                newPlayerHP = 1;
            }
            player.setHealth((int) newPlayerHP);
        }
    }
    /**
     * Called from damage event,
     * used to update the players
     * health and kill etc if
     * necessary
     *
     * @param player
     * @param damage
     * @since 1.0
     */
    public static void handlePlayerBeingDamaged(Player player, Entity damager, double damage) {
        double maxHP = getPlayerMaxHPLive(player);
        double currentHP = getPlayerHPLive(player);
        double newHP = currentHP - damage;

        if (newHP <= 0 && DuelMechanics.isDueling(player.getUniqueId())) {
            newHP = 1;
        }

        if (newHP <= 0) {
            if (player.hasMetadata("last_death_time")) {
                if (player.getMetadata("last_death_time").get(0).asLong() > 100) {
                    player.setMetadata("last_death_time", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
                    player.setHealth(0);
                    KarmaHandler.handlePlayerPsuedoDeath(player, damager);
                    CombatLog.removeFromCombat(player);
                    Bukkit.broadcastMessage(player.getName() + " has died.");
                    return;
                }
            } else {
                player.setMetadata("last_death_time", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
                player.setHealth(0);
                KarmaHandler.handlePlayerPsuedoDeath(player, damager);
                Bukkit.broadcastMessage(player.getName() + " has died.");
                return;
            }
        }

        setPlayerHPLive(player, (int) newHP);
        double playerHPPercent = (newHP / maxHP);
        double newPlayerHPToDisplay = playerHPPercent * 20.0D;
        int convHPToDisplay = (int) newPlayerHPToDisplay;
        if (convHPToDisplay <= 0) {
            convHPToDisplay = 1;
        }
        if (convHPToDisplay > 20) {
            convHPToDisplay = 20;
        }
        player.setHealth(convHPToDisplay);
    }

    /**
     * Calculates the players MaximumHP
     * from their armor and weapon
     *
     * @param player
     * @return int
     * @since 1.0
     */
    public static int calculateMaxHPFromItems(Player player) {
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

    /**
     * Calculates the HP value
     * of an itemstack
     *
     * @param itemStack
     * @return int
     * @since 1.0
     */
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
            healthValue += healthValue * ((nmsItem.getTag().getInt("vitality") * 0.034D) / 100.0D);
        }
        return healthValue;
    }

    /**
     * Calculates the players HPRegen
     * from their armor and weapon
     *
     * @param player
     * @return int
     * @since 1.0
     */
    private static int calculateHealthRegenFromItems(Player player) {
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

        totalHPRegen += 5;

        return (int) totalHPRegen;
    }

    /**
     * Calculates the HPRegen value
     * of an itemstack
     *
     * @param itemStack
     * @return int
     * @since 1.0
     */
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
            healthRegen += healthRegen * ((nmsItem.getTag().getInt("vitality") * 0.3D) / 100.0D);
        }
        return healthRegen;
    }

    /**
     * Sets the players HP Regen
     * metadata to the given amount
     *
     * @param player
     * @param regenAmount
     * @since 1.0
     */
    public static void setPlayerHPRegenLive(Player player, int regenAmount) {
        player.setMetadata("regenHP", new FixedMetadataValue(DungeonRealms.getInstance(), regenAmount));
    }

    /**
     * Returns the players current HPRegen
     *
     * @param player
     * @return int
     * @since 1.0
     */
    public static int getPlayerHPRegenLive(Player player) {
        if (player.hasMetadata("regenHP")) {
            return player.getMetadata("regenHP").get(0).asInt();
        } else {
            return calculateHealthRegenFromItems(player);
        }
    }

}
