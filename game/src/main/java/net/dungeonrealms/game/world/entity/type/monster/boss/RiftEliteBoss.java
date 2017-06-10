package net.dungeonrealms.game.world.entity.type.monster.boss;

import lombok.Getter;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemArmorShield;
import net.dungeonrealms.game.listener.combat.AttackResult;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.dungeons.BossType;
import net.dungeonrealms.game.mechanic.dungeons.Dungeon;
import net.dungeonrealms.game.mechanic.dungeons.DungeonBoss;
import net.dungeonrealms.game.mechanic.dungeons.rifts.EliteRift;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.staff.StaffGiant;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.dungeonrealms.game.world.item.Item;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftHumanEntity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Rar349 6/8/2017
 */
public class RiftEliteBoss extends StaffGiant implements DungeonBoss {

	private int ourTier;
	@Getter
	private boolean isInAir = false;
	@Getter
    private BossStage stage = BossStage.NOTHING;
	@Getter
	private long lastStageSwitch = 0L;
	private boolean hasThrownPortal = false;

    public RiftEliteBoss(World world) {
        super(world);
        this.fireProof = true;
        this.stage = BossStage.NOTHING;
        lastStageSwitch = System.currentTimeMillis();
    }


    @Override
    public void onBossAttacked(Player player) {

    }

    public void setOurTier(int newTier) {
        this.ourTier = newTier;
        super.setTier(newTier);
    }

    @Override
    public BossType getBossType() {
        return BossType.RiftEliteBoss;
    }

    @Override
    public int getTier() {
        return ourTier;
    }

    @Override
    public EnumMonster getEnum() {
        return EnumMonster.RiftElite;
    }

    @Override
    public void setArmor() {
        ItemArmor armor = (ItemArmor) new ItemArmor().setRarity(Item.ItemRarity.RARE).setTier(ourTier).setGlowing(true);
        ItemStack[] gear = armor.generateArmorSet();
        getBukkit().getEquipment().setArmorContents(gear);
        getBukkit().getEquipment().setItemInMainHand(new ItemArmorShield().setTier(ourTier).setRarity(Item.ItemRarity.getRandomRarity(true)).setGlowing(true).generateItem());
        getBukkit().getEquipment().setItemInOffHand(new ItemArmorShield().setTier(ourTier).setRarity(Item.ItemRarity.getRandomRarity(true)).setGlowing(true).generateItem());
    }


    @Override
    public void collide(Entity e) {}

    @Override
    public void a(EntityLiving entityLiving, float v) {
        //Don't shoot. We are just extending staff giant so that he doesn't path right on top of us. He needs to target us but doesn't need to attack us.
    }

    @Override
    public void n() {
        super.n();
        if(isInAir && getBukkit().isOnGround()) {
            handleGroundStomp();
            isInAir = false;
        }

        if(shouldSwitchStages()) switchStage(getNextStage());
        if(shouldThrowBlackHoles()) throwBlackHoles(getNumberOfBlackHolesToThrow());
    }

    public Player getTarget() {
        try {
            EntityLiving living = getGoalTarget();
            CraftHumanEntity humanEnt = ((EntityHuman) living).getBukkitEntity();
            return (Player) humanEnt;
        } catch(Exception e) {
            return null;
        }
    }

    public void handleGroundStomp() {
        Block under = getBukkit().getLocation().clone().subtract(0,1,0).getBlock();
        for(int blocks = 0; blocks < 25; blocks++) {
            FallingBlock block = getBukkit().getWorld().spawnFallingBlock(getBukkit().getLocation().clone().add(0,1,0), under.getType(),under.getData());
            block.setVelocity(new Vector(ThreadLocalRandom.current().nextDouble(-1,1),0.5,ThreadLocalRandom.current().nextDouble(-1,1)).multiply(4));
            block.setHurtEntities(false);
            block.setDropItem(false);
            block.setInvulnerable(true);
        }
        knockbackNearbyPlayers();
    }

    private boolean shouldThrowBlackHoles() {
        EliteRift rift = (EliteRift) getDungeon();
        return getStage().equals(BossStage.BLACK_HOLE) && !hasThrownPortal && rift.getBlackHoles().isEmpty() && System.currentTimeMillis() - lastStageSwitch > 7000;
    }

