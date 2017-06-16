package net.dungeonrealms.lobby;


import net.dungeonrealms.common.game.database.player.PlayerRank;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.menu.AbstractMenu;
import net.dungeonrealms.common.game.menu.gui.GUIButtonClickEvent;
import net.dungeonrealms.common.game.menu.item.GUIButton;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;


public class ShardSelector extends AbstractMenu {

//    private Set<UUID> accepted = new HashSet<>();
    public ShardSelector(Player player) {
        super(Lobby.getInstance(), "DungeonRealms Shards", AbstractMenu.round(getFilteredServers().size()), player.getUniqueId());
        setDestroyOnExit(true);

        List<BungeeServerInfo> servers = new ArrayList<>(getFilteredServers().values());

        Collections.sort(servers, (o1, o2) -> {

            if (!o1.getServerName().contains("us"))
                return -1;

            String numString1 = o1.getServerName().substring(o1.getServerName().length() - 1);
            String numString = o2.getServerName().substring(o2.getServerName().length() - 1);
            if (StringUtils.isNumeric(numString) && StringUtils.isNumeric(numString1)) {
                int o2num = Integer.parseInt(numString);
                int o1num = Integer.parseInt(numString1);
                return o1num - o2num;
            }
            return -1;
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
                        return;
                    }

                    if (!rank.isAtLeast(shard.getType().getMinRank())) {
                        player.sendMessage(ChatColor.RED + "This is a " + ChatColor.BOLD + ChatColor.UNDERLINE + shard.getType().name() + " ONLY" + ChatColor.RED + " shard!");

                        if (shard.getType().getMinRank() == PlayerRank.SUB)
                            player.sendMessage(ChatColor.RED + "You can subscribe at: " + ChatColor.UNDERLINE + "http://www.dungeonrealms.net/store");
                        return;
                    }

                    if (player.hasMetadata("denied")) {
                        //CANT!!!
                        player.sendMessage(ChatColor.RED + "You do NOT have the Dungeon Realms Resource Pack Enabled.");
                        player.sendMessage(ChatColor.RED + "Some Aspects of the game will NOT make any sense.");
                        player.sendMessage(ChatColor.GRAY + "Please enter this code to " + ChatColor.GREEN + ChatColor.BOLD + "CONFIRM" + ChatColor.GRAY + " you do not wish to download our resource pack.");

                        int number = ThreadLocalRandom.current().nextInt(9000) + 1000;

                        player.sendMessage(ChatColor.GRAY + "Pack Bypass Code: " + ChatColor.BOLD + ChatColor.UNDERLINE + number);
                        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_AMBIENT, 1, 1.8F);

                        Lobby.chatCallbacks.put(player.getUniqueId(), chatEvent -> {
                            String msg = chatEvent.getMessage();
                            Lobby.chatCallbacks.remove(player.getUniqueId());
                            if (msg.equals(String.valueOf(number))) {
                                Bukkit.getLogger().info(player.getName() + " Manually entered " + number + " to not have to download pack.." );
                                player.sendMessage(ChatColor.RED + "Coded entered successfully, Please Just use /pack to install the pack instead");
                                BungeeUtils.sendToServer(player.getName(), info.getServerName());
                            } else {
                                player.sendMessage(ChatColor.RED + "You entered an INVALID code!");
                                player.sendMessage(ChatColor.GRAY + "Please try to select a server again, or just download the Resource Pack with /pack and not have to do this");
                            }
                        });
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
