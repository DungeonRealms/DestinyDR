package net.dungeonrealms.game.mechanic;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.achievements.AchievementManager;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Created by chase on 7/11/2016.
 */
public class TutorialIsland implements GenericMechanic, Listener {

    public static HashMap<String, List<String>> quest_map = new HashMap<>();
    // Player_name, List of remaining NPC names to be spoken too.

    public static Map<UUID, List<String>> WELCOMES = new HashMap<>();

    public static HashMap<String, List<String>> completion_delay = new HashMap<>();
    // Player_name, List of NPC names who have a timer event to tell them they've completed running. (used for rewards)

    public static final String tutorialRegion = "tutorial_island";
    // Region name of tutorial island.

    List<String> got_exp = new ArrayList<>();
    // Already got exp.

    private static TutorialIsland instance = null;

    public List<String> getWelcomes(UUID uuid) {
        List<String> welcomes;
        if (!WELCOMES.containsKey(uuid)) {
            welcomes = new ArrayList<>();
            WELCOMES.put(uuid, welcomes);
        } else welcomes = WELCOMES.get(uuid);
        return welcomes;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    @Override
    public void startInitialization() {
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::hideVanishedPlayers, 100L, 1L);
    }


    public static TutorialIsland getInstance() {
        if (instance == null) {
            instance = new TutorialIsland();
        }
        return instance;
    }

    private void hideVanishedPlayers() {
        GameAPI._hiddenPlayers.stream().filter(player -> player != null).forEach(player -> {
            for (Player player1 : Bukkit.getOnlinePlayers()) {
                // GMs can see hidden players whereas non-GMs cannot.
                if (player1.getUniqueId().toString().equals(player.getUniqueId().toString()) || Rank.isGM(player1)) {
                    player1.showPlayer(player);
                } else {
                    player1.hidePlayer(player);
                }
            }
        });
    }


    public boolean onTutorialIsland(UUID uuid) {
        return AchievementManager.REGION_TRACKER.get(uuid).equalsIgnoreCase("tutorial_island");
    }

