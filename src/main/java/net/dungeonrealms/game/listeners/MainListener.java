package net.dungeonrealms.game.listeners;

import com.connorlinfoot.bountifulapi.BountifulAPI;
import com.oracle.jrockit.jfr.EventDefinition;
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
import net.dungeonrealms.game.mechanics.PlayerManager;
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
import net.dungeonrealms.game.player.trade.Trade;
import net.dungeonrealms.game.player.trade.TradeManager;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.world.entities.utils.EntityAPI;
import net.dungeonrealms.game.world.entities.utils.MountUtils;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.items.repairing.RepairAPI;
import net.dungeonrealms.game.world.party.Affair;
import net.dungeonrealms.game.world.realms.Realms;
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
                    event.getExited().sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "For it's own safety, your mount has returned to the stable.");
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
        Realms.getInstance().doLogout(event.getPlayer());

        //Ensures the player has played at least 5 seconds before saving to the database.
        if (DatabaseAPI.getInstance().PLAYER_TIME.containsKey(event.getPlayer().getUniqueId()) && DatabaseAPI.getInstance().PLAYER_TIME.get(event.getPlayer().getUniqueId()) > 5) {
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
            if (API.getRegionName(event.getRightClicked().getLocation()).contains("tutorial")) {
                event.getPlayer().teleport(new Location(Bukkit.getWorlds().get(0), -378, 85, 362));
                ItemManager.giveStarter(event.getPlayer());
            }
        }
    }


    @EventHandler
    public void onPlayerFish(PlayerFishEvent e) {
        final Player pl = e.getPlayer();
        e.setExpToDrop(0);

        if (e.getCaught() != null)
            e.getCaught().remove();
        if (!(Fishing.isDRFishingPole(pl.getEquipment().getItemInMainHand()))) {
            e.setCancelled(true);
            return; // Get out of here.
        }

        if (e.getState().equals(State.FISHING)) {
            Location loc = Fishing.getInstance().getFishingSpot(e.getPlayer().getLocation());
            if (loc == null) {
                e.getPlayer().sendMessage(ChatColor.RED + "There are " + ChatColor.UNDERLINE + "no" + ChatColor.RED + " populated fishing spots near this location.");
                e.getPlayer().sendMessage(ChatColor.GRAY + "Look for particles above water blocks to signify active fishing spots.");
                e.setCancelled(true);
            }
        }

        if (e.getState() == State.FAILED_ATTEMPT || e.getState() == State.CAUGHT_ENTITY) {
            RepairAPI.subtractCustomDurability(pl, pl.getEquipment().getItemInMainHand(), 1);
        } else if (e.getState() == State.CAUGHT_FISH) {
            RepairAPI.subtractCustomDurability(pl, pl.getEquipment().getItemInMainHand(), 2);
        }

        if (e.getState() == State.CAUGHT_FISH) {
            final Location fish_loc = Fishing.getInstance().getFishingSpot(pl.getLocation());
            final int spot_tier = Fishing.getInstance().getFishingSpotTier(pl.getLocation());
            if (e.getCaught() != null)
                e.getCaught().remove();

            if (fish_loc == null || spot_tier == -1) {
                pl.sendMessage(ChatColor.RED + "You must be near a Fishing Location to catch fish!");
                return;
            }

            pl.sendMessage(ChatColor.GRAY + "You examine your catch... ");
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                int do_i_get_fish = new Random().nextInt(100);

                int item_tier = Fishing.getRodTier(pl.getEquipment().getItemInMainHand());
                int success_rate = 0;

                if (item_tier > spot_tier) {
                    success_rate = 100;
                }
                if (item_tier == spot_tier) {
                    int lvl = CraftItemStack.asNMSCopy(pl.getEquipment().getItemInMainHand()).getTag().getInt("level");
                    success_rate = 50 + (2 * (20 - Math.abs((Fishing.getNextLevelUp(item_tier) - lvl))));
                }

                int success_mod = Fishing.getSuccessChance(pl.getEquipment().getItemInMainHand());
                success_rate += success_mod; // %CHANCE

                if (success_rate <= do_i_get_fish) {
                    pl.sendMessage(ChatColor.RED + "It got away..");
                    return;
                }

                if (Fishing.isDRFishingPole(pl.getEquipment().getItemInMainHand())) {
                    // They get fish!
                    ItemStack fish = Fishing.getFishDrop(spot_tier);
                    if (pl.getInventory().firstEmpty() != -1) {
                        pl.getInventory().setItem(pl.getInventory().firstEmpty(), fish);
                    } else {
                        // Full inventory!
                        pl.getWorld().dropItem(pl.getLocation(), fish);
                    }
                    pl.sendMessage(ChatColor.GREEN + "... you caught some " + fish.getItemMeta().getDisplayName() + ChatColor.GREEN + "!");

                    int exp = Fishing.getFishEXP(Fishing.getFishTier(fish));
                    Fishing.gainExp(pl.getEquipment().getItemInMainHand(), pl, exp);
                    GamePlayer gamePlayer = API.getGamePlayer(pl);
                    if (gamePlayer == null) return;
                    gamePlayer.addExperience(exp / 8, false);
                    gamePlayer.getPlayerStatistics().setFishCaught(gamePlayer.getPlayerStatistics().getFishCaught() + 1);
                    int doi_double_drop = new Random().nextInt(100) + 1;
                    if (Fishing.getDoubleDropChance(pl.getEquipment().getItemInMainHand()) >= doi_double_drop) {
                        fish = Fishing.getFishDrop(spot_tier);
                        if (pl.getInventory().firstEmpty() != -1) {
                            pl.getInventory().setItem(pl.getInventory().firstEmpty(), fish);
                        } else {
                            // Full inventory!
                            pl.getWorld().dropItem(pl.getLocation(), fish);
                        }
                        if ((boolean) DatabaseAPI.getInstance().getData(PlayerManager.PlayerToggles.DEBUG.getDbField(), pl.getUniqueId())) {
                            pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "          DOUBLE FISH CATCH" + ChatColor.YELLOW + " (2x)");
                        }
                    }

                    int doi_triple_drop = new Random().nextInt(100) + 1;
                    if (Fishing.getTripleDropChance(pl.getEquipment().getItemInMainHand()) >= doi_triple_drop) {
                        fish = Fishing.getFishDrop(spot_tier);
                        if (pl.getInventory().firstEmpty() != -1) {
                            pl.getInventory().setItem(pl.getInventory().firstEmpty(), fish);
                        } else {
                            // Full inventory!
                            pl.getWorld().dropItem(pl.getLocation(), fish);
                        }

                        fish = Fishing.getFishDrop(spot_tier);
                        if (pl.getInventory().firstEmpty() != -1) {
                            pl.getInventory().setItem(pl.getInventory().firstEmpty(), fish);
                        } else {
                            // Full inventory!
                            pl.getWorld().dropItem(pl.getLocation(), fish);
                        }
                        if ((boolean) DatabaseAPI.getInstance().getData(PlayerManager.PlayerToggles.DEBUG.getDbField(), pl.getUniqueId())) {
                            pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "          TRIPLE FISH CATCH" + ChatColor.YELLOW + " (3x)");
                        }
                    }

                    int junk_chance = Fishing.getJunkFindChance(pl.getEquipment().getItemInMainHand());
                    if (junk_chance >= (new Random().nextInt(100) + 1)) {
                        int junk_type = new Random().nextInt(100) + 1; // 0, 1, 2
                        ItemStack junk = null;
                        if (junk_type > 70 && junk_type < 95) {
                            if (spot_tier == 1) {
                                junk = ItemManager.createHealthPotion(1, false, false);
                                junk.setAmount(5 + new Random().nextInt(3));
                            }
                            if (spot_tier == 2) {
                                junk = ItemManager.createHealthPotion(2, false, false);
                                junk.setAmount(4 + new Random().nextInt(3));
                            }
                            if (spot_tier == 3) {
                                junk = ItemManager.createHealthPotion(3, false, false);
                                junk.setAmount(2 + new Random().nextInt(3));
                            }
                            if (spot_tier == 4) {
                                junk = ItemManager.createHealthPotion(4, false, false);
                                junk.setAmount(1 + new Random().nextInt(3));
                            }
                            if (spot_tier == 5) {
                                junk = ItemManager.createHealthPotion(5, false, false);
                                junk.setAmount(1 + new Random().nextInt(3));
                            }
                        }

                        if (junk_type >= 95) {
                            if (spot_tier == 1) {
                                junk = ItemManager.createArmorScrap(1);
                                junk.setAmount(20 + new Random().nextInt(7));
                            }
                            if (spot_tier == 2) {
                                junk = ItemManager.createArmorScrap(2);
                                junk.setAmount(15 + new Random().nextInt(7));
                            }
                            if (spot_tier == 3) {
                                junk = ItemManager.createArmorScrap(3);
                                junk.setAmount(10 + new Random().nextInt(7));
                            }
                            if (spot_tier == 4) {
                                junk = ItemManager.createArmorScrap(4);
                                junk.setAmount(5 + new Random().nextInt(7));
                            }
                            if (spot_tier == 5) {
                                junk = ItemManager.createArmorScrap(5);
                                junk.setAmount(2 + new Random().nextInt(6));
                            }
                        }

                        if (junk != null) {
                            int item_count = junk.getAmount();
                            if (junk.getType() == Material.POTION) {
                                // Not stackable.
                                int amount = junk.getAmount();
                                ItemStack single_junk = junk;
                                single_junk.setAmount(1);
                                while (amount > 0) {
                                    amount--;
                                    if (pl.getInventory().firstEmpty() != -1) {
                                        pl.getInventory().setItem(pl.getInventory().firstEmpty(), single_junk);
                                    } else {
                                        // Full inventory!
                                        pl.getWorld().dropItem(pl.getLocation(), single_junk);
                                    }
                                }
                            } else {
                                if (pl.getInventory().firstEmpty() != -1) {
                                    pl.getInventory().setItem(pl.getInventory().firstEmpty(), junk);
                                } else {
                                    // Full inventory!
                                    pl.getWorld().dropItem(pl.getLocation(), junk);
                                }
                            }

                            pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "  YOU FOUND SOME JUNK! -- " + item_count + "x "
                                    + junk.getItemMeta().getDisplayName());
                        }
                    }

                    int treasure_chance = Fishing.getTreasureFindChance(pl.getEquipment().getItemInMainHand());
                    if (treasure_chance >= (new Random().nextInt(300) + 1)) {
                        // Give em treasure!
                        int treasure_type = new Random().nextInt(3); // 0, 1
                        ItemStack treasure = null;
                        if (treasure_type == 0) {
                            // OOA
                            treasure = CraftItemStack.asCraftCopy(ItemManager.createOrbofAlteration());
                        }

                        if (treasure != null) {

                            if (pl.getInventory().firstEmpty() != -1) {
                                pl.getInventory().setItem(pl.getInventory().firstEmpty(), treasure);
                            } else {
                                // Full inventory!
                                pl.getWorld().dropItem(pl.getLocation(), treasure);
                            }

                            pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "  YOU FOUND SOME TREASURE! -- a(n) "
                                    + treasure.getItemMeta().getDisplayName());
                        }
                    }
                }
            }, 10l);
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
                    pl.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
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


    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerAttemptTrade(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;
        if (!API.isItemDroppable(event.getItemDrop().getItemStack())) return;
        if (!API.isItemTradeable(event.getItemDrop().getItemStack())) return;
        if (API.isItemSoulbound(event.getItemDrop().getItemStack())) return;
        Player pl = event.getPlayer();

        Player trader = TradeManager.getTarget(pl);
        if (trader == null) {
            return;
        }
        if (!TradeManager.canTrade(trader.getUniqueId())) {
            event.setCancelled(true);
            pl.sendMessage(ChatColor.YELLOW + trader.getName() + " is currently busy.");
            return;
        }
        if (CombatLog.isInCombat(pl)) {
            pl.sendMessage(ChatColor.YELLOW + "You cannot trade while in combat.");
            pl.sendMessage(ChatColor.GRAY + "Wait " + ChatColor.BOLD + "a few seconds" + ChatColor.GRAY + " and try again.");
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        TradeManager.startTrade(pl, trader);
        Trade trade = TradeManager.getTrade(pl.getUniqueId());
        if (trade == null) {
            return;
        }
        ItemStack item = pl.getInventory().getItemInMainHand();
        trader.playSound(trader.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1F, 0.8F);
        pl.playSound(pl.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1F, 0.8F);
        trade.inv.addItem(item.clone());
        event.getPlayer().getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void chunkUnload(ChunkUnloadEvent event) {
        if (event.getWorld() == Bukkit.getWorlds().get(0)) {
            if (event.getChunk().getEntities().length > 0) {
                for (Entity entity : event.getChunk().getEntities()) {
                    if (!(entity instanceof Item) && !(entity instanceof Player)) {
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
            } else if (Rank.isGM((Player) event.getTarget())) {
                //TODO: Check for /allowfight @alan.
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
    public void characterJournalPartyInvite(EntityDamageByEntityEvent event) {
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
