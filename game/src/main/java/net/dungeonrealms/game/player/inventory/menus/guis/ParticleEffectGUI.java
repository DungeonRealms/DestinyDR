package net.dungeonrealms.game.player.inventory.menus.guis;

import com.google.common.collect.Lists;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.Purchaseables;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

public class ParticleEffectGUI extends GUIMenu {
    public ParticleEffectGUI(Player player, GUIMenu previous) {
        super(player, 27, "Player Effects", previous);
    }

    @Override
    protected void setItems() {

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        setItem(getSize() - 1, getBackButton(ChatColor.GRAY + "Click to return to Profile Menu."));

        setItem(getSize() - 2, new GUIItem(Material.ARMOR_STAND).setName(ChatColor.GREEN + "Turn Off Effect").setClick(e -> {
            if (DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.containsKey(player)) {
                DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.remove(player);
                wrapper.setActiveTrail(null);
                player.sendMessage(ChatColor.GREEN + "You have disabled your effect.");
            } else {
                player.sendMessage(ChatColor.RED + "You don't have a player effect currently activated.");
            }
        }));

        int i = 0;
        for (ParticleAPI.ParticleEffect effect : ParticleAPI.ParticleEffect.values()) {
            List<String> lore = Lists.newArrayList();

            if (effect == ParticleAPI.ParticleEffect.GOLD_BLOCK)
                lore.addAll(Purchaseables.GOLDEN_CURSE.getDescription(true));

            boolean activated = wrapper.getActiveTrail() == effect;
            lore.add("");

            if (activated) {
                lore.add(ChatColor.GREEN.toString() + ChatColor.BOLD + "ACTIVATED");
                lore.add(ChatColor.GRAY + "Click to de-activate this effect.");
            } else {
                if (wrapper.hasEffectUnlocked(effect)) {
                    lore.add(ChatColor.GREEN + ChatColor.BOLD.toString() + "UNLOCKED");
                    lore.add(ChatColor.GRAY + "Click to activate this effect.");
                } else {
                    lore.add(ChatColor.RED.toString() + ChatColor.BOLD + "LOCKED");
                    if (effect.getPrice() > 0) {
                        lore.add("");
                        lore.add(ChatColor.GREEN + "Cost: " + ChatColor.WHITE.toString() + effect.getPrice() + ChatColor.GREEN + " E-Cash");
                    }
                }
            }


            setItem(i++, new GUIItem(effect.getSelectionItem()).setName(ChatColor.GREEN + effect.getDisplayName()).setEnchanted(activated).setLore(lore).setClick(e -> {
                if (!wrapper.hasEffectUnlocked(effect)) {
                    //Unlock?
                    if (effect.getPrice() <= 0) {
                        player.sendMessage(ChatColor.GREEN + "You do not have this effect unlocked!");
                        return;
                    }
                    if (wrapper.getEcash() >= effect.getPrice()) {
                        wrapper.withdrawEcash(effect.getPrice());
                        player.sendMessage(ChatColor.GREEN + "You have purchased the " + effect.getDisplayName() + " effect!");
                        wrapper.getParticles().add(effect);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.1F);
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have enough E-Cash for this effect!");
                        player.sendMessage(ChatColor.GRAY + "Current E-Cash: " + ChatColor.GREEN + wrapper.getEcash());
                        return;
                    }
                }
                if (wrapper.getActiveTrail() == effect) {
                    wrapper.setActiveTrail(null);
                    player.sendMessage(ChatColor.RED + "Active Effect has been cleared.");
                } else {
                    wrapper.setActiveTrail(effect);
                    player.sendMessage(ChatColor.GREEN + "The " + ChatColor.BOLD + effect.getDisplayName() + ChatColor.GREEN + " trail has been activated.");
                }
                setItems();
            }));
        }
    }
}
