package net.dungeonrealms.game.item.items.functional.ecash;

import com.google.common.collect.Lists;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemClickEvent.ItemClickListener;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.data.HorseTier;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class ItemMount extends FunctionalItem implements ItemClickListener {

    public HorseTier horseTier = HorseTier.TIER_1;

    public ItemMount(HorseTier tier) {
        super(ItemType.MOUNT);
        setPermUntradeable(true);
        setTagString("tier", tier.name());
        this.horseTier = tier;
    }

    public ItemMount(ItemStack item) {
        super(item);
        if (hasTag("tier")) {
            this.horseTier = HorseTier.valueOf(getTagString("tier"));
        }
    }

    @Override
    protected void loadItem() {
        super.loadItem();
        if (hasTag("tier")) {
            this.horseTier = HorseTier.valueOf(getTagString("tier"));
        }
    }

    @Override
    public void updateItem() {
        setTagString("tier", horseTier.name());
        super.updateItem();
    }

    public static void attemptSummonMount(Player player, String name) {
        if (!canSummonMount(player))
            return;
        PlayerWrapper pw = PlayerWrapper.getWrapper(player);
        EnumMounts mountType = pw.getActiveMount();

        if (mountType == null) {
            player.sendMessage(ChatColor.RED + "You don't have an active mount, please enter the mounts section in your profile to set one.");
            player.closeInventory();
            return;
        }

        if (!pw.getMountsUnlocked().contains(mountType)) {
            player.sendMessage(ChatColor.RED + "You do not own this mount.");
            pw.setActiveMount(null);
            return;
        }

        int currentlySummoning = MetadataUtils.Metadata.SUMMONING.get(player).asInt();
        if (currentlySummoning != -1 && (Bukkit.getScheduler().isCurrentlyRunning(currentlySummoning) || Bukkit.getScheduler().isQueued(currentlySummoning))) {
            player.sendMessage(ChatColor.RED + "You are already summoning a mount!");
            return;
        }

        if (name == null) name = mountType.getDisplayName();
        int max = 5;

        player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "SUMMONING " + ChatColor.UNDERLINE + name
                + ChatColor.WHITE + " ... " + max + ChatColor.BOLD + "s");
        Location startingLocation = player.getLocation().clone();

        BukkitRunnable run = new BukkitRunnable() {
            int count = 0;

            public void run() {
                if (!player.isOnline() || player.isDead() || player.getLocation().distanceSquared(startingLocation) > 4) {
                    player.sendMessage(ChatColor.RED + "Mount Summon - " + ChatColor.BOLD + "CANCELLED");
                    cancel();
                    return;
                }
                count++;
                if (count < max) {
                    player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "SUMMONING" + ChatColor.WHITE + " ... " + (max - count) + ChatColor.BOLD + "s");
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SPELL, player.getLocation(), 1F, 0F, 1F, .1F, 40);
                } else {
                    MountUtils.spawnMount(player, mountType, pw.getActiveMountSkin());
                    cancel();
                }
            }
        };
        run.runTaskTimer(DungeonRealms.getInstance(), 20, 20);
        int id = run.getTaskId();
        MetadataUtils.Metadata.SUMMONING.set(player, id);
    }

    private static boolean canSummonMount(Player player) {
        // Dismiss existing mount.
        if (MountUtils.hasActiveMount(player)) {
            MountUtils.removeMount(player);
            return false;
        }

        if (player.getEyeLocation().getBlock().getType() != Material.AIR) {
            player.sendMessage(ChatColor.RED + "You cannot summon a mount here!");
            return false;
        }

        if (CombatLog.isInCombat(player)) {
            player.sendMessage(ChatColor.RED + "You cannot summon a mount while in combat!");
            return false;
        }

        return true;
    }

    @Override
    public void onClick(ItemClickEvent evt) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(evt.getPlayer());

        EnumMounts mount = wrapper.getHighestHorseUnlocked();
        if (mount == null) {
            return;
        }

        wrapper.setActiveMount(mount);
        attemptSummonMount(evt.getPlayer(), getDisplayName());
    }

    @Override
    protected String getDisplayName() {
        return horseTier.getColor() + horseTier.getName();
    }

    @Override
    protected String[] getLore() {
        List<String> lore = Lists.newArrayList(ChatColor.RED + "Speed: " + horseTier.getSpeed() + "%");
        if (horseTier.getJump() > 100) {
            lore.add(ChatColor.RED + "Jump: " + horseTier.getJump() + "%");
        }
        lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString() + horseTier.getDescription());
        return lore.toArray(new String[lore.size()]);
    }

    @Override
    protected ItemUsage[] getUsage() {
        return INTERACT_RIGHT_CLICK;
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.SADDLE);
    }
}
