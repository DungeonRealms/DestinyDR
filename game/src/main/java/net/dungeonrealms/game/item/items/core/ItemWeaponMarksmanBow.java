package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.mechanic.data.ScrapTier;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.dungeonrealms.game.world.item.Item;
import net.minecraft.server.v1_9_R2.DataWatcher;
import net.minecraft.server.v1_9_R2.DataWatcherObject;
import net.minecraft.server.v1_9_R2.DataWatcherRegistry;
import net.minecraft.server.v1_9_R2.PacketPlayOutEntityMetadata;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemWeaponMarksmanBow extends ItemWeaponRanged{

    public ItemWeaponMarksmanBow() {
        super(ItemType.MARKSMAN_BOW);
    }

    public ItemWeaponMarksmanBow(ItemStack item) {
        super(item);
    }

    public static boolean isMarksmanBow(ItemStack item) {
        return isType(item, ItemType.MARKSMAN_BOW);
    }

    @Override
    public int getShootDelay() {
        return 650;
    }

    @Override
    public Sound getShootSound() {
        return Sound.ENTITY_ENDERDRAGON_SHOOT;
    }

    @Override
    public void fireProjectile(Player player, boolean takeDurability) {
        //What is the point of this?
        DataWatcher watcher = new DataWatcher(((CraftPlayer) player).getHandle());
        watcher.register(new DataWatcherObject<>(5, DataWatcherRegistry.a), (byte) 1);
        for (Player player1 : Bukkit.getOnlinePlayers())
            if (player != player1)
                ((CraftPlayer) player1).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(((CraftPlayer) player).getHandle().getId(), watcher, true));

        DamageAPI.fireMarksmanBowProjectile(player, this, takeDurability);
    }

    @Override
    public int getRepairParticle(ScrapTier tier) {
        return 5;
    }

    public static void getDamageBoost(ItemWeaponMarksmanBow bow) {
        bow.getAttributes().getAttribute(Item.WeaponAttributeType.DAMAGE_BOOST);
    }
}
