package net.dungeonrealms.game.listeners;

import net.dungeonrealms.API;
import net.dungeonrealms.game.handlers.FriendHandler;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.rank.Rank;
import net.dungeonrealms.game.world.entities.Entities;
import net.dungeonrealms.game.world.entities.utils.EntityAPI;
import net.dungeonrealms.game.world.realms.Realms;
import net.dungeonrealms.game.world.realms.instance.obj.RealmStatus;
import net.dungeonrealms.game.world.realms.instance.obj.RealmToken;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/21/2016
 */
public class RealmListener implements Listener {

    @EventHandler
    public void onWorld(PlayerChangedWorldEvent event) {
        World to = event.getPlayer().getWorld();

        if (Realms.getInstance().getRealm(to) != null) {
            RealmToken realm = Realms.getInstance().getRealm(to);
            realm.getPlayersInRealm().add(event.getPlayer().getUniqueId());

            if (!event.getPlayer().getPlayer().getUniqueId().equals(realm.getOwner()))
                event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "You have entered " + ChatColor.BOLD + Bukkit.getPlayer(realm.getOwner()).getName() + "'s" + ChatColor.LIGHT_PURPLE + " realm.");

            if (!Realms.getInstance().getRealmTitle(realm.getOwner()).equals(""))
                event.getPlayer().sendMessage(ChatColor.GRAY + Realms.getInstance().getRealmTitle(realm.getOwner()));

        } else if (Realms.getInstance().getRealm(event.getFrom()) != null) {
            Realms.getInstance().getRealm(event.getFrom()).getPlayersInRealm().remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;

        if (event.getTo().getY() <= 0) {
            RealmToken realm = Realms.getInstance().getRealm(event.getPlayer().getLocation().getWorld());

            if (realm == null) return;

            event.getPlayer().teleport(realm.getPortalLocation().clone().add(0, 1, 0));
            realm.getPlayersInRealm().remove(event.getPlayer().getUniqueId());
        }
    }


