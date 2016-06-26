package net.dungeonrealms.game.profession;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by Chase on Oct 27, 2015
 */
public class Mining implements GenericMechanic {


    public static int getOreEXP(ItemStack stackInHand, Material m) {
        //TODO incorporate Modifiers? Enchants? etc.

        if (m == Material.COAL_ORE) {
            return 90 + new Random().nextInt(35);
        }
        if (m == Material.EMERALD_ORE) {
            return 275 + new Random().nextInt(35);
        }
        if (m == Material.IRON_ORE) {
            return 460 + new Random().nextInt(80);
        }
        if (m == Material.DIAMOND_ORE) {
            return 820 + new Random().nextInt(40);
        }
        if (m == Material.GOLD_ORE) {
            return 1025 + new Random().nextInt(55);
        }
        return 1;
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


    /**
     * Checks if itemstack is our pickaxe
     *
     * @param stack
     * @return boolean
     * @since 1.0
     */
    public static boolean isDRPickaxe(ItemStack stack) {
        if (stack.getType() == Material.WOOD_PICKAXE || stack.getType() == Material.STONE_PICKAXE || stack.getType() == Material.IRON_PICKAXE || stack.getType() == Material.GOLD_PICKAXE || stack.getType() == Material.DIAMOND_PICKAXE) {
            net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
            return !(nms == null || nms.getTag() == null) && nms.getTag().hasKey("type")
                    && nms.getTag().getString("type").equalsIgnoreCase("pick");
        }
        return false;
    }

    /**
     * Returns tier of our pick itemstack.
     *
     * @param stack
     * @return Integer
     */
    public static int getPickTier(ItemStack stack) {
        switch (stack.getType()) {
            case WOOD_PICKAXE:
                return 1;
            case STONE_PICKAXE:
                return 2;
            case IRON_PICKAXE:
                return 3;
            case DIAMOND_PICKAXE:
                return 4;
            case GOLD_PICKAXE:
                return 5;
        }
        return 1;
    }

    /**
     * Check amount of exp pick gets from block mined
     *
     * @param stackInHand
     * @param type
     * @return integer
     * @since 1.0
     */
//		gain = (gain * multiplier);
//		int guildExp = (int) (gain * (1.0f / 100.0f));

    /**
     * Returns the respawn time of ore in seconds
     *
     * @param oreType
     * @return integer
     * @since 1.0
     */
    public static int getOreRespawnTime(Material oreType) {
        switch (oreType) {
            case COAL_ORE:
                return 120;
            case EMERALD_ORE:
                return 300;
            case IRON_ORE:
                return 600;
            case DIAMOND_ORE:
                return 1200;
            case GOLD_ORE:
                return 2400;
        }
        return 0;
    }

    /**
     * Adds experienceGain to players pick
     *
     * @param stackInHand
     * @param experienceGain
     * @param p
     * @since 1.0
     */
    public static void addExperience(ItemStack stackInHand, int experienceGain, Player p) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stackInHand);
        int currentXP = nms.getTag().getInt("XP");
        int maxXP = nms.getTag().getInt("maxXP");
        int tier = nms.getTag().getInt("itemTier");
        currentXP += experienceGain;
        if (currentXP > maxXP) {
            lvlUp(tier, p);
            return;
        } else
            nms.getTag().setInt("XP", currentXP);
        stackInHand = CraftItemStack.asBukkitCopy(nms);
        p.getEquipment().setItemInMainHand(stackInHand);
        ItemMeta meta = stackInHand.getItemMeta();
        List<String> lore = stackInHand.getItemMeta().getLore();
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
        int lvl = CraftItemStack.asNMSCopy(stackInHand).getTag().getInt("level");
        lore.set(0, ChatColor.GRAY.toString() + "Level: " + API.getTierColor(tier) + lvl);
        lore.set(1, ChatColor.GRAY.toString() + currentXP + ChatColor.GRAY + " / " + ChatColor.GRAY + maxXP);
        lore.set(2, ChatColor.GRAY + "EXP: " + newexpBar);

