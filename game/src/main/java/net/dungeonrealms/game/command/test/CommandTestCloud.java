package net.dungeonrealms.game.command.test;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.items.core.ProfessionItem;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.player.cosmetics.particles.SpecialParticleEffect;
import net.dungeonrealms.game.player.cosmetics.particles.SpecialParticles;
import net.dungeonrealms.game.player.cosmetics.particles.impl.CrateOpeningEffect;
import net.dungeonrealms.game.player.cosmetics.particles.impl.FourthOfJulyAuraEffect;
import net.dungeonrealms.game.player.cosmetics.particles.impl.FourthOfJulySpiral;
import net.dungeonrealms.game.player.inventory.menus.guis.merchant.ItemMerchant;
import net.dungeonrealms.game.player.inventory.menus.guis.polls.PollManager;
import net.dungeonrealms.game.player.inventory.menus.guis.polls.PollSelectionGUI;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.CrateGUI;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates.Crates;
import net.dungeonrealms.game.quests.compass.CompassGoal;
import net.dungeonrealms.game.quests.compass.CompassManager;
import net.dungeonrealms.game.quests.compass.CompassNode;
import net.dungeonrealms.tool.coupon.CouponCodeGenerator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by Rar349 on 6/7/2017.
 */
public class CommandTestCloud extends BaseCommand {
    public CommandTestCloud() {
        super("drcloud");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        if (!Rank.isDev((Player) sender)) return false;
        Player player = (Player) sender;
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);

        //PollManager.openPollBooth(player);
        //String s = CompassManager.calculateCompassString(player);
        //System.out.println(s);
        //player.sendMessage(s);

        /*CompassManager manager = CompassManager.getManager(player);
        if(manager != null) {
            manager.registerGoal(new CompassGoal(player.getLocation().getBlock().getLocation().clone(), ChatColor.GREEN));
        }*/

        new ItemMerchant(player, 0).open(player,null);
        return true;
    }
}
