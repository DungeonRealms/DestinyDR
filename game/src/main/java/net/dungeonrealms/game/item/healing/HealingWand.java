package net.dungeonrealms.game.item.healing;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.handler.EnergyHandler;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.items.core.setbonus.SetBonus;
import net.dungeonrealms.game.item.items.core.setbonus.SetBonuses;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.title.TitleAPI;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.item.CC;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class HealingWand extends FunctionalItem implements ItemClickEvent.ItemClickListener {

    public HealingWand() {
        super(ItemType.HEALING_WAND);
        setPermUntradeable(true);
    }

    public HealingWand(ItemStack item) {
        super(item);
        setPermUntradeable(true);
    }

    @Override
    protected ItemStack getStack() {
        setAntiDupe(true);
        setPermUntradeable(true);
        return new ItemStack(Material.FEATHER, 1);
    }

    @Override
    public void onClick(ItemClickEvent evt) {
        Player player = evt.getPlayer();

        if (!SetBonus.hasSetBonus(player, SetBonuses.HEALER)) {
            player.sendMessage(ChatColor.RED + "You must be wearing full Mage Armor to use this Healing Wand!");
            return;
        }

        if (GameAPI.isInSafeRegion(player.getLocation())) {
            player.sendMessage(ChatColor.RED + "You cannot use this in a Safe Zone!");
            return;
        }

        HealingAbility ability = HealingAbility.getFromUsage(evt.getUsage(), player);
        if (ability == null) {
            evt.setCancelled(true);
            return;
        }

        //Aura heal?
        float energy = EnergyHandler.getPlayerCurrentEnergy(player);
        if (energy == 1) {
            //Costs everything.
            //Do ability.
            if (ability.getHealInstance().onAbilityUse(player, ability, evt)) {
                if (!ability.getHealInstance().isOnCooldown()) {
                    TitleAPI.sendActionBar(player, ChatColor.GREEN + "❢ " + ability.getName() + " ❢", 20);

                    PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                    //Dont take energy if they are lucky.
                    if (ThreadLocalRandom.current().nextInt(100) > wrapper.getAttributes().getAttribute(Item.ArmorAttributeType.LUCK).getValue()) {
                        player.setExp(0.0F);
                        player.setTotalExperience(0);
                        EnergyHandler.updatePlayerEnergyBar(player);
                        MountUtils.removeMount(player);
                        ability.getHealInstance().setOnCooldown(ability.getHealInstance().cooldown);
                    } else
                        ParticleAPI.spawnParticle(Particle.CRIT, player.getLocation().add(0, 1.4, 0), 10, .25F, .1F);
                } else {
                    Bukkit.getLogger().info("Unable to use " + ability.getName() + " For " + player.getName());
                }
            }
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_WOLF_PANT, 12F, 1.5F);
            TitleAPI.sendActionBar(player, ChatColor.RED + "❢ Not enough Energy to cast " + ability.getName() + " ❢", 20);
        }
    }

    @Override
    protected String getDisplayName() {
        return ChatColor.AQUA + ChatColor.BOLD.toString() + "Healing Wand";
    }

    @Override
    protected String[] getLore() {
        return new String[]{
                ChatColor.GRAY + ChatColor.ITALIC.toString() + "A Powerful wand capable of",
                ChatColor.GRAY + ChatColor.ITALIC.toString() + "life and death.",
                "",
                CC.Green + "Right-Click " + CC.Gray + "to use " + CC.GreenB + HealingAbility.AURA_HEAL.getName(),
                CC.Green + "Shift Right-Click " + CC.Gray + "to use " + CC.RedB + HealingAbility.WITHERING_PULSE.getName(),
                "",
                CC.Green + "Left-Click " + CC.Gray + "to use " + CC.GreenB + HealingAbility.MEND_WOUNDS.getName(),
                CC.Green + "Shift Left-Click " + CC.Gray + "to use " + CC.GreenB + HealingAbility.REGENERATION.getName(),
        };
    }

    @Override
    protected ItemUsage[] getUsage() {
        return new ItemUsage[]{
                ItemUsage.LEFT_CLICK_AIR, ItemUsage.LEFT_CLICK_BLOCK, ItemUsage.LEFT_CLICK_ENTITY,
                ItemUsage.RIGHT_CLICK_BLOCK, ItemUsage.RIGHT_CLICK_AIR, ItemUsage.RIGHT_CLICK_ENTITY};
    }
}
