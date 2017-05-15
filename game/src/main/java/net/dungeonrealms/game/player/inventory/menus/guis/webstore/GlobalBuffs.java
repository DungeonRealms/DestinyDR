package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import lombok.Getter;
import net.dungeonrealms.game.mechanic.data.EnumBuff;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.util.Arrays;

@Getter
public enum GlobalBuffs {

    //ECASH
    ECASH_LOOT_BUFF_20(EnumBuff.LOOT, 20, 1_000),
    ECASH_LOOT_BUFF_35(EnumBuff.LOOT, 35, 1_500),
    ECASH_LOOT_BUFF_50(EnumBuff.LOOT, 50, 2_000),

    ECASH_LEVEL_BUFF_20(EnumBuff.LEVEL, 20, 1_000),
    ECASH_LEVEL_BUFF_35(EnumBuff.LEVEL, 35, 1_500),
    ECASH_LEVEL_BUFF_50(EnumBuff.LEVEL, 50, 2_000),


    ECASH_PROF_BUFF_20(EnumBuff.PROFESSION, 20, 1_000),
    ECASH_PROF_BUFF_35(EnumBuff.PROFESSION, 35, 1_500),
    ECASH_PROF_BUFF_50(EnumBuff.PROFESSION, 50, 2_000),

    //SHOP
    LOOT_BUFF_20(EnumBuff.LOOT, Purchaseables.LOOT_BUFF_20, 3600, 20),
    LOOT_BUFF_40(EnumBuff.LOOT, Purchaseables.LOOT_BUFF_40, 3600, 40),

    LEVEL_BUFF_20(EnumBuff.LEVEL, Purchaseables.LEVEL_BUFF_20, 3600, 20),
    LEVEL_BUFF_40(EnumBuff.LEVEL, Purchaseables.LEVEL_BUFF_40, 3600, 40),

    PROF_BUFF_20(EnumBuff.PROFESSION, Purchaseables.PROFESSION_BUFF_20, 3600, 20),
    PROF_BUFF_40(EnumBuff.PROFESSION, Purchaseables.PROFESSION_BUFF_40, 3600, 40);

    EnumBuff buffCategory;
    Purchaseables purchaseable;
    int duration;
    int buffPower;
    int ecashCost;

    GlobalBuffs(EnumBuff buffCategory, int buffPower, int ecash) {
        this.buffCategory = buffCategory;
        this.purchaseable = null;
        this.buffPower = buffPower;
        this.duration = 3600;
        this.ecashCost = ecash;
    }

    GlobalBuffs(net.dungeonrealms.game.mechanic.data.EnumBuff buffCategory, Purchaseables purchaseable, int duration, int buffPower) {
        this.buffCategory = buffCategory;
        this.purchaseable = purchaseable;
        this.buffPower = buffPower;
        this.duration = duration;
        this.ecashCost = -1;
    }

    public String getFormattedTime() {
        return DurationFormatUtils.formatDurationWords(getDuration() * 1000, true, true);
    }


    public static GlobalBuffs getGlobalBuff(Purchaseables item) {
        return Arrays.stream(GlobalBuffs.values()).filter(buff -> buff.getPurchaseable() != null && buff.getPurchaseable().equals(item)).findFirst().orElse(null);

    }
}
