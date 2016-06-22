package net.dungeonrealms.game.listeners;

import com.connorlinfoot.bountifulapi.BountifulAPI;
import com.vexsoftware.votifier.model.VotifierEvent;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.donate.DonationEffects;
import net.dungeonrealms.game.events.PlayerEnterRegionEvent;
import net.dungeonrealms.game.events.PlayerMessagePlayerEvent;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.handlers.KarmaHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.miscellaneous.Cooldown;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.duel.DuelOffer;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.player.inventory.GUI;
import net.dungeonrealms.game.player.inventory.NPCMenus;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.player.rank.Rank;
import net.dungeonrealms.game.player.trade.TradeManager;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.world.entities.utils.EntityAPI;
import net.dungeonrealms.game.world.entities.utils.MountUtils;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.items.repairing.RepairAPI;
import net.dungeonrealms.game.world.party.Affair;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_9_R2.EntityArmorStand;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityUnleashEvent.UnleashReason;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;
import java.util.UUID;

/**
 * Created by Nick on 9/17/2015.
 */
public class MainListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onVote(VotifierEvent event) {
        if (Bukkit.getPlayer(event.getVote().getUsername()) != null) {
            Player player = Bukkit.getPlayer(event.getVote().getUsername());

            String rank = Rank.getInstance().getRank(player.getUniqueId());

            GamePlayer gamePlayer = API.getGamePlayer(player);
            int expToLevel = gamePlayer.getEXPNeeded(gamePlayer.getLevel());
            int expToGive = expToLevel / 20;
            expToGive += 100;
            TextComponent bungeeMessage = new TextComponent(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE");
            bungeeMessage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://minecraftservers.org/server/298658"));
            bungeeMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to vote!").create()));

            switch (rank.toLowerCase()) {
                case "default":
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$INC, EnumData.ECASH, 15, true);
                    Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.VOTE);
                    if (API.getGamePlayer(player) == null) {
                        return;
                    }
                    gamePlayer.addExperience(expToGive, false);
                    final JSONMessage normal = new JSONMessage(ChatColor.AQUA + player.getName() + ChatColor.RESET + ChatColor.GRAY + " voted for 15 ECASH & 5% EXP @ vote ", ChatColor.WHITE);
                    normal.addURL(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE", ChatColor.AQUA, "http://minecraftservers.org/server/298658");
                    for (Player player1 : Bukkit.getOnlinePlayers()) {
                        normal.sendToPlayer(player1);
                    }
                    break;
                case "sub":
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$INC, EnumData.ECASH, 20, true);
                    Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.VOTE_AS_SUB);
                    if (API.getGamePlayer(player) == null) {
                        return;
                    }
                    final JSONMessage normal2 = new JSONMessage(ChatColor.AQUA + player.getName() + ChatColor.RESET + ChatColor.GRAY + " voted for 25 ECASH & 5% EXP @ vote ", ChatColor.WHITE);
                    normal2.addURL(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE", ChatColor.AQUA, "http://minecraftservers.org/server/298658");
                    for (Player player1 : Bukkit.getOnlinePlayers()) {
                        normal2.sendToPlayer(player1);
                    }
                    break;
                case "sub+":
                case "sub++":
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$INC, EnumData.ECASH, 25, true);
                    Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.VOTE_AS_SUB_PLUS);
                    if (API.getGamePlayer(player) == null) {
                        return;
                    }
                    gamePlayer.addExperience(expToGive, false);
                    final JSONMessage normal3 = new JSONMessage(ChatColor.AQUA + player.getName() + ChatColor.RESET + ChatColor.GRAY + " voted for 25 ECASH & 5% EXP @ vote ", ChatColor.WHITE);
                    normal3.addURL(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE", ChatColor.AQUA, "http://minecraftservers.org/server/298658");
                    for (Player player1 : Bukkit.getOnlinePlayers()) {
                        normal3.sendToPlayer(player1);
                    }
                    break;
                default:
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$INC, EnumData.ECASH, 15, true);
                    Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.VOTE);
                    if (API.getGamePlayer(player) == null) {
                        return;
                    }
                    gamePlayer.addExperience(expToGive, false);
                    final JSONMessage normal4 = new JSONMessage(ChatColor.AQUA + player.getName() + ChatColor.RESET + ChatColor.GRAY + " voted for 15 ECASH & 5% EXP @ vote ", ChatColor.WHITE);
                    normal4.addURL(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE", ChatColor.AQUA, "http://minecraftservers.org/server/298658");
                    for (Player player1 : Bukkit.getOnlinePlayers()) {
                        normal4.sendToPlayer(player1);
                    }
                    break;
            }
        } else {
            /*
            This shouldn't ever happen because the Bungee plugin passes the vote down to the
            server that the player joins, but.. if it does happen!
             */
            DatabaseAPI.getInstance().update(UUID.fromString(event.getVote().getUsername()), EnumOperators.$INC, EnumData.ECASH, 15, false);
            Utils.log.warning("Unable to process rank for user: " + event.getVote().getUsername() + " the vote was passed to the server and the player ISNT ONLINE WTF?");
        }

    }

    @EventHandler
    public void onPm(PlayerMessagePlayerEvent event) {
        if (event.getSender().equals(event.getReceiver())) {
            Achievements.getInstance().giveAchievement(event.getSender().getUniqueId(), Achievements.EnumAchievements.MESSAGE_YOURSELF);
        } else {
            Achievements.getInstance().giveAchievement(event.getSender().getUniqueId(), Achievements.EnumAchievements.SEND_A_PM);
        }
    }

    @EventHandler
    public void onPing(ServerListPingEvent event) {
        if (!DungeonRealms.getInstance().hasFinishedSetup()) event.setMotd("offline");
        else event.setMotd(DungeonRealms.getInstance().shardid);
    }

    /**
     * Monitors and checks the players language.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onChat(AsyncPlayerChatEvent event) {
        Chat.getInstance().doChat(event);
        GuildMechanics.getInstance().doChat(event);
        Chat.getInstance().doLocalChat(event);
    }

    /**
     * This event is used for the Database.
     *
     * @param event the event.
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onAsyncJoin(AsyncPlayerPreLoginEvent event) {
        if (!DungeonRealms.getInstance().hasFinishedSetup() && !DungeonRealms.getInstance().getDevelopers().contains(event.getName())) {
            event.disallow(Result.KICK_OTHER, ChatColor.GREEN + "The server is still setting up reconnect shortly!");
            return;
        }
        DatabaseAPI.getInstance().requestPlayer(event.getUniqueId());
    }

    /**
     * This event is the main event once the player has actually entered the
     * world! It is now safe to do things to the player e.g BountifulAPI or
     * adding PotionEffects.. etc..
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        DatabaseAPI.getInstance().PLAYER_TIME.put(event.getPlayer().getUniqueId(), 0);
        Player player = event.getPlayer();
        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 4));
        player.teleport(new Location(Bukkit.getWorlds().get(0), 0, 255, 0, 0f, 0f));
        BountifulAPI.sendTitle(player, 1, 20 * 3, 1, ChatColor.GREEN.toString() + "Loading character for '" + player.getName() + "' ...", ChatColor.GRAY.toString() + "Do not disconnect");
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            armor[i] = new ItemStack(Material.AIR);
        }
        player.getInventory().setArmorContents(armor);
        player.sendMessage(ChatColor.GREEN + "Loading your data.. This will only take a moment!");

        CombatLog.checkCombatLog(player.getUniqueId());
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> API.handleLogin(player.getUniqueId()), 20L * 3);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            if (player.isOnline()) {
                if ((Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.LOGGERDIED, player.getUniqueId()).toString()))) {
                    player.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "You logged out while in combat, you're doppelganger was killed and alas your items are gone.");
                    player.teleport(Teleportation.Cyrennica);
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.LOGGERDIED, false, true);
                    player.getInventory().addItem(new ItemBuilder().setItem(ItemManager.createHealthPotion(1, false, false)).setNBTString("subtype", "starter").build());
                    player.getInventory().addItem(new ItemBuilder().setItem(ItemManager.createHealthPotion(1, false, false)).setNBTString("subtype", "starter").build());
                    player.getInventory().addItem(new ItemBuilder().setItem(ItemManager.createHealthPotion(1, false, false)).setNBTString("subtype", "starter").build());
                    player.getInventory().addItem(new ItemBuilder().setItem(new ItemGenerator().setTier(Item.ItemTier.TIER_1).setRarity(Item.ItemRarity.COMMON)
                            .setType(Item.ItemType.AXE).generateItem().getItem()).setNBTString("subtype", "starter").build());
                }
            }
        }, 20L * 5);
    }

    /**
     * Cancel spawning unless it's CUSTOM. So we don't have RANDOM SHEEP. We
     * have.. CUSTOM SHEEP. RAWR SHEEP EAT ME>.. AH RUN!
     *
     * @param event
     * @WARNING: THIS EVENT IS VERY INTENSIVE!
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            event.setCancelled(true);
        }
    }

    /**
     * Makes sure to despawn mounts on dismount and remove from hashmap
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMountDismount(VehicleExitEvent event) {
        if (!(API.isPlayer(event.getExited()))) {
            if (event.getExited() instanceof EntityArmorStand) {
                event.getExited().remove();
            }
            return;
        }
        if (EntityAPI.hasMountOut(event.getExited().getUniqueId())) {
            if (event.getVehicle().hasMetadata("type")) {
                String metaValue = event.getVehicle().getMetadata("type").get(0).asString();
                if (metaValue.equalsIgnoreCase("mount")) {
                    event.getVehicle().remove();
                    EntityAPI.removePlayerMountList(event.getExited().getUniqueId());
                    event.getExited().sendMessage("For it's own safety, your mount has returned to the stable.");
                }
            }
        }
    }

    /**
     * Handles player leaving the server
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Chat.listenForMessage(event.getPlayer(), null, null);
        event.setQuitMessage(null);
        GuildMechanics.getInstance().doLogout(event.getPlayer());
        //Ensures the player has played at least 5 seconds before saving to the database.
        if (DatabaseAPI.getInstance().PLAYER_TIME.containsKey(event.getPlayer().getUniqueId()) && DatabaseAPI.getInstance().PLAYER_TIME.get(event.getPlayer().getUniqueId()) > 5) {
            DatabaseAPI.getInstance().PLAYER_TIME.remove(event.getPlayer().getUniqueId());
            Player player = event.getPlayer();
            // Player leaves while in duel
            if (DuelingMechanics.isDueling(player.getUniqueId())) {
                DuelingMechanics.getOffer(player.getUniqueId()).handleLogOut(player);
            }
            API.handleLogout(player.getUniqueId());

            if (EntityAPI.hasPetOut(player.getUniqueId())) {
                net.minecraft.server.v1_9_R2.Entity playerPet = EntityAPI.getPlayerPet(player.getUniqueId());
                if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(playerPet)) {
                    DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(playerPet);
                }
                if (playerPet.isAlive()) { // Safety check
                    playerPet.dead = true;
                }
                // .damageEntity(DamageSource.GENERIC, 20);
                EntityAPI.removePlayerPetList(player.getUniqueId());
            }

            if (EntityAPI.hasMountOut(player.getUniqueId())) {
                net.minecraft.server.v1_9_R2.Entity playerMount = EntityAPI.getPlayerMount(player.getUniqueId());
                if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(playerMount)) {
                    DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(playerMount);
                }
                if (playerMount.isAlive()) { // Safety check
                    if (playerMount.passengers != null) {
                        playerMount.passengers.stream().forEach(passenger -> passenger = null);
                    }
                    playerMount.dead = true;
                }
                EntityAPI.removePlayerMountList(player.getUniqueId());
            }
        }
        if (Affair.getInstance().isInParty(event.getPlayer())) {
            Affair.getInstance().removeMember(event.getPlayer(), false);
        }
    }

    /**
     * Handles players bringing up commands for a specific player.
     *
     * @param theevent
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerShiftRightClickPlayer(PlayerInteractEntityEvent theevent) {
        if (API.isPlayer(theevent.getRightClicked())) {
            if (theevent.getPlayer().isSneaking()) {
                Player playerClicked = (Player) theevent.getRightClicked();
                GUI gui = new GUI(playerClicked.getName(), 27, event -> {
                    if (event.getPosition() < 27) {
                        int slot = event.getPosition();
                        if (event.getInventory().getItem(slot) != null
                                && event.getInventory().getItem(slot).getType() != Material.AIR) {
                            ItemStack item = event.getInventory().getItem(slot);
                            // Duel Request
                            if (item.getType() == Material.IRON_SWORD) {
                                event.setWillClose(true);
                                event.setWillDestroy(true);
                                theevent.getPlayer().closeInventory();
                                Player p1 = theevent.getPlayer();
                                if (API.isNonPvPRegion(p1.getLocation()) && API.isNonPvPRegion(playerClicked.getLocation())) {
                                    DuelingMechanics.sendDuelRequest(p1, playerClicked);
                                }
                            } else if (item.getType() == Material.EMERALD) {
                                event.setWillClose(true);
                                event.setWillDestroy(true);
                                event.willDestroy();
                                TradeManager.sendTradeRequest(theevent.getPlayer().getUniqueId(), playerClicked.getUniqueId());
                            } else if (item.getType() == Material.PAPER) {
                                theevent.getPlayer().closeInventory();
                                theevent.getPlayer().chat("@i@ " + playerClicked.getName() + " ");
                            }
                        }
                    }
                }, DungeonRealms.getInstance());

                gui.setOption(4, Utils.getPlayerHead(playerClicked),
                        ChatColor.AQUA.toString() + playerClicked.getName());
                gui.setOption(8, new ItemStack(Material.IRON_SWORD), "Challenge to duel",
                        ChatColor.GRAY + "Challenges " + playerClicked.getName() + " to a battle!");
                gui.setOption(17, new ItemStack(Material.PAPER), "Private Message",
                        ChatColor.GRAY + "Privately message " + playerClicked.getName());
                gui.setOption(26, new ItemStack(Material.EMERALD), "Trade",
                        ChatColor.GRAY + "Send a trade request to " + playerClicked.getName());
                gui.open(theevent.getPlayer());
            }
        }
    }


    /**
     * Checks player movement, adds a trail of gold blocks if they have the perk
     * and the situation is correct.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!API.isPlayer(event.getPlayer()))
            return;

        if (DuelingMechanics.isDueling(event.getPlayer().getUniqueId())) {
            DuelOffer offer = DuelingMechanics.getOffer(event.getPlayer().getUniqueId());
            assert offer != null;
            if (!offer.canFight) return;
            if (event.getTo().distance(offer.centerPoint) >= 15) {
                event.setCancelled(true);
                event.getPlayer().teleport(event.getFrom());
                event.getPlayer().sendMessage(ChatColor.RED + "Can't leave area while in battle!");
            }
        }

        if (!(DonationEffects.getInstance().PLAYER_GOLD_BLOCK_TRAILS.contains(event.getPlayer())))
            return;
        Player player = event.getPlayer();
        if (!(player.getWorld().equals(Bukkit.getWorlds().get(0))))
            return;
        if (player.getLocation().getBlock().getType() != Material.AIR)
            return;
        Material material = player.getLocation().subtract(0, 1, 0).getBlock().getType();
        if (material == Material.DIRT || material == Material.GRASS || material == Material.STONE
                || material == Material.COBBLESTONE || material == Material.GRAVEL || material == Material.LOG
                || material == Material.SMOOTH_BRICK || material == Material.BEDROCK || material == Material.GLASS
                || material == Material.SANDSTONE || material == Material.SAND || material == Material.BOOKSHELF
                || material == Material.MOSSY_COBBLESTONE || material == Material.OBSIDIAN
                || material == Material.SNOW_BLOCK || material == Material.CLAY || material == Material.STAINED_CLAY
                || material == Material.WOOL) {
            DonationEffects.getInstance().PLAYER_GOLD_BLOCK_TRAIL_INFO
                    .put(player.getLocation().subtract(0, 1, 0).getBlock().getLocation(), material);
            player.getLocation().subtract(0, 1, 0).getBlock().setType(Material.GOLD_BLOCK);
            player.getLocation().subtract(0, 1, 0).getBlock().setMetadata("time",
                    new FixedMetadataValue(DungeonRealms.getInstance(), 10));
        }


    }

    /**
     * Checks player movement, if they are chaotic and entering or currently in
     * a Non-PvP zone, remove them from it.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMoveWhileChaotic(PlayerMoveEvent event) {
        if (!API.isPlayer(event.getPlayer())) {
            return;
        }
        Player player = event.getPlayer();
        if (API.getGamePlayer(player) == null) {
            return;
        }
        if (API.getGamePlayer(player).getPlayerAlignment() != KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
            return;
        }
        if (!(player.getWorld().equals(Bukkit.getWorlds().get(0)))) {
            return;
        }
        if (API.isInSafeRegion(event.getFrom()) || API.isNonPvPRegion(event.getFrom())) {
            event.setCancelled(true);
            player.teleport(KarmaHandler.CHAOTIC_RESPAWNS.get(new Random().nextInt(KarmaHandler.CHAOTIC_RESPAWNS.size() - 1)));
            player.sendMessage(ChatColor.RED + "The guards have kicked you of of this area due to your alignment");
            return;
        }
        if (API.isInSafeRegion(event.getTo()) || API.isNonPvPRegion(event.getTo())) {
            event.setCancelled(true);
            player.teleport(new Location(player.getWorld(), event.getFrom().getX(), event.getFrom().getY(), event.getFrom().getZ(), player.getLocation().getPitch() * -1, player.getLocation().getPitch() * -1));
            player.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " enter " + ChatColor.BOLD.toString() + "NON-PVP" + ChatColor.RED + " zones with a Chaotic alignment.");
        }
    }

    /**
     * Checks for player interacting with NPC Players, opens an inventory if
     * they have one.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void playerInteractWithNPC(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player))
            return;
        if (API.isPlayer(event.getRightClicked()))
            return;

        String npcNameStripped = ChatColor.stripColor(event.getRightClicked().getName());
        if (npcNameStripped.equals(""))
            return;

        if (Cooldown.hasCooldown(event.getPlayer().getUniqueId())) return;

        // AVOID DOUBLE CLICK //
        Cooldown.addCooldown(event.getPlayer().getUniqueId(), 1000L);

        if (npcNameStripped.equalsIgnoreCase("Animal Tamer")) {
            NPCMenus.openMountPurchaseMenu(event.getPlayer());
            return;
        }
        if (npcNameStripped.equalsIgnoreCase("Merchant")) {
            NPCMenus.openMerchantMenu(event.getPlayer());
            return;
        }
        if (npcNameStripped.equalsIgnoreCase("E-Cash Vendor")) {
            NPCMenus.openECashPurchaseMenu(event.getPlayer());
            return;
        }
        if (npcNameStripped.equalsIgnoreCase("Wizard")) {
            NPCMenus.openWizardMenu(event.getPlayer());
            return;
        }
        if (npcNameStripped.equalsIgnoreCase("Dungeoneer")) {
            NPCMenus.openDungeoneerMenu(event.getPlayer());
//            event.getPlayer().sendMessage(ChatColor.RED + "Sorry, I'm restocking my wares!");
            return;
        }
        if (npcNameStripped.equalsIgnoreCase("Skill Trainer")) {
            NPCMenus.openProfessionPurchaseMenu(event.getPlayer());
            return;
        }
        if (npcNameStripped.equalsIgnoreCase("Food Vendor")) {
            NPCMenus.openFoodVendorMenu(event.getPlayer());
            return;
        }
        if (npcNameStripped.equalsIgnoreCase("Item Vendor")) {
            NPCMenus.openItemVendorMenu(event.getPlayer());
            return;
        }
        if (npcNameStripped.equalsIgnoreCase("Guild Registrar")) {
            GuildMechanics.getInstance().startGuildCreationDialogue(event.getPlayer());
            return;
        }
        if (npcNameStripped.equalsIgnoreCase("Innkeeper")) {
            NPCMenus.openHearthstoneRelocateMenu(event.getPlayer());
            return;
        }
        if (npcNameStripped.equalsIgnoreCase("Ship Captain")) {
            event.getPlayer().teleport(new Location(Bukkit.getWorlds().get(0), -378, 85, 362));
            ItemManager.giveStarter(event.getPlayer());
            return;
        }
    }

    /**
     * Handle players catching fish, gives them exp/fish
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void catchFish(PlayerFishEvent event) {
        if (event.getState().equals(State.FISHING)) {
            Location loc = Fishing.getInstance().getFishingSpot(event.getPlayer().getLocation());
            if (loc == null) {
                event.getPlayer().sendMessage(ChatColor.RED + "There are " + ChatColor.UNDERLINE + "no" + ChatColor.RED + " populated fishing spots near this location.");
                event.getPlayer().sendMessage(ChatColor.GRAY + "Look for particles above water blocks to signify active fishing spots.");
                event.setCancelled(true);
            }
        } else {
            Player p = event.getPlayer();
            ItemStack stack = p.getEquipment().getItemInMainHand();
            if (stack != null && stack.getType() == Material.FISHING_ROD) {
                RepairAPI.subtractCustomDurability(p, stack, 2);
                if (event.getState() == State.CAUGHT_FISH) {
                    if (Fishing.isDRFishingPole(stack)) {
                        event.getCaught().remove();
                        event.setExpToDrop(0);
                        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
                        int tier = nms.getTag().getInt("itemTier");
                        if (new Random().nextInt(100) <= Fishing.getChance(tier)) {
                            ItemStack fish = Fishing.getFishItem(stack);
                            int experienceGain = Fishing.getFishEXP(tier);
                            Fishing.gainExp(stack, p, experienceGain);
                            if (API.getGamePlayer(event.getPlayer()) != null) {
                                API.getGamePlayer(event.getPlayer()).addExperience((experienceGain / 8), false);
                            }
                            p.getInventory().addItem(fish);
                        } else {
                            p.sendMessage("Oh no, it got away!");
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks for players quitting the merchant NPC
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCloseInventory(InventoryCloseEvent event) {
        if (!event.getInventory().getName().equalsIgnoreCase("Merchant")) {
            return;
        }
        Player player = (Player) event.getPlayer();
        if (!API.isPlayer(player)) {
            return;
        }
        int slot_Variable = -1;
        while (slot_Variable < 26) {
            slot_Variable++;
            if (!(slot_Variable == 0 || slot_Variable == 1 || slot_Variable == 2 || slot_Variable == 3 || slot_Variable == 9 || slot_Variable == 10 || slot_Variable == 11
                    || slot_Variable == 12 || slot_Variable == 18 || slot_Variable == 19 || slot_Variable == 20 || slot_Variable == 21)) {
                continue;
            }
            ItemStack itemStack = event.getInventory().getItem(slot_Variable);
            if (itemStack == null || itemStack.getType() == Material.AIR || CraftItemStack.asNMSCopy(itemStack).hasTag() && CraftItemStack.asNMSCopy(itemStack).getTag().hasKey("acceptButton") || itemStack.getType() == Material.THIN_GLASS) {
                continue;
            }
            if (itemStack.getType() == Material.EMERALD) {
                itemStack = BankMechanics.createBankNote(itemStack.getAmount());
            }
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
            } else {
                player.getInventory().setItem(player.getInventory().firstEmpty(), itemStack);
            }
        }
        player.getOpenInventory().getTopInventory().clear();
        player.updateInventory();
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCraft(CraftItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityImmunityAfterHit(EntityDamageByEntityEvent e) {
        if (e.getCause() == DamageCause.PROJECTILE) return;
        if (API.isPlayer(e.getEntity())) return;
        if (e.getEntity() instanceof LivingEntity) {
            LivingEntity ent = (LivingEntity) e.getEntity();
            ent.setMaximumNoDamageTicks(0);
            ent.setNoDamageTicks(0);
        }
    }

    /**
     * Checks for player punching a map on a wall
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerHitMap(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player && event.getEntity() instanceof ItemFrame) {
            Player player = (Player) event.getRemover();
            ItemFrame itemFrame = (ItemFrame) event.getEntity();
            if (player.getInventory().firstEmpty() != -1 && (itemFrame.getItem().getType() == Material.MAP)) {
                ItemStack map = itemFrame.getItem();
                if (!(player.getInventory().contains(map))) {
                    player.getInventory().addItem(map);
                    player.updateInventory();
                    player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1F, 0.8F);
                    Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.CARTOGRAPHER);
                }
            }
        }
    }

    /**
     * Checks for player punching a map on a wall
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerHitMapItemFrame(EntityDamageByEntityEvent event) {
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
        }
    }

    /**
     * Prevents players from shearing sheep etc.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerShearEntityEvent(PlayerShearEntityEvent event) {
        if (event.getPlayer().isOp()) {
            return;
        }
        event.setCancelled(true);
    }

    /**
     * Prevents players from dropping maps
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMapDrop(PlayerDropItemEvent event) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(event.getItemDrop().getItemStack());
        if (!(event.isCancelled())) {
            Player pl = event.getPlayer();
            // The maps gonna drop! DESTROY IT!
            if (event.getItemDrop().getItemStack().getType() == Material.MAP) {
                event.getItemDrop().remove();
                if (pl.getEquipment().getItemInMainHand().getType() == Material.MAP) {
                    pl.setItemInHand(new ItemStack(Material.AIR));
                } else if (pl.getItemOnCursor().getType() == Material.MAP) {
                    pl.setItemOnCursor(new ItemStack(Material.AIR));
                }
                pl.playSound(pl.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1F, 2F);
                pl.updateInventory();
                return;
            }
            if (nms == null || !nms.hasTag())
                return;
            if (nms.getTag().hasKey("subtype")) event.getItemDrop().remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void chunkUNload(ChunkUnloadEvent event) {
        if (event.getWorld() == Bukkit.getWorlds().get(0)) {
            //TODO: Check for better ways of doing this
            if (event.getChunk().getEntities().length > 0) {
                //SpawningMechanics.getInstance().getChunkMobBaseSpawners(event.getChunk()).stream().filter(spawner -> spawner.getSPAWNED_MONSTERS().size() > 2).forEach(spawner -> spawner.setFirstSpawn(true));
                for (Entity entity : event.getChunk().getEntities()) {
                    if (!(entity instanceof org.bukkit.entity.Item) && !(entity instanceof Player)) {
                        if (!(entity instanceof ItemFrame) && !(entity instanceof Painting) && !(entity instanceof Hanging)) {
                            entity.remove();
                        }
                    }
                }
            }
        } else if (event.getWorld().getName().contains("DUNGEON")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void chunkLoad(ChunkLoadEvent event) {
        if (event.getWorld() == Bukkit.getWorlds().get(0)) {
            if (event.getChunk().getEntities().length > 0) {
                for (Entity entity : event.getChunk().getEntities()) {
                    if (!(entity instanceof org.bukkit.entity.Item) && !(entity instanceof Player)) {
                        if (!(entity instanceof ItemFrame) && !(entity instanceof Painting) && !(entity instanceof Hanging)) {
                            entity.remove();
                        }
                    }
                }
            }
        } else if (event.getWorld().getName().contains("DUNGEON")) {
            if (event.getChunk().getEntities().length > 0) {
                for (Entity entity : event.getChunk().getEntities()) {
                    entity.remove();
                }
            }
        }
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        if (event.getItem().getItemStack().getAmount() <= 0) {
            event.setCancelled(true);
            event.getItem().remove();
            return;
            //Prevent weird MC glitch.
        }
        if (event.getItem().getItemStack().getType() == Material.ARROW) {
            event.setCancelled(true);
            event.getItem().remove();
            return;
        }
        if (event.getItem().getItemStack().getType() != Material.EMERALD) {
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void avalonTP(PlayerEnterRegionEvent event) {
        if (event.getRegion().equalsIgnoreCase("teleport_underworld")) {
            event.getPlayer().teleport(Teleportation.Underworld);
        } else if (event.getRegion().equalsIgnoreCase("teleport_overworld")) {
            event.getPlayer().teleport(Teleportation.Overworld);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void playerInteractMule(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Horse)) return;
        Horse horse = (Horse) event.getRightClicked();
        event.setCancelled(true);
        if (horse.getVariant() != Variant.MULE) return;
        if (horse.getOwner() == null) {
            horse.remove();
            return;
        }
        Player p = event.getPlayer();
        if (horse.getOwner().getUniqueId().toString().equalsIgnoreCase(event.getPlayer().getUniqueId().toString())) {
            horse.setLeashHolder(p);
            Inventory inv = MountUtils.inventories.get(p.getUniqueId());
            p.openInventory(inv);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void unLeashMule(EntityUnleashEvent event) {
        if (!(event.getEntity() instanceof Horse)) return;
        Horse horse = (Horse) event.getEntity();
        if (horse.getVariant() != Variant.MULE) return;
        if (!event.getReason().equals(UnleashReason.PLAYER_UNLEASH)) {
            horse.remove();
            return;
        }
        if (horse.getOwner() == null) {
            horse.remove();
            return;
        }
        horse.setLeashHolder((Player) horse.getOwner());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void chickenLayEgg(ItemSpawnEvent event) {
        if (event.getEntityType() != EntityType.EGG) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDragonEggMove(BlockFromToEvent event) {
        if (event.getBlock().getType() == Material.DRAGON_EGG) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void entityTarget(EntityTargetEvent event) {
        if (event.getTarget() != null) {
            if (!(event.getTarget() instanceof Player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void explosionDungeon(EntityExplodeEvent event) {
        if (event.getEntity().getWorld().getName().contains("DUNGEON")) {
            event.blockList().stream().forEach(block -> block.setType(Material.AIR));
            event.setYield(0);
            event.blockList().clear();
            event.getEntity().remove();
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void characterJournalPartyInvnite(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            if (!API.isPlayer(event.getEntity())) return;
            if (((Player) event.getDamager()).getEquipment().getItemInMainHand() != null) {
                ItemStack stack = ((Player) event.getDamager()).getEquipment().getItemInMainHand();
                if (stack.getType() == Material.WRITTEN_BOOK) {
                    event.setCancelled(true);
                    event.setDamage(0);
                    Player player = (Player) event.getDamager();
                    Player invite = (Player) event.getEntity();
                    if (Affair.getInstance().isInParty(invite)) {
                        player.sendMessage(ChatColor.RED + "That player is already in a party!");
                    } else {
                        if (Affair.getInstance().isInParty(player)) {
                            if (Affair.getInstance().isOwner(player)) {
                                if (Affair.getInstance().getParty(player).get().getMembers().size() >= 7) {
                                    player.sendMessage(ChatColor.RED + "Your party has reached the max player count!");
                                    return;
                                }
                                Affair.getInstance().invitePlayer(invite, player);
                            } else {
                                player.sendMessage(new String[]{
                                        ChatColor.RED + "You are NOT the leader of your party.",
                                        ChatColor.GRAY + "Type " + ChatColor.BOLD + "/pquit" + ChatColor.GRAY + " to quit your current party."
                                });
                            }
                        } else {
                            Affair.getInstance().createParty(player);
                            Affair.getInstance().invitePlayer(invite, player);
                        }
                    }
                }
            }
        }
    }
}
