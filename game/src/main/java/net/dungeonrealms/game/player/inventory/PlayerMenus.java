package net.dungeonrealms.game.player.inventory;

import com.google.common.collect.Lists;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerToggles.Toggles;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.achievements.Achievements.AchievementCategory;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.ParticleAPI.ParticleEffect;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.player.inventory.menus.guis.PetSelectionGUI;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveOpenProfile;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMountSkins;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * Created by Nick on 9/29/2015.
 */
public class PlayerMenus {

    public static void openFriendsMenu(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getWrapper(player);
        Map<UUID, Integer> friends = wrapper.getFriendsList();

        Inventory inv = Bukkit.createInventory(null, 54, "Friends");

        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to go back!",
                ChatColor.GRAY + "Display Item"
        }));


        int slot = 9;
        for (UUID u : friends.keySet()) {
            String name = SQLDatabaseAPI.getInstance().getNameFromUUID(u);
            ItemStack stack = editItem(name, name, new String[]{
                    ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Right-Click " + ChatColor.GRAY + "to delete!",
                    ChatColor.GRAY + "Display Item"
            });

            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("info", new NBTTagString(u.toString()));
            nmsStack.setTag(tag);


            inv.setItem(slot, CraftItemStack.asBukkitCopy(nmsStack));

            if (slot >= 54) break;
            slot++;
        }

        player.openInventory(inv);

    }

    public static void openFriendInventory(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getWrapper(player);

        Inventory inv = Bukkit.createInventory(null, 45, "Friend Management");

        inv.setItem(0, editItem(new ItemStack(Material.BOOK_AND_QUILL), ChatColor.GREEN + "Add Friend", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to add friend!",
                ChatColor.GRAY + "Display Item"
        }));

        inv.setItem(1, editItem(new ItemStack(Material.CHEST), ChatColor.GREEN + "View Friend", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to view friends!",
                ChatColor.GRAY + "Display Item"
        }));

        int slot = 9;
        for (Map.Entry<UUID, Integer> from : wrapper.getPendingFriends().entrySet()) {
            String name = SQLDatabaseAPI.getInstance().getUsernameFromUUID(from.getKey());
            ItemStack stack = editItem(name, name, new String[]{
                    ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to accept!",
                    ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Right-Click " + ChatColor.GRAY + "to deny!",
                    ChatColor.GRAY + "Display Item"
            });

            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("info", new NBTTagString(from.getKey().toString()));
            nmsStack.setTag(tag);

            inv.setItem(slot, CraftItemStack.asBukkitCopy(nmsStack));

            if (slot >= 44) break;
            slot++;
        }

        player.openInventory(inv);

    }

    public static void openPlayerPetMenu(Player player, InventoryAction action) {
    	
        if (GameAPI.getGamePlayer(player) != null && GameAPI.getGamePlayer(player).isJailed()) {
            Inventory jailed = Bukkit.createInventory(null, 0, ChatColor.RED + "You are jailed");
            player.openInventory(jailed);
            return;
        }
        new PetSelectionGUI(player).open(player, action);
//
//        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
//        if (wrapper == null) return;
//
//        Map<EnumPets, PetData> playerPets = wrapper.getPetsUnlocked();
//
//        int size = EnumPets.values().length + 2; //Add 2 for the buttons
//        size -= size % 9;
//
//        Inventory inv = Bukkit.createInventory(null, size, "Pet Selection");
//        inv.setItem(size - 2, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));
//        inv.setItem(size -1, editItem(new ItemStack(Material.LEASH), ChatColor.GREEN + "Dismiss Pet", new String[]{}));
//
//
//        for(EnumPets pets : EnumPets.values()) {
//            if(!pets.isShowInGui() && !Rank.isGM(player))
//            	continue;
//
//            PetData hisData = playerPets.get(pets);
//            ItemStack itemStack = new ItemStack(Material.MONSTER_EGG, 1, (short) pets.getEggShortData());
//            boolean isLocked = hisData == null || !hisData.isUnlocked();
//            if(pets.isSubGetsFree() && wrapper.getRank().isSUB())
//                isLocked = false;
//
//            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
//            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
//            tag.set("pet", new NBTTagString(pets.name()));
//            tag.set("petName", new NBTTagString(hisData != null && hisData.getPetName() != null ? hisData.getPetName() : pets.getDisplayName()));
//            nmsStack.setTag(tag);
//            inv.addItem(new ItemPetSelector(editItemWithShort(CraftItemStack.asBukkitCopy(nmsStack), (short) pets.getEggShortData(), ChatColor.WHITE + pets.getDisplayName(), new String[]{
//                    ChatColor.GREEN + "Left Click: " + ChatColor.WHITE + "Summon Pet",
//                    ChatColor.GREEN + "Right Click: " + ChatColor.WHITE + "Rename Pet",
//                    "",
//                    ChatColor.GREEN + "Name: " + ChatColor.WHITE + (hisData != null && hisData.getPetName() != null ? hisData.getPetName() : pets.getDisplayName()),
//                    (isLocked ? ChatColor.RED : ChatColor.GREEN) + "" + ChatColor.BOLD + (isLocked ? "" : "UN") + "LOCKED",
//                    ChatColor.GRAY + "Display Item"
//            })).generateItem());
//        }
//        player.openInventory(inv);
    }
