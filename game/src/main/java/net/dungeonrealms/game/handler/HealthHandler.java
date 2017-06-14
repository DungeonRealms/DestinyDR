package net.dungeonrealms.game.handler;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.database.PlayerToggles.Toggles;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.punishment.PunishAPI;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;
import net.dungeonrealms.game.handler.KarmaHandler.EnumPlayerAlignments;
import net.dungeonrealms.game.item.items.functional.ecash.ItemDPSDummy;
import net.dungeonrealms.game.listener.combat.AttackResult;
import net.dungeonrealms.game.listener.combat.DamageResultType;
import net.dungeonrealms.game.listener.combat.DamageType;
import net.dungeonrealms.game.mastery.DamageTracker;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.HealTracker;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.duel.DuelOffer;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.player.inventory.menus.DPSDummy;
import net.dungeonrealms.game.title.TitleAPI;
import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_9_R2.EntityHuman;
import net.minecraft.server.v1_9_R2.EntityInsentient;
import net.minecraft.server.v1_9_R2.SoundEffects;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.AbstractProjectile;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.inventivetalent.bossbar.BossBarAPI;
import org.inventivetalent.bossbar.BossBarAPI.Style;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Kieran on 10/3/2015.
 */
public class HealthHandler implements GenericMechanic {

    @Getter
    private static HashMap<UUID, DamageTracker> monsterTrackers = new HashMap<>();

