package net.dungeonrealms.commands;

import net.dungeonrealms.API;
import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.donate.DonationEffects;
import net.dungeonrealms.guild.Guild;
import net.dungeonrealms.items.EnumItem;
import net.dungeonrealms.items.Item;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.items.armor.ArmorGenerator;
import net.dungeonrealms.items.repairing.RepairAPI;
import net.dungeonrealms.mastery.RealmManager;
import net.dungeonrealms.mechanics.ItemManager;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.dungeonrealms.world.glyph.Glyph;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Created by Nick on 9/17/2015.
 */
public class CommandAdd extends BasicCommand {

    public CommandAdd(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player player = (Player) s;
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "[WARNING] " + ChatColor.YELLOW + "You do not have permissions for this!");
            return false;
        }
        if (args.length > 0) {
            switch (args[0]) {
                case "uuid":
                    player.sendMessage(Bukkit.getPlayer(API.getUUIDFromName(player.getName())).getDisplayName());
                    break;
                case "name":
                    player.sendMessage(API.getNameFromUUID(player.getUniqueId()));
                    break;
                case "ga":
                    player.getInventory().addItem(Glyph.getInstance().getBaseGlyph(args[1], Integer.valueOf(args[2]), Glyph.GlyphType.ARMOR));
                    break;
                case "gw":
                    player.getInventory().addItem(Glyph.getInstance().getBaseGlyph(args[1], Integer.valueOf(args[2]), Glyph.GlyphType.WEAPON));
                    break;
                case "guild":
                    Guild.getInstance().createGuild(args[1], args[2], player.getUniqueId());
                    break;
                case "uploadrealm":
                    new RealmManager().uploadRealm(player.getUniqueId());
                    break;
                case "realm":
                    new RealmManager().downloadRealm(player.getUniqueId());
                    break;
                case "weapon":
                    player.getInventory().addItem(new ItemGenerator().next());
                    break;
                case "armor":
                    player.getInventory().addItem(new ArmorGenerator().next());
                    break;
                case "particle":
                    if (args[1] != null)
                        ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.getById(Integer.valueOf(args[1])), player.getLocation(), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 1F, 250);
                    break;
                case "bank":
                    net.minecraft.server.v1_8_R3.ItemStack nmsBank = CraftItemStack.asNMSCopy(new ItemStack(Material.ENDER_CHEST));
                    NBTTagCompound Banktag = nmsBank.getTag() == null ? new NBTTagCompound() : nmsBank.getTag();
                    Banktag.set("type", new NBTTagString("bank"));
                    nmsBank.setTag(Banktag);
                    player.getInventory().addItem(CraftItemStack.asBukkitCopy(nmsBank));
                    break;
                case "trail":
                    if (args[1] != null)
                        DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.put(player, ParticleAPI.ParticleEffect.getById(Integer.valueOf(args[1])));
                    break;
                case "gold":
                    DonationEffects.getInstance().PLAYER_GOLD_BLOCK_TRAILS.add(player);
                    break;
                case "pick":
                    int tier = 1;
                    if (args.length == 2)
                        tier = Integer.parseInt(args[1]);
                    player.getInventory().addItem(ItemManager.createPickaxe(tier));
                    break;
                case "rod":
                    int rodTier = 1;
                    if (args.length == 2)
                        rodTier = Integer.parseInt(args[1]);
                    player.getInventory().addItem(ItemManager.createFishingPole(rodTier));
                    break;
                case "resetbook":
                    player.getInventory().addItem(ItemManager.createItem(EnumItem.RetrainingBook));
                    break;
                case "journal":
                    player.getInventory().addItem(ItemManager.createCharacterJournal(player));
                    break;
                case "scrap":
                    player.getInventory().addItem(ItemManager.createArmorScrap(1));
                    player.getInventory().addItem(ItemManager.createArmorScrap(2));
                    player.getInventory().addItem(ItemManager.createArmorScrap(3));
                    player.getInventory().addItem(ItemManager.createArmorScrap(4));
                    player.getInventory().addItem(ItemManager.createArmorScrap(5));
                    break;
                case "potion":
                    player.getInventory().addItem(ItemManager.createHealthPotion(1, false, false));
                    player.getInventory().addItem(ItemManager.createHealthPotion(2, false, false));
                    player.getInventory().addItem(ItemManager.createHealthPotion(3, false, false));
                    player.getInventory().addItem(ItemManager.createHealthPotion(4, false, false));
                    player.getInventory().addItem(ItemManager.createHealthPotion(5, false, false));
                    player.getInventory().addItem(ItemManager.createHealthPotion(1, false, true));
                    player.getInventory().addItem(ItemManager.createHealthPotion(2, false, true));
                    player.getInventory().addItem(ItemManager.createHealthPotion(3, false, true));
                    player.getInventory().addItem(ItemManager.createHealthPotion(4, false, true));
                    player.getInventory().addItem(ItemManager.createHealthPotion(5, false, true));
                    break;
                case "food":
                    player.setFoodLevel(1);
                    player.getInventory().addItem(ItemManager.createHealingFood(1, Item.ItemModifier.COMMON));
                    player.getInventory().addItem(ItemManager.createHealingFood(1, Item.ItemModifier.RARE));
                    player.getInventory().addItem(ItemManager.createHealingFood(1, Item.ItemModifier.LEGENDARY));
                    player.getInventory().addItem(ItemManager.createHealingFood(2, Item.ItemModifier.COMMON));
                    player.getInventory().addItem(ItemManager.createHealingFood(2, Item.ItemModifier.RARE));
                    player.getInventory().addItem(ItemManager.createHealingFood(2, Item.ItemModifier.LEGENDARY));
                    player.getInventory().addItem(ItemManager.createHealingFood(3, Item.ItemModifier.COMMON));
                    player.getInventory().addItem(ItemManager.createHealingFood(3, Item.ItemModifier.RARE));
                    player.getInventory().addItem(ItemManager.createHealingFood(3, Item.ItemModifier.LEGENDARY));
                    player.getInventory().addItem(ItemManager.createHealingFood(4, Item.ItemModifier.COMMON));
                    player.getInventory().addItem(ItemManager.createHealingFood(4, Item.ItemModifier.RARE));
                    player.getInventory().addItem(ItemManager.createHealingFood(4, Item.ItemModifier.LEGENDARY));
                    player.getInventory().addItem(ItemManager.createHealingFood(5, Item.ItemModifier.COMMON));
                    player.getInventory().addItem(ItemManager.createHealingFood(5, Item.ItemModifier.RARE));
                    player.getInventory().addItem(ItemManager.createHealingFood(5, Item.ItemModifier.LEGENDARY));
                    break;
                case "test":
                    Bukkit.broadcastMessage("Get2" + String.valueOf(RepairAPI.getCustomDurability(player.getItemInHand())));
                    break;
            }
        }

        return true;
    }
}
