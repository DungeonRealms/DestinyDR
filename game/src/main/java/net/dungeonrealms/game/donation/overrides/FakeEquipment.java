package net.dungeonrealms.game.donation.overrides;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.collect.MapMaker;
import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.item.items.core.setbonus.SetBonus;
import net.dungeonrealms.game.item.items.core.setbonus.SetBonuses;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.minecraft.server.v1_9_R2.Container;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
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
        (this.manager = ProtocolLibrary.getProtocolManager()).addPacketListener(this.listener =
                new PacketAdapter(parameteters.optionAsync().listenerPriority(ListenerPriority.HIGH).types(PacketType.Play.Server.ENTITY_EQUIPMENT, PacketType.Play.Server.NAMED_ENTITY_SPAWN, PacketType.Play.Server.WINDOW_ITEMS, PacketType.Play.Server.SET_SLOT, PacketType.Play.Client.SET_CREATIVE_SLOT).plugin(DungeonRealms.getInstance())) {
                    @Override
                    public void onPacketSending(final PacketEvent event) {
                        PacketContainer packet = event.getPacket();
                        final PacketType type = event.getPacketType();
                        final Player observingPlayer = event.getPlayer();
                        if (PacketType.Play.Server.ENTITY_EQUIPMENT.equals(type)) {
                            final LivingEntity visibleEntity = (LivingEntity) packet.getEntityModifier(event).read(0);
                            if (!(observingPlayer instanceof EntityHumanNPC.PlayerNPC) && (!(visibleEntity instanceof EntityHumanNPC.PlayerNPC) || (visibleEntity.getCustomName() != null && visibleEntity.getCustomName().contains("Wizard")))) {
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
                                if (items.length >= 4) {
                                    boolean changes = false;
                                    if (SetBonus.hasSetBonus(observingPlayer, SetBonuses.HEALER)) {
                                        //Show armor as dyed?

                                        //5, 6, 7, 8
                                        ItemStack boots = observingPlayer.getPlayer().getEquipment().getBoots();
                                        if (boots != null) {
                                            Color color = getHealerColor(boots);

                                            if (color != null) {
                                                changes = true;
                                                //Spoof all armor.
                                                //5 - 8 is helmet/  boots
                                                for (int i = 5; i < 9; i++) {
                                                    items[i] = createLeatherArmor(items[i], color);
                                                }
                                            }
                                        }
                                    }

                                    if (currentHat != null) {
                                        ItemStack helmet = items[5];
                                        if (helmet != null && helmet.getType() != Material.AIR) {
                                            helmet.setType(currentHat.getItemType());
                                            helmet.setDurability(currentHat.getDurability());
                                            ItemMeta meta = helmet.getItemMeta();
                                            meta.spigot().setUnbreakable(true);
                                            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
                                            helmet.setItemMeta(meta);
                                            items[5] = helmet;
                                            changes = true;
                                        }
                                    }

                                    if (changes)
                                        packet.getItemArrayModifier().write(0, items);

                                }
//                        }
                            } catch (Exception e) {
                                e.printStackTrace();
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
                                boolean hat = false;
                                if (slot == 5) {

                                    if (helmet != null && helmet.getType() != Material.AIR) {
                                        CosmeticOverrides currentHat = getActiveOverride(observingPlayer);
                                        if (currentHat != null) {
                                            hat = true;
                                            helmet.setType(currentHat.getItemType());
                                            helmet.setDurability(currentHat.getDurability());
                                        }
                                    }

                                }
//                                final LivingEntity visibleEntity = (LivingEntity) packet.getEntityModifier(event).read(0);
                                if (!hat && helmet != null && SetBonus.hasSetBonus(observingPlayer, SetBonuses.HEALER)) {
                                    if (slot >= 5 && slot <= 8) {
                                        //Armor we need replacin?
                                        Color color = getHealerColor(helmet);
                                        ItemStack newArmor = createLeatherArmor(helmet.clone(), color);
                                        packet.getItemModifier().write(0, newArmor);
//                                        helmet.setItemMeta(newArmor.getItemMeta());
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

    public Color getHealerColor(ItemStack item) {
        NBTWrapper wrapper = new NBTWrapper(item);
        String id = wrapper.getString("customId");
        if (id == null) return null;
        return id.endsWith("t4") ? Color.AQUA : id.endsWith("t5") ? Color.YELLOW : id.endsWith("t3") ? Color.GRAY : null;
    }

    private static Map<Material, Material> converted = new HashMap<>();

    private static final Color DEFAULT_LEATHER = Color.fromRGB(10511680);

    public ItemStack createLeatherArmor(ItemStack item, Color color) {
        if (item == null) return null;
        if (color == null) return item;
        item = item.clone();
        Material newLeatherType = item.getType().name().startsWith("LEATHER_") ? item.getType() : converted.get(item.getType());
        if (newLeatherType == null) {
            String name = item.getType().name();
            if (!name.contains("_")) return null;
            newLeatherType = Material.getMaterial("LEATHER_" + item.getType().name().split("_")[1]);
//            System.out.println("Converted " + item.getType() + " to " + newLeatherType);
            converted.put(item.getType(), newLeatherType);
        }


        if (newLeatherType != null || item.getType().name().startsWith("LEATHER_")) {
            if (newLeatherType != null && item.getType() != newLeatherType)
                item.setType(newLeatherType);

            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();

            Color col = meta.getColor();
            if (!color.equals(col)) {
                meta.setColor(color);
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                item.setItemMeta(meta);
            }
        }
        return item;
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