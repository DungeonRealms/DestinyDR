package net.dungeonrealms.lobby;


import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.player.Rank.PlayerRank;
import net.dungeonrealms.common.game.menu.AbstractMenu;
import net.dungeonrealms.common.game.menu.gui.GUIButtonClickEvent;
import net.dungeonrealms.common.game.menu.item.GUIButton;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class ShardSelector extends AbstractMenu {

    public ShardSelector(Player player) {
        super(Lobby.getInstance(), "DungeonRealms Shards", AbstractMenu.round(getFilteredServers().size()), player.getUniqueId());
        setDestroyOnExit(true);

        List<BungeeServerInfo> servers = new ArrayList<>(getFilteredServers().values());

        Collections.sort(servers, (o1, o2) -> {

            if (!o1.getServerName().contains("us"))
                return -1;

            int o1num = Integer.parseInt(o1.getServerName().substring(o1.getServerName().length() - 1));
            int o2num = Integer.parseInt(o2.getServerName().substring(o2.getServerName().length() - 1));

            return o1num - o2num;
        });

        PlayerRank rank = Rank.getRank(player);

        // DISPLAY AVAILABLE SHARDS //
        for (BungeeServerInfo info : servers) {
            ShardInfo shard = ShardInfo.getByPseudoName(info.getServerName());

            // Don't show shard if you aren't allowed to see them.
            if (!rank.isAtLeast(shard.getType().getMinRank()) && shard.getType().getMinRank() != PlayerRank.SUB)
                continue;

            GUIButton button = new GUIButton(new ItemStack(Material.matchMaterial(shard.getType().getIcon()), 1, (short) shard.getType().getMeta())) {

                @Override
                public void action(GUIButtonClickEvent event) throws Exception {
                    Player player = event.getWhoClicked();
                    player.closeInventory();

                    if (info.getOnlinePlayers() >= info.getMaxPlayers() && !rank.isSUB()) {
                        player.sendMessage(new String[]{
                                ChatColor.RED + "This shard is " + ChatColor.BOLD + ChatColor.UNDERLINE + "FULL" + ChatColor.RED + " for normal users!",
                                ChatColor.RED + "You can subscribe at: " + ChatColor.UNDERLINE + "http://www.dungeonrealms.net/store" + ChatColor.RED + " to bypass this."
                        });
                    }

                    if (!rank.isAtLeast(shard.getType().getMinRank())) {
                        player.sendMessage(ChatColor.RED + "This is a " + ChatColor.BOLD + ChatColor.UNDERLINE + shard.getType().name() + " ONLY" + ChatColor.RED + " shard!");

                        if (shard.getType().getMinRank() == PlayerRank.SUB)
                            player.sendMessage(ChatColor.RED + "You can subscribe at: " + ChatColor.UNDERLINE + "http://www.dungeonrealms.net/store");
                        return;
                    }

                    BungeeUtils.sendToServer(player.getName(), info.getServerName());
                }
            };

            List<String> lore = new ArrayList<>();

            if (shard.getType().getDescription().length() > 0)
                lore.add(ChatColor.RED + "" + ChatColor.ITALIC + shard.getType().getDescription());


            //final int slot = getServerType(shardID).equals("") ? getSize() : Math.min(getInventorySize(), getInventorySize() - 1) - (getSize() - getNormalServers());

            lore.add(ChatColor.GREEN + "This shard is online!");
            lore.add(ChatColor.WHITE + "Click here to load your");
            lore.add(ChatColor.WHITE + "character onto this shard.");
            lore.add(" ");

            try {
                String[] data = info.getMotd1().replace("}", "").replace("\"", "").split(",");
                lore.add(ChatColor.GRAY + "Load: " + data[1]);

                lore.add(ChatColor.GRAY + "Online: " + info.getOnlinePlayers() + "/" + info.getMaxPlayers());

                if (data.length >= 3)
                    lore.add(ChatColor.GRAY + "Build: " + ChatColor.GOLD + data[2]);

            } catch (Exception e) {
                Bukkit.getLogger().info("Problem parsing " + info.getServerName());
                e.printStackTrace();
            }
            button.setDisplayName(shard.getType().getColor() + "" + ChatColor.BOLD + shard.getShardID());
            button.setLore(lore);

            set(getSize(), button);
        }
    }

    private static Map<String, BungeeServerInfo> getFilteredServers() {
        Map<String, BungeeServerInfo> filteredServers = new HashMap<>();

        for (Map.Entry<String, BungeeServerInfo> e : BungeeServerTracker.getTrackedServers().entrySet()) {
            String bungeeName = e.getKey();
            if (ShardInfo.getByPseudoName(bungeeName) == null) continue;
            BungeeServerInfo info = e.getValue();

            if (!info.isOnline() || info.getMotd1().contains("offline"))
                continue;

            filteredServers.put(bungeeName, info);
        }

        return filteredServers;
    }

    @Override
    public void open(Player player) {
        if (getSize() == 0) {
            player.sendMessage(ChatColor.RED + "Unable to find an available shard for you.");
            return;
        }

        try {
            AtomicInteger secondsLeft = Lobby.getInstance().getRecentLogouts().getIfPresent(player.getUniqueId());

            if (secondsLeft != null) {
                if (secondsLeft.get() > 0 && !Rank.isTrialGM(player)) {
                    int left = secondsLeft.get();
                    player.sendMessage(ChatColor.RED + "You must wait " + left + " second(s) before you can transfer between shards.");
                    return;
                } else {
                    Lobby.getInstance().getRecentLogouts().invalidate(player.getUniqueId());
                }
            }
        } catch (Exception e) {
            //Catches an NPE relating to if a player has a last shard transfer time
            e.printStackTrace();
        }

        player.openInventory(inventory);
    }

}
