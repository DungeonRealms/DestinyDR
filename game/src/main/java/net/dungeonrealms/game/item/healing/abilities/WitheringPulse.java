package net.dungeonrealms.game.item.healing.abilities;

import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerToggles;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.affair.party.Party;
import net.dungeonrealms.game.guild.GuildWrapper;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.healing.Healing;
import net.dungeonrealms.game.item.healing.HealingAbility;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.item.CC;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.TimeUnit;

public class WitheringPulse extends Healing {

    @Override
    public boolean onAbilityUse(Player player, HealingAbility ability, ItemClickEvent event) {

        cooldown = 8;

        GuildWrapper guild = GuildDatabase.getAPI().getPlayersGuildWrapper(player.getUniqueId());

        Party party = Affair.getParty(player);

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        int potency = wrapper.getAttributes().getAttribute(Item.ArmorAttributeType.POTENCY).getValue();

        if (!wrapper.getToggles().getState(PlayerToggles.Toggles.PVP)) {
            player.sendMessage(ChatColor.RED + "You cannot use this ability while PVP is toggled off!");
            return false;
        }

        String withered = "";
        int radius = 5;

        boolean affected = false;
        if(!isOnCooldown()) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_BREAK_BLOCK, 1, .9F);
            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                if (entity instanceof Player) {
                    Player other = (Player) entity;
                    if (GameAPI._hiddenPlayers.contains(other)) continue;

                    if (other instanceof EntityHumanNPC.PlayerNPC) continue;

                    if (party != null && party.isMember(other) || guild != null && guild.isMember(other.getUniqueId()))
                        continue;

                    if (!GameAPI.isNonPvPRegion(other.getLocation())) {
                        DamageAPI.knockbackEntity(player, other, 1.8F);

                        int duration = (20 * 4);
                        duration += duration * (potency * .01);
                        other.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, duration, potency >= 100 ? 1 : 0));

                        affected = true;
                        withered += other.getName() + ", ";
                    }
                }
            }
            GamePlayer playerGP = GameAPI.getGamePlayer(player);
            if (affected) {
                KarmaHandler.update(player);
                playerGP.setPvpTaggedUntil(System.currentTimeMillis() + 1000 * 10L);
                MountUtils.removeMount(player);
            }

            if (withered.isEmpty())
                withered = "None";
            else
                withered = withered.substring(0, withered.length() - 2);
            Utils.sendCenteredDebug(player, CC.RedB + "WITHERED (" + CC.Red + withered + CC.RedB + ")");
        } else {
            Utils.sendCenteredDebug(player, CC.DarkRedB + "YOU CANNOT USE WITHERING PULSE FOR ANOTHER " + TimeUnit.MILLISECONDS.toSeconds(time - System.currentTimeMillis()) + "s.");
        }
        return true;
    }
}
