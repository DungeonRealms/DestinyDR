package net.dungeonrealms.game.player.menu;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.player.Rank.PlayerRank;
import net.dungeonrealms.common.game.menu.AbstractMenu;
import net.dungeonrealms.common.game.menu.gui.GUIButtonClickEvent;
import net.dungeonrealms.common.game.menu.item.GUIButton;
import net.dungeonrealms.common.game.updater.Updater;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.title.TitleAPI;
import net.dungeonrealms.game.world.realms.Realms;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/18/2016
 */

public class ShardSwitcher extends AbstractMenu {

    public ShardSwitcher(Player player) {
        super(DungeonRealms.getInstance(), "DungeonRealms Shards", AbstractMenu.round(getFilteredServers().size()), player.getUniqueId());
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

            GUIButton button = new GUIButton(new ItemStack(Material.getMaterial(shard.getType().getIcon()), 1, (short) shard.getType().getMeta())) {

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

                    GameAPI.getGamePlayer(player).setSharding(true);
                    Metadata.SHARDING.set(player, true);

                    TitleAPI.sendTitle(player, 1, 300, 1, ChatColor.YELLOW + "Loading Shard - " + ChatColor.BOLD + shard.getShardID() + ChatColor.YELLOW + " ...", ChatColor.GRAY.toString() + "Do not disconnect");

                    player.sendMessage(ChatColor.GRAY + "Retrieving relevant server information...");
                    player.sendMessage(" ");
                    player.sendMessage("                     " + ChatColor.YELLOW + "Loading Shard - " + ChatColor.BOLD + shard.getShardID() + ChatColor.YELLOW + " ...");
                    player.sendMessage(ChatColor.GRAY + "Your current game session has been paused while you are transferred.");

                    final Location startingLocation = player.getLocation();

                    if (GameAPI.isInSafeRegion(startingLocation) || Rank.isTrialGM(player)) {
                        GameAPI.moveToShard(player, shard.getPseudoName());
                        return;
                    }
                    final int[] taskTimer = {5};

                    new Updater(DungeonRealms.getInstance(), 20L, null) {
                        @Override
                        public void run() {
                            if (taskTimer[0] <= 0) {
                                return;
                            }

                            if (!player.isOnline()) {
                                cancel();
                                return;
                            }

                            if (startingLocation.distanceSquared(player.getLocation()) >= 2.0D || CombatLog.isInCombat(player)) {
                                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/shard - CANCELLED");
                                TitleAPI.sendTitle(player, 1, 1, 1, "");
                                GameAPI.getGamePlayer(player).setAbleToDrop(true);
                                Metadata.SHARDING.remove(player);
                                GameAPI.getGamePlayer(player).setSharding(false);
                                cancel();
                                return;
                            }

                            player.sendMessage(ChatColor.RED + "Commencing shard transfer... " + net.md_5.bungee.api.ChatColor.BOLD + taskTimer[0] + "s");
                            taskTimer[0]--;

                            if (taskTimer[0] <= 0) {
                                GameAPI.moveToShard(player, shard.getPseudoName());
                                cancel();
                            }
                        }
                    };
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

    public String getServerType(String shardID) {
        if (shardID.contains("SUB")) return "Subscribers Only";
        if (shardID.contains("YT")) return "YouTubers Only";
        if (shardID.contains("BR")) return "Brazilian Shard";
        if (shardID.contains("RP")) return "Role-playing Shard";
        if (shardID.contains("CS")) return "Support Agents Only";
        return "";
    }

    @Override
    public void open(Player player) {
        if (getSize() == 0) {
            player.sendMessage(ChatColor.RED + "Unable to find an available shard for you.");
            return;
        }

        if (Realms.getInstance().getRealm(player.getWorld()) != null) {
            player.sendMessage(ChatColor.RED + "You cannot shard in a realm.");
            return;
        }

        if (CombatLog.isInCombat(player)) {
            player.sendMessage(ChatColor.RED + "You cannot transfer shards while in combat.");
            return;
        }

        long lastShardTransfer = PlayerWrapper.getPlayerWrapper(player).getLastShardTransfer();

        if (lastShardTransfer != 0 && !Rank.isTrialGM(player)) {
            if (GameAPI.isInSafeRegion(player.getLocation()) && (System.currentTimeMillis() - lastShardTransfer) < 30000) {
                player.sendMessage(ChatColor.RED + "You must wait 30 seconds before you can transfer between shards.");
                return;
            } else if (!GameAPI.isInSafeRegion(player.getLocation()) && (System.currentTimeMillis() - lastShardTransfer) < 300000) {
                player.sendMessage(ChatColor.RED + "You must wait 5 minutes before you can transfer between shards.");
                return;
            }
        }


        player.openInventory(inventory);
    }

}
