package net.dungeonrealms.game.mechanic.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import org.bukkit.ChatColor;
import org.bukkit.Material;

@AllArgsConstructor
@Getter
public enum HorseTier {

    TIER_1(EnumMounts.TIER1_HORSE, Material.IRON_BARDING, "Old Horse", "An old brown starter horse.", 3_000, 120, 0.2F, 100),
    TIER_2(EnumMounts.TIER2_HORSE, Material.IRON_BARDING, "Traveler's Horse", "A standard healthy horse.", 7_000, 140, 0.218F, 110),
    TIER_3(EnumMounts.TIER3_HORSE, Material.DIAMOND_BARDING, "Knight's Horse", "A fast well-bred horse.", 15_000, 170, .25F, 115),
    TIER_4(EnumMounts.TIER4_HORSE, Material.GOLD_BARDING, "War Stallion", "A trusty powerful steed.", 30_000, 200, .245F, 125),
    MULE(EnumMounts.MULE, Material.SADDLE, "Old Storage Mule", "An old worn mule.", 5000, 0, .2F, 0);

    private EnumMounts mount;
    private Material armor;
    private String name;
    private String description;
    private int price;
    private int speed;
    private float rawSpeed;
    private int jump;

    public int getTier() {
        return getId();
    }

    public int getId() {
        return ordinal() + 1;
    }

    public static HorseTier getByMount(EnumMounts mounts) {
        for (HorseTier t : values())
            if (t.getMount() == mounts)
                return t;
        return null;
    }

    public HorseTier getRequirement() {
        if(this == MULE)return null;
        HorseTier previous = null;
        for(HorseTier tier : values()){
            if(tier == this)return previous;
            previous = tier;
        }
        return null;
    }

    public ChatColor getColor() {
        return GameAPI.getTierColor(ordinal() + 1);
    }

    public static HorseTier getByTier(int tier) {
        return values()[tier - 1];
    }
}
