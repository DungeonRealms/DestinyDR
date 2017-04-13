package net.dungeonrealms.game.handler;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.punishment.PunishAPI;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.listener.combat.AttackResult;
import net.dungeonrealms.game.listener.combat.DamageResultType;
import net.dungeonrealms.game.listener.combat.DamageType;
import net.dungeonrealms.game.mastery.DamageTracker;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.chat.GameChat;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.duel.DuelOffer;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_9_R2.EntityArmorStand;
import net.minecraft.server.v1_9_R2.EntityInsentient;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.inventivetalent.bossbar.BossBarAPI;
import org.inventivetalent.bossbar.BossBarAPI.Style;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Kieran on 10/3/2015.
 */
public class HealthHandler implements GenericMechanic {

    @Getter
    private static HashMap<UUID, DamageTracker> monsterTrackers = new HashMap<>();

    @Getter
    private static HealthHandler instance = new HealthHandler();

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    public void startInitialization() {
        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), () -> {
            Bukkit.getServer().getOnlinePlayers().stream().forEach(this::updatePlayerOverheadHP);
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
        
        Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
        	GamePlayer gp = GameAPI.getGamePlayer(player);
            setPlayerMaxHP(player, gp.getAttributes().getAttribute(ArmorAttributeType.HEALTH_POINTS).getValue() + 50);
            int hp = Integer.valueOf(DatabaseAPI.getInstance().getData(EnumData.HEALTH, player.getUniqueId()).toString());

            // 1 <= HP <= Max HP
            hp = Math.min(getPlayerMaxHP(player), hp);
            hp = Math.max(1, hp);
            setPlayerHP(player, hp);

            int hpRegen = gp.getAttributes().getAttribute(ArmorAttributeType.HEALTH_REGEN).getValue() + 5;
            setPlayerHPRegen(player, hpRegen);
            
            //  MARK PLAYER AS DONE.  //
            player.setMetadata("lastDeathTime", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
            player.removeMetadata("loggingIn", DungeonRealms.getInstance());
        }, 21);
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
    	player.getActivePotionEffects().clear();
        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.HEALTH, getPlayerHP(player), true);
    }

    /**
     * Returns the players current HP
     *
     * @param player
     * @return int
     * @since 1.0
     */
    public static int getPlayerHP(Player player) {
    	return player.hasMetadata("currentHP") ? player.getMetadata("currentHP").get(0).asInt() : 50;
    }

    /**
     * Returns the monsters current HP
     *
     * @param entity
     * @return int
     * @since 1.0
     */
    public static int getMonsterHP(LivingEntity entity) {
    	return entity.hasMetadata("currentHP") ? entity.getMetadata("currentHP").get(0).asInt() : 100;
    }

    /**
     * Sets the players HP bar
     * Called in "updatePlayerHPBars"
     *
     * @param player
     * @param hp
     * @since 1.0
     */
    private void updatePlayerOverheadHP(Player player) {
        Player showData = player;
        
        if(player.getGameMode() == GameMode.SPECTATOR && player.getSpectatorTarget() instanceof Player)
        	showData = (Player)player.getSpectatorTarget();

        GamePlayer gamePlayer = GameAPI.getGamePlayer(showData);
        if (!gamePlayer.isAttributesLoaded())
            return;
        
        int currentHP = getPlayerHP(showData);
        double maxHP = getPlayerMaxHP(showData);
        
        double healthPercentage = Math.min((double) currentHP / maxHP, 1D);
        float healthToDisplay = (float) (healthPercentage * 100.F);
        
        //  GENERATE LEVEL STRING  //
        int playerLevel = gamePlayer.getLevel();
        String playerLevelInfo = ChatColor.AQUA + "" + ChatColor.BOLD + "LV. " + ChatColor.AQUA + playerLevel;

        String separator = ChatColor.WHITE + " - ";
        
        //  GENERATE HP STRING  //
        ChatColor hpColor = ChatColor.RED;
        if (GameAPI.isInSafeRegion(player.getLocation())) {
        	hpColor = ChatColor.GREEN;
        } else if (GameAPI.isNonPvPRegion(player.getLocation())) {
        	hpColor = ChatColor.YELLOW;
        }
        String playerHPInfo = hpColor + "" + ChatColor.BOLD + "HP " + hpColor + currentHP
        		+ (currentHP != maxHP ? ChatColor.BOLD + " / " + hpColor + (int)maxHP : "");
        
        //  GENERATE XP STRING  //
        double exp = ((double) gamePlayer.getExperience()) / ((double) gamePlayer.getEXPNeeded(playerLevel));
        exp *= 100;
        String playerEXPInfo = ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "XP " + ChatColor.LIGHT_PURPLE + (int) exp + "%";
        if (playerLevel == 100)
            playerEXPInfo = ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "MAX";
        
        //  DISPLAY THE BAR  //
        BossBarAPI.removeAllBars(player);
        BossBarAPI.addBar(player, new TextComponent("    " + playerLevelInfo + separator + playerHPInfo + separator + playerEXPInfo), BossBarAPI.Color.valueOf(hpColor.name()), getStyle(maxHP), healthToDisplay);
        
        //  UPDATE HP FOR OTHER PLAYERS  //
        int finalHp = currentHP;
        DungeonRealms.getInstance().getServer().getScheduler().runTask(DungeonRealms.getInstance(), () -> ScoreboardHandler.getInstance().updatePlayerHP(player, finalHp));
    }

    private static Style getStyle(double maxHealth) {
    	int[] healthInc = new int[] {0, 500, 1000, 3000, 5000};
    	Style[] styles = new Style[] {Style.PROGRESS, Style.NOTCHED_6, Style.NOTCHED_10, Style.NOTCHED_12, Style.NOTCHED_20};
    	
    	for (int i = 1; i < healthInc.length; i++)
    		if (healthInc[i] > maxHealth)
    			return styles[i - 1];
    	
    	return styles[styles.length - 1];
    }


    /**
     * Sets a player's HP.
     */
    public static void setPlayerHP(Player player, int hp) {
        if (player.hasMetadata("maxHP") && hp > player.getMetadata("maxHP").get(0).asInt())
            hp = getPlayerMaxHP(player);
        setEntityHP(player, hp);
    }

    /**
     * Sets an monster's HP.
     */
    public static void setMonsterHP(LivingEntity monster, int hp) {
    	setEntityHP(monster, hp);
    }
    
    private static void setEntityHP(LivingEntity entity, int hp) {
        entity.setMetadata("currentHP", new FixedMetadataValue(DungeonRealms.getInstance(), hp));
        
        //  SET VANILLA HEALTH  //
        double vanillaMax = entity.getMaxHealth();
        double vanillaHealth = (hp / getMonsterMaxHP(entity)) * vanillaMax;
        vanillaHealth = Math.min(vanillaHealth, vanillaMax - (1 / vanillaMax));
        vanillaHealth = Math.max(1, vanillaHealth);
        entity.setHealth((int)vanillaHealth);
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
    public static int getMonsterMaxHPOnSpawn(LivingEntity entity) {
        return calculateMaxHPFromItems(entity);
    }

    /**
     * Return a player's maximum HP. Calculates if not set.
     */
    public static int getPlayerMaxHP(Player player) {
        if (player.hasMetadata("maxHP")) {
            return player.getMetadata("maxHP").get(0).asInt();
        } else {
        	int maxHP = calculateMaxHPFromItems(player);
            player.setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), maxHP));
            return maxHP;
        }
    }

    /**
     * Gets a monster's max HP.
     */
    public static int getMonsterMaxHP(LivingEntity ent) {
    	return getEntityMaxHP(ent);
    }
    
    private static int getEntityMaxHP(LivingEntity entity) {
    	return entity.hasMetadata("maxHP") ? entity.getMetadata("maxHP").get(0).asInt() : 100;
    }

    /**
     * Reloads the player HP from armor.
     * Called after a death or whenever a related inventory click occurs.
     */
    public static void updatePlayerHP(Player player) {
        setPlayerMaxHP(player, calculateMaxHPFromItems(player));
        GamePlayer gp = GameAPI.getGamePlayer(player);
        setPlayerHPRegen(player, gp.getAttributes().getAttribute(ArmorAttributeType.HEALTH_REGEN).getValue() + 5);
        if (getPlayerHP(player) > getPlayerMaxHP(player))
            setPlayerHP(player, getPlayerMaxHP(player));
    }

    /**
    * Sets a player's maximum HP.
    */
    public static void setPlayerMaxHP(Player player, int maxHP) {
        player.setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), maxHP));
    }

    /**
     * Handles all players regenerating
     * their health.
     */
    private void regenerateHealth() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            //10 seconds of taking damage in a duel
            if (CombatLog.isInCombat(player) || CombatLog.inPVP(player) || player.hasMetadata("lastDamageTaken") && (System.currentTimeMillis() - player.getMetadata("lastDamageTaken").get(0).asLong()) < 10_000L)
                continue;
                
            if (getPlayerHP(player) <= 0 && player.getHealth() <= 0)
                continue;
            
            if (player.hasMetadata("starving"))
                continue;
            
            if (GameAPI.getGamePlayer(player) == null || !GameAPI.getGamePlayer(player).isAttributesLoaded())
                continue;
            
            healPlayer(player, getPlayerHPRegen(player));
        }
    }

    /**
     * Heals the specified player.
     */
    public static void healPlayer(Player player, int healAmount) {
    	double currentHP = getPlayerHP(player);
        double maxHP = getPlayerMaxHP(player);
        if (currentHP >= maxHP)
        	return;
        
        double newHealth = Math.min(currentHP + healAmount, maxHP);

        if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId()).toString()))
            player.sendMessage(ChatColor.GREEN + "        +" + healAmount + ChatColor.BOLD + " HP" + ChatColor.GRAY + " [" + (int) newHealth + "/" + (int) maxHP + "HP]");

        setPlayerHP(player, (int) Math.min(getMonsterHP(player) + healAmount, getEntityMaxHP(player)));
    }

    /**
     * Heals the specified Monster.
     */
    public static void healMonster(LivingEntity entity, int healAmount) {
    	setMonsterHP(entity, (int) Math.min(getMonsterHP(entity) + healAmount, getEntityMaxHP(entity)));
    }

    public static void damagePlayer(AttackResult res) {
    	damagePlayer(res, res.getCause());
    }
    
    public static void damagePlayer(AttackResult res, DamageCause cause) {
    	damagePlayer(res, cause, true);
    }
    
    public static void damagePlayer(AttackResult res, DamageCause cause, boolean updateCombat) {
    	Player player = res.getDefender().getPlayer();
    	LivingEntity attacker = res.getAttacker().getEntity();
    	double damage = res.getWeightedDamage();
    	final GamePlayer gp = GameAPI.getGamePlayer(player);
    	
    	//  DONT DAMAGE THESE  //
        if (player.getGameMode().equals(GameMode.SPECTATOR) || player.getGameMode().equals(GameMode.CREATIVE) || gp
                == null || gp.isInvulnerable())
            return;

        
        boolean isReflectedDamage = res.getResult() == DamageResultType.REFLECT;

        if (damage < 0) {
           	Utils.log.info("Negative damage dealt to " + player.getName() + " Damager: " + attacker.getName() + " Damage: " + damage);
            damage = 1;
        }
        
        double currentHP = getPlayerHP(player);
        double newHP = currentHP - damage;

        if (cause == null || cause != EntityDamageEvent.DamageCause.FALL) {
            if (updateCombat) {
                if (!(attacker instanceof Player)) {
                	CombatLog.updateCombat(player);
                } else {
                	CombatLog.updatePVP(player);
                }
            }
        }
        
        if (attacker != null) {
            if (attacker instanceof Player) {

            	//  Destroys certain items spawned in by Vawke. (Should be safe to remove now.)
                if (damage > 3000) {
                    // Destroy the item from the game.
                    Bukkit.getPlayer(attacker.getName()).getInventory().setItemInMainHand(new ItemStack(Material.AIR));

                    attacker.sendMessage(ChatColor.YELLOW + "The weapon you posses is not of this world and has been returned to the Gods.");

                        GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED + "[WARNING] " +
                                ChatColor.WHITE + "Destroyed illegal weapon (" + damage + " ) from " + attacker.getName() + " on shard {SERVER}.");
                    return;
                }

            } else if (attacker instanceof Projectile) {
                attacker = (LivingEntity) ((Projectile) attacker).getShooter();
            }
        }

        if (GameAPI.isPlayer(attacker)) {
            if (newHP <= 0 && DuelingMechanics.isDueling(player.getUniqueId())) {
                DuelOffer offer = DuelingMechanics.getOffer(player.getUniqueId());
                if (offer != null) {
                    player.setMetadata("duelCooldown", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis() + 1000));
                    ((Player)attacker).setMetadata("duelCooldown", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis() + 1000));
                    offer.endDuel((Player)attacker, player);
                }
                return;
            }
            if (!DuelingMechanics.isDuelPartner(player.getUniqueId(), attacker.getUniqueId())) {
                if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK && !isReflectedDamage)
                    if (!(attacker.hasMetadata("duelCooldown") && attacker.getMetadata("duelCooldown").size() > 0 && attacker.getMetadata("duelCooldown").get(0).asLong() > System.currentTimeMillis()))
                        KarmaHandler.getInstance().handleAlignmentChanges((Player) attacker);
                        
                if (newHP <= 0 && Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_CHAOTIC_PREVENTION, attacker.getUniqueId()).toString())) {
                    if (KarmaHandler.getInstance().getPlayerRawAlignment(player) == KarmaHandler.EnumPlayerAlignments.LAWFUL) {
                        player.setFireTicks(0);
                        player.getActivePotionEffects().clear();
                        newHP = 1;
                        attacker.sendMessage(ChatColor.YELLOW + "Your Chaotic Prevention Toggle has activated preventing the death of " + player.getName() + "!");
                        player.sendMessage(ChatColor.YELLOW + attacker.getName() + " has their Chaotic Prevention Toggle ON, your life has been spared!");
                    }
                }
            }

            if (!isReflectedDamage) {
                //Track this player damage for when we die.
                player.setMetadata("lastPlayerToDamageExpire", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis() + 3000));
                player.setMetadata("lastPlayerToDamage", new FixedMetadataValue(DungeonRealms.getInstance(), attacker.getName()));
            }

            if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, attacker.getUniqueId()).toString())) {
                String msg = ChatColor.RED + "     " + (int) damage + ChatColor.BOLD + " DMG" + ChatColor.RED + " -> " + ChatColor.RED + player.getName() + ChatColor.RED + " [" + (int) newHP + ChatColor.BOLD + "HP]";
                attacker.sendMessage(msg);
                GameAPI.runAsSpectators(attacker, (pl) -> pl.sendMessage(msg));
            }
            player.playSound(player.getLocation(), Sound.ENCHANT_THORNS_HIT, 1F, 1F);
        }

        //player.getWorld().playEffect(player.getLocation().clone().add(0, 1, 0), Effect.STEP_SOUND, 152);
        
        //  DISABLE DEATH FROM FIRE TICK  //
        if (cause == DamageCause.FIRE_TICK && newHP <= 0)
        	return;
        	
        
        if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId()).toString())) {
        	if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
        		String msg = ChatColor.RED + "     -" + (int) damage + ChatColor.BOLD + " HP" + ChatColor.GRAY + " [-"
        				+ (int)res.getTotalArmor() + "%A -> -" + (int)res.getTotalArmorReduction() + ChatColor.BOLD + "DMG" +
        				ChatColor.GRAY
        				+ "]" + ChatColor.GREEN + " [" + (int) newHP + ChatColor.BOLD + "HP" + ChatColor.GREEN + "]";
        		player.sendMessage(msg);
        		GameAPI.runAsSpectators(player, (pl) -> pl.sendMessage(msg));
        	} else { // foreign damage
        		DamageType type = DamageType.getByReason(cause);
                String msg = ChatColor.RED + "     -" + (int) damage + ChatColor.BOLD + " HP " + type.getDisplay() + ChatColor.GREEN + " [" + (int) newHP + ChatColor.BOLD + "HP" + ChatColor.GREEN + "]";
                GameAPI.runAsSpectators(player, (pl) -> pl.sendMessage(msg));
                player.sendMessage(msg);
            }
        }

        if (newHP <= 0)
            if (handlePlayerDeath(player, attacker)) return;

        setPlayerHP(player, (int) newHP);
        
        if (attacker != null && attacker.getType() != EntityType.PLAYER) {
            EntityMechanics.MONSTER_LAST_ATTACK.put(attacker, 15);
            if (!EntityMechanics.MONSTERS_LEASHED.contains(attacker))
                EntityMechanics.MONSTERS_LEASHED.add(attacker);
        }
    }
    
    public static boolean handlePlayerDeath(Player player, LivingEntity leAttacker) {
    	Chat.listenForMessage(player, null, null);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 1f);
        
        //  PERMA DEATH ON EVENT SHARDS  //
        if (DungeonRealms.getInstance().isEventShard) {
            if (Rank.isTrialGM(player)) {
                player.sendMessage(ChatColor.GOLD + "Death has acknowledged your identity and chosen to spare your life.");
            } else {
                Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
                    if(player.isOnline())
                        player.kickPlayer(ChatColor.RED + "You have been eliminated from this event.");
                    PunishAPI.ban(player.getUniqueId(), player.getName(), "Event Controller", -1, "You have been eliminated", null);
                }, 5);
            }
    	}
        
        if(player.hasMetadata("lastDeathTime") && System.currentTimeMillis() - player.getMetadata("lastDeathTime").get(0).asLong() <= 100)
        	return false;
        
        if (!(leAttacker instanceof Player)) {
            Player damagerKiller = getKillerFromRecentDamage(player);
            if (damagerKiller != null)
                leAttacker = damagerKiller;
        }
        
        String killerName = "";
        String deathMessage = "";
        
        String deadPlayerName = GameChat.getPreMessage(player).replaceAll(":", "").trim();
        if (ChatColor.stripColor(deadPlayerName).startsWith("<G>"))
            deadPlayerName = deadPlayerName.split(">")[1];
        
        if (leAttacker instanceof Player) {
        	
        	Player killer = (Player)leAttacker;
        	//  SET NAME  //
        	killerName = leAttacker.getName();
        	killerName = GameChat.getPreMessage((Player) leAttacker).replaceAll(":", "").trim();
            if (ChatColor.stripColor(killerName).startsWith("<G>"))
                killerName = killerName.split(">")[1];
            
        	if (Achievements.getInstance().hasAchievement(player.getUniqueId(), Achievements.EnumAchievements.INFECTED))
                Achievements.getInstance().giveAchievement(killer.getUniqueId(), Achievements.EnumAchievements.INFECTED);
        	
        	ItemStack item = player.getInventory().getItemInMainHand();
        	String suffix = "";
        	if(item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName())
        		suffix = " with a(n) " + item.getItemMeta().getDisplayName();
        	
        	deathMessage = deadPlayerName + " was killed by " + killerName + suffix + ChatColor.WHITE + ".";
        } else {
        	deathMessage = deadPlayerName + " was killed by The World.";
        }
        
        //  ANNOUNCE MESSAGE  //
        final String finalDeathMessage = deathMessage;
        GameAPI.getNearbyPlayers(player.getLocation(), 100).forEach(p -> p.sendMessage(finalDeathMessage));
        
        GamePlayer gp = GameAPI.getGamePlayer(player);
       	gp.setPvpTaggedUntil(0);
       	
       	final LivingEntity finalKiller = leAttacker;
       	
        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
            player.setMetadata("lastDeathTime", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
            player.damage(player.getMaxHealth());
            if (finalKiller != null)
                KarmaHandler.getInstance().handlePlayerPsuedoDeath(player, finalKiller);
            
            gp.calculateAllAttributes();
            CombatLog.removeFromCombat(player);
            if (CombatLog.inPVP(player))
                CombatLog.removeFromPVP(player);
        });
        return true;
    }

    public static Player getKillerFromRecentDamage(Player player) {
        if (player.hasMetadata("lastPlayerToDamageExpire")) {
            long expiration = player.getMetadata("lastPlayerToDamageExpire").get(0).asLong();
            if (expiration > System.currentTimeMillis()) {
                String killer = player.getMetadata("lastPlayerToDamage").get(0).asString();
                Player onlineKiller = Bukkit.getPlayer(killer);
                if (onlineKiller != null) {
                    return onlineKiller;
                }
            }
        }
        return null;
    }

    private static void handleMonsterDamageTracker(UUID monster, Player attacker, double damage) {
        if (monsterTrackers.containsKey(monster)) {
            monsterTrackers.get(monster).addPlayerDamage(attacker, damage);
        } else {
            DamageTracker damageTracker = new DamageTracker(monster);
            damageTracker.addPlayerDamage(attacker, damage);
            monsterTrackers.put(monster, damageTracker);
        }
    }

    private static void checkForNewTarget(LivingEntity monster) {
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
     * Damages an entity.
     */
    public static void damageEntity(AttackResult res) {
    	if(res.getDefender().isPlayer()) {
    		damagePlayer(res);
    	} else {
    		damageMonster(res);
    	}
    }
    
    /**
     * Damages a monster.
     */
    public static void damageMonster(AttackResult res) {
    	if (res.getDamage() <= 0)
    		return;
    	
    	LivingEntity defender = res.getDefender().getEntity();
    	LivingEntity attacker = res.getAttacker().getEntity();
    	double damage = res.getWeightedDamage();
    	
    	if(defender instanceof EntityArmorStand)
    		return;
    	
    	if (attacker != null && GameAPI.isPlayer(attacker) && damage > 3000) {
    		res.getAttacker().getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
    		attacker.sendMessage(ChatColor.YELLOW + "The weapon you posses is not of this world and has been returned to the Gods.");
    		
    		//  ALERT STAFF  //
            GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED + "[WARNING] " + ChatColor.WHITE + "Destroyed illegal item ("
            		+ damage + ") from " + attacker.getName() + " on shard {SERVER}.");
            return;
    	}
    	
    	
    	double maxHP = getMonsterMaxHP(defender);
    	double currentHP = getMonsterHP(defender);
    	double newHP = currentHP - damage;
    	
    	if (currentHP <= 0) {
    		if (!defender.isDead())
            	defender.setHealth(0);
            return;
        }

        defender.playEffect(EntityEffect.HURT);

        if (attacker != null && GameAPI.isPlayer(attacker)) {
        	handleMonsterDamageTracker(defender.getUniqueId(), (Player) attacker, damage);
        	checkForNewTarget(defender);
        	//entity.getWorld().playEffect(entity.getLocation().clone().add(0, 1, 0), Effect.STEP_SOUND, 152);
        	
        	if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, attacker.getUniqueId()).toString())) {
        		if (!defender.hasMetadata("uuid")) {
        			String customNameAppended = defender.getMetadata("customname").get(0).asString().trim();
        			ChatColor npcTierColor = GameAPI.getTierColor(defender.getMetadata("tier").get(0).asInt());
        			attacker.sendMessage(ChatColor.RED + "     " + (int) damage + ChatColor.BOLD + " DMG" + ChatColor.RED + " -> " + ChatColor.GRAY + npcTierColor + customNameAppended + npcTierColor + " [" + (int) (newHP < 0 ? 0 : newHP) + "HP]");
        		}
        	}
        }

        if (newHP <= 0) {
        	//  KILL ENTITY  //
        	defender.playEffect(EntityEffect.DEATH);
            setMonsterHP(defender, 0);
            defender.damage(defender.getHealth());
            defender.setMaximumNoDamageTicks(2000);
            defender.setNoDamageTicks(1000);
            
            net.minecraft.server.v1_9_R2.Entity entity1 = ((CraftEntity) defender).getHandle();
            
            // I am assuming there is a delay here so they don't vanish during their death animation. Unsure why it dies several times.
            Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
                if (!defender.isDead()) {
                	defender.setMaximumNoDamageTicks(200);
                	defender.setNoDamageTicks(100);
                    entity1.die();
                    
                    //  FIRE DEATH EVENT  //
                    EntityDeathEvent event = new EntityDeathEvent(defender, new ArrayList<>());
                    Bukkit.getPluginManager().callEvent(event);
                    Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
                    	defender.remove();
                        entity1.die();
                    }, 30L);
                }
            }, 1L);
            
            if (EntityMechanics.MONSTER_LAST_ATTACK.containsKey(defender))
                EntityMechanics.MONSTER_LAST_ATTACK.remove(defender);
            if (EntityMechanics.MONSTERS_LEASHED.contains(defender))
                EntityMechanics.MONSTERS_LEASHED.remove(defender);
            
            return;
        }

        setMonsterHP(defender, (int) newHP);
        
        //  TO VANILLA HP  //
        double monsterHPPercent = (newHP / maxHP);
        double newMonsterHPToDisplay = monsterHPPercent * defender.getMaxHealth();
        int convHPToDisplay = (int) newMonsterHPToDisplay;
        convHPToDisplay = Math.max(1, convHPToDisplay);
        convHPToDisplay = Math.min(convHPToDisplay, (int) defender.getMaxHealth());
        
        if (defender.hasMetadata("type") && defender.hasMetadata("level") && defender.hasMetadata("tier")) {
            int tier = defender.getMetadata("tier").get(0).asInt();
            boolean elite = defender.hasMetadata("elite");
            defender.setCustomName(EntityMechanics.getInstance().generateOverheadBar(defender, newHP, maxHP, tier, elite));
            defender.setCustomNameVisible(true);
            defender.setHealth(convHPToDisplay);
            if (!EntityMechanics.MONSTERS_LEASHED.contains(defender))
                EntityMechanics.MONSTERS_LEASHED.add(defender);
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
    public static int calculateMaxHPFromItems(LivingEntity entity) {
        int totalHP = 0; // base hp

        if (entity.hasMetadata("type"))
            totalHP += ((DRMonster) ((CraftLivingEntity) entity).getHandle()).getAttributes().getAttribute(ArmorAttributeType.HEALTH_POINTS).getValue();
        else if (GameAPI.isPlayer(entity))
            totalHP += 50 + GameAPI.getGamePlayer((Player)entity).getAttributes().getAttribute(ArmorAttributeType.HEALTH_POINTS).getValue();

        double[] hostileModifier = new double[] {.9, 1.1, 1.3, 1.6, 2};
        double[] eliteModifier = new double[] {1.8, 2.5, 3, 4, 5.5};
        
        int tier = entity.getMetadata("tier").get(0).asInt();
        
        //  HOSTILE MODIFIER  //
        if (EnumEntityType.HOSTILE_MOB.isType(entity))
        	totalHP *= hostileModifier[tier - 1];
        
        //  ELITE MODIFIER  //
        if (entity.hasMetadata("entity"))
        	totalHP *= eliteModifier[tier - 1];

        //  BOSS MULTIPLIER  //
        if (entity.hasMetadata("boss"))
            totalHP *= 6;

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
    public static void setPlayerHPRegen(Player player, int regenAmount) {
        player.setMetadata("regenHP", new FixedMetadataValue(DungeonRealms.getInstance(), regenAmount));
    }

    /**
     * Returns the players current HPRegen
     *
     * @param player
     * @return int
     * @since 1.0
     */
    public static int getPlayerHPRegen(Player player) {
        if (player.hasMetadata("regenHP")) {
            return player.getMetadata("regenHP").get(0).asInt();
        } else {
        	GamePlayer gp = GameAPI.getGamePlayer(player);
        	if (gp != null) {
        		int hpRegen = gp.getAttributes().getAttribute(ArmorAttributeType.HEALTH_REGEN).getValue() + 5;
            	player.setMetadata("regenHP", new FixedMetadataValue(DungeonRealms.getInstance(), hpRegen));
            	return hpRegen;
        	}
        	return 0;
        }
    }
}
