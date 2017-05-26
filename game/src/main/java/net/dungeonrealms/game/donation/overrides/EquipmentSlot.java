package net.dungeonrealms.game.donation.overrides;

import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public enum EquipmentSlot
    {
        MAINHAND(0), 
        BOOTS(1), 
        LEGS(2), 
        CHEST(3), 
        HEAD(4), 
        OFFHAND(5);
        
        private int id;
        
        private EquipmentSlot(final int id) {
            this.id = id;
        }
        
        public ItemStack getEquipment(final LivingEntity entity) {
            switch (this) {
                case MAINHAND: {
                    return entity.getEquipment().getItemInMainHand();
                }
                case OFFHAND: {
                    return entity.getEquipment().getItemInOffHand();
                }
                case BOOTS: {
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
        
        public boolean isEmpty(final LivingEntity entity) {
            final ItemStack stack = this.getEquipment(entity);
            return stack != null && stack.getType() == Material.AIR;
        }
        
        public int getId() {
            return this.id;
        }
        
        public static EquipmentSlot fromSlot(final EnumWrappers.ItemSlot itemSlot) {
            for (final EquipmentSlot slot : values()) {
                if (slot.name().equals(itemSlot.name())) {
                    return slot;
                }
            }
            throw new IllegalArgumentException("Cannot find slot id: " + itemSlot.name());
        }
    }