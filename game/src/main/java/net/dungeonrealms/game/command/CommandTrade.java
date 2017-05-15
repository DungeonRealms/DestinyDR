package net.dungeonrealms.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.util.Cooldown;
import net.dungeonrealms.database.PlayerToggles;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.trade.Trade;
import net.dungeonrealms.game.player.trade.TradeManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Rar349 on 5/14/2017.
 */
public class CommandTrade extends BaseCommand {

    public CommandTrade() {
        super("trade", "/trade <player>", "Trade a player that is near you!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {
            return false;
        }
        if(args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Use /trade <player>");
            return true;
        }

        Player player = (Player) sender;
        Player other = Bukkit.getPlayer(args[0]);
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(other);
        if(other == null) {
            sender.sendMessage(ChatColor.RED + "This player is not online!");
            return true;
        }

        if(player.getLocation().distanceSquared(other.getLocation()) > 10) {
            player.sendMessage("This player is too far away!");
            return true;
        }

        Trade playerTrade = TradeManager.getTrade(player.getUniqueId());
        Trade otherTrade = TradeManager.getTrade(other.getUniqueId());
        if (playerTrade != null) {
            player.sendMessage(ChatColor.RED + "You are already in a trade!");
            Constants.log.info(player.getName() + " Attempted to trade while in a trade!");
            return true;
        }
        if(otherTrade != null) {
            player.sendMessage(ChatColor.RED + "This person is already in a trade with someone else!");
            return true;
        }

        if (GameAPI._hiddenPlayers.contains(other) || other.getGameMode() == GameMode.SPECTATOR) return true;

        if (!wrapper.getToggles().getState(PlayerToggles.Toggles.TRADE) && !Rank.isTrialGM(player)) {
            player.sendMessage(ChatColor.RED + other.getName() + " has Trades disabled.");
            other.sendMessage(ChatColor.RED + "Trade attempted, but your trades are disabled.");
            other.sendMessage(ChatColor.RED + "Use " + ChatColor.YELLOW + "/toggletrade " + ChatColor.RED + " to enable trades.");
            return true;
        }


        if (!TradeManager.canTrade(other.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + other.getName() + " is currently busy.");
            return true;
        }
        if (CombatLog.isInCombat(player)) {
            player.sendMessage(ChatColor.YELLOW + "You cannot trade while in combat.");
            player.sendMessage(ChatColor.GRAY + "Wait " + ChatColor.BOLD + "a few seconds" + ChatColor.GRAY + " and try again.");
            return true;
        }


        if (Cooldown.hasCooldown(player.getUniqueId())) {
            return true;
        }


        Cooldown.addCooldown(player.getUniqueId(), 20 * 5);
        TradeManager.startTrade(player, other);
        Trade trade = TradeManager.getTrade(player.getUniqueId());
        if (trade == null)
            return true;

        other.playSound(other.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1F, 0.8F);
        player.playSound(player.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1F, 0.8F);


        return true;
    }
}
