package net.dungeonrealms.game.handlers;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.profession.Fishing;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Created by Kieran on 9/24/2015.
 */
public class EnergyHandler implements GenericMechanic {

    private static EnergyHandler instance = null;

    public static EnergyHandler getInstance() {
        if (instance == null) {
            instance = new EnergyHandler();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }

    @Override
	public void startInitialization() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::regenerateAllPlayerEnergy, 40, 3L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::removePlayerEnergySprint, 40, 10L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::addStarvingPotionEffect, 40, 15L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::regenerateFoodInSafezones, 40, 40L);
    }

    @Override
    public void stopInvocation() {

    }

    /**
     * Handles players logging out,
     * removes metadata from the player
     *
     * @param player
     * @since 1.0
     */
    public void handleLogoutEvents(Player player) {
        if (player.hasMetadata("starving")) {
            player.removeMetadata("starving", DungeonRealms.getInstance());
        }
        if (player.hasMetadata("sprinting")) {
            player.removeMetadata("sprinting", DungeonRealms.getInstance());
        }
        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.CURRENT_FOOD, player.getFoodLevel(), false);
    }

    /**
     * Handles players logging in,
     * adds metadata to the player if
     * applicable (no food level).
     *
     * @param player
     * @since 1.0
     */
    public void handleLoginEvents(Player player) {
        int foodLevel = Integer.valueOf(String.valueOf(DatabaseAPI.getInstance().getData(EnumData.CURRENT_FOOD, player.getUniqueId())));
        if (foodLevel < 0) {
            foodLevel = 0;
        }
        player.setFoodLevel(foodLevel);
        if (foodLevel <= 0) {
            if (player.isOp() || player.getGameMode() == GameMode.CREATIVE) {
                return;
            }
            if (!(player.hasMetadata("starving"))) {
                player.setMetadata("starving", new FixedMetadataValue(DungeonRealms.getInstance(), "true"));
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "**STARVING**"), 20L);
            }
        }
    }

    private void regenerateFoodInSafezones() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!API.isInSafeRegion(player.getLocation())) {
                continue;
            }
            if (player.getFoodLevel() >= 20) {
                continue;
            }
            player.setFoodLevel(player.getFoodLevel() + 8);
        }
    }

    /**
     * Handles the regeneration of energy
     * for all players applicable on the
     * server.
     *
     * @since 1.0
     */
    private void regenerateAllPlayerEnergy() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!API.isPlayer(player)) {
                continue;
            }
            if (getPlayerCurrentEnergy(player) == 1.0F) {
                continue;
            }
            if (getPlayerCurrentEnergy(player) > 1.0F) {
                player.setExp(1.0F);
                updatePlayerEnergyBar(player);
                continue;
            }
            float regenAmount = getPlayerEnergyRegenerationAmount(player.getUniqueId());
            if (!(player.hasPotionEffect(PotionEffectType.SLOW_DIGGING))) {
                if (player.hasMetadata("starving")) {
                    regenAmount = 0.06F;
                }
                regenAmount = regenAmount / 6.3F;
                GamePlayer gp = API.getGamePlayer(player);
                if (gp == null || gp.getStats() == null) return;
                regenAmount += (int) (regenAmount * gp.getStats().getEnergyRegen());
                if(Fishing.fishBuffs.containsKey(player.getUniqueId()) && Fishing.fishBuffs.get(player.getUniqueId()).equalsIgnoreCase("Energy Regen")){
                	regenAmount += .15F;
                }
                addEnergyToPlayerAndUpdate(player, regenAmount);
            }
        }
    }

    /**
     * Returns the players current
     * energy value
     *
     * @param player
     * @return float
     * @since 1.0
     */
    public static float getPlayerCurrentEnergy(Player player) {
        return player.getExp();
    }

    /**
     * Updates the players Minecraft EXP bar
     * with our custom energy values
     *
     * @param player
     * @since 1.0
     */
    private static void updatePlayerEnergyBar(Player player) {
        float currExp = getPlayerCurrentEnergy(player);
        double percent = currExp * 100.00D;
        if (percent > 100) {
            percent = 100;
        }
        if (percent < 0) {
            percent = 0;
        }
        player.setLevel(((int) percent));
    }

    /**
     * Returns the players current
     * energy regeneration value
     *
     * @param uuid
     * @return float
     * @since 1.0
     */
    public float getPlayerEnergyRegenerationAmount(UUID uuid) {
        float regenAmount;
        Player player = Bukkit.getPlayer(uuid);
        double regenMod = 0;
        for (ItemStack itemStack : player.getEquipment().getArmorContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            regenMod += getEnergyValueOfArmor(itemStack);
        }

        regenAmount = ((float) regenMod / 100.0F);
        regenAmount += 0.10F;
        double intMod = 0;
        for (ItemStack itemStack : player.getEquipment().getArmorContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            intMod += getIntellectValueOfArmor(itemStack);
            //regenAmount = getIntellectValueOfArmor(itemStack, regenAmount);
        }

        //regenAmount += (regenAmount / 100F) * (getEnergyValueOfArmor(itemStack) + 1);
        if (intMod > 0) {
            regenAmount += (float) (((intMod * 0.015F) / 100.0F));
        }

        return regenAmount;
    }

    /**
     * Calculates the Energy Regen value
     * of an itemstack
     *
     * @param itemStack
     * @return int
     * @since 1.0
     */
    public int getEnergyValueOfArmor(ItemStack itemStack) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(itemStack));
        int energyRegen = 0;
        if (nmsItem == null || nmsItem.getTag() == null) {
            return 0;
        }
        if (!(nmsItem.getTag().getString("type").equalsIgnoreCase("armor"))) {
            return 0;
        }
        if (nmsItem.getTag().getInt("energyRegen") > 0) {
            energyRegen += nmsItem.getTag().getInt("energyRegen");
        }
        return energyRegen;
    }

    /**
     * Calculates the Intellect value
     * of an itemstack
     *
     * @param itemStack
     * @return int
     * @since 1.0
     */
    /*public float getIntellectValueOfArmor(ItemStack itemStack, float regenTotal) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(itemStack));
        if (nmsItem == null || nmsItem.getTag() == null) {
            return regenTotal;
        }
        if (!(nmsItem.getTag().getString("type").equalsIgnoreCase("armor"))) {
            return regenTotal;
        }
        if (nmsItem.getTag().getInt("intellect") > 0) {
            regenTotal += regenTotal * ((nmsItem.getTag().getInt("intellect") * 0.015F) / 100.0f);
        }
        return regenTotal;
    }*/

    public int getIntellectValueOfArmor(ItemStack itemStack) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(itemStack));
        int intMod = 0;
        if (nmsItem == null || nmsItem.getTag() == null) {
            return intMod;
        }
        if (!(nmsItem.getTag().getString("type").equalsIgnoreCase("armor"))) {
            return intMod;
        }
        if (nmsItem.getTag().getInt("intellect") > 0) {
            intMod += nmsItem.getTag().getInt("intellect");
        }
        return intMod;
    }


    /**
     * Returns the players current
     * energy regeneration value
     *
     * @param uuid
     * @return float
     * @since 1.0
     */
    public float getPlayerEnergyPercentage(UUID uuid) {
        int regenAmount = 100;
        Player player = Bukkit.getPlayer(uuid);
        EntityEquipment playerEquipment = player.getEquipment();
        ItemStack[] playerArmor = playerEquipment.getArmorContents();
        NBTTagCompound nmsTags[] = new NBTTagCompound[4];
        if (playerArmor[3].getType() != null && playerArmor[3].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(playerArmor[3]).getTag() != null) {
                nmsTags[0] = CraftItemStack.asNMSCopy(playerArmor[3]).getTag();
            }
        }
        if (playerArmor[2].getType() != null && playerArmor[2].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(playerArmor[2]).getTag() != null) {
                nmsTags[1] = CraftItemStack.asNMSCopy(playerArmor[2]).getTag();
            }
        }
        if (playerArmor[1].getType() != null && playerArmor[1].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(playerArmor[1]).getTag() != null) {
                nmsTags[2] = CraftItemStack.asNMSCopy(playerArmor[1]).getTag();
            }
        }
        if (playerArmor[0] != null && playerArmor[0].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(playerArmor[0]).getTag() != null) {
                nmsTags[3] = CraftItemStack.asNMSCopy(playerArmor[0]).getTag();
            }
        }
        for (NBTTagCompound nmsTag : nmsTags) {
            if (nmsTag == null) {
                regenAmount += 0;
            } else {
                if (nmsTag.getInt("energyRegen") != 0) {
                    regenAmount += nmsTag.getInt("energyRegen");
                }
            }
        }
        return regenAmount;
    }

    /**
     * Adds energy to the defined player
     *
     * @param player
     * @param amountToAdd
     * @since 1.0
     */
    private static void addEnergyToPlayerAndUpdate(Player player, float amountToAdd) {
        if (getPlayerCurrentEnergy(player) == 1) {
            return;
        }
        player.setExp(player.getExp() + amountToAdd);
        updatePlayerEnergyBar(player);
    }

    /**
     * Handles the removal of energy while
     * players are sprinting
     *
     * @return float
     * @since 1.0
     */
    private void removePlayerEnergySprint() {
        Bukkit.getOnlinePlayers().stream().filter(Player::isSprinting).forEach(player -> {
            removeEnergyFromPlayerAndUpdate(player.getUniqueId(), 0.15F);
            if (getPlayerCurrentEnergy(player) <= 0 || player.hasMetadata("starving")) {
                player.setSprinting(false);
                player.removeMetadata("sprinting", DungeonRealms.getInstance());
                if (!player.hasPotionEffect(PotionEffectType.JUMP)) {
                    int foodLevel = player.getFoodLevel();
                    if (player.getFoodLevel() > 1) {
                        player.setFoodLevel(1);
                    }
                    player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "**EXHAUSTED**");
                    if (foodLevel > 1) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.setFoodLevel(foodLevel), 40L);
                    }
                }
            }
        });
    }

    /**
     * Handles the removal of energy from
     * a player and updates their bar.
     * Used when auto-attacking etc.
     *
     * @param uuid
     * @param amountToRemove
     * @since 1.0
     */
    public static void removeEnergyFromPlayerAndUpdate(UUID uuid, float amountToRemove) {
        Player player = Bukkit.getPlayer(uuid);
        if (player.isOp()) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (API.isInSafeRegion(player.getLocation())) return;
        if (player.hasMetadata("last_energy_remove")) {
            if ((System.currentTimeMillis() - player.getMetadata("last_energy_remove").get(0).asLong()) < 100) {
                return;
            }
        }
        player.setMetadata("last_energy_remove", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
        if (getPlayerCurrentEnergy(player) <= 0) return;
        if ((getPlayerCurrentEnergy(player) - amountToRemove) <= 0) {
            player.setExp(0.0F);
            updatePlayerEnergyBar(player);
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 50, 4)), 0L);
            return;
        }
        player.setExp(getPlayerCurrentEnergy(player) - amountToRemove);
        updatePlayerEnergyBar(player);
    }

    /**
     * Adds the hunger potion effect
     * to a player and "starving" as
     * metadata when they have 0 food level.
     *
     * @since 1.0
     */
    private void addStarvingPotionEffect() {
        Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPotionEffect(PotionEffectType.HUNGER) && player.hasMetadata("starving")).forEach(player -> {
            if (player.getFoodLevel() <= 0) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 0)), 0L);
            } else {
                player.removeMetadata("starving", DungeonRealms.getInstance());
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.removePotionEffect(PotionEffectType.HUNGER), 0L);
            }
        });
    }

    /**
     * Returns the energy cost
     * of an item
     *
     * @param itemStack
     * @return float
     * @since 1.0
     */
    public static float getWeaponSwingEnergyCost(ItemStack itemStack) {
        Material material = itemStack.getType();
        switch (material) {
            case AIR:
                return 0.045f;
            case WOOD_SWORD:
                return 0.054f;
            case STONE_SWORD:
                return 0.064f;
            case IRON_SWORD:
                return 0.075f;
            case DIAMOND_SWORD:
                return 0.113f;
            case GOLD_SWORD:
                return 0.123f;
            case WOOD_AXE:
                return 0.0642F * 1.1F;
            case STONE_AXE:
                return 0.0763F * 1.1F;
            case IRON_AXE:
                return 0.09F * 1.1F;
            case DIAMOND_AXE:
                return 0.113F * 1.1F;
            case GOLD_AXE:
                return 0.123F * 1.1F;
            case WOOD_SPADE:
                return 0.0642F;
            case STONE_SPADE:
                return 0.0763F;
            case IRON_SPADE:
                return 0.09F;
            case DIAMOND_SPADE:
                return 0.113F;
            case GOLD_SPADE:
                return 0.123F;
            case WOOD_HOE:
                return 0.11F / 1.1F;
            case STONE_HOE:
                return 0.13F / 1.1F;
            case IRON_HOE:
                return 0.14F / 1.1F;
            case DIAMOND_HOE:
                return 0.15F / 1.1F;
            case GOLD_HOE:
                return 0.16F / 1.1F;
            case BOW:
                return 0.099F;
        }
        return 0.10F;
    }
}