    @EventHandler
    public void onPortalDestory(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (!event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;

        RealmToken realm = Realms.getInstance().getRealm(event.getClickedBlock().getLocation());

        if (realm != null && realm.getOwner().equals(event.getPlayer().getUniqueId()))
            Realms.getInstance().closeRealmPortal(realm.getOwner(), true);
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
        Player p = e.getPlayer();

        if (p.getWorld().equals(Bukkit.getWorlds().get(0))) return;

        RealmToken realm = Realms.getInstance().getRealm(p.getLocation().getWorld());

        if (realm == null) return;

        if (p.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        String world = e.getBlock().getWorld().getName();
        if (e.getBlock().getType() == Material.PORTAL) {
            e.setCancelled(true);
            return;
        }

        if (!p.isOp() && (e.getBlock().getType() == Material.TRAPPED_CHEST || e.getBlock().getType() == Material.GOLD_BLOCK)) {
            if (e.getBlock().getType() == Material.TRAPPED_CHEST) {
                p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " place this "
                        + e.getBlock().getType().name().toUpperCase() + " as it is an illegal item.");
            }
            p.updateInventory();
            e.setCancelled(true);
            return;
        }


        if (!realm.getOwner().equals(p.getUniqueId()) && !realm.getBuilders().contains(p.getUniqueId()) && !Rank.isGM(p)) {
            p.sendMessage(ChatColor.RED + "You aren't authorized to build in " + Bukkit.getPlayer(realm.getOwner()).getName() + "'s realm.");
            p.sendMessage(ChatColor.GRAY + Bukkit.getPlayer(realm.getOwner()).getName() + " will have to " + ChatColor.UNDERLINE + "Sneak Left Click" + ChatColor.GRAY +
                    " you with their Realm Portal Rune to add you to their builder list.");
            e.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAddBuilder(EntityDamageByEntityEvent event) {
        if (!API.isPlayer(event.getDamager()) || !API.isPlayer(event.getEntity())) return;

        Player p = (Player) event.getDamager();
        Player target = (Player) event.getEntity();

        if (p.getEquipment().getItemInMainHand() == null || p.getEquipment().getItemInMainHand().getType() != Material.NETHER_STAR)
            return;
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(p.getEquipment().getItemInMainHand());
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null) return;
        if (tag.hasKey("realmPortalRune") && !(tag.getString("realmPortalRune").equalsIgnoreCase("true"))) return;

        if (!p.isSneaking()) return;

        if (!Realms.getInstance().isRealmCached(p.getUniqueId())) {
            p.sendMessage(ChatColor.GREEN + "You must open your realm portal to add builders.");
            return;
        }

        event.setCancelled(true);
        event.setDamage(0);

        RealmToken realm = Realms.getInstance().getRealm(p.getUniqueId());


        if (!(FriendHandler.getInstance().areFriends(p, target.getUniqueId()))) {
            p.sendMessage(ChatColor.RED + "Cannot add a non-buddy to realm build list.");
            p.sendMessage(ChatColor.GRAY + "Type '" + ChatColor.BOLD + "/add " + target.getName() + ChatColor.GRAY
                    + "' to add them to your buddy list.");
            return;
        }

        if (!realm.getBuilders().contains(target.getUniqueId())) {
            p.sendMessage(ChatColor.GREEN + "" + ChatColor.UNDERLINE + "ADDED " + ChatColor.GREEN + "" + ChatColor.BOLD + target.getName()
                    + ChatColor.GREEN + " to your realm builder list.");
            p.sendMessage(ChatColor.GRAY + target.getName()
                    + " can now place/destroy blocks in your realm until you logout of your current game session.");
            target.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "ADDED" + ChatColor.GREEN + " to " + p.getName() + "'s build list.");
            target.sendMessage(ChatColor.GRAY + "You can now place/destroy blocks in their realm until the end of their game session.");

            realm.getBuilders().add(target.getUniqueId());
        } else {
            p.sendMessage(ChatColor.RED + "" + ChatColor.UNDERLINE + "REMOVED " + ChatColor.RED + "" + ChatColor.BOLD + target.getName()
                    + ChatColor.RED + " from your realm builder list.");
            p.sendMessage(ChatColor.GRAY + target.getName() + " can no longer place/destroy blocks in your realm.");
            target.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "REMOVED " + ChatColor.RED + "from " + p.getName() + "'s builder list.");
            target.sendMessage(ChatColor.GRAY + "You can no longer place/destroy blocks in their realm.");

            realm.getBuilders().remove(target.getUniqueId());
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCreativeBlockBreak(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!(e.hasBlock()) || e.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        if (p.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        if (p.getWorld().equals(Bukkit.getWorlds().get(0))) return;


        RealmToken realm = Realms.getInstance().getRealm(p.getWorld());

        if (realm == null) return;


        if (!realm.getOwner().equals(p.getUniqueId()) && !realm.getBuilders().contains(p.getUniqueId()) && !Rank.isGM(p)) {
            p.sendMessage(ChatColor.RED + "You aren't authorized to build in " + Bukkit.getPlayer(realm.getOwner()).getName() + "'s realm.");
            p.sendMessage(ChatColor.GRAY + Bukkit.getPlayer(realm.getOwner()).getName() + " will have to " + ChatColor.UNDERLINE + "Sneak Left Click" + ChatColor.GRAY +
                    " you with their Realm Portal Rune to add you to their builder list.");
            return;
        }

        Block b = e.getClickedBlock();

        e.setCancelled(true);
        Material m = b.getType();
        if (m == Material.AIR || m == Material.PORTAL) {
            return;
        }
        ItemStack loot = (new ItemStack(b.getType(), 1, b.getData()));

        if (b.getType() == Material.WATER || b.getType() == Material.STATIONARY_WATER || b.getType() == Material.LAVA
                || b.getType() == Material.STATIONARY_LAVA) {
            return;
        }


        if (b.getType() == Material.CHEST) {
            Chest c = (Chest) b.getState();
            Inventory c_inv = c.getInventory();
            int in_chest = getUsedSlots(c_inv);
            int available_on_player = getAvailableSlots(p.getInventory());
            if (in_chest > available_on_player) {
                p.sendMessage(ChatColor.RED + "You do not have enough room in your inventory for all the items in this chest.");
                p.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "REQ: " + in_chest + " slots");
                return;
            }
            for (ItemStack is : c_inv.getContents()) {
                if (is != null && is.getType() != Material.AIR) {
                    p.getInventory().setItem(p.getInventory().firstEmpty(), is);
                }
            }

            c_inv.clear();
        }

        if (b.getType() == Material.ITEM_FRAME) {
            b.setType(Material.AIR);
            loot.setTypeId(0);
        }

        if (b.getType() == Material.SKULL) {
            loot.setType(Material.SKULL_ITEM);
        }
        if (b.getType() == Material.DOUBLE_PLANT) {
            if (b.getData() == (byte) 0) {
                loot.setType(Material.DOUBLE_PLANT);
            } else {
                e.setCancelled(true);
                e.setUseInteractedBlock(Event.Result.DENY);
                b.setType(Material.DOUBLE_PLANT);
                loot.setType(Material.AIR);
            }
        }
        if (b.getType() == Material.REDSTONE_TORCH_ON) {
            loot.setType(Material.REDSTONE_TORCH_OFF);
        }

        if (b.getTypeId() == 64 || b.getTypeId() == 71) {
            // Door break, ensure they don't get the stupid bottom part.
            Location l = b.getLocation();
            int block_id = b.getTypeId();
            if (l.add(0, 1, 0).getBlock().getTypeId() == block_id) {
                // Door.
                if (block_id == 64) {
                    loot.setTypeId(324);
                }
                if (block_id == 71) {
                    loot.setTypeId(330);
                }
            }

            l.subtract(0, 1, 0);
            if (l.subtract(0, 1, 0).getBlock().getTypeId() == block_id) {
                if (block_id == 64) {
                    loot.setTypeId(324);
                }
                if (block_id == 71) {
                    loot.setTypeId(330);
                }
                l.getBlock().setType(Material.AIR);
            }
        }

        if (b.getType() == Material.REDSTONE_WIRE) {
            loot = new ItemStack(Material.REDSTONE, 1);
            // loot.setDurability((short)0);
        }

        if (b.getType() == Material.PISTON_BASE) {
            loot.setTypeId(33);
        }

        if (b.getType() == Material.PISTON_MOVING_PIECE || b.getType() == Material.PISTON_EXTENSION) {
            loot.setTypeId(0);
        }

        if (b.getType() == Material.PISTON_STICKY_BASE) {
            loot.setTypeId(29);
        }

        if (b.getType() == Material.WALL_SIGN) {
            loot.setType(Material.SIGN);
        }

        if (b.getType() == Material.SIGN_POST) {
            loot.setType(Material.SIGN);
        }

        if (b.getType() == Material.BED) {
            loot.setType(Material.AIR);
        }

        if (b.getType() == Material.BED_BLOCK) {
            loot.setType(Material.AIR);
        }

        if (b.getType() == Material.REDSTONE_TORCH_OFF) {
            loot.setType(Material.REDSTONE_TORCH_ON);
        }

        if (b.getTypeId() == 93 || b.getTypeId() == 94) {
            loot.setTypeId(356);
        }

        p.getWorld().playEffect(b.getLocation(), Effect.SMOKE, 20);
        p.getWorld().playSound(b.getLocation(), getSoundEffect(b.getType()), 1.0F, 0.5F);
        b.setType(Material.AIR);


        int amount = loot.getAmount();
        int max_stack = loot.getMaxStackSize();
        Inventory i = p.getInventory();
        int slot = -1;

        HashMap<Integer, ? extends ItemStack> invItems = i.all(loot.getType());
        for (Map.Entry<Integer, ? extends ItemStack> entry : invItems.entrySet()) {
            ItemStack item = entry.getValue();
            int stackAmount = item.getAmount();
            if (item.getDurability() != loot.getDurability()) {
                continue;
            }
            if ((stackAmount + amount) <= max_stack) {
                slot = entry.getKey();
                item.setAmount(item.getAmount() + amount);
                b.setType(Material.AIR);
                p.getInventory().setItem(slot, item);
                p.updateInventory();
                break; // Set stack more, no need to add a new item to it.
            }
        }

        if (slot == -1) {
            // We never found a stack to add it to.
            if (p.getInventory().firstEmpty() == -1) {
                // No space!
                p.sendMessage(ChatColor.RED + "No inventory space.");
                e.setCancelled(true);
                b.setType(m); // Revert the block.
                return;
            }
            // There's room!
            p.getInventory().setItem(p.getInventory().firstEmpty(), loot);
        }

        p.updateInventory();
    }

    private Sound getSoundEffect(Material type) {
        switch (type) {
            case WOOL:
                return Sound.BLOCK_CLOTH_BREAK;
            case ANVIL:
                return Sound.BLOCK_ANVIL_BREAK;
            case LADDER:
                return Sound.BLOCK_LADDER_BREAK;
            case GRAVEL:
                return Sound.BLOCK_GRAVEL_BREAK;
            case SOUL_SAND:
            case SAND:
                return Sound.BLOCK_SAND_BREAK;
            case GRASS:
                return Sound.BLOCK_GRASS_BREAK;
            case DIRT:
                return Sound.BLOCK_GRAVEL_BREAK;
            case GLASS:
                return Sound.BLOCK_GLASS_BREAK;
            case CHEST:
            case WOOD:
                return Sound.BLOCK_WOOD_BREAK;
            case SNOW:
                return Sound.BLOCK_SNOW_BREAK;
            default:
                return Sound.BLOCK_STONE_BREAK;
        }
    }


    public int getAvailableSlots(Inventory i) {
        int count = 0;
        for (ItemStack is : i.getContents()) {
            if (is == null || is.getType() == Material.AIR) {
                count++;
            }
        }
        return count;
    }

    public int getUsedSlots(Inventory i) {
        int count = 0;
        for (ItemStack is : i.getContents()) {
            if (is != null && is.getType() != Material.AIR) {
                count++;
            }
        }
        return count;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerEnterPortal(PlayerPortalEvent event) {
        if (event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) {
            if (EntityAPI.hasPetOut(event.getPlayer().getUniqueId())) {
                Entity pet = Entities.PLAYER_PETS.get(event.getPlayer().getUniqueId());
                pet.dead = true;
                EntityAPI.removePlayerPetList(event.getPlayer().getUniqueId());
            }
            if (EntityAPI.hasMountOut(event.getPlayer().getUniqueId())) {
                Entity mount = Entities.PLAYER_MOUNTS.get(event.getPlayer().getUniqueId());
                mount.dead = true;
                EntityAPI.removePlayerMountList(event.getPlayer().getUniqueId());
            }

            if (!CombatLog.isInCombat(event.getPlayer())) {
                RealmToken realm = Realms.getInstance().getRealm(event.getFrom());

                if (realm == null) return;

                if (!Realms.getInstance().isRealmLoaded(realm.getOwner()))
                    return;

                if (realm.getStatus() != RealmStatus.OPENED) return;

                // SAVES THEIR LOCATION
                DatabaseAPI.getInstance().update(event.getPlayer().getUniqueId(), EnumOperators.$SET, EnumData.CURRENT_LOCATION, API.locationToString(event.getFrom()), true);
                event.setTo(Realms.getInstance().getRealmWorld(realm.getOwner()).getSpawnLocation().clone().add(0, 2, 0));

            } else {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "You cannot enter a realm while in combat!");
            }
        } else if (Realms.getInstance().getRealm(event.getPlayer().getLocation().getWorld()) != null) {
            if (EntityAPI.hasPetOut(event.getPlayer().getUniqueId())) {
                Entity pet = Entities.PLAYER_PETS.get(event.getPlayer().getUniqueId());
                pet.dead = true;
                EntityAPI.removePlayerPetList(event.getPlayer().getUniqueId());
            }
            if (EntityAPI.hasMountOut(event.getPlayer().getUniqueId())) {
                Entity mount = Entities.PLAYER_MOUNTS.get(event.getPlayer().getUniqueId());
                mount.dead = true;
                EntityAPI.removePlayerMountList(event.getPlayer().getUniqueId());
            }

            RealmToken realm = Realms.getInstance().getRealm(event.getPlayer().getLocation().getWorld());
            event.setTo(realm.getPortalLocation().clone().add(0, 1, 0));
        }
    }
}
