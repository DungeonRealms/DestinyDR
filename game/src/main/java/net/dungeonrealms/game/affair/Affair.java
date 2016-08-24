package net.dungeonrealms.game.affair;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.affair.party.Party;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.ScoreboardHandler;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.DungeonManager;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.chat.GameChat;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Nick on 11/9/2015.
 */
public class Affair implements GenericMechanic {

    static Affair instance = null;

    public static Affair getInstance() {
        if (instance == null) {
            instance = new Affair();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Getter
    private final List<UUID> PARTY_CHAT = new ArrayList<>();

    public CopyOnWriteArrayList<Party> _parties = new CopyOnWriteArrayList<>();
    public static ConcurrentHashMap<Player, Party> _invitations = new ConcurrentHashMap<>();

    @Override
    public void startInitialization() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> _parties.forEach(party -> {
            if (party.getOwner() == null) {
                removeParty(party);
            } else {
            /*
            Scoreboards
             */
                Scoreboard board;
                if (party.getPartyScoreboard() == null) {
                    board = party.createScoreboard();
                    party.setPartyScoreboard(board);
                } else {
                    board = party.getPartyScoreboard();
                }

                Objective objective = board.getObjective("party");
                if (objective == null) {
                    objective = board.registerNewObjective("party", "dummy");
                    objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                    objective.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Party");
                }

                List<Player> allPlayers = new ArrayList<>();
                allPlayers.add(party.getOwner());
                allPlayers.addAll(party.getMembers());

                for (Player player : allPlayers) {
                    if (player != null) {
                        Score score = objective.getScore(player.getName());
                        score.setScore(HealthHandler.getInstance().getPlayerHPLive(player));

                        //Only set the scoreboard if we need to as setScoreboard will send packets and also cause the sb to flicker
                        if (player.getScoreboard() != board) {
                            player.setScoreboard(board);
                        }
                    }
                }

            }

        }), 0, 15);
    }

    public void doChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        String message = event.getMessage();

