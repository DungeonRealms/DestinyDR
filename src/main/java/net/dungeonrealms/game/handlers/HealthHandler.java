package net.dungeonrealms.game.handlers;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.chat.GameChat;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.duel.DuelOffer;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.player.rank.Rank;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.world.entities.Entities;
import net.dungeonrealms.game.world.entities.types.monsters.DRMonster;
import net.dungeonrealms.game.world.party.Affair;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_9_R2.EntityArmorStand;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
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
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            for(Player pl : Bukkit.getServer().getOnlinePlayers()) {
                setPlayerOverheadHP(pl, getPlayerHPLive(pl));
            }
        }, 0L, 5L);
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
            if (Rank.isGM(player)) {
                setPlayerHPLive(player, 10000);
                healPlayerByAmount(player, 20);
            } else if (hp > 0) {
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
        }, 40L);
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

    //private void updatePlayerHPBars() {
    //    Bukkit.getOnlinePlayers().stream().filter(player -> getPlayerHPLive(player) > 0).forEach(player -> setPlayerOverheadHP(player, getPlayerHPLive(player)));
    //}

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
    private void setPlayerOverheadHP(Player player, int hp) {
        GamePlayer gamePlayer = API.getGamePlayer(player);
        if (gamePlayer == null) {
            return;
        }
        ScoreboardHandler.getInstance().updatePlayerHP(player, hp);
        double maxHP = getPlayerMaxHPLive(player);
        double healthPercentage = ((double) hp / maxHP);
        if (healthPercentage * 100.0F > 100.0F) {
            healthPercentage = 1.0;
        }
        float healthToDisplay = (float) (healthPercentage * 100.F);
        int playerLevel = gamePlayer.getLevel();
        String playerLevelInfo = ChatColor.AQUA.toString() + ChatColor.BOLD + "LVL " + ChatColor.AQUA + playerLevel;
        String separator = ChatColor.WHITE.toString() + " - ";
        String playerHPInfo;
        BossBarAPI.Color color;
        if (API.isInSafeRegion(player.getLocation())) {
            color = BossBarAPI.Color.GREEN;
            playerHPInfo = ChatColor.GREEN.toString() + ChatColor.BOLD + "HP " + ChatColor.GREEN + hp + ChatColor.BOLD + " / " + ChatColor.GREEN + (int) maxHP;
        } else if (API.isNonPvPRegion(player.getLocation())) {
            color = BossBarAPI.Color.YELLOW;
            playerHPInfo = ChatColor.YELLOW.toString() + ChatColor.BOLD + "HP " + ChatColor.YELLOW + hp + ChatColor.BOLD + " / " + ChatColor.YELLOW + (int) maxHP;
        } else {
            color = BossBarAPI.Color.RED;
            playerHPInfo = ChatColor.RED.toString() + ChatColor.BOLD + "HP " + ChatColor.RED + hp + ChatColor.BOLD + " / " + ChatColor.RED + (int) maxHP;
        }
        String playerEXPInfo = ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "XP " + ChatColor.LIGHT_PURPLE + Math.round((gamePlayer.getExperience() / gamePlayer.getEXPNeeded(playerLevel)) * 100.0) + "%";
        BossBarAPI.removeAllBars(player);
        BossBarAPI.addBar(player, new TextComponent("    " + playerLevelInfo + separator + playerHPInfo + separator + playerEXPInfo), color, BossBarAPI.Style.NOTCHED_20, healthToDisplay);
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
           return calculateMaxHPFromItems(entity);
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

                amountToHealPlayer += gp.getStats().getHPRegen();

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
            if (entity.getHealth() != entity.getMaxHealth()) {
                entity.setHealth(entity.getMaxHealth());
            }
        }

        if ((currentHP + (double) amount) >= maxHP) {
            entity.setHealth(entity.getMaxHealth());
            setMonsterHPLive(entity, (int) maxHP);
        } else if (entity.getHealth() <= (entity.getMaxHealth() - 1) && ((currentHP + (double) amount) < maxHP)) {
            setMonsterHPLive(entity, (int) (getMonsterHPLive(entity) + (double) amount));
            double monsterHPPercent = (getMonsterHPLive(entity) + (double) amount) / maxHP;
            double newMonsterHP = monsterHPPercent * entity.getMaxHealth();
            if (newMonsterHP >= (entity.getMaxHealth() - 0.5D)) {
                if (monsterHPPercent >= 1.0D) {
                    newMonsterHP = entity.getMaxHealth();
                } else {
                    newMonsterHP = entity.getMaxHealth();
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
    public void handlePlayerBeingDamaged(Player player, Entity damager, double damage, double armourReducedDamage, double totalArmor) {
        if (!API.isPlayer(player)) {
            return;
        }
        double maxHP = getPlayerMaxHPLive(player);
        double currentHP = getPlayerHPLive(player);
        double newHP = currentHP - damage;

        LivingEntity leAttacker = null;
        if (damager != null) {
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
        }

        if (damager instanceof Player) {
            leAttacker = (LivingEntity) damager;
        }
        if (leAttacker != null) {
            CombatLog.addToCombat(player);
        }

        if (newHP <= 0 && DuelingMechanics.isDueling(player.getUniqueId())) {
            DuelOffer offer = DuelingMechanics.getOffer(player.getUniqueId());
            offer.endDuel((Player) leAttacker, player);
            return;
        }

        if (leAttacker instanceof Player) {
            if (!DuelingMechanics.isDuelPartner(player.getUniqueId(), leAttacker.getUniqueId())) {
                KarmaHandler.getInstance().handleAlignmentChanges((Player) leAttacker);
                if (newHP <= 0 && API.isPlayer(leAttacker) && Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_CHAOTIC_PREVENTION, leAttacker.getUniqueId()).toString())) {
                    if (KarmaHandler.getInstance().getPlayerRawAlignment(player).equalsIgnoreCase(KarmaHandler.EnumPlayerAlignments.LAWFUL.name())) {
                        newHP = 1;
                        leAttacker.sendMessage(ChatColor.YELLOW + "Your Chaotic Prevention Toggle has activated preventing the death of " + player.getName() + "!");
                        player.sendMessage(ChatColor.YELLOW + leAttacker.getName() + " has their Chaotic Prevention Toggle ON, your life has been spared!");
                    }
                }
            }
            if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, leAttacker.getUniqueId()).toString())) {
                leAttacker.sendMessage(ChatColor.RED + "     " + (int) damage + ChatColor.BOLD + " DMG" + ChatColor.RED + " -> " + ChatColor.DARK_PURPLE + player.getName() + ChatColor.RED + " [" + (int) newHP + ChatColor.BOLD + "HP" + ChatColor.DARK_PURPLE + "]");
            }
            player.playEffect(EntityEffect.HURT);
            player.playSound(player.getLocation(), Sound.ENCHANT_THORNS_HIT, 1F, 1F);
        }

        if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId()).toString())) {
            player.sendMessage(ChatColor.RED + "     -" + (int) damage + ChatColor.BOLD + " HP" + ChatColor.GRAY + " [-"
                    + (int) totalArmor + "%A -> -" + (int) armourReducedDamage + ChatColor.BOLD + "DMG" + ChatColor.GRAY
                    + "]" + ChatColor.GREEN + " [" + (int) newHP + ChatColor.BOLD + "HP" + ChatColor.GREEN + "]");
        }

        if (newHP <= 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 1f);
            if (player.hasMetadata("last_death_time")) {
                if (player.getMetadata("last_death_time").get(0).asLong() > 100) {
                    String killerName = "";
                    if (leAttacker instanceof Player) {
                        killerName = GameChat.getPreMessage((Player) leAttacker).replaceAll(":", "").trim();
                        if (ChatColor.stripColor(killerName).startsWith("<G>")) {
                            killerName = killerName.split(">")[1];
                        }

                        if (Achievements.getInstance().hasAchievement(player.getUniqueId(), Achievements.EnumAchievements.INFECTED)) {
                            Player killer = (Player) leAttacker;
                            Achievements.getInstance().giveAchievement(killer.getUniqueId(), Achievements.EnumAchievements.INFECTED);
                        }
                    } else {
                        if (leAttacker != null) {
                            if (leAttacker.hasMetadata("customname")) {
                                killerName = leAttacker.getMetadata("customname").get(0).asString().trim();
                            }
                        } else {
                            killerName = "The World";
                        }
                    }
                    String deadPlayerName = GameChat.getPreMessage(player).replaceAll(":", "").trim();
                    if (ChatColor.stripColor(deadPlayerName).startsWith("<G>")) {
                        deadPlayerName = deadPlayerName.split(">")[1];
                    }
                    final String finalDeadPlayerName = deadPlayerName;
                    final String finalKillerName = killerName;
                    API.getNearbyPlayers(player.getLocation(), 100).stream().forEach(player1 -> player1.sendMessage(finalDeadPlayerName  + " was killed by a(n) " + finalKillerName));
                    final LivingEntity finalLeAttacker = leAttacker;
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                        player.setMetadata("last_death_time", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
                        player.damage(player.getMaxHealth());
                        if (finalLeAttacker != null) {
                            KarmaHandler.getInstance().handlePlayerPsuedoDeath(player, finalLeAttacker);
                        }
                        CombatLog.removeFromCombat(player);
                    }, 20L);
                    return;
                }
            } else {
                String killerName = "";
                if (leAttacker instanceof Player) {
                    killerName = leAttacker.getName();
                } else {
                    if (leAttacker != null) {
                        if (leAttacker.hasMetadata("customname")) {
                            killerName = leAttacker.getMetadata("customname").get(0).asString().trim();
                        }
                    } else {
                        killerName = "The World";
                    }
                }
                final String finalKillerName = killerName;
                API.getNearbyPlayers(player.getLocation(), 100).stream().forEach(player1 -> player1.sendMessage((GameChat.getPreMessage(player).trim().replace(":", "") + " was killed by a(n) " + finalKillerName)));
                final LivingEntity finalLeAttacker = leAttacker;
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    player.setMetadata("last_death_time", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
                    player.damage(25);
                    if (finalLeAttacker != null) {
                        KarmaHandler.getInstance().handlePlayerPsuedoDeath(player, finalLeAttacker);
                    }
                    CombatLog.removeFromCombat(player);
                }, 20L);
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
        if (currentHP <= 0) {
            if (!entity.isDead()) {
                entity.setHealth(0);
            }
            return;
        }

        if (attacker != null) {
            if (API.isPlayer(attacker)) {
                if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, attacker.getUniqueId()).toString())) {
                    if (!entity.hasMetadata("uuid")) {
                        ChatColor npcTierColor = API.getTierColor(entity.getMetadata("tier").get(0).asInt());
                        attacker.sendMessage(ChatColor.RED + "     " + (int) damage + ChatColor.BOLD + " DMG" + ChatColor.RED + " -> " + ChatColor.GRAY + npcTierColor + customNameAppended + npcTierColor + " [" + (int) newHP + "HP]");
                    }
                }
            }
        }

        if (newHP <= 0) {
            entity.playEffect(EntityEffect.DEATH);
            setMonsterHPLive(entity, 0);
            net.minecraft.server.v1_9_R2.Entity entity1 = ((CraftEntity) entity).getHandle();
            entity.damage(entity.getHealth());
            entity.setMaximumNoDamageTicks(2000);
            entity.setNoDamageTicks(1000);
            Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
                if (!entity.isDead()) {
                    entity.setMaximumNoDamageTicks(200);
                    entity.setNoDamageTicks(10);
                    EntityDeathEvent event = new EntityDeathEvent(entity,  new ArrayList<>());
                    Bukkit.getPluginManager().callEvent(event);
                    entity.setHealth(0);
                    entity.remove();
                }
                if (!entity1.dead) {
                    entity1.dead = true;
                }
            }, 5L);
            if (Entities.MONSTER_LAST_ATTACK.containsKey(entity)) {
                Entities.MONSTER_LAST_ATTACK.remove(entity);
            }
            if (Entities.MONSTERS_LEASHED.contains(entity)) {
                Entities.MONSTERS_LEASHED.remove(entity);
            }
            if (entity.hasMetadata("type") && entity.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile") && !entity.hasMetadata("uuid") && !entity.hasMetadata("boss")) {
                if (attacker != null) {
                    if (attacker instanceof Player) {
                        ((DRMonster) entity1).onMonsterDeath((Player) attacker);
                        int exp = API.getMonsterExp((Player) attacker, entity);
                        if (API.getGamePlayer((Player) attacker) == null) {
                            return;
                        }
                        if (Affair.getInstance().isInParty((Player) attacker)) {
                            List<Player> nearbyPlayers = API.getNearbyPlayers(attacker.getLocation(), 10);
                            List<Player> nearbyPartyMembers = new ArrayList<>();
                            if (!nearbyPlayers.isEmpty()) {
                                for (Player player : nearbyPlayers) {
                                    if (player.equals(attacker)) {
                                        continue;
                                    }
                                    if (!API.isPlayer(attacker)) {
                                        continue;
                                    }
                                    if (Affair.getInstance().areInSameParty((Player) attacker, player)) {
                                        nearbyPartyMembers.add(player);
                                    }
                                }
                                if (nearbyPartyMembers.size() > 0) {
                                    nearbyPartyMembers.add((Player) attacker);
                                    switch (nearbyPartyMembers.size()) {
                                        case 1:
                                            break;
                                        case 2:
                                            break;
                                        case 3:
                                            exp *= 1.2;
                                            break;
                                        case 4:
                                            exp *= 1.3;
                                            break;
                                        case 5:
                                            exp *= 1.4;
                                            break;
                                        case 6:
                                            exp *= 1.5;
                                            break;
                                        case 7:
                                            exp *= 1.6;
                                            break;
                                        case 8:
                                            exp *= 1.7;
                                            break;
                                        default:
                                            break;
                                    }
                                    exp /= nearbyPartyMembers.size();
                                    for (Player player : nearbyPartyMembers) {
                                        API.getGamePlayer(player).addExperience(exp, true);
                                    }
                                } else {
                                    API.getGamePlayer((Player) attacker).addExperience(exp, false);
                                }
                            } else {
                                API.getGamePlayer((Player) attacker).addExperience(exp, false);
                            }
                        } else {
                            API.getGamePlayer((Player) attacker).addExperience(exp, false);
                        }
                    }
                }
            }
            return;
        }

        if (attacker != null) {
            if (entity != null) {
                setMonsterHPLive(entity, (int) newHP);
                double monsterHPPercent = (newHP / maxHP);
                double newMonsterHPToDisplay = monsterHPPercent * entity.getMaxHealth();
                int convHPToDisplay = (int) newMonsterHPToDisplay;
                if (convHPToDisplay <= 1) {
                    convHPToDisplay = 1;
                }
                if (convHPToDisplay > (int) entity.getMaxHealth()) {
                    convHPToDisplay = (int) entity.getMaxHealth();
                }
                if (entity.hasMetadata("type") && entity.hasMetadata("level") && entity.hasMetadata("tier")) {
                    int tier = entity.getMetadata("tier").get(0).asInt();
                    boolean elite = entity.hasMetadata("elite");
                    entity.setCustomName(Entities.getInstance().generateOverheadBar(entity, newHP, maxHP, tier, elite));
                    entity.setCustomNameVisible(true);
                    entity.setHealth(convHPToDisplay);
                    if (!Entities.MONSTERS_LEASHED.contains(entity)) {
                        Entities.MONSTERS_LEASHED.add(entity);
                    }
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
        
        if (entity.hasMetadata("elite")) {
            totalHP *= 3;
        }

        if (entity.hasMetadata("boss")) {
            totalHP *= 4;
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
    public int getHealthValueOfArmor(ItemStack itemStack) {
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = (CraftItemStack.asNMSCopy(itemStack));
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

    public int getVitalityValueOfArmor(ItemStack itemStack, double hpTotal) {
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = (CraftItemStack.asNMSCopy(itemStack));
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
    public int getHealthRegenValueOfArmor(ItemStack itemStack) {
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = (CraftItemStack.asNMSCopy(itemStack));
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

    public int getHealthRegenVitalityFromArmor(ItemStack itemStack, double totalRegen) {
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = (CraftItemStack.asNMSCopy(itemStack));
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
        setPlayerHPRegenLive(player, calculateHealthRegenFromItems(player));
        COMBAT_ARMORSWITCH.remove(player);
    }
}
