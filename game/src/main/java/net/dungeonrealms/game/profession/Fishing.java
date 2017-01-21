package net.dungeonrealms.game.profession;

import com.google.common.collect.Maps;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.Map.Entry;

/**
 * Created by Chase on Oct 28, 2015
 */
public class Fishing implements GenericMechanic {
    /**
     * @param tier
     * @return
     */
    public static int getTierLvl(int tier) {
        switch (tier) {
            case 1:
                return 1;
            case 2:
                return 20;
            case 3:
                return 40;
            case 4:
                return 60;
            case 5:
                return 80;
        }
        return 1;
    }

    public static int getNextLevelUp(int tier) {
        if (tier == 1) {
            return 20;
        }
        if (tier == 2) {
            return 40;
        }
        if (tier == 3) {
            return 60;
        }
        if (tier == 4) {
            return 80;
        }
        if (tier == 5) {
            return 100;
        }
        return -1;
    }

    public static int getLvl(ItemStack i) {
        return CraftItemStack.asNMSCopy(i).getTag().getInt("level");
    }

    public static int getFishTier(ItemStack fish) {
        return CraftItemStack.asNMSCopy(fish).getTag().getInt("itemTier");
    }

    public static boolean hasEnchants(ItemStack is) {
        ItemMeta meta = is.getItemMeta();
        List<String> lore = meta.getLore();
        for (String line : lore) {
            for (FishingRodEnchant enchants : FishingRodEnchant.values()) {
                if (line.contains(enchants.name))
                    return true;
            }
        }
        return false;
    }

    public enum EnumFish {
        Shrimp("A raw and pink crustacean", 1),
        Anchovie("A small blue, oily fish", 1),
        Crayfish("A lobster-like and brown crustacean", 1),
        Carp("A Large, silver-scaled fish", 2),
        Herring("A colourful and medium-sized fish", 2),
        Sardine("A small and oily green fish", 2),
        Salmon("A beautiful jumping fish", 3),
        Trout("A non-migrating Salmon", 3),
        Cod("A cold-water, deep sea fish", 3),
        Lobster("A Large, red crustacean", 4),
        Tuna("A large, sapphire blue fish", 4),
        Bass("A very large and white fish", 4),
        Shark("A terrifying and massive predator", 5),
        Swordfish("An elongated fish with a long bill", 5),
        Monkfish("A flat, large, and scary-looking fish", 5);


        public int tier;
        public String desc;

        EnumFish(String desc, int tier) {
            this.desc = desc;
            this.tier = tier;
        }

        public static EnumFish getFish(int tier) {
            List<EnumFish> fishList = getTieredFishList(tier);
            return fishList.get(random.nextInt(fishList.size() - 1));
        }

        public ItemStack buildFish(EnumFish fish) {
            ItemStack stack = null;
            switch (fish.tier) {
                case 1:
                    stack = new ItemStack(Material.RAW_FISH, 1, (short) 0);
                    break;
                case 2:
                    stack = new ItemStack(Material.RAW_FISH, 1, (short) 2);
                    break;
                case 3:
                    stack = new ItemStack(Material.RAW_FISH, 1, (short) 1);
                    break;
                case 4:
                    stack = new ItemStack(Material.RAW_FISH, 1, (short) 3);
                    break;
                case 5:
                    stack = new ItemStack(Material.COOKED_FISH, 1);
                    break;
            }

            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(fish.name());
            List<String> lore = new ArrayList<>();


            return stack;
        }

        private static List<EnumFish> getTieredFishList(int tier) {
            List<EnumFish> fishList = new ArrayList<>();
            for (EnumFish fish : values()) {
                if (fish.tier == tier)
                    fishList.add(fish);
            }
            return fishList;
        }

        public static String getFishDesc(String fish_name) {
            for (EnumFish fish : values()) {
                if (fish.name().equalsIgnoreCase(fish_name))
                    return fish.desc;
            }
            return "A freshly caught fish.";
        }
    }

    private static Random random = new Random();


