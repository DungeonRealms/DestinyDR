package net.dungeonrealms.game.item.healing;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.healing.abilities.AuraHeal;
import net.dungeonrealms.game.item.healing.abilities.MendWounds;
import net.dungeonrealms.game.item.healing.abilities.Regeneration;
import net.dungeonrealms.game.item.healing.abilities.WitheringPulse;
import org.bukkit.entity.Player;

import java.util.List;

@AllArgsConstructor
@Getter
public enum HealingAbility {

    AURA_HEAL("Aura Heal", Lists.newArrayList(ItemUsage.RIGHT_CLICK_AIR, ItemUsage.RIGHT_CLICK_ENTITY, ItemUsage.RIGHT_CLICK_BLOCK), new AuraHeal()),
    MEND_WOUNDS("Mend Wounds", Lists.newArrayList(ItemUsage.LEFT_CLICK_AIR, ItemUsage.LEFT_CLICK_BLOCK, ItemUsage.LEFT_CLICK_ENTITY), new MendWounds()),

    WITHERING_PULSE("Withering Pulse", Lists.newArrayList(ItemUsage.RIGHT_CLICK_AIR, ItemUsage.RIGHT_CLICK_ENTITY, ItemUsage.RIGHT_CLICK_BLOCK), new WitheringPulse()),
    REGENERATION("Regeneration", Lists.newArrayList(ItemUsage.LEFT_CLICK_AIR, ItemUsage.LEFT_CLICK_ENTITY, ItemUsage.LEFT_CLICK_BLOCK), new Regeneration());

    private String name;

    private List<ItemUsage> usages;

    private Healing healInstance;

    public static HealingAbility getFromUsage(ItemUsage usage, Player player) {
        for (HealingAbility ability : values()) {
            if (ability.getUsages().contains(usage)) {
                if (ability == AURA_HEAL) {
                    if (player.isSneaking()) continue;
                } else if (ability == WITHERING_PULSE) {
                    if (!player.isSneaking()) continue;
                } else if (ability == MEND_WOUNDS)
                    if (player.isSneaking()) continue;
                return ability;
            }
        }
        return null;
    }
}
