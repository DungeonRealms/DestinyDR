package net.dungeonrealms.game.donation.overrides;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.collect.MapMaker;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.minecraft.server.v1_9_R2.Container;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public abstract class FakeEquipment {
    private Map<Object, EnumWrappers.ItemSlot> processedPackets;
    private Plugin plugin;
    private ProtocolManager manager;
    private PacketListener listener;

    public FakeEquipment(final Plugin plugin) {
        this.processedPackets = new MapMaker().weakKeys().makeMap();
        this.plugin = plugin;

        PacketAdapter.AdapterParameteters parameteters = new PacketAdapter.AdapterParameteters();
        (this.manager = ProtocolLibrary.getProtocolManager()).addPacketListener(this.listener = new PacketAdapter(parameteters.optionAsync().listenerPriority(ListenerPriority.HIGH).types(PacketType.Play.Server.ENTITY_EQUIPMENT, PacketType.Play.Server.NAMED_ENTITY_SPAWN, PacketType.Play.Server.WINDOW_ITEMS, PacketType.Play.Server.SET_SLOT, PacketType.Play.Client.SET_CREATIVE_SLOT).plugin(DungeonRealms.getInstance())) {
            @Override
            public void onPacketSending(final PacketEvent event) {
                PacketContainer packet = event.getPacket();
                final PacketType type = event.getPacketType();
                final Player observingPlayer = event.getPlayer();
                if (PacketType.Play.Server.ENTITY_EQUIPMENT.equals(type)) {
                    final LivingEntity visibleEntity = (LivingEntity) packet.getEntityModifier(event).read(0);
                    if (!observingPlayer.hasMetadata("NPC") && (!visibleEntity.hasMetadata("NPC") || (visibleEntity.getCustomName() != null && visibleEntity.getCustomName().contains("Wizard")))) {
                        final EnumWrappers.ItemSlot itemSlot = packet.getItemSlots().getValues().get(0);
                        final ItemStack equipment = packet.getItemModifier().read(0);
                        final EquipmentSendingEvent sendingEvent = new EquipmentSendingEvent(observingPlayer, visibleEntity, itemSlot, equipment);
                        final EnumWrappers.ItemSlot previous = FakeEquipment.this.processedPackets.get(packet.getHandle());
                        if (previous != null) {
                            packet = event.getPacket().deepClone();
                            sendingEvent.setSlot(previous);
                            ItemStack equipmentStack = FakeEquipment.this.getEquipment(previous, visibleEntity);
                            if (equipmentStack != null) sendingEvent.setEquipment(equipmentStack.clone());
                        }
                        if (FakeEquipment.this.onEquipmentSending(sendingEvent)) {
                            FakeEquipment.this.processedPackets.put(packet.getHandle(), (previous != null) ? previous : itemSlot);
                        }
                        if (itemSlot != sendingEvent.getSlot()) {
                            packet.getItemSlots().write(1, itemSlot);
                        }
                        if (equipment != sendingEvent.getEquipment()) {
                            packet.getItemModifier().write(0, sendingEvent.getEquipment());
                        }
                    }
                } else if (PacketType.Play.Server.NAMED_ENTITY_SPAWN.equals(type)) {
                    final LivingEntity visibleEntity = (LivingEntity) packet.getEntityModifier(event).read(0);

                    FakeEquipment.this.onEntitySpawn(observingPlayer, visibleEntity);
                } else if (PacketType.Play.Server.WINDOW_ITEMS.equals(type)) {
                    int slot = packet.getIntegers().read(0);
                    if (slot != ((CraftPlayer) observingPlayer).getHandle().defaultContainer.windowId)
                        return;

                    ItemStack[] items = packet.getItemArrayModifier().read(0);


                    try {
//                        String data = MetadataUtils.Metadata.ACTIVE_HAT.get(observingPlayer).asString();
//                        if (data != null && !data.isEmpty()) {
                        CosmeticOverrides currentHat = getActiveOverride(observingPlayer);
                        if (currentHat != null && items.length >= 4) {
                            ItemStack helmet = items[5];
                            if (helmet != null && helmet.getType() != Material.AIR) {
                                helmet.setType(currentHat.getItemType());
                                helmet.setDurability(currentHat.getDurability());
                                items[5] = helmet;
                                packet.getItemArrayModifier().write(0, items);
                            }
                        }
//                        }
                    } catch (Exception e) {
                        Bukkit.getLogger().info("Error getting hat from " + observingPlayer.getName());
                    }
                } else if (PacketType.Play.Server.SET_SLOT.equals(type)) {
                    try {
                        int windowId = packet.getIntegers().read(0);
                        int slot = packet.getIntegers().read(1);


                        Container contain = ((CraftPlayer) observingPlayer).getHandle().defaultContainer;
                        if (contain == null || contain.windowId != windowId)
                            return;
                        ItemStack helmet = packet.getItemModifier().read(0);

                        if (slot == 5) {
                            if (helmet != null && helmet.getType() != Material.AIR) {
                                CosmeticOverrides currentHat = getActiveOverride(observingPlayer);
                                if (currentHat != null) {
                                    helmet.setType(currentHat.getItemType());
                                    helmet.setDurability(currentHat.getDurability());
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                final PacketType type = event.getPacketType();
                final Player observingPlayer = event.getPlayer();

                if (type.equals(PacketType.Play.Client.SET_CREATIVE_SLOT)) {
                    int slot = packet.getIntegers().read(0);
                    ItemStack item = packet.getItemModifier().read(0);
                    if (slot == 5) {
                        //Helmet?
                        Bukkit.getLogger().info("Trying to set helmet to " + item);
                        CosmeticOverrides override = getActiveOverride(event.getPlayer());

                        if (override != null && override.getDurability() == item.getDurability() && override.getItemType() == item.getType()) {
                            //Cancel... set to our helmet..
                            packet.getItemModifier().write(0, event.getPlayer().getEquipment().getHelmet());
                        }
                    }
                }
            }
        });
    }

    public CosmeticOverrides getActiveOverride(Player player) {
        String data = MetadataUtils.Metadata.ACTIVE_HAT.get(player).asString();
        if (data != null && !data.isEmpty()) {
            try {
                return CosmeticOverrides.valueOf(data);
            } catch (Exception e) {
                Bukkit.getLogger().info("Error getting hat from " + data);
                e.printStackTrace();
            }
        }
        return null;
    }

    public ItemStack getEquipment(final EnumWrappers.ItemSlot slot, final LivingEntity entity) {
        switch (slot) {
            case MAINHAND: {
                return entity.getEquipment().getItemInMainHand();
            }
            case OFFHAND: {
                return entity.getEquipment().getItemInOffHand();
            }
            case FEET: {
                return entity.getEquipment().getBoots();
            }
            case LEGS: {
                return entity.getEquipment().getLeggings();
            }
            case CHEST: {
                return entity.getEquipment().getChestplate();
            }
            case HEAD: {
                return entity.getEquipment().getHelmet();
            }
            default: {
                throw new IllegalArgumentException("Unknown slot: " + this);
            }
        }
    }

    protected void onEntitySpawn(final Player client, final LivingEntity visibleEntity) {
    }

    protected abstract boolean onEquipmentSending(final EquipmentSendingEvent p0);

    public void updateSlot(final Player client, final LivingEntity visibleEntity, final EquipmentSlot slot) {
        if (this.listener == null) {
            throw new IllegalStateException("FakeEquipment has closed.");
        }
        final PacketContainer equipmentPacket = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
        equipmentPacket.getIntegers().write(0, visibleEntity.getEntityId()).write(1, slot.getId());
        equipmentPacket.getItemModifier().write(0, slot.getEquipment(visibleEntity));
        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(client, equipmentPacket);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("Unable to update slot.", e);
                }
            }
        });
    }

    public void close() {
        if (this.listener != null) {
            this.manager.removePacketListener(this.listener);
            this.listener = null;
        }
    }
}