    public static boolean onTutorialIsland(Location loc) {
        if (loc == null) {
            return false;
        }
        if (GameAPI.getRegionName(loc).equalsIgnoreCase(tutorialRegion)) {
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemDrop(PlayerDropItemEvent e) {
        Player pl = e.getPlayer();
        if (onTutorialIsland(pl.getLocation())) {
            e.setCancelled(true);
            pl.updateInventory();
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() == EntityType.ITEM_FRAME) {
            ItemFrame is = (ItemFrame) event.getEntity();
            is.setItem(is.getItem());
            is.setRotation(Rotation.NONE);
            event.setCancelled(true);
            if (event.getDamager() instanceof Player) {
                if (is.getItem().getType() != Material.MAP) return;
                Player plr = (Player) event.getDamager();
                if (plr.getInventory().contains(is.getItem())) {
                    return;
                }
                plr.getInventory().addItem(is.getItem());
            }
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() == EntityType.ITEM_FRAME) {
            event.setCancelled(true);
            ItemFrame is = (ItemFrame) event.getRightClicked();
            is.setItem(is.getItem());
            is.setRotation(Rotation.NONE);
            return;
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player pl = event.getPlayer();
        if (onTutorialIsland(pl.getLocation())) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                pl.sendMessage("");
                pl.sendMessage("");
                pl.sendMessage("");
                pl.sendMessage(ChatColor.YELLOW + "                    Welcome to " + ChatColor.UNDERLINE + "Dungeon Realms!");


                pl.sendMessage(ChatColor.GRAY + "Before you dive into the mystical world of Andalucia and discover all of its wonders, you are encouraged to go through this short introductory " + ChatColor.YELLOW + "Tutorial Island.");
                pl.sendMessage("");
                pl.sendMessage(ChatColor.GRAY + "You'll get a crash course on game mechanics and " + ChatColor.UNDERLINE + "get free loot" + ChatColor.GRAY + " just for completing it!");
                pl.sendMessage("");
            }, 80L);
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "OBJECTIVE: " + ChatColor.GRAY + "Speak with the Island Greeter.");
                pl.playSound(pl.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 63f);
            }, 90L);
            if (!(quest_map.containsKey(pl.getName()))) {
                List<String> quests_left = new ArrayList<>(Arrays.asList("Master Miner", "Master Fisherman",
                        "Equipment Master", "Interface Guide", "Item Enchanter", "Armor " +
                                "Guide", "Alignment Guide", ChatColor.YELLOW.toString() + "Neutral Guide", ChatColor
                                .RED.toString
                                        () + "Chaotic Guide", ChatColor.LIGHT_PURPLE + "[100]" + ChatColor.GRAY + " Lee"));
                quest_map.put(pl.getName(), quests_left);
                completion_delay.put(pl.getName(), new ArrayList<>());
            }
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteractNPC(PlayerInteractEntityEvent e) {
        final Player pl = e.getPlayer();
        if (!(onTutorialIsland(pl.getUniqueId()))) return;

        Entity e_npc = e.getRightClicked();
        if (!(e_npc instanceof Player)) {
            return;
        }
        Player npc = (Player) e_npc;
        if (!(npc.hasMetadata("NPC"))) {
            return;
        }

        if (npc.getName().equalsIgnoreCase("Ship Sailor")) {
            e.setCancelled(true);
            pl.performCommand("skip");
            return;
        }

        if (npc.getName().equalsIgnoreCase("Ship Captain")) {
            // Check to see if they're ready to head to the mainland.
            if (quest_map.containsKey(pl.getName()) && quest_map.get(pl.getName()).size() > 0) {
                List<String> all_quests = new ArrayList<>(Arrays.asList("Island Greeter", "Master Miner", "Master Fisherman", "Equipment Master", "Interface Guide", "Item Enchanter", "Armor Guide", "Alignment Guide", ChatColor.RED.toString() + "Chaotic Guide", ChatColor.YELLOW.toString() + "Neutral Guide", ChatColor.LIGHT_PURPLE + "[100]" + ChatColor.GRAY + " Lee"));
                List<String> quest_list = quest_map.get(pl.getName());
                if (quest_list.size() > 0) {
                    pl.sendMessage("");
                    for (String s : all_quests) {
                        if (quest_list.contains(s)) {
                            pl.sendMessage(ChatColor.RED.toString() + s);
                            continue; // They haven't completed this one.
                        }
                        pl.sendMessage(ChatColor.GREEN.toString() + s + ChatColor.GREEN + " - DONE!");
                    }
                }
                pl.sendMessage(ChatColor.GRAY + "Ship Captain: " + ChatColor.WHITE + "You cannot leave until you speak with " + ChatColor.UNDERLINE.toString() + quest_list.size() + ChatColor.WHITE + " more Guide(s)!");

                e.setCancelled(true);
                return; // They don't get to leave yet!
            }
            pl.sendMessage("");
            pl.sendMessage(ChatColor.GRAY + "Ship Captain: " + ChatColor.WHITE + "Are you ready to start ye adventure " + pl.getName() + "?"); //+ " " + ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "Y " + ChatColor.GRAY.toString() + "/" + ChatColor.RED.toString() + ChatColor.BOLD.toString() + " N");
            pl.sendMessage(ChatColor.GRAY + "Type either '" + ChatColor.GREEN + "Y" + ChatColor.GRAY + "' or '" + ChatColor.RED + "N" + ChatColor.GRAY + "' -- Yes or No; Once you leave this island you can never come back, your epic adventure in the lands of Andalucia will begin!");
            pl.sendMessage("");
            e.setCancelled(true);
            Chat.listenForMessage(e.getPlayer(), ev -> {
                if (ev.getMessage().equalsIgnoreCase("y")) {

                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                        ev.getPlayer().sendMessage(ChatColor.GRAY + "Ship Captain: " + ChatColor.WHITE + "Argh! We'll be casting off in a few moments!");
                        ev.getPlayer().teleport(new Location(Bukkit.getWorlds().get(0), -600 + .5, 60 + 1.5, 473 + .5, -1F, 2.5F));
                        ItemManager.giveStarter(e.getPlayer());

                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                            final JSONMessage normal = new JSONMessage(ChatColor.GOLD + " ❢ " + ChatColor.YELLOW + "Need more information? Visit our wiki " + ChatColor.WHITE);
                            normal.addURL(ChatColor.GOLD.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE!", ChatColor.GREEN, "http://dungeonrealms.wikia.com/wiki/Main_Page");
                            normal.addSuggestCommand(ChatColor.YELLOW.toString() + " or for any questions. Click " + ChatColor.GOLD.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE!", ChatColor.GREEN, "/ask ");
                            normal.addText(ChatColor.GOLD + " ❢ ");

                            pl.sendMessage("");
                            normal.sendToPlayer(ev.getPlayer());
                            pl.sendMessage("");

                            ev.getPlayer().playSound(ev.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 0.5F);
                        }, 40);
                    });
                }
            }, pla -> pla.sendMessage(ChatColor.GRAY + "Ship Captain: " + ChatColor.WHITE + "Argh! Speak to me when ye ready to leave!"));
            return;
        } else if (npc.getName().equalsIgnoreCase("Island Greeter")) {
            // Give the player 1x gem.

            // TODO: Should be a banker...
        } else if (npc.getName().equalsIgnoreCase("Master Miner") && !(quest_map.get(pl.getName()).contains("Master Miner")) && !(completion_delay.get(pl.getName()).contains(npc.getName()))) {
            // Give the player 5x coal ore, tell them to trade it.
            if (!(pl.getInventory().contains(Material.COAL_ORE))) {
                e.setCancelled(true);
                ItemStack reward = (Mining.getBlock(Material.COAL_ORE));
                reward.setAmount(5);
                if (pl.getInventory().firstEmpty() != -1) {
                    pl.getInventory().addItem(reward);
                    pl.sendMessage(ChatColor.GRAY.toString() + "Master Miner: " + ChatColor.WHITE.toString() + "Ahh, here be some ore for ye' time. You could trade it with the Merchant!");
                    pl.playSound(pl.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1F, 1F);
                }
            }
        } else if (npc.getName().equalsIgnoreCase("Master Fisherman") && !(quest_map.get(pl.getName()).contains("Master Fisherman")) && !(completion_delay.get(pl.getName()).contains(npc.getName()))) {
            // Give the player 1x raw fish, tell them to cook it.
            if (!(pl.getInventory().contains(Material.RAW_FISH)) && !(pl.getInventory().contains(Material.COOKED_FISH))) {
                e.setCancelled(true);
                ItemStack reward = GameAPI.makeItemUntradeable(Fishing.getFishDrop(1));
                if (pl.getInventory().firstEmpty() != -1) {
                    pl.getInventory().addItem(reward);
                    pl.sendMessage(ChatColor.GRAY.toString() + "Master Fisherman: " + ChatColor.WHITE.toString() + "Here's a freshly caught " + reward.getItemMeta().getDisplayName() + "! You should cook it over by the fire.");
                    pl.playSound(pl.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1F, 1F);
                }
            }
        } else if (npc.getName().equalsIgnoreCase("Master Duelist") && !(quest_map.get(pl.getName()).contains("Master Duelist")) && !(completion_delay.get(pl.getName()).contains(npc.getName()))) {
            // Give the player a sword.
            if (!(pl.getInventory().contains(Material.WOOD_SWORD)) && !(pl.getInventory().contains(Material.WOOD_AXE))) {
                e.setCancelled(true);
                if (pl.getInventory().firstEmpty() != -1) {
                    if (Utils.randInt(0, 1) == 1) {
                        pl.getInventory().addItem(ItemGenerator.getNamedItem("training_sword"));
                    } else {
                        pl.getInventory().addItem(ItemGenerator.getNamedItem("training_axe"));
                    }
                    pl.sendMessage(ChatColor.GRAY.toString() + "Master Duelist: " + ChatColor.WHITE.toString() + "Right then, here's a training weapon -- give it a few swings at the dummy targets!");
                    pl.playSound(pl.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1F, 1F);
                }
            }
        } else if (npc.getName().equalsIgnoreCase("Master Marksmen") && !(quest_map.get(pl.getName()).contains("Master Marksmen")) && !(completion_delay.get(pl.getName()).contains(npc.getName()))) {
            // todo: give something else because before he gave arrows
        } else if (npc.getName().equalsIgnoreCase("Armor Guide") && !(quest_map.get(pl.getName()).contains("Armor Guide")) && !(completion_delay.get(pl.getName()).contains(npc.getName()))) {
            // Give the player 1x T1 Scrap
            if (!(pl.getInventory().contains(Material.LEATHER))) {
                e.setCancelled(true);
                ItemStack reward = GameAPI.makeItemUntradeable(ItemManager.createArmorScrap(1));
                if (pl.getInventory().firstEmpty() != -1) {
                    pl.getInventory().addItem(reward);
                    pl.sendMessage(ChatColor.GRAY.toString() + "Armor Guide: " + ChatColor.WHITE.toString() + "Gah! Phew! Nearly burnt me'self there, here's an armor scrap for listening to an old man ramble. Use it to repair your equipment in the field.");
                    pl.playSound(pl.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1F, 1F);
                }
            }
        } else if (npc.getName().equalsIgnoreCase("Item Enchanter") && !(quest_map.get(pl.getName()).contains("Item Enchanter")) && !(completion_delay.get(pl.getName()).contains(npc.getName()))) {
            // Give the player 1x T1 Weapon Scroll, tell them to use it.
            if (!(pl.getInventory().contains(Material.EMPTY_MAP))) {
                e.setCancelled(true);
                ItemStack reward = GameAPI.makeItemUntradeable(ItemManager.createWeaponEnchant(1));
                if (pl.getInventory().firstEmpty() != -1) {
                    pl.getInventory().addItem(reward);
                    pl.sendMessage(ChatColor.GRAY.toString() + "Item Enchanter: " + ChatColor.WHITE.toString() + "Use this enchantment scroll on your weapon to increase its potency.");
                    pl.playSound(pl.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1F, 1F);
                }
            }
        } else if (npc.getName().equalsIgnoreCase(ChatColor.LIGHT_PURPLE + "[100]" + ChatColor.GRAY + " Lee") && !(quest_map.get(pl.getName()).contains(ChatColor.LIGHT_PURPLE + "[100]" + ChatColor.GRAY + " Lee")) && !(completion_delay.get(pl.getName()).contains(npc.getName()))) {
            if (got_exp.contains(pl.getName())) return;
            got_exp.add(pl.getName());

            final List<String> messages = Arrays.asList(
                    "Hello there, I'm the leveling master and I'll be teaching you about leveling.",
                    "The first thing you'l notice your HP bar, at the top, now displays your level.",
                    "Your level is also displayed in your book and on your character's name tag for others to see.",
                    "You can receive experience by killing mobs, completing dungeons and also by killing players.",
                    "You will only receive experience from mobs that are within an +/- 8 level range.",
                    "This means if you're level 16 and you kill a mob level 12, you will receive experience.",
                    "However, if you're level 16 and you kill a mob level 25, you won't receive any experience.",
                    "You can also gain experience as part of a party!",
                    "You will, however, receive less experience if you are part of a party.",
                    "Dungeons also provide additional experience for completing them.",
                    "To use/wear certain tiers you will require a certain level.",
                    "Tier 1 requires a level of 1 (default level).",
                    "Tier 2 requires a level of 20.",
                    "Tier 3 requires a level of 40.",
                    "Tier 4 requires a level of 60.",
                    "And finally, Tier 5 requires a level of 80.",
                    "Likewise with weapons and armour, the individual horse tiers require a certain level too.",
                    "A tier 1 horse requires level 1 (default level)",
                    "A tier 2 horse requires level 30.",
                    "A tier 3 horse requires level 60.",
                    "And finally, a tier 4 horse requires level 90.",
                    "Good luck adventure, those who become a master of leveling will receive something special!",
                    "Good luck on your quest to level 100!"
            );

            new BukkitRunnable() {
                private int pos = 0;

                @Override
                public void run() {
                    if (messages.size() - 1 >= pos) {
                        pl.sendMessage(ChatColor.LIGHT_PURPLE + "[100]" + ChatColor.GRAY + " Lee" + ": " + ChatColor.WHITE + "\"" + messages.get(pos) + "\"");
                        pos++;
                    } else {
                        GameAPI.getGamePlayer(pl).addExperience(50, false, true);
                        cancel();
                    }
                }
            }.runTaskTimer(DungeonRealms.getInstance(), 0L, 20L * 3L);
        }

        if (quest_map.containsKey(pl.getName())) {
            List<String> quests_left = quest_map.get(pl.getName());
            if (quests_left.contains(npc.getName())) {
                if (completion_delay.containsKey(pl.getName())) {
                    List<String> lcd = completion_delay.get(pl.getName());
                    lcd.add(npc.getName());
                    completion_delay.put(pl.getName(), lcd);
                } else {
                    completion_delay.put(pl.getName(), new ArrayList<>(Collections.singletonList(npc.getName())));
                }

                quests_left.remove(npc.getName());
                quest_map.put(pl.getName(), quests_left);

                final String npc_name = npc.getName();
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    List<String> lcd = completion_delay.get(pl.getName());
                    if (lcd == null) return;
                    lcd.remove(npc_name);
                    completion_delay.put(pl.getName(), lcd);
                    pl.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "       OBJECTIVE COMPLETE:" + ChatColor.GREEN.toString() + " Speak to " + ChatColor.UNDERLINE + npc_name + ChatColor.GREEN.toString() + "!");
                    if (npc_name.equalsIgnoreCase("Island Greeter")) {
                        pl.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Your next objective is to follow the road out of the house and meet your first guide.");
                    } else {
                        if (npc_name.equalsIgnoreCase("Master Miner") || npc_name.equalsIgnoreCase("Master Fisherman") || npc_name.equalsIgnoreCase("Master Duelist") || npc_name.equalsIgnoreCase("Master Marksmen") || npc_name.equalsIgnoreCase("Armor Guide") || npc_name.equalsIgnoreCase("Item Enchanter")) {
                            pl.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Claim your " + ChatColor.UNDERLINE + "reward" + ChatColor.RESET + ChatColor.GRAY + ChatColor.ITALIC + " by speaking to " + npc_name + ", then continue down the road.");
                        } else {
                            pl.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "To discover your next objective, finish speaking with the " + npc_name + ChatColor.GRAY + ", then continue down the road.");
                        }
                    }
                    pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 4.0F);
                }, 6 * 20L);

            }
        }
    }


    @Override
    public void stopInvocation() {

    }
}
