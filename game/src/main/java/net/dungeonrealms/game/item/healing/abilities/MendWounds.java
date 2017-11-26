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
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.item.CC;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class MendWounds extends Healing {

    @Override
    public boolean onAbilityUse(Player player, HealingAbility ability, ItemClickEvent event) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);

        Entity clicked = event.getClickedEntity();
        if (clicked == null || !(clicked instanceof Player) || clicked instanceof EntityHumanNPC.PlayerNPC) return false;

        if (GameAPI._hiddenPlayers.contains(clicked)) return false;

        GuildWrapper guild = GuildDatabase.getAPI().getPlayersGuildWrapper(player.getUniqueId());
        Party party = Affair.getParty(player);

        if (guild != null && guild.isMember(clicked.getUniqueId()) || party != null && party.isMember((Player) clicked)) {

            double toHealPercent = 30;

            HealingMap map = healingMap.get(clicked.getUniqueId());
            if (map != null && !map.canHeal(player.getUniqueId())) {
                return false;
            }

            double potency = wrapper.getAttributes().getAttribute(Item.ArmorAttributeType.POTENCY).getValue();

            double toIncrease = (potency * .01) * toHealPercent;

            toHealPercent += potency + toIncrease;

            double current = HealthHandler.getHP(clicked);

            double hpToHeal = toHealPercent * 0.01 * HealthHandler.getMaxHP(clicked);
            HealthHandler.heal(clicked, (int) hpToHeal, true, player.getName() + "'s " + ability.getName());

            if (wrapper.getAlignment() == KarmaHandler.EnumPlayerAlignments.LAWFUL) {
                PlayerWrapper wrap = PlayerWrapper.getPlayerWrapper((Player) clicked);
                if (wrap.getAlignment() == KarmaHandler.EnumPlayerAlignments.NEUTRAL || wrap.getAlignment() == KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
                    KarmaHandler.update(player);
                }
            }
            GamePlayer playerGP = GameAPI.getGamePlayer(player);

            KarmaHandler.update(player);
            playerGP.setPvpTaggedUntil(System.currentTimeMillis() + 1000 * 10L);
            CombatLog.addToPVP(player);
            MountUtils.removeMount(player);

            double newHP = HealthHandler.getHP(clicked);
            Utils.sendCenteredDebug(player, CC.YellowB + "MENDING WOUNDS (" + CC.Yellow + clicked.getName() + CC.YellowB + ")" + CC.GreenB + " + " + Math.ceil(hpToHeal) + "HP" + CC.Gray + " [" + format.format(current) + " -> " + format.format(newHP) + "]");
            ParticleAPI.spawnParticle(Particle.VILLAGER_HAPPY, clicked.getLocation().add(0, 1.75, 0), 10, .3F, .4F);
            if (map == null) {
                map = new HealingMap();
                healingMap.put(clicked.getUniqueId(), map);
            }

            map.heal(player.getUniqueId());
            return true;
        }
        return false;
    }
}
