package net.dungeonrealms.game.listener.combat;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.PlayerToggles.Toggles;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.dungeonrealms.game.world.item.Item.WeaponAttributeType;

@Getter @Setter
public class AttackResult {

	private CombatEntity attacker;
	private CombatEntity defender;
	private ItemWeapon weapon;
	private Projectile projectile;
	
	private double damage;

	private double totalArmor;
	private double totalArmorReduction;
	
	private DamageResultType result = DamageResultType.NORMAL;
	private DamageCause cause = DamageCause.ENTITY_ATTACK;
	
	public AttackResult(LivingEntity attacker, LivingEntity victim) {
		this(attacker, victim, null);
	}
	
	public AttackResult(LivingEntity attacker, LivingEntity victim, Projectile proj) {
		this.attacker = new CombatEntity(attacker);
		this.defender = new CombatEntity(victim);
		this.projectile = proj;
		
		loadAttributeFromMeta();
	}
	
	public boolean hasProjectile() {
		return getProjectile() != null;
	}
	
	public boolean hasWeapon() {
		return getWeapon() != null;
	}
	
	public double getWeightedDamage() {
		if(getResult() == DamageResultType.REFLECT)
			return getDamage();
		double dmg = getDamage();
		dmg -= getTotalArmorReduction();
		return Math.max(0, dmg);
	}
	
	public DamageCause getCause() {
		return getResult() == DamageResultType.REFLECT ? DamageCause.THORNS : this.cause;
	}
	
	/**
	 * Applies the damage from this.
	 * Call after performing calculations.
	 * 
	 * Handles creation of damage holograms.
	 */
	public void applyDamage() {
		LivingEntity receiver = getDefender().getEntity();
		String defenderName = Metadata.CUSTOM_NAME.get(receiver).asString();
        
    	Player hologramOwner = getAttacker().isPlayer() ? getAttacker().getPlayer() : (getDefender().isPlayer() ? (Player)receiver : null);

    	//Take into account elementalDamage.
		if (getResult() != DamageResultType.NORMAL) {
        	if (getAttacker() != null)
        		getAttacker().getEntity().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "                   *OPPONENT " + getResult().getPastTenseName() + "* (" + defenderName + ChatColor.RED + ChatColor.BOLD + ")");
        	
        	if (hologramOwner != null)
        		DamageAPI.createDamageHologram(hologramOwner, receiver.getLocation(), ChatColor.RED + "*" + getResult().name() + "*");
        	receiver.getWorld().playSound(receiver.getLocation(), getResult().getSound(), getResult().getVolume(), getResult().getPitch());
        	
        	//  REFLECT  //
        	if(getResult() == DamageResultType.REFLECT) {
        		AttackResult res = new AttackResult(receiver, getAttacker().getEntity());
        		res.setDamage(EntityAPI.isBoss(receiver) || EntityAPI.isElite(receiver) ? getDamage() * 0.4 : getDamage());
        		HealthHandler.damageEntity(res);
        	}
        	return;
        }
        
        if (hologramOwner != null)
        	DamageAPI.createDamageHologram(hologramOwner, receiver.getLocation(), getDamage());

        HealthHandler.damageEntity(this);
	}

	public boolean checkChaoticPrevention() {
		PlayerWrapper receiver = PlayerWrapper.getWrapper(getDefender().getPlayer());
		PlayerWrapper damager = PlayerWrapper.getWrapper(getAttacker().getPlayer());
		Player defender = getDefender().getPlayer();
		Player attacker = getAttacker().getPlayer();
		
		if (getWeightedDamage() >= HealthHandler.getHP(defender) && receiver.getAlignment() == KarmaHandler.EnumPlayerAlignments.LAWFUL) {
            if (damager.getAlignment() != KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
                if (getAttacker().getWrapper().getToggles().getState(Toggles.CHAOTIC_PREVENTION)) {
                	defender.setFireTicks(0);
                	defender.getActivePotionEffects().clear();
                	
                	attacker.updateInventory();
                	attacker.sendMessage(ChatColor.YELLOW + "Your Chaotic Prevention Toggle has activated preventing the death of " + defender.getName() + "!");
                	defender.sendMessage(ChatColor.YELLOW + defender.getName() + " has their Chaotic Prevention Toggle ON, your life has been spared!");
                	
                	return true;
                }
            }
        }
		
		return false;
	}
	
	private void loadAttributeFromMeta() {
		if(!hasProjectile() || getAttacker().getEntity() == null)
			return;
		
		AttributeList attributes = new AttributeList();
		for (WeaponAttributeType attribute : WeaponAttributeType.values()) {
			String nbt = attribute.getNBTName();
			if (attribute.isRange()) {
				if (getProjectile().hasMetadata(nbt + "Min"))
					attributes.setStatRange(attribute, getProjectile().getMetadata(nbt + "Min").get(0).asInt(),
							getProjectile().getMetadata(nbt + "Max").get(0).asInt());
			} else {
				if (getProjectile().hasMetadata(nbt))
					attributes.setStat(attribute, getProjectile().getMetadata(nbt).get(0).asInt());
			}
		}
		// We don't use the actual attacker attributes when it's a projectile, so 
		// players can't switch their items and use stats from one item when another was used.
		getAttacker().setAttributes(attributes);
	}
	
	public class CombatEntity {
		
		@Getter private LivingEntity entity;
		@Setter @Getter private AttributeList attributes;
		
		public CombatEntity(LivingEntity le) {
			this.entity = le;
			setAttributes(le != null ? EntityAPI.getAttributes(le) : new AttributeList());
		}
		
		public boolean isPlayer() {
			return getEntity() instanceof Player;
		}
		
		public Player getPlayer() {
			assert isPlayer();
			return (Player) getEntity();
		}
		
		public PlayerWrapper getWrapper() {
			assert isPlayer();
			return PlayerWrapper.getPlayerWrapper(getPlayer());
		}
	}
}
