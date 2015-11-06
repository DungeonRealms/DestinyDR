package net.dungeonrealms.miscellaneous;

import net.dungeonrealms.items.enchanting.EnchantmentAPI;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

/**
 * Created by Nick on 11/3/2015.
 */
public class SandS implements GenericMechanic {

    static SandS instance = null;

    public static SandS getInstance() {
        if (instance == null) {
            instance = new SandS();
        }
        return instance;
    }


    @Override
    public EnumPriority startPriority() {
        return EnumPriority.ARCHBISHOPS;
    }

    @Override
    public void startInitialization() {

    }

    @Override
    public void stopInvocation() {

    }

    public ItemStack getScroll(ScrollType type, int tier) {
        switch (type) {
            case ENCHANTMENT_SCROLL:
                switch (tier) {
                    case 1:
                        return new ItemBuilder().setItem(new ItemStack(Material.PAPER), ChatColor.WHITE.toString() + ChatColor.BOLD + "Scroll: " + ChatColor.WHITE + "Enchant Leather Armor", new String[]{
                                ChatColor.RED + "+5% HP",
                                ChatColor.RED + "+5% HP REGEN",
                                ChatColor.GRAY + "  - OR -",
                                ChatColor.RED + "+1% ENERGY REGEN",

                        }).setNBTString("scrollTier", String.valueOf(tier)).setNBTString("scrollType", String.valueOf(type.getId())).build();
                    case 2:
                        return new ItemBuilder().setItem(new ItemStack(Material.PAPER), ChatColor.WHITE.toString() + ChatColor.BOLD + "Scroll: " + ChatColor.GREEN + "Enchant Chain Armor", new String[]{
                                ChatColor.RED + "+5% HP",
                                ChatColor.RED + "+5% HP REGEN",
                                ChatColor.GRAY + "  - OR -",
                                ChatColor.RED + "+1% ENERGY REGEN",

                        }).setNBTString("scrollTier", String.valueOf(tier)).setNBTString("scrollType", String.valueOf(type.getId())).build();
                    case 3:
                        return new ItemBuilder().setItem(new ItemStack(Material.PAPER), ChatColor.WHITE.toString() + ChatColor.BOLD + "Scroll: " + ChatColor.AQUA + "Enchant Iron Armor", new String[]{
                                ChatColor.RED + "+5% HP",
                                ChatColor.RED + "+5% HP REGEN",
                                ChatColor.GRAY + "  - OR -",
                                ChatColor.RED + "+1% ENERGY REGEN",

                        }).setNBTString("scrollTier", String.valueOf(tier)).setNBTString("scrollType", String.valueOf(type.getId())).build();
                    case 4:
                        return new ItemBuilder().setItem(new ItemStack(Material.PAPER), ChatColor.WHITE.toString() + ChatColor.BOLD + "Scroll: " + ChatColor.LIGHT_PURPLE + "Enchant Diamond Armor", new String[]{
                                ChatColor.RED + "+5% HP",
                                ChatColor.RED + "+5% HP REGEN",
                                ChatColor.GRAY + "  - OR -",
                                ChatColor.RED + "+1% ENERGY REGEN",

                        }).setNBTString("scrollTier", String.valueOf(tier)).setNBTString("scrollType", String.valueOf(type.getId())).build();
                    case 5:
                        return new ItemBuilder().setItem(new ItemStack(Material.PAPER), ChatColor.WHITE.toString() + ChatColor.BOLD + "Scroll: " + ChatColor.YELLOW + "Enchant Gold Armor", new String[]{
                                ChatColor.RED + "+5% HP",
                                ChatColor.RED + "+5% HP REGEN",
                                ChatColor.GRAY + "  - OR -",
                                ChatColor.RED + "+1% ENERGY REGEN",

                        }).setNBTString("scrollTier", String.valueOf(tier)).setNBTString("scrollType", String.valueOf(type.getId())).build();
                }
            case WHITE_SCROLL:
                switch (tier) {
                    case 1:
                        return new ItemBuilder().setItem(new ItemStack(Material.PAPER), ChatColor.WHITE.toString() + ChatColor.BOLD + "White Scroll: " + ChatColor.WHITE + "Protect Leather Equipment", new String[]{
                                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Apply to any T1 item to " + ChatColor.UNDERLINE + "prevent" + ChatColor.GRAY.toString() + ChatColor.ITALIC + " it",
                                ChatColor.GRAY.toString() + ChatColor.ITALIC + "from being destroyed if the next",
                                ChatColor.GRAY.toString() + ChatColor.ITALIC + "enchantment scroll fails."

                        }).setNBTString("scrollTier", String.valueOf(tier)).setNBTString("scrollType", String.valueOf(type.getId())).build();
                    case 2:
                        return new ItemBuilder().setItem(new ItemStack(Material.PAPER), ChatColor.WHITE.toString() + ChatColor.BOLD + "White Scroll: " + ChatColor.GREEN + "Protect Chain Equipment", new String[]{
                                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Apply to any T2 item to " + ChatColor.UNDERLINE + "prevent" + ChatColor.GRAY.toString() + ChatColor.ITALIC + " it",
                                ChatColor.GRAY.toString() + ChatColor.ITALIC + "from being destroyed if the next",
                                ChatColor.GRAY.toString() + ChatColor.ITALIC + "enchantment scroll fails."

                        }).setNBTString("scrollTier", String.valueOf(tier)).setNBTString("scrollType", String.valueOf(type.getId())).build();
                    case 3:
                        return new ItemBuilder().setItem(new ItemStack(Material.PAPER), ChatColor.WHITE.toString() + ChatColor.BOLD + "White Scroll: " + ChatColor.AQUA + "Protect Iron Equipment", new String[]{
                                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Apply to any T3 item to " + ChatColor.UNDERLINE + "prevent" + ChatColor.GRAY.toString() + ChatColor.ITALIC + " it",
                                ChatColor.GRAY.toString() + ChatColor.ITALIC + "from being destroyed if the next",
                                ChatColor.GRAY.toString() + ChatColor.ITALIC + "enchantment scroll fails."

                        }).setNBTString("scrollTier", String.valueOf(tier)).setNBTString("scrollType", String.valueOf(type.getId())).build();
                    case 4:
                        return new ItemBuilder().setItem(new ItemStack(Material.PAPER), ChatColor.WHITE.toString() + ChatColor.BOLD + "White Scroll: " + ChatColor.LIGHT_PURPLE + "Protect Diamond Equipment", new String[]{
                                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Apply to any T4 item to " + ChatColor.UNDERLINE + "prevent" + ChatColor.GRAY.toString() + ChatColor.ITALIC + " it",
                                ChatColor.GRAY.toString() + ChatColor.ITALIC + "from being destroyed if the next",
                                ChatColor.GRAY.toString() + ChatColor.ITALIC + "enchantment scroll fails."

                        }).setNBTString("scrollTier", String.valueOf(tier)).setNBTString("scrollType", String.valueOf(type.getId())).build();
                    case 5:
                        return new ItemBuilder().setItem(new ItemStack(Material.PAPER), ChatColor.WHITE.toString() + ChatColor.BOLD + "White Scroll: " + ChatColor.YELLOW + "Protect Gold Equipment", new String[]{
                                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Apply to any T5 item to " + ChatColor.UNDERLINE + "prevent" + ChatColor.GRAY.toString() + ChatColor.ITALIC + " it",
                                ChatColor.GRAY.toString() + ChatColor.ITALIC + "from being destroyed if the next",
                                ChatColor.GRAY.toString() + ChatColor.ITALIC + "enchantment scroll fails."

                        }).setNBTString("scrollTier", String.valueOf(tier)).setNBTString("scrollType", String.valueOf(type.getId())).build();
                }
                break;
        }
        return null;
    }

