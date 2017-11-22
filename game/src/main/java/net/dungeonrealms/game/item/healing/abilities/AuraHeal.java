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
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class AuraHeal extends Healing {

    private long time = System.currentTimeMillis();

    @Override
    public boolean onAbilityUse(Player player, HealingAbility ability, ItemClickEvent event) {
        int radius = 9;

        GuildWrapper guild = GuildDatabase.getAPI().getPlayersGuildWrapper(player.getUniqueId());
        PlayerWrapper wrap = PlayerWrapper.getPlayerWrapper(player);

        Party party = Affair.getParty(player);
        String whoWasHealed = "";
        boolean affected = false;
        for (Entity nearby : player.getNearbyEntities(radius, radius, radius)) {
            if (nearby instanceof Player) {
                Player other = (Player) nearby;
                if(other instanceof EntityHumanNPC.PlayerNPC)continue;
                if (GameAPI._hiddenPlayers.contains(other)) continue;
                if (guild != null && guild.isMember(other.getUniqueId()) || party != null && party.isMember(other)) {
                    //Heal..
                    if (time <= System.currentTimeMillis()) {
                        HealingMap map = healingMap.get(other.getUniqueId());
                        if (map != null && !map.canHeal(player.getUniqueId())) {
                            continue;
                        }

                        double toRegen = HealthHandler.getMaxHP(other) * .15;

                        HealthHandler.heal(other, (int) toRegen, true, player.getName() + "'s " + ability.getName());

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

                        affected = true;
                        ParticleAPI.spawnParticle(Particle.HEART, other.getLocation().add(0, 1, 0), 30, 1F, .01F);
                        whoWasHealed += other.getName() + ", ";
                        time = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(6);
                    } else {
                        Utils.sendCenteredDebug(player, CC.DarkRedB + "YOU CANNOT USE AURA HEAL FOR ANOTHER " + TimeUnit.MILLISECONDS.toSeconds(time - System.currentTimeMillis()) + "s.");
                    }
                }
            }
        }

        if (affected) {
            CombatLog.addToPVP(player);
            MountUtils.removeMount(player);
        }

        if (!whoWasHealed.isEmpty()) {
            whoWasHealed = whoWasHealed.substring(0, whoWasHealed.length() - 2);
        } else {
            whoWasHealed = "None";
        }
        Utils.sendCenteredDebug(player, CC.YellowB + "AURA HEALED (" + CC.Yellow + whoWasHealed + CC.YellowB + ")");

        return true;
    }
}