        meta.setLore(lore);
        stackInHand.setItemMeta(meta);
        p.getEquipment().setItemInMainHand(stackInHand);
    }


    public static int getBreakChance(ItemStack is) {
        int i_level = CraftItemStack.asNMSCopy(is).getTag().getInt("level");
        Material m = is.getType();
        int win = 50;
        if (m == Material.WOOD_PICKAXE) {
            win += ((i_level) * 2);
        } else if (m == Material.STONE_PICKAXE) {
            win += ((i_level - 20) * 2); // +2% per level.
        } else if (m == Material.IRON_PICKAXE) {
            win += ((i_level - 40) * 2); // +2% per
        } else if (m == Material.DIAMOND_PICKAXE) {
            win += ((i_level - 60) * 2); // +2% per level.
        } else if (m == Material.GOLD_PICKAXE) {
            win += ((i_level - 80) * 2);
        }// +2% per level.
        return win;
    }

    /**
     * Sets players item in hand to upgraded Tier
     *
     * @param tier
     * @param p
     * @since 1.0
     */
    public static void lvlUp(int tier, Player p) {
        ItemStack pick = p.getEquipment().getItemInMainHand();
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(pick);
        int lvl = nms.getTag().getInt("level") + 1;
        boolean addEnchant = false;
        if (lvl < 101) {
            switch (lvl) {
                case 20:
                    tier = 2;
                    addEnchant = true;
                    break;
                case 40:
                    tier = 3;
                    addEnchant = true;
                    break;
                case 60:
                    tier = 4;
                    addEnchant = true;
                    break;
                case 80:
                    tier = 5;
                    addEnchant = true;
                    break;
            }

            p.sendMessage(ChatColor.YELLOW + "Your pick has increased to level " + ChatColor.AQUA + lvl);
            nms.getTag().setInt("maxXP", getEXPNeeded(lvl));
            nms.getTag().setInt("XP", 0);
            nms.getTag().setInt("level", lvl);
            nms.getTag().setInt("itemTier", tier);
            pick.setType(getPickType(tier));

            pick = CraftItemStack.asBukkitCopy(nms);
            ItemMeta meta = pick.getItemMeta();
            List<String> lore = meta.getLore();
            String expBar = ChatColor.RED + "||||||||||||||||||||" + "||||||||||||||||||||" + "||||||||||";
            lore.set(0, ChatColor.GRAY.toString() + "Level: " + API.getTierColor(tier) + lvl);
            lore.set(1, ChatColor.GRAY.toString() + 0 + ChatColor.GRAY.toString() + " / " + ChatColor.GRAY + Mining.getEXPNeeded(lvl));
            lore.set(2, ChatColor.GRAY.toString() + "EXP: " + expBar);
            String name = "Novice Pickaxe";

            switch (tier) {
                case 1:
                    name = ChatColor.WHITE + "Novice Pickaxe";
                    break;
                case 2:
                    name = ChatColor.GREEN.toString() + "Apprentice Pickaxe";
                    break;
                case 3:
                    name = ChatColor.AQUA.toString() + "Expert Pickaxe";
                    break;
                case 4:
                    name = ChatColor.LIGHT_PURPLE.toString() + "Supreme Pickaxe";
                    break;
                case 5:
                    name = ChatColor.YELLOW.toString() + "Master Pickaxe";
                    break;
                default:
                    break;
            }
            meta.setDisplayName(name);
            if (addEnchant) {
                giveEnchant(pick);
            }


            meta.setLore(lore);
            pick.setItemMeta(meta);
            p.getEquipment().setItemInMainHand(pick);
        }
    }

    private static void giveEnchant(ItemStack pick) {
    }

    /**
     * @param tier
     * @return
     */
    private static Material getPickType(int tier) {
        switch (tier) {
            case 1:
                return Material.WOOD_PICKAXE;
            case 2:
                return Material.STONE_PICKAXE;
            case 3:
                return Material.IRON_PICKAXE;
            case 4:
                return Material.DIAMOND_PICKAXE;
            case 5:
                return Material.GOLD_PICKAXE;
        }
        return null;
    }

    /**
     * @param type
     * @return
     */
    public static int getBlockTier(Material type) {
        switch (type) {
            case COAL_ORE:
                return 1;
            case EMERALD_ORE:
                return 2;
            case IRON_ORE:
                return 3;
            case DIAMOND_ORE:
                return 4;
            case GOLD_ORE:
                return 5;
            default:
                return 0;
        }
    }

    public static ItemStack getBlock(Material mat) {
        if (coalOre == null)
            initializeOre();
        switch (mat) {
            case COAL_ORE:
                return coalOre;
            case EMERALD_ORE:
                return emeraldOre;
            case IRON_ORE:
                return ironOre;
            case DIAMOND_ORE:
                return diamondOre;
            case GOLD_ORE:
                return goldOre;
            default:
                return coalOre;
        }
    }

    private static ItemStack coalOre;
    private static ItemStack emeraldOre;
    private static ItemStack ironOre;
    private static ItemStack diamondOre;
    private static ItemStack goldOre;


    private static void initializeOre() {
        coalOre = new ItemBuilder().setItem(Material.COAL_ORE, (short) 0, API.getTierColor(1).toString() + "Coal Ore", new String[]{ChatColor.GRAY + "A chunk of coal ore."}).build();
        emeraldOre = new ItemBuilder().setItem(Material.EMERALD_ORE, (short) 0, API.getTierColor(2).toString() + "Emerald Ore", new String[]{ChatColor.GRAY + "An unrefined piece of emerald ore."}).build();
        ironOre = new ItemBuilder().setItem(Material.IRON_ORE, (short) 0, API.getTierColor(3).toString() + "Iron Ore", new String[]{ChatColor.GRAY + "A piece of raw iron."}).build();
        diamondOre = new ItemBuilder().setItem(Material.DIAMOND_ORE, (short) 0, API.getTierColor(4).toString() + "Diamond Ore", new String[]{ChatColor.GRAY + "A sharp chunk of diamond ore."}).build();
        goldOre = new ItemBuilder().setItem(Material.GOLD_ORE, (short) 0, API.getTierColor(5).toString() + "Gold Ore", new String[]{ChatColor.GRAY + "A sparking piece of gold ore"}).build();
    }

    private HashMap<Location, Material> ORE_LOCATIONS = new HashMap<>();

    public void loadOreLocations() {
        int count = 0;
        ArrayList<String> CONFIG = (ArrayList<String>) DungeonRealms.getInstance().getConfig().getStringList("orespawns");
        for (String line : CONFIG) {
            if (line.contains("=")) {
                try {
                    String[] cords = line.split("=")[0].split(",");
                    Location loc = new Location(Bukkit.getWorlds().get(0), Double.parseDouble(cords[0]),
                            Double.parseDouble(cords[1]), Double.parseDouble(cords[2]));

                    String material_data = line.split("=")[1];
                    Material m = Material.getMaterial(material_data);

                    ORE_LOCATIONS.put(loc, m);

                    count++;
                } catch (NullPointerException npe) {
                    npe.printStackTrace();
                }
            }
        }
        Utils.log.info("[Profession] " + count + " ORE SPAWN locations have been LOADED.");
    }

    private static Mining instance;

    public static Mining getInstance() {
        if (instance == null)
            instance = new Mining();
        return instance;

    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
    public void startInitialization() {
        loadOreLocations();
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), this::placeOre);
    }

    /**
     * Place all ore from ORE_LOCATIONS
     *
     * @since 1.0;
     */
    private void placeOre() {
        ORE_LOCATIONS.keySet().stream().forEach(loc -> loc.getWorld().getBlockAt(loc).setType(ORE_LOCATIONS.get(loc)));
    }

    @Override
    public void stopInvocation() {

    }

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

    /**
     * @param i
     * @return
     */
    public static double getLvl(ItemStack i) {
        return CraftItemStack.asNMSCopy(i).getTag().getInt("level");
    }
}