    public boolean isScroll(ItemStack itemStack) {
        return CraftItemStack.asNMSCopy(itemStack).hasTag() && CraftItemStack.asNMSCopy(itemStack).getTag().hasKey("scrollTier");
    }

    public boolean isItemProtected(ItemStack itemStack) {
        if (CraftItemStack.asNMSCopy(itemStack).hasTag() && CraftItemStack.asNMSCopy(itemStack).getTag().hasKey("protected")) {
            return Boolean.valueOf(CraftItemStack.asNMSCopy(itemStack).getTag().getString("protected"));
        }
        return false;
    }

    public enum ScrollType {
        /**
         * 8 levels.
         * Each time you enchant something it levels up +1,+2,+3
         * 5 different type of enchantment scrolls.
         * Left click on item, (booster on terms of core stats)
         * either,
         * INCREASE Hp by 5% regardless..
         * If they have HP regen goes up by 5% OR if it has energy regen increase 1%.
         * ____________________
         * WEAPONS
         * minimum and maximum damage are increased by 5% regardless of ANYTHING.
         * Can use to +1,+2,+3 from +4 to +12 each increasing level of enchant.
         */
        ENCHANTMENT_SCROLL(0),
        /**
         * PROTECTION SCROLLS
         * protects items, doesn't stop enchant from failing but if it does fail
         * the item doesn't get destroyed.
         */
        WHITE_SCROLL(1),;

