package net.dungeonrealms.game.anticheat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class PacketModifier implements GenericMechanic {

    private PacketListener listener;
    private List<String> ALLOWED_TAGS = Arrays.asList("display", "pages", "generation", "SkullOwner", "AttributeModifiers", "ench", "Unbreakable", "HideFlags", "CanDestroy", "PickupDelay", "CanPlaceOn");

    @Override
    public void startInitialization() {
        //This makes it so players can't tell which mobs will drop what items, or learn what items another player is using.
        listener = new PacketAdapter(DungeonRealms.getInstance(), PacketType.Play.Server.ENTITY_EQUIPMENT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                if (event.getPacketType().equals(PacketType.Play.Server.ENTITY_EQUIPMENT)) {
                    ItemStack original = packet.getItemModifier().read(0);
                    if (original == null || original.getType() == Material.AIR || original.getType() == Material.SHIELD)
                        return;

                    final LivingEntity visibleEntity = (LivingEntity) packet.getEntityModifier(event).read(0);
                    if(visibleEntity instanceof EntityHumanNPC.PlayerNPC){
                        return;
                    }

                    final Player observingPlayer = event.getPlayer();
                    if (visibleEntity == observingPlayer) return;
                    //Remove all data the client doesn't need to see.
                    ItemStack item = stripNBT(original);
                    ItemMeta meta = item.getItemMeta();
                    if (meta.hasLore())
                        meta.setLore(Arrays.asList(ItemRarity.UNIQUE.getName()));
                    if (meta.hasDisplayName())
                        meta.setDisplayName(ChatColor.MAGIC + "TRUMP STEAKS");
                    if (ItemArmor.isArmor(item) || ItemWeapon.isWeapon(item))
                        item.setDurability((short) 0);
                    item.setItemMeta(meta);
                    packet.getItemModifier().write(0, item);
                }
            }
        };
        ProtocolLibrary.getProtocolManager().addPacketListener(listener);
    }

    @Override
    public void stopInvocation() {
        if (listener != null)
            ProtocolLibrary.getProtocolManager().removePacketListener(listener);
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    public ItemStack stripNBT(ItemStack item) {
        net.minecraft.server.v1_9_R2.ItemStack stripped = CraftItemStack.asNMSCopy(item.clone());
        if (stripped.hasTag()) {
            NBTTagCompound tag = new NBTTagCompound();
            for (String key : stripped.getTag().c())
                if (ALLOWED_TAGS.contains(key))
                    tag.set(key, stripped.getTag().get(key));
            stripped.setTag(tag);
        }
        return CraftItemStack.asBukkitCopy(stripped);
    }
}