    private int getNumberOfBlackHolesToThrow() {
        double maxHealth = HealthHandler.getMaxHP(getBukkit());
        double currentHealth = HealthHandler.getHP(getBukkit());

        double percent = 100 * (currentHealth / maxHealth);
        int toReturn = (int)(4 - (percent / 30));
        if(toReturn < 1) toReturn = 1;
        return toReturn;
    }

    private boolean shouldSwitchStages() {
        return System.currentTimeMillis() - lastStageSwitch >= TimeUnit.SECONDS.toMillis(45);
    }

    private void throwBlackHoles(int numberToThrow) {
        if(!getStage().equals(BossStage.BLACK_HOLE)) return;

        a(EnumHand.MAIN_HAND);
        playSound(Sound.ENTITY_WITHER_SHOOT, 3F, 1.4F);
        EliteRift rift = (EliteRift)getDungeon();
        double maxY = 1;
        double minY = 0.5;
        for(int blocks = 0; blocks < numberToThrow; blocks++) {
            FallingBlock block = getBukkit().getWorld().spawnFallingBlock(getBukkit().getLocation().clone().add(0,1,0), Material.COAL_BLOCK,(byte)0);
            Vector vec = rift.getMap().getCenterLocation().toVector().subtract(block.getLocation().toVector());
            vec.setY(ThreadLocalRandom.current().nextDouble(minY,maxY));
            maxY -= 0.25;
            minY -= 0.15;
                if(vec.length() != 0) vec.normalize();
                vec.multiply(ThreadLocalRandom.current().nextDouble(1,4));
                vec.add(new Vector(ThreadLocalRandom.current().nextDouble(-1,1), 0, ThreadLocalRandom.current().nextDouble(-1,1)));
            //block.setVelocity(new Vector(ThreadLocalRandom.current().nextDouble(-1,1),0.5,ThreadLocalRandom.current().nextDouble(-1,1)).multiply(1.3));
            block.setVelocity(vec);
            block.setHurtEntities(false);
            block.setDropItem(false);
            block.setInvulnerable(true);
        }

        hasThrownPortal = true;
    }

    public void knockbackNearbyPlayers() {
        for (org.bukkit.entity.Entity near : getBukkit().getNearbyEntities(5, 5, 5)) {
            if (near == null || !(near instanceof Player)) continue;
            Player nearPlayer = (Player) near;//lemme get that compile
            AttackResult result = new AttackResult(getBukkit(), nearPlayer);
            result.setDamage(HealthHandler.getHP(nearPlayer) * .15);
            result.setTotalArmor(0);
            HealthHandler.damagePlayer(result);
            DamageAPI.knockbackEntity(getBukkit(), nearPlayer, 3);
        }
    }

    private BossStage getNextStage() {
        BossStage[] stages = BossStage.getBossStages();
        BossStage toReturn = stages[ThreadLocalRandom.current().nextInt(stages.length)];
        if(toReturn.equals(this.stage)) return getNextStage();
        return toReturn;
    }

    private void switchStage(BossStage stage) {
        EliteRift rift = (EliteRift) getDungeon();
        rift.repairBlocksNaturally(true);//Repair all lava blocks.
        rift.clearBlackHoles();
        if(stage.getToSay() != null) say(stage.getToSay());
        this.getBukkit().getEquipment().setItemInMainHand(new ItemStack(stage.getWeapon().getItemType(),1,stage.getWeapon().getData()));
        this.stage = stage;
        this.lastStageSwitch = System.currentTimeMillis();
        this.hasThrownPortal = false;
    }

    public void jump(int height) {
        getBukkit().setVelocity(new Vector(0,1,0).normalize().multiply(height));
        say("RAAARRRRGGHHHH!!!!!");
        isInAir = true;
    }


    @Getter
    public enum BossStage {
	    NOTHING("You can not defeat me!", new MaterialData(Material.SHIELD)),
        LAVA_TRAIL("You better run!", new MaterialData(Material.LAVA_BUCKET)),
        BLACK_HOLE("I WILL CONSUME YOU!!!", new MaterialData(Material.EYE_OF_ENDER));


	    private String toSay;
	    private MaterialData weapon;
	    BossStage(String toSay, MaterialData weapon) {
	        this.toSay = toSay;
	        this.weapon = weapon;
        }

        public static BossStage[] getBossStages() {
	        BossStage[] toReturn = new BossStage[BossStage.values().length - 1];
	        int counter = 0;
	        for(BossStage stage : BossStage.values()) {
	            if(stage.equals(BossStage.NOTHING)) continue;
	            toReturn[counter++] = stage;
            }

            return toReturn;
        }
    }
}