package net.dungeonrealms.game.profession;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.mechanics.ParticleAPI.ParticleEffect;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.miscellaneous.RandomHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

    public enum EnumFish {
        Shrimp("A raw and pink crustacean", 1), Anchovie("A small blue, oily fish", 1), Crayfish("A lobster-like and brown crustacean", 1),
        Carp("A Large, silver-scaled fish", 2), Herring("A colourful and medium-sized fish", 2), Sardine("A small and oily green fish", 2),
        Salmon("A beautiful jumping fish", 3), Trout("A non-migrating Salmon", 3), Cod("A cold-water, deep sea fish", 3),
        Lobster("A Large, red crustacean", 4), Tuna("A large, sapphire blue fish", 4), Bass("A very large and white fish", 4),
        Shark("A terrifying and massive predator", 5), Swordfish("An elongated fish with a long bill", 5), Monkfish("A flat, large, and scary-looking fish", 5);


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
        int fish_type = new Random().nextInt(3); // 0, 1, 2
        String fish_name = "";
        int hunger_to_heal = 0;

        int buff_chance = 0;
        int do_i_buff = new Random().nextInt(100);

        boolean fish_buff = false;
        String fish_buff_s = "";

        if (tier == 1) {
            buff_chance = 20;
            hunger_to_heal = 10;// %

            if (fish_type == 0) {
                fish_name = ChatColor.WHITE.toString() + "Shrimp";
            }
            if (fish_type == 1) {
                fish_name = ChatColor.WHITE.toString() + "Anchovies";
            }
            if (fish_type == 2) {
                fish_name = ChatColor.WHITE.toString() + "Crayfish";
            }

            if (buff_chance >= do_i_buff) {
                fish_buff = true;
                int buff_type = new Random().nextInt(100);
                int buff_val = 0;
                if (buff_type >= 0 && buff_type <= 15) {
                    // Of Power (DMG) 1-2%
                    buff_val = new Random().nextInt(2) + 1;
                    fish_name += " of Lesser Power";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% DMG " + ChatColor.GRAY.toString() + "(20s)";
                }
                if (buff_type > 15 && buff_type <= 25) {
                    // Of Health 1-3% HP (instant heal)
                    buff_val = new Random().nextInt(3) + 1;
                    fish_name = ChatColor.WHITE.toString() + "Small, Healing " + fish_name;
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% HP " + ChatColor.GRAY.toString() + "(instant)";
                }
                if (buff_type > 25 && buff_type <= 50) {
                    // Of Speed 15 seconds of speed I.
                    fish_name += " of Lesser Agility";
                    fish_buff_s = ChatColor.RED.toString() + "SPEED (I) BUFF " + ChatColor.GRAY.toString() + "(15s)";
                }
                if (buff_type > 50 && buff_type <= 60) {
                    // Of Satiety, fill up 20% of food (2 full squares)
                    buff_val = 20;
                    fish_name += " of Minor Satiety";
                    fish_buff_s = ChatColor.RED.toString() + "-" + buff_val + "% HUNGER " + ChatColor.GRAY.toString() + "(instant)";
                }
                if (buff_type > 60 && buff_type <= 70) {
                    // Of Defence (ARMOR%) 1-2% ARMOR
                    buff_val = new Random().nextInt(2) + 1;
                    fish_name += " of Weak Defense";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% ARMOR " + ChatColor.GRAY.toString() + "(20s)";
                }
                if (buff_type > 70) {
                    // Nightvision for 60 seconds.
                    buff_val = new Random().nextInt(2) + 1;
                    fish_name += " of Vision";
                    fish_buff_s = ChatColor.RED.toString() + "NIGHTVISION (I) BUFF " + ChatColor.GRAY.toString() + "(30s)";
                }
            }
        }

        if (tier == 2) {
            buff_chance = 25;
            hunger_to_heal = 20;// %

            if (fish_type == 0) {
                fish_name = ChatColor.GREEN.toString() + "Heron";
            }
            if (fish_type == 1) {
                fish_name = ChatColor.GREEN.toString() + "Herring";
            }
            if (fish_type == 2) {
                fish_name = ChatColor.GREEN.toString() + "Sardine";
            }

            if (buff_chance >= do_i_buff) {
                fish_buff = true;
                int buff_type = new Random().nextInt(100);
                int buff_val = 0;
                if (buff_type >= 0 && buff_type <= 10) {
                    // Of Power (DMG) 1-2%
                    buff_val = new Random().nextInt(3) + 1;
                    fish_name += " of Power";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% DMG " + ChatColor.GRAY.toString() + "(25s)";
                }
                if (buff_type > 10 && buff_type <= 15) {
                    // Of HP REGEN
                    buff_val = new Random().nextInt(5) + 5;
                    fish_name += " of Regeneration";
                    fish_buff_s = ChatColor.RED.toString() + "REGEN " + buff_val + "% HP " + ChatColor.GRAY.toString() + "(over 10s)";
                }
                if (buff_type > 15 && buff_type <= 20) {
                    // OF BLOCK%
                    buff_val = new Random().nextInt(5) + 1;
                    fish_name += " of Blocking";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% BLOCK " + ChatColor.GRAY.toString() + "(25s)";
                }
                if (buff_type > 20 && buff_type <= 30) {
                    // Of Health (instant heal)
                    buff_val = new Random().nextInt(5) + 1;
                    fish_name = ChatColor.GREEN.toString() + "Healing " + fish_name;
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% HP " + ChatColor.GRAY.toString() + "(instant)";
                }
                if (buff_type > 30 && buff_type <= 55) {
                    fish_name += " of Agility";
                    fish_buff_s = ChatColor.RED.toString() + "SPEED (I) BUFF " + ChatColor.GRAY.toString() + "(20s)";
                }
                if (buff_type > 55 && buff_type <= 65) {
                    // Of Satiety, fill up 20% of food (2 full squares)
                    buff_val = 25;
                    fish_name += " of Satiety";
                    fish_buff_s = ChatColor.RED.toString() + "-" + buff_val + "% HUNGER " + ChatColor.GRAY.toString() + "(instant)";
                }
                if (buff_type > 65 && buff_type <= 75) {
                    // Of Defence (ARMOR%) 1-2% ARMOR
                    buff_val = new Random().nextInt(3) + 1;
                    fish_name += " of Defense";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% ARMOR " + ChatColor.GRAY.toString() + "(25s)";
                }
                if (buff_type > 75) {
                    // Nightvision for 60 seconds.
                    buff_val = new Random().nextInt(2) + 1;
                    fish_name += " of Vision";
                    fish_buff_s = ChatColor.RED.toString() + "NIGHTVISION (I) BUFF " + ChatColor.GRAY.toString() + "(45s)";
                }
            }
        }

        if (tier == 3) {
            buff_chance = 33;
            hunger_to_heal = 30;// %

            if (fish_type == 0) {
                fish_name = ChatColor.AQUA.toString() + "Salmon";
            }
            if (fish_type == 1) {
                fish_name = ChatColor.AQUA.toString() + "Trout";
            }
            if (fish_type == 2) {
                fish_name = ChatColor.AQUA.toString() + "Cod";
            }

            if (buff_chance >= do_i_buff) {
                fish_buff = true;
                int buff_type = new Random().nextInt(100);
                int buff_val = 0;
                if (buff_type >= 0 && buff_type <= 10) {
                    // Of Power (DMG) 1-2%
                    buff_val = new Random().nextInt(3) + 3;
                    fish_name += " of Greater Power";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% DMG " + ChatColor.GRAY.toString() + "(30s)";
                }
                if (buff_type > 10 && buff_type <= 15) {
                    // Of HP REGEN
                    buff_val = new Random().nextInt(11) + 5;
                    fish_name += " of Mighty Regeneration";
                    fish_buff_s = ChatColor.RED.toString() + "REGEN " + buff_val + "% HP " + ChatColor.GRAY.toString() + "(over 10s)";
                }
                if (buff_type > 15 && buff_type <= 20) {
                    // OF BLOCK%
                    buff_val = new Random().nextInt(5) + 1;
                    fish_name += " of Blocking";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% BLOCK " + ChatColor.GRAY.toString() + "(30s)";
                }
                if (buff_type > 20 && buff_type <= 30) {
                    // Of Health (instant heal)
                    buff_val = new Random().nextInt(4) + 4;
                    fish_name = ChatColor.AQUA.toString() + "Large, Healing " + fish_name;
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% HP " + ChatColor.GRAY.toString() + "(instant)";
                }
                if (buff_type > 30 && buff_type <= 55) {
                    fish_name += " of Lasting Agility";
                    fish_buff_s = ChatColor.RED.toString() + "SPEED (I) BUFF " + ChatColor.GRAY.toString() + "(30s)";
                }
                if (buff_type > 55 && buff_type <= 65) {
                    // Of Satiety, fill up 20% of food (2 full squares)
                    buff_val = 30;
                    fish_name += " of Great Satiety";
                    fish_buff_s = ChatColor.RED.toString() + "-" + buff_val + "% HUNGER " + ChatColor.GRAY.toString() + "(instant)";
                }
                if (buff_type > 65 && buff_type <= 75) {
                    // Of Defence (ARMOR%) 1-2% ARMOR
                    buff_val = new Random().nextInt(3) + 3;
                    fish_name += " of Mighty Defense";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% ARMOR " + ChatColor.GRAY.toString() + "(30s)";
                }
                if (buff_type > 75) {
                    // Nightvision for 60 seconds.
                    buff_val = new Random().nextInt(2) + 1;
                    fish_name += " of Lasting Vision";
                    fish_buff_s = ChatColor.RED.toString() + "NIGHTVISION (I) BUFF " + ChatColor.GRAY.toString() + "(60s)";
                }
            }
        }

        if (tier == 4) {
            buff_chance = 33;
            hunger_to_heal = 40;// %

            if (fish_type == 0) {
                fish_name = ChatColor.LIGHT_PURPLE.toString() + "Lobster";
            }
            if (fish_type == 1) {
                fish_name = ChatColor.LIGHT_PURPLE.toString() + "Tuna";
            }
            if (fish_type == 2) {
                fish_name = ChatColor.LIGHT_PURPLE.toString() + "Bass";
            }

            int buff_time = new Random().nextInt(10) + 40; // Up to 49s.

            if (buff_chance >= do_i_buff) {
                fish_buff = true;
                int buff_type = new Random().nextInt(100);
                int buff_val = 0;
                if (buff_type >= 0 && buff_type <= 10) {
                    // Of Power (DMG) 1-2%
                    buff_val = new Random().nextInt(6) + 5;
                    fish_name += " of Ancient Power";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% DMG " + ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                }
                if (buff_type > 10 && buff_type <= 15) {
                    // Of HP REGEN
                    buff_val = new Random().nextInt(6) + 10;
                    fish_name += " of Enhanced Regeneration";
                    fish_buff_s = ChatColor.RED.toString() + "REGEN " + buff_val + "% HP " + ChatColor.GRAY.toString() + "(over 10s)";
                }
                if (buff_type > 15 && buff_type <= 20) {
                    // OF BLOCK%
                    buff_val = new Random().nextInt(5) + 4;
                    fish_name += " of Greater Blocking";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% BLOCK " + ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                }
                if (buff_type > 20 && buff_type <= 30) {
                    // Of Health (instant heal)
                    buff_val = new Random().nextInt(4) + 4;
                    fish_name = ChatColor.LIGHT_PURPLE.toString() + "Healthy " + fish_name;
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% HP " + ChatColor.GRAY.toString() + "(instant)";
                }
                if (buff_type > 30 && buff_type <= 55) {
                    fish_name += " of Bursting Agility";
                    fish_buff_s = ChatColor.RED.toString() + "SPEED (II) BUFF " + ChatColor.GRAY.toString() + "(15s)";
                }
                if (buff_type > 55 && buff_type <= 65) {
                    // Of Satiety, fill up 20% of food (2 full squares)
                    buff_val = 30;
                    fish_name = ChatColor.LIGHT_PURPLE.toString() + "Huge " + fish_name;
                    fish_buff_s = ChatColor.RED.toString() + "-" + buff_val + "% HUNGER " + ChatColor.GRAY.toString() + "(instant)";
                }
                if (buff_type > 65 && buff_type <= 75) {
                    // Of Defence (ARMOR%) 1-2% ARMOR
                    buff_val = new Random().nextInt(5) + 4;
                    fish_name += " of Fortified Defense";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% ARMOR " + ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                }
                if (buff_type >= 75 && buff_type < 80) {
                    // Vampirism
                    buff_val = new Random().nextInt(2) + 4;
                    fish_name = "Albino " + ChatColor.LIGHT_PURPLE.toString() + fish_name;
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% LIFESTEAL " + ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                }
                if (buff_type > 80) {
                    // Nightvision for 60 seconds.
                    fish_name += " of Eagle Vision";
                    fish_buff_s = ChatColor.RED.toString() + "NIGHTVISION (II) BUFF " + ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                }
            }
        }

        if (tier == 5) {
            buff_chance = 45;
            hunger_to_heal = 50;// %

            if (fish_type == 0) {
                fish_name = ChatColor.YELLOW.toString() + "Shark";
            }
            if (fish_type == 1) {
                fish_name = ChatColor.YELLOW.toString() + "Swordfish";
            }
            if (fish_type == 2) {
                fish_name = ChatColor.YELLOW.toString() + "Monkfish";
            }

            int buff_time = new Random().nextInt(11) + 50; // Up to 60s.

            if (buff_chance >= do_i_buff) {
                fish_buff = true;
                int buff_type = new Random().nextInt(100);
                int buff_val = 0;
                if (buff_type >= 0 && buff_type <= 10) {
                    // Of Power (DMG) 1-2%
                    buff_val = new Random().nextInt(11) + 5;
                    fish_name += " of Legendary Power";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% DMG " + ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                }
                if (buff_type > 10 && buff_type <= 15) {
                    // Of HP REGEN
                    buff_val = new Random().nextInt(6) + 10;
                    fish_name += " of Extreme Regeneration";
                    fish_buff_s = ChatColor.RED.toString() + "REGEN " + buff_val + "% HP " + ChatColor.GRAY.toString() + "(over 10s)";
                }
                if (buff_type > 15 && buff_type <= 20) {
                    // OF BLOCK%
                    buff_val = new Random().nextInt(5) + 4;
                    fish_name += " of Greater Blocking";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% BLOCK " + ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                }
                if (buff_type > 20 && buff_type <= 30) {
                    // Of Health (instant heal)
                    buff_val = new Random().nextInt(6) + 5;
                    fish_name = ChatColor.YELLOW.toString() + "Legendary " + fish_name + " of Medicine";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% HP " + ChatColor.GRAY.toString() + "(instant)";
                }
                if (buff_type > 30 && buff_type <= 45) {
                    fish_name += " of Godlike Speed";
                    fish_buff_s = ChatColor.RED.toString() + "SPEED (II) BUFF " + ChatColor.GRAY.toString() + "(30s)";
                }
                if (buff_type > 45 && buff_type <= 50) {
                    // Of Satiety, fill up 20% of food (2 full squares)
                    buff_val = 40;
                    fish_name = ChatColor.YELLOW.toString() + "Gigantic " + fish_name;
                    fish_buff_s = ChatColor.RED.toString() + "-" + buff_val + "% HUNGER " + ChatColor.GRAY.toString() + "(instant)";
                }
                if (buff_type > 50 && buff_type <= 60) {
                    // Of Defence (ARMOR%) 1-2% ARMOR
                    buff_val = new Random().nextInt(6) + 5;
                    fish_name = ChatColor.YELLOW.toString() + "Hardended " + fish_name + " of Legendary Defense";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% ARMOR " + ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                }
                if (buff_type > 60 && buff_type <= 65) {
                    // Vampirism
                    buff_val = new Random().nextInt(5) + 3;
                    fish_name = "Albino " + ChatColor.YELLOW.toString() + fish_name;
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% LIFESTEAL " + ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                }
                if (buff_type > 65 && buff_type <= 85) {
                    // Nightvision for 60 seconds.
                    fish_name += " of Omniscient Vision";
                    fish_buff_s = ChatColor.RED.toString() + "NIGHTVISION (II) BUFF " + ChatColor.GRAY.toString() + "(" + (buff_time + 60) + "s)";
                }
                if (buff_type > 85 && buff_type <= 90) {
                    // Critical hit bonus
                    buff_val = new Random().nextInt(5) + 1;
                    fish_name = "Perfect " + ChatColor.YELLOW.toString() + fish_name + " of Accuracy";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% CRITICAL HIT " + ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                }
                if (buff_type > 90) {
                    // Energy Regen Buff
                    buff_val = new Random().nextInt(5) + 1;
                    fish_name = ChatColor.YELLOW.toString() + fish_name + " of Hidden Energy";
                    fish_buff_s = ChatColor.RED.toString() + "+" + buff_val + "% ENERGY REGEN " + ChatColor.GRAY.toString() + "(" + buff_time + "s)";
                }
            }
        }

        List<String> fish_lore = new ArrayList<String>();
        if (fish_buff == true) {
            fish_lore.add(fish_buff_s);
        }
        fish_lore.add(ChatColor.RED + "-" + hunger_to_heal + "% HUNGER " + ChatColor.GRAY.toString() + "(instant)");
        fish_lore.add(EnumFish.getFishDesc(fish_name));

        // Helps prevent stackability.

        if (fish_name.contains(ChatColor.WHITE.toString())) {
            fish_name = ChatColor.WHITE.toString() + "Raw " + fish_name;
        }
        if (fish_name.contains(ChatColor.GREEN.toString())) {
            fish_name = ChatColor.GREEN.toString() + "Raw " + fish_name;
        }
        if (fish_name.contains(ChatColor.AQUA.toString())) {
            fish_name = ChatColor.AQUA.toString() + "Raw " + fish_name;
        }
        if (fish_name.contains(ChatColor.LIGHT_PURPLE.toString())) {
            fish_name = ChatColor.LIGHT_PURPLE.toString() + "Raw " + fish_name;
        }
        if (fish_name.contains(ChatColor.YELLOW.toString())) {
            fish_name = ChatColor.YELLOW.toString() + "Raw " + fish_name;
        }

        ItemStack fish = new ItemStack(Material.RAW_FISH, 1);
        ItemMeta im = fish.getItemMeta();
        im.setDisplayName(fish_name);
        im.setLore(fish_lore);
        fish.setItemMeta(im);
        return fish;
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
            return (int) (2.5D * (250 + random.nextInt((int) (250 * 0.3D))));
        }
        if (tier == 2) {
            return (int) (2.5D * (430 + random.nextInt((int) (430 * 0.3D))));
        }
        if (tier == 3) {
            return (int) (2.5D * (820 + random.nextInt((int) (820 * 0.3D))));
        }
        if (tier == 4) {
            return (int) (2.5D * (1050 + random.nextInt((int) (1050 * 0.3D))));
        }
        if (tier == 5) {
            return (int) (2.5D * (1230 + random.nextInt((int) (1230 * 0.3D))));
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
        return nms.hasTag() && nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("rod");
    }

//    public static HashMap<UUID, String> fishBuffs = new HashMap<>();

    /**
     * Add Experience to the specified stack(fishing pole)
     *
     * @param stack
     */
    public static void gainExp(ItemStack stack, Player p, int exp) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        int tier = Fishing.getRodTier(stack);
        int xp = nms.getTag().getInt("XP");
        int maxXP = nms.getTag().getInt("maxXP");
        xp += exp;
        nms.getTag().setInt("XP", xp);
        ItemMeta meta = stack.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        String expBar = "||||||||||||||||||||" + "||||||||||||||||||||" + "||||||||||";
        double percentDone = 100.0 * xp / maxXP;
        double percentDoneDisplay = (percentDone / 100) * 50.0D;
        int display = (int) percentDoneDisplay;
        if (display <= 0) {
            display = 1;
        }
        if (display > 50) {
            display = 50;
        }
        expBar = ChatColor.GREEN.toString() + expBar.substring(0, display) + ChatColor.RED.toString()
                + expBar.substring(display, expBar.length());
        lore.add(ChatColor.GRAY.toString() + "Tier: " + ChatColor.WHITE.toString() + tier);
        lore.add(ChatColor.WHITE.toString() + xp + ChatColor.GRAY + "/" + ChatColor.GRAY + maxXP);
        lore.add(ChatColor.GRAY.toString() + "EXP: " + expBar);
        meta.setLore(lore);
        stack.setItemMeta(meta);
        p.setItemInHand(stack);
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

    private HashMap<Location, Integer> FISHING_LOCATIONS = new HashMap<>();
    public HashMap<Location, List<Location>> FISHING_PARTICLES = new HashMap<>();

    public void generateFishingParticleBlockList() {
        int count = 0;

        for (Entry<Location, Integer> data : FISHING_LOCATIONS.entrySet()) {
            Location epicenter = data.getKey();
            List<Location> lfishingParticles = new ArrayList<>();
            int radius = 10;
            for (int x = -(radius); x <= radius; x++) {
                for (int y = -(radius); y <= radius; y++) {
                    for (int z = -(radius); z <= radius; z++) {
                        Location loc = epicenter.getBlock().getRelative(x, y, z).getLocation();
                        if (loc.getBlock().getType() == Material.WATER
                                || loc.getBlock().getType() == Material.STATIONARY_WATER) {
                            if (loc.add(0, 1, 0).getBlock().getType() == Material.AIR) {
                                if (!(lfishingParticles.contains(loc))) {
                                    lfishingParticles.add(loc);
                                    count++;
                                }
                            }
                        }
                    }
                }
            }

            FISHING_PARTICLES.put(epicenter, lfishingParticles);
        }

        Utils.log.info("[Professions] Loaded a total of " + count + " possible FISHING PARTICLE locations.");
    }

    public Location getFishingSpot(Location loc) {
        Location closest_loc = null;
        for (Location fish_loc : FISHING_LOCATIONS.keySet()) {
            double dist_sqr = loc.distanceSquared(fish_loc);
            if (dist_sqr <= 100) {
                closest_loc = fish_loc;
            }
        }

        if (closest_loc == null) {
            return null;
        }

        return closest_loc;
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
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> ParticleAPI.sendParticleToLocation(ParticleEffect.SPLASH,
                                epicenter, random.nextFloat(), random.nextFloat(), random.nextFloat(), 0.4F, 20), 0L);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    data.getValue().stream().filter(loc -> random.nextInt(chance) == 1).forEach(loc -> {
                        try {
                            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> ParticleAPI.sendParticleToLocation(ParticleEffect.SPLASH,
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
