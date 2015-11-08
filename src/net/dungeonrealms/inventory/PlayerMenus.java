package net.dungeonrealms.inventory;

import com.mongodb.Block;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.types.mounts.EnumMounts;
import net.dungeonrealms.entities.types.pets.EnumPets;
import net.dungeonrealms.handlers.MailHandler;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.ItemManager;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.dungeonrealms.miscellaneous.ItemBuilder;
import net.dungeonrealms.mongo.Database;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumGuildData;
import net.dungeonrealms.teleportation.TeleportAPI;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Nick on 9/29/2015.
 */
@SuppressWarnings({"unchecked", "chasesTouch"})
public class PlayerMenus {

    public static void openFriendsMenu(Player player) {
        UUID uuid = player.getUniqueId();
        ArrayList<String> friendRequest = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.FRIENDS, uuid);

        Inventory inv = Bukkit.createInventory(null, 54, "Friends");

        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to go back!"
        }));


        int slot = 9;
        for (String s : friendRequest) {
            String name = API.getNameFromUUID(s);
            ItemStack stack = editItem(name, name, new String[]{
                    ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Right-Click " + ChatColor.GRAY + "to delete!"
            });

            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("info", new NBTTagString(s));
            nmsStack.setTag(tag);


            inv.setItem(slot, CraftItemStack.asBukkitCopy(nmsStack));

            if (slot >= 54) break;
            slot++;
        }

        player.openInventory(inv);

    }

    public static void openFriendInventory(Player player) {
        UUID uuid = player.getUniqueId();
        ArrayList<String> friendRequest = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.FRIEND_REQUSTS, uuid);

        Inventory inv = Bukkit.createInventory(null, 45, "Friend Management");

        inv.setItem(0, editItem(new ItemStack(Material.BOOK_AND_QUILL), ChatColor.GREEN + "Add Friend", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to add friend!"
        }));

        inv.setItem(1, editItem(new ItemStack(Material.CHEST), ChatColor.GREEN + "View Friend", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to view friends!"
        }));

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");

        int slot = 9;
        for (String s : friendRequest) {
            String from = s.split(",")[0];
            String name = API.getNameFromUUID(from);

            long unix = Long.valueOf(s.split(",")[1]);
            Date sentDate = new Date(unix * 1000);
            String date = sdf.format(sentDate);

            ItemStack stack = editItem(name, name, new String[]{
                    ChatColor.GRAY + "Sent: " + date,
                    "",
                    ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to accept!",
                    ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Right-Click " + ChatColor.GRAY + "to deny!"
            });

            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("info", new NBTTagString(s));
            nmsStack.setTag(tag);

            inv.setItem(slot, CraftItemStack.asBukkitCopy(nmsStack));

            if (slot >= 44) break;
            slot++;
        }

        player.openInventory(inv);

    }

    public static void openMailInventory(Player player) {
        UUID uuid = player.getUniqueId();
        ArrayList<String> mail = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MAILBOX, uuid);

        Inventory inv = Bukkit.createInventory(null, 45, "Mailbox");
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");

        inv.setItem(0, editItem(new ItemStack(Material.BOOK_AND_QUILL), ChatColor.GREEN + "Send Mail", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to send mail!"
        }));
        inv.setItem(8, editItem(new ItemStack(Material.EMERALD), ChatColor.GREEN + "Tax", new String[]{
                ChatColor.GRAY + "Tax: " + ChatColor.AQUA + "1 GEM"
        }));

        int slot = 9;
        for (String s : mail) {
            String from = s.split(",")[0];
            long unix = Long.valueOf(s.split(",")[1]);
            String serializedItem = s.split(",")[2];
            Date sentDate = new Date(unix * 1000);
            String loginTime = sdf.format(sentDate);

            ItemStack mailTemplateItem = MailHandler.getInstance().setItemAsMail(editItem(new ItemStack(Material.CHEST), ChatColor.GREEN + "Mail Item", new String[]{
                    ChatColor.GRAY + "From: " + ChatColor.AQUA + from,
                    ChatColor.GRAY + "Sent: " + ChatColor.AQUA + sentDate,
                    "",
                    ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to open!"
            }), s);

            inv.setItem(slot, mailTemplateItem);
            if (slot >= 44) break;
            slot++;
        }

        player.openInventory(inv);

    }

    public static void openGuildManagement(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, "Guild Management");
        UUID uuid = player.getUniqueId();
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumGuildData.NAME, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        List<String> officers = (List<String>) DatabaseAPI.getInstance().getData(EnumGuildData.OFFICERS, guildName);
        List<String> members = (List<String>) DatabaseAPI.getInstance().getData(EnumGuildData.MEMBERS, guildName);

        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));


        inv.setItem(8, editItem(new ItemStack(Material.BEDROCK), ChatColor.RED + "Delete Guild", new String[]{
                ChatColor.RED + "This action CANNOT be undone!",
                "",
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to purge guild!",
                "",
                ChatColor.RED + "You must be rank " + ChatColor.GREEN + "Owner" + ChatColor.RED + "!",
        }));

        inv.setItem(10, editItem("Nemanja011sl", ChatColor.GREEN + "Invite a player", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to add a player!",
                "",
                ChatColor.RED + "You must be rank " + ChatColor.GREEN + "Officer" + ChatColor.RED + "!",
        }));

        inv.setItem(11, editItem("rarest_of_pepes", ChatColor.GREEN + "Remove a player", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to remove a player!",
                "",
                ChatColor.RED + "You must be rank " + ChatColor.GREEN + "Officer" + ChatColor.RED + "!",
        }));

        inv.setItem(13, editItem("TeaZ", ChatColor.GREEN + "Promote a player", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to promote a player!",
                "",
                ChatColor.RED + "You must be ranked >=" + ChatColor.GREEN + "CoOwner" + ChatColor.RED + "!",
        }));

        inv.setItem(14, editItem("Arcaniax", ChatColor.GREEN + "Demote a player", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to demote a player!",
                "",
                ChatColor.RED + "You must be ranked >=" + ChatColor.GREEN + "CoOwner" + ChatColor.RED + "!",
        }));

        inv.setItem(36, editItem(new ItemStack(Material.WOOL, 1, (short) 5), ChatColor.GREEN + "Pick a Guild Icon!", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to edit the Guild Icon!",
                "",
                ChatColor.RED + "You must be ranked " + ChatColor.AQUA + "Owner" + ChatColor.RED + "!"
        }));

        player.openInventory(inv);
    }

    public static void openGuildMembers(Player player) {
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumGuildData.NAME, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()));
        String clanTag = (String) DatabaseAPI.getInstance().getData(EnumGuildData.CLAN_TAG, guildName);
        List<String> members = (List<String>) DatabaseAPI.getInstance().getData(EnumGuildData.MEMBERS, guildName);


        if (members.size() >= 50) {
            Inventory tooMuch = Bukkit.createInventory(null, 0, ChatColor.RED + "Too many members to list!");
            player.openInventory(tooMuch);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, "Guild - " + ChatColor.translateAlternateColorCodes('&', clanTag) + ChatColor.RESET + " - Members");

        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        for (String s : members) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                String name = API.getNameFromUUID(s);
                inv.addItem(editItem(name, ChatColor.GREEN + "Member " + name, new String[]{}));
            }, 0l);
        }

        player.openInventory(inv);
    }

    public static void openGuildOfficers(Player player) {
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumGuildData.NAME, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()));
        String clanTag = (String) DatabaseAPI.getInstance().getData(EnumGuildData.CLAN_TAG, guildName);
        List<String> members = (List<String>) DatabaseAPI.getInstance().getData(EnumGuildData.OFFICERS, guildName);


        if (members.size() >= 50) {
            Inventory tooMuch = Bukkit.createInventory(null, 0, ChatColor.RED + "Too many officers to list!");
            player.openInventory(tooMuch);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, "Guild - " + ChatColor.translateAlternateColorCodes('&', clanTag) + ChatColor.RESET + " - Officers");

        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        for (String s : members) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> inv.addItem(editItem(API.getNameFromUUID(s), ChatColor.GREEN + "Officer " + API.getNameFromUUID(s), new String[]{})), 0l);
        }

        player.openInventory(inv);
    }

    public static void openGuildRankingBoard(Player player) {

        Inventory inv = Bukkit.createInventory(null, 18, "Top Guilds");

        inv.addItem(editItem("Mapparere", ChatColor.GREEN + "Top Ranked Guilds", new String[]{
                ChatColor.GRAY + "Filter:" + ChatColor.AQUA + " Level"
        }));

        Block<Document> printDocumentBlock = document -> {
            Object info = document.get("info");

            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () ->
            {
                Date creationDate = new Date(Long.valueOf(String.valueOf(((Document) info).get("unixCreation"))) * 1000);
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");
                String date = sdf.format(creationDate);

                Material m = Material.valueOf(((String) ((Document) info).get("icon")));

                inv.addItem(editItem(new ItemStack(m), ChatColor.GREEN + String.valueOf(((Document) info).get("name")), new String[]{
                        ChatColor.GRAY + "ClanTag: " + ChatColor.translateAlternateColorCodes('&', String.valueOf(((Document) info).get("clanTag"))),
                        ChatColor.GRAY + "Level: " + ChatColor.AQUA + String.valueOf(((Document) info).get("netLevel")),
                        ChatColor.GRAY + "Officers: " + ChatColor.AQUA + ((ArrayList<String>) ((Document) info).get("officers")).size(),
                        ChatColor.GRAY + "Members: " + ChatColor.AQUA + ((ArrayList<String>) ((Document) info).get("members")).size(),
                        ChatColor.GRAY + "Created: " + ChatColor.AQUA + date,
                }));

            }, 0l);
        };
        SingleResultCallback<Void> callbackWhenFinished = (result, t) -> Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.openInventory(inv), 1l);

        Database.guilds.find(Filters.exists("info.netLevel")).sort(Sorts.descending("info.netLevel")).limit(16).forEach(printDocumentBlock, callbackWhenFinished);
    }

    public static void openPlayerGuildInventory(Player player) {
        UUID uuid = player.getUniqueId();

        String owner = (String) DatabaseAPI.getInstance().getData(EnumGuildData.OWNER, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumGuildData.NAME, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        String clanTag = (String) DatabaseAPI.getInstance().getData(EnumGuildData.CLAN_TAG, guildName);
        Integer netLevel = (Integer) DatabaseAPI.getInstance().getData(EnumGuildData.LEVEL, guildName);

        Inventory inv = Bukkit.createInventory(null, 54, "Guild - " + ChatColor.translateAlternateColorCodes('&', clanTag));

        inv.setItem(0, editItem("MHF_WSkeleton ", ChatColor.GREEN + "Guild Management", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click" + ChatColor.GRAY + " for guild Management.",
                "",
                ChatColor.RED + "You must " + ChatColor.GREEN + "Officer " + ChatColor.RED + "or higher!"
        }));

        inv.setItem(4, editItem(new ItemStack(Material.EMERALD), ChatColor.GREEN + "Guild Name" + ChatColor.GRAY + ": " + ChatColor.RESET + guildName, new String[]{
                ChatColor.GRAY + "ClanTag: " + ChatColor.translateAlternateColorCodes('&', clanTag),
                ChatColor.GRAY + "Guild Master: " + ChatColor.AQUA + Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName(),
                ChatColor.GRAY + "Level: " + ChatColor.AQUA + netLevel
        }));

        inv.setItem(8, editItem(new ItemStack(Material.DIAMOND_SWORD), ChatColor.RED + "Guild Wars", new String[]{
                ChatColor.RED + "This feature is upcoming!",
        }));
        inv.setItem(17, editItem("Seska_Rotan", ChatColor.GREEN + "Guild Ranking", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click" + ChatColor.GRAY + " to view highest Guilds!",
        }));

        inv.setItem(53, editItem("CruXXx", ChatColor.GREEN + "Guild Spoils", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click" + ChatColor.GRAY + " to claim Guild Spoils!",
        }));

        inv.setItem(18, editItem("Turmfalke2000", ChatColor.GREEN + "Guild Officers", new String[]{}));
        inv.setItem(27, editItem("Miner", ChatColor.GREEN + "Guild Members", new String[]{}));

        player.openInventory(inv);

    }

    public static void openPlayerPetMenu(Player player) {
        UUID uuid = player.getUniqueId();

        List<String> playerPets = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.PETS, uuid);

        if (playerPets.size() <= 0) {
            Inventory noPets = Bukkit.createInventory(null, 0, ChatColor.RED + "You have no Pets!");
            player.openInventory(noPets);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, "Pet Selection");
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));
        inv.setItem(26, editItem(new ItemStack(Material.LEASH), ChatColor.GREEN + "Dismiss Pet", new String[]{}));

        for (String pet : playerPets) {
            String petType;
            String particleType = "";
            String petName = "";
            if (pet.contains("-")) {
                petType = pet.split("-")[0];
                particleType = pet.split("-")[1];
                petName = ParticleAPI.ParticleEffect.getChatColorByName(particleType) + particleType + " " + ChatColor.GREEN + petType.toUpperCase();
            } else {
                petType = pet;
                petName = ChatColor.GREEN + petType.toUpperCase();
            }
            ItemStack itemStack = new ItemStack(Material.MONSTER_EGG, 1, (short) EnumPets.getByName(petType).getEggShortData());
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("petType", new NBTTagString(petType));
            if (!particleType.equalsIgnoreCase("")) {
                tag.set("particleType", new NBTTagString(particleType));
            }
            nmsStack.setTag(tag);
            inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStack), petName, new String[]{
            }));
        }

        player.openInventory(inv);
    }

    public static void openPlayerMountMenu(Player player) {
        UUID uuid = player.getUniqueId();

        List<String> playerMounts = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MOUNTS, uuid);

        if (playerMounts.size() <= 0) {
            Inventory noMounts = Bukkit.createInventory(null, 0, ChatColor.RED + "You have no Mounts!");
            player.openInventory(noMounts);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, "Mount Selection");
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));
        inv.setItem(26, editItem(new ItemStack(Material.LEASH), ChatColor.GREEN + "Dismiss Mount", new String[]{}));

        for (String mountType : playerMounts) {
            ItemStack itemStack = EnumMounts.getByName(mountType).getSelectionItem();
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("mountType", new NBTTagString(mountType));
            nmsStack.setTag(tag);
            inv.addItem(editItemWithShort(CraftItemStack.asBukkitCopy(nmsStack), EnumMounts.getByName(mountType).getShortID(), ChatColor.GREEN + mountType.toUpperCase(), new String[]{
            }));
        }

        player.openInventory(inv);
    }

    public static void openPlayerParticleMenu(Player player) {
        UUID uuid = player.getUniqueId();

        List<String> playerTrails = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.PARTICLES, uuid);

        if (playerTrails == null || playerTrails.size() <= 0) {
            Inventory noTrails = Bukkit.createInventory(null, 0, ChatColor.RED + "You have no Player Trails!");
            player.openInventory(noTrails);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, "Player Trail Selection");
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));
        inv.setItem(26, editItem(new ItemStack(Material.ARMOR_STAND), ChatColor.GREEN + "Turn off Trail", new String[]{}));

        for (String trailType : playerTrails) {
            ItemStack itemStack = ParticleAPI.ParticleEffect.getByName(trailType).getSelectionItem();
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("playerTrailType", new NBTTagString(trailType));
            nmsStack.setTag(tag);
            inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStack), ChatColor.GREEN + trailType.toUpperCase(), new String[]{
            }));
        }

        player.openInventory(inv);
    }

    public static void openPlayerProfileMenu(Player player) {

        Inventory inv = Bukkit.createInventory(null, 27, "Profile");
        inv.setItem(0, editItem(new ItemStack(Material.EXP_BOTTLE), ChatColor.GREEN + "Attributes", new String[]{
                ChatColor.DARK_GRAY + "Player Attributes",
                "",
                ChatColor.GRAY + "As you play throughout Dungeon Realms,",
                ChatColor.GRAY + "your player will acquire attribute points.",
                ChatColor.GRAY + "With Attribute points you can improve",
                ChatColor.GRAY + "several of many individual character",
                ChatColor.GRAY + "skills!",
                "",
                ChatColor.YELLOW + "Click to view Player Attributes!"
        }));
        inv.setItem(1, editItem("xFinityPro", ChatColor.GREEN + "Friends", new String[]{
                ChatColor.DARK_GRAY + "Player Friends",
                "",
                ChatColor.GRAY + "Its okay if you're alone!",
                "",
                ChatColor.YELLOW + "Click to open Friend Management!"
        }));
        inv.setItem(4, editItem(Utils.getPlayerHead(player), ChatColor.GREEN + "Player Profile", new String[]{
        }));
        inv.setItem(6, editItem(new ItemStack(Material.EYE_OF_ENDER), ChatColor.GREEN + "Player Trails", new String[]{
                ChatColor.DARK_GRAY + "Player Trails",
                "",
                ChatColor.GRAY + "Want to be the envy of your friends?",
                ChatColor.GRAY + "Get yourself a Particle Trail!",
                "",
                ChatColor.YELLOW + "Click to view Player Trails!"
        }));
        inv.setItem(7, editItem(new ItemStack(Material.SADDLE), ChatColor.GREEN + "Player Mounts", new String[]{
                ChatColor.DARK_GRAY + "Player Mounts",
                "",
                ChatColor.GRAY + "Want to travel in style?",
                ChatColor.GRAY + "Mounts are the solution!",
                "",
                ChatColor.YELLOW + "Click to view Player Mounts!"
        }));
        inv.setItem(8, editItem(new ItemStack(Material.NAME_TAG), ChatColor.GREEN + "Player Pets", new String[]{
                ChatColor.DARK_GRAY + "Player Pets",
                "",
                ChatColor.GRAY + "Want a friendly companion",
                ChatColor.GRAY + "to bring along on your",
                ChatColor.GRAY + "adventures? Pets are the",
                ChatColor.GRAY + "solution!",
                "",
                ChatColor.YELLOW + "Click to view Player Pets!"
        }));
        inv.setItem(18, editItem(new ItemStack(Material.EMERALD), ChatColor.GREEN + "Donations", new String[]{
                ChatColor.DARK_GRAY + "Micro Transactions",
                "",
                ChatColor.GRAY + "Want to access to more awesomeness?",
                ChatColor.GRAY + "Consider donating to support Dungeon Realms",
                ChatColor.GRAY + "in return you'll receive several in-game",
                ChatColor.GRAY + "perks!",
                "",
                ChatColor.YELLOW + "Click to view Micro Transactions!"
        }));
        inv.setItem(22, editItem(new ItemStack(Material.QUARTZ), ChatColor.GREEN + "Hearthstone", new String[]{
                ChatColor.DARK_GRAY + "Player Hearthstone",
                "",
                ChatColor.GRAY + "Teleport home to your saved location of",
                ChatColor.YELLOW + TeleportAPI.getLocationFromDatabase(player.getUniqueId()),
                "",
                ChatColor.YELLOW + "Click to use your Hearhtstone!"
        }));
        inv.setItem(26, editItem(new ItemStack(Material.REDSTONE_COMPARATOR), ChatColor.GREEN + "Settings & Preferences", new String[]{
                ChatColor.DARK_GRAY + "Settings & Preferences",
                "",
                ChatColor.GRAY + "Help us help you! By adjusting",
                ChatColor.GRAY + "your player settings & preferences",
                ChatColor.GRAY + "we can optimize your gaming experience",
                ChatColor.GRAY + "on this RPG!",
                "",
                ChatColor.YELLOW + "Click to view Settings & Preferences!"
        }));

        player.openInventory(inv);
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

	/**
	 * @param player
	 */
	public static void openToggleMenu(Player player) {
		Inventory inv = Bukkit.createInventory(null, 9, "Toggles");
		boolean toggle1 = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_CHAOTIC_PREVENTION, player.getUniqueId());
		boolean toggle2 = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId());
		boolean toggle3 = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DUEL, player.getUniqueId());
		boolean toggle4 = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, player.getUniqueId());
		boolean toggle5 = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_PVP, player.getUniqueId());
		boolean toggle6 = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_RECEIVE_MESSAGE, player.getUniqueId());
		boolean toggle7 = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_TRADE, player.getUniqueId());
		boolean toggle8 = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_TRADE_CHAT, player.getUniqueId());
		ItemStack stack1 = null;
		ItemStack stack2 = null;
		ItemStack stack3 = null;
		ItemStack stack4 = null;
		ItemStack stack5 = null;
		ItemStack stack6 = null;
		ItemStack stack7 = null;
		ItemStack stack8 = null;
		
		if(toggle1)
			stack1 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), "Toggle CHAOTIC PREVENTION", new String[]{(ChatColor.YELLOW + "Currently ON")}).build();
		else
			stack1 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.SILVER.getData()), "Toggle CHAOTIC PREVENTION", new String[]{(ChatColor.YELLOW + "Currently OFF")}).build();
		
		if(toggle2)
			stack2 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), "Toggle DEBUG", new String[]{(ChatColor.YELLOW + "Currently ON")}).build();
		else
			stack2 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.SILVER.getData()), "Toggle DEBUG", new String[]{(ChatColor.YELLOW + "Currently OFF")}).build();
		
		if(toggle3)
			stack3 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), "Toggle DUEL", new String[]{(ChatColor.YELLOW + "Currently ON")}).build();
		else
			stack3 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.SILVER.getData()), "Toggle DUEL", new String[]{(ChatColor.YELLOW + "Currently OFF")}).build();
		
		if(toggle4)
			stack4 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), "Toggle GLOBAL CHAT", new String[]{(ChatColor.YELLOW + "Currently ON")}).build();
		else
			stack4 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.SILVER.getData()), "Toggle GLOBAL CHAT", new String[]{(ChatColor.YELLOW + "Currently OFF")}).build();
		
		if(toggle5)
			stack5 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), "Toggle PVP", new String[]{(ChatColor.YELLOW + "Currently ON")}).build();
		else
			stack5 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.SILVER.getData()), "Toggle PVP", new String[]{(ChatColor.YELLOW + "Currently OFF")}).build();
		
		if(toggle6)
			stack6 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), "Toggle RECEIVE MESSAGE", new String[]{(ChatColor.YELLOW + "Currently ON")}).build();
		else
			stack6 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.SILVER.getData()), "Toggle RECEIVE MESSAGE", new String[]{(ChatColor.YELLOW + "Currently OFF")}).build();
		
		if(toggle7)
			stack7 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), "Toggle TRADE", new String[]{(ChatColor.YELLOW + "Currently ON")}).build();
		else
			stack7 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.SILVER.getData()), "Toggle TRADE", new String[]{(ChatColor.YELLOW + "Currently OFF")}).build();
		
		if(toggle8)
			stack8 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), "Toggle TRADE CHAT", new String[]{(ChatColor.YELLOW + "Currently ON")}).build();
		else
			stack8 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.SILVER.getData()), "Toggle TRADE CHAT", new String[]{(ChatColor.YELLOW + "Currently OFF")}).build();

		ItemStack back = ItemManager.createItem(Material.BARRIER, ChatColor.YELLOW + "Back", new String[]{ChatColor.AQUA + "Back to the Profile Menu!"});
		
		inv.setItem(0, stack1);
		inv.setItem(1, stack2);
		inv.setItem(2, stack3);
		inv.setItem(3, stack4);
		inv.setItem(4, stack5);
		inv.setItem(5, stack6);
		inv.setItem(6, stack7);
		inv.setItem(7, stack8);
		inv.setItem(8, back);
		player.openInventory(inv);
	}

}
