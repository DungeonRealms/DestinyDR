package net.dungeonrealms.game.player.menu;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.functional.ItemHearthstone;
import net.dungeonrealms.game.item.items.functional.ItemPlayerProfile;
import net.dungeonrealms.game.item.items.functional.ItemQuestCompass;
import net.dungeonrealms.game.item.items.functional.ecash.*;
import net.dungeonrealms.game.mechanic.PlayerManager;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_9_R2.EnumHand;
import net.minecraft.server.v1_9_R2.PacketPlayInBlockDig;
import net.minecraft.server.v1_9_R2.PacketPlayInBlockPlace;
import net.minecraft.server.v1_9_R2.PacketPlayOutSetCooldown;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import static com.comphenix.protocol.PacketType.Play.Client.CLIENT_COMMAND;

public class CraftingMenu implements GenericMechanic, Listener {

    private static PacketListener listener;

    public void startInitialization() {
        listener = new PacketAdapter(DungeonRealms.getInstance(), CLIENT_COMMAND, PacketType.Play.Client.BLOCK_DIG, PacketType.Play.Client.BLOCK_PLACE) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
                if (player.getGameMode() != GameMode.SURVIVAL) return;
                PacketType type = packet.getType();
                if (type == CLIENT_COMMAND && packet.getClientCommands().read(0) == EnumWrappers.ClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
                    if (player.getOpenInventory().getTopInventory() instanceof CraftingInventory) {
                        if (player.getOpenInventory().getTopInventory() instanceof CraftingInventory) {
                            CraftingInventory inventory = (CraftingInventory) player.getOpenInventory().getTopInventory();
                            inventory.setItem(1, getProfileItem(new ItemPlayerProfile(player).generateItem()));
                            inventory.setItem(2, getProfileItem(new ItemHearthstone(player).generateItem()));
                            inventory.setItem(3, getProfileItem(new ItemPetSelector().generateItem()));
                            inventory.setItem(4, getProfileItem(new ItemMountSelection().generateItem()));
//                            inventory.setResult(getProfileItem(new ItemQuestCompass().generateItem()));
//                            if (player.getCompassTarget() != null && !player.getCompassTarget().equals(TeleportLocation.CYRENNICA.getLocation())) {
//                                    player.getOpenInventory().getTopInventory().setItem(0, getProfileItem(new ItemQuestCompass().generateItem()));
//                                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
//                                    if (player.getOpenInventory().getTopInventory() != null && player.getOpenInventory().getTopInventory().equals(inventory)) {
//                                        //Still open?
////                                        inventory.setResult(getProfileItem(new ItemQuestCompass().generateItem()));
////                                        player.updateInventory();
////                                        inventory.setResult(getProfileItem(new ItemQuestCompass().generateItem()));
//                                    }
//                                }, 2);
//                            }
                        }
                    }
                    GameAPI.runAsSpectators(player, (spectator) -> {
                        spectator.sendMessage(ChatColor.YELLOW + player.getName() + " opened their inventory.");
                        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> spectator.openInventory(player.getInventory()));
                    });
                } else {
                    if (event.getPacketType().equals(PacketType.Play.Client.BLOCK_DIG)) {
                        PacketPlayInBlockDig pack = (PacketPlayInBlockDig) event.getPacket().getHandle();
                        if (pack.c() == PacketPlayInBlockDig.EnumPlayerDigType.RELEASE_USE_ITEM) {
                            System.out.println("Released item!");
                        }
                    } else if (event.getPacketType().equals(PacketType.Play.Client.BLOCK_PLACE)) {
                        PacketPlayInBlockPlace pack = (PacketPlayInBlockPlace) event.getPacket().getHandle();
                        if (pack.a() == EnumHand.OFF_HAND && event.getPlayer().getInventory().getItemInOffHand() != null && event.getPlayer().getInventory().getItemInOffHand().getType() == Material.SHIELD) {
                            System.out.println("Starting click!!");
                            if (player.hasMetadata("blocking")) {
                                player.removeMetadata("blocking", DungeonRealms.getInstance());
                                return;
                            }
                            player.setMetadata("blocking", new FixedMetadataValue(DungeonRealms.getInstance(), ""));

                            ((CraftPlayer)event.getPlayer()).getHandle().cA();
//                            ((CraftPlayer) event.getPlayer()).getHandle().playerConnection.sendPacket(
//                                    new PacketPlayOutSetCooldown(net.minecraft.server.v1_9_R2.Item.getById(event.getPlayer().getInventory().getItemInOffHand().getTypeId()), 20 * 5));
                        }
                    }
                }
            }
        };
        ProtocolLibrary.getProtocolManager().addPacketListener(listener);
        Bukkit.getServer().getPluginManager().registerEvents(this, DungeonRealms.getInstance());
    }

    public void stopInvocation() {
        ProtocolLibrary.getProtocolManager().removePacketListener(listener);
        HandlerList.unregisterAll(this);
    }