    public static ItemStack getFishDrop(int tier) {
        int fish_type = random.nextInt(3); // 0, 1, 2
        String fish_name = "";
        int hunger_to_heal = 0;

        int buff_chance = 0;
        int do_i_buff = random.nextInt(100);

        boolean fish_buff = false;
        String fish_buff_s = "";

        if (tier == 1) {
            buff_chance = 20;
            hunger_to_heal = 10;// %

            if (fish_type == 0) {
                fish_name = ChatColor.WHITE.toString() + "Shrimp";
            } else if (fish_type == 1) {
                fish_name = ChatColor.WHITE.toString() + "Anchovies";
            } else if (fish_type == 2) {
                fish_name = ChatColor.WHITE.toString() + "Crayfish";
            }

            if (buff_chance >= do_i_buff) {
                fish_buff = true;
                int buff_type = random.nextInt(100);
                int buff_val = 0;
                if (buff_type >= 0 && buff_type <= 15) {
                    // Of Power (DMG) 1-2%
                    buff_val = random.nextInt(2) + 1;
                    fish_name += " of Lesser Power";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% DMG " + ChatColor.GRAY.toString() + "(20s)";
                } else if (buff_type > 15 && buff_type <= 25) {
                    // Of Health 1-3% HP (instant heal)
                    buff_val = random.nextInt(3) + 1;
                    fish_name = ChatColor.WHITE.toString() + "Small, Healing " + fish_name;
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% HP " + ChatColor.GRAY.toString() + "(instant)";
                } else if (buff_type > 25 && buff_type <= 50) {
                    // Of Speed 15 seconds of speed I.
                    fish_name += " of Lesser Agility";
                    fish_buff_s = ChatColor.RED.toString() + "SPEED (I) BUFF " + ChatColor.GRAY.toString() + "(15s)";
                } else if (buff_type > 50 && buff_type <= 60) {
                    // Of Satiety, fill up 20% of food (2 full squares)
                    buff_val = 20;
                    fish_name += " of Minor Satiety";
                    fish_buff_s = ChatColor.RED.toString() + "-" + buff_val + "% HUNGER " + ChatColor.GRAY.toString() + "(instant)";
                } else if (buff_type > 60 && buff_type <= 70) {
                    // Of Defence (ARMOR%) 1-2% ARMOR
                    buff_val = random.nextInt(2) + 1;
                    fish_name += " of Weak Defense";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% ARMOR " + ChatColor.GRAY.toString() + "(20s)";
                } else if (buff_type > 70) {
                    // Nightvision for 60 seconds.
                    buff_val = random.nextInt(2) + 1;
                    fish_name += " of Vision";
                    fish_buff_s = ChatColor.RED.toString() + "NIGHTVISION (I) BUFF " + ChatColor.GRAY.toString() + "(30s)";
                }
            }
        } else if (tier == 2) {
            buff_chance = 25;
            hunger_to_heal = 20;// %

            if (fish_type == 0) {
                fish_name = ChatColor.GREEN.toString() + "Heron";
            } else if (fish_type == 1) {
                fish_name = ChatColor.GREEN.toString() + "Herring";
            } else if (fish_type == 2) {
                fish_name = ChatColor.GREEN.toString() + "Sardine";
            }
            if (buff_chance >= do_i_buff) {
                fish_buff = true;
                int buff_type = random.nextInt(100);
                int buff_val = 0;
                if (buff_type >= 0 && buff_type <= 10) {
                    // Of Power (DMG) 1-2%
                    buff_val = random.nextInt(3) + 1;
                    fish_name += " of Power";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% DMG " + ChatColor.GRAY.toString() + "(25s)";
                } else if (buff_type > 10 && buff_type <= 15) {
                    // Of HP REGEN
                    buff_val = random.nextInt(5) + 5;
                    fish_name += " of Regeneration";
                    fish_buff_s = ChatColor.RED.toString() + "REGEN " + buff_val + "% HP " + ChatColor.GRAY.toString() + "(over 10s)";
                } else if (buff_type > 15 && buff_type <= 20) {
                    // OF BLOCK%
                    buff_val = random.nextInt(5) + 1;
                    fish_name += " of Blocking";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% BLOCK " + ChatColor.GRAY.toString() + "(25s)";
                } else if (buff_type > 20 && buff_type <= 30) {
                    // Of Health (instant heal)
                    buff_val = random.nextInt(5) + 1;
                    fish_name = ChatColor.GREEN.toString() + "Healing " + fish_name;
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% HP " + ChatColor.GRAY.toString() + "(instant)";
                } else if (buff_type > 30 && buff_type <= 55) {
                    fish_name += " of Agility";
                    fish_buff_s = ChatColor.RED.toString() + "SPEED (I) BUFF " + ChatColor.GRAY.toString() + "(20s)";
                } else if (buff_type > 55 && buff_type <= 65) {
                    // Of Satiety, fill up 20% of food (2 full squares)
                    buff_val = 25;
                    fish_name += " of Satiety";
                    fish_buff_s = ChatColor.RED.toString() + "-" + buff_val + "% HUNGER " + ChatColor.GRAY.toString() + "(instant)";
                } else if (buff_type > 65 && buff_type <= 75) {
                    // Of Defence (ARMOR%) 1-2% ARMOR
                    buff_val = random.nextInt(3) + 1;
                    fish_name += " of Defense";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% ARMOR " + ChatColor.GRAY.toString() + "(25s)";
                } else if (buff_type > 75) {
                    // Nightvision for 60 seconds.
                    buff_val = random.nextInt(2) + 1;
                    fish_name += " of Vision";
                    fish_buff_s = ChatColor.RED.toString() + "NIGHTVISION (I) BUFF " + ChatColor.GRAY.toString() + "(45s)";
                }
            }
        } else if (tier == 3) {
            buff_chance = 33;
            hunger_to_heal = 30;// %

            if (fish_type == 0) {
                fish_name = ChatColor.AQUA.toString() + "Salmon";
            } else if (fish_type == 1) {
                fish_name = ChatColor.AQUA.toString() + "Trout";
            } else if (fish_type == 2) {
                fish_name = ChatColor.AQUA.toString() + "Cod";
            }
            if (buff_chance >= do_i_buff) {
                fish_buff = true;
                int buff_type = random.nextInt(100);
                int buff_val = 0;
                if (buff_type >= 0 && buff_type <= 10) {
                    // Of Power (DMG) 1-2%
                    buff_val = random.nextInt(3) + 3;
                    fish_name += " of Greater Power";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% DMG " + ChatColor.GRAY.toString() + "(30s)";
                } else if (buff_type > 10 && buff_type <= 15) {
                    // Of HP REGEN
                    buff_val = random.nextInt(11) + 5;
                    fish_name += " of Mighty Regeneration";
                    fish_buff_s = ChatColor.RED.toString() + "REGEN " + buff_val + "% HP " + ChatColor.GRAY.toString() + "(over 10s)";
                } else if (buff_type > 15 && buff_type <= 20) {
                    // OF BLOCK%
                    buff_val = random.nextInt(5) + 1;
                    fish_name += " of Blocking";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% BLOCK " + ChatColor.GRAY.toString() + "(30s)";
                } else if (buff_type > 20 && buff_type <= 30) {
                    // Of Health (instant heal)
                    buff_val = random.nextInt(4) + 4;
                    fish_name = ChatColor.AQUA.toString() + "Large, Healing " + fish_name;
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% HP " + ChatColor.GRAY.toString() + "(instant)";
                } else if (buff_type > 30 && buff_type <= 55) {
                    fish_name += " of Lasting Agility";
                    fish_buff_s = ChatColor.RED.toString() + "SPEED (I) BUFF " + ChatColor.GRAY.toString() + "(30s)";
                } else if (buff_type > 55 && buff_type <= 65) {
                    // Of Satiety, fill up 20% of food (2 full squares)
                    buff_val = 30;
                    fish_name += " of Great Satiety";
                    fish_buff_s = ChatColor.RED.toString() + "-" + buff_val + "% HUNGER " + ChatColor.GRAY.toString() + "(instant)";
                } else if (buff_type > 65 && buff_type <= 75) {
                    // Of Defence (ARMOR%) 1-2% ARMOR
                    buff_val = random.nextInt(3) + 3;
                    fish_name += " of Mighty Defense";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% ARMOR " + ChatColor.GRAY.toString() + "(30s)";
                } else if (buff_type > 75) {
                    // Nightvision for 60 seconds.
                    buff_val = random.nextInt(2) + 1;
                    fish_name += " of Lasting Vision";
                    fish_buff_s = ChatColor.RED.toString() + "NIGHTVISION (I) BUFF " + ChatColor.GRAY.toString() + "(60s)";
                }
            }
        } else if (tier == 4) {
            buff_chance = 33;
            hunger_to_heal = 40;// %

            if (fish_type == 0) {
                fish_name = ChatColor.LIGHT_PURPLE.toString() + "Lobster";
            } else if (fish_type == 1) {
                fish_name = ChatColor.LIGHT_PURPLE.toString() + "Tuna";
            } else if (fish_type == 2) {
                fish_name = ChatColor.LIGHT_PURPLE.toString() + "Bass";
            }

            int buff_time = random.nextInt(10) + 40; // Up to 49s.

            if (buff_chance >= do_i_buff) {
                fish_buff = true;
                int buff_type = random.nextInt(100);
                int buff_val = 0;
                if (buff_type >= 0 && buff_type <= 10) {
                    // Of Power (DMG) 1-2%
                    buff_val = random.nextInt(6) + 5;
                    fish_name += " of Ancient Power";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% DMG " + ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                } else if (buff_type > 10 && buff_type <= 15) {
                    // Of HP REGEN
                    buff_val = random.nextInt(6) + 10;
                    fish_name += " of Enhanced Regeneration";
                    fish_buff_s = ChatColor.RED.toString() + "REGEN " + buff_val + "% HP " + ChatColor.GRAY.toString() + "(over 10s)";
                } else if (buff_type > 15 && buff_type <= 20) {
                    // OF BLOCK%
                    buff_val = random.nextInt(5) + 4;
                    fish_name += " of Greater Blocking";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% BLOCK " + ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                } else if (buff_type > 20 && buff_type <= 30) {
                    // Of Health (instant heal)
                    buff_val = random.nextInt(4) + 4;
                    fish_name = ChatColor.LIGHT_PURPLE.toString() + "Healthy " + fish_name;
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% HP " + ChatColor.GRAY.toString() + "(instant)";
                } else if (buff_type > 30 && buff_type <= 55) {
                    fish_name += " of Bursting Agility";
                    fish_buff_s = ChatColor.RED.toString() + "SPEED (II) BUFF " + ChatColor.GRAY.toString() + "(15s)";
                } else if (buff_type > 55 && buff_type <= 65) {
                    // Of Satiety, fill up 20% of food (2 full squares)
                    buff_val = 30;
                    fish_name = ChatColor.LIGHT_PURPLE.toString() + "Huge " + fish_name;
                    fish_buff_s = ChatColor.RED.toString() + "-" + buff_val + "% HUNGER " + ChatColor.GRAY.toString() + "(instant)";
                } else if (buff_type > 65 && buff_type <= 75) {
                    // Of Defence (ARMOR%) 1-2% ARMOR
                    buff_val = random.nextInt(5) + 4;
                    fish_name += " of Fortified Defense";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% ARMOR " + ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                } else if (buff_type >= 75 && buff_type < 80) {
                    // Vampirism
                    buff_val = random.nextInt(2) + 4;
                    fish_name = "Albino " + ChatColor.LIGHT_PURPLE.toString() + fish_name;
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% LIFESTEAL " +
                            ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                } else if (buff_type > 80) {
                    // Nightvision for 60 seconds.
                    fish_name += " of Eagle Vision";
                    fish_buff_s = ChatColor.RED.toString() + "NIGHTVISION (II) BUFF " + ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                }
            }
        } else if (tier == 5) {
            buff_chance = 45;
            hunger_to_heal = 50;// %

            if (fish_type == 0) {
                fish_name = ChatColor.YELLOW.toString() + "Shark";
            } else if (fish_type == 1) {
                fish_name = ChatColor.YELLOW.toString() + "Swordfish";
            } else if (fish_type == 2) {
                fish_name = ChatColor.YELLOW.toString() + "Monkfish";
            }

            int buff_time = random.nextInt(11) + 50; // Up to 60s.

            if (buff_chance >= do_i_buff) {
                fish_buff = true;
                int buff_type = random.nextInt(100);
                int buff_val = 0;
                if (buff_type >= 0 && buff_type <= 10) {
                    // Of Power (DMG) 1-2%
                    buff_val = random.nextInt(11) + 5;
                    fish_name += " of Legendary Power";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% DMG " + ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                } else if (buff_type > 10 && buff_type <= 15) {
                    // Of HP REGEN
                    buff_val = random.nextInt(6) + 10;
                    fish_name += " of Extreme Regeneration";
                    fish_buff_s = ChatColor.RED.toString() + "REGEN " + buff_val + "% HP " + ChatColor.GRAY.toString() + "(over 10s)";
                } else if (buff_type > 15 && buff_type <= 20) {
                    // OF BLOCK%
                    buff_val = random.nextInt(5) + 4;
                    fish_name += " of Greater Blocking";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% BLOCK " + ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                } else if (buff_type > 20 && buff_type <= 30) {
                    // Of Health (instant heal)
                    buff_val = random.nextInt(6) + 5;
                    fish_name = ChatColor.YELLOW.toString() + "Legendary " + fish_name + " of Medicine";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% HP " + ChatColor.GRAY.toString() + "(instant)";
                } else if (buff_type > 30 && buff_type <= 45) {
                    fish_name += " of Godlike Speed";
                    fish_buff_s = ChatColor.RED.toString() + "SPEED (II) BUFF " + ChatColor.GRAY.toString() + "(30s)";
                } else if (buff_type > 45 && buff_type <= 50) {
                    // Of Satiety, fill up 20% of food (2 full squares)
                    buff_val = 40;
                    fish_name = ChatColor.YELLOW.toString() + "Gigantic " + fish_name;
                    fish_buff_s = ChatColor.RED.toString() + "-" + buff_val + "% HUNGER " + ChatColor.GRAY.toString() + "(instant)";
                } else if (buff_type > 50 && buff_type <= 60) {
                    // Of Defence (ARMOR%) 1-2% ARMOR
                    buff_val = random.nextInt(6) + 5;
                    fish_name = ChatColor.YELLOW.toString() + "Hardended " + fish_name + " of Legendary Defense";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% ARMOR " + ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                } else if (buff_type > 60 && buff_type <= 65) {
                    // Vampirism
                    buff_val = random.nextInt(5) + 3;
                    fish_name = "Albino " + ChatColor.YELLOW.toString() + fish_name;
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% LIFE STEAL " + ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                } else if (buff_type > 65 && buff_type <= 85) {
                    // Nightvision for 60 seconds.
                    fish_name += " of Omniscient Vision";
                    fish_buff_s = ChatColor.RED.toString() + "NIGHTVISION (II) BUFF " + ChatColor.GRAY.toString() + "(" + (buff_time + 60) + "s)";
                } else if (buff_type > 85 && buff_type <= 90) {
                    // Critical hit bonus
                    buff_val = random.nextInt(5) + 1;
                    fish_name = "Perfect " + ChatColor.YELLOW.toString() + fish_name + " of Accuracy";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% CRITICAL HIT " + ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                } else if (buff_type > 90) {
                    // Energy Regen Buff
                    buff_val = random.nextInt(5) + 1;
                    fish_name = ChatColor.YELLOW.toString() + fish_name + " of Hidden Energy";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% ENERGY REGEN " + ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                }
            }
        }

        List<String> fish_lore = new ArrayList<>();
        if (fish_buff) {
            fish_lore.add(fish_buff_s);
        }
        fish_lore.add(ChatColor.RED + "-" + hunger_to_heal + "% HUNGER " + ChatColor.GRAY.toString() + "(instant)");
        fish_lore.add(ChatColor.GRAY.toString() + EnumFish.getFishDesc(fish_name));


        if (fish_name.contains(ChatColor.WHITE.toString())) {
            fish_name = ChatColor.WHITE.toString() + "Raw " + fish_name;
        } else if (fish_name.contains(ChatColor.GREEN.toString())) {
            fish_name = ChatColor.GREEN.toString() + "Raw " + fish_name;
        } else if (fish_name.contains(ChatColor.AQUA.toString())) {
            fish_name = ChatColor.AQUA.toString() + "Raw " + fish_name;
        } else if (fish_name.contains(ChatColor.LIGHT_PURPLE.toString())) {
            fish_name = ChatColor.LIGHT_PURPLE.toString() + "Raw " + fish_name;
        } else if (fish_name.contains(ChatColor.YELLOW.toString())) {
            fish_name = ChatColor.YELLOW.toString() + "Raw " + fish_name;
        }

        ItemStack fish = new ItemStack(Material.RAW_FISH, 1);

        ItemMeta im = fish.getItemMeta();
        im.setDisplayName(fish_name);
        im.setLore(fish_lore);
        fish.setItemMeta(im);

        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(fish);
        nms.getTag().setInt("itemTier", tier);

        return CraftItemStack.asBukkitCopy(nms);
    }


    public static int getEXPNeeded(int level) {
        if (level == 1) {
            return 176; // formula doens't work on level 1.
        }
        if (level == 100) {
            return 0;
        }
        int previous_level = level - 1;
        return (int) (Math.pow((previous_level), 2) + ((previous_level) * 20) + 150 + ((previous_level) * 4) + getEXPNeeded((previous_level)));
    }

    public static int getFishEXP(int tier) {
        if (tier == 1) {
            return (int) (2.0D * (250 + random.nextInt((int) (250 * 0.3D))));
        }
        if (tier == 2) {
            return (int) (2.0D * (430 + random.nextInt((int) (430 * 0.3D))));
        }
        if (tier == 3) {
            return (int) (2.0D * (820 + random.nextInt((int) (820 * 0.3D))));
        }
        if (tier == 4) {
            return (int) (2.0D * (1050 + random.nextInt((int) (1050 * 0.3D))));
        }
        if (tier == 5) {
            return (int) (2.0D * (1230 + random.nextInt((int) (1230 * 0.3D))));
        }
        return 1;
    }

    /**
     * Check if itemstack is a DR fishing pole.
     *
     * @param stack
     * @return boolean
     * @since 1.0
     */
    public static boolean isDRFishingPole(ItemStack stack) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        return nms != null && nms.hasTag() && nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("rod") && stack.getType() == Material.FISHING_ROD;
    }

//    public static HashMap<UUID, String> fishBuffs = new HashMap<>();

