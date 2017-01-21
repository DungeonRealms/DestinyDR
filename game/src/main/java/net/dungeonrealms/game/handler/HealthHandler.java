package net.dungeonrealms.game.handler;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.mastery.DamageTracker;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.player.chat.GameChat;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.duel.DuelOffer;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.item.Item;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_9_R2.EntityArmorStand;
import net.minecraft.server.v1_9_R2.EntityInsentient;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.inventivetalent.bossbar.BossBarAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Kieran on 10/3/2015.
 */
public class HealthHandler implements GenericMechanic {

    @Getter
    private HashMap<UUID, DamageTracker> monsterTrackers = new HashMap<>();

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
        /** Whoever thought this was a great idea is autistic and should be permanently banned from being able to use an IDE
         Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
         Bukkit.getServer().getOnlinePlayers().stream().forEach(pl -> setPlayerOverheadHP(pl, getPlayerHPLive(pl)));
         }, 0L, 20L);
         */
        Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            Bukkit.getServer().getOnlinePlayers().stream().forEach(pl -> setPlayerOverheadHP(pl, getPlayerHPLive(pl)));
        }, 0L, 20L);
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::regenerateHealth, 40L, 20L);

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
        player.setMetadata("loggingIn", new FixedMetadataValue(DungeonRealms.getInstance(), "yes"));
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            setPlayerMaxHPLive(player, GameAPI.getStaticAttributeVal(Item.ArmorAttributeType.HEALTH_POINTS, player) + 50);
            int hp = Integer.valueOf(String.valueOf(DatabaseAPI.getInstance().getData(EnumData.HEALTH, player.getUniqueId())));
            if (Rank.isGM(player)) {
                setPlayerHPLive(player, 10000);
            } else if (hp > 0) {
                if (hp > getPlayerMaxHPLive(player)) {
                    hp = getPlayerMaxHPLive(player);
                }
                setPlayerHPLive(player, hp);
            } else {
                setPlayerHPLive(player, 10);
            }
            setPlayerHPRegenLive(player, getPlayerHPRegenLive(player));
            player.setMetadata("last_death_time", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
            player.removeMetadata("loggingIn", DungeonRealms.getInstance());
        }, 20); // 1 sec
    }

    /**
     * Handles players logging out,
     * removes potion effects and
     * updates database for web usage.
     *
     * @param player
     * @since 1.0
     */
    public void handleLogoutEvents(Player player) {
        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.HEALTH, getPlayerHPLive(player), true);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                player.removePotionEffect(potionEffect.getType());
            }
        });
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
        GamePlayer gamePlayer = GameAPI.getGamePlayer(player);
        if (gamePlayer == null || !gamePlayer.isAttributesLoaded()) {
            return;
        }
        double maxHP = getPlayerMaxHPLive(player);
        double healthPercentage = ((double) hp / maxHP);
        if (healthPercentage * 100.0F > 100.0F) {
            healthPercentage = 1.0;
        }
        float healthToDisplay = (float) (healthPercentage * 100.F);
        int playerLevel = gamePlayer.getLevel();
        String playerLevelInfo = ChatColor.AQUA.toString() + ChatColor.BOLD + "LV. " + ChatColor.AQUA + playerLevel;
        String separator = ChatColor.WHITE.toString() + " - ";
        String playerHPInfo;
        BossBarAPI.Color color;
        if (GameAPI.isInSafeRegion(player.getLocation())) {
            color = BossBarAPI.Color.GREEN;
            playerHPInfo = hp != maxHP ? ChatColor.GREEN.toString() + ChatColor.BOLD + "HP " + ChatColor.GREEN + hp + ChatColor.BOLD + " / " + ChatColor.GREEN + (int) maxHP : ChatColor.GREEN.toString() + ChatColor.BOLD + "HP " + ChatColor.GREEN + (int) maxHP;
        } else if (GameAPI.isNonPvPRegion(player.getLocation())) {
            color = BossBarAPI.Color.YELLOW;
            playerHPInfo = hp != maxHP ? ChatColor.YELLOW.toString() + ChatColor.BOLD + "HP " + ChatColor.YELLOW + hp + ChatColor.BOLD + " / " + ChatColor.YELLOW + (int) maxHP : ChatColor.YELLOW.toString() + ChatColor.BOLD + "HP " + ChatColor.YELLOW + (int) maxHP;
        } else {
            color = BossBarAPI.Color.RED;
            playerHPInfo = hp != maxHP ? ChatColor.RED.toString() + ChatColor.BOLD + "HP " + ChatColor.RED + hp + ChatColor.BOLD + " / " + ChatColor.RED + (int) maxHP : ChatColor.RED.toString() + ChatColor.BOLD + "HP " + ChatColor.RED + (int) maxHP;
        }
        double exp = ((double) gamePlayer.getExperience()) / ((double) gamePlayer.getEXPNeeded(playerLevel));
        exp *= 100;
        String playerEXPInfo = ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "XP " + ChatColor.LIGHT_PURPLE + (int) exp + "%";
        if (playerLevel == 100) {
            playerEXPInfo = ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "MAX";
        }
        BossBarAPI.removeAllBars(player);
        BossBarAPI.addBar(player, new TextComponent("    " + playerLevelInfo + separator + playerHPInfo + separator + playerEXPInfo), color, getStyle(maxHP), healthToDisplay);
        // Do this sync
        DungeonRealms.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> ScoreboardHandler.getInstance().updatePlayerHP(player, hp));
    }

    private BossBarAPI.Style getStyle(double maxHealth) {

        if (maxHealth >= 5000) {
            return BossBarAPI.Style.NOTCHED_20;
        } else if (maxHealth >= 3000) {
            return BossBarAPI.Style.NOTCHED_12;
        } else if (maxHealth >= 1000) {
            return BossBarAPI.Style.NOTCHED_10;
        } else if (maxHealth >= 500) {
            return BossBarAPI.Style.NOTCHED_6;
        }
        return BossBarAPI.Style.PROGRESS;
    }


    /**
     * Sets the players HP metadata
     * to the given value.
     *
     * @param player
     * @param hp
     * @since 1.0
     */
    /*
     && !Rank.isGM(player) MOVE TO ITS OWN METHOD. No need to call for every player and waste resources so GMs can set their HP easier.
     */
    public void setPlayerHPLive(Player player, int hp) {
        if (player.hasMetadata("maxHP") && hp > player.getMetadata("maxHP").get(0).asInt()) {
            player.setMetadata("currentHP", new FixedMetadataValue(DungeonRealms.getInstance(), player.getMetadata("maxHP").get(0).asInt()));
            return;
        }
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
            player.setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), calculateMaxHPFromItems(player)));
            return this.calculateMaxHPFromItems(player);
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
            if (CombatLog.isInCombat(player)) {
                continue;
            }
            if (getPlayerHPLive(player) <= 0 && player.getHealth() <= 0) {
                continue;
            }
            if (player.hasMetadata("starving")) {
                continue;
            }
            if (GameAPI.getGamePlayer(player) == null || !GameAPI.getGamePlayer(player).isAttributesLoaded()) {
                continue;
            }
            double currentHP = getPlayerHPLive(player);
            double amountToHealPlayer = getPlayerHPRegenLive(player);
            double maxHP = getPlayerMaxHPLive(player);

            if (currentHP >= maxHP) {
                if (player.getHealth() != 20) {
                    player.setHealth(20);
                }
                continue;
            }

            if ((currentHP + amountToHealPlayer) >= maxHP) {
                player.setHealth(20);
                setPlayerHPLive(player, (int) maxHP);
            } else if ((currentHP + amountToHealPlayer) < maxHP) {
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
                player.sendMessage(ChatColor.GREEN + "        +" + amount + ChatColor.BOLD + " HP" + ChatColor.GRAY + " [" + (int) newHealth + "/" + (int) maxHP + "HP]");
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
            player.sendMessage(ChatColor.GREEN + "        +" + amount + ChatColor.BOLD + " HP" + ChatColor.GRAY + " [" + (int) newHealth + "/" + (int) maxHP + "HP]");
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
            } else if (newMonsterHP < 1) {
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
        // default damage cause is entity attack (called in onMonsterHitEntity and onPlayerHitEntity)
        handlePlayerBeingDamaged(player, damager, damage, armourReducedDamage, totalArmor, EntityDamageEvent.DamageCause.ENTITY_ATTACK);
    }

    public void handlePlayerBeingDamaged(Player player, Entity damager, double damage, double armourReducedDamage, double totalArmor, EntityDamageEvent.DamageCause cause) {
        final GamePlayer gp = GameAPI.getGamePlayer(player);
        if (player.getGameMode().equals(GameMode.SPECTATOR) || player.getGameMode().equals(GameMode.CREATIVE) || gp
                == null || gp.isInvulnerable())
            return;

        double maxHP = getPlayerMaxHPLive(player);
        double currentHP = getPlayerHPLive(player);
        double newHP = currentHP - damage;

        if (!(damager instanceof Player)) {
            // Player is damaged by a creature
            if (CombatLog.isInCombat(player)) {
                CombatLog.updateCombat(player);
            } else {
                CombatLog.addToCombat(player);
            }
        } else {
            // Player is pvping
            if (CombatLog.inPVP(player)) {
                CombatLog.updatePVP(player);
            } else {
                CombatLog.addToPVP(player);
            }
        }

        LivingEntity leAttacker = null;
        if (damager != null) {
            if (damager instanceof Player) {
                leAttacker = (LivingEntity) damager;

                // Temporary Hack
                if (damage > 3000) {
                    // Destroy the item from the game.
                    Bukkit.getPlayer(leAttacker.getName()).getInventory().setItemInMainHand(new ItemStack(Material.AIR));

                    // Prompt the user that this weapon is not allowed.
                    leAttacker.sendMessage(ChatColor.YELLOW + "The weapon you posses is not of this world and has been returned to the Gods.");

                    // User isn't a GM, let's alert the GMs about this violation.
                    if (!Rank.isGM((Player) leAttacker)) {
                        GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED.toString() + "[ANTI CHEAT] " +
                                ChatColor.WHITE + "Destroyed illegal weapon (" + damage + " ) from " + leAttacker.getName() + " on shard " + ChatColor.GOLD + ChatColor.UNDERLINE + DungeonRealms.getInstance().shardid);
                    }
                    return;
                }

            } else if (damager instanceof CraftLivingEntity) {
                leAttacker = (LivingEntity) damager;
            } else if (damager instanceof Projectile) {
                leAttacker = (LivingEntity) ((Projectile) damager).getShooter();
            }
        }

        if (leAttacker instanceof Player) {
            if (newHP <= 0 && DuelingMechanics.isDueling(player.getUniqueId())) {
                DuelOffer offer = DuelingMechanics.getOffer(player.getUniqueId());
                if (offer != null) {
                    GamePlayer attackPLayer = GameAPI.getGamePlayer((Player)leAttacker);

                    player.setMetadata("duel_cooldown", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis() + 1000));
                    leAttacker.setMetadata("duel_cooldown", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis() + 1000));
                    offer.endDuel((Player) leAttacker, player);
                }
                return;
            }
            if (!DuelingMechanics.isDuelPartner(player.getUniqueId(), leAttacker.getUniqueId())) {
                if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                    KarmaHandler.getInstance().handleAlignmentChanges((Player) leAttacker);
                }
                if (newHP <= 0 && GameAPI.isPlayer(leAttacker) && Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_CHAOTIC_PREVENTION, leAttacker.getUniqueId()).toString())) {
                    if (KarmaHandler.getInstance().getPlayerRawAlignment(player) == KarmaHandler.EnumPlayerAlignments.LAWFUL) {
                        player.setFireTicks(0);
                        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                            player.removePotionEffect(potionEffect.getType());
                        }
                        newHP = 1;
                        leAttacker.sendMessage(ChatColor.YELLOW + "Your Chaotic Prevention Toggle has activated preventing the death of " + player.getName() + "!");
                        player.sendMessage(ChatColor.YELLOW + leAttacker.getName() + " has their Chaotic Prevention Toggle ON, your life has been spared!");
                    }
                }
            }
            if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, leAttacker.getUniqueId()).toString())) {
                leAttacker.sendMessage(ChatColor.RED + "     " + (int) damage + ChatColor.BOLD + " DMG" + ChatColor.RED + " -> " + ChatColor.RED + player.getName() + ChatColor.RED + " [" + (int) newHP + ChatColor.BOLD + "HP" + "]");
            }
            player.playSound(player.getLocation(), Sound.ENCHANT_THORNS_HIT, 1F, 1F);
        }

        //player.getWorld().playEffect(player.getLocation().clone().add(0, 1, 0), Effect.STEP_SOUND, 152);

        if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId()).toString())) {
            if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                player.sendMessage(ChatColor.RED + "     -" + (int) damage + ChatColor.BOLD + " HP" + ChatColor.GRAY + " [-"
                        + (int) totalArmor + "%A -> -" + (int) armourReducedDamage + ChatColor.BOLD + "DMG" +
                        ChatColor.GRAY
                        + "]" + ChatColor.GREEN + " [" + (int) newHP + ChatColor.BOLD + "HP" + ChatColor.GREEN + "]");
            } else { // foreign damage
                ChatColor causeColor;
                String damageCauseName;
                switch (cause) {
                    case FALL:
                        causeColor = ChatColor.GRAY;
                        damageCauseName = "(FALL)";
                        break;
                    case THORNS:
                        causeColor = ChatColor.GREEN;
                        damageCauseName = "(THORNS)";
                        break;
                    case CUSTOM: // reflect
                        causeColor = ChatColor.GOLD;
                        damageCauseName = "(REFLECT)";
                        break;
                    case SUFFOCATION:
                        causeColor = ChatColor.BLACK;
                        damageCauseName = "(SUFFOCATION)";
                        break;
                    case DROWNING:
                        causeColor = ChatColor.DARK_BLUE;
                        damageCauseName = "(DROWNING)";
                        break;
                    case FIRE:
                        causeColor = ChatColor.DARK_RED;
                        damageCauseName = "(IN FIRE)";
                        break;
                    case FIRE_TICK:
                        causeColor = ChatColor.RED;
                        damageCauseName = "(ON FIRE)";
                        break;
                    case POISON:
                        causeColor = ChatColor.DARK_GREEN;
                        damageCauseName = "(POISON)";
                        break;
                    case LAVA:
                        causeColor = ChatColor.RED;
                        damageCauseName = "(LAVA)";
                        break;
                    case CONTACT:
                        causeColor = ChatColor.GREEN;
                        damageCauseName = "(CACTUS)";
                        break;
                    default: // it should never get here
                        causeColor = ChatColor.GRAY;
                        damageCauseName = "(CUSTOM)";
                        break;
                }
                player.sendMessage(ChatColor.RED + "     -" + (int) damage + ChatColor.BOLD + " HP " + causeColor + ChatColor.BOLD.toString() + damageCauseName + ChatColor.GREEN + " [" + (int) newHP + ChatColor.BOLD + "HP" + ChatColor.GREEN + "]");
            }
        }

        if (newHP <= 0) {
            if (handlePlayerDeath(player, leAttacker)) return;
        }

        setPlayerHPLive(player, (int) newHP);
        double playerHPPercent = (newHP / maxHP);
        double newPlayerHPToDisplay = playerHPPercent * 20.0D;
        int convHPToDisplay = (int) newPlayerHPToDisplay;
        if (convHPToDisplay <= 0) {
            convHPToDisplay = 1;
        } else if (convHPToDisplay > 20) {
            convHPToDisplay = 20;
        }
        player.setHealth(convHPToDisplay);
        if (leAttacker != null && leAttacker.getType() != EntityType.PLAYER) {
            EntityMechanics.MONSTER_LAST_ATTACK.put(leAttacker, 15);
            if (!EntityMechanics.MONSTERS_LEASHED.contains(leAttacker)) {
                EntityMechanics.MONSTERS_LEASHED.add(leAttacker);
            }
        }
    }

    public boolean handlePlayerDeath(Player player, LivingEntity leAttacker) {
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 1f);
        if (player.hasMetadata("last_death_time")) {
            if (System.currentTimeMillis() - player.getMetadata("last_death_time").get(0).asLong() > 100) {
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
                GameAPI.getNearbyPlayers(player.getLocation(), 100).forEach(player1 -> player1.sendMessage(finalDeadPlayerName + " was killed by a(n) " + finalKillerName));
                final LivingEntity finalLeAttacker = leAttacker;
                GameAPI.getGamePlayer(player).setPvpTaggedUntil(0);
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    player.setMetadata("last_death_time", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
                    player.damage(player.getMaxHealth());
                    if (finalLeAttacker != null) {
                        KarmaHandler.getInstance().handlePlayerPsuedoDeath(player, finalLeAttacker);
                    }
                    GameAPI.calculateAllAttributes(player);
                    CombatLog.removeFromCombat(player);
                    if (CombatLog.inPVP(player)) {
                        CombatLog.removeFromPVP(player);
                    }
                }, 1L);
                return true;
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
            GameAPI.getNearbyPlayers(player.getLocation(), 100).forEach(player1 -> player1.sendMessage((GameChat.getPreMessage(player).trim().replace(":", "") + " was killed by a(n) " + finalKillerName)));
            final LivingEntity finalLeAttacker = leAttacker;
            GameAPI.getGamePlayer(player).setPvpTaggedUntil(0);
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                player.setMetadata("last_death_time", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
                player.damage(player.getMaxHealth());
                if (finalLeAttacker != null) {
                    KarmaHandler.getInstance().handlePlayerPsuedoDeath(player, finalLeAttacker);
                }
                GameAPI.calculateAllAttributes(player);
                CombatLog.removeFromCombat(player);
                if (CombatLog.inPVP(player)) {
                    CombatLog.removeFromPVP(player);
                }
            }, 1L);
            return true;
        }
        return false;
    }

    private void handleMonsterDamageTracker(UUID monster, Player attacker, double damage) {
        if (monsterTrackers.containsKey(monster)) {
            monsterTrackers.get(monster).addPlayerDamage(attacker, damage);
        } else {
            DamageTracker damageTracker = new DamageTracker(monster);
            damageTracker.addPlayerDamage(attacker, damage);
            monsterTrackers.put(monster, damageTracker);
        }
    }

    private void checkForNewTarget(LivingEntity monster) {
        if (!(monster instanceof Creature)) return;
        if (monsterTrackers.containsKey(monster.getUniqueId())) {
            DamageTracker tracker = monsterTrackers.get(monster.getUniqueId());
            if (tracker == null) return;
            Player damageDealer = tracker.findHighestDamageDealer();
            if (damageDealer == null || !damageDealer.isOnline()) return;
            for (Entity entity : monster.getNearbyEntities(10, 10, 10)) {
                if (!(entity instanceof Player)) continue;
                if (!entity.getName().equalsIgnoreCase(damageDealer.getName())) continue;
                ((EntityInsentient) ((CraftLivingEntity) monster).getHandle()).setGoalTarget(((CraftLivingEntity) entity).getHandle(), EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY, false);
                break;
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
        if (damage == 0) {
            return;
        }
        if (damage < 0) {
            damage = 1;
        }

        // Temporary Hack
        if (attacker != null && GameAPI.isPlayer(attacker) && damage > 3000) {
            // Destroy the item from the game.
            Bukkit.getPlayer(attacker.getName()).getInventory().setItemInMainHand(new ItemStack(Material.AIR));

            // Prompt the user that this weapon is not allowed.
            attacker.sendMessage(ChatColor.YELLOW + "The weapon you posses is not of this world and has been returned to the Gods.");

            // User isn't a GM, let's alert the GMs about this violation.
            if (!Rank.isGM((Player) attacker)) {
                GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED.toString() + "[ANTI CHEAT] " +
                        ChatColor.WHITE + "Destroyed illegal weapon (" + damage + " ) from " + attacker.getName() + " on shard " + ChatColor.GOLD + ChatColor.UNDERLINE + DungeonRealms.getInstance().shardid);
            }
            return;
        }

        double maxHP = getMonsterMaxHPLive(entity);
        double currentHP = getMonsterHPLive(entity);
        double newHP = currentHP - damage;
        if (newHP >= currentHP) return;
        if (entity instanceof EntityArmorStand) return;
        if (currentHP <= 0) {
            if (!entity.isDead()) {
                entity.setHealth(0);
            }
            return;
        }

        entity.playEffect(EntityEffect.HURT);

        if (attacker != null) {
            if (GameAPI.isPlayer(attacker)) {
                handleMonsterDamageTracker(entity.getUniqueId(), (Player) attacker, damage);
                checkForNewTarget(entity);
                //entity.getWorld().playEffect(entity.getLocation().clone().add(0, 1, 0), Effect.STEP_SOUND, 152);

                if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, attacker.getUniqueId()).toString())) {
                    if (!entity.hasMetadata("uuid")) {
                        String customNameAppended = (entity.getMetadata("customname").get(0).asString().trim());
                        ChatColor npcTierColor = GameAPI.getTierColor(entity.getMetadata("tier").get(0).asInt());
                        attacker.sendMessage(ChatColor.RED + "     " + (int) damage + ChatColor.BOLD + " DMG" + ChatColor.RED + " -> " + ChatColor.GRAY + npcTierColor + customNameAppended + npcTierColor + " [" + (int) (newHP < 0 ? 0 : newHP) + "HP]");
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
                    entity.setNoDamageTicks(100);
                    entity1.die();
                    EntityDeathEvent event = new EntityDeathEvent(entity, new ArrayList<>());
                    Bukkit.getPluginManager().callEvent(event);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                        entity.remove();
                        entity1.die();
                    }, 30L);
                }
            }, 1L);
            if (EntityMechanics.MONSTER_LAST_ATTACK.containsKey(entity)) {
                EntityMechanics.MONSTER_LAST_ATTACK.remove(entity);
            }
            if (EntityMechanics.MONSTERS_LEASHED.contains(entity)) {
                EntityMechanics.MONSTERS_LEASHED.remove(entity);
            }
            return;
        }

        setMonsterHPLive(entity, (int) newHP);
        double monsterHPPercent = (newHP / maxHP);
        double newMonsterHPToDisplay = monsterHPPercent * entity.getMaxHealth();
        int convHPToDisplay = (int) newMonsterHPToDisplay;
        if (convHPToDisplay <= 1) {
            convHPToDisplay = 1;
        } else if (convHPToDisplay > (int) entity.getMaxHealth()) {
            convHPToDisplay = (int) entity.getMaxHealth();
        }
        if (entity.hasMetadata("type") && entity.hasMetadata("level") && entity.hasMetadata("tier")) {
            int tier = entity.getMetadata("tier").get(0).asInt();
            boolean elite = entity.hasMetadata("elite");
            entity.setCustomName(EntityMechanics.getInstance().generateOverheadBar(entity, newHP, maxHP, tier, elite));
            entity.setCustomNameVisible(true);
            entity.setHealth(convHPToDisplay);
            if (!EntityMechanics.MONSTERS_LEASHED.contains(entity)) {
                EntityMechanics.MONSTERS_LEASHED.add(entity);
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
        int totalHP = 0; // base hp

        if (entity.hasMetadata("type"))
            totalHP += ((DRMonster) ((CraftLivingEntity) entity).getHandle()).getAttributes().get("healthPoints")[1];
        else if (GameAPI.isPlayer(entity))
            totalHP += 50 + GameAPI.getStaticAttributeVal(Item.ArmorAttributeType.HEALTH_POINTS, (Player) entity);

        if (entity.hasMetadata("type") && entity.getMetadata("type").get(0).asString().equals("hostile")) {
            switch (entity.getMetadata("tier").get(0).asInt()) {
                case 1:
                    totalHP *= .9;
                    break;
                case 2:
                    totalHP *= 1.1;
                    break;
                case 3:
                    totalHP *= 1.3;
                    break;
                case 4:
                    totalHP *= 1.6;
                    break;
                case 5:
                    totalHP *= 2;
                    break;
            }
        }

        if (entity.hasMetadata("elite")) {
            switch (entity.getMetadata("tier").get(0).asInt()) {
                case 1:
                    totalHP *= 1.8;
                    break;
                case 2:
                    totalHP *= 2.5;
                    break;
                case 3:
                    totalHP *= 3.;
                    break;
                case 4:
                    totalHP *= 4.;
                    break;
                case 5:
                    totalHP *= 5.5;
                    break;
            }
        }

        if (entity.hasMetadata("boss")) {
            totalHP *= 6;
        }

        return totalHP;
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
            int hpRegen = GameAPI.getStaticAttributeVal(Item.ArmorAttributeType.HEALTH_REGEN, player) + 5;
            player.setMetadata("regenHP", new FixedMetadataValue(DungeonRealms.getInstance(), hpRegen));
            return hpRegen;
        }
    }
}
