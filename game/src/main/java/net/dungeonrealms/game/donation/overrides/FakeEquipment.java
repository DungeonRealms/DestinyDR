package net.dungeonrealms.game.donation.overrides;

import com.comphenix.protocol.wrappers.*;
import org.bukkit.plugin.*;
import com.google.common.collect.*;
import com.comphenix.protocol.*;
import org.bukkit.inventory.*;
import com.comphenix.protocol.events.*;
import org.bukkit.entity.*;
import java.lang.reflect.*;
import java.util.Map;

import org.bukkit.*;

public abstract class FakeEquipment
{
    private Map<Object, EnumWrappers.ItemSlot> processedPackets;
    private Plugin plugin;
    private ProtocolManager manager;
    private PacketListener listener;
    
    public FakeEquipment(final Plugin plugin) {
        this.processedPackets = new MapMaker().weakKeys().makeMap();
        this.plugin = plugin;
        (this.manager = ProtocolLibrary.getProtocolManager()).addPacketListener(this.listener = (PacketListener)new PacketAdapter(plugin, new PacketType[] { PacketType.Play.Server.ENTITY_EQUIPMENT, PacketType.Play.Server.NAMED_ENTITY_SPAWN, PacketType.Play.Server.WINDOW_DATA }) {
            public void onPacketSending(final PacketEvent event) {
                PacketContainer packet = event.getPacket();
                final PacketType type = event.getPacketType();
                final LivingEntity visibleEntity = (LivingEntity)packet.getEntityModifier(event).read(0);
                final Player observingPlayer = event.getPlayer();
                if (PacketType.Play.Server.ENTITY_EQUIPMENT.equals(type)) {
                    if (!observingPlayer.hasMetadata("NPC") && (!visibleEntity.hasMetadata("NPC") || (visibleEntity.getCustomName() != null && visibleEntity.getCustomName().contains("Wizard")))) {
                        final EnumWrappers.ItemSlot itemSlot = packet.getItemSlots().getValues().get(0);
                        final ItemStack equipment = (ItemStack)packet.getItemModifier().read(0);
                        final EquipmentSendingEvent sendingEvent = new EquipmentSendingEvent(observingPlayer, visibleEntity, itemSlot, equipment);
                        final EnumWrappers.ItemSlot previous = FakeEquipment.this.processedPackets.get(packet.getHandle());
                        if (previous != null) {
                            packet = event.getPacket().deepClone();
                            sendingEvent.setSlot(previous);
                            sendingEvent.setEquipment(FakeEquipment.this.getEquipment(previous, visibleEntity).clone());
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
                }
                else if (PacketType.Play.Server.NAMED_ENTITY_SPAWN.equals(type)) {
                    FakeEquipment.this.onEntitySpawn(observingPlayer, visibleEntity);
                }
                else {
                    if (!PacketType.Play.Server.WINDOW_DATA.equals(type)) {
                        throw new IllegalArgumentException("Unknown packet type:" + type);
                    }
                    Bukkit.getLogger().info("Window item packet: " + visibleEntity);
                    Bukkit.getLogger().info("a: " + packet.getIntegers().read(0) + " b: " + packet.getIntegers().read(1) + " c: " + packet.getIntegers().read(2));
                }
            }
        });
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
                }
                catch (InvocationTargetException e) {
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