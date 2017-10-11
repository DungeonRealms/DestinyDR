package net.dungeonrealms.game.item.healing.abilities;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.healing.Healing;
import net.dungeonrealms.game.item.healing.HealingAbility;
import net.dungeonrealms.game.mastery.MetadataUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Regeneration extends Healing {

    @Override
    public boolean onAbilityUse(Player player, HealingAbility ability, ItemClickEvent event) {
        player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1, .8F);
        //Should start regenerating now..
        GameAPI.addCooldown(player, MetadataUtils.Metadata.REGEN_ABILITY, 10);
        return true;
    }

}
