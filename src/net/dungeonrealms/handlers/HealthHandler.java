package net.dungeonrealms.handlers;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.duel.DuelMechanics;
import net.dungeonrealms.entities.Entities;
import net.dungeonrealms.mastery.GamePlayer;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.inventivetalent.bossbar.BossBarAPI;

import java.util.Random;

/**
 * Created by Kieran on 10/3/2015.
 */
public class HealthHandler implements GenericMechanic{

    private static HealthHandler instance = null;

    public static HealthHandler getInstance() {
        if (instance == null) {
            instance = new HealthHandler();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    public void startInitialization() {
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::updatePlayerHPBars, 40, 5L);
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::regenerateHealth, 40, 20L);
    }

    @Override
    public void stopInvocation() {

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
        setPlayerMaxHPLive(player, getPlayerMaxHPOnLogin(player));
        setPlayerHPLive(player, getPlayerMaxHPOnLogin(player));
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
        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.HEALTH, getPlayerMaxHPLive(player), false);
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
    }

    /**
     * Updates players "HP Bars"
     * using the bossbar API
     *
     * @since 1.0
     */
    private void updatePlayerHPBars() {
        Bukkit.getOnlinePlayers().stream().filter(player -> getPlayerHPLive(player) > 0).forEach(player -> setPlayerOverheadHP(player, getPlayerHPLive(player)));
    }

    /**
     * Returns the players current HP
     *
     * @param player
     * @return int
     * @since 1.0
     */
    public int getPlayerHPLive(Player player) {
        if (player.hasMetadata("currentHP")) {
            return player.getMetadata("currentHP").get(0).asInt();
        } else {
            return 50; //This shouldn't happen but safety return. Probably kick them or something if their data cannot be loaded.
        }
    }

    /**
     * Returns the monsters current HP
     *
     * @param entity
     * @return int
     * @since 1.0
     */
    public int getMonsterHPLive(LivingEntity entity) {
        if (entity.hasMetadata("currentHP")) {
            return entity.getMetadata("currentHP").get(0).asInt();
        } else {
            return 100;
        }
    }

    /**
     * Sets the players HP bar
     * Called in "updatePlayerHPBars"
     *
     * @param player
     * @param hp
     * @since 1.0
     */
    public void setPlayerOverheadHP(Player player, int hp) {
        //Check their Max HP from wherever we decide to store it, get it as a percentage.
        //Update BarAPI thing with it.
        ScoreboardHandler.getInstance().updatePlayerHP(player, hp);
        if (!API.isPlayer(player)) {
            return;
        }
        double maxHP = getPlayerMaxHPLive(player);
        double healthPercentage = ((double) hp / maxHP);
        if (healthPercentage * 100.0F > 100.0F) {
            healthPercentage = 1.0;
        }
        float healthToDisplay = (float) (healthPercentage * 100.F);
        GamePlayer gamePlayer = API.getGamePlayer(player);
        if(gamePlayer == null){
        	//Utils.log.info("NULL GAME PLAYER");
        	return;
        }
        int playerLevel = gamePlayer.getLevel();
        double currentEXP = gamePlayer.getExperience();
        double expToLevel = gamePlayer.getEXPNeeded(playerLevel);
        String playerLevelInfo = ChatColor.AQUA.toString() + ChatColor.BOLD + "LVL " + ChatColor.AQUA + playerLevel;
        String separator = ChatColor.BLACK.toString() + ChatColor.BOLD + " - ";
        String playerHPInfo = "";
        if (API.isInSafeRegion(player.getLocation())) {
            playerHPInfo = ChatColor.GREEN.toString() + ChatColor.BOLD + "HP " + ChatColor.GREEN + hp + ChatColor.BOLD + " / " + ChatColor.GREEN + (int) maxHP;
        }
        if (!API.isInSafeRegion(player.getLocation()) && API.isNonPvPRegion(player.getLocation())) {
            playerHPInfo = ChatColor.YELLOW.toString() + ChatColor.BOLD + "HP " + ChatColor.YELLOW + hp + ChatColor.BOLD + " / " + ChatColor.YELLOW + (int) maxHP;
        }
        if (!API.isInSafeRegion(player.getLocation()) && !API.isNonPvPRegion(player.getLocation())) {
            playerHPInfo = ChatColor.RED.toString() + ChatColor.BOLD + "HP " + ChatColor.RED + hp + ChatColor.BOLD + " / " + ChatColor.RED + (int) maxHP;
        }
        String playerEXPInfo = ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "EXP " + ChatColor.LIGHT_PURPLE + Math.round((currentEXP / expToLevel) * 100.0) + "%";

        BossBarAPI.setMessage(player, playerLevelInfo + separator + playerHPInfo + separator + playerEXPInfo, 100F);
        BossBarAPI.setHealth(player, healthToDisplay);
    }

    /**
     * Sets the players HP metadata
     * to the given value.
     *
     * @param player
     * @param hp
     * @since 1.0
     */
    public void setPlayerHPLive(Player player, int hp) {
        player.setMetadata("currentHP", new FixedMetadataValue(DungeonRealms.getInstance(), hp));
    }

    /**
     * Sets the monsters HP metadata
     * to the given value.
     *
     * @param entity
     * @param hp
     * @since 1.0
     */
    public void setMonsterHPLive(LivingEntity entity, int hp) {
        entity.setMetadata("currentHP", new FixedMetadataValue(DungeonRealms.getInstance(), hp));
    }

    /**
     * Returns the players max HP
     * Called on login (calculates it from items
     * in their inventory)
     * Pretty expensive check.
     *
     * @param player
     * @return int
     * @since 1.0
     */
    public int getPlayerMaxHPOnLogin(Player player) {
        return API.getGamePlayer(player).getPlayerMaxHP();
    }

    /**
     * Returns the entities max HP
     * Called on login (calculates it from items
     * in their inventory)
     * Pretty expensive check.
     *
     * @param entity
     * @return int
     * @since 1.0
     */
    public int getMonsterMaxHPOnSpawn(LivingEntity entity) {
        if (entity.hasMetadata("maxHP")) {
            return (entity.getMetadata("maxHP").get(0).asInt() + calculateMaxHPFromItems(entity));
        } else {
            return 100 + calculateMaxHPFromItems(entity);
        }
    }

    /**
     * Returns the players current MaximumHP
     *
     * @param player
     * @return int
     * @since 1.0
     */
    public int getPlayerMaxHPLive(Player player) {
        if (player.hasMetadata("maxHP")) {
            return player.getMetadata("maxHP").get(0).asInt();
        } else {
            return calculateMaxHPFromItems(player);
        }
    }

    /**
     * Returns the monsters current MaximumHP
     *
     * @param entity
     * @return int
     * @since 1.0
     */
    public int getMonsterMaxHPLive(LivingEntity entity) {
        if (entity.hasMetadata("maxHP")) {
            return entity.getMetadata("maxHP").get(0).asInt();
        } else {
            return 100;
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
    public void setPlayerMaxHPLive(Player player, int maxHP) {
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
            if (!API.isPlayer(player)) {
                continue;
            }
            //Check their Max HP from wherever we decide to store it.
            if (!CombatLog.isInCombat(player)) {
                double currentHP = getPlayerHPLive(player);
                double amountToHealPlayer = getPlayerHPRegenLive(player);
                GamePlayer gp = API.getGamePlayer(player);
                if(gp == null || gp.getStats() == null)
                	return;
                amountToHealPlayer += amountToHealPlayer * gp.getStats().getHPRegen();
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

    /**
     * Heals a player by the
     * specified amount. Used
     * currently for lifesteal on
     * weapons/armor.
     *
     * @param player
     * @param amount
     * @since 1.0
     */
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
     * Heals a monster by the
     * specified amount. Used
     * currently for lifesteal on
     * weapons/armor.
     *
     * @param entity
     * @param amount
     * @since 1.0
     */
    public void healMonsterByAmount(LivingEntity entity, int amount) {
        double currentHP = getMonsterHPLive(entity);
        double maxHP = getMonsterMaxHPLive(entity);
        if (currentHP + 1 > maxHP) {
            if (entity.getHealth() != 20) {
                entity.setHealth(20);
            }
        }

        if ((currentHP + (double) amount) >= maxHP) {
            entity.setHealth(20);
            setMonsterHPLive(entity, (int) maxHP);
        } else if (entity.getHealth() <= 19 && ((currentHP + (double) amount) < maxHP)) {
            setMonsterHPLive(entity, (int) (getMonsterHPLive(entity) + (double) amount));
            double monsterHPPercent = (getMonsterHPLive(entity) + (double) amount) / maxHP;
            double newMonsterHP = monsterHPPercent * 20;
            if (newMonsterHP >= 19.50D) {
                if (monsterHPPercent >= 1.0D) {
                    newMonsterHP = 20;
                } else {
                    newMonsterHP = 19;
                }
            }
            if (newMonsterHP < 1) {
                newMonsterHP = 1;
            }
            entity.setHealth((int) newMonsterHP);
        }
    }

    /**
     * Called from damage event,
     * used to update the players
     * health and kill etc if
     * necessary
     *
     * @param player
     * @param damager
     * @param damage
     * @since 1.0
     */
    public void handlePlayerBeingDamaged(Player player, Entity damager, double damage) {
        double maxHP = getPlayerMaxHPLive(player);
        double currentHP = getPlayerHPLive(player);
        double newHP = currentHP - damage;

        if (newHP <= 0 && DuelMechanics.isDueling(player.getUniqueId())) {
            newHP = 1;
        }

        if (newHP <= 0 && API.isPlayer(damager) && Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_CHAOTIC_PREVENTION, damager.getUniqueId()).toString())) {
            if (KarmaHandler.getInstance().getPlayerRawAlignment(player).equalsIgnoreCase(KarmaHandler.EnumPlayerAlignments.LAWFUL.name())) {
                newHP = 1;
                damager.sendMessage(ChatColor.YELLOW + "Your Chaotic Prevention Toggle has activated preventing the death of " + player.getName() + "!");
                player.sendMessage(ChatColor.YELLOW + damager.getName() + " has their Chaotic Prevention Toggle ON, your life has been spared!");
            }
        }

        if (newHP <= 0) {
            if (player.hasMetadata("last_death_time")) {
                if (player.getMetadata("last_death_time").get(0).asLong() > 100) {
                    player.setMetadata("last_death_time", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
                    player.damage(25);
                    //TODO: WATCH THIS
                    //player.setHealth(0);
                    KarmaHandler.getInstance().handlePlayerPsuedoDeath(player, damager);
                    CombatLog.removeFromCombat(player);
                    Bukkit.broadcastMessage(player.getName() + " has died.");
                    return;
                }
            } else {
                player.setMetadata("last_death_time", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
                player.damage(25);
                //TODO: WATCH THIS
                //player.setHealth(0);
                KarmaHandler.getInstance().handlePlayerPsuedoDeath(player, damager);
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
        LivingEntity leAttacker = null;
        if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId()).toString())) {
            player.sendMessage(ChatColor.RED + "     -" + (int) damage + ChatColor.BOLD + " HP" + ChatColor.RED + " -> " + ChatColor.GREEN + " [" + newHP + ChatColor.BOLD + "HP" + ChatColor.GREEN + "]");
        }
        if (API.isPlayer(damager)) {
            if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, damager.getUniqueId()).toString())) {
                damager.sendMessage(ChatColor.RED + "     " + (int) damage + ChatColor.BOLD + " Damage" + ChatColor.RED + " -> " + ChatColor.DARK_PURPLE + player.getName() + "[" + newHP + ChatColor.BOLD + "HP" + ChatColor.DARK_PURPLE + "]");
            }
            return;
        }
        if (damager instanceof CraftLivingEntity) {
            if (damager.hasMetadata("type")) {
                leAttacker = (LivingEntity) damager;
            }
        } else {
            switch (damager.getType()) {
                case ARROW:
                    if (((Arrow) damager).getShooter() instanceof LivingEntity) {
                        leAttacker = (LivingEntity) ((Arrow) damager).getShooter();
                    }
                    break;
                case SNOWBALL:
                    if (((Snowball) damager).getShooter() instanceof LivingEntity) {
                        leAttacker = (LivingEntity) ((Snowball) damager).getShooter();
                    }
                    break;
                case WITHER_SKULL:
                    if (((WitherSkull) damager).getShooter() instanceof LivingEntity) {
                        leAttacker = (LivingEntity) ((WitherSkull) damager).getShooter();
                    }
                    break;
                default:
                    break;
            }
        }
        if (!(leAttacker == null) && !(API.isPlayer(leAttacker))) {
            Entities.getInstance().MONSTER_LAST_ATTACK.put(leAttacker, 15);
            if (!Entities.getInstance().MONSTERS_LEASHED.contains(leAttacker)) {
                Entities.getInstance().MONSTERS_LEASHED.add(leAttacker);
            }
        }
    }

    /**
     * Called from damage event,
     * used to update the monsters
     * health and kill etc if
     * necessary
     *
     * @param entity
     * @param damage
     * @since 1.0
     */
    public void handleMonsterBeingDamaged(LivingEntity entity, LivingEntity attacker, double damage) {
        double maxHP = getMonsterMaxHPLive(entity);
        double currentHP = getMonsterHPLive(entity);
        double newHP = currentHP - damage;
        if(entity instanceof EntityArmorStand)
        	return;

        if (newHP <= 0) {
            setMonsterHPLive(entity, 0);
            net.minecraft.server.v1_8_R3.Entity entity1 = ((CraftEntity) entity).getHandle();
            entity1.damageEntity(DamageSource.GENERIC, 20F);
            if (!entity1.dead) {
                entity1.dead = true;
            }
            if (Entities.getInstance().MONSTER_LAST_ATTACK.containsKey(entity)) {
                Entities.getInstance().MONSTER_LAST_ATTACK.remove(entity);
            }
            if (Entities.getInstance().MONSTERS_LEASHED.contains(entity)) {
                Entities.getInstance().MONSTERS_LEASHED.remove(entity);
            }
            if(entity.hasMetadata("type") && entity.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")){
            	((net.dungeonrealms.entities.types.monsters.Monster)entity1).onMonsterDeath();
            	if(attacker instanceof Player){
            		double exp  = 0;
            		int lvl = entity.getMetadata("level").get(0).asInt();
            		int tier = entity.getMetadata("tier").get(0).asInt();
            		switch(tier){
            		case 1:
            			exp = 100 + new Random().nextInt(25);
            			break;
            		case 2:
            			exp = 150 + new Random().nextInt(25);
            			break;
            		case 3:
            			exp = 200 + new Random().nextInt(25);
            			break;
            		case 4:
            			exp = 250 + new Random().nextInt(25);
            			break;
            		case 5:
            			exp = 300 + new Random().nextInt(25);
            			break;
            		}
            		API.getGamePlayer((Player) attacker).addExperience(exp);
            	}
            }
            return;
        }

        setMonsterHPLive(entity, (int) newHP);
        double monsterHPPercent = (newHP / maxHP);
        double newMonsterHPToDisplay = monsterHPPercent * 20.0D;
        int convHPToDisplay = (int) newMonsterHPToDisplay;
        if (convHPToDisplay <= 0) {
            convHPToDisplay = 1;
        }
        if (convHPToDisplay > 20) {
            convHPToDisplay = 20;
        }

        if (API.isPlayer(attacker)) {
            if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, attacker.getUniqueId()).toString())) {
                if (entity.getPassenger() != null) {
                    String customNameAppended = entity.getPassenger().getCustomName().split("]")[1];
                    attacker.sendMessage(ChatColor.RED + "" + (int) damage + ChatColor.BOLD + " Damage" + ChatColor.RED + " -> " + ChatColor.DARK_PURPLE + customNameAppended + ChatColor.BOLD + " [" + newHP + "]");
                } else {
                    attacker.sendMessage(ChatColor.RED + "" + (int) damage + ChatColor.BOLD + " Damage" + ChatColor.RED + " -> " + ChatColor.DARK_PURPLE + "MOB" + ChatColor.BOLD + " [" + newHP + "]");
                }
            }
        }
        int level = entity.getMetadata("level").get(0).asInt();
        EntityLiving entity1 = ((CraftLivingEntity)entity).getHandle();	
        String name = entity.getMetadata("customname").get(0).asString();
        if (entity.getPassenger() != null && !entity.hasMetadata("isElite"))
		entity.getPassenger().setCustomName(entity.getMetadata("currentHP").get(0).asInt() + ChatColor.RED.toString()+ " ❤" + ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] "
				+ ChatColor.RESET + name);
        entity.setHealth(convHPToDisplay);
        if (!Entities.getInstance().MONSTERS_LEASHED.contains(entity)) {
            Entities.getInstance().MONSTERS_LEASHED.add(entity);
        }
    }

    /**
     * Calculates the entities MaximumHP
     * from their armor and weapon
     *
     * @param entity
     * @return int
     * @since 1.0
     */
    public int calculateMaxHPFromItems(LivingEntity entity) {
        double totalHP = 50;
        for (ItemStack itemStack : entity.getEquipment().getArmorContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            totalHP += getHealthValueOfArmor(itemStack);
        }

        for (ItemStack itemStack : entity.getEquipment().getArmorContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            totalHP = getVitalityValueOfArmor(itemStack, totalHP);
        }

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
    private int getHealthValueOfArmor(ItemStack itemStack) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(itemStack));
        int healthValue = 0;
        if (nmsItem == null || nmsItem.getTag() == null) {
            return 0;
        }
        if (!(nmsItem.getTag().getString("type").equalsIgnoreCase("armor"))) {
            return 0;
        }
        if (nmsItem.getTag().getInt("healthPoints") > 0) {
            healthValue += nmsItem.getTag().getInt("healthPoints");
        }
        return healthValue;
    }

    private int getVitalityValueOfArmor(ItemStack itemStack, double hpTotal) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(itemStack));
        if (nmsItem == null || nmsItem.getTag() == null) {
            return 0;
        }
        if (!(nmsItem.getTag().getString("type").equalsIgnoreCase("armor"))) {
            return 0;
        }
        if (nmsItem.getTag().getInt("vitality") > 0) {
            hpTotal += hpTotal * ((nmsItem.getTag().getInt("vitality") * 0.034D) / 100.0D);
        }
        return (int) hpTotal;
    }

    /**
     * Calculates the players HPRegen
     * from their armor and weapon
     *
     * @param player
     * @return int
     * @since 1.0
     */
    private int calculateHealthRegenFromItems(Player player) {
        double totalHPRegen = 5;
        for (ItemStack itemStack : player.getEquipment().getArmorContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            totalHPRegen += getHealthRegenValueOfArmor(itemStack);
        }

        for (ItemStack itemStack : player.getEquipment().getArmorContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            totalHPRegen = getHealthRegenVitalityFromArmor(itemStack, totalHPRegen);
        }

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
    private int getHealthRegenValueOfArmor(ItemStack itemStack) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(itemStack));
        int healthRegen = 0;
        if (nmsItem == null || nmsItem.getTag() == null) {
            return 0;
        }
        if (!(nmsItem.getTag().getString("type").equalsIgnoreCase("armor"))) {
            return 0;
        }
        if (nmsItem.getTag().getInt("healthRegen") > 0) {
            healthRegen += nmsItem.getTag().getInt("healthRegen");
        }
        return healthRegen;
    }

    private int getHealthRegenVitalityFromArmor(ItemStack itemStack, double totalRegen) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(itemStack));
        if (nmsItem == null || nmsItem.getTag() == null) {
            return 0;
        }
        if (!(nmsItem.getTag().getString("type").equalsIgnoreCase("armor"))) {
            return 0;
        }
        if (nmsItem.getTag().getInt("vitality") > 0) {
            totalRegen += totalRegen * ((nmsItem.getTag().getInt("vitality") * 0.3D) / 100.0D);
        }
        return (int) totalRegen;
    }

    /**
     * Sets the players HP Regen
     * metadata to the given amount
     *
     * @param player
     * @param regenAmount
     * @since 1.0
     */
    public void setPlayerHPRegenLive(Player player, int regenAmount) {
        player.setMetadata("regenHP", new FixedMetadataValue(DungeonRealms.getInstance(), regenAmount));
    }

    /**
     * Returns the players current HPRegen
     *
     * @param player
     * @return int
     * @since 1.0
     */
    public int getPlayerHPRegenLive(Player player) {
        if (player.hasMetadata("regenHP")) {
            return player.getMetadata("regenHP").get(0).asInt();
        } else {
            return calculateHealthRegenFromItems(player);
        }
    }
}
