package net.dungeonrealms.inventory;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mastery.GamePlayer;
import net.dungeonrealms.mechanics.ItemManager;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.minebone.anvilapi.core.AnvilApi;
import com.minebone.anvilapi.nms.anvil.AnvilGUIInterface;
import com.minebone.anvilapi.nms.anvil.AnvilSlot;

import java.util.Arrays;

/**
 * Created by Kieran on 10/26/2015.
 */
public class NPCMenus {

    public static void openMountPurchaseMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Mount Vendor");
        ItemStack t1Horse = new ItemStack(Material.SADDLE);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackT1 = CraftItemStack.asNMSCopy(t1Horse);
        NBTTagCompound tagT1 = nmsStackT1.getTag() == null ? new NBTTagCompound() : nmsStackT1.getTag();
        tagT1.set("mountType", new NBTTagString("T1HORSE"));
        tagT1.setInt("mountCost", 5000);
        nmsStackT1.setTag(tagT1);
        ItemStack diamondHorse = new ItemStack(Material.DIAMOND_BARDING);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackDiamond = CraftItemStack.asNMSCopy(diamondHorse);
        NBTTagCompound tagDiamond = nmsStackDiamond.getTag() == null ? new NBTTagCompound() : nmsStackDiamond.getTag();
        tagDiamond.set("mountType", new NBTTagString("DIAMONDHORSE"));
        tagDiamond.setInt("mountCost", 12500);
        nmsStackDiamond.setTag(tagDiamond);
        ItemStack goldHorse = new ItemStack(Material.GOLD_BARDING);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackGold = CraftItemStack.asNMSCopy(goldHorse);
        NBTTagCompound tagGold = nmsStackGold.getTag() == null ? new NBTTagCompound() : nmsStackGold.getTag();
        tagGold.set("mountType", new NBTTagString("GOLDHORSE"));
        tagGold.setInt("mountCost", 25000);
        nmsStackGold.setTag(tagGold);

        inv.setItem(0, editItem(CraftItemStack.asBukkitCopy(nmsStackT1), (short) 0, ChatColor.GREEN + "Old Horse Mount", new String[]{
                ChatColor.RED + "Speed 120%",
                ChatColor.AQUA + "5000 Gems"}));
        inv.setItem(1, editItem(CraftItemStack.asBukkitCopy(nmsStackDiamond), (short) 0, ChatColor.GREEN + "Traveler's Horse Mount", new String[]{
                ChatColor.RED + "Speed 140%",
                ChatColor.RED + "Jump 110%",
                ChatColor.AQUA + "12500 Gems"}));
        inv.setItem(2, editItem(CraftItemStack.asBukkitCopy(nmsStackGold), (short) 0, ChatColor.GREEN + "Knight's Horse Mount", new String[]{
                ChatColor.RED + "Speed 160%",
                ChatColor.RED + "Jump 110%",
                ChatColor.AQUA + "25000 Gems"}));
        //TODO: Add Mule when Chase finishes it.