//    @EventHandler(priority = EventPriority.LOWEST)
//    public void onPLayerCraft(CraftItemEvent event) {
//        if (event.getInventory().getResult() != null) {
//            Bukkit.getLogger().info("Name: " + event.getInventory().getName() + " Holder: " + event.getInventory().getHolder());
//            event.setCancelled(true);
//        }
//    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getName().equals("container.crafting") && event.getRawSlot() == 0 && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getInventory().getType() != InventoryType.WORKBENCH) {
            event.setCancelled(true);
//            Bukkit.getLogger().info("Inventory: " + event.getInventory().getName() + " Holder: " + event.getInventory().getHolder() + " Type: " + event.getInventory().getType());
        }
//        Bukkit.getLogger().info("Click: " + event.getAction() + " Slot: " + event.getRawSlot() + " Inv: " + event.getClickedInventory().getName() + " Inv: " + event.getInventory().getName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCraftingInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (player.getOpenInventory().getTopInventory() instanceof CraftingInventory) {
            if (player.getOpenInventory().getTopInventory().getItem(0) != null)
                player.getOpenInventory().getTopInventory().setItem(0, null);
            player.getOpenInventory().getTopInventory().setItem(1, null);
            player.getOpenInventory().getTopInventory().setItem(2, null);
            player.getOpenInventory().getTopInventory().setItem(3, null);
            player.getOpenInventory().getTopInventory().setItem(4, null);
        }
    }

    public static void addMountItem(Player player) {
        if (PlayerManager.hasItem(player.getInventory(), ItemType.MOUNT))
            return;

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);

        EnumMounts highestHorse = wrapper.getHighestHorseUnlocked();
        if (highestHorse == null || highestHorse.getHorseTier() == null) highestHorse = EnumMounts.TIER1_HORSE;

        addOrReplace(player, new ItemMount(highestHorse.getHorseTier()).generateItem(), Material.SADDLE);
    }

    public static void addPetItem(Player player) {
        if (PlayerManager.hasItem(player.getInventory(), ItemType.PET))
            return;
        player.getInventory().addItem(getProfileItem(new ItemPetSelector().generateItem()));
    }

    private static ItemStack getProfileItem(ItemStack item) {
        return new NBTWrapper(item).setInt("profileItem", 1).build();
    }

    public static void addMuleItem(Player player) {
        if (PlayerManager.hasItem(player.getInventory(), ItemType.MULE))
            return;

        addOrReplace(player, new ItemMuleMount(player).generateItem(), Material.LEASH);
    }

    public static void addTrailItem(Player player) {
        if (PlayerManager.hasItem(player.getInventory(), ItemType.PARTICLE_TRAIL))
            return;

        player.getInventory().addItem(new ItemParticleTrail().generateItem());
    }

    private static void addOrReplace(Player player, ItemStack itemStack, Material material) {
        int slot = player.getInventory().first(material);
        if (slot == -1) {
            player.getInventory().addItem(itemStack);
        } else {
            //Overwrite.
            player.getInventory().setItem(slot, itemStack);
        }
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }
}

