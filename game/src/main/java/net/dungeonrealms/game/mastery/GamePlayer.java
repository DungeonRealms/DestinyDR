package net.dungeonrealms.game.mastery;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.donation.Buff;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.handler.ProtectionHandler;
import net.dungeonrealms.game.handler.ScoreboardHandler;
import net.dungeonrealms.game.mechanic.data.EnumBuff;
import net.dungeonrealms.game.player.chat.GameChat;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.statistics.PlayerStatistics;
import net.dungeonrealms.game.player.stats.PlayerStats;
import net.dungeonrealms.game.title.TitleAPI;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;

/**
 * Created by Nick on 10/19/2015.
 */
@Getter
@Setter
public class GamePlayer {

    private boolean sharding = false;

    @Getter
    private PlayerStats stats;
    private PlayerStatistics playerStatistics;
    
    private Player player;

    /**
     * Attribute values and their values
     */
    private AttributeList attributes = new AttributeList();

    @Getter
    private ArrayList<String> ignoredPlayers = new ArrayList<>();
    private boolean attributesLoaded;
    private String currentWeapon; // used so we only reload weapon stats when we need to.

    private int playerEXP;

    // Game Master
    private boolean isInvulnerable;
    private boolean isTargettable;
    private boolean isStreamMode;

    private boolean isJailed;

    private boolean ableToDrop;
    private boolean ableToSuicide;
    private boolean ableToOpenInventory;

    private String lastMessager;

    // for forcefield
    private long pvpTaggedUntil;