        private int id;

        ScrollType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static ScrollType getById(int id) {
            for (ScrollType st : values()) {
                if (st.getId() == id) {
                    return st;
                }
            }
            return null;
        }
    }

    /**
     * Invokes the change.
     *
     * @param type
     * @param player
     * @param itemStack
     * @apiNote Precheck should be preformed ot make usre theitem has
     * protections scroll! This method doesn't do that!
     * @since 1.0
     */
    public void invokeChange(int type, Player player, ItemStack itemStack, int tier) {

        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);

        NBTTagCompound tag = nmsStack.getTag();

        System.out.println("invoke change");

        switch (type) {
            /*
            Armor
             */
            case 0:
                if (tag.hasKey("healthPoints")) {
                    System.out.println("PAST HEALTH POINTS " + tag.getInt("healthPoints"));
                    int preHealthPoints = tag.getInt("healthPoints");
                    int postHealthPoints = (int) Math.round((preHealthPoints * .05) + preHealthPoints);
                    System.out.println("CHECK: POST: " + postHealthPoints + " PRE: " + preHealthPoints);
                    tag.set("healthPoints", new NBTTagInt(postHealthPoints));
                }
                if (tag.hasKey("healthRegen")) {
                    int preHealthRegen = tag.getInt("healthRegen");
                    int postHealthRegen = (int) Math.round((preHealthRegen * .05) + preHealthRegen);
                    tag.set("healthRegen", new NBTTagInt(postHealthRegen));
                } else if (tag.hasKey("energyRegen")) {
                    int energyRegen = (int) Math.round((tag.getInt("energyRegen") * .01) + tag.getInt("energyRegen"));
                    tag.set("healthRegen", new NBTTagInt(energyRegen));
                }
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SPLASH, player.getLocation(), 0, 0, 0, 10, 100);
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 63f);
                //TODO: Remove Protection
                break;
            /*
            Items/=(Weapons)
             */
            case 1:
                if (tag.hasKey("damage")) {
                    int damage = (int) Math.round((tag.getInt("damage") * .05) + tag.getInt("damage"));
                    tag.set("damage", new NBTTagInt(damage));
                }
                break;
            default:
                Utils.log.warning("Default called in invokeChange() ... FAIL");
        }


        nmsStack.setTag(tag);

        ItemStack bukkitStack = EnchantmentAPI.removeItemProtection(CraftItemStack.asBukkitCopy(nmsStack));
        ItemMeta meta = bukkitStack.getItemMeta();
        String name = meta.getDisplayName();
        if (name.contains("]")) {
            String rawName = name.split("]")[1].trim();
            int cTier = Integer.valueOf(name.split("\\+")[1].split("]")[0]);
            if (cTier == 13) {
                player.sendMessage(ChatColor.RED + "You cannot enchant that weapon any higher!");
                return;
            }
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "[+" + (cTier + 1) + "] " + ChatColor.RESET + rawName);
        } else {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "[+" + tier + "] " + ChatColor.RESET + name);
        }
        bukkitStack.setItemMeta(meta);

        player.getInventory().remove(itemStack);
        player.getInventory().addItem(bukkitStack);

    }

    public void applyScroll(InventoryClickEvent event, Player player, ItemStack itemStack, ItemStack scrollItem) {
        if (!isScroll(scrollItem)) return;

        NBTTagCompound tag = CraftItemStack.asNMSCopy(scrollItem).getTag();
        ScrollType scrollType = ScrollType.getById(Integer.valueOf(tag.getString("scrollType")));
        int tier = Integer.valueOf(tag.getString("scrollTier"));

        double number = new Random().nextInt(101);

        event.setCursor(new ItemStack(Material.AIR));
        event.setCurrentItem(new ItemStack(Material.AIR));

        switch (scrollType) {
            case ENCHANTMENT_SCROLL:
                //Applying Attributes Here
                if (EnchantmentAPI.isItemArmor(itemStack)) {
                    int armorTier = CraftItemStack.asNMSCopy(itemStack).getTag().getInt("armorTier");
                    if (armorTier != tier) return;
                    switch (armorTier) {
                        case 1:
                            if (number < 95) {
                                invokeChange(0, player, itemStack, 1);
                            } else {
                                if (EnchantmentAPI.isItemProtected(itemStack)) {
                                    player.sendMessage(ChatColor.RED + "Your enchant failed! Fortunately, you keep your item because it has a protection scroll!");
                                    EnchantmentAPI.removeItemProtection(itemStack);
                                } else {
                                    player.getInventory().remove(itemStack);
                                }
                            }
                            break;
                        case 2:
                            if (number < 85) {
                                invokeChange(0, player, itemStack, 2);
                            } else {
                                if (EnchantmentAPI.isItemProtected(itemStack)) {
                                    player.sendMessage(ChatColor.RED + "Your enchant failed! Fortunately, you keep your item because it has a protection scroll!");
                                    EnchantmentAPI.removeItemProtection(itemStack);
                                } else {
                                    player.getInventory().remove(itemStack);
                                }
                            }
                            break;
                        case 3:
                            if (number < 75) {
                                invokeChange(0, player, itemStack, 3);
                            } else {
                                if (EnchantmentAPI.isItemProtected(itemStack)) {
                                    player.sendMessage(ChatColor.RED + "Your enchant failed! Fortunately, you keep your item because it has a protection scroll!");
                                    EnchantmentAPI.removeItemProtection(itemStack);
                                } else {
                                    player.getInventory().remove(itemStack);
                                }
                            }
                            break;
                        case 4:
                            if (number < 30) {
                                invokeChange(0, player, itemStack, 4);
                            } else {
                                if (EnchantmentAPI.isItemProtected(itemStack)) {
                                    player.sendMessage(ChatColor.RED + "Your enchant failed! Fortunately, you keep your item because it has a protection scroll!");
                                    EnchantmentAPI.removeItemProtection(itemStack);
                                } else {
                                    player.getInventory().remove(itemStack);
                                }
                            }
                            break;
                        case 5:
                            if (number < 10) {
                                invokeChange(0, player, itemStack, 5);
                            } else {
                                if (EnchantmentAPI.isItemProtected(itemStack)) {
                                    player.sendMessage(ChatColor.RED + "Your enchant failed! Fortunately, you keep your item because it has a protection scroll!");
                                    EnchantmentAPI.removeItemProtection(itemStack);
                                } else {
                                    player.getInventory().remove(itemStack);
                                }
                            }
                            break;
                        default:
                            Utils.log.warning("Unable to find max switch() of SandS armorTier. " + tag.getInt("armorTier"));
                    }
                } else if (EnchantmentAPI.isItemWeapon(itemStack)) {
                    int itemTier = CraftItemStack.asNMSCopy(itemStack).getTag().getInt("itemTier");
                    if (itemTier != tier) return;
                    switch (itemTier) {
                        case 1:
                            if (number < 95) {
                                invokeChange(1, player, itemStack, 1);
                            } else {
                                player.sendMessage(ChatColor.RED + "You enchant failed!");
                            }
                            break;
                        case 2:
                            if (number < 95) {
                                invokeChange(1, player, itemStack, 2);
                            } else {
                                player.sendMessage(ChatColor.RED + "You enchant failed!");
                            }
                            break;
                        case 3:
                            if (number < 95) {
                                invokeChange(1, player, itemStack, 3);
                            } else {
                                player.sendMessage(ChatColor.RED + "You enchant failed!");
                            }
                            break;
                        case 4:
                            if (number < 95) {
                                invokeChange(1, player, itemStack, 4);
                            } else {
                                player.sendMessage(ChatColor.RED + "You enchant failed!");
                            }
                            break;
                        case 5:
                            if (number < 95) {
                                invokeChange(1, player, itemStack, 5);
                            } else {
                                player.sendMessage(ChatColor.RED + "You enchant failed!");
                            }
                            break;
                        default:
                            Utils.log.warning("Unable to find max switch() of SandS itemTier.");
                    }
                } else {
                    Utils.log.warning("JIMMY CRICKETS, ITS NOT ARMOR OR AN ITEM!? ITS A... NULL!");
                }
                break;
            case WHITE_SCROLL:
                System.out.println("WHITE SCROLLCALLED");
                int genericTier = 0;
                if (CraftItemStack.asNMSCopy(itemStack).getTag().hasKey("itemTier")) {
                    genericTier = CraftItemStack.asNMSCopy(itemStack).getTag().getInt("itemTier");
                    if (genericTier != tier) {
                        System.out.println("Returning white scroll item " + genericTier + " != " + tier);
                    }
                } else if (CraftItemStack.asNMSCopy(itemStack).getTag().hasKey("armorTier")) {
                    genericTier = CraftItemStack.asNMSCopy(itemStack).getTag().getInt("armorTier");
                    if (genericTier != tier) {
                        System.out.println("Returning white scroll armor " + genericTier + " != " + tier);
                        return;
                    }
                }
                System.out.println("GENERIC TIER: " + genericTier);
                switch (genericTier) {
                    case 1:
                        if (EnchantmentAPI.isItemProtected(itemStack)) {
                            player.sendMessage(ChatColor.RED + "That item is already protected!");
                        } else {
                            player.getInventory().addItem(EnchantmentAPI.addItemProtection(itemStack));
                        }
                        break;
                    case 2:
                        if (EnchantmentAPI.isItemProtected(itemStack)) {
                            player.sendMessage(ChatColor.RED + "That item is already protected!");
                        } else {
                            player.getInventory().addItem(EnchantmentAPI.addItemProtection(itemStack));
                        }
                        break;
                    case 3:
                        if (EnchantmentAPI.isItemProtected(itemStack)) {
                            player.sendMessage(ChatColor.RED + "That item is already protected!");
                        } else {
                            player.getInventory().addItem(EnchantmentAPI.addItemProtection(itemStack));
                        }
                        break;
                    case 4:
                        if (EnchantmentAPI.isItemProtected(itemStack)) {
                            player.sendMessage(ChatColor.RED + "That item is already protected!");
                        } else {
                            player.getInventory().addItem(EnchantmentAPI.addItemProtection(itemStack));
                        }
                        break;
                    case 5:
                        if (EnchantmentAPI.isItemProtected(itemStack)) {
                            player.sendMessage(ChatColor.RED + "That item is already protected!");
                        } else {
                            player.getInventory().addItem(EnchantmentAPI.addItemProtection(itemStack));
                        }
                        break;
                    default:
                        Utils.log.warning("Unable to find max switch() of SandS itemTier. " + genericTier);
                }

        }

    }
}
