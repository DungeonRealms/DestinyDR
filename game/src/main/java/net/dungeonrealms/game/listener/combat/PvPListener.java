package net.dungeonrealms.game.listener.combat;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.EntityHuman;
import net.minecraft.server.v1_9_R2.PacketPlayOutAnimation;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class PvPListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void playerAttackPlayer(EntityDamageByEntityEvent event) {
        boolean isProjectile = DamageAPI.isBowProjectile(event.getDamager()) || DamageAPI.isStaffProjectile(event.getDamager());
        boolean isPlayer = GameAPI.isPlayer(event.getDamager()) && GameAPI.isPlayer(event.getEntity());

        Projectile projectile = isProjectile ? (Projectile) event.getDamager() : null;

        //  DONT HANDLE IF IT'S NOT PLAYER VS PLAYER  //
        if (!isProjectile && !isPlayer)
            return;

        if (isProjectile && !(projectile.getShooter() instanceof Player)) return; //Shooter is not a player

        Player attacker = isProjectile ? (Player) projectile.getShooter() : (Player) event.getDamager();
        if (event.getEntity() instanceof Player) {
            Player defender = (Player) event.getEntity();

            // Projectiles can be knocked back into the player.
            if (attacker.equals(event.getEntity()))
                return;

            if (defender.getGameMode() != GameMode.SURVIVAL)
                return;

            boolean isDuel = DuelingMechanics.isDuelPartner(attacker.getUniqueId(), defender.getUniqueId());

            if (!isDuel)
                CombatLog.updatePVP(attacker);

            defender.playEffect(EntityEffect.HURT);
            //decrement the knockback?
            DamageAPI.knockbackPlayerPVP(attacker, defender, 0.3);

            EntityHuman defend = ((CraftPlayer) defender).getHandle();
            defender.setSprinting(false);

            GamePlayer damagerGP = GameAPI.getGamePlayer(defender);

            //Dont tag them if they are in a duel..
            if (!isDuel) {
                damagerGP.setPvpTaggedUntil(System.currentTimeMillis() + 1000 * 10L);
            } else {
                // Marks the player as not able to regen health while in a duel.
                defender.setMetadata("lastDamageTaken", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
            }
            //Make it seem like we are taking damage.
            GameAPI.getNearbyPlayersAsync(defender.getLocation(), 20).forEach(pl -> ((CraftPlayer) pl).getHandle().playerConnection.sendPacket(new PacketPlayOutAnimation(defend, 1)));
            event.setDamage(0.0D);
            event.setCancelled(true);

            ItemStack held = attacker.getEquipment().getItemInMainHand();
            AttackResult res = new AttackResult(attacker, defender);

            if (!isProjectile && !ItemWeapon.isWeapon(held)) {
                res.setDamage(1);
                res.applyDamage();
                return;
            }

            //  ALIGNMENT IGNORES DUELS  //
            if (!isDuel)
                if (!(attacker.hasMetadata("duelCooldown") && attacker.getMetadata("duelCooldown").get(0).asLong() > System.currentTimeMillis()))
                    KarmaHandler.update(attacker);

            DamageAPI.calculateWeaponDamage(res, !isDuel);

            if (!isDuel && res.checkChaoticPrevention())
                return;

            DamageAPI.calculateWeaponDamage(res, !isDuel);
            res.applyDamage();

            if (!isProjectile)
                DamageAPI.handlePolearmAOE(event, res.getDamage() / 2, attacker);
        }
    }
}