        player.openInventory(inv);
    }

    public static void openProfessionPurchaseMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Profession Vendor");
        ItemStack pickAxe = ItemManager.createPickaxe(1);
        ItemStack fishingRod = ItemManager.createFishingPole(1);
		String expBar = "||||||||||||||||||||" + "||||||||||||||||||||" + "||||||||||";
    	inv.addItem(editItem(pickAxe, (short) 0, pickAxe.getItemMeta().getDisplayName(), new String[]{expBar, ChatColor.AQUA + "100 Gems"}));
    	inv.addItem(editItem(fishingRod, (short) 0, fishingRod.getItemMeta().getDisplayName(), new String[]{expBar, ChatColor.AQUA + "100 Gems"}));

        player.openInventory(inv);
    }
    
    public static void openWizardMenu(Player player){
            	GamePlayer gp = API.getGamePlayer(player);
            	if(gp.getLevel() >= 10){
            		if(gp.getStats().resetAmounts > 0){
            			player.sendMessage(ChatColor.GREEN + "You have a free stat reset available!");
            			AnvilGUIInterface gui = AnvilApi.createNewGUI(player, e -> {
    						if (e.getSlot() == AnvilSlot.OUTPUT) {
    							if(e.getName().equalsIgnoreCase("Yes") || e.getName().equalsIgnoreCase("y")){
    								gp.getStats().freeResets -= 1;
    							}else{
    								e.destroy();
    							}
    						}
    					});
    					ItemStack stack = new ItemStack(Material.INK_SACK, 1, DyeColor.GREEN.getDyeData());
    					ItemMeta meta = stack.getItemMeta();
    					meta.setDisplayName("Use your ONE stat points reset?");
    					stack.setItemMeta(meta);
    					gui.setSlot(AnvilSlot.INPUT_LEFT, stack);
    					Bukkit.getScheduler().scheduleAsyncRepeatingTask(DungeonRealms.getInstance(), () -> {
    						player.sendMessage("Opening stat reset confirmation");
    					}, 0, 20 * 3);
    					Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
    					gui.open();
    					}, 20 * 5);
            		}else{
            		player.sendMessage(ChatColor.RED + "You have already used your free stat reset for your character.");
            		player.sendMessage(ChatColor.YELLOW + "You may purchase more resets from the E-Cash vendor!.");
            		}
            	}else{
            		player.sendMessage(ChatColor.RED + "You need to be level 10 to use your ONE reset.");
            	}
            	
            }

    public static void openECashPurchaseMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "E-Cash Vendor");

        ItemStack flameTrail = new ItemStack(Material.BLAZE_POWDER);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackFlameTrail = CraftItemStack.asNMSCopy(flameTrail);
        NBTTagCompound tagFlameTrail = nmsStackFlameTrail.getTag() == null ? new NBTTagCompound() : nmsStackFlameTrail.getTag();
        tagFlameTrail.set("playerTrailType", new NBTTagString("FLAME"));
        tagFlameTrail.setInt("ecashCost", 649);
        nmsStackFlameTrail.setTag(tagFlameTrail);
        ItemStack musicTrail = new ItemStack(Material.GOLD_RECORD);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackMusicTrail = CraftItemStack.asNMSCopy(musicTrail);
        NBTTagCompound tagMusicTrail = nmsStackMusicTrail.getTag() == null ? new NBTTagCompound() : nmsStackMusicTrail.getTag();
        tagMusicTrail.set("playerTrailType", new NBTTagString("NOTE"));
        tagMusicTrail.setInt("ecashCost", 649);
        nmsStackMusicTrail.setTag(tagMusicTrail);
        ItemStack cloudTrail = new ItemStack(Material.BEACON);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackCloudTrail = CraftItemStack.asNMSCopy(cloudTrail);
        NBTTagCompound tagCloudTrail = nmsStackCloudTrail.getTag() == null ? new NBTTagCompound() : nmsStackCloudTrail.getTag();
        tagCloudTrail.set("playerTrailType", new NBTTagString("CLOUD"));
        tagCloudTrail.setInt("ecashCost", 649);
        nmsStackCloudTrail.setTag(tagCloudTrail);
        ItemStack cursedTrail = new ItemStack(Material.POTION);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackCursedTrail = CraftItemStack.asNMSCopy(cursedTrail);
        NBTTagCompound tagCursedTrail = nmsStackCursedTrail.getTag() == null ? new NBTTagCompound() : nmsStackCursedTrail.getTag();
        tagCursedTrail.set("playerTrailType", new NBTTagString("WITCHMAGIC"));
        tagCursedTrail.setInt("ecashCost", 649);
        nmsStackCursedTrail.setTag(tagCursedTrail);
        ItemStack zombieHorse = new ItemStack(Material.SKULL_ITEM, 1, (short) 2);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackZombieHorse = CraftItemStack.asNMSCopy(zombieHorse);
        NBTTagCompound tagZombieHorse = nmsStackZombieHorse.getTag() == null ? new NBTTagCompound() : nmsStackZombieHorse.getTag();
        tagZombieHorse.set("mountType", new NBTTagString("ZOMBIEHORSE"));
        tagZombieHorse.setInt("ecashCost", 399);
        nmsStackZombieHorse.setTag(tagZombieHorse);
        ItemStack skeletonHorse = new ItemStack(Material.SKULL_ITEM, 1, (short) 0);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackSkeletonHorse = CraftItemStack.asNMSCopy(skeletonHorse);
        NBTTagCompound tagSkeletonHorse = nmsStackSkeletonHorse.getTag() == null ? new NBTTagCompound() : nmsStackSkeletonHorse.getTag();
        tagSkeletonHorse.set("mountType", new NBTTagString("SKELETONHORSE"));
        tagSkeletonHorse.setInt("ecashCost", 399);
        nmsStackSkeletonHorse.setTag(tagSkeletonHorse);
        ItemStack rabbitPet = new ItemStack(Material.MONSTER_EGG, 1, (short) 101);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackRabbitPet = CraftItemStack.asNMSCopy(rabbitPet);
        NBTTagCompound tagRabbitPet = nmsStackRabbitPet.getTag() == null ? new NBTTagCompound() : nmsStackRabbitPet.getTag();
        tagRabbitPet.set("petType", new NBTTagString("RABBIT"));
        tagRabbitPet.setInt("ecashCost", 749);
        nmsStackRabbitPet.setTag(tagRabbitPet);
        ItemStack ocelotPet = new ItemStack(Material.MONSTER_EGG, 1, (short) 98);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackOcelotPet = CraftItemStack.asNMSCopy(ocelotPet);
        NBTTagCompound tagOcelotPet = nmsStackOcelotPet.getTag() == null ? new NBTTagCompound() : nmsStackOcelotPet.getTag();
        tagOcelotPet.set("petType", new NBTTagString("OCELOT"));
        tagOcelotPet.setInt("ecashCost", 749);
        nmsStackOcelotPet.setTag(tagOcelotPet);

        inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStackFlameTrail), (short) 0, ChatColor.RED + "Flame Trail", new String[]{
                ChatColor.AQUA + "649 E-Cash"}));
        inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStackMusicTrail), (short) 0, ChatColor.LIGHT_PURPLE + "Musical Trail", new String[]{
                ChatColor.AQUA + "649 E-Cash"}));
        inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStackCloudTrail), (short) 0, ChatColor.WHITE + "Cloud Trail", new String[]{
                ChatColor.AQUA + "649 E-Cash"}));
        inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStackCursedTrail), (short) 0, ChatColor.DARK_PURPLE + "Cursed Trail", new String[]{
                ChatColor.AQUA + "649 E-Cash"}));
        inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStackZombieHorse), (short) 2, ChatColor.GREEN + "Zombie Horse", new String[]{
                ChatColor.RED + "Speed 160%",
                ChatColor.RED + "Jump 110%",
                ChatColor.AQUA + "399 E-Cash"}));
        inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStackSkeletonHorse), (short) 0, ChatColor.GRAY + "Skeleton Horse", new String[]{
                ChatColor.RED + "Speed 160%",
                ChatColor.RED + "Jump 110%",
                ChatColor.AQUA + "399 E-Cash"}));
        inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStackRabbitPet), (short) 101, ChatColor.YELLOW + "Rabbit Pet", new String[]{
                ChatColor.AQUA + "749 E-Cash"}));
        inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStackOcelotPet), (short) 98, ChatColor.YELLOW + "Ocelot Pet", new String[]{
                ChatColor.AQUA + "749 E-Cash"}));

        player.openInventory(inv);
    }

    public static void openHearthstoneRelocateMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Hearthstone Re-Location");

        ItemStack cyrennica = new ItemStack(Material.BEACON);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackCyrennica = CraftItemStack.asNMSCopy(cyrennica);
        NBTTagCompound tagCyrennica = nmsStackCyrennica.getTag() == null ? new NBTTagCompound() : nmsStackCyrennica.getTag();
        tagCyrennica.set("hearthstoneLocation", new NBTTagString("CYRENNICA"));
        tagCyrennica.setInt("gemCost", 1000);
        nmsStackCyrennica.setTag(tagCyrennica);
        ItemStack harrisons = new ItemStack(Material.BEACON);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackHarrisons = CraftItemStack.asNMSCopy(harrisons);
        NBTTagCompound tagHarrisons = nmsStackHarrisons.getTag() == null ? new NBTTagCompound() : nmsStackHarrisons.getTag();
        tagHarrisons.set("hearthstoneLocation", new NBTTagString("HARRISON_FIELD"));
        tagHarrisons.setInt("gemCost", 1500);
        nmsStackHarrisons.setTag(tagHarrisons);
        ItemStack darkOak = new ItemStack(Material.BEACON);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackDarkOak = CraftItemStack.asNMSCopy(darkOak);
        NBTTagCompound tagDarkOak = nmsStackDarkOak.getTag() == null ? new NBTTagCompound() : nmsStackDarkOak.getTag();
        tagDarkOak.set("hearthstoneLocation", new NBTTagString("DARK_OAK"));
        tagDarkOak.setInt("gemCost", 3500);
        nmsStackDarkOak.setTag(tagDarkOak);
        ItemStack tripoli = new ItemStack(Material.BEACON);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackTripoli = CraftItemStack.asNMSCopy(tripoli);
        NBTTagCompound tagTripoli = nmsStackTripoli.getTag() == null ? new NBTTagCompound() : nmsStackCyrennica.getTag();
        tagTripoli.set("hearthstoneLocation", new NBTTagString("TRIPOLI"));
        tagTripoli.setInt("gemCost", 7500);
        nmsStackTripoli.setTag(tagTripoli);
        ItemStack gloomyHollows = new ItemStack(Material.BEACON);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackGloomyHollows = CraftItemStack.asNMSCopy(gloomyHollows);
        NBTTagCompound tagGloomyHollows = nmsStackGloomyHollows.getTag() == null ? new NBTTagCompound() : nmsStackGloomyHollows.getTag();
        tagGloomyHollows.set("hearthstoneLocation", new NBTTagString("GLOOMY_HOLLOWS"));
        tagGloomyHollows.setInt("gemCost", 3500);
        nmsStackGloomyHollows.setTag(tagGloomyHollows);
        ItemStack crestguardKeep = new ItemStack(Material.BEACON);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackCrestguardKeep = CraftItemStack.asNMSCopy(crestguardKeep);
        NBTTagCompound tagCrestguardKeep = nmsStackCrestguardKeep.getTag() == null ? new NBTTagCompound() : nmsStackCrestguardKeep.getTag();
        tagCrestguardKeep.set("hearthstoneLocation", new NBTTagString("CRESTGUARD"));
        tagCrestguardKeep.setInt("gemCost", 15000);
        nmsStackCrestguardKeep.setTag(tagCrestguardKeep);
        ItemStack trollsbaneTavern = new ItemStack(Material.BEACON);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackTrollsbaneTavern = CraftItemStack.asNMSCopy(trollsbaneTavern);
        NBTTagCompound tagTrollsbaneTavern = nmsStackTrollsbaneTavern.getTag() == null ? new NBTTagCompound() : nmsStackTrollsbaneTavern.getTag();
        tagTrollsbaneTavern.set("hearthstoneLocation", new NBTTagString("TROLLSBANE"));
        tagTrollsbaneTavern.setInt("gemCost", 7500);
        nmsStackTrollsbaneTavern.setTag(tagTrollsbaneTavern);
        ItemStack deadpeaksMountain = new ItemStack(Material.BEACON);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackDeadpeaksMountain = CraftItemStack.asNMSCopy(deadpeaksMountain);
        NBTTagCompound tagDeadpeaksMountain = nmsStackDeadpeaksMountain.getTag() == null ? new NBTTagCompound() : nmsStackDeadpeaksMountain.getTag();
        tagDeadpeaksMountain.set("hearthstoneLocation", new NBTTagString("DEADPEAKS"));
        tagDeadpeaksMountain.setInt("gemCost", 25000);
        nmsStackDeadpeaksMountain.setTag(tagDeadpeaksMountain);

        inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStackCyrennica), (short) 0, ChatColor.WHITE + "Cyrennica", new String[]{
                ChatColor.AQUA + "1000 Gems"}));
        inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStackHarrisons), (short) 0, ChatColor.WHITE + "Harrison Fields", new String[]{
                ChatColor.AQUA + "1500 Gems"}));
        inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStackGloomyHollows), (short) 0, ChatColor.WHITE + "Gloomy Hollows", new String[]{
                ChatColor.AQUA + "3500 Gems"}));
        inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStackDarkOak), (short) 0, ChatColor.WHITE + "Dark Oak Tavern", new String[]{
                ChatColor.AQUA + "3500 Gems"}));
        inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStackTripoli), (short) 0, ChatColor.WHITE + "Tripoli", new String[]{
                ChatColor.AQUA + "7500 Gems"}));
        inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStackTrollsbaneTavern), (short) 0, ChatColor.WHITE + "Trollsbane Tavern", new String[]{
                ChatColor.AQUA + "7500 Gems"}));
        inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStackCrestguardKeep), (short) 0, ChatColor.WHITE + "Crestguard Keep", new String[]{
                ChatColor.AQUA + "15000 Gems"}));
        inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStackDeadpeaksMountain), (short) 0, ChatColor.WHITE + "Deadpeaks Mountain", new String[]{
                ChatColor.AQUA + "25000 Gems"}));

        player.openInventory(inv);
    }

    public static ItemStack editItem(ItemStack itemStack, short shortDataID, String name, String[] lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        itemStack.setDurability(shortDataID);
        itemStack.setItemMeta(meta);
        itemStack.setAmount(1);
        return itemStack;
    }
}