    @SuppressWarnings("unchecked")
	public GamePlayer(Player player) {
        this.player = player;
        this.stats = new PlayerStats(player.getUniqueId());
        this.playerStatistics = new PlayerStatistics(player.getUniqueId());
        GameAPI.GAMEPLAYERS.put(player.getName(), this);
        this.playerEXP = (int) DatabaseAPI.getInstance().getData(EnumData.EXPERIENCE, player.getUniqueId());
        this.isTargettable = true;
        this.isInvulnerable = false;
        this.isStreamMode = false;
        this.lastMessager = null;
        this.pvpTaggedUntil = 0;
        
        
        Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> {
            this.ignoredPlayers = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.IGNORED, player.getUniqueId());
            if (this.ignoredPlayers == null) this.ignoredPlayers = new ArrayList<>();
        });
    }

    /**
     * Get the players tier.
     *
     * @return The players Tier
     * @since 1.0
     */
    public Tier getPlayerTier() {
        int level = getLevel();
        if (level >= 1 || level <= 9) {
            return Tier.TIER1;
        } else if (level >= 10 || level <= 19) {
            return Tier.TIER2;
        } else if (level >= 20 || level <= 29) {
            return Tier.TIER3;
        } else if (level >= 30 || level <= 39) {
            return Tier.TIER4;
        } else if (level >= 40 || level <= 100) {
            return Tier.TIER5;
        } else
            return Tier.TIER1;
    }

    /**
     * Checks if player is in party.
     *
     * @return
     * @since 1.0
     */
    public boolean isInParty() {
        return Affair.getInstance().isInParty(getPlayer());
    }

    /**
     * Simple Tiers
     *
     * @since 1.0
     */
    enum Tier {
        TIER1,
        TIER2,
        TIER3,
        TIER4,
        TIER5,
    }

    /**
     * Gets the players level
     *
     * @return the level
     * @since 1.0
     */
    public int getLevel() {
        return stats.getLevel();
    }
    
    public void updateWeapon() {
    	ItemStack item = getPlayer().getInventory().getItemInMainHand();
    	if (item == null)
    		return;
    	String epoch = AntiDuplication.getUniqueEpochIdentifier(item);
    	if(epoch != null && !epoch.equals(getCurrentWeapon()))
    		calculateAllAttributes();
    }
    
    /**
     * Recalculate all player attributes based on gear.
     */
    public void calculateAllAttributes() {
    	//  CALCULATE FROM ITEMS  //
        getAttributes().addStats(getPlayer().getInventory().getItemInMainHand());
        for (ItemStack armor : getPlayer().getInventory().getArmorContents())
        	getAttributes().addStats(armor);
        
        setCurrentWeapon(AntiDuplication.getUniqueEpochIdentifier(getPlayer().getEquipment().getItemInMainHand()));

        for(Stats stat : Stats.values())
        	getAttributes().addStat(stat.getType(), getStats().getStat(stat));

        // apply stat bonuses (str, dex, int, and vit)
        getAttributes().applyStatBonuses();

        if (!isAttributesLoaded())
            HealthHandler.getInstance().handleLoginEvents(getPlayer());

        // so energy regen doesn't start before attributes have been loaded
        setAttributesLoaded(true);
    }

    /**
     * Gets the players experience.
     *
     * @return the experience
     * @since 1.0
     */
    public int getExperience() {
        return playerEXP;
    }

    /**
     * Checks if the player is in a Dungeon
     *
     * @return Is player in Dungeon?
     */
    public boolean isInDungeon() {
        return getPlayer().getWorld().getName().contains("DUNGEON");
    }

    /**
     * Gets the players current alignment.
     *
     * @return the alignment
     * @since 1.0
     */
    public KarmaHandler.EnumPlayerAlignments getPlayerAlignment() {
        return KarmaHandler.getInstance().getPlayerRawAlignment(getPlayer());
    }

    public KarmaHandler.EnumPlayerAlignments getPlayerAlignmentDB() {
        return KarmaHandler.EnumPlayerAlignments.getByName(String.valueOf(DatabaseAPI.getInstance().getData(EnumData.ALIGNMENT, getPlayer().getUniqueId())));
    }

    /**
     * Gets the player's current HP.
     * Defaults to 50 if not set.
     */
    public int getHP() {
    	return getPlayer().hasMetadata("currentHP") ? getPlayer().getMetadata("currentHP").get(0).asInt() : 50;
    }
    
    /**
     * Gets the player's max HP.
     */
    public int getMaxHP() {
        return HealthHandler.getMaxHP(getPlayer());
    }

    /**
     * Sets the player's max HP.
     */
    public void setPlayerMaxHP(int maxHP) {
        getPlayer().setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), maxHP));
    }

    /**
     * Sets the player's current HP.
     */
    public void setPlayerHP(int hp) {
        getPlayer().setMetadata("currentHP", new FixedMetadataValue(DungeonRealms.getInstance(), hp));
    }
    
    public int getEcashBalance() {
        return (int) DatabaseAPI.getInstance().getData(EnumData.ECASH, getPlayer().getUniqueId());
    }

    public int getEXPNeeded(int level) {
        if (level >= 101) {
            return 0;
        }
        double difficulty = 1;
        if (level >= 1 && level < 40) {
            difficulty = 1.3;
        } else if (level >= 40 && level < 60) {
            difficulty = 1.6;
        } else if (level >= 60 && level < 80) {
            difficulty = 2.2;
        } else if (level >= 80) {
            difficulty = 2.6;
        }
//        return (int) (100 * Math.pow(level, 2.24)); old level exp formula
        return (int) ((100 * Math.pow(level, 2)) * difficulty + 500); // patch 1.9 exp formula
    }


    /**
     * Add experience to the player
     *
     * @param experienceToAdd the amount of experience to add.
     * @apiNote Will automagically level the player up if the experience is enough.
     * @since 1.0
     */
    public void addExperience(int experienceToAdd, boolean isParty, boolean displayMessage) {
        int level = getLevel();
        if (level >= 100) return;
        int experience = getExperience();
        String expPrefix = ChatColor.YELLOW.toString() + ChatColor.BOLD + "        + ";
        if (isParty) {
            expPrefix = ChatColor.YELLOW.toString() + ChatColor.BOLD + "            " + ChatColor.AQUA.toString() + ChatColor.BOLD + "P " + ChatColor.RESET + ChatColor.GRAY + " >> " + ChatColor.YELLOW.toString() + ChatColor.BOLD + "+";
        }

        // Bonuses
        int expBonus = 0;
        if (Rank.isSubscriberPlus(getPlayer())) {
            expBonus = (int) (experienceToAdd * 0.1);
        } else if (Rank.isSubscriber(getPlayer())) {
            expBonus = (int) (experienceToAdd * 0.05);
        }
        //

        int futureExperience = experience + experienceToAdd + expBonus;
        int levelBuffBonus = 0;
        
        if (DonationEffects.getInstance().hasBuff(EnumBuff.LEVEL)) {
        	Buff levelBuff = DonationEffects.getInstance().getBuff(EnumBuff.LEVEL);
            levelBuffBonus = Math.round(experienceToAdd * (levelBuff.getBonusAmount() / 100f));
            experienceToAdd += levelBuffBonus;
        }
        int xpNeeded = getEXPNeeded(level);
        if (futureExperience >= xpNeeded) {
            int continuedExperience = futureExperience - xpNeeded;
            updateLevel(level + 1, true, false);
            addExperience(continuedExperience, isParty, displayMessage);
        } else {
            setPlayerEXP(futureExperience);
            if (displayMessage) {
                if ((boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, getPlayer().getUniqueId())) {
                    getPlayer().sendMessage(expPrefix + ChatColor.YELLOW + Math.round(experienceToAdd) + ChatColor.BOLD + " EXP " + ChatColor.GRAY + "[" + Math.round(futureExperience - expBonus - levelBuffBonus) + ChatColor.BOLD + "/" + ChatColor.GRAY + Math.round(getEXPNeeded(level)) + " EXP]");
                    if (expBonus > 0) {
                        getPlayer().sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "        " + GameChat.getRankPrefix(Rank.getInstance().getRank(getPlayer().getUniqueId()).toLowerCase()) + ChatColor.RESET + ChatColor.GRAY + " >> " + ChatColor.YELLOW.toString() + ChatColor.BOLD + "+" + ChatColor.YELLOW + Math.round(expBonus) + ChatColor.BOLD + " EXP " + ChatColor.GRAY + "[" + Math.round(futureExperience - levelBuffBonus) + ChatColor.BOLD + "/" + ChatColor.GRAY + Math.round(getEXPNeeded(level)) + " EXP]");
                    }
                    if (levelBuffBonus > 0) {
                        getPlayer().sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "        " + ChatColor.GOLD
                                .toString() + ChatColor.BOLD + "XP BUFF >> " + ChatColor.YELLOW.toString() + ChatColor.BOLD
                                + "+" + ChatColor.YELLOW + Math.round(levelBuffBonus) + ChatColor.BOLD + " EXP " +
                                ChatColor.GRAY + "[" + Math.round(futureExperience) + ChatColor.BOLD + "/" +
                                ChatColor.GRAY + Math.round(getEXPNeeded(level)) + " EXP]");
                    }
                }
            }
        }
    }

    /**
     * Updates a player's level. Can be called for a natural level up or for
     * an artificial change of a player's level via /set level or other means.
     *
     * @param newLevel - the new level
     * @param levelUp  - if the level change is natural
     * @param levelSet - if the level change is set artificially
     */
    public void updateLevel(int newLevel, boolean levelUp, boolean levelSet) {
        setPlayerEXP(0);
        DatabaseAPI.getInstance().update(getPlayer().getUniqueId(), EnumOperators.$SET, EnumData.EXPERIENCE, 0, true);
        DatabaseAPI.getInstance().update(getPlayer().getUniqueId(), EnumOperators.$INC, EnumData.LEVEL, 1, true);

        if (levelUp) { // natural level up
            getStats().lvlUp();

            if (newLevel != getLevel()) return; // not a natural level up

            getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, .4F);
            getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1F);

            Firework firework = (Firework) getPlayer().getLocation().getWorld().spawnEntity(getPlayer().getLocation().clone(), EntityType.FIREWORK);
            FireworkMeta fireworkMeta = firework.getFireworkMeta();
            FireworkEffect effect = FireworkEffect.builder().flicker(true).withColor(Color.LIME).withFade(Color.WHITE).with(FireworkEffect.Type.BALL_LARGE).trail(true).build();
            fireworkMeta.addEffect(effect);
            fireworkMeta.setPower(1);
            firework.setFireworkMeta(fireworkMeta);

            getPlayer().sendMessage("");
            Utils.sendCenteredMessage(getPlayer(), ChatColor.AQUA.toString() + ChatColor.BOLD + "******************************");
            Utils.sendCenteredMessage(getPlayer(), ChatColor.GREEN.toString() + ChatColor.BOLD + "LEVEL UP");
            getPlayer().sendMessage("");
            Utils.sendCenteredMessage(getPlayer(), ChatColor.GRAY + "You are now level: " + ChatColor.GREEN + newLevel);
            Utils.sendCenteredMessage(getPlayer(), ChatColor.GRAY + "EXP to next level: " + ChatColor.GREEN + getEXPNeeded(newLevel));
            Utils.sendCenteredMessage(getPlayer(), ChatColor.GRAY + "Free stat points: " + ChatColor.GREEN + this.getStats().getFreePoints());
            Utils.sendCenteredMessage(getPlayer(), ChatColor.AQUA.toString() + ChatColor.BOLD + "******************************");
            getPlayer().sendMessage("");
        } else if (levelSet) { // level was set
            getStats().setPlayerLevel(newLevel);

            Utils.sendCenteredMessage(getPlayer(), ChatColor.YELLOW + "Your level has been set to: " + ChatColor.LIGHT_PURPLE + newLevel);
            getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 63f);
        }

        // update scoreboard
        ScoreboardHandler.getInstance().setPlayerHeadScoreboard(getPlayer(), getPlayerAlignment().getAlignmentColor(), newLevel);
        switch (newLevel) {
            case 10:
                Achievements.getInstance().giveAchievement(getPlayer().getUniqueId(), Achievements.EnumAchievements.LEVEL_10);
                break;
            case 25:
                Achievements.getInstance().giveAchievement(getPlayer().getUniqueId(), Achievements.EnumAchievements.LEVEL_25);
                break;
            case 50:
                Achievements.getInstance().giveAchievement(getPlayer().getUniqueId(), Achievements.EnumAchievements.LEVEL_50);
                break;
            case 100:
                Achievements.getInstance().giveAchievement(getPlayer().getUniqueId(), Achievements.EnumAchievements.LEVEL_100);
                break;
            default:
                break;
        }
    }

    /**
     * Get the factorial of a number.
     *
     * @param n
     * @return
     * @since 1.0
     */
    public int factorial(int n) {
        int output;
        if (n == 0) return 0;
        output = factorial(n - 1) * n;
        return output;
    }

    /**
     * Checks Document for boolean value
     *
     * @return boolean
     */
    public boolean hasShopOpen() {
        return (boolean) DatabaseAPI.getInstance().getData(EnumData.HASSHOP, getPlayer().getUniqueId());
    }

    public boolean hasNewbieProtection() {
        return ProtectionHandler.getInstance().hasNewbieProtection(getPlayer());
    }

    public void setInvulnerable(boolean flag) {
        if (CombatLog.isInCombat(getPlayer())) CombatLog.removeFromCombat(getPlayer());
        if (CombatLog.inPVP(getPlayer())) CombatLog.removeFromPVP(getPlayer());
        isInvulnerable = flag;
    }

    public void setPvpTaggedUntil(long time) {
        if (!isPvPTagged())
            TitleAPI.sendActionBar(getPlayer(), ChatColor.RED + "PvP Tagged - " + ChatColor.BOLD + "10s", 4 * 20);
        this.pvpTaggedUntil = time;
    }

    public void setStreamMode(boolean flag) {
        isStreamMode = flag;
    }

    public boolean isPvPTagged() {
        return pvpTaggedUntil > 0 && pvpTaggedUntil > System.currentTimeMillis();
    }

    public int secsPvPTaggedLeft() {
        return isPvPTagged() ? 0 : (int) (pvpTaggedUntil - System.currentTimeMillis()) / 1000;
    }
}
