package net.dungeonrealms.items;

import net.dungeonrealms.mastery.Utils;

import java.util.Random;

/**
 * Created by Nick on 9/19/2015.
 */
public class DamageMeta {

    public int next(Item.ItemTier tier, Item.ItemModifier modifier, Item.AttributeType type) {
        switch (tier) {
            case TIER_1:
                switch (modifier) {
                    case COMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 6);
                            case ARMOR_PENETRATION:
                                return random(1, 6);
                            case BLIND:
                                return random(1, 6);
                            case CRITICAL_HIT:
                                return random(1, 6);
                            case DAMAGE:
                                return random(2, 5);
                            case DEXTERITY:
                                return random(1, 6);
                            case FIRE_DAMAGE:
                                return random(1, 6);
                            case ICE_DAMAGE:
                                return random(1, 6);
                            case KNOCK_BACK:
                                return random(1, 6);
                            case LIFE_STEAL:
                                return random(1, 3);
                            case PURE_DAMAGE:
                                return random(1, 3);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(1, 4);
                            case VS_PLAYER:
                                return random(1, 3);
                        }
                    case UNCOMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 9);
                            case ARMOR_PENETRATION:
                                return random(1, 9);
                            case BLIND:
                                return random(1, 9);
                            case CRITICAL_HIT:
                                return random(1, 9);
                            case DAMAGE:
                                return random(2, 9);
                            case DEXTERITY:
                                return random(2, 8);
                            case FIRE_DAMAGE:
                                return random(1, 9);
                            case ICE_DAMAGE:
                                return random(1, 9);
                            case KNOCK_BACK:
                                return random(1, 8);
                            case LIFE_STEAL:
                                return random(1, 4);
                            case PURE_DAMAGE:
                                return random(2, 6);
                            case VITALITY:
                                return random(1, 9);
                            case VS_MONSTERS:
                                return random(1, 8);
                            case VS_PLAYER:
                                return random(1, 8);
                        }
                    case RARE:
                        switch (type) {
                            case ACCURACY:
                                return random(2, 5);
                            case ARMOR_PENETRATION:
                                return random(2, 6);
                            case BLIND:
                                return random(2, 3);
                            case CRITICAL_HIT:
                                return random(2, 5);
                            case DAMAGE:
                                return random(3, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(2, 3);
                            case ICE_DAMAGE:
                                return random(2, 6);
                            case KNOCK_BACK:
                                return random(2, 5);
                            case LIFE_STEAL:
                                return random(2, 7);
                            case PURE_DAMAGE:
                                return random(2, 6);
                            case VITALITY:
                                return random(2, 3);
                            case VS_MONSTERS:
                                return random(2, 12);
                            case VS_PLAYER:
                                return random(2, 3);
                        }
                    case UNIQUE:
                        switch (type) {
                            case ACCURACY:
                                return random(5, 12);
                            case ARMOR_PENETRATION:
                                return random(5, 12);
                            case BLIND:
                                return random(5, 12);
                            case CRITICAL_HIT:
                                return random(5, 15);
                            case DAMAGE:
                                return random(5, 19);
                            case DEXTERITY:
                                return random(5, 14);
                            case FIRE_DAMAGE:
                                return random(5, 13);
                            case ICE_DAMAGE:
                                return random(5, 13);
                            case KNOCK_BACK:
                                return random(5, 12);
                            case LIFE_STEAL:
                                return random(2, 4);
                            case PURE_DAMAGE:
                                return random(5, 16);
                            case VITALITY:
                                return random(5, 13);
                            case VS_MONSTERS:
                                return random(5, 14);
                            case VS_PLAYER:
                                return random(5, 3);
                        }
                    case LEGENDARY:
                        switch (type) {
                            case ACCURACY:
                                return random(5, 21);
                            case ARMOR_PENETRATION:
                                return random(5, 21);
                            case BLIND:
                                return random(5, 21);
                            case CRITICAL_HIT:
                                return random(5, 24);
                            case DAMAGE:
                                return random(5, 21);
                            case DEXTERITY:
                                return random(5, 22);
                            case FIRE_DAMAGE:
                                return random(5, 25);
                            case ICE_DAMAGE:
                                return random(5, 26);
                            case KNOCK_BACK:
                                return random(5, 22);
                            case LIFE_STEAL:
                                return random(3, 6);
                            case PURE_DAMAGE:
                                return random(5, 26);
                            case VITALITY:
                                return random(5, 27);
                            case VS_MONSTERS:
                                return random(5, 21);
                            case VS_PLAYER:
                                return random(5, 26);
                        }
                }
                break;
            case TIER_2:
                switch (modifier) {
                    case COMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 5);
                            case ARMOR_PENETRATION:
                                return random(1, 6);
                            case BLIND:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 5);
                            case DAMAGE:
                                return random(3, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(1, 3);
                            case ICE_DAMAGE:
                                return random(1, 3);
                            case KNOCK_BACK:
                                return random(1, 2);
                            case LIFE_STEAL:
                                return 1;
                            case PURE_DAMAGE:
                                return random(3, 6);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(3, 12);
                            case VS_PLAYER:
                                return random(1, 3);
                        }
                    case UNCOMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 5);
                            case ARMOR_PENETRATION:
                                return random(1, 6);
                            case BLIND:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 5);
                            case DAMAGE:
                                return random(3, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(1, 3);
                            case ICE_DAMAGE:
                                return random(1, 3);
                            case KNOCK_BACK:
                                return random(1, 2);
                            case LIFE_STEAL:
                                return 1;
                            case PURE_DAMAGE:
                                return random(3, 6);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(3, 12);
                            case VS_PLAYER:
                                return random(1, 3);
                        }
                    case RARE:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 5);
                            case ARMOR_PENETRATION:
                                return random(1, 6);
                            case BLIND:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 5);
                            case DAMAGE:
                                return random(3, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(1, 3);
                            case ICE_DAMAGE:
                                return random(1, 3);
                            case KNOCK_BACK:
                                return random(1, 2);
                            case LIFE_STEAL:
                                return 1;
                            case PURE_DAMAGE:
                                return random(3, 6);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(3, 12);
                            case VS_PLAYER:
                                return random(1, 3);
                        }
                    case UNIQUE:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 5);
                            case ARMOR_PENETRATION:
                                return random(1, 6);
                            case BLIND:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 5);
                            case DAMAGE:
                                return random(3, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(1, 3);
                            case ICE_DAMAGE:
                                return random(1, 3);
                            case KNOCK_BACK:
                                return random(1, 2);
                            case LIFE_STEAL:
                                return 1;
                            case PURE_DAMAGE:
                                return random(3, 6);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(3, 12);
                            case VS_PLAYER:
                                return random(1, 3);
                        }
                    case LEGENDARY:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 5);
                            case ARMOR_PENETRATION:
                                return random(1, 6);
                            case BLIND:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 5);
                            case DAMAGE:
                                return random(3, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(1, 3);
                            case ICE_DAMAGE:
                                return random(1, 3);
                            case KNOCK_BACK:
                                return random(1, 2);
                            case LIFE_STEAL:
                                return 1;
                            case PURE_DAMAGE:
                                return random(3, 6);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(3, 12);
                            case VS_PLAYER:
                                return random(1, 3);
                        }
                }
                break;
            case TIER_3:
                switch (modifier) {
                    case COMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 5);
                            case ARMOR_PENETRATION:
                                return random(1, 6);
                            case BLIND:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 5);
                            case DAMAGE:
                                return random(3, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(1, 3);
                            case ICE_DAMAGE:
                                return random(1, 3);
                            case KNOCK_BACK:
                                return random(1, 2);
                            case LIFE_STEAL:
                                return 1;
                            case PURE_DAMAGE:
                                return random(3, 6);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(3, 12);
                            case VS_PLAYER:
                                return random(1, 3);
                        }
                    case UNCOMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 5);
                            case ARMOR_PENETRATION:
                                return random(1, 6);
                            case BLIND:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 5);
                            case DAMAGE:
                                return random(3, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(1, 3);
                            case ICE_DAMAGE:
                                return random(1, 3);
                            case KNOCK_BACK:
                                return random(1, 2);
                            case LIFE_STEAL:
                                return 1;
                            case PURE_DAMAGE:
                                return random(3, 6);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(3, 12);
                            case VS_PLAYER:
                                return random(1, 3);
                        }
                    case RARE:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 5);
                            case ARMOR_PENETRATION:
                                return random(1, 6);
                            case BLIND:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 5);
                            case DAMAGE:
                                return random(3, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(1, 3);
                            case ICE_DAMAGE:
                                return random(1, 3);
                            case KNOCK_BACK:
                                return random(1, 2);
                            case LIFE_STEAL:
                                return 1;
                            case PURE_DAMAGE:
                                return random(3, 6);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(3, 12);
                            case VS_PLAYER:
                                return random(1, 3);
                        }
                    case UNIQUE:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 5);
                            case ARMOR_PENETRATION:
                                return random(1, 6);
                            case BLIND:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 5);
                            case DAMAGE:
                                return random(3, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(1, 3);
                            case ICE_DAMAGE:
                                return random(1, 3);
                            case KNOCK_BACK:
                                return random(1, 2);
                            case LIFE_STEAL:
                                return 1;
                            case PURE_DAMAGE:
                                return random(3, 6);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(3, 12);
                            case VS_PLAYER:
                                return random(1, 3);
                        }
                    case LEGENDARY:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 5);
                            case ARMOR_PENETRATION:
                                return random(1, 6);
                            case BLIND:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 5);
                            case DAMAGE:
                                return random(3, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(1, 3);
                            case ICE_DAMAGE:
                                return random(1, 3);
                            case KNOCK_BACK:
                                return random(1, 2);
                            case LIFE_STEAL:
                                return 1;
                            case PURE_DAMAGE:
                                return random(3, 6);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(3, 12);
                            case VS_PLAYER:
                                return random(1, 3);
                        }
                }
                break;
            case TIER_4:
                switch (modifier) {
                    case COMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 5);
                            case ARMOR_PENETRATION:
                                return random(1, 6);
                            case BLIND:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 5);
                            case DAMAGE:
                                return random(3, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(1, 3);
                            case ICE_DAMAGE:
                                return random(1, 3);
                            case KNOCK_BACK:
                                return random(1, 2);
                            case LIFE_STEAL:
                                return 1;
                            case PURE_DAMAGE:
                                return random(3, 6);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(3, 12);
                            case VS_PLAYER:
                                return random(1, 3);
                        }
                    case UNCOMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 5);
                            case ARMOR_PENETRATION:
                                return random(1, 6);
                            case BLIND:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 5);
                            case DAMAGE:
                                return random(3, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(1, 3);
                            case ICE_DAMAGE:
                                return random(1, 3);
                            case KNOCK_BACK:
                                return random(1, 2);
                            case LIFE_STEAL:
                                return 1;
                            case PURE_DAMAGE:
                                return random(3, 6);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(3, 12);
                            case VS_PLAYER:
                                return random(1, 3);
                        }
                    case RARE:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 5);
                            case ARMOR_PENETRATION:
                                return random(1, 6);
                            case BLIND:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 5);
                            case DAMAGE:
                                return random(3, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(1, 3);
                            case ICE_DAMAGE:
                                return random(1, 3);
                            case KNOCK_BACK:
                                return random(1, 2);
                            case LIFE_STEAL:
                                return 1;
                            case PURE_DAMAGE:
                                return random(3, 6);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(3, 12);
                            case VS_PLAYER:
                                return random(1, 3);
                        }
                    case UNIQUE:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 5);
                            case ARMOR_PENETRATION:
                                return random(1, 6);
                            case BLIND:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 5);
                            case DAMAGE:
                                return random(3, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(1, 3);
                            case ICE_DAMAGE:
                                return random(1, 3);
                            case KNOCK_BACK:
                                return random(1, 2);
                            case LIFE_STEAL:
                                return 1;
                            case PURE_DAMAGE:
                                return random(3, 6);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(3, 12);
                            case VS_PLAYER:
                                return random(1, 3);
                        }
                    case LEGENDARY:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 5);
                            case ARMOR_PENETRATION:
                                return random(1, 6);
                            case BLIND:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 5);
                            case DAMAGE:
                                return random(3, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(1, 3);
                            case ICE_DAMAGE:
                                return random(1, 3);
                            case KNOCK_BACK:
                                return random(1, 2);
                            case LIFE_STEAL:
                                return 1;
                            case PURE_DAMAGE:
                                return random(3, 6);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(3, 12);
                            case VS_PLAYER:
                                return random(1, 3);
                        }
                }
                break;
            case TIER_5:
                switch (modifier) {
                    case COMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 5);
                            case ARMOR_PENETRATION:
                                return random(1, 6);
                            case BLIND:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 5);
                            case DAMAGE:
                                return random(3, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(1, 3);
                            case ICE_DAMAGE:
                                return random(1, 3);
                            case KNOCK_BACK:
                                return random(1, 2);
                            case LIFE_STEAL:
                                return 1;
                            case PURE_DAMAGE:
                                return random(3, 6);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(3, 12);
                            case VS_PLAYER:
                                return random(1, 3);
                        }
                    case UNCOMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 5);
                            case ARMOR_PENETRATION:
                                return random(1, 6);
                            case BLIND:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 5);
                            case DAMAGE:
                                return random(3, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(1, 3);
                            case ICE_DAMAGE:
                                return random(1, 3);
                            case KNOCK_BACK:
                                return random(1, 2);
                            case LIFE_STEAL:
                                return 1;
                            case PURE_DAMAGE:
                                return random(3, 6);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(3, 12);
                            case VS_PLAYER:
                                return random(1, 3);
                        }
                    case RARE:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 5);
                            case ARMOR_PENETRATION:
                                return random(1, 6);
                            case BLIND:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 5);
                            case DAMAGE:
                                return random(3, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(1, 3);
                            case ICE_DAMAGE:
                                return random(1, 3);
                            case KNOCK_BACK:
                                return random(1, 2);
                            case LIFE_STEAL:
                                return 1;
                            case PURE_DAMAGE:
                                return random(3, 6);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(3, 12);
                            case VS_PLAYER:
                                return random(1, 3);
                        }
                    case UNIQUE:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 5);
                            case ARMOR_PENETRATION:
                                return random(1, 6);
                            case BLIND:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 5);
                            case DAMAGE:
                                return random(3, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(1, 3);
                            case ICE_DAMAGE:
                                return random(1, 3);
                            case KNOCK_BACK:
                                return random(1, 2);
                            case LIFE_STEAL:
                                return 1;
                            case PURE_DAMAGE:
                                return random(3, 6);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(3, 12);
                            case VS_PLAYER:
                                return random(1, 3);
                        }
                    case LEGENDARY:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 5);
                            case ARMOR_PENETRATION:
                                return random(1, 6);
                            case BLIND:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 5);
                            case DAMAGE:
                                return random(3, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(1, 3);
                            case ICE_DAMAGE:
                                return random(1, 3);
                            case KNOCK_BACK:
                                return random(1, 2);
                            case LIFE_STEAL:
                                return 1;
                            case PURE_DAMAGE:
                                return random(3, 6);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(3, 12);
                            case VS_PLAYER:
                                return random(1, 3);
                        }
                }
                break;
            default:
                Utils.log.warning("Unable to switch() data of DamageMeta for " + tier.getMaterial());
        }
        return 0;
    }


    int random(int min, int max) {
        return new Random().nextInt(max - min) + min;
    }
}