    /**
     * Add Experience to the specified stack(fishing pole)
     *
     * @param stack
     */
    public static void gainExp(ItemStack stack, Player p, int exp) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        int currentXP = nms.getTag().getInt("XP");
        int maxXP = nms.getTag().getInt("maxXP");
        int tier = nms.getTag().getInt("itemTier");
        int professionBuffBonus = 0;
        if (DonationEffects.getInstance().getActiveProfessionBuff() != null) {
            professionBuffBonus = Math.round(exp * (DonationEffects.getInstance().getActiveProfessionBuff()
                    .getBonusAmount() / 100f));
            exp += professionBuffBonus;
        }
        currentXP += exp;

        if ((boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, p.getUniqueId())) {
            p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "          +" + ChatColor.YELLOW + exp + ChatColor.BOLD + " EXP"
                    + ChatColor.YELLOW + ChatColor.GRAY + " [" + Math.round(currentXP - professionBuffBonus) + ChatColor.BOLD + "/" + ChatColor.GRAY + getEXPNeeded(getLvl(stack)) + " EXP]");
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            if (professionBuffBonus > 0) {
                p.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "        " + ChatColor.GOLD
                        .toString() + ChatColor.BOLD + "PROF. BUFF >> " + ChatColor.YELLOW.toString() + ChatColor.BOLD
                        + "+" + ChatColor.YELLOW + Math.round(professionBuffBonus) + ChatColor.BOLD + " EXP " +
                        ChatColor.GRAY + "[" + currentXP + ChatColor.BOLD + "/" + ChatColor.GRAY + getEXPNeeded
                        (getLvl(stack)) + " EXP]");
            }
        }

        if (currentXP > maxXP) {
            lvlUp(tier, p);
            return;
        } else
            nms.getTag().setInt("XP", currentXP);
        stack = CraftItemStack.asBukkitCopy(nms);
        p.getEquipment().setItemInMainHand(stack);
        ItemMeta meta = stack.getItemMeta();
        List<String> lore = stack.getItemMeta().getLore();
        String expBar = "||||||||||||||||||||" + "||||||||||||||||||||" + "||||||||||";
        double percentDone = 100.0 * currentXP / maxXP;
        double percentDoneDisplay = (percentDone / 100) * 50.0D;
        int display = (int) percentDoneDisplay;
        if (display <= 0) {
            display = 1;
        }
        if (display > 50) {
            display = 50;
        }
        String newexpBar = ChatColor.GREEN.toString() + expBar.substring(0, display) + ChatColor.RED.toString()
                + expBar.substring(display, expBar.length());
        int lvl = CraftItemStack.asNMSCopy(stack).getTag().getInt("level");
        lore.set(0, ChatColor.GRAY.toString() + "Level: " + GameAPI.getTierColor(tier) + lvl);
        lore.set(1, ChatColor.GRAY.toString() + currentXP + ChatColor.GRAY + " / " + ChatColor.GRAY + maxXP);
        lore.set(2, ChatColor.GRAY + "EXP: " + newexpBar);

        meta.setLore(lore);
        if (!meta.hasEnchant(Enchantment.LURE))
            meta.addEnchant(Enchantment.LURE, 3, false);

        stack.setItemMeta(meta);
        p.getEquipment().setItemInMainHand(stack);
    }


    public static int getTreasureFindChance(ItemStack is) {
        int chance = 0;

        if (!(isDRFishingPole(is))) {
            return chance;
        }

        for (String s : is.getItemMeta().getLore()) {
            if (s.contains("TREASURE FIND")) {
                chance = Integer.parseInt(s.substring(s.lastIndexOf(" ") + 1, s.lastIndexOf("%")));
                return chance;
            }
        }

        return chance;
    }

    public static int getJunkFindChance(ItemStack is) {
        int chance = 0;

        if (!(isDRFishingPole(is))) {
            return chance;
        }

        for (String s : is.getItemMeta().getLore()) {
            if (s.contains("JUNK FIND")) {
                chance = Integer.parseInt(s.substring(s.lastIndexOf(" ") + 1, s.lastIndexOf("%")));
                return chance;
            }
        }

        return chance;
    }

    public static int getDoubleDropChance(ItemStack is) {
        int chance = 0;

        if (!(isDRFishingPole(is))) {
            return chance;
        }

        for (String s : is.getItemMeta().getLore()) {
            if (s.contains("DOUBLE")) {
                chance = Integer.parseInt(s.substring(s.lastIndexOf(" ") + 1, s.lastIndexOf("%")));
                return chance;
            }
        }

        return chance;
    }

    public static int getTripleDropChance(ItemStack is) {
        int chance = 0;

        if (!(isDRFishingPole(is))) {
            return chance;
        }

        for (String s : is.getItemMeta().getLore()) {
            if (s.contains("TRIPLE")) {
                chance = Integer.parseInt(s.substring(s.lastIndexOf(" ") + 1, s.lastIndexOf("%")));
                return chance;
            }
        }

        return chance;
    }

    public static int getSuccessChance(ItemStack is) {
        int chance = 0;

        if (!(isDRFishingPole(is))) {
            return chance;
        }

        for (String s : is.getItemMeta().getLore()) {
            if (s.contains("SUCCESS")) {
                chance = Integer.parseInt(s.substring(s.lastIndexOf("+") + 1, s.lastIndexOf("%")));
                return chance;
            }
        }

        return chance;
    }

    public static int getDurabilityBuff(ItemStack is) {
        int buff = 0;

        if (!(isDRFishingPole(is))) {
            return buff;
        }

        for (String s : is.getItemMeta().getLore()) {
            if (s.contains("DURABILITY")) {
                buff = Integer.parseInt(s.substring(s.lastIndexOf("+") + 1, s.lastIndexOf("%")));
                return buff;
            }
        }

        return buff;
    }

    /**
     * Get the enchant leve of a fishing rod
     *
     * @param itemStack  The fishing rod
     * @param rodEnchant The enchant to check for
     * @return The enchant level
     */
    public static int getEnchantBuff(ItemStack itemStack, FishingRodEnchant rodEnchant) {
        switch (rodEnchant) {
            case Durability:
                return getDurabilityBuff(itemStack);
            case CatchingSuccess:
                return getSuccessChance(itemStack);
            case TripleCatch:
                return getTripleDropChance(itemStack);
            case TreasureFind:
                return getTreasureFindChance(itemStack);
            case DoubleCatch:
                return getDoubleDropChance(itemStack);
            case JunkFind:
                return getJunkFindChance(itemStack);
            default:
                break;
        }
        return 0;
    }

    /**
     * Get all enchant data of a fishing rod
     *
     * @param itemStack The fishing rod
     * @return Enchant data
     */
    public static HashMap<FishingRodEnchant, Integer> getEnchantData(ItemStack itemStack) {
        if (isDRFishingPole(itemStack)) {
            if (hasEnchants(itemStack)) {
                HashMap<FishingRodEnchant, Integer> objectMap = Maps.newHashMap();
                for (FishingRodEnchant rodEnchant : FishingRodEnchant.values()) {
                    objectMap.put(rodEnchant, getEnchantBuff(itemStack, rodEnchant));
                }
            }
        }
        return null;
    }

    /**
     * Add an enchant to a fishing rod
     *
     * @param itemStack The itemstack
     * @param enchant   The enchant
     * @param buffLevel The enchant level
     */
    public static void addDefaultEnchant(ItemStack itemStack, FishingRodEnchant enchant, int buffLevel) {
        ItemMeta meta = itemStack.getItemMeta();
        List<String> lore = meta.getLore();
        Iterator<String> i = lore.iterator();
        int prevValue = -1;

        while (i.hasNext()) {
            String line = i.next();
            if (line.contains(enchant.name)) {
                prevValue = Integer.valueOf(line.substring(line.indexOf("+"), line.indexOf("%")));
                i.remove();
            }
        }

        String clone = lore.get(lore.size() - 1);
        int value = buffLevel;
        if (value == 0)
            value = 1;
        if (prevValue != -1 && prevValue > value)
            value = prevValue;
        lore.remove(lore.size() - 1);
        lore.add(ChatColor.RED + enchant.name + " +" + value + "%");
        lore.add(clone);
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }


    public static void giveRandomStatBuff(ItemStack stack, int tier) {
        int typeID = new Random().nextInt(6);
        ItemMeta meta = stack.getItemMeta();
        List<String> lore = meta.getLore();
        FishingRodEnchant enchant = null;
        main:
        switch (tier) {
            case 0:
            case 1:
            case 2:
            case 3:
                switch (typeID) {
                    case 0:
                    case 1:
                        enchant = FishingRodEnchant.DoubleCatch;
                        break main;
                    case 2:
                        enchant = FishingRodEnchant.CatchingSuccess;
                        break main;
                    case 3:
                        enchant = FishingRodEnchant.TripleCatch;
                        break main;
                    case 4:
                        enchant = FishingRodEnchant.Durability;
                        break main;
                    case 5:
                        enchant = FishingRodEnchant.JunkFind;
                        break main;
                }
            case 4:
            case 5:
                switch (typeID) {
                    case 0:
                        enchant = FishingRodEnchant.DoubleCatch;
                        break main;
                    case 1:
                        enchant = FishingRodEnchant.TreasureFind;
                        break main;
                    case 2:
                        enchant = FishingRodEnchant.CatchingSuccess;
                        break main;
                    case 3:
                        enchant = FishingRodEnchant.TripleCatch;
                        break main;
                    case 4:
                        enchant = FishingRodEnchant.Durability;
                        break main;
                    case 5:
                        enchant = FishingRodEnchant.JunkFind;
                        break main;
                }
        }

        Iterator<String> i = lore.iterator();
        int prevValue = -1;

        while (i.hasNext()) {
            String line = i.next();
            if (line.contains(enchant.name)) {
                prevValue = Integer.valueOf(line.substring(line.indexOf("+"), line.indexOf("%")));
                i.remove();
            }
        }


        String clone = lore.get(lore.size() - 1);
        int value = enchant.getBuff(tier);
        if (value == 0)
            value = 1;
        if (prevValue != -1 && prevValue > value)
            value = prevValue;
        lore.remove(lore.size() - 1);
        lore.add(ChatColor.RED + enchant.name + " +" + value + "%");
        lore.add(clone);
        meta.setLore(lore);
        stack.setItemMeta(meta);

    }

    public static void lvlUp(int tier, Player p) {
        ItemStack rod = p.getEquipment().getItemInMainHand();
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(rod);
        int lvl = nms.getTag().getInt("level") + 1;
        boolean addEnchant = false;
        if (lvl < 101) {
            switch (lvl) {
                case 20:
                    tier = 2;
                    addEnchant = true;
                    Achievements.getInstance().giveAchievement(p.getUniqueId(), Achievements.EnumAchievements.FISHINGROD_LEVEL_I);
                    break;
                case 40:
                    tier = 3;
                    addEnchant = true;
                    Achievements.getInstance().giveAchievement(p.getUniqueId(), Achievements.EnumAchievements.FISHINGROD_LEVEL_II);
                    break;
                case 60:
                    tier = 4;
                    addEnchant = true;
                    Achievements.getInstance().giveAchievement(p.getUniqueId(), Achievements.EnumAchievements.FISHINGROD_LEVEL_III);
                    break;
                case 80:
                    tier = 5;
                    addEnchant = true;
                    Achievements.getInstance().giveAchievement(p.getUniqueId(), Achievements.EnumAchievements.FISHINGROD_LEVEL_IV);
                    break;
                case 100:
                    addEnchant = true;
                    Achievements.getInstance().giveAchievement(p.getUniqueId(), Achievements.EnumAchievements.FISHINGROD_LEVEL_V);
                    p.sendMessage(ChatColor.YELLOW + "Congratulations! Your Fishing Rod has reached " + ChatColor.UNDERLINE + "LVL 100"
                            + ChatColor.YELLOW + " this means you can no longe repair it. You now have TWO options.");
                    p.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "(1) " + ChatColor.YELLOW + "You can exchange the Fishing Rod at the merchant for a 'Buff Token' that will hold all the custom stats of your Fishing Rod and may be applied to a new Fishing Rod.");
                    p.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "(2) " + ChatColor.YELLOW + "If you continue to use this" +
                            " Fishing Rod until it runs out of durability, it will transform into a LVL 1 Fishing Rod "
                            + ", but it will retain all its custom stats.");
                    p.sendMessage("");
                    break;
                default:
                    break;
            }
            nms = CraftItemStack.asNMSCopy(rod);
            p.sendMessage(ChatColor.YELLOW + "Your Fishing Rod has increased to level " + ChatColor.AQUA + lvl);
            nms.getTag().setInt("maxXP", getEXPNeeded(lvl));
            nms.getTag().setInt("XP", 0);
            nms.getTag().setInt("level", lvl);
            nms.getTag().setInt("itemTier", tier);

            rod = CraftItemStack.asBukkitCopy(nms);
            ItemMeta meta = rod.getItemMeta();
            List<String> lore = meta.getLore();
            String expBar = ChatColor.RED + "||||||||||||||||||||" + "||||||||||||||||||||" + "||||||||||";
            lore.set(0, ChatColor.GRAY.toString() + "Level: " + GameAPI.getTierColor(tier) + lvl);
            lore.set(1, ChatColor.GRAY.toString() + 0 + ChatColor.GRAY.toString() + " / " + ChatColor.GRAY + Mining.getEXPNeeded(lvl));
            lore.set(2, ChatColor.GRAY.toString() + "EXP: " + expBar);
            String name = "Basic Fishing Rod";

            switch (tier) {
                case 1:
                    name = ChatColor.WHITE + "Basic Fishing Rod";
                    lore.set(lore.size() - 1, ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of wood and thread.");
                    break;
                case 2:
                    name = ChatColor.GREEN.toString() + "Advanced Fishing Rod";
                    lore.set(lore.size() - 1, ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of oak wood and thread.");
                    break;
                case 3:
                    name = ChatColor.AQUA.toString() + "Expert Fishing Rod";
                    lore.set(lore.size() - 1, ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of ancient oak wood and spider silk.");
                    break;
                case 4:
                    name = ChatColor.LIGHT_PURPLE.toString() + "Supreme Fishing Rod";
                    lore.set(lore.size() - 1, ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of jungle bamboo and spider silk.");
                    break;
                case 5:
                    name = ChatColor.YELLOW.toString() + (lvl == 100 ? "Grand " : "") + "Master Fishing Rod";
                    lore.set(lore.size() - 1, ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of rich mahogany and enchanted silk.");
                    break;
                default:
                    break;
            }
            meta.setDisplayName(name);
            meta.setLore(lore);
            if (!meta.hasEnchant(Enchantment.LURE))
                meta.addEnchant(Enchantment.LURE, 3, false);
            rod.setItemMeta(meta);
            if (addEnchant)
                giveRandomStatBuff(rod, tier);

            p.getEquipment().setItemInMainHand(rod);
        }
    }


    /**
     * Get the tier of said Rod.
     *
     * @param rodStack
     * @return Integer
     * @since 1.0
     */
    public static int getRodTier(ItemStack rodStack) {
        return CraftItemStack.asNMSCopy(rodStack).getTag().getInt("itemTier");
    }


    public enum FishingRodEnchant {
        DoubleCatch("DOUBLE CATCH"),
        TripleCatch("TRIPLE CATCH"),
        TreasureFind("TREASURE FIND"),
        Durability("DURABILITY"),
        CatchingSuccess("FISHING SUCCESS"),
        JunkFind("JUNK FIND");


        public String name;

        FishingRodEnchant(String display) {
            this.name = display;
        }

        public int getBuff(int tier) {
            Random rand = new Random();
            switch (this) {
                case DoubleCatch:
                    switch (tier) {
                        case 0:
                        case 1:
                        case 2:
                            return rand.nextInt(5) + 1;
                        case 3:
                            return rand.nextInt(9) + 1;
                        case 4:
                            return rand.nextInt(13) + 1;
                        case 5:
                            return rand.nextInt(24) + 1;
                    }
                case TripleCatch:
                    switch (tier) {
                        case 0:
                        case 1:
                        case 2:
                            return rand.nextInt(2) + 1;
                        case 3:
                            return rand.nextInt(3) + 1;
                        case 4:
                            return rand.nextInt(4) + 1;
                        case 5:
                            return rand.nextInt(5) + 1;
                    }
                    break;
                case TreasureFind:
                    switch (tier) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            return 0;
                        case 4:
                        case 5:
                            return 1;
                    }
                    break;
                case Durability:
                    switch (tier) {
                        case 0:
                        case 1:
                            return rand.nextInt(5) + 1;
                        case 2:
                            return rand.nextInt(10) + 1;
                        case 3:
                            return rand.nextInt(15) + 1;
                        case 4:
                            return rand.nextInt(20) + 1;
                        case 5:
                            return rand.nextInt(25) + 1;
                    }
                    break;
                case CatchingSuccess:
                    switch (tier) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                            return rand.nextInt(2) + 1;
                        case 5:
                            return rand.nextInt(6) + 1;
                    }
                    break;
                case JunkFind:
                    switch (tier) {
                        case 0:
                        case 1:
                            return rand.nextInt(11) + 1;
                        case 2:
                            return rand.nextInt(12) + 1;
                        case 3:
                            return rand.nextInt(13) + 1;
                        case 4:
                            return rand.nextInt(14) + 1;
                        case 5:
                            return rand.nextInt(15) + 1;
                    }
                    break;
            }

            return 1;
        }

        public static FishingRodEnchant getEnchant(String enchantTypeString) {
            for (FishingRodEnchant temp : values()) {
                Bukkit.getLogger().info(temp.name + " || " + enchantTypeString);
                if (temp.name().equalsIgnoreCase(enchantTypeString) || temp.name.contains(enchantTypeString) || temp.name.equalsIgnoreCase(enchantTypeString))
                    return temp;
            }
            return FishingRodEnchant.DoubleCatch;
        }
    }

    public static ItemStack getEnchant(int tier, FishingRodEnchant enchant) {
        int stat = enchant.getBuff(tier);
        String statBuff = ChatColor.RED + enchant.name + " " + stat + "%";
        ItemStack stack = new ItemBuilder().setItem(Material.EMPTY_MAP, (short) 0, ChatColor.WHITE + ChatColor.BOLD.toString() + "Scroll: " + ChatColor.YELLOW + "Fishing Rod Enchant", new String[]{statBuff, ChatColor.GRAY + "Imbues a fishing rod with special attributes."}).build();

        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        nms.getTag().setString("type", "fishingenchant");
        nms.getTag().setInt(enchant.name(), stat);
        return AntiDuplication.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nms));
    }

    public static ItemStack getEnchant(int tier, FishingRodEnchant enchant, int percent) {
        String statBuff = ChatColor.RED + enchant.name + " " + percent + "%";
        ItemStack stack = new ItemBuilder().setItem(Material.EMPTY_MAP, (short) 0, ChatColor.WHITE + ChatColor.BOLD.toString() + "Scroll: " + ChatColor.YELLOW + "Fishing Rod Enchant", new String[]{statBuff, ChatColor.GRAY + "Imbues a fishing rod with special attributes."}).build();
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        nms.getTag().setString("type", "fishingenchant");
        nms.getTag().setInt(enchant.name(), percent);
        return AntiDuplication.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nms));
    }


    public HashMap<Location, Integer> FISHING_LOCATIONS = new HashMap<>();
    public HashMap<Location, List<Location>> FISHING_PARTICLES = new HashMap<>();

    public void generateFishingParticleBlockList() {

    }

    public Location getFishingSpot(Location loc) {
        for (Location fish_loc : FISHING_LOCATIONS.keySet()) {
            double dist_sqr = loc.distanceSquared(fish_loc);
            if (dist_sqr <= 100)
                return fish_loc;
        }
        return null;
    }

    public Integer getFishingSpotTier(Location loc) {
        for (Location fish_loc : FISHING_LOCATIONS.keySet()) {
            double dist_sqr = loc.distanceSquared(fish_loc);
            if (dist_sqr <= 100) {
                return FISHING_LOCATIONS.get(fish_loc);
            }
        }
        return -1;
    }

    public static boolean isCustomFish(ItemStack is) {
        if (is != null && is.getType() == Material.COOKED_FISH && is.hasItemMeta() && is.getItemMeta().hasDisplayName() && is.getItemMeta().hasLore()) {
            return true;
        }
        return false;
    }

    public static boolean isCustomRawFish(ItemStack is) {
        if (is != null && is.getType() == Material.RAW_FISH && is.hasItemMeta() && is.getItemMeta().hasDisplayName() && is.getItemMeta().hasLore()) {
            return true;
        }
        return false;
    }

    public static void restoreFood(Player p, ItemStack fish) {
        List<String> lore = fish.getItemMeta().getLore();
        int food_to_restore = 0;

        for (String s : lore) {
            if (s.contains("% HUNGER")) {
                double percent = Integer.parseInt(s.substring(s.indexOf("-") + 1, s.indexOf("%")));
                int local_amount = (int) ((percent / 100.0D) * 20D);
                food_to_restore += local_amount;
            }
        }

        int cur_food = p.getFoodLevel();
        if (cur_food + food_to_restore >= 20) {
            p.setFoodLevel(20);
            p.setSaturation(20);
        } else {
            p.setFoodLevel(cur_food + food_to_restore);
            p.setSaturation(p.getSaturation() + food_to_restore);
        }
    }

    public static void applyFishBuffs(Player p, ItemStack fish) {
        List<String> lore = fish.getItemMeta().getLore();

        for (String s : lore) {
            s = ChatColor.stripColor(s);
            if (s.contains("% HP (instant)")) {
                double percent_to_heal = Double.parseDouble(s.substring(s.indexOf("+") + 1, s.indexOf("%"))) / 100;
                double max_hp = HealthHandler.getInstance().getPlayerMaxHPLive(p);
                int amount_to_heal = (int) Math.round((percent_to_heal * max_hp));
                double current_hp = HealthHandler.getInstance().getPlayerHPLive(p);
                if (current_hp + 1 > max_hp) {
                    continue;
                }
                if ((boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, p.getUniqueId())) {
                    p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "+" + ChatColor.GREEN + amount_to_heal + ChatColor.BOLD + " HP"
                            + ChatColor.GREEN + " FROM " + fish.getItemMeta().getDisplayName() + ChatColor.GRAY + " ["
                            + ((int) current_hp + amount_to_heal) + "/" + (int) max_hp + "HP]");
                }

                if ((current_hp + amount_to_heal) >= max_hp) {
                    p.setHealth(20);
                    HealthHandler.getInstance().setPlayerHPLive(p, (int) max_hp);
                } else if (p.getHealth() <= 19 && ((current_hp + amount_to_heal) < max_hp)) {
                    HealthHandler.getInstance().setPlayerHPLive(p, HealthHandler.getInstance().getPlayerHPLive(p) + amount_to_heal);
                    double health_percent = (HealthHandler.getInstance().getPlayerHPLive(p) + amount_to_heal) / max_hp;
                    double new_health_display = health_percent * 20;
                    if (new_health_display > 19) {
                        if (health_percent >= 1) {
                            new_health_display = 20;
                        } else if (health_percent < 1) {
                            new_health_display = 19;
                        }
                    }
                    if (new_health_display < 1) {
                        new_health_display = 1;
                    }
                    p.setHealth((int) new_health_display);

                }
            } else if (s.startsWith("REGEN")) {
                if (p.hasMetadata("fishhpRegen")) {
                    p.sendMessage(ChatColor.GRAY + "You already have fish hp regen applied.");
                    return;
                }

                double percent_to_regen = Double.parseDouble(s.substring(s.indexOf(" ") + 1, s.indexOf("%"))) / 100.0D;
                int regen_interval = Integer.parseInt(s.substring(s.lastIndexOf(" ") + 1, s.lastIndexOf("s")));
                double max_hp = HealthHandler.getInstance().getPlayerMaxHPLive(p);

                final int amount_to_regen_per_interval = (int) (max_hp * percent_to_regen) / regen_interval;
                p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "      " + ChatColor.GREEN + amount_to_regen_per_interval + ChatColor.BOLD
                        + " HP/s" + ChatColor.GREEN + " FROM " + fish.getItemMeta().getDisplayName() + ChatColor.GRAY + " [" + regen_interval + "s]");
                GameAPI.getGamePlayer(p).changeAttributeValPercentage(Item.ArmorAttributeType.HEALTH_REGEN, (float) percent_to_regen);

                p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, (int) (regen_interval + (regen_interval * 0.25)), 0));
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    p.removeMetadata("fishhpRegen", DungeonRealms.getInstance());
                    p.removePotionEffect(PotionEffectType.REGENERATION);
                    GameAPI.getGamePlayer(p).changeAttributeValPercentage(Item.ArmorAttributeType.HEALTH_REGEN, (float) -percent_to_regen);
                    p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "   " + amount_to_regen_per_interval + " HP/s " + ChatColor.RED + "FROM "
                            + fish.getItemMeta().getDisplayName() + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
                }, regen_interval * 20L);
            } else if (s.startsWith("SPEED")) {

                String tier_symbol = s.substring(s.indexOf("(") + 1, s.indexOf(")"));
                int effect_tier = 0;
                if (tier_symbol.equalsIgnoreCase("II")) {
                    effect_tier = 1;
                }
                int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, effect_time * 20, effect_tier));
            } else if (s.startsWith("NIGHTVISION")) {

                String tier_symbol = s.substring(s.indexOf("(") + 1, s.indexOf(")"));
                int effect_tier = 0;
                if (tier_symbol.equalsIgnoreCase("II")) {
                    effect_tier = 1;
                }
                int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, effect_time * 20, effect_tier));
            } else if (s.contains("ENERGY REGEN")) {

                if (p.hasMetadata("fishEnRegen")) {
                    p.sendMessage(ChatColor.GRAY + "You already have fish energy regen applied.");
                    return;
                }

                final int bonus_percent = Integer.parseInt(s.substring(s.indexOf("+") + 1, s.indexOf("%")));
                int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                GameAPI.getGamePlayer(p).changeAttributeValPercentage(Item.ArmorAttributeType.ENERGY_REGEN, bonus_percent);
                p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "      " + ChatColor.GREEN + bonus_percent + ChatColor.BOLD + " Energy/s"
                        + ChatColor.GREEN + " FROM " + fish.getItemMeta().getDisplayName() + ChatColor.GRAY + " [" + effect_time + "s]");
                p.setMetadata("fishEnRegen", new FixedMetadataValue(DungeonRealms.getInstance(), true));

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    p.removeMetadata("fishEnRegen", DungeonRealms.getInstance());
                    GameAPI.getGamePlayer(p).changeAttributeValPercentage(Item.ArmorAttributeType.ENERGY_REGEN, -bonus_percent);
                    p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + " +" + bonus_percent + "% Energy " + ChatColor.RED + "FROM "
                            + fish.getItemMeta().getDisplayName() + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
                }, effect_time * 20L);
            } else if (s.contains("% DMG")) {
                if (p.hasMetadata("fishDMG")) {
                    p.sendMessage(ChatColor.GRAY + "You already have fish damage applied.");
                    return;
                }

                final int bonus_percent = Integer.parseInt(s.substring(s.indexOf("+") + 1, s.indexOf("%")));
                int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                GameAPI.getGamePlayer(p).changeAttributeValPercentage(Item.WeaponAttributeType.DAMAGE, bonus_percent);
                p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "+" + ChatColor.GREEN + bonus_percent + ChatColor.BOLD + "% DMG"
                        + ChatColor.GREEN + " FROM " + fish.getItemMeta().getDisplayName() + ChatColor.GRAY + " [" + effect_time + "s]");

                p.setMetadata("fishDMG", new FixedMetadataValue(DungeonRealms.getInstance(), true));

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    p.removeMetadata("fishDMG", DungeonRealms.getInstance());
                    GameAPI.getGamePlayer(p).changeAttributeValPercentage(Item.WeaponAttributeType.DAMAGE, -bonus_percent);
                    p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "+" + bonus_percent + "% DMG " + ChatColor.RED + "FROM "
                            + fish.getItemMeta().getDisplayName() + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
                }, effect_time * 20L);


            } else if (s.contains("% ARMOR")) {
                if (p.hasMetadata("fishArmor")) {
                    p.sendMessage(ChatColor.GRAY + "You already have fish armor applied.");
                    return;
                }

                p.setMetadata("fishArmor", new FixedMetadataValue(DungeonRealms.getInstance(), true));

                final int bonus_percent = Integer.parseInt(s.substring(s.indexOf("+") + 1, s.indexOf("%")));
                int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                GameAPI.getGamePlayer(p).changeAttributeValPercentage(Item.ArmorAttributeType.ARMOR, bonus_percent);
                p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "+" + ChatColor.GREEN + bonus_percent + ChatColor.BOLD + "% ARMOR"
                        + ChatColor.GREEN + " FROM " + fish.getItemMeta().getDisplayName() + ChatColor.GRAY + " [" + effect_time + "s]");


                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    p.removeMetadata("fishArmor", DungeonRealms.getInstance());
                    GameAPI.getGamePlayer(p).changeAttributeValPercentage(Item.ArmorAttributeType.ARMOR, -bonus_percent);
                    p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "+" + bonus_percent + "% ARMOR " + ChatColor.RED + "FROM "
                            + fish.getItemMeta().getDisplayName() + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
                }, effect_time * 20L);
            } else if (s.contains("% BLOCK")) {
                if (p.hasMetadata("fishBlock")) {
                    p.sendMessage(ChatColor.GRAY + "You already have fish block applied.");
                    return;
                }

                p.setMetadata("fishBlock", new FixedMetadataValue(DungeonRealms.getInstance(), true));

                final int bonus_percent = Integer.parseInt(s.substring(s.indexOf("+") + 1, s.indexOf("%")));
                int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                GameAPI.getGamePlayer(p).changeAttributeValPercentage(Item.ArmorAttributeType.BLOCK, bonus_percent);
                p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "+" + ChatColor.GREEN + bonus_percent + ChatColor.BOLD + "% BLOCK"
                        + ChatColor.GREEN + " FROM " + fish.getItemMeta().getDisplayName() + ChatColor.GRAY + " [" + effect_time + "s]");

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    p.removeMetadata("fishBlock", DungeonRealms.getInstance());
                    GameAPI.getGamePlayer(p).changeAttributeValPercentage(Item.ArmorAttributeType.BLOCK, -bonus_percent);
                    p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "+" + bonus_percent + "% BLOCK " + ChatColor.RED + "FROM "
                            + fish.getItemMeta().getDisplayName() + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
                }, effect_time * 20L);
            } else if (s.contains("% LIFESTEAL")) {
                if (p.hasMetadata("fishLifesteal")) {
                    p.sendMessage(ChatColor.GRAY + "You already have fish life steal applied.");
                    return;
                }

                p.setMetadata("fishLifesteal", new FixedMetadataValue(DungeonRealms.getInstance(), true));
                final int bonus_percent = Integer.parseInt(s.substring(s.indexOf("+") + 1, s.indexOf("%")));
                int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                GameAPI.getGamePlayer(p).changeAttributeValPercentage(Item.WeaponAttributeType.LIFE_STEAL, bonus_percent);
                p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "+" + ChatColor.GREEN + bonus_percent + ChatColor.BOLD + "% LIFESTEAL"
                        + ChatColor.GREEN + " FROM " + fish.getItemMeta().getDisplayName() + ChatColor.GRAY + " [" + effect_time + "s]");

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    p.removeMetadata("fishLifesteal", DungeonRealms.getInstance());
                    GameAPI.getGamePlayer(p).changeAttributeValPercentage(Item.WeaponAttributeType.LIFE_STEAL, -bonus_percent);
                    p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "+" + bonus_percent + "% LIFESTEAL " + ChatColor.RED + "FROM "
                            + fish.getItemMeta().getDisplayName() + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
                }, effect_time * 20L);
            } else if (s.contains("% CRIT")) {
                if (p.hasMetadata("fishCrit")) {
                    p.sendMessage(ChatColor.GRAY + "You already have fish life steal applied.");
                    return;
                }

                p.setMetadata("fishCrit", new FixedMetadataValue(DungeonRealms.getInstance(), true));

                final int bonus_percent = Integer.parseInt(s.substring(s.indexOf("+") + 1, s.indexOf("%")));
                int effect_time = Integer.parseInt(s.substring(s.lastIndexOf("(") + 1, s.lastIndexOf("s")));
                GameAPI.getGamePlayer(p).changeAttributeValPercentage(Item.WeaponAttributeType.CRITICAL_HIT, bonus_percent);
                p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "+" + ChatColor.GREEN + bonus_percent + ChatColor.BOLD + "% CRIT"
                        + ChatColor.GREEN + " FROM " + fish.getItemMeta().getDisplayName() + ChatColor.GRAY + " [" + effect_time + "s]");

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    p.removeMetadata("fishCrit", DungeonRealms.getInstance());

                    GameAPI.getGamePlayer(p).changeAttributeValPercentage(Item.WeaponAttributeType.CRITICAL_HIT, -bonus_percent);
                    p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "+" + bonus_percent + "% CRIT " + ChatColor.RED + "FROM "
                            + fish.getItemMeta().getDisplayName() + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
                }, effect_time * 20L);
            }
        }
    }

    public void loadFishingLocations() {
        int count = 0;
        ArrayList<String> CONFIG = (ArrayList<String>) DungeonRealms.getInstance().getConfig()
                .getStringList("fishingspawns");
        for (String line : CONFIG) {
            if (line.contains("=")) {
                String[] cords = line.split("=")[0].split(",");
                Location loc = new Location(Bukkit.getWorlds().get(0), Double.parseDouble(cords[0]),
                        Double.parseDouble(cords[1]), Double.parseDouble(cords[2]));

                int tier = Integer.parseInt(line.split("=")[1]);
                FISHING_LOCATIONS.put(loc, tier);
                count++;
            }
        }
        Utils.log.info("[Professions] " + count + " FISHING SPOT locations have been LOADED.");
    }

    private static Fishing instance;

    public static Fishing getInstance() {
        if (instance == null)
            instance = new Fishing();
        return instance;

    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    public static int splashCounter = 10;

    @Override
    public void startInitialization() {
        loadFishingLocations();
        generateFishingParticleBlockList();
        DungeonRealms.getInstance().getServer().getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), () -> {
            int chance = splashCounter * splashCounter;
            if (splashCounter == 1) {
                splashCounter = 21;
            }
            splashCounter--;
            if (FISHING_PARTICLES.size() <= 0) {
                return;
            }
            try {
                for (Entry<Location, List<Location>> data : FISHING_PARTICLES.entrySet()) {
                    Location epicenter = data.getKey();
                    try {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SPLASH,
                                epicenter, random.nextFloat(), random.nextFloat(), random.nextFloat(), 0.4F, 20), 0L);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    data.getValue().stream().filter(loc -> random.nextInt(chance) == 1).forEach(loc -> {
                        try {
                            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SPLASH,
                                    epicenter, random.nextFloat(), random.nextFloat(), random.nextFloat(), 0.4F, 20), 0L);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    });
                }
            } catch (ConcurrentModificationException cme) {
                Utils.log.info("[Professions] [ASYNC] Something went wrong checking a fishing spot and adding particles!");
            }
        }, 200L, 15L);
    }

    @Override
    public void stopInvocation() {

    }

}
