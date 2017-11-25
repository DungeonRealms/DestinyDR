package net.dungeonrealms.game.item.healing.abilities;

import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.affair.party.Party;
import net.dungeonrealms.game.guild.GuildWrapper;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.healing.Healing;
import net.dungeonrealms.game.item.healing.HealingAbility;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class AuraHeal extends Healing {

    private long time = System.currentTimeMillis();

    @Override
    public boolean onAbilityUse(Player player, HealingAbility ability, ItemClickEvent event) {
        int radius = 9;

        GuildWrapper guild = GuildDatabase.getAPI().getPlayersGuildWrapper(player.getUniqueId());
        PlayerWrapper wrap = PlayerWrapper.getPlayerWrapper(player);

        boolean affected = false;

        Party party = Affair.getParty(player);
        if (time <= System.currentTimeMillis()) {
            for (Entity nearby : player.getNearbyEntities(radius, radius, radius)) {
                if (nearby instanceof Player) {
                    Player other = (Player) nearby;
                    if (!(other instanceof EntityHumanNPC.PlayerNPC) && !GameAPI._hiddenPlayers.contains(other)) {
                        if ((guild != null && guild.isMember(other.getUniqueId())) || (party != null && party.isMember(other))) {
                            //Heal..
                            HealingMap map = healingMap.get(other.getUniqueId());
                            if (map != null && !map.canHeal(player.getUniqueId())) {
                                continue;
                            }

                            int current = HealthHandler.getHP(other);
                            int toRegen = (int) (HealthHandler.getMaxHP(other) * .15);

                            HealthHandler.heal(other, toRegen, true, player.getName() + "'s " + ability.getName());

                            if (wrap.getAlignment() == KarmaHandler.EnumPlayerAlignments.LAWFUL) {
                                PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(other);
                                if (wrapper.getAlignment() == KarmaHandler.EnumPlayerAlignments.NEUTRAL || wrapper.getAlignment() == KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
                                    KarmaHandler.update(player);
                                }
                            }

                            if (map == null) {
                                map = new HealingMap();
                                healingMap.put(other.getUniqueId(), map);
                            }

                            map.heal(player.getUniqueId());
                            Utils.sendCenteredDebug(player, CC.YellowB + "MENDING WOUNDS (" + CC.Yellow + other.getName() + CC.YellowB + ")" + CC.GreenB + " + " + Math.ceil(toRegen) + "HP" + CC.Gray + " [" + format.format(current) + " -> " + format.format(HealthHandler.getHP(other)) + "]");

                            affected = true;
                            ParticleAPI.spawnParticle(Particle.HEART, other.getLocation().add(0, 1, 0), 30, 1F, .01F);
                            time = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(6);
                        }
                    }
                }
            }
        } else {
        Utils.sendCenteredDebug(player, CC.DarkRedB + "YOU CANNOT USE AURA HEAL FOR ANOTHER " + TimeUnit.MILLISECONDS.toSeconds(time - System.currentTimeMillis()) + "s.");
        }

        if (affected) {
            CombatLog.addToPVP(player);
            MountUtils.removeMount(player);
        }
        else {
            Utils.sendCenteredDebug(player, CC.YellowB + "AURA HEALED (" + CC.Yellow + "None" + CC.YellowB + ")");
        }
        return true;
    }
}