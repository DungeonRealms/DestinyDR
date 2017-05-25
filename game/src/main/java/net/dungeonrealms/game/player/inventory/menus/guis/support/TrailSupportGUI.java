package net.dungeonrealms.game.player.inventory.menus.guis.support;

import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Rar349 on 5/19/2017.
 */
public class TrailSupportGUI extends SupportGUI {

    public TrailSupportGUI(Player viewer, String other) {
        super(viewer,other,45,other + "'s Trail Management");
    }

    @Override
    protected void setItems() {

        int slot = 0;
        Set<ParticleAPI.ParticleEffect> particles = getWrapper().getParticles();
        for(ParticleAPI.ParticleEffect effect : ParticleAPI.ParticleEffect.values()) {
            ItemStack item = effect.getSelectionItem();
            boolean unlocked =particles.contains(effect);
            List<String> lore = new ArrayList<>();
            lore.add(unlocked ? "Click here to lock this trail!" : "Click here to unlock this trail!");
            setItem(slot++, new GUIItem(item).setLore(lore).setClick((evt) -> {
                if(unlocked) {
                    getWrapper().getParticles().remove(effect);
                } else {
                    getWrapper().getParticles().add(effect);
                }
                saveData();
            }));
        }

    }
}
