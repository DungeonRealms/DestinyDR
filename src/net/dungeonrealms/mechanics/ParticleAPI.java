package net.dungeonrealms.mechanics;

import java.lang.reflect.Field;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.dungeonrealms.API;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

/**
 * Created by Kieran on 9/20/2015.
 */
public class ParticleAPI {

    public enum ParticleEffect {
        FIREWORKS_SPARK(0, "FIREWORKS", EnumParticle.FIREWORKS_SPARK, new org.bukkit.inventory.ItemStack(Material.FIREWORK)),
        BUBBLE(1, "BUBBLE", EnumParticle.WATER_BUBBLE, new org.bukkit.inventory.ItemStack(Material.AIR)),
        TOWN_AURA(2, "TOWNAURA", EnumParticle.TOWN_AURA, new org.bukkit.inventory.ItemStack(Material.FIREWORK)),
        CRIT(3, "CRITICAL", EnumParticle.CRIT, new org.bukkit.inventory.ItemStack(Material.GOLD_SWORD)),
        MAGIC_CRIT(4, "MAGICCRIT", EnumParticle.CRIT_MAGIC, new org.bukkit.inventory.ItemStack(Material.GOLD_HOE)),
        WITCH_MAGIC(5, "WITCHMAGIC", EnumParticle.SPELL_WITCH, new org.bukkit.inventory.ItemStack(Material.CAULDRON_ITEM)),
        NOTE(6, "NOTE", EnumParticle.NOTE, new org.bukkit.inventory.ItemStack(Material.NOTE_BLOCK)),
        PORTAL(7, "PORTAL", EnumParticle.PORTAL, new org.bukkit.inventory.ItemStack(Material.PORTAL)),
        ENCHANTMENT_TABLE(8, "ENCHANTMENT", EnumParticle.ENCHANTMENT_TABLE, new org.bukkit.inventory.ItemStack(Material.ENCHANTMENT_TABLE)),
        FLAME(9, "FLAME", EnumParticle.FLAME, new org.bukkit.inventory.ItemStack(Material.FIREBALL)),
        LAVA(10, "LAVA", EnumParticle.LAVA, new org.bukkit.inventory.ItemStack(Material.LAVA_BUCKET)),
        SPLASH(11, "SPLASH", EnumParticle.WATER_SPLASH, new org.bukkit.inventory.ItemStack(Material.WATER_BUCKET)),
        LARGE_SMOKE(12, "LARGESMOKE", EnumParticle.SMOKE_LARGE, new org.bukkit.inventory.ItemStack(Material.AIR)),
        RED_DUST(13, "REDDUST", EnumParticle.REDSTONE, new org.bukkit.inventory.ItemStack(Material.REDSTONE)),
        SNOWBALL_POOF(14, "SNOWBALL", EnumParticle.SNOWBALL, new org.bukkit.inventory.ItemStack(Material.SNOW_BLOCK)),
        SMALL_SMOKE(15, "SMOKEY", EnumParticle.SMOKE_NORMAL, new org.bukkit.inventory.ItemStack(Material.INK_SACK)),
        CLOUD(16, "CLOUD", EnumParticle.CLOUD, new org.bukkit.inventory.ItemStack(Material.BEACON)),
        HAPPY_VILLAGER(17, "POISON", EnumParticle.VILLAGER_HAPPY, new org.bukkit.inventory.ItemStack(Material.FIREWORK)),
        SPELL(18, "SPELL", EnumParticle.SPELL, new org.bukkit.inventory.ItemStack(Material.BLAZE_POWDER)),
        SNOW_SHOVEL(19, "SNOWING", EnumParticle.SNOW_SHOVEL, new org.bukkit.inventory.ItemStack(Material.SNOW_BALL));

        private int id;
        private String rawName;
        private EnumParticle particle;
        private org.bukkit.inventory.ItemStack selectionItem;

        ParticleEffect(int id, String rawName, EnumParticle particle,  org.bukkit.inventory.ItemStack selectionItem) {
            this.id = id;
            this.rawName = rawName;
            this.particle = particle;
            this.selectionItem = selectionItem;
        }

        public static ParticleEffect getById(int id) {
            for (ParticleEffect particleEffect : values()) {
                if (particleEffect.id == id) {
                    return particleEffect;
                }
            }
            return null;
        }

        public static ParticleEffect getByName(String rawName) {
            for (ParticleEffect particleEffect : values()) {
                if (particleEffect.rawName.equalsIgnoreCase(rawName)) {
                    return particleEffect;
                }
            }
            return null;
        }

        public EnumParticle getParticle() {
            return particle;
        }

        public org.bukkit.inventory.ItemStack getSelectionItem() {
            return selectionItem;
        }

        public static ChatColor getChatColorByName(String rawName) {
            switch (getByName(rawName)) {
                case FLAME:
                    return ChatColor.RED;
                case HAPPY_VILLAGER:
                    return ChatColor.DARK_GREEN;
            }
            return ChatColor.WHITE;
        }
    }

    /**
     * Sends a particle to a location so that every player within 25 blocks can see it
     *
     * @param particleEffect
     * @param location
     * @param xOffset
     * @param yOffset
     * @param zOffset
     * @param particleSpeed
     * @param particleCount
     * @since 1.0
     */
    public static void sendParticleToLocation(final ParticleEffect particleEffect, final Location location, final float xOffset, final float yOffset, final float zOffset, final float particleSpeed, final int particleCount) {
        Object packet = null;
        try {
            packet = newPacket(particleEffect, location, xOffset, yOffset, zOffset, particleSpeed, particleCount);
        } catch (Exception e) {
            Utils.log.info("Something went wrong creating a packet");
        }

        for (Player player : API.getNearbyPlayers(location, 25)) {
            try {
                sendPacketToPlayer(player.getUniqueId(), packet);
            } catch (Exception e) {
                Utils.log.info("Unable to send particle packet to player " + player.getName());
            }
        }
    }

    /**
     * Creates a new packet to send to players with given parameters
     *
     * @param particleEffect
     * @param location
     * @param xOffset
     * @param yOffset
     * @param zOffset
     * @param particleSpeed
     * @param particleCount
     * @since 1.0
     */
    private static Object newPacket(ParticleEffect particleEffect, Location location, float xOffset, float yOffset, float zOffset, float particleSpeed, int particleCount) throws Exception {
        Object packet = new PacketPlayOutWorldParticles();
        setPacketValue(packet, "a", particleEffect.getParticle());
        setPacketValue(packet, "b", (float) location.getX());
        setPacketValue(packet, "c", (float) location.getY());
        setPacketValue(packet, "d", (float) location.getZ());
        setPacketValue(packet, "e", xOffset);
        setPacketValue(packet, "f", zOffset);
        setPacketValue(packet, "g", yOffset);
        setPacketValue(packet, "h", particleSpeed);
        setPacketValue(packet, "i", particleCount);
        return packet;
    }

    /**
     * Sets the packets value so that the location etc registers correctly
     *
     * @param instance
     * @param fieldName
     * @param value
     * @since 1.0
     */
    private static void setPacketValue(Object instance, String fieldName, Object value) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, value);
    }

    /**
     * Sends the packet to a player
     *
     * @param uuid
     * @param packet
     * @since 1.0
     */
    private static void sendPacketToPlayer(UUID uuid, Object packet) {
        ((CraftPlayer) Bukkit.getPlayer(uuid)).getHandle().playerConnection.sendPacket((Packet) packet);
    }
}
