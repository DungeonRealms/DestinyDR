package net.dungeonrealms.game.world.items;

import net.dungeonrealms.game.world.items.armor.Armor;

import java.util.Random;

/**
 * Created by Nick on 9/19/2015.
 */
public class DamageMeta {

    /**
     * Generates values for Armor attributes based on the
     * Armor tier and rarity.
     * @param tier
     * @param modifier
     * @param type
     * @return int
     * @since 1.0
     */
    public int nextArmor(Armor.ArmorTier tier, Armor.ArmorModifier modifier, Armor.ArmorAttributeType type) {
        switch (tier) {
            case TIER_1:
                switch (modifier) {
                    case COMMON:
                        switch (type) {
                            case ARMOR:
                                return random(1, 2);
                            case HEALTH_POINTS:
                                return random(10, 90);
                            case HEALTH_REGEN:
                                return random(20, 35);
                            case ENERGY_REGEN:
                                return random(1, 2);
                            case INTELLECT:
                                return random(1, 15);
                            case FIRE_RESISTANCE:
                                return random(1, 5);
                            case BLOCK:
                                return random(1, 5);
                            case LUCK:
                                return random(1, 5);
                            case THORNS:
                                return random(1, 3);
                            case STRENGTH:
                                return random(1, 15);
                            case VITALITY:
                                return random(1, 15);
                            case DODGE:
                                return random(1, 5);
                            case DAMAGE:
                                return random(1, 2);
                            case DEXTERITY:
                                return random(1, 15);
                        }
                    case UNCOMMON:
                        switch (type) {
                            case ARMOR:
                                return random(1, 2);
                            case HEALTH_POINTS:
                                return random(90, 150);
                            case HEALTH_REGEN:
                                return random(22, 35);
                            case ENERGY_REGEN:
                                return random(1, 2);
                            case INTELLECT:
                                return random(3, 15);
                            case FIRE_RESISTANCE:
                                return random(1, 5);
                            case BLOCK:
                                return random(1, 5);
                            case LUCK:
                                return random(1, 5);
                            case THORNS:
                                return random(1, 3);
                            case STRENGTH:
                                return random(3, 15);
                            case VITALITY:
                                return random(3, 15);
                            case DODGE:
                                return random(1, 5);
                            case DAMAGE:
                                return random(1, 2);
                            case DEXTERITY:
                                return random(3, 15);
                        }
                    case RARE:
                        switch (type) {
                            case ARMOR:
                                return random(1, 2);
                            case HEALTH_POINTS:
                                return random(150, 180);
                            case HEALTH_REGEN:
                                return random(24, 35);
                            case ENERGY_REGEN:
                                return random(1, 2);
                            case INTELLECT:
                                return random(4, 15);
                            case FIRE_RESISTANCE:
                                return random(2, 5);
                            case BLOCK:
                                return random(2, 5);
                            case LUCK:
                                return random(1, 5);
                            case THORNS:
                                return random(1, 3);
                            case STRENGTH:
                                return random(5, 15);
                            case VITALITY:
                                return random(5, 15);
                            case DODGE:
                                return random(2, 5);
                            case DAMAGE:
                                return random(1, 2);
                            case DEXTERITY:
                                return random(5, 15);
                        }
                    case UNIQUE:
                        switch (type) {
                            case ARMOR:
                                return random(1, 2);
                            case HEALTH_POINTS:
                                return random(200, 280);
                            case HEALTH_REGEN:
                                return random(26, 40);
                            case ENERGY_REGEN:
                                return random(1, 2);
                            case INTELLECT:
                                return random(7, 15);
                            case FIRE_RESISTANCE:
                                return random(2, 5);
                            case BLOCK:
                                return random(3, 5);
                            case LUCK:
                                return random(2, 5);
                            case THORNS:
                                return random(1, 3);
                            case STRENGTH:
                                return random(7, 15);
                            case VITALITY:
                                return random(7, 15);
                            case DODGE:
                                return random(3, 5);
                            case DAMAGE:
                                return random(1, 2);
                            case DEXTERITY:
                                return random(7, 15);
                        }
                    case LEGENDARY:
                        switch (type) {
                            case ARMOR:
                                return random(2, 3);
                            case HEALTH_POINTS:
                                return random(300, 420);
                            case HEALTH_REGEN:
                                return random(26, 45);
                            case ENERGY_REGEN:
                                return random(1, 3);
                            case INTELLECT:
                                return random(7, 15);
                            case FIRE_RESISTANCE:
                                return random(2, 5);
                            case BLOCK:
                                return random(3, 5);
                            case LUCK:
                                return random(2, 5);
                            case THORNS:
                                return random(1, 3);
                            case STRENGTH:
                                return random(7, 15);
                            case VITALITY:
                                return random(7, 15);
                            case DODGE:
                                return random(3, 5);
                            case DAMAGE:
                                return random(1, 2);
                            case DEXTERITY:
                                return random(7, 15);
                        }
                }
                break;
            case TIER_2:
                switch (modifier) {
                    case COMMON:
                        switch (type) {
                            case ARMOR:
                                return random(1, 2);
                            case HEALTH_POINTS:
                                return random(260, 340);
                            case HEALTH_REGEN:
                                return random(45, 60);
                            case ENERGY_REGEN:
                                return random(1, 3);
                            case INTELLECT:
                                return random(10, 35);
                            case FIRE_RESISTANCE:
                                return random(1, 7);
                            case BLOCK:
                                return random(1, 8);
                            case LUCK:
                                return random(1, 8);
                            case THORNS:
                                return random(1, 5);
                            case STRENGTH:
                                return random(10, 35);
                            case VITALITY:
                                return random(10, 35);
                            case DODGE:
                                return random(1, 8);
                            case DAMAGE:
                                return random(2, 3);
                            case DEXTERITY:
                                return random(10, 35);
                        }
                    case UNCOMMON:
                        switch (type) {
                            case ARMOR:
                                return random(2, 3);
                            case HEALTH_POINTS:
                                return random(420, 500);
                            case HEALTH_REGEN:
                                return random(47, 60);
                            case ENERGY_REGEN:
                                return random(1, 3);
                            case INTELLECT:
                                return random(10, 35);
                            case FIRE_RESISTANCE:
                                return random(1, 7);
                            case BLOCK:
                                return random(1, 8);
                            case LUCK:
                                return random(1, 8);
                            case THORNS:
                                return random(1, 5);
                            case STRENGTH:
                                return random(10, 35);
                            case VITALITY:
                                return random(10, 35);
                            case DODGE:
                                return random(1, 8);
                            case DAMAGE:
                                return random(2, 3);
                            case DEXTERITY:
                                return random(10, 35);
                        }
                    case RARE:
                        switch (type) {
                            case ARMOR:
                                return random(2, 3);
                            case HEALTH_POINTS:
                                return random(500, 580);
                            case HEALTH_REGEN:
                                return random(49, 60);
                            case ENERGY_REGEN:
                                return random(1, 3);
                            case INTELLECT:
                                return random(14, 35);
                            case FIRE_RESISTANCE:
                                return random(2, 7);
                            case BLOCK:
                                return random(2, 8);
                            case LUCK:
                                return random(1, 8);
                            case THORNS:
                                return random(1, 5);
                            case STRENGTH:
                                return random(14, 35);
                            case VITALITY:
                                return random(14, 35);
                            case DODGE:
                                return random(2, 8);
                            case DAMAGE:
                                return random(2, 3);
                            case DEXTERITY:
                                return random(14, 35);
                        }
                    case UNIQUE:
                        switch (type) {
                            case ARMOR:
                                return random(3, 4);
                            case HEALTH_POINTS:
                                return random(580, 700);
                            case HEALTH_REGEN:
                                return random(52, 65);
                            case ENERGY_REGEN:
                                return random(1, 3);
                            case INTELLECT:
                                return random(18, 35);
                            case FIRE_RESISTANCE:
                                return random(2, 7);
                            case BLOCK:
                                return random(3, 8);
                            case LUCK:
                                return random(2, 8);
                            case THORNS:
                                return random(1, 5);
                            case STRENGTH:
                                return random(18, 35);
                            case VITALITY:
                                return random(18, 35);
                            case DODGE:
                                return random(3, 8);
                            case DAMAGE:
                                return random(2, 3);
                            case DEXTERITY:
                                return random(18, 35);
                        }
                    case LEGENDARY:
                        switch (type) {
                            case ARMOR:
                                return random(4, 5);
                            case HEALTH_POINTS:
                                return random(700, 900);
                            case HEALTH_REGEN:
                                return random(55, 70);
                            case ENERGY_REGEN:
                                return random(1, 4);
                            case INTELLECT:
                                return random(20, 35);
                            case FIRE_RESISTANCE:
                                return random(4, 7);
                            case BLOCK:
                                return random(3, 8);
                            case LUCK:
                                return random(2, 8);
                            case THORNS:
                                return random(1, 5);
                            case STRENGTH:
                                return random(20, 35);
                            case VITALITY:
                                return random(20, 35);
                            case DODGE:
                                return random(3, 8);
                            case DAMAGE:
                                return random(2, 3);
                            case DEXTERITY:
                                return random(20, 35);
                        }
                }
                break;
            case TIER_3:
                switch (modifier) {
                    case COMMON:
                        switch (type) {
                            case ARMOR:
                                return random(3, 4);
                            case HEALTH_POINTS:
                                return random(580, 700);
                            case HEALTH_REGEN:
                                return random(65, 80);
                            case ENERGY_REGEN:
                                return random(1, 4);
                            case INTELLECT:
                                return random(20, 75);
                            case FIRE_RESISTANCE:
                                return random(1, 20);
                            case BLOCK:
                                return random(1, 10);
                            case LUCK:
                                return random(1, 10);
                            case THORNS:
                                return random(1, 10);
                            case STRENGTH:
                                return random(20, 75);
                            case VITALITY:
                                return random(20, 75);
                            case DODGE:
                                return random(1, 10);
                            case DAMAGE:
                                return random(3, 5);
                            case DEXTERITY:
                                return random(20, 75);
                        }
                    case UNCOMMON:
                        switch (type) {
                            case ARMOR:
                                return random(4, 5);
                            case HEALTH_POINTS:
                                return random(700, 900);
                            case HEALTH_REGEN:
                                return random(68, 80);
                            case ENERGY_REGEN:
                                return random(1, 4);
                            case INTELLECT:
                                return random(22, 75);
                            case FIRE_RESISTANCE:
                                return random(1, 20);
                            case BLOCK:
                                return random(1, 10);
                            case LUCK:
                                return random(1, 10);
                            case THORNS:
                                return random(1, 10);
                            case STRENGTH:
                                return random(22, 75);
                            case VITALITY:
                                return random(22, 75);
                            case DODGE:
                                return random(1, 10);
                            case DAMAGE:
                                return random(3, 5);
                            case DEXTERITY:
                                return random(22, 75);
                        }
                    case RARE:
                        switch (type) {
                            case ARMOR:
                                return random(5, 6);
                            case HEALTH_POINTS:
                                return random(900, 1100);
                            case HEALTH_REGEN:
                                return random(70, 80);
                            case ENERGY_REGEN:
                                return random(1, 4);
                            case INTELLECT:
                                return random(25, 75);
                            case FIRE_RESISTANCE:
                                return random(1, 20);
                            case BLOCK:
                                return random(2, 10);
                            case LUCK:
                                return random(1, 10);
                            case THORNS:
                                return random(1, 10);
                            case STRENGTH:
                                return random(25, 75);
                            case VITALITY:
                                return random(25, 75);
                            case DODGE:
                                return random(2, 10);
                            case DAMAGE:
                                return random(3, 5);
                            case DEXTERITY:
                                return random(25, 75);
                        }
                    case UNIQUE:
                        switch (type) {
                            case ARMOR:
                                return random(6, 8);
                            case HEALTH_POINTS:
                                return random(1100, 1350);
                            case HEALTH_REGEN:
                                return random(72, 80);
                            case ENERGY_REGEN:
                                return random(1, 4);
                            case INTELLECT:
                                return random(30, 75);
                            case FIRE_RESISTANCE:
                                return random(1, 20);
                            case BLOCK:
                                return random(3, 10);
                            case LUCK:
                                return random(2, 10);
                            case THORNS:
                                return random(1, 10);
                            case STRENGTH:
                                return random(30, 75);
                            case VITALITY:
                                return random(30, 75);
                            case DODGE:
                                return random(3, 10);
                            case DAMAGE:
                                return random(3, 7);
                            case DEXTERITY:
                                return random(30, 75);
                        }
                    case LEGENDARY:
                        switch (type) {
                            case ARMOR:
                                return random(8, 10);
                            case HEALTH_POINTS:
                                return random(1350, 1600);
                            case HEALTH_REGEN:
                                return random(75, 80);
                            case ENERGY_REGEN:
                                return random(1, 5);
                            case INTELLECT:
                                return random(40, 75);
                            case FIRE_RESISTANCE:
                                return random(1, 20);
                            case BLOCK:
                                return random(3, 10);
                            case LUCK:
                                return random(3, 10);
                            case THORNS:
                                return random(1, 10);
                            case STRENGTH:
                                return random(40, 75);
                            case VITALITY:
                                return random(40, 75);
                            case DODGE:
                                return random(3, 10);
                            case DAMAGE:
                                return random(3, 8);
                            case DEXTERITY:
                                return random(40, 75);
                        }
                }
                break;
            case TIER_4:
                switch (modifier) {
                    case COMMON:
                        switch (type) {
                            case ARMOR:
                                return random(6, 8);
                            case HEALTH_POINTS:
                                return random(1800, 2600);
                            case HEALTH_REGEN:
                                return random(110, 130);
                            case ENERGY_REGEN:
                                return random(1, 5);
                            case INTELLECT:
                                return random(40, 115);
                            case FIRE_RESISTANCE:
                                return random(1, 32);
                            case BLOCK:
                                return random(3, 10);
                            case LUCK:
                                return random(1, 10);
                            case THORNS:
                                return random(1, 10);
                            case STRENGTH:
                                return random(40, 115);
                            case VITALITY:
                                return random(40, 115);
                            case DODGE:
                                return random(1, 10);
                            case DAMAGE:
                                return random(7, 8);
                            case DEXTERITY:
                                return random(40, 115);
                        }
                    case UNCOMMON:
                        switch (type) {
                            case ARMOR:
                                return random(8, 10);
                            case HEALTH_POINTS:
                                return random(2600, 3200);
                            case HEALTH_REGEN:
                                return random(115, 130);
                            case ENERGY_REGEN:
                                return random(2, 5);
                            case INTELLECT:
                                return random(44, 115);
                            case FIRE_RESISTANCE:
                                return random(1, 32);
                            case BLOCK:
                                return random(3, 10);
                            case LUCK:
                                return random(1, 10);
                            case THORNS:
                                return random(1, 10);
                            case STRENGTH:
                                return random(44, 115);
                            case VITALITY:
                                return random(44, 115);
                            case DODGE:
                                return random(1, 10);
                            case DAMAGE:
                                return random(7, 8);
                            case DEXTERITY:
                                return random(44, 115);
                        }
                    case RARE:
                        switch (type) {
                            case ARMOR:
                                return random(10, 12);
                            case HEALTH_POINTS:
                                return random(3200, 3600);
                            case HEALTH_REGEN:
                                return random(117, 130);
                            case ENERGY_REGEN:
                                return random(2, 5);
                            case INTELLECT:
                                return random(50, 115);
                            case FIRE_RESISTANCE:
                                return random(1, 32);
                            case BLOCK:
                                return random(4, 11);
                            case LUCK:
                                return random(4, 11);
                            case THORNS:
                                return random(1, 11);
                            case STRENGTH:
                                return random(50, 115);
                            case VITALITY:
                                return random(50, 115);
                            case DODGE:
                                return random(2, 11);
                            case DAMAGE:
                                return random(8, 10);
                            case DEXTERITY:
                                return random(50, 115);
                        }
                    case UNIQUE:
                        switch (type) {
                            case ARMOR:
                                return random(12, 13);
                            case HEALTH_POINTS:
                                return random(3600, 3900);
                            case HEALTH_REGEN:
                                return random(120, 130);
                            case ENERGY_REGEN:
                                return random(2, 5);
                            case INTELLECT:
                                return random(55, 115);
                            case FIRE_RESISTANCE:
                                return random(1, 32);
                            case BLOCK:
                                return random(4, 11);
                            case LUCK:
                                return random(2, 11);
                            case THORNS:
                                return random(1, 11);
                            case STRENGTH:
                                return random(55, 115);
                            case VITALITY:
                                return random(55, 115);
                            case DODGE:
                                return random(4, 11);
                            case DAMAGE:
                                return random(8, 11);
                            case DEXTERITY:
                                return random(55, 115);
                        }
                    case LEGENDARY:
                        switch (type) {
                            case ARMOR:
                                return random(13, 14);
                            case HEALTH_POINTS:
                                return random(3900, 4200);
                            case HEALTH_REGEN:
                                return random(125, 130);
                            case ENERGY_REGEN:
                                return random(3, 6);
                            case INTELLECT:
                                return random(60, 115);
                            case FIRE_RESISTANCE:
                                return random(1, 32);
                            case BLOCK:
                                return random(4, 12);
                            case LUCK:
                                return random(3, 12);
                            case THORNS:
                                return random(1, 12);
                            case STRENGTH:
                                return random(60, 115);
                            case VITALITY:
                                return random(60, 115);
                            case DODGE:
                                return random(4, 12);
                            case DAMAGE:
                                return random(8, 12);
                            case DEXTERITY:
                                return random(60, 115);
                        }
                }
                break;
            case TIER_5:
                switch (modifier) {
                    case COMMON:
                        switch (type) {
                            case ARMOR:
                                return random(12, 13);
                            case HEALTH_POINTS:
                                return random(4200, 5800);
                            case HEALTH_REGEN:
                                return random(250, 300);
                            case ENERGY_REGEN:
                                return random(3, 6);
                            case INTELLECT:
                                return random(80, 315);
                            case FIRE_RESISTANCE:
                                return random(1, 45);
                            case BLOCK:
                                return random(3, 12);
                            case LUCK:
                                return random(3, 12);
                            case THORNS:
                                return random(3, 12);
                            case STRENGTH:
                                return random(80, 315);
                            case VITALITY:
                                return random(80, 315);
                            case DODGE:
                                return random(3, 12);
                            case DAMAGE:
                                return random(11, 12);
                            case DEXTERITY:
                                return random(80, 315);
                        }
                    case UNCOMMON:
                        switch (type) {
                            case ARMOR:
                                return random(13, 14);
                            case HEALTH_POINTS:
                                return random(5800, 6900);
                            case HEALTH_REGEN:
                                return random(250, 300);
                            case ENERGY_REGEN:
                                return random(3, 7);
                            case INTELLECT:
                                return random(85, 315);
                            case FIRE_RESISTANCE:
                                return random(1, 45);
                            case BLOCK:
                                return random(3, 12);
                            case LUCK:
                                return random(3, 12);
                            case THORNS:
                                return random(3, 12);
                            case STRENGTH:
                                return random(85, 315);
                            case VITALITY:
                                return random(85, 315);
                            case DODGE:
                                return random(3, 12);
                            case DAMAGE:
                                return random(12, 13);
                            case DEXTERITY:
                                return random(85, 315);
                        }
                    case RARE:
                        switch (type) {
                            case ARMOR:
                                return random(14, 15);
                            case HEALTH_POINTS:
                                return random(6900, 7700);
                            case HEALTH_REGEN:
                                return random(250, 300);
                            case ENERGY_REGEN:
                                return random(3, 7);
                            case INTELLECT:
                                return random(90, 315);
                            case FIRE_RESISTANCE:
                                return random(1, 45);
                            case BLOCK:
                                return random(4, 12);
                            case LUCK:
                                return random(4, 12);
                            case THORNS:
                                return random(4, 12);
                            case STRENGTH:
                                return random(90, 315);
                            case VITALITY:
                                return random(90, 315);
                            case DODGE:
                                return random(4, 12);
                            case DAMAGE:
                                return random(13, 15);
                            case DEXTERITY:
                                return random(90, 315);
                        }
                    case UNIQUE:
                        switch (type) {
                            case ARMOR:
                                return random(12, 13);
                            case HEALTH_POINTS:
                                return random(7700, 8200);
                            case HEALTH_REGEN:
                                return random(250, 300);
                            case ENERGY_REGEN:
                                return random(3, 7);
                            case INTELLECT:
                                return random(100, 315);
                            case FIRE_RESISTANCE:
                                return random(1, 45);
                            case BLOCK:
                                return random(4, 12);
                            case LUCK:
                                return random(4, 12);
                            case THORNS:
                                return random(4, 12);
                            case STRENGTH:
                                return random(100, 315);
                            case VITALITY:
                                return random(100, 315);
                            case DODGE:
                                return random(4, 12);
                            case DAMAGE:
                                return random(15, 17);
                            case DEXTERITY:
                                return random(100, 315);
                        }
                    case LEGENDARY:
                        switch (type) {
                            case ARMOR:
                                return random(15, 16);
                            case HEALTH_POINTS:
                                return random(8200, 8500);
                            case HEALTH_REGEN:
                                return random(250, 300);
                            case ENERGY_REGEN:
                                return random(4, 7);
                            case INTELLECT:
                                return random(110, 315);
                            case FIRE_RESISTANCE:
                                return random(1, 45);
                            case BLOCK:
                                return random(4, 12);
                            case LUCK:
                                return random(3, 12);
                            case THORNS:
                                return random(1, 12);
                            case STRENGTH:
                                return random(110, 315);
                            case VITALITY:
                                return random(110, 315);
                            case DODGE:
                                return random(4, 12);
                            case DAMAGE:
                                return random(17, 19);
                            case DEXTERITY:
                                return random(110, 315);
                        }
                }
                break;
            default:
                break;
        }
        return 0;
    }

    /**
     * Generates values for Item attributes based on the
     * Item tier and rarity.
     * @param tier
     * @param modifier
     * @param type
     * @return int
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
                                return random(15, 25);
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
                                return random(25, 40);
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
                                return random(30, 50);
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
                                return random(60, 80);
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
                                return random(90, 100);
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
                                return random(270, 275);
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
                                return random(275, 290);
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
                break;
        }
        return 0;
    }


    private int random(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }
}