    private volatile static Map<UUID, HealTracker> healTracker = new ConcurrentHashMap<>();

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    public void startInitialization() {
        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), () ->
                Bukkit.getServer().getOnlinePlayers().forEach(HealthHandler::updateBossBar), 0L, 20L);
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::regenerateHealth, 40L, 20L);

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            healTracker.forEach((id, tracker) -> {
                String cd = tracker.getHealTimer();
                if (cd == null || !tracker.getPlayerHealing().isOnline()) {
                    healTracker.remove(id);
                    if (tracker.getPlayerHealing().isOnline())
                        TitleAPI.sendActionBar(tracker.getPlayerHealing(), "", -1);

                    return;
                }

                TitleAPI.sendActionBar(tracker.getPlayerHealing(), cd, -1);
            });
        }, 20, 2);
    }

    @Override
    public void stopInvocation() {

    }

    /**
     * Calculates the player's stats on login.
     */
    public static void handleLogin(Player player) {
        PlayerWrapper pw = PlayerWrapper.getWrapper(player);
        pw.calculateAllAttributes();

        setHP(player, pw.getHealth()); // Load health from db

        //  MARK PLAYER AS DONE.  //
        player.setMetadata("lastDeathTime", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
    }

    /**
     * Handles players logging out,
     * removes potion effects and
     * updates database for web usage.
     */
    public static void handleLogout(Player player) {
        player.getActivePotionEffects().clear();
        PlayerWrapper.getWrapper(player).setHealth(getHP(player));
    }

    /**
     * Sets both maxHP and current HP to this value.
     */
    public static void initHP(Entity e, int hp) {
        setMaxHP(e, hp);
        setHP(e, hp);
    }

    /**
     * Set the max HP of an entity.
     */
    public static void setMaxHP(Entity e, int maxHP) {
        Metadata.MAX_HP.set(e, maxHP);
        setHP(e, Math.min(getHP(e), maxHP)); // Cap the entities HP at the max HP limit.
    }

    /**
     * Sets an entities HP.
     */
    public static void setHP(Entity e, int currentHP) {
        Metadata.CURRENT_HP.set(e, Math.min(currentHP, getMaxHP(e)));

        if (!(e instanceof LivingEntity))
            return;
        LivingEntity le = (LivingEntity) e;

        //  SET VANILLA HEALTH  //
        double vanillaMax = le.getMaxHealth();
        double vanillaHealth = getHPPercent(e) * vanillaMax;

        // 1 <= Health <= maxHealth
        if (vanillaHealth > vanillaMax - (1 / vanillaMax)) // If we're less than half a heart from max health, set it to max.
            vanillaHealth = vanillaMax;

        le.setHealth(Math.max(1, vanillaHealth));
    }

    /**
     * Gets an entities current HP.
     */
    public static int getHP(Entity e) {
        return Metadata.CURRENT_HP.get(e).asInt();
    }

    /**
     * Gets an entities Max HP.
     */
    public static int getMaxHP(Entity e) {
        if (!Metadata.MAX_HP.has(e) && e instanceof LivingEntity)
            calculateHP((LivingEntity) e);
        return Metadata.MAX_HP.get(e).asInt();
    }

    /**
     * Gets the HP percentage in decimal form (95% = .95)
     */
    public static double getHPPercent(Entity e) {
        return getMaxHP(e) == 0 ? 0D : getHP(e) / (double) getMaxHP(e);
    }

    /**
     * Set player HP/s regen.
     */
    public static void setRegen(Player player, int regen) {
        Metadata.HP_REGEN.set(player, regen);
    }

    /**
     * Gets a player's HP/s regen.
     * Calculates if not present.
     */
    public static int getRegen(Player player) {
        PlayerWrapper pw = PlayerWrapper.getWrapper(player);
        int hpRegen = pw.getAttributes().getAttribute(ArmorAttributeType.HEALTH_REGEN).getValue() + 10;
        hpRegen += pw.getAttributes().getAttribute(ArmorAttributeType.VITALITY).getValue() * 0.3;
        if (!Metadata.HP_REGEN.has(player) && pw != null)
            Metadata.HP_REGEN.set(player, hpRegen);
        return Metadata.HP_REGEN.get(player).asInt();
    }

    /**
     * Sets the players HP bar
     * Called in "updatePlayerHPBars"
     */
    public static void updateBossBar(Player player) {
        Player showData = player;

        if (player.getGameMode() == GameMode.SPECTATOR && player.getSpectatorTarget() instanceof Player)
            showData = (Player) player.getSpectatorTarget();

        PlayerWrapper pw = PlayerWrapper.getWrapper(showData);
        if (pw == null || !pw.isAttributesLoaded())
            return;

        int currentHP = getHP(showData);
        double maxHP = getMaxHP(showData);

        float healthPercent = (float) (100.0F * getHPPercent(showData));

        //  GENERATE LEVEL STRING  //
        int playerLevel = pw.getLevel();
        String playerLevelInfo = ChatColor.AQUA.toString() + ChatColor.BOLD + "LV. " + ChatColor.AQUA + playerLevel;

        String separator = ChatColor.WHITE + " - ";

        //  GENERATE HP STRING  //
        ChatColor hpColor = ChatColor.RED;
        if (GameAPI.isInSafeRegion(player.getLocation())) {
            hpColor = ChatColor.GREEN;
        } else if (GameAPI.isNonPvPRegion(player.getLocation())) {
            hpColor = ChatColor.YELLOW;
        }
        String playerHPInfo = hpColor.toString() + ChatColor.BOLD + "HP " + hpColor + currentHP
                + (currentHP != maxHP ? ChatColor.BOLD + " / " + hpColor + (int) maxHP : "");

        //  GENERATE XP STRING  //
        double exp = (double) pw.getExperience() / (double) pw.getEXPNeeded();
        exp *= 100;

        String playerEXPInfo = ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "XP " + ChatColor.LIGHT_PURPLE + (int) exp + "%";
        if (playerLevel == 100)
            playerEXPInfo = ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "MAX";

        //  DISPLAY THE BAR  //
        BossBarAPI.removeAllBars(player);
        BossBarAPI.addBar(player, new TextComponent("    " + playerLevelInfo + separator + playerHPInfo + separator + playerEXPInfo), BossBarAPI.Color.valueOf(hpColor.name()), getStyle(maxHP), healthPercent);

        //  UPDATE HP FOR OTHER PLAYERS  //
        int finalHp = currentHP;
        DungeonRealms.getInstance().getServer().getScheduler().runTask(DungeonRealms.getInstance(), () -> ScoreboardHandler.getInstance().updatePlayerHP(player, finalHp));
    }

    private static Style getStyle(double maxHealth) {
        int[] healthInc = new int[]{0, 500, 1000, 3000, 5000};
        Style[] styles = new Style[]{Style.PROGRESS, Style.NOTCHED_6, Style.NOTCHED_10, Style.NOTCHED_12, Style.NOTCHED_20};

        for (int i = 1; i < healthInc.length; i++)
            if (healthInc[i] > maxHealth)
                return styles[i - 1];

        return styles[styles.length - 1];
    }

    /**
     * Reloads the player HP from armor.
     * Called after a death or whenever a related inventory click occurs.
     */
    public static void updatePlayerHP(Player player) {
        calculateHP(player);

        // Recalculates HP regen.
        Metadata.HP_REGEN.remove(player);
        getRegen(player);
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

            if (getHP(player) <= 0 && player.getHealth() <= 0)
                continue;

            if (player.hasMetadata("starving"))
                continue;

            PlayerWrapper pw = PlayerWrapper.getWrapper(player);
            if (pw == null || !pw.isAttributesLoaded())
                continue;

            heal(player, getRegen(player), false);
        }
    }

    public static void trackHeal(Player entity) {
        HealTracker tracker = healTracker.get(entity.getUniqueId());
        if (tracker == null) {
            tracker = new HealTracker(entity);
            healTracker.put(entity.getUniqueId(), tracker);
        } else {
            tracker.trackHeal();
        }
    }

    public static boolean heal(Entity e, int amount, boolean showDebug) {
        int currentHP = getHP(e);
        int maxHP = getMaxHP(e);
        if (currentHP >= maxHP || amount <= 0)
            return false; // Don't bother

        if (amount > 0 && e instanceof Player) {
            //Check alignment for heal decrease?
            HealTracker tracker = healTracker.get(e.getUniqueId());
            if (tracker != null && tracker.isOnCooldown()) {
                amount *= tracker.getHealMultiplier();
            }
        }

        setHP(e, currentHP + amount);

        // Show message to players only.
        if (!(e instanceof Player))
            return true;

        int newHP = getHP(e);
        if (showDebug)
            PlayerWrapper.getWrapper((Player) e).sendDebug(ChatColor.GREEN + "        +" + (newHP - currentHP)
                    + ChatColor.BOLD + " HP" + ChatColor.GRAY + " [" + newHP + "/" + maxHP + "HP]");

        return true;
    }

    /**
     * Damages an entity.
     * Does not perform certain calculations.
     */
    public static void damageEntity(Entity e, int dmg) {
        AttackResult ar = new AttackResult((LivingEntity) e, null);
        ar.setDamage(dmg);
        damageEntity(ar);
    }

    /**
     * Damages an entity.
     */
    public static void damageEntity(AttackResult res) {
        if (res.getDefender().isPlayer()) {
            damagePlayer(res);
        } else {
            damageMonster(res);
        }
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
        double damage = res.getDamage();

        if (!res.getDefender().getWrapper().isVulnerable())
            return;

        boolean isReflectedDamage = res.getResult() == DamageResultType.REFLECT;

        if (player.isDead()) {
            return;
        }
        if (damage < 0) {
            Utils.log.info("Negative damage dealt to " + player.getName() + " Damager: " + attacker.getName() + " Damage: " + damage);
            damage = 1;
        }

        double currentHP = getHP(player);
        double newHP = currentHP - damage;
        PlayerWrapper defender = PlayerWrapper.getWrapper(player);
        if (cause == null || cause != EntityDamageEvent.DamageCause.FALL) {
            if (updateCombat) {
                if (!(attacker instanceof Player)) {
                    CombatLog.updateCombat(player);
                } else {
                    CombatLog.updatePVP(player);
                }
            }
        }

        if (attacker != null)
            if (attacker instanceof Projectile)
                attacker = (LivingEntity) ((Projectile) attacker).getShooter();

        if (GameAPI.isPlayer(attacker)) {
            if (newHP <= 0 && DuelingMechanics.isDueling(player.getUniqueId())) {
                newHP = 1;
                DuelOffer offer = DuelingMechanics.getOffer(player.getUniqueId());
                if (offer != null) {
                    player.setMetadata("duelCooldown", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis() + 1000));
                    ((Player) attacker).setMetadata("duelCooldown", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis() + 1000));
                    offer.endDuel((Player) attacker, player);
                }
                return;
            }

            PlayerWrapper pw = PlayerWrapper.getWrapper((Player) attacker);

            if (!DuelingMechanics.isDuelPartner(player.getUniqueId(), attacker.getUniqueId())) {
                if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK && !isReflectedDamage)
                    if (!(attacker.hasMetadata("duelCooldown") && attacker.getMetadata("duelCooldown").size() > 0 && attacker.getMetadata("duelCooldown").get(0).asLong() > System.currentTimeMillis()))
                        KarmaHandler.update((Player) attacker);

                if (newHP <= 0 && pw.getToggles().getState(Toggles.CHAOTIC_PREVENTION) && defender.getAlignment() == EnumPlayerAlignments.LAWFUL) {
                    player.setFireTicks(0);
                    player.getActivePotionEffects().clear();
                    newHP = 1;
                    attacker.sendMessage(ChatColor.YELLOW + "Your Chaotic Prevention Toggle has activated preventing the death of " + player.getName() + "!");
                    player.sendMessage(ChatColor.YELLOW + attacker.getName() + " has their Chaotic Prevention Toggle ON, your life has been spared!");
                }
            } else if (newHP <= 1) {
                //DONT KILL!!!!
                Bukkit.getLogger().info("Setting hp to 1 instead of " + newHP + " to prevent death.");
                newHP = 1;
            }

            if (!isReflectedDamage) {
                //Track this player damage for when we die.
                player.setMetadata("lastPlayerToDamageExpire", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis() + 3000));
                player.setMetadata("lastPlayerToDamage", new FixedMetadataValue(DungeonRealms.getInstance(), attacker.getName()));
            }

            pw.sendDebug(ChatColor.RED + "     " + (int) Math.ceil(damage) + ChatColor.BOLD + " DMG" + ChatColor.RED + " -> " + ChatColor.RED + player.getName() + ChatColor.RED + " [" + (int) newHP + ChatColor.BOLD + "HP]");

        }

        player.playSound(player.getLocation(), Sound.ENCHANT_THORNS_HIT, 1F, 1F);

        if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            defender.sendDebug(ChatColor.RED + "     -" + (int) Math.ceil(damage) + ChatColor.BOLD + " HP" + ChatColor.GRAY + " [-"
                    + (int) res.getTotalArmor() + "%A -> -" + (int) res.getTotalArmorReduction() + ChatColor.BOLD + "DMG" +
                    ChatColor.GRAY
                    + "]" + ChatColor.GREEN + " [" + (int) newHP + ChatColor.BOLD + "HP" + ChatColor.GREEN + "]");
        } else { // foreign damage
            DamageType type = DamageType.getByReason(cause);
            defender.sendDebug(ChatColor.RED + "     -" + (int) Math.ceil(damage) + ChatColor.BOLD + " HP " + type.getDisplay() + ChatColor.GREEN + " [" + (int) newHP + ChatColor.BOLD + "HP" + ChatColor.GREEN + "]");
        }

        if (res.hasProjectile() && res.getDefender().isPlayer()) {
            //Knockback?
            Player pl = res.getDefender().getPlayer();

            res.getDefender().getPlayer().playEffect(EntityEffect.HURT);
            Projectile shot = res.getProjectile();

            if (shot instanceof Arrow) {
                shot.remove();
                EntityHuman human = ((CraftPlayer) pl).getHandle();
                human.k(human.bY() + 1);
                ((AbstractProjectile) shot).getHandle().a(SoundEffects.t, 1.0F, 1.2F / (ThreadLocalRandom.current().nextFloat() * 0.2F + 0.9F));
            }

            DamageAPI.knockbackEntityVanilla(res.getDefender().getPlayer(), .25F, res.getAttacker().getEntity().getLocation());
        }

        Random r = ThreadLocalRandom.current();
        player.getWorld().spawnParticle(Particle.BLOCK_DUST, player.getLocation().clone().add(0, 1, 0), 10,
                r.nextGaussian(), r.nextGaussian(), r.nextGaussian(), 152);

        //  DISABLE DEATH FROM FIRE TICK  //
        if (cause == DamageCause.FIRE_TICK && newHP <= 0)
            return;

        if(player.isDead()) return;

        if (newHP <= 0 && handlePlayerDeath(player, attacker))
            return;

        setHP(player, (int) newHP);

        if (attacker != null && attacker.getType() != EntityType.PLAYER) {
            EntityMechanics.MONSTER_LAST_ATTACK.put(attacker, 15);
            if (!EntityMechanics.MONSTERS_LEASHED.contains(attacker))
                EntityMechanics.MONSTERS_LEASHED.add(attacker);
        }
    }

    public static boolean handlePlayerDeath(Player player, LivingEntity leAttacker) {
        Chat.listenForMessage(player, null);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 1f);

        //  PERMA DEATH ON EVENT SHARDS  //
        if (DungeonRealms.isEvent()) {
            if (Rank.isTrialGM(player)) {
                player.sendMessage(ChatColor.GOLD + "Death has acknowledged your identity and chosen to spare your life.");
            } else {
                Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
                    if (player.isOnline())
                        player.kickPlayer(ChatColor.RED + "You have been eliminated from this event.");
                    PunishAPI.ban(player.getUniqueId(), player.getName(), 0, -1, "You have been eliminated", null);
                }, 5);
            }
        }

        if (player.hasMetadata("lastDeathTime") && System.currentTimeMillis() - player.getMetadata("lastDeathTime").get(0).asLong() <= 500)
            return false;

        if (!(leAttacker instanceof Player)) {
            Player damagerKiller = getKillerFromRecentDamage(player);
            if (damagerKiller != null)
                leAttacker = damagerKiller;
        }

        String killerName = "";
        String deathMessage = "";

        PlayerWrapper dead = PlayerWrapper.getWrapper(player);
        String deadPlayerName = dead.getChatName();
        deathMessage = deadPlayerName + ChatColor.WHITE + " was killed by ";

        if (leAttacker instanceof Player) {
            PlayerWrapper killer = PlayerWrapper.getWrapper((Player) leAttacker);
            killerName = killer.getChatName();

            if (Achievements.hasAchievement(player.getUniqueId(), EnumAchievements.INFECTED))
                Achievements.giveAchievement(killer.getPlayer(), EnumAchievements.INFECTED);

            ItemStack item = player.getInventory().getItemInMainHand();
            String suffix = item != null ? " with a(n) " + Utils.getItemName(item) : "";

            deathMessage += killerName + ChatColor.WHITE + suffix;
        } else if (leAttacker != null && Metadata.CUSTOM_NAME.has(leAttacker)) {
            deathMessage += ChatColor.GRAY + Metadata.CUSTOM_NAME.get(leAttacker).asString();
        } else {
            deathMessage += "The World";
        }

        deathMessage += ChatColor.WHITE + ".";

        //  ANNOUNCE MESSAGE  //
        final String finalDeathMessage = deathMessage;
        GameAPI.getNearbyPlayers(player.getLocation(), 100).forEach(p -> p.sendMessage(finalDeathMessage));

        GamePlayer gp = GameAPI.getGamePlayer(player);
        gp.setPvpTaggedUntil(0);

        final LivingEntity finalKiller = leAttacker;
        player.setMetadata("lastDeathTime", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
            player.damage(player.getMaxHealth());
            if (finalKiller != null)
                KarmaHandler.handlePlayerPsuedoDeath(player, finalKiller);

            PlayerWrapper.getWrapper(player).calculateAllAttributes();
            CombatLog.removeFromCombat(player);
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
                if (onlineKiller != null)
                    return onlineKiller;
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
     * Damages a monster.
     */
    public static void damageMonster(AttackResult res) {
//        System.out.println("Damage -> Monster = " + res.getDamage());
        if (res.getDamage() <= 0)
            return;

        LivingEntity defender = res.getDefender().getEntity();
        LivingEntity attacker = res.getAttacker().getEntity();
        double damage = res.getDamage();

        boolean isDPSDummy = EnumEntityType.DPS_DUMMY.isType(defender);

        if (!EntityAPI.isMonster(defender) && !isDPSDummy) {
            System.out.println("Not living.");
            return;
        }


        double maxHP = isDPSDummy ? 0 : getMaxHP(defender);
        double currentHP = isDPSDummy ? 0 : getHP(defender);
        double newHP = currentHP - damage;

        if (currentHP <= 0 && !isDPSDummy) {
            if (!defender.isDead())
                defender.setHealth(0);
            return;
        }

        defender.playEffect(EntityEffect.HURT);

        if (Utils.randChance(7))
            ParticleAPI.spawnBlockParticles(defender.getLocation().clone().add(0, 1, 0), Material.REDSTONE_BLOCK);

        if (attacker != null && GameAPI.isPlayer(attacker)) {
            handleMonsterDamageTracker(defender.getUniqueId(), (Player) attacker, damage);
            checkForNewTarget(defender);

            if (isDPSDummy) {
                DPSDummy dummy = ItemDPSDummy.dpsDummies.get(defender);
                if (dummy != null) {
                    dummy.trackDamage(attacker.getUniqueId(), damage);
                }
            }
            if (!defender.hasMetadata("uuid")) {
                PlayerWrapper atk = PlayerWrapper.getWrapper((Player) attacker);
                String customNameAppended = Metadata.CUSTOM_NAME.get(defender).asString();
                ChatColor npcTierColor = GameAPI.getTierColor(EntityAPI.getTier(defender));
                atk.sendDebug(ChatColor.RED + "     " + (int) Math.ceil(damage) + ChatColor.BOLD + " DMG" + ChatColor.RED + " -> " + ChatColor.GRAY + npcTierColor + customNameAppended + npcTierColor + " [" + (int) (newHP < 0 ? 0 : newHP) + "HP]");
            }
        }

        if (newHP <= 0 && !isDPSDummy) {
            //  KILL ENTITY  //
            defender.playEffect(EntityEffect.DEATH);
            setHP(defender, 0);
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

            EntityMechanics.MONSTER_LAST_ATTACK.remove(defender);
            EntityMechanics.MONSTERS_LEASHED.remove(defender);

            return;
        }


        if (isDPSDummy) return;

        setHP(defender, (int) newHP);

        //  TO VANILLA HP  //
        double monsterHPPercent = (newHP / maxHP);
        double newMonsterHPToDisplay = monsterHPPercent * defender.getMaxHealth();
        int convHPToDisplay = (int) newMonsterHPToDisplay;
        convHPToDisplay = Math.max(1, convHPToDisplay);
        convHPToDisplay = Math.min(convHPToDisplay, (int) defender.getMaxHealth());

        if (EntityAPI.isMonster(defender)) {
            EntityAPI.showHPBar(EntityAPI.getMonster(defender));
            defender.setHealth(convHPToDisplay);
            if (!EntityMechanics.MONSTERS_LEASHED.contains(defender))
                EntityMechanics.MONSTERS_LEASHED.add(defender);
        }
    }

    /**
     * Calculates an entities max HP from their gear.
     */
    public static void calculateHP(LivingEntity entity) {
        int totalHP = 50; // base hp

        // Apply armor boost.
        totalHP += EntityAPI.getAttributes(entity).getAttribute(ArmorAttributeType.HEALTH_POINTS).getValue();
        totalHP = (int) (totalHP + (totalHP * (EntityAPI.getAttributes(entity).getAttribute(ArmorAttributeType.VITALITY).getValue() * 0.0003)));

        double[] hostileModifier = new double[]{.9, 1.1, 1.3, 1.6, 2};
        double[] eliteModifier = new double[]{1.5, 1.9, 2, 3, 4};

        // Apply monster boosts.
        int tier = EntityAPI.getTier(entity);
        if (tier > 0) {

            //  HOSTILE MODIFIER  //
            if (EnumEntityType.HOSTILE_MOB.isType(entity))
                totalHP *= hostileModifier[tier - 1];

            //  ELITE MODIFIER  //
            if (EntityAPI.isElite(entity))
                totalHP *= eliteModifier[tier - 1];

            //  BOSS MULTIPLIER  //
            if (EntityAPI.isBoss(entity))
                totalHP *= 6;
        }

        setMaxHP(entity, totalHP);
    }
}