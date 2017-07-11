package net.dungeonrealms.game.item.items.functional;

import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.common.util.TimeUtil;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.items.core.Aura;
import net.dungeonrealms.game.item.items.core.AuraType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.Set;

public class ItemLootAura extends FunctionalItem implements ItemClickEvent.ItemClickListener {

    @Getter
    private AuraType auraType;

    @Getter
    private double multiplier;

    @Getter
    private double duration;

    private int radius = 20;

    public static volatile Set<Aura> activeAuras = new ConcurrentSet<>();

    public ItemLootAura(ItemStack item) {
        super(item);

        if (hasTag("aura"))
            this.auraType = AuraType.getFromName(getTagString("aura"));

        if (hasTag("mult"))
            this.multiplier = getTag().getDouble("mult");

        if (hasTag("dur"))
            this.duration = getTagInt("dur");
    }

    public ItemLootAura(AuraType type, double multiplier, int durInSeconds) {
        super(ItemType.BUFF_AURA);
        this.auraType = type;
        this.multiplier = multiplier;
        this.duration = durInSeconds;

        setPermUntradeable(true);
        setUndroppable(true);
        setAntiDupe(true);
    }

    @Override
    public void updateItem() {
        super.updateItem();

        setTagString("aura", auraType.name());
        getTag().setDouble("dur", duration);
        getTag().setDouble("mult", multiplier);
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.DIAMOND);
    }

    @Override
    public void onClick(ItemClickEvent evt) {
        Block block = evt.getClickedBlock();
        if (block == null) {
            evt.setCancelled(true);
            return;
        }

        Player player = evt.getPlayer();
        if (block.getRelative(BlockFace.UP).getType() != Material.AIR) {
            evt.setCancelled(true);
            evt.getPlayer().sendMessage(ChatColor.RED + "You cannot place an Aura here!");

            return;
        }

        if (GameAPI.isInSafeRegion(block.getLocation())) {
            evt.getPlayer().sendMessage(ChatColor.RED + "You cannot place an Aura in a Safe Zone!");
            evt.setCancelled(true);
            return;
        }

        evt.setUsed(true);
        Aura aura = new Aura(block.getLocation().add(0, 1, 0), evt.getPlayer().getName(), auraType, multiplier, (int) duration);
        activeAuras.add(aura);

        String text = ChatColor.GREEN + player.getName() + " has activated a " + auraType.getColor() + ChatColor.BOLD + format.format(multiplier) + "% " + auraType.getName(false) + " Buff" + ChatColor.GREEN + " for " + TimeUtil.formatDifference((long) duration, true) + " nearby!";
        player.sendMessage(text);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
        for (Entity ent : player.getNearbyEntities(40, 40, 40)) {
            if (ent instanceof Player) {
                Player pl = (Player) ent;
                pl.sendMessage(text);
            }
        }
    }

    @Override
    protected String getDisplayName() {
        return this.auraType.getColor() + this.auraType.getName() + " Aura";
    }

    private static DecimalFormat format = new DecimalFormat("#.#");

    @Override
    protected String[] getLore() {
        return arr("", ChatColor.GRAY + "Place down to activate a",
                auraType.getColor().toString() + format.format(multiplier) + "% " + auraType.getName() + " Buff" +
                        ChatColor.GRAY + " for " + auraType.getColor() + TimeUtil.formatDifference((long) duration, true),
                ChatColor.GRAY + "in a " + auraType.getColor() + radius + "x" + radius + ChatColor.GRAY + " area around it.");
    }

    @Override
    protected ItemUsage[] getUsage() {
        return arr(ItemUsage.RIGHT_CLICK_BLOCK);
    }


    public static double getDropMultiplier(Location loc, AuraType type) {
        double mult = 0.0;
        for (Aura aura : activeAuras) {
            if (aura.isInsideAura(loc) && aura.getType() == type) {

                //So a 10% buff would add 2% extra.
                if (mult > 0)
                    mult += Math.max(1.25, aura.getMultiplier() / 10);
                else
                    mult += aura.getMultiplier();
            }
        }

        return mult;
    }
}