        if (PARTY_CHAT.contains(player.getUniqueId())) {
            sendPartyChat(player, message);
            event.setCancelled(true);
        }
    }

    public void sendPartyChat(Player player, String message) {
        Party party = Affair.getInstance().getParty(player).get();

        List<Player> everyone = new ArrayList<>();
        {
            everyone.add(party.getOwner());
            everyone.addAll(party.getMembers());
        }
        String finalChat = Chat.getInstance().checkForBannedWords(message);

        if (finalChat.contains("@i@") && player.getEquipment().getItemInMainHand() != null && player.getEquipment().getItemInMainHand().getType() != Material.AIR) {
            String[] split = finalChat.split("@i@");
            String after = "";
            String before = "";
            if (split.length > 0)
                before = split[0];
            if (split.length > 1)
                after = split[1];


            ItemStack stack = player.getInventory().getItemInMainHand();

            List<String> hoveredChat = new ArrayList<>();
            ItemMeta meta = stack.getItemMeta();
            hoveredChat.add((meta.hasDisplayName() ? meta.getDisplayName() : stack.getType().name()));
            if (meta.hasLore())
                hoveredChat.addAll(meta.getLore());
            String prefix = net.md_5.bungee.api.ChatColor.LIGHT_PURPLE + "<" + net.md_5.bungee.api.ChatColor.BOLD + "P" + net.md_5.bungee.api.ChatColor.LIGHT_PURPLE + "> " + net.md_5.bungee.api.ChatColor.GRAY + GameChat.getName(player, Rank.getInstance().getRank(player.getUniqueId()), true) + net.md_5.bungee.api.ChatColor.GRAY + ": ";
            final JSONMessage normal = new JSONMessage(prefix, org.bukkit.ChatColor.WHITE);
            normal.addText(before + "");
            normal.addHoverText(hoveredChat, org.bukkit.ChatColor.BOLD + org.bukkit.ChatColor.UNDERLINE.toString() + "SHOW");
            normal.addText(after);
            everyone.forEach(normal::sendToPlayer);
            return;
        }
        everyone.forEach(player1 -> player1.sendMessage(net.md_5.bungee.api.ChatColor.LIGHT_PURPLE + "<" + net.md_5.bungee.api.ChatColor.BOLD + "P" + net.md_5.bungee.api.ChatColor.LIGHT_PURPLE + "> " + net.md_5.bungee.api.ChatColor.GRAY + GameChat.getName(player, Rank.getInstance().getRank(player.getUniqueId()), true) + net.md_5.bungee.api.ChatColor.GRAY + ": " + message.toString()));

    }

    public void togglePartyChat(Player player) {
        if (PARTY_CHAT.contains(player.getUniqueId())) {
            PARTY_CHAT.remove(player.getUniqueId());
            player.sendMessage(ChatColor.GRAY + "Messages will now be default sent to local chat.");
        } else {
            PARTY_CHAT.add(player.getUniqueId());
            player.sendMessage(ChatColor.DARK_AQUA + "Messages will now be default sent to party chat. Type " + ChatColor.UNDERLINE + "/l <msg>" + ChatColor.DARK_AQUA + " to speak in local.");
            player.sendMessage(ChatColor.GRAY + "To change back to default local, type " + ChatColor.BOLD + "/p" + ChatColor.GRAY + " again.");
        }
    }

    public void invitePlayer(Player inviting, Player invitor) {
        _invitations.put(inviting, getParty(invitor).get());
        inviting.sendMessage(
                ChatColor.LIGHT_PURPLE.toString() + ChatColor.UNDERLINE + invitor.getName() + ChatColor.GRAY + " has invited you to join their party! To accept, type "
                        + ChatColor.LIGHT_PURPLE + "/paccept" + ChatColor.GRAY + " or to decline, type " + ChatColor.LIGHT_PURPLE + "/pdecline");

        invitor.sendMessage(ChatColor.GRAY + "You have invited " + ChatColor.LIGHT_PURPLE + inviting.getDisplayName() + ChatColor.GRAY + " to join your party.");
    }

    public void removeParty(Party party) {

        List<Player> allPlayers = new ArrayList<>();
        allPlayers.add(party.getOwner());
        allPlayers.addAll(party.getMembers());

        allPlayers.forEach(player -> {
            if (GameAPI.getGamePlayer(player) != null) {
                if (GameAPI.getGamePlayer(player).isInDungeon()) {
                    DungeonManager.DungeonObject dungeonObject = DungeonManager.getInstance().getDungeon(player.getWorld());
                    if (!dungeonObject.beingRemoved) {
                        dungeonObject.beingRemoved = true;
                        DungeonManager.getInstance().removeInstance(dungeonObject);
                    }
                }
            }
            player.setScoreboard(ScoreboardHandler.getInstance().mainScoreboard);
            player.sendMessage(ChatColor.RED + "Your party has been disbanded.");
        });


        Utils.log.info("Deleted Old Party: " + party.getOwner().getName());

        _parties.remove(party);
    }

    public void removeMember(Player player, boolean kicked) {
        if (!getParty(player).isPresent()) {
            return;
        }

        if (isOwner(player)) {
            removeParty(getParty(player).get());
            return;
        }

        Party party = getParty(player).get();

        party.getMembers().remove(player);
        Scoreboard board = party.getPartyScoreboard();
        board.resetScores(player.getName());

        if (kicked)
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You have been kicked out of the party.");
        else
            player.sendMessage(ChatColor.RED + "You have left the party.");

        party.getOwner().sendMessage(ChatColor.LIGHT_PURPLE + "<" + ChatColor.BOLD + "P" + ChatColor.LIGHT_PURPLE + "> " + ChatColor.GRAY + player.getDisplayName() + " has " + ChatColor.LIGHT_PURPLE + ChatColor.UNDERLINE + "left" + ChatColor.GRAY + " the party.");
        party.getMembers().stream().filter(player1 -> !player1.getName().equals(party.getOwner().getName())).forEach(player1 -> player1.sendMessage(ChatColor.LIGHT_PURPLE + "<" + ChatColor.BOLD + "P" + ChatColor.LIGHT_PURPLE + "> " + ChatColor.GRAY + player.getDisplayName() + " has " + ChatColor.LIGHT_PURPLE + ChatColor.UNDERLINE + "left" + ChatColor.GRAY + " the party."));

        player.setScoreboard(ScoreboardHandler.getInstance().mainScoreboard);

        if (player.isOnline() && GameAPI.getGamePlayer(player) != null) {
            if (GameAPI.getGamePlayer(player).isInDungeon()) {
                DungeonManager.getInstance().getPlayers_Entering_Dungeon().put(player.getName(), 300);
                player.teleport(Teleportation.Cyrennica);
            }
        }

    }

    public boolean isOwner(Player player) {
        return isInParty(player) && getParty(player).get().getOwner().equals(player);
    }

    public boolean areInSameParty(Player player1, Player player2) {
        return isInParty(player1) && isInParty(player2) && (getParty(player1).get().getOwner().getName()
                .equalsIgnoreCase(getParty(player2).get().getOwner().getName().toLowerCase()));
    }

    public int amountInParty(Party party) {
        return party.getMembers().size() + 1;
    }

    public boolean isInParty(Player player) {
        for (Party party : _parties) {
            if (party.getOwner().getName().equals(player.getName())) {
                return true;
            }
            for (Player player1 : party.getMembers()) {
                if (player.getName().equals(player1.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void createParty(Player player) {
        _parties.add(new Party(player, new ArrayList<>()));
        player.sendMessage(new String[]{
                ChatColor.GREEN + ChatColor.BOLD.toString() + "Your party has been created!",
                ChatColor.GRAY + "To invite more people to join your party, " + ChatColor.UNDERLINE + "Left Click" + ChatColor.GRAY + " them with your character journal or use " + ChatColor.BOLD + "/pinvite" + ChatColor.GRAY + ". To kick, use " + ChatColor.BOLD + "/pkick" + ChatColor.GRAY + ". To chat with party, use " + ChatColor.BOLD + "/p" + ChatColor.GRAY + ". To change the loot profile, use " + ChatColor.BOLD + "/ploot"
        });

        Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.PARTY_MAKER);
    }

    public Optional<Party> getParty(Player player) {
        return _parties.stream().filter(affairO -> affairO.getOwner().equals(player) || affairO.getMembers().contains(player)).findFirst();
    }

    @Override
    public void stopInvocation() {

    }
}


