package net.dungeonrealms.game.listener.world;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.type.EnumData;
import net.dungeonrealms.game.events.PlayerEnterRegionEvent;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.mechanics.DungeonManager;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.world.entities.Entities;
import net.dungeonrealms.game.world.entities.types.EnderCrystal;
import net.dungeonrealms.game.world.entities.utils.EntityAPI;
import net.dungeonrealms.game.world.party.Affair;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Created by Kieran Quigley (Proxying) on 15-Jun-16.
 */
public class DungeonListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void T1Death(EntityDeathEvent event) {
        if (!event.getEntity().getWorld().getName().contains("DUNGEON")) return;
        if (event.getEntity() instanceof Player) return;
        if (DungeonManager.getInstance().getDungeon(event.getEntity().getWorld()) == null) return;
        if (DungeonManager.getInstance().getDungeon(event.getEntity().getWorld()).getType() != DungeonManager.DungeonType.BANDIT_TROVE)
            return;
        if (event.getEntity().hasMetadata("elite")) {
            if (event.getEntity().hasMetadata("customname")) {
                String name = ChatColor.stripColor(event.getEntity().getMetadata("customname").get(0).asString());
                if (name.equalsIgnoreCase("Mad Bandit Pyromancer")) {
                    ItemStack key = ItemManager.createItem(Material.GLOWSTONE_DUST, ChatColor.GREEN + "Magical Dust", new String[]{
                            ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "A strange substance that animates objects.", ChatColor.RED + "Dungeon Item"});
                    if (event.getEntity().getKiller() != null) {
                        event.getEntity().getKiller().getInventory().addItem(key);
                    } else {
                        event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation().add(0, 1, 0), key);
                    }
                    for (Player player : GameAPI.getNearbyPlayers(event.getEntity().getLocation(), 30)) {
                        player.sendMessage(ChatColor.RED + event.getEntity().getMetadata("customname").get(0).asString() + ChatColor.WHITE + ": " + ChatColor.WHITE + "Talk about going out with a...blast.");
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void T3Death(EntityDeathEvent event) {
        if (!event.getEntity().getWorld().getName().contains("DUNGEON")) return;
        if (event.getEntity() instanceof Player) return;
        if (DungeonManager.getInstance().getDungeon(event.getEntity().getWorld()) == null) return;
        if (DungeonManager.getInstance().getDungeon(event.getEntity().getWorld()).getType() != DungeonManager.DungeonType.VARENGLADE)
            return;
        DungeonManager.DungeonObject dungeonObject = DungeonManager.getInstance().getDungeon(event.getEntity().getWorld());
        if (dungeonObject.keysDropped <= 10 && !dungeonObject.hasBossSpawned) {
            if (event.getEntity().hasMetadata("customname")) {
                String name = ChatColor.stripColor(event.getEntity().getMetadata("customname").get(0).asString());
                if (name.equalsIgnoreCase("The Priest")) {
                    ItemStack key = ItemManager.createItem(Material.TRIPWIRE_HOOK, ChatColor.LIGHT_PURPLE + "A mystical key", new String[]{
                            ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "One of four mysterious keys.", ChatColor.RED + "Dungeon Item"});
                    key.setAmount(2);
                    if (event.getEntity().getKiller() != null) {
                        event.getEntity().getKiller().getInventory().addItem(key);
                    } else {
                        event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation().add(0, 2, 0), key);
                    }
                    dungeonObject.keysDropped = dungeonObject.keysDropped + 2;
                    return;
                }
            }
            if (new Random().nextInt(20) <= 14) {
                ItemStack key = ItemManager.createItem(Material.TRIPWIRE_HOOK, ChatColor.LIGHT_PURPLE + "A mystical key", new String[]{
                        ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "One of four mysterious keys.", ChatColor.RED + "Dungeon Item"});
                if (event.getEntity().getKiller() != null) {
                    event.getEntity().getKiller().getInventory().addItem(key);
                } else {
                    event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation().add(0, 2, 0), key);
                }
                dungeonObject.keysDropped = dungeonObject.keysDropped + 1;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void T4Death(EntityDeathEvent event) {
        if (!event.getEntity().getWorld().getName().contains("DUNGEON")) return;
        if (event.getEntity() instanceof Player) return;
        if (DungeonManager.getInstance().getDungeon(event.getEntity().getWorld()) == null) return;
        if (DungeonManager.getInstance().getDungeon(event.getEntity().getWorld()).getType() != DungeonManager.DungeonType.THE_INFERNAL_ABYSS)
            return;
        if (event.getEntity().hasMetadata("customname")) {
            String name = ChatColor.stripColor(event.getEntity().getMetadata("customname").get(0).asString());
            if (event.getEntity().getType() == EntityType.ENDERMAN || name.equalsIgnoreCase("The Devastator") || name.equalsIgnoreCase("The Annihilator")) {
                if (name.equalsIgnoreCase("The Devastator")) {
                    ItemStack key = ItemManager.createItem(Material.TRIPWIRE_HOOK, ChatColor.GREEN + "Doorkey A", new String[]{
                            ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "A key required in the Infernal Abyss Dungeon", ChatColor.RED + "Dungeon Item"});
                    if (event.getEntity().getKiller() != null) {
                        event.getEntity().getKiller().getInventory().addItem(key);
                    } else {
                        event.getEntity().getWorld().dropItemNaturally(new Location(event.getEntity().getWorld(), event.getEntity().getLocation().getX(), event.getEntity().getLocation().getY(), -event.getEntity().getLocation().getZ()), key);
                    }
                } else if (name.equalsIgnoreCase("The Annihilator")) {
                    ItemStack key = ItemManager.createItem(Material.TRIPWIRE_HOOK, ChatColor.GREEN + "Doorkey B", new String[]{
                            ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "A key required in the Infernal Abyss Dungeon", ChatColor.RED + "Dungeon Item"});
                    if (event.getEntity().getKiller() != null) {
                        event.getEntity().getKiller().getInventory().addItem(key);
                    } else {
                        event.getEntity().getWorld().dropItemNaturally(new Location(event.getEntity().getWorld(), event.getEntity().getLocation().getX(), event.getEntity().getLocation().getY(), -event.getEntity().getLocation().getZ()), key);
                    }
                }
                for (Player player : event.getEntity().getWorld().getPlayers()) {
                    player.removePotionEffect(PotionEffectType.WITHER);
                }
                DungeonManager.getInstance().getDungeon_Wither_Effect().remove(event.getEntity().getWorld().getName());
            } else if (name.equalsIgnoreCase("Fire Lord Of The Abyss")) {
                ItemStack key = ItemManager.createItem(Material.BLAZE_POWDER, ChatColor.RED + "A Heart of Fire", new String[]{
                        ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "An ingredient taken from a Fire Lord", ChatColor.RED + "Dungeon Item"});
                if (event.getEntity().getKiller() != null) {
                    event.getEntity().getKiller().getInventory().addItem(key);
                } else {
                    event.getEntity().getWorld().dropItemNaturally(new Location(event.getEntity().getWorld(), event.getEntity().getLocation().getX(), event.getEntity().getLocation().getY(), -event.getEntity().getLocation().getZ()), key);
                }
            } else if (name.equalsIgnoreCase("Ice Lord Of The Abyss")) {
                ItemStack key = ItemManager.createItem(Material.ICE, ChatColor.BLUE + "A Heart of Ice", new String[]{
                        ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "An ingredient taken from an Ice Lord", ChatColor.RED + "Dungeon Item"});
                if (event.getEntity().getKiller() != null) {
                    event.getEntity().getKiller().getInventory().addItem(key);
                } else {
                    event.getEntity().getWorld().dropItemNaturally(new Location(event.getEntity().getWorld(), event.getEntity().getLocation().getX(), event.getEntity().getLocation().getY(), -event.getEntity().getLocation().getZ()), key);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void playerCraftT4(InventoryClickEvent event) {
        if (!event.getWhoClicked().getWorld().getName().contains("DUNGEON")) return;
        if (DungeonManager.getInstance().getDungeon(event.getWhoClicked().getWorld()) == null) return;
        if (DungeonManager.getInstance().getDungeon(event.getWhoClicked().getWorld()).getType() != DungeonManager.DungeonType.THE_INFERNAL_ABYSS)
            return;
        if (event.getCursor() == null) return;
        if (event.getCurrentItem() == null) return;
        if (!event.getInventory().getName().equalsIgnoreCase("container.crafting")) return;
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) return;
        ItemStack cursorItem = event.getCursor();
        ItemStack slotItem = event.getCurrentItem();
        DungeonManager.DungeonObject dungeonObject = DungeonManager.getInstance().getDungeon(event.getWhoClicked().getWorld());
        if (slotItem.getType() == Material.BLAZE_POWDER) {
            //Dragging Heart of Ice onto Heart of Fire
            if (cursorItem.getType() == Material.ICE) {
                if (slotItem.hasItemMeta() && cursorItem.hasItemMeta()) {
                    if (DungeonManager.getInstance().isDungeonItem(slotItem) && DungeonManager.getInstance().isDungeonItem(cursorItem)) {
                        if (!dungeonObject.canSpawnBoss) {
                            int percentToKill = (int) (dungeonObject.maxAlive * 0.80);
                            int killed = dungeonObject.killed;
                            event.getWhoClicked().sendMessage(ChatColor.RED + "You need to kill " + ChatColor.UNDERLINE + (percentToKill - killed) + ChatColor.RED + " monsters to create this item.");
                            return;
                        }
                        event.setCancelled(true);
                        slotItem = ItemManager.createItem(Material.FIREBALL, ChatColor.LIGHT_PURPLE + "The Inferno Seal", new String[]{
                                ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "A demonic aura surrounds the relic.", ChatColor.RED + "Dungeon Item"});
                        event.setCursor(null);
                        event.setCurrentItem(slotItem);
                    }
                }
            }
        } else if (slotItem.getType() == Material.ICE) {
            //Dragging Heart of Fire onto Heart of ICE
            if (cursorItem.getType() == Material.BLAZE_POWDER) {
                if (slotItem.hasItemMeta() && cursorItem.hasItemMeta()) {
                    if (DungeonManager.getInstance().isDungeonItem(slotItem) && DungeonManager.getInstance().isDungeonItem(cursorItem)) {
                        if (!dungeonObject.canSpawnBoss) {
                            int percentToKill = (int) (dungeonObject.maxAlive * 0.80);
                            int killed = dungeonObject.killed;
                            event.getWhoClicked().sendMessage(ChatColor.RED + "You need to kill " + ChatColor.UNDERLINE + (percentToKill - killed) + ChatColor.RED + " monsters to create this item.");
                            return;
                        }
                        event.setCancelled(true);
                        slotItem = ItemManager.createItem(Material.FIREBALL, ChatColor.LIGHT_PURPLE + "The Inferno Seal", new String[]{
                                ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "A demonic aura surrounds the relic.", ChatColor.RED + "Dungeon Item"});
                        event.setCursor(null);
                        event.setCurrentItem(slotItem);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerAttemptDungeonExit(PlayerEnterRegionEvent event) {
        if (!event.getPlayer().getWorld().getName().contains("DUNGEON")) return;
        if (event.getRegion().toLowerCase().startsWith("exit_instance")) {
            Player player = event.getPlayer();
            if (player.isOnline() && GameAPI.getGamePlayer(player) != null) {
                if (GameAPI.getGamePlayer(player).isInDungeon()) {
                    if (!DatabaseAPI.getInstance().getData(EnumData.CURRENT_LOCATION, player.getUniqueId()).equals("")) {
                        String[] locationString = String.valueOf(DatabaseAPI.getInstance().getData(EnumData.CURRENT_LOCATION, player.getUniqueId())).split(",");
                        player.teleport(new Location(Bukkit.getWorlds().get(0), Double.parseDouble(locationString[0]), Double.parseDouble(locationString[1]), Double.parseDouble(locationString[2]), Float.parseFloat(locationString[3]), Float.parseFloat(locationString[4])));
                    } else {
                        player.teleport(Teleportation.Cyrennica);
                    }
                }
            }
            if (Affair.getInstance().isInParty(player)) {
                List<Player> toSend = new ArrayList<>();
                Affair.AffairO party = Affair.getInstance().getParty(player).get();
                toSend.addAll(party.getMembers());
                toSend.add(party.getOwner());
                for (Player player1 : toSend) {
                    if (player.getName().equals(player1.getName())) {
                        continue;
                    }
                    player1.sendMessage(ChatColor.LIGHT_PURPLE.toString() + "<" + ChatColor.BOLD + "P" + ChatColor.LIGHT_PURPLE + ">" + ChatColor.GRAY + " "
                            + player.getName() + " has " + ChatColor.RED + ChatColor.UNDERLINE + "left" + ChatColor.GRAY + " the dungeon.");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerAttemptDungeonEnter(PlayerEnterRegionEvent event) {
        if (!event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;
        if (event.getRegion().toLowerCase().startsWith("instance_")) {
            Player player = event.getPlayer();
            if (DungeonManager.getInstance().getPlayers_Entering_Dungeon().containsKey(player.getName())) {
                player.sendMessage(ChatColor.GRAY + "You currently have a Dungeon cooldown timer, please wait " + ChatColor.RED + ChatColor.UNDERLINE
                        + DungeonManager.getInstance().getPlayers_Entering_Dungeon().get(player.getName()) + "s" + ChatColor.RESET + ChatColor.RED + " before entering.");
                return;
            }
            if (EntityAPI.hasPetOut(event.getPlayer().getUniqueId())) {
                net.minecraft.server.v1_9_R2.Entity pet = Entities.PLAYER_PETS.get(event.getPlayer().getUniqueId());
                pet.dead = true;
                EntityAPI.removePlayerPetList(event.getPlayer().getUniqueId());
            }
            if (EntityAPI.hasMountOut(event.getPlayer().getUniqueId())) {
                net.minecraft.server.v1_9_R2.Entity mount = Entities.PLAYER_MOUNTS.get(event.getPlayer().getUniqueId());
                mount.dead = true;
                EntityAPI.removePlayerMountList(event.getPlayer().getUniqueId());
            }
//            if (GameAPI.getGamePlayer(player) == null || GameAPI.getGamePlayer(player).getLevel() < 5) {
//                player.sendMessage(ChatColor.RED + "You need to be " + ChatColor.UNDERLINE + "at least" + ChatColor.RED + " level 5 to enter a dungeon.");
//                return;
//            }
            String dungeonName = event.getRegion().substring(event.getRegion().indexOf("_") + 1, event.getRegion().length());
            if (dungeonName.equalsIgnoreCase("dodungeon")) {
                dungeonName = "DODungeon";
            }
            if (dungeonName.equalsIgnoreCase("t1dungeon")) {
                dungeonName = "T1Dungeon";
            }

            boolean isPartyInInstance = false;

            if (Affair.getInstance().isInParty(player)) {
                Affair.AffairO party = Affair.getInstance().getParty(player).get();
                List<Player> partyMembers = new ArrayList<>();
                partyMembers.add(party.getOwner());
                partyMembers.addAll(party.getMembers());
                DungeonManager.DungeonObject partyDungeon = null;
                for (Player player1 : partyMembers) {
                    if (player1.getWorld().getName().contains("DUNGEON")) {
                        partyDungeon = DungeonManager.getInstance().getDungeon(player1.getWorld());
                        isPartyInInstance = true;
                        break;
                    }
                }
                if (isPartyInInstance) {
                    if (partyDungeon != null) {
                        switch (partyDungeon.getType()) {
                            case BANDIT_TROVE:
                                if (!dungeonName.equalsIgnoreCase("T1Dungeon")) {
                                    player.sendMessage(ChatColor.RED + "Your party is already inside a " + ChatColor.UNDERLINE + "different" + ChatColor.RED + " instanced dungeon.");
                                    player.sendMessage(ChatColor.GRAY + "You'll need to either leave your current party or wait for them to finish their run.");
                                    return;
                                }
                                break;
                            case VARENGLADE:
                                if (!dungeonName.equalsIgnoreCase("DODungeon")) {
                                    player.sendMessage(ChatColor.RED + "Your party is already inside a " + ChatColor.UNDERLINE + "different" + ChatColor.RED + " instanced dungeon.");
                                    player.sendMessage(ChatColor.GRAY + "You'll need to either leave your current party or wait for them to finish their run.");
                                    return;
                                }
                                break;
                            case THE_INFERNAL_ABYSS:
                                if (!dungeonName.equalsIgnoreCase("fireydungeon")) {
                                    player.sendMessage(ChatColor.RED + "Your party is already inside a " + ChatColor.UNDERLINE + "different" + ChatColor.RED + " instanced dungeon.");
                                    player.sendMessage(ChatColor.GRAY + "You'll need to either leave your current party or wait for them to finish their run.");
                                    return;
                                }
                                break;
                            default:
                                return;
                        }
                        if (!partyDungeon.getPlayerList().containsKey(player)) {
                            if (partyDungeon.getTime() > 600) {
                                player.sendMessage(ChatColor.RED + "This Dungeon was created before you joined the party, you cannot join this session.");
                                return;
                            } else {
                                partyDungeon.getPlayerList().put(player, true);
                            }
                        }
                        boolean hasTeleported = false;
                        DungeonManager.getInstance().getPlayers_Entering_Dungeon().put(player.getName(), 100);
                        for (Player player1 : partyMembers) {
                            if (player.getName().equals(player1.getName())) {
                                continue;
                            }
                            if (player1.getWorld().getName().contains("DUNGEON")) {
                                player1.sendMessage(ChatColor.LIGHT_PURPLE + "<" + ChatColor.BOLD + "P" + ChatColor.LIGHT_PURPLE + ">" + ChatColor.GRAY
                                        + " " + player.getName() + " has " + ChatColor.GREEN + ChatColor.UNDERLINE + "joined" + ChatColor.GRAY
                                        + " the dungeon.");
                                if (!hasTeleported) {
                                    player.teleport(player1.getLocation());
                                    player.setFallDistance(0F);
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                                        DungeonManager.getInstance().sendWorldEnvironment(player, World.Environment.NETHER);
                                    }, 5L);
                                    hasTeleported = true;
                                }
                            }
                        }
                        return;
                    }
                    return;
                }
                if (!(Affair.getInstance().getParty(player).get()).getOwner().getName().equals(player.getName())) {
                    player.sendMessage(ChatColor.RED + "You are " + ChatColor.UNDERLINE + "NOT" + ChatColor.RED + " the party leader.");
                    player.sendMessage(ChatColor.GRAY + "Only the party leader can start a new dungeon instance.");
                    return;
                }
            } else {
                Affair.getInstance().createParty(player);
            }

            if (!DungeonManager.getInstance().canCreateInstance()) {
                player.sendMessage(ChatColor.RED + "All available dungeon instances are " + ChatColor.UNDERLINE + "full" + ChatColor.RED
                        + " on this shard.");
                player.sendMessage(ChatColor.GRAY + "Use /shard to try a different one.");
                return;
            }

            Map<Player, Boolean> partyList = new HashMap<>();
            for (Player player1 : Affair.getInstance().getParty(player).get().getMembers()) {
                if (player1.getLocation().distanceSquared(player.getLocation()) <= 200) {
                    DungeonManager.getInstance().getPlayers_Entering_Dungeon().put(player1.getName(), 100);
                    partyList.put(player1, true);
                } else {
                    partyList.put(player1, false);
                }
            }
            partyList.put(player, true);
            DungeonManager.DungeonType dungeonType;
            if (dungeonName.equalsIgnoreCase("T1Dungeon")) {
                dungeonType = DungeonManager.DungeonType.BANDIT_TROVE;
            } else if (dungeonName.equalsIgnoreCase("DODungeon")) {
                dungeonType = DungeonManager.DungeonType.VARENGLADE;
            } else if (dungeonName.equalsIgnoreCase("fireydungeon")) {
                dungeonType = DungeonManager.DungeonType.THE_INFERNAL_ABYSS;
            } else {
                dungeonType = null;
            }
            if (dungeonType == null) return;
            DungeonManager.getInstance().getPlayers_Entering_Dungeon().put(player.getName(), 100);
            DungeonManager.getInstance().createNewInstance(dungeonType, partyList, dungeonName);
            player.sendMessage(ChatColor.GRAY + "Loading Instance: '" + ChatColor.UNDERLINE + dungeonType.name().replaceAll("_", " ") + ChatColor.GRAY
                    + "' -- Please wait...");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerHitEndercrystal(EntityDamageEvent event) {
        if (event.getEntity().getType() != EntityType.ENDER_CRYSTAL) return;
        if (!event.getEntity().getWorld().getName().contains("DUNGEON")) return;
        DungeonManager.DungeonObject dungeonObject = DungeonManager.getInstance().getDungeon(event.getEntity().getWorld());
        if (dungeonObject.getType() != DungeonManager.DungeonType.THE_INFERNAL_ABYSS) return;
        event.setCancelled(true);
        event.setDamage(0);

        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event;
            if (entityDamageByEntityEvent.getDamager() instanceof Player) {
                Player player = (Player) entityDamageByEntityEvent.getDamager();
                Block block = event.getEntity().getLocation().subtract(0D, 1D, 0D).getBlock();
                if (block.getType() == Material.BEDROCK) {
                    block.setType(Material.AIR);
                    block.getLocation().add(0, 1, 0).getBlock().setType(Material.AIR);
                }
                try {
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.MAGIC_CRIT, block.getLocation().add(0, 1, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 1F, 50);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                DungeonManager.getInstance().getDungeon_Wither_Effect().put(player.getWorld().getName(), 90);

                for (Player player1 : player.getWorld().getPlayers()) {
                    player1.removePotionEffect(PotionEffectType.WITHER);
                    player1.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, (int) (90 * 20L), 0));
                    player1.playSound(player1.getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 5F, 1.5F);
                    player1.sendMessage(ChatColor.YELLOW + "Debuff timer refreshed, " + ChatColor.UNDERLINE + HealthHandler.getInstance().getPlayerMaxHPLive(player1)
                            + " DMG " + ChatColor.YELLOW + "will be inflicted in 90s unless another beacon is activated.");
                }
                event.getEntity().remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (!event.getPlayer().getWorld().getName().contains("DUNGEON")) return;
        DungeonManager.DungeonObject dungeonObject = DungeonManager.getInstance().getDungeon(event.getPlayer().getWorld());
        if (dungeonObject.getType() != DungeonManager.DungeonType.THE_INFERNAL_ABYSS) return;
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block.getType() == Material.BEDROCK || block.getType() == Material.FIRE) {
            if (block.getType() == Material.BEDROCK) {
                Block baseBlock = block.getLocation().add(0.D, 1D, 0D).getBlock();
                if (baseBlock.getType() == Material.FIRE) {
                    block = baseBlock;
                } else {
                    return;
                }
            }

            block.setType(Material.AIR);
            block.getLocation().subtract(0D, 1D, 0D).getBlock().setType(Material.AIR);

            for (Entity entity : block.getChunk().getEntities()) {
                if (!(entity instanceof EnderCrystal)) {
                    continue;
                }
                if (entity.getLocation().distanceSquared(block.getLocation()) <= 4) {
                    entity.remove();
                    break;
                }
            }

            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.MAGIC_CRIT, block.getLocation().add(0, 1, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 1F, 50);
            } catch (Exception e) {
                e.printStackTrace();
            }

            DungeonManager.getInstance().getDungeon_Wither_Effect().put(player.getWorld().getName(), 90);

            for (Player player1 : player.getWorld().getPlayers()) {
                player1.removePotionEffect(PotionEffectType.WITHER);
                player1.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, (int) (90 * 20L), 0));
                player1.playSound(player1.getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 5F, 1.5F);
                player1.sendMessage(ChatColor.YELLOW + "Debuff timer refreshed, " + ChatColor.UNDERLINE + HealthHandler.getInstance().getPlayerMaxHPLive(player1)
                        + " DMG " + ChatColor.YELLOW + "will be inflicted in 90s unless another beacon is activated.");
            }
        }
    }
}
