	package net.dungeonrealms.handlers;

    import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.duel.DuelOffer;
import net.dungeonrealms.duel.DuelingMechanics;
import net.dungeonrealms.entities.Entities;
import net.dungeonrealms.mastery.GamePlayer;
import net.dungeonrealms.mechanics.SoundAPI;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.dungeonrealms.profession.Fishing;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.*;
    import org.bukkit.event.entity.EntityDeathEvent;
    import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.inventivetalent.bossbar.BossBarAPI;

import java.util.ArrayList;
import java.util.List;

    /**
 * Created by Kieran on 10/3/2015.
 */
public class HealthHandler implements GenericMechanic {

    private static HealthHandler instance = null;

    public static HealthHandler getInstance() {
        if (instance == null) {
            instance = new HealthHandler();
        }
        return instance;
    }
    public static List<Player> COMBAT_ARMORSWITCH = new ArrayList<>();

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    public void startInitialization() {
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::updatePlayerHPBars, 40, 6L);
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
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            setPlayerMaxHPLive(player, getPlayerMaxHPOnLogin(player));
            int hp = Integer.valueOf(String.valueOf(DatabaseAPI.getInstance().getData(EnumData.HEALTH, player.getUniqueId())));
            if (hp > 0) {
                if (hp > getPlayerMaxHPLive(player)) {
                    hp = getPlayerMaxHPLive(player);
                }
                setPlayerHPLive(player, hp);
                healPlayerByAmount(player, 5);
            } else {
                setPlayerHPLive(player, 10);
                healPlayerByAmount(player, 5);
            }
            setPlayerHPRegenLive(player, getPlayerHPRegenLive(player));
            player.setMetadata("last_death_time", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
        }, 50L);
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
        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.HEALTH, getPlayerHPLive(player), false);
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
        if (gamePlayer == null) {
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
           return calculateMaxHPFromItems(entity) - 80;
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
            return API.getGamePlayer(player).getPlayerMaxHP();
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

                if (gp == null || gp.getStats() == null) return;

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
                } else if ((currentHP + amountToHealPlayer) < maxHP) {
                    if(Fishing.fishBuffs.containsKey(player.getUniqueId()) && Fishing.fishBuffs.get(player.getUniqueId()).equalsIgnoreCase("HP Regen")){
                    	amountToHealPlayer += (maxHP * 0.10);
                    }

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
                return;
            }
            return;
        }
        if ((currentHP + (double) amount) >= maxHP) {
            player.setHealth(20);
            setPlayerHPLive(player, (int) maxHP);
            if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId()).toString())) {
                double newHealth = currentHP + amount;
                if (newHealth >= maxHP) {
                    newHealth = maxHP;
                }
                player.sendMessage(ChatColor.GREEN + "     +" + amount + ChatColor.BOLD + " HP" + ChatColor.AQUA + " -> " + ChatColor.GREEN + " [" + (int) newHealth + ChatColor.BOLD + "HP" + ChatColor.GREEN + "]");
            }
            return;
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

        if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId()).toString())) {
            double newHealth = currentHP + amount;
            if (newHealth >= maxHP) {
                newHealth = maxHP;
            }
            player.sendMessage(ChatColor.GREEN + "     +" + amount + ChatColor.BOLD + " HP" + ChatColor.AQUA + " -> " + ChatColor.GREEN + " [" + (int) newHealth + ChatColor.BOLD + "HP" + ChatColor.GREEN + "]");
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
        if (!API.isPlayer(player)) {
            return;
        }
        double maxHP = getPlayerMaxHPLive(player);
        double currentHP = getPlayerHPLive(player);
        double newHP = currentHP - damage;

        LivingEntity leAttacker = null;
        if (damager instanceof CraftLivingEntity) {
            leAttacker = (LivingEntity) damager;
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

        CombatLog.addToCombat(player);
        if (leAttacker instanceof Player) {
            player.playEffect(EntityEffect.HURT);
            SoundAPI.getInstance().playSoundAtLocation("damage.hit", player.getLocation(), 6);
        }
        if (newHP <= 0 && DuelingMechanics.isDueling(player.getUniqueId())) {
        	damage = 0;
            newHP = 1;
        	DuelOffer offer = DuelingMechanics.getOffer(player.getUniqueId());
            offer.endDuel((Player) leAttacker, player);
            return;
        }

        if (newHP <= 0 && API.isPlayer(leAttacker) && Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_CHAOTIC_PREVENTION, leAttacker.getUniqueId()).toString())) {
            if (KarmaHandler.getInstance().getPlayerRawAlignment(player).equalsIgnoreCase(KarmaHandler.EnumPlayerAlignments.LAWFUL.name())) {
                newHP = 1;
                leAttacker.sendMessage(ChatColor.YELLOW + "Your Chaotic Prevention Toggle has activated preventing the death of " + player.getName() + "!");
                player.sendMessage(ChatColor.YELLOW + leAttacker.getName() + " has their Chaotic Prevention Toggle ON, your life has been spared!");
            }
        }

        if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId()).toString())) {
            player.sendMessage(ChatColor.RED + "     -" + (int) damage + ChatColor.BOLD + " HP" + ChatColor.RED + " -> " + ChatColor.GREEN + " [" + (int) newHP + ChatColor.BOLD + "HP" + ChatColor.GREEN + "]");
        }

        if (API.isPlayer(leAttacker)) {
            if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, leAttacker.getUniqueId()).toString())) {
                leAttacker.sendMessage(ChatColor.RED + "     " + (int) damage + ChatColor.BOLD + " Damage" + ChatColor.RED + " -> " + ChatColor.DARK_PURPLE + player.getName() + "[" + (int) newHP + ChatColor.BOLD + "HP" + ChatColor.DARK_PURPLE + "]");
            }
        }

        if (leAttacker instanceof Player) {
            KarmaHandler.getInstance().handleAlignmentChanges((Player) leAttacker);
        }
        if (newHP <= 0) {
            player.playSound(player.getLocation(), Sound.WITHER_SPAWN, 1f, 1f);
            if (player.hasMetadata("last_death_time")) {
                if (player.getMetadata("last_death_time").get(0).asLong() > 100) {
                    player.setMetadata("last_death_time", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
                    player.damage(25);
                    //TODO: WATCH THIS
                    //player.setHealth(0);
                    KarmaHandler.getInstance().handlePlayerPsuedoDeath(player, leAttacker);
                    CombatLog.removeFromCombat(player);
                    API.getNearbyPlayers(player.getLocation(), 100).stream().forEach(player1 -> player1.sendMessage(player.getName() + " has died!"));
                    return;
                }
            } else {
                player.setMetadata("last_death_time", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
                player.damage(25);
                //TODO: WATCH THIS
                //player.setHealth(0);
                KarmaHandler.getInstance().handlePlayerPsuedoDeath(player, leAttacker);
                API.getNearbyPlayers(player.getLocation(), 100).stream().forEach(player1 -> player1.sendMessage(player.getName() + " has died!"));
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
        if (!(leAttacker == null) && !(API.isPlayer(leAttacker))) {
            Entities.MONSTER_LAST_ATTACK.put(leAttacker, 15);
            if (!Entities.MONSTERS_LEASHED.contains(leAttacker)) {
                Entities.MONSTERS_LEASHED.add(leAttacker);
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
        if (entity instanceof EntityArmorStand) return;
        
        if (API.isPlayer(attacker)) {
            if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, attacker.getUniqueId()).toString())) {
            	if(!entity.hasMetadata("uuid")){
                String customNameAppended = (entity.getMetadata("customname").get(0).asString());
                attacker.sendMessage(ChatColor.RED + "     " + (int) damage + ChatColor.BOLD + " Damage" + ChatColor.RED + " -> " + ChatColor.DARK_PURPLE + API.getTierColor(entity.getMetadata("tier").get(0).asInt()) + customNameAppended + ChatColor.DARK_PURPLE + ChatColor.BOLD + "[" + (int) newHP + "HP]");
            	}
            }
        }

        if (newHP <= 0) {
            //entity.playEffect(EntityEffect.DEATH);
            setMonsterHPLive(entity, 0);
            net.minecraft.server.v1_8_R3.Entity entity1 = ((CraftEntity) entity).getHandle();
            entity.damage(entity.getHealth());
            //entity1.damageEntity(DamageSource.GENERIC, 50F);
            Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
                if (!entity.isDead()) {
                    entity.setMaximumNoDamageTicks(200);
                    entity.setNoDamageTicks(10);
                    EntityDeathEvent event = new EntityDeathEvent(entity,  new ArrayList<>());
                    Bukkit.getPluginManager().callEvent(event);
                    entity.setHealth(0);
                }
            }, 1L);
            /*if (!entity1.dead) {
                entity1.dead = true;
            }*/
            if (Entities.MONSTER_LAST_ATTACK.containsKey(entity)) {
                Entities.MONSTER_LAST_ATTACK.remove(entity);
            }
            if (Entities.MONSTERS_LEASHED.contains(entity)) {
                Entities.MONSTERS_LEASHED.remove(entity);
            }
            if (entity.hasMetadata("type") && entity.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile") && !entity.hasMetadata("uuid")) {
                ((net.dungeonrealms.entities.types.monsters.Monster) entity1).onMonsterDeath();
                if (attacker instanceof Player) {
                    int exp = API.getMonsterExp((Player) attacker, entity);
                    API.getGamePlayer((Player) attacker).addExperience(exp);
                }
            }
            return;
        }

        if (entity != null) {
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
            if (entity.hasMetadata("type") && entity.hasMetadata("level")) {
                int level = entity.getMetadata("level").get(0).asInt();
                String lvlName = ChatColor.LIGHT_PURPLE + "[" + level + "] ";
                String name = "";
                if(!entity.hasMetadata("elite") && !entity.hasMetadata("boss") && !entity.hasMetadata("uuid"))
                name = entity.getMetadata("customname").get(0).asString();
                else name = entity.getCustomName();
                int hp = entity.getMetadata("currentHP").get(0).asInt();
                if (!entity.hasMetadata("elite") && !entity.hasMetadata("boss") && !entity.hasMetadata("uuid"))
                    entity.setCustomName(lvlName + ChatColor.RESET + name + ChatColor.RED.toString() + "❤ " + ChatColor.RESET + hp);
                entity.setHealth(convHPToDisplay);
                if (!Entities.MONSTERS_LEASHED.contains(entity)) {
                    Entities.MONSTERS_LEASHED.add(entity);
                }
            }
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
        
        if(entity.hasMetadata("elite"))
        	totalHP *= 5;
        
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
            return (int) hpTotal;
        }
        if (!(nmsItem.getTag().getString("type").equalsIgnoreCase("armor"))) {
            return (int) hpTotal;
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
    public int calculateHealthRegenFromItems(Player player) {
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
            return (int) totalRegen;
        }
        if (!(nmsItem.getTag().getString("type").equalsIgnoreCase("armor"))) {
            return (int) totalRegen;
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

    public void recalculateHPAfterCombat(Player player) {
        if (!COMBAT_ARMORSWITCH.contains(player)) {
            return;
        }
        setPlayerMaxHPLive(player, calculateMaxHPFromItems(player));
        if (getPlayerHPLive(player) > getPlayerMaxHPLive(player)) {
            setPlayerHPLive(player, getPlayerMaxHPLive(player));
        }
        COMBAT_ARMORSWITCH.remove(player);
    }
}