//
//    public static void openPlayerMountMenu(Player player) {
//        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
//        if (wrapper == null) return;
//
//        if (GameAPI.getGamePlayer(player) != null && GameAPI.getGamePlayer(player).isJailed()) {
//            Inventory jailed = Bukkit.createInventory(null, 0, ChatColor.RED + "You are jailed");
//            player.openInventory(jailed);
//            return;
//        }
//
//        List<EnumMounts> playerMounts = wrapper.getMountsUnlocked();
//
//        if (playerMounts.isEmpty()) {
//            Inventory noMounts = Bukkit.createInventory(null, 0, ChatColor.RED + "You have no Mounts!");
//            player.openInventory(noMounts);
//            return;
//        }
//
//        Inventory inv = Bukkit.createInventory(null, 27, "Mount Selection");
//        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));
//        inv.setItem(26, editItem(new ItemStack(Material.LEASH), ChatColor.GREEN + "Dismiss Mount", new String[]{}));
//
//        for (EnumMounts m : EnumMounts.values()) {
//        	VanillaItem mount = new VanillaItem(m.getSelectionItem());
//        	mount.setTagString("mountType", m.name());
//        	mount.setDisplay(true);
//        	mount.setDisplayName(ChatColor.GREEN + m.getDisplayName());
//        	if (m.getMountData() != null)
//        		m.getMountData().getLore().forEach(mount::addLore);
//        	inv.addItem(mount.generateItem());
//        }
//
//        player.openInventory(inv);
//    }

    public static void openPlayerParticleMenu(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getWrapper(player);
        Set<ParticleEffect> trails = Rank.isSUB(player) ? new HashSet<>(Lists.newArrayList(ParticleEffect.values())) : wrapper.getTrails();

        if (trails.isEmpty()) {
            Inventory noTrails = Bukkit.createInventory(null, 0, ChatColor.RED + "You have no Player Effects!");
            player.openInventory(noTrails);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, "Player Effect Selection");
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));
        inv.setItem(26, editItem(new ItemStack(Material.ARMOR_STAND), ChatColor.GREEN + "Turn off Effect", new String[]{}));
        
        for (ParticleEffect effect : trails) {
        	VanillaItem item = new VanillaItem(effect.getSelectionItem());
        	item.setTagString("playerTrailType", effect.name());
        	item.setDisplay(true);
        	item.setDisplayName(ChatColor.GREEN + effect.getDisplayName());
        	inv.addItem(item.generateItem());
        }

        player.openInventory(inv);
    }

    public static void openPlayerMountSkinMenu(Player player) {
    	Set<EnumMountSkins> skins = PlayerWrapper.getWrapper(player).getMountSkins();

        if (skins.isEmpty()) {
            Inventory noSkins = Bukkit.createInventory(null, 0, ChatColor.RED + "You have no Mount Skins!");
            player.openInventory(noSkins);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, "Mount Skin Selection");
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));
        inv.setItem(26, editItem(new ItemStack(Material.ARMOR_STAND), ChatColor.GREEN + "Turn off Mount Skin", new String[]{}));
        
        for (EnumMountSkins s : skins) {
        	VanillaItem item = new VanillaItem(s.getSelectionItem());
        	item.setTagString("skinType", s.name());
        	item.setDisplay(true);
        	item.setDisplayName(ChatColor.GREEN + s.getDisplayName());
        	inv.addItem(item.generateItem());
        }

        player.openInventory(inv);
    }

    public static void openPlayerAchievementsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Achievements");
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));
        for (int i = 2; i < 2 + AchievementCategory.values().length; i++) {
        	AchievementCategory c = AchievementCategory.values()[i - 2];
        	inv.setItem(i, editItem(new ItemStack(c.getIcon()), ChatColor.GOLD + c.getName(), new String[] {
        		"", ChatColor.GRAY + "" + ChatColor.ITALIC + "Achievements related to " + c.getDescription() + ".", ""}));
        }
        
        player.openInventory(inv);
    }
    
    public static void openPlayerProfileMenu(Player player) {
    	player.openInventory(getPlayerProfileMenu(player));
    }

    public static Inventory getPlayerProfileMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Profile");
        inv.setItem(0, editItem(new ItemStack(Material.EXP_BOTTLE), ChatColor.GOLD + "Stat Distribution", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Place stat points on different attributes. ",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View Attributes.",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(1, editItem("Shrek", ChatColor.GOLD + "Friend List", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Add or remove friends.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View Friend list.",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(4, editItem(Utils.getPlayerHead(player), ChatColor.GREEN + "Player Profile", new String[]{
        }));
        inv.setItem(6, editItem(new ItemStack(Material.EYE_OF_ENDER), ChatColor.GOLD + "Effects", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Stand out amongst the rest",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "with powerful effects.",
                "",
                ChatColor.WHITE + "Left-Click:" + ChatColor.GREEN + " View obtained effect.",
                ChatColor.WHITE + "Right-Click:" + ChatColor.GREEN + " Receive effect item.",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(7, editItem(new ItemStack(Material.SADDLE), ChatColor.GOLD + "Mounts", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Travel Andalucia quickly.",
                "",
                ChatColor.WHITE + "Left-Click:" + ChatColor.GREEN + " View obtained mounts.",
                ChatColor.WHITE + "Right-Click:" + ChatColor.GREEN + " Receive Saddle.",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(8, editItem(new ItemStack(Material.NAME_TAG), ChatColor.GOLD + "Pets", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Travel with a cute companion.",
                "",
                ChatColor.WHITE + "Left-Click:" + ChatColor.GREEN + " View available pets.",
                ChatColor.WHITE + "Right-Click:" + ChatColor.GREEN + " Receive Pet Leash.",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(16, editItem(new ItemStack(Material.LEASH), ChatColor.GOLD + "Storage Mule", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Inventory getting full on your travels?",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "purchase a Mule from the Animal Tamer.",
                "",
                ChatColor.WHITE + "Left-Click:" + ChatColor.GREEN + " Spawn Storage Mule.",
                ChatColor.WHITE + "Right-Click:" + ChatColor.GREEN + " Receive Mule Leash.",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(17, editItem(new ItemStack(Material.ARMOR_STAND), ChatColor.GOLD + "Mount Skins", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Equip your mount with a fancy skin.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View obtained mount skins.",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(18, editItem(new ItemStack(Material.EMERALD), ChatColor.GOLD + "E-Cash Vendor", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "E-Cash is obtained by voting and online store purchase.",
                ChatColor.GRAY + "http://dungeonrealms.net/shop",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " Open the E-Cash Vendor.",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(24, editItem(new ItemStack(Material.COMPASS), ChatColor.GOLD + "Achievements", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Check your progress.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View achievements.",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(26, editItem(new ItemStack(Material.REDSTONE_COMPARATOR), ChatColor.GOLD + "Toggles", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Adjust preferences here.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " Open toggles menu.",
                ChatColor.GRAY + "Display Item"
        }));
        Quests.getInstance().triggerObjective(player, ObjectiveOpenProfile.class);
        return inv;
    }

    public static ItemStack editItem(String playerName, String name, String[] lore) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(playerName);
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack editItem(ItemStack itemStack, String name, String[] lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(meta);
        itemStack.setAmount(1);
        return itemStack;
    }

    public static ItemStack editItemWithShort(ItemStack itemStack, short shortID, String name, String[] lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        itemStack.setDurability(shortID);
        meta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(meta);
        itemStack.setAmount(1);
        return itemStack;
    }

    public static ItemStack editItem(ItemStack itemStack, String[] lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(meta);
        itemStack.setAmount(1);
        return itemStack;
    }

    /**
     * @param player
     */
    public static void openToggleMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, ((int) Math.ceil((1 + Toggles.values().length) / 9.0) * 9), "Toggles");

        int i = 0;
        PlayerWrapper wrapper = PlayerWrapper.getWrapper(player);
        for (Toggles t : Toggles.getToggles(player)) {
        	if (!wrapper.getRank().isAtLeast(t.getMinRank()))
        		continue;
        	boolean toggle = wrapper.getToggles().getState(t);
        	VanillaItem vi = new VanillaItem(new ItemStack(Material.INK_SACK, 1, (short) (toggle ? 10 : 8)));
        	vi.setDisplay(true);
        	vi.setDisplayName(( toggle ? ChatColor.GREEN : ChatColor.RED) + "/" + t.getCommand());
        	vi.addLore(t.getDescription());
        	vi.setTagString("toggle", t.name());
        	inv.setItem(i, vi.generateItem());
        	i++;
        }

        inv.setItem(i, ItemManager.createItem(Material.BARRIER, ChatColor.YELLOW + "Back", new String[]{
                ChatColor.AQUA + "Back to the Profile Menu!",
                ChatColor.GRAY + "Display Item"}));
        player.openInventory(inv);
    }

    /**
     * Opens the GM Toggles menu.
     * (user must be GM)
     *
     * @param player
     */
    public static void openGameMasterTogglesMenu(Player player) {
        if (!Rank.isTrialGM(player)) return;
        boolean isToggled = false;

        Inventory inv = Bukkit.createInventory(null, 9, "Game Master Toggles");
        GamePlayer gp = GameAPI.getGamePlayer(player);
        if (gp == null) return;

        // Invisible
        isToggled = GameAPI._hiddenPlayers.contains(player);
        inv.setItem(0, new ItemBuilder().setItem(new ItemStack(Material.INK_SACK, 1, (short) (isToggled ? 10 : 8)), (isToggled ? ChatColor.GREEN : ChatColor.RED) + "Invisible Mode", new String[]{
                ChatColor.GRAY + "Toggling this will make you invisible to players and mobs.",
                ChatColor.GRAY + "Display Item"}).build());

        // Allow Fight
        isToggled = !gp.isInvulnerable() && gp.isTargettable();
        inv.setItem(1, new ItemBuilder().setItem(new ItemStack(Material.INK_SACK, 1, (short) (isToggled ? 10 : 8)), (isToggled ? ChatColor.GREEN : ChatColor.RED) + "Allow Combat", new String[]{
                ChatColor.GRAY + "Toggling this will make you vulnerable to attacks but also allow outgoing damage.",
                ChatColor.GRAY + "Display Item"}).build());

        // Stream Mode
        isToggled = gp.isStreamMode();
        inv.setItem(2, new ItemBuilder().setItem(new ItemStack(Material.INK_SACK, 1, (short) (isToggled ? 10 : 8)), (isToggled ? ChatColor.GREEN : ChatColor.RED) + "Stream Mode", new String[]{
                ChatColor.GRAY + "Disable sensitive messages from being displayed.",
                ChatColor.GRAY + "Display Item"}).build());

        player.openInventory(inv);
    }

    /**
     * Opens the Head GM Toggles menu.
     * (user must be Head GM)
     *
     * @param player
     */
    public static void openHeadGameMasterTogglesMenu(Player player) {
        if (!Rank.isHeadGM(player)) return;
        boolean isToggled = false;

        Inventory inv = Bukkit.createInventory(null, 9, "Head Game Master Toggles");
        GamePlayer gp = GameAPI.getGamePlayer(player);
        if (gp == null) return;

        // Game Master Extended Permissions
        isToggled = DungeonRealms.getInstance().isGMExtendedPermissions;
        inv.setItem(0, new ItemBuilder().setItem(new ItemStack(Material.INK_SACK, 1, (short) (isToggled ? 10 : 8)), (isToggled ? ChatColor.GREEN : ChatColor.RED) + "Game Master Extended Permissions", new String[]{
                ChatColor.GRAY + "Toggling this will allow GMs to have extended permissions.",
                ChatColor.GRAY + "This should be used for events and grants access to features such as adding items.",
                ChatColor.GRAY + "Display Item"}).build());

        player.openInventory(inv);
    }

}