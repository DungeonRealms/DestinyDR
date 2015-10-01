package net.dungeonrealms.items;

import net.dungeonrealms.items.armor.Armor;

import java.util.Random;

/**
 * Created by Nick on 9/19/2015.
 */
public class DamageMeta {

    public int nextArmor(Armor.ArmorTier tier, Armor.ArmorModifier modifier, Armor.ArmorAttributeType type) {
        switch (tier) {
            case TIER_1:
                switch (modifier) {
                    case COMMON:
                        switch (type) {
                            case ARMOR:
                                return random(1, 6);
                        }
                        break;
                }
        }
        return 5;
    }

    /**
     * @param tier
     * @param modifier
     * @param type
     * @return
     * @since 1.0
     */
    public int nextWeapon(Item.ItemTier tier, Item.ItemModifier modifier, Item.AttributeType type) {
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
                            case POISON_DAMAGE:
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
                            case STRENGTH:
                                return random(1, 3);
                            case INTELLECT:
                                return random(1, 6);
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
                            case POISON_DAMAGE:
                                return random(1, 9);
                            case KNOCK_BACK:
                                return random(1, 8);
                            case LIFE_STEAL:
                                return random(1, 4);
                            case PURE_DAMAGE:
                                return random(2, 5);
                            case VITALITY:
                                return random(1, 3);
                            case VS_MONSTERS:
                                return random(1, 7);
                            case VS_PLAYER:
                                return random(1, 8);
                            case STRENGTH:
                                return random(1, 3);
                            case INTELLECT:
                                return random(1, 3);
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
                                return random(4, 9);
                            case DEXTERITY:
                                return random(2, 4);
                            case FIRE_DAMAGE:
                                return random(2, 3);
                            case ICE_DAMAGE:
                                return random(2, 6);
                            case POISON_DAMAGE:
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
                            case STRENGTH:
                                return random(2, 6);
                            case INTELLECT:
                                return random(2, 6);
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
                                return random(5, 12);
                            case DEXTERITY:
                                return random(3, 7);
                            case FIRE_DAMAGE:
                                return random(5, 13);
                            case ICE_DAMAGE:
                                return random(5, 13);
                            case POISON_DAMAGE:
                                return random(5, 13);
                            case KNOCK_BACK:
                                return random(5, 12);
                            case LIFE_STEAL:
                                return random(2, 4);
                            case PURE_DAMAGE:
                                return random(5, 12);
                            case VITALITY:
                                return random(3, 7);
                            case VS_MONSTERS:
                                return random(5, 14);
                            case VS_PLAYER:
                                return random(5, 8);
                            case STRENGTH:
                                return random(3, 7);
                            case INTELLECT:
                                return random(3, 7);
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
                                return random(5, 16);
                            case DEXTERITY:
                                return random(4, 10);
                            case FIRE_DAMAGE:
                                return random(5, 25);
                            case ICE_DAMAGE:
                                return random(5, 26);
                            case POISON_DAMAGE:
                                return random(5, 26);
                            case KNOCK_BACK:
                                return random(5, 22);
                            case LIFE_STEAL:
                                return random(3, 6);
                            case PURE_DAMAGE:
                                return random(5, 26);
                            case VITALITY:
                                return random(7, 10);
                            case VS_MONSTERS:
                                return random(5, 21);
                            case VS_PLAYER:
                                return random(5, 26);
                            case STRENGTH:
                                return random(4, 10);
                            case INTELLECT:
                                return random(4, 10);
                        }
                }
                break;
            case TIER_2:
                switch (modifier) {
                    case COMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(2, 5);
                            case ARMOR_PENETRATION:
                                return random(2, 7);
                            case BLIND:
                                return random(2, 4);
                            case CRITICAL_HIT:
                                return random(4, 7);
                            case DAMAGE:
                                return random(15, 23);
                            case DEXTERITY:
                                return random(7, 10);
                            case FIRE_DAMAGE:
                                return random(7, 14);
                            case ICE_DAMAGE:
                                return random(7, 10);
                            case POISON_DAMAGE:
                                return random(7, 10);
                            case KNOCK_BACK:
                                return random(2, 5);
                            case LIFE_STEAL:
                                return random(2, 10);
                            case PURE_DAMAGE:
                                return random(3, 7);
                            case VITALITY:
                                return random(7, 10);
                            case VS_MONSTERS:
                                return random(8, 12);
                            case VS_PLAYER:
                                return random(4, 9);
                            case STRENGTH:
                                return random(7, 10);
                            case INTELLECT:
                                return random(7, 10);
                        }
                    case UNCOMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(9, 15);
                            case ARMOR_PENETRATION:
                                return random(8, 13);
                            case BLIND:
                                return random(4, 7);
                            case CRITICAL_HIT:
                                return random(7, 10);
                            case DAMAGE:
                                return random(20, 35);
                            case DEXTERITY:
                                return random(10, 12);
                            case FIRE_DAMAGE:
                                return random(9, 19);
                            case ICE_DAMAGE:
                                return random(9, 19);
                            case POISON_DAMAGE:
                                return random(9, 19);
                            case KNOCK_BACK:
                                return random(2, 5);
                            case LIFE_STEAL:
                                return random(4, 10);
                            case PURE_DAMAGE:
                                return random(5, 7);
                            case VITALITY:
                                return random(9, 16);
                            case VS_MONSTERS:
                                return random(10, 18);
                            case VS_PLAYER:
                                return random(5, 10);
                            case STRENGTH:
                                return random(7, 10);
                            case INTELLECT:
                                return random(7, 10);
                        }
                    case RARE:
                        switch (type) {
                            case ACCURACY:
                                return random(13, 19);
                            case ARMOR_PENETRATION:
                                return random(13, 19);
                            case BLIND:
                                return random(5, 9);
                            case CRITICAL_HIT:
                                return random(9, 14);
                            case DAMAGE:
                                return random(25, 45);
                            case DEXTERITY:
                                return random(10, 16);
                            case FIRE_DAMAGE:
                                return random(11, 19);
                            case ICE_DAMAGE:
                                return random(11, 19);
                            case POISON_DAMAGE:
                                return random(11, 19);
                            case KNOCK_BACK:
                                return random(3, 5);
                            case LIFE_STEAL:
                                return random(6, 10);
                            case PURE_DAMAGE:
                                return random(5, 9);
                            case VITALITY:
                                return random(10, 16);
                            case VS_MONSTERS:
                                return random(15, 22);
                            case VS_PLAYER:
                                return random(6, 10);
                            case STRENGTH:
                                return random(7, 13);
                            case INTELLECT:
                                return random(7, 13);
                        }
                    case UNIQUE:
                        switch (type) {
                            case ACCURACY:
                                return random(15, 19);
                            case ARMOR_PENETRATION:
                                return random(15, 19);
                            case BLIND:
                                return random(7, 10);
                            case CRITICAL_HIT:
                                return random(11, 16);
                            case DAMAGE:
                                return random(40, 60);
                            case DEXTERITY:
                                return random(14, 22);
                            case FIRE_DAMAGE:
                                return random(14, 19);
                            case ICE_DAMAGE:
                                return random(14, 19);
                            case POISON_DAMAGE:
                                return random(14, 19);
                            case KNOCK_BACK:
                                return random(3, 5);
                            case LIFE_STEAL:
                                return random(6, 13);
                            case PURE_DAMAGE:
                                return random(6, 10);
                            case VITALITY:
                                return random(18, 22);
                            case VS_MONSTERS:
                                return random(18, 22);
                            case VS_PLAYER:
                                return random(8, 10);
                            case STRENGTH:
                                return random(9, 13);
                            case INTELLECT:
                                return random(9, 13);
                        }
                    case LEGENDARY:
                        switch (type) {
                            case ACCURACY:
                                return random(17, 21);
                            case ARMOR_PENETRATION:
                                return random(17, 21);
                            case BLIND:
                                return random(9, 14);
                            case CRITICAL_HIT:
                                return random(12, 16);
                            case DAMAGE:
                                return random(50, 70);
                            case DEXTERITY:
                                return random(13, 20);
                            case FIRE_DAMAGE:
                                return random(14, 24);
                            case ICE_DAMAGE:
                                return random(14, 24);
                            case POISON_DAMAGE:
                                return random(14, 24);
                            case KNOCK_BACK:
                                return random(3, 5);
                            case LIFE_STEAL:
                                return random(6, 15);
                            case PURE_DAMAGE:
                                return random(6, 13);
                            case VITALITY:
                                return random(18, 25);
                            case VS_MONSTERS:
                                return random(18, 25);
                            case VS_PLAYER:
                                return random(8, 13);
                            case STRENGTH:
                                return random(10, 13);
                            case INTELLECT:
                                return random(10, 13);
                        }
                }
                break;
            case TIER_3:
                switch (modifier) {
                    case COMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(15, 24);
                            case ARMOR_PENETRATION:
                                return random(15, 24);
                            case BLIND:
                                return random(7, 10);
                            case CRITICAL_HIT:
                                return random(11, 20);
                            case DAMAGE:
                                return random(80, 100);
                            case DEXTERITY:
                                return random(12, 20);
                            case FIRE_DAMAGE:
                                return random(17, 21);
                            case ICE_DAMAGE:
                                return random(17, 21);
                            case POISON_DAMAGE:
                                return random(17, 21);
                            case KNOCK_BACK:
                                return random(3, 5);
                            case LIFE_STEAL:
                                return random(9, 16);
                            case PURE_DAMAGE:
                                return random(8, 15);
                            case VITALITY:
                                return random(18, 27);
                            case VS_MONSTERS:
                                return random(18, 27);
                            case VS_PLAYER:
                                return random(15, 25);
                            case STRENGTH:
                                return random(14, 18);
                            case INTELLECT:
                                return random(14, 18);
                        }
                    case UNCOMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(18, 27);
                            case ARMOR_PENETRATION:
                                return random(18, 28);
                            case BLIND:
                                return random(7, 10);
                            case CRITICAL_HIT:
                                return random(16, 25);
                            case DAMAGE:
                                return random(100, 130);
                            case DEXTERITY:
                                return random(10, 20);
                            case FIRE_DAMAGE:
                                return random(24, 31);
                            case ICE_DAMAGE:
                                return random(24, 31);
                            case POISON_DAMAGE:
                                return random(24, 31);
                            case KNOCK_BACK:
                                return random(3, 5);
                            case LIFE_STEAL:
                                return random(10, 16);
                            case PURE_DAMAGE:
                                return random(13, 18);
                            case VITALITY:
                                return random(20, 30);
                            case VS_MONSTERS:
                                return random(25, 27);
                            case VS_PLAYER:
                                return random(25, 30);
                            case STRENGTH:
                                return random(15, 19);
                            case INTELLECT:
                                return random(15, 19);
                        }
                    case RARE:
                        switch (type) {
                            case ACCURACY:
                                return random(24, 27);
                            case ARMOR_PENETRATION:
                                return random(24, 28);
                            case BLIND:
                                return random(8, 10);
                            case CRITICAL_HIT:
                                return random(16, 25);
                            case DAMAGE:
                                return random(120, 150);
                            case DEXTERITY:
                                return random(10, 25);
                            case FIRE_DAMAGE:
                                return random(26, 31);
                            case ICE_DAMAGE:
                                return random(26, 31);
                            case POISON_DAMAGE:
                                return random(26, 31);
                            case KNOCK_BACK:
                                return random(3, 5);
                            case LIFE_STEAL:
                                return random(12, 16);
                            case PURE_DAMAGE:
                                return random(15, 18);
                            case VITALITY:
                                return random(20, 30);
                            case VS_MONSTERS:
                                return random(25, 30);
                            case VS_PLAYER:
                                return random(25, 30);
                            case STRENGTH:
                                return random(15, 20);
                            case INTELLECT:
                                return random(15, 20);
                        }
                    case UNIQUE:
                        switch (type) {
                            case ACCURACY:
                                return random(24, 27);
                            case ARMOR_PENETRATION:
                                return random(24, 28);
                            case BLIND:
                                return random(8, 10);
                            case CRITICAL_HIT:
                                return random(16, 25);
                            case DAMAGE:
                                return random(140, 165);
                            case DEXTERITY:
                                return random(10, 25);
                            case FIRE_DAMAGE:
                                return random(26, 31);
                            case ICE_DAMAGE:
                                return random(26, 31);
                            case POISON_DAMAGE:
                                return random(26, 31);
                            case KNOCK_BACK:
                                return random(3, 5);
                            case LIFE_STEAL:
                                return random(12, 16);
                            case PURE_DAMAGE:
                                return random(15, 18);
                            case VITALITY:
                                return random(20, 30);
                            case VS_MONSTERS:
                                return random(25, 30);
                            case VS_PLAYER:
                                return random(25, 30);
                            case STRENGTH:
                                return random(15, 23);
                            case INTELLECT:
                                return random(15, 23);
                        }
                    case LEGENDARY:
                        switch (type) {
                            case ACCURACY:
                                return random(26, 30);
                            case ARMOR_PENETRATION:
                                return random(26, 30);
                            case BLIND:
                                return random(8, 10);
                            case CRITICAL_HIT:
                                return random(16, 25);
                            case DAMAGE:
                                return random(170, 200);
                            case DEXTERITY:
                                return random(15, 25);
                            case FIRE_DAMAGE:
                                return random(26, 34);
                            case ICE_DAMAGE:
                                return random(26, 34);
                            case POISON_DAMAGE:
                                return random(26, 34);
                            case KNOCK_BACK:
                                return random(3, 5);
                            case LIFE_STEAL:
                                return random(14, 17);
                            case PURE_DAMAGE:
                                return random(15, 22);
                            case VITALITY:
                                return random(22, 30);
                            case VS_MONSTERS:
                                return random(25, 32);
                            case VS_PLAYER:
                                return random(25, 32);
                            case STRENGTH:
                                return random(15, 25);
                            case INTELLECT:
                                return random(15, 25);
                        }
                }
                break;
            case TIER_4:
                switch (modifier) {
                    case COMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(26, 30);
                            case ARMOR_PENETRATION:
                                return random(26, 30);
                            case BLIND:
                                return random(8, 10);
                            case CRITICAL_HIT:
                                return random(16, 25);
                            case DAMAGE:
                                return random(180, 200);
                            case DEXTERITY:
                                return random(24, 30);
                            case FIRE_DAMAGE:
                                return random(28, 34);
                            case ICE_DAMAGE:
                                return random(28, 34);
                            case POISON_DAMAGE:
                                return random(28, 34);
                            case KNOCK_BACK:
                                return random(3, 5);
                            case LIFE_STEAL:
                                return random(15, 18);
                            case PURE_DAMAGE:
                                return random(17, 24);
                            case VITALITY:
                                return random(24, 30);
                            case VS_MONSTERS:
                                return random(27, 32);
                            case VS_PLAYER:
                                return random(27, 32);
                            case STRENGTH:
                                return random(17, 25);
                            case INTELLECT:
                                return random(17, 25);
                        }
                    case UNCOMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(26, 32);
                            case ARMOR_PENETRATION:
                                return random(26, 32);
                            case BLIND:
                                return random(8, 14);
                            case CRITICAL_HIT:
                                return random(16, 25);
                            case DAMAGE:
                                return random(195, 205);
                            case DEXTERITY:
                                return random(26, 30);
                            case FIRE_DAMAGE:
                                return random(30, 34);
                            case ICE_DAMAGE:
                                return random(30, 34);
                            case POISON_DAMAGE:
                                return random(30, 34);
                            case KNOCK_BACK:
                                return random(3, 5);
                            case LIFE_STEAL:
                                return random(15, 19);
                            case PURE_DAMAGE:
                                return random(17, 25);
                            case VITALITY:
                                return random(26, 30);
                            case VS_MONSTERS:
                                return random(27, 32);
                            case VS_PLAYER:
                                return random(27, 32);
                            case STRENGTH:
                                return random(19, 25);
                            case INTELLECT:
                                return random(19, 25);
                        }
                    case RARE:
                        switch (type) {
                            case ACCURACY:
                                return random(26, 32);
                            case ARMOR_PENETRATION:
                                return random(26, 34);
                            case BLIND:
                                return random(8, 14);
                            case CRITICAL_HIT:
                                return random(18, 25);
                            case DAMAGE:
                                return random(205, 225);
                            case DEXTERITY:
                                return random(26, 33);
                            case FIRE_DAMAGE:
                                return random(31, 34);
                            case ICE_DAMAGE:
                                return random(31, 34);
                            case POISON_DAMAGE:
                                return random(31, 34);
                            case KNOCK_BACK:
                                return random(3, 5);
                            case LIFE_STEAL:
                                return random(15, 19);
                            case PURE_DAMAGE:
                                return random(18, 25);
                            case VITALITY:
                                return random(26, 33);
                            case VS_MONSTERS:
                                return random(27, 34);
                            case VS_PLAYER:
                                return random(27, 32);
                            case STRENGTH:
                                return random(21, 25);
                            case INTELLECT:
                                return random(21, 25);
                        }
                    case UNIQUE:
                        switch (type) {
                            case ACCURACY:
                                return random(26, 34);
                            case ARMOR_PENETRATION:
                                return random(26, 35);
                            case BLIND:
                                return random(8, 16);
                            case CRITICAL_HIT:
                                return random(18, 25);
                            case DAMAGE:
                                return random(230, 260);
                            case DEXTERITY:
                                return random(27, 33);
                            case FIRE_DAMAGE:
                                return random(32, 37);
                            case ICE_DAMAGE:
                                return random(31, 37);
                            case POISON_DAMAGE:
                                return random(31, 37);
                            case KNOCK_BACK:
                                return random(3, 5);
                            case LIFE_STEAL:
                                return random(16, 19);
                            case PURE_DAMAGE:
                                return random(20, 25);
                            case VITALITY:
                                return random(27, 33);
                            case VS_MONSTERS:
                                return random(30, 34);
                            case VS_PLAYER:
                                return random(30, 34);
                            case STRENGTH:
                                return random(24, 27);
                            case INTELLECT:
                                return random(24, 27);
                        }
                    case LEGENDARY:
                        switch (type) {
                            case ACCURACY:
                                return random(26, 35);
                            case ARMOR_PENETRATION:
                                return random(26, 35);
                            case BLIND:
                                return random(8, 16);
                            case CRITICAL_HIT:
                                return random(18, 25);
                            case DAMAGE:
                                return random(255, 275);
                            case DEXTERITY:
                                return random(27, 34);
                            case FIRE_DAMAGE:
                                return random(32, 38);
                            case ICE_DAMAGE:
                                return random(31, 38);
                            case POISON_DAMAGE:
                                return random(31, 38);
                            case KNOCK_BACK:
                                return random(3, 5);
                            case LIFE_STEAL:
                                return random(16, 20);
                            case PURE_DAMAGE:
                                return random(20, 25);
                            case VITALITY:
                                return random(27, 34);
                            case VS_MONSTERS:
                                return random(30, 35);
                            case VS_PLAYER:
                                return random(30, 35);
                            case STRENGTH:
                                return random(24, 28);
                            case INTELLECT:
                                return random(24, 28);
                        }
                }
                break;
            case TIER_5:
                switch (modifier) {
                    case COMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(26, 34);
                            case ARMOR_PENETRATION:
                                return random(26, 35);
                            case BLIND:
                                return random(8, 16);
                            case CRITICAL_HIT:
                                return random(18, 25);
                            case DAMAGE:
                                return random(250, 280);
                            case DEXTERITY:
                                return random(27, 35);
                            case FIRE_DAMAGE:
                                return random(32, 39);
                            case ICE_DAMAGE:
                                return random(31, 39);
                            case POISON_DAMAGE:
                                return random(31, 39);
                            case KNOCK_BACK:
                                return random(3, 5);
                            case LIFE_STEAL:
                                return random(16, 20);
                            case PURE_DAMAGE:
                                return random(20, 28);
                            case VITALITY:
                                return random(27, 35);
                            case VS_MONSTERS:
                                return random(30, 37);
                            case VS_PLAYER:
                                return random(30, 37);
                            case STRENGTH:
                                return random(25, 28);
                            case INTELLECT:
                                return random(25, 28);
                        }
                    case UNCOMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(26, 35);
                            case ARMOR_PENETRATION:
                                return random(26, 35);
                            case BLIND:
                                return random(8, 16);
                            case CRITICAL_HIT:
                                return random(18, 25);
                            case DAMAGE:
                                return random(270, 305);
                            case DEXTERITY:
                                return random(27, 36);
                            case FIRE_DAMAGE:
                                return random(33, 39);
                            case ICE_DAMAGE:
                                return random(32, 39);
                            case POISON_DAMAGE:
                                return random(32, 39);
                            case KNOCK_BACK:
                                return random(3, 5);
                            case LIFE_STEAL:
                                return random(16, 20);
                            case PURE_DAMAGE:
                                return random(24, 28);
                            case VITALITY:
                                return random(27, 36);
                            case VS_MONSTERS:
                                return random(31, 37);
                            case VS_PLAYER:
                                return random(31, 37);
                            case STRENGTH:
                                return random(25, 30);
                            case INTELLECT:
                                return random(25, 30);
                        }
                    case RARE:
                        switch (type) {
                            case ACCURACY:
                                return random(27, 37);
                            case ARMOR_PENETRATION:
                                return random(28, 37);
                            case BLIND:
                                return random(9, 20);
                            case CRITICAL_HIT:
                                return random(19, 26);
                            case DAMAGE:
                                return random(290, 315);
                            case DEXTERITY:
                                return random(29, 39);
                            case FIRE_DAMAGE:
                                return random(35, 41);
                            case ICE_DAMAGE:
                                return random(35, 41);
                            case POISON_DAMAGE:
                                return random(35, 41);
                            case KNOCK_BACK:
                                return random(3, 5);
                            case LIFE_STEAL:
                                return random(17, 22);
                            case PURE_DAMAGE:
                                return random(25, 34);
                            case VITALITY:
                                return random(29, 39);
                            case VS_MONSTERS:
                                return random(31, 37);
                            case VS_PLAYER:
                                return random(31, 37);
                            case STRENGTH:
                                return random(25, 31);
                            case INTELLECT:
                                return random(25, 31);
                        }
                    case UNIQUE:
                        switch (type) {
                            case ACCURACY:
                                return random(26, 40);
                            case ARMOR_PENETRATION:
                                return random(26, 40);
                            case BLIND:
                                return random(8, 18);
                            case CRITICAL_HIT:
                                return random(18, 27);
                            case DAMAGE:
                                return random(300, 325);
                            case DEXTERITY:
                                return random(27, 40);
                            case FIRE_DAMAGE:
                                return random(33, 45);
                            case ICE_DAMAGE:
                                return random(33, 45);
                            case POISON_DAMAGE:
                                return random(33, 45);
                            case KNOCK_BACK:
                                return random(3, 5);
                            case LIFE_STEAL:
                                return random(17, 22);
                            case PURE_DAMAGE:
                                return random(23, 31);
                            case VITALITY:
                                return random(27, 40);
                            case VS_MONSTERS:
                                return random(31, 42);
                            case VS_PLAYER:
                                return random(31, 42);
                            case STRENGTH:
                                return random(25, 31);
                            case INTELLECT:
                                return random(25, 31);
                        }
                    case LEGENDARY:
                        switch (type) {
                            case ACCURACY:
                                return random(30, 40);
                            case ARMOR_PENETRATION:
                                return random(30, 40);
                            case BLIND:
                                return random(10, 20);
                            case CRITICAL_HIT:
                                return random(18, 30);
                            case DAMAGE:
                                return random(330, 350);
                            case DEXTERITY:
                                return random(30, 40);
                            case FIRE_DAMAGE:
                                return random(36, 45);
                            case ICE_DAMAGE:
                                return random(36, 45);
                            case POISON_DAMAGE:
                                return random(36, 45);
                            case KNOCK_BACK:
                                return random(3, 5);
                            case LIFE_STEAL:
                                return random(18, 23);
                            case PURE_DAMAGE:
                                return random(25, 32);
                            case VITALITY:
                                return random(30, 40);
                            case VS_MONSTERS:
                                return random(34, 44);
                            case VS_PLAYER:
                                return random(34, 44);
                            case STRENGTH:
                                return random(27, 31);
                            case INTELLECT:
                                return random(27, 31);
                        }
                }
                break;
            default:
        }
        return 0;
    }


    private int random(int min, int max) {
        return new Random().nextInt(max - min) + min;
    }
}
