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
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 2);
                            case DAMAGE:
                                return random(2, 7);
                            case DEXTERITY:
                                return random(1, 15);
                            case FIRE_DAMAGE:
                                return random(1, 3);
                            case ICE_DAMAGE:
                                return random(1, 3);
                            case POISON_DAMAGE:
                                return random(1, 3);
                            case LIFE_STEAL:
                                return random(1, 2);
                            case PURE_DAMAGE:
                                return random(1, 3);
                            case VITALITY:
                                return random(1, 15);
                            case VS_MONSTERS:
                                return random(1, 7);
                            case VS_PLAYER:
                                return random(1, 7);
                            case STRENGTH:
                                return random(1, 15);
                            case INTELLECT:
                                return random(1, 15);
                        }
                    case UNCOMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(1, 9);
                            case ARMOR_PENETRATION:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 2);
                            case DAMAGE:
                                return random(4, 9);
                            case DEXTERITY:
                                return random(1, 15);
                            case FIRE_DAMAGE:
                                return random(1, 4);
                            case ICE_DAMAGE:
                                return random(1, 4);
                            case POISON_DAMAGE:
                                return random(1, 4);
                            case LIFE_STEAL:
                                return random(1, 2);
                            case PURE_DAMAGE:
                                return random(2, 5);
                            case VITALITY:
                                return random(1, 15);
                            case VS_MONSTERS:
                                return random(1, 7);
                            case VS_PLAYER:
                                return random(1, 8);
                            case STRENGTH:
                                return random(1, 15);
                            case INTELLECT:
                                return random(1, 15);
                        }
                    case RARE:
                        switch (type) {
                            case ACCURACY:
                                return random(2, 5);
                            case ARMOR_PENETRATION:
                                return random(1, 3);
                            case CRITICAL_HIT:
                                return random(1, 2);
                            case DAMAGE:
                                return random(6, 12);
                            case DEXTERITY:
                                return random(1, 15);
                            case FIRE_DAMAGE:
                                return random(1, 5);
                            case ICE_DAMAGE:
                                return random(1, 5);
                            case POISON_DAMAGE:
                                return random(1, 5);
                            case LIFE_STEAL:
                                return random(1, 2);
                            case PURE_DAMAGE:
                                return random(2, 6);
                            case VITALITY:
                                return random(1, 15);
                            case VS_MONSTERS:
                                return random(1, 7);
                            case VS_PLAYER:
                                return random(1, 7);
                            case STRENGTH:
                                return random(1, 15);
                            case INTELLECT:
                                return random(1, 15);
                        }
                    case UNIQUE:
                        switch (type) {
                            case ACCURACY:
                                return random(5, 12);
                            case ARMOR_PENETRATION:
                                return random(2, 3);
                            case CRITICAL_HIT:
                                return random(1, 2);
                            case DAMAGE:
                                return random(20, 35);
                            case DEXTERITY:
                                return random(1, 15);
                            case FIRE_DAMAGE:
                                return random(2, 5);
                            case ICE_DAMAGE:
                                return random(2, 5);
                            case POISON_DAMAGE:
                                return random(2, 5);
                            case LIFE_STEAL:
                                return random(1, 2);
                            case PURE_DAMAGE:
                                return random(5, 12);
                            case VITALITY:
                                return random(1, 15);
                            case VS_MONSTERS:
                                return random(1, 7);
                            case VS_PLAYER:
                                return random(1, 7);
                            case STRENGTH:
                                return random(1, 15);
                            case INTELLECT:
                                return random(1, 15);
                        }
                    case LEGENDARY:
                        switch (type) {
                            case ACCURACY:
                                return random(5, 21);
                            case ARMOR_PENETRATION:
                                return random(2, 3);
                            case CRITICAL_HIT:
                                return random(1, 2);
                            case DAMAGE:
                                return random(20, 35);
                            case DEXTERITY:
                                return random(1, 15);
                            case FIRE_DAMAGE:
                                return random(3, 5);
                            case ICE_DAMAGE:
                                return random(3, 5);
                            case POISON_DAMAGE:
                                return random(3, 5);
                            case LIFE_STEAL:
                                return random(1, 2);
                            case PURE_DAMAGE:
                                return random(5, 26);
                            case VITALITY:
                                return random(1, 15);
                            case VS_MONSTERS:
                                return random(1, 7);
                            case VS_PLAYER:
                                return random(1, 7);
                            case STRENGTH:
                                return random(1, 15);
                            case INTELLECT:
                                return random(1, 15);
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
                                return random(3, 5);
                            case CRITICAL_HIT:
                                return random(2, 3);
                            case DAMAGE:
                                return random(20, 35);
                            case DEXTERITY:
                                return random(1, 35);
                            case FIRE_DAMAGE:
                                return random(5, 7);
                            case ICE_DAMAGE:
                                return random(5, 7);
                            case POISON_DAMAGE:
                                return random(5, 7);
                            case LIFE_STEAL:
                                return random(2, 3);
                            case PURE_DAMAGE:
                                return random(3, 7);
                            case VITALITY:
                                return random(1, 35);
                            case VS_MONSTERS:
                                return random(6, 11);
                            case VS_PLAYER:
                                return random(6, 11);
                            case STRENGTH:
                                return random(1, 35);
                            case INTELLECT:
                                return random(1, 35);
                        }
                    case UNCOMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(9, 15);
                            case ARMOR_PENETRATION:
                                return random(3, 5);
                            case CRITICAL_HIT:
                                return random(2, 3);
                            case DAMAGE:
                                return random(20, 35);
                            case DEXTERITY:
                                return random(1, 35);
                            case FIRE_DAMAGE:
                                return random(6, 7);
                            case ICE_DAMAGE:
                                return random(6, 7);
                            case POISON_DAMAGE:
                                return random(6, 7);
                            case LIFE_STEAL:
                                return random(2, 3);
                            case PURE_DAMAGE:
                                return random(5, 7);
                            case VITALITY:
                                return random(1, 35);
                            case VS_MONSTERS:
                                return random(6, 11);
                            case VS_PLAYER:
                                return random(6, 11);
                            case STRENGTH:
                                return random(1, 35);
                            case INTELLECT:
                                return random(1, 35);
                        }
                    case RARE:
                        switch (type) {
                            case ACCURACY:
                                return random(13, 19);
                            case ARMOR_PENETRATION:
                                return random(3, 5);
                            case CRITICAL_HIT:
                                return random(2, 3);
                            case DAMAGE:
                                return random(25, 45);
                            case DEXTERITY:
                                return random(1, 35);
                            case FIRE_DAMAGE:
                                return random(6, 8);
                            case ICE_DAMAGE:
                                return random(6, 8);
                            case POISON_DAMAGE:
                                return random(6, 8);
                            case LIFE_STEAL:
                                return random(2, 3);
                            case PURE_DAMAGE:
                                return random(5, 9);
                            case VITALITY:
                                return random(1, 35);
                            case VS_MONSTERS:
                                return random(6, 11);
                            case VS_PLAYER:
                                return random(6, 11);
                            case STRENGTH:
                                return random(1, 35);
                            case INTELLECT:
                                return random(1, 35);
                        }
                    case UNIQUE:
                        switch (type) {
                            case ACCURACY:
                                return random(15, 19);
                            case ARMOR_PENETRATION:
                                return random(3, 5);
                            case CRITICAL_HIT:
                                return random(2, 3);
                            case DAMAGE:
                                return random(100, 120);
                            case DEXTERITY:
                                return random(1, 35);
                            case FIRE_DAMAGE:
                                return random(6, 9);
                            case ICE_DAMAGE:
                                return random(6, 9);
                            case POISON_DAMAGE:
                                return random(6, 9);
                            case LIFE_STEAL:
                                return random(2, 3);
                            case PURE_DAMAGE:
                                return random(6, 10);
                            case VITALITY:
                                return random(1, 35);
                            case VS_MONSTERS:
                                return random(6, 11);
                            case VS_PLAYER:
                                return random(6, 11);
                            case STRENGTH:
                                return random(1, 35);
                            case INTELLECT:
                                return random(1, 35);
                        }
                    case LEGENDARY:
                        switch (type) {
                            case ACCURACY:
                                return random(17, 21);
                            case ARMOR_PENETRATION:
                                return random(4, 5);
                            case CRITICAL_HIT:
                                return random(2, 3);
                            case DAMAGE:
                                return random(120, 150);
                            case DEXTERITY:
                                return random(1, 35);
                            case FIRE_DAMAGE:
                                return random(6, 9);
                            case ICE_DAMAGE:
                                return random(6, 9);
                            case POISON_DAMAGE:
                                return random(6, 9);
                            case LIFE_STEAL:
                                return random(2, 3);
                            case PURE_DAMAGE:
                                return random(6, 13);
                            case VITALITY:
                                return random(1, 35);
                            case VS_MONSTERS:
                                return random(6, 11);
                            case VS_PLAYER:
                                return random(6, 11);
                            case STRENGTH:
                                return random(1, 35);
                            case INTELLECT:
                                return random(1, 35);
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
                                return random(5, 8);
                            case CRITICAL_HIT:
                                return random(3, 5);
                            case DAMAGE:
                                return random(100, 120);
                            case DEXTERITY:
                                return random(1, 75);
                            case FIRE_DAMAGE:
                                return random(10, 13);
                            case ICE_DAMAGE:
                                return random(10, 13);
                            case POISON_DAMAGE:
                                return random(10, 13);
                            case LIFE_STEAL:
                                return random(3, 5);
                            case PURE_DAMAGE:
                                return random(8, 15);
                            case VITALITY:
                                return random(1, 75);
                            case VS_MONSTERS:
                                return random(8, 14);
                            case VS_PLAYER:
                                return random(8, 14);
                            case STRENGTH:
                                return random(1, 75);
                            case INTELLECT:
                                return random(1, 75);
                        }
                    case UNCOMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(18, 27);
                            case ARMOR_PENETRATION:
                                return random(5, 8);
                            case CRITICAL_HIT:
                                return random(3, 5);
                            case DAMAGE:
                                return random(100, 130);
                            case DEXTERITY:
                                return random(1, 75);
                            case FIRE_DAMAGE:
                                return random(10, 14);
                            case ICE_DAMAGE:
                                return random(10, 14);
                            case POISON_DAMAGE:
                                return random(10, 14);
                            case LIFE_STEAL:
                                return random(3, 5);
                            case PURE_DAMAGE:
                                return random(13, 18);
                            case VITALITY:
                                return random(1, 75);
                            case VS_MONSTERS:
                                return random(8, 14);
                            case VS_PLAYER:
                                return random(8, 14);
                            case STRENGTH:
                                return random(1, 75);
                            case INTELLECT:
                                return random(1, 75);
                        }
                    case RARE:
                        switch (type) {
                            case ACCURACY:
                                return random(24, 27);
                            case ARMOR_PENETRATION:
                                return random(5, 8);
                            case CRITICAL_HIT:
                                return random(3, 5);
                            case DAMAGE:
                                return random(120, 150);
                            case DEXTERITY:
                                return random(1, 75);
                            case FIRE_DAMAGE:
                                return random(10, 15);
                            case ICE_DAMAGE:
                                return random(10, 15);
                            case POISON_DAMAGE:
                                return random(10, 15);
                            case LIFE_STEAL:
                                return random(3, 5);
                            case PURE_DAMAGE:
                                return random(15, 18);
                            case VITALITY:
                                return random(1, 75);
                            case VS_MONSTERS:
                                return random(8, 14);
                            case VS_PLAYER:
                                return random(8, 14);
                            case STRENGTH:
                                return random(1, 75);
                            case INTELLECT:
                                return random(1, 75);
                        }
                    case UNIQUE:
                        switch (type) {
                            case ACCURACY:
                                return random(24, 27);
                            case ARMOR_PENETRATION:
                                return random(5, 8);
                            case CRITICAL_HIT:
                                return random(3, 5);
                            case DAMAGE:
                                return random(170, 190);
                            case DEXTERITY:
                                return random(1, 75);
                            case FIRE_DAMAGE:
                                return random(10, 16);
                            case ICE_DAMAGE:
                                return random(10, 16);
                            case POISON_DAMAGE:
                                return random(10, 16);
                            case LIFE_STEAL:
                                return random(3, 5);
                            case PURE_DAMAGE:
                                return random(15, 18);
                            case VITALITY:
                                return random(1, 75);
                            case VS_MONSTERS:
                                return random(8, 14);
                            case VS_PLAYER:
                                return random(8, 14);
                            case STRENGTH:
                                return random(1, 75);
                            case INTELLECT:
                                return random(1, 75);
                        }
                    case LEGENDARY:
                        switch (type) {
                            case ACCURACY:
                                return random(26, 30);
                            case ARMOR_PENETRATION:
                                return random(5, 8);
                            case CRITICAL_HIT:
                                return random(3, 5);
                            case DAMAGE:
                                return random(195, 205);
                            case DEXTERITY:
                                return random(1, 75);
                            case FIRE_DAMAGE:
                                return random(10, 17);
                            case ICE_DAMAGE:
                                return random(10, 17);
                            case POISON_DAMAGE:
                                return random(10, 17);
                            case LIFE_STEAL:
                                return random(3, 5);
                            case PURE_DAMAGE:
                                return random(15, 22);
                            case VITALITY:
                                return random(1, 75);
                            case VS_MONSTERS:
                                return random(8, 14);
                            case VS_PLAYER:
                                return random(8, 14);
                            case STRENGTH:
                                return random(1, 75);
                            case INTELLECT:
                                return random(1, 75);
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
                                return random(8, 12);
                            case CRITICAL_HIT:
                                return random(5, 7);
                            case DAMAGE:
                                return random(170, 190);
                            case DEXTERITY:
                                return random(1, 155);
                            case FIRE_DAMAGE:
                                return random(19, 24);
                            case ICE_DAMAGE:
                                return random(19, 24);
                            case POISON_DAMAGE:
                                return random(19, 24);
                            case LIFE_STEAL:
                                return random(5, 7);
                            case PURE_DAMAGE:
                                return random(17, 24);
                            case VITALITY:
                                return random(1, 155);
                            case VS_MONSTERS:
                                return random(11, 17);
                            case VS_PLAYER:
                                return random(11, 17);
                            case STRENGTH:
                                return random(1, 155);
                            case INTELLECT:
                                return random(1, 155);
                        }
                    case UNCOMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(26, 32);
                            case ARMOR_PENETRATION:
                                return random(8, 12);
                            case CRITICAL_HIT:
                                return random(5, 7);
                            case DAMAGE:
                                return random(195, 205);
                            case DEXTERITY:
                                return random(1, 155);
                            case FIRE_DAMAGE:
                                return random(20, 25);
                            case ICE_DAMAGE:
                                return random(20, 25);
                            case POISON_DAMAGE:
                                return random(20, 25);
                            case LIFE_STEAL:
                                return random(5, 7);
                            case PURE_DAMAGE:
                                return random(17, 25);
                            case VITALITY:
                                return random(1, 155);
                            case VS_MONSTERS:
                                return random(11, 17);
                            case VS_PLAYER:
                                return random(11, 17);
                            case STRENGTH:
                                return random(1, 155);
                            case INTELLECT:
                                return random(1, 155);
                        }
                    case RARE:
                        switch (type) {
                            case ACCURACY:
                                return random(26, 32);
                            case ARMOR_PENETRATION:
                                return random(8, 12);
                            case CRITICAL_HIT:
                                return random(5, 7);
                            case DAMAGE:
                                return random(205, 225);
                            case DEXTERITY:
                                return random(1, 155);
                            case FIRE_DAMAGE:
                                return random(21, 26);
                            case ICE_DAMAGE:
                                return random(21, 26);
                            case POISON_DAMAGE:
                                return random(21, 26);
                            case LIFE_STEAL:
                                return random(5, 7);
                            case PURE_DAMAGE:
                                return random(18, 25);
                            case VITALITY:
                                return random(1, 155);
                            case VS_MONSTERS:
                                return random(11, 17);
                            case VS_PLAYER:
                                return random(11, 17);
                            case STRENGTH:
                                return random(1, 155);
                            case INTELLECT:
                                return random(1, 155);
                        }
                    case UNIQUE:
                        switch (type) {
                            case ACCURACY:
                                return random(26, 34);
                            case ARMOR_PENETRATION:
                                return random(8, 12);
                            case CRITICAL_HIT:
                                return random(5, 7);
                            case DAMAGE:
                                return random(250, 270);
                            case DEXTERITY:
                                return random(1, 155);
                            case FIRE_DAMAGE:
                                return random(22, 27);
                            case ICE_DAMAGE:
                                return random(21, 27);
                            case POISON_DAMAGE:
                                return random(21, 27);
                            case LIFE_STEAL:
                                return random(5, 7);
                            case PURE_DAMAGE:
                                return random(20, 25);
                            case VITALITY:
                                return random(1, 155);
                            case VS_MONSTERS:
                                return random(11, 17);
                            case VS_PLAYER:
                                return random(11, 17);
                            case STRENGTH:
                                return random(1, 155);
                            case INTELLECT:
                                return random(1, 155);
                        }
                    case LEGENDARY:
                        switch (type) {
                            case ACCURACY:
                                return random(26, 35);
                            case ARMOR_PENETRATION:
                                return random(8, 12);
                            case CRITICAL_HIT:
                                return random(5, 7);
                            case DAMAGE:
                                return random(270, 275);
                            case DEXTERITY:
                                return random(1, 155);
                            case FIRE_DAMAGE:
                                return random(21, 28);
                            case ICE_DAMAGE:
                                return random(21, 28);
                            case POISON_DAMAGE:
                                return random(21, 28);
                            case LIFE_STEAL:
                                return random(5, 7);
                            case PURE_DAMAGE:
                                return random(20, 25);
                            case VITALITY:
                                return random(1, 155);
                            case VS_MONSTERS:
                                return random(11, 17);
                            case VS_PLAYER:
                                return random(11, 17);
                            case STRENGTH:
                                return random(1, 155);
                            case INTELLECT:
                                return random(1, 155);
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
                                return random(12, 15);
                            case CRITICAL_HIT:
                                return random(7, 10);
                            case DAMAGE:
                                return random(270, 290);
                            case DEXTERITY:
                                return random(1, 315);
                            case FIRE_DAMAGE:
                                return random(36, 38);
                            case ICE_DAMAGE:
                                return random(36, 48);
                            case POISON_DAMAGE:
                                return random(36, 48);
                            case LIFE_STEAL:
                                return random(7, 10);
                            case PURE_DAMAGE:
                                return random(20, 28);
                            case VITALITY:
                                return random(1, 315);
                            case VS_MONSTERS:
                                return random(14, 20);
                            case VS_PLAYER:
                                return random(14, 20);
                            case STRENGTH:
                                return random(1, 315);
                            case INTELLECT:
                                return random(1, 315);
                        }
                    case UNCOMMON:
                        switch (type) {
                            case ACCURACY:
                                return random(26, 35);
                            case ARMOR_PENETRATION:
                                return random(12, 15);
                            case CRITICAL_HIT:
                                return random(7, 10);
                            case DAMAGE:
                                return random(270, 290);
                            case DEXTERITY:
                                return random(1, 315);
                            case FIRE_DAMAGE:
                                return random(38, 49);
                            case ICE_DAMAGE:
                                return random(38, 49);
                            case POISON_DAMAGE:
                                return random(38, 49);
                            case LIFE_STEAL:
                                return random(7, 10);
                            case PURE_DAMAGE:
                                return random(24, 28);
                            case VITALITY:
                                return random(1, 315);
                            case VS_MONSTERS:
                                return random(14, 20);
                            case VS_PLAYER:
                                return random(14, 20);
                            case STRENGTH:
                                return random(1, 315);
                            case INTELLECT:
                                return random(1, 315);
                        }
                    case RARE:
                        switch (type) {
                            case ACCURACY:
                                return random(27, 37);
                            case ARMOR_PENETRATION:
                                return random(12, 15);
                            case CRITICAL_HIT:
                                return random(7, 10);
                            case DAMAGE:
                                return random(290, 315);
                            case DEXTERITY:
                                return random(1, 315);
                            case FIRE_DAMAGE:
                                return random(40, 51);
                            case ICE_DAMAGE:
                                return random(40, 51);
                            case POISON_DAMAGE:
                                return random(40, 51);
                            case LIFE_STEAL:
                                return random(7, 10);
                            case PURE_DAMAGE:
                                return random(25, 34);
                            case VITALITY:
                                return random(1, 315);
                            case VS_MONSTERS:
                                return random(14, 20);
                            case VS_PLAYER:
                                return random(14, 20);
                            case STRENGTH:
                                return random(1, 315);
                            case INTELLECT:
                                return random(1, 315);
                        }
                    case UNIQUE:
                        switch (type) {
                            case ACCURACY:
                                return random(26, 40);
                            case ARMOR_PENETRATION:
                                return random(13, 15);
                            case CRITICAL_HIT:
                                return random(8, 10);
                            case DAMAGE:
                                return random(315, 325);
                            case DEXTERITY:
                                return random(1, 315);
                            case FIRE_DAMAGE:
                                return random(43, 55);
                            case ICE_DAMAGE:
                                return random(43, 55);
                            case POISON_DAMAGE:
                                return random(43, 55);
                            case LIFE_STEAL:
                                return random(8, 10);
                            case PURE_DAMAGE:
                                return random(23, 31);
                            case VITALITY:
                                return random(1, 315);
                            case VS_MONSTERS:
                                return random(14, 20);
                            case VS_PLAYER:
                                return random(14, 20);
                            case STRENGTH:
                                return random(1, 315);
                            case INTELLECT:
                                return random(1, 315);
                        }
                    case LEGENDARY:
                        switch (type) {
                            case ACCURACY:
                                return random(30, 40);
                            case ARMOR_PENETRATION:
                                return random(13, 15);
                            case CRITICAL_HIT:
                                return random(8, 10);
                            case DAMAGE:
                                return random(325, 350);
                            case DEXTERITY:
                                return random(1, 315);
                            case FIRE_DAMAGE:
                                return random(46, 55);
                            case ICE_DAMAGE:
                                return random(46, 55);
                            case POISON_DAMAGE:
                                return random(46, 55);
                            case LIFE_STEAL:
                                return random(8, 10);
                            case PURE_DAMAGE:
                                return random(25, 32);
                            case VITALITY:
                                return random(1, 315);
                            case VS_MONSTERS:
                                return random(14, 20);
                            case VS_PLAYER:
                                return random(14, 20);
                            case STRENGTH:
                                return random(1, 315);
                            case INTELLECT:
                                return random(1, 315);
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