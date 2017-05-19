package net.dungeonrealms.game.item.items.functional.ecash;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ItemJukebox extends FunctionalItem implements ItemClickEvent.ItemClickListener {

    private static Map<Block, MobileJukebox> mobileJukeboxes = new ConcurrentHashMap<>();

    private static int taskID;

    public ItemJukebox(ItemStack item) {
        super(item);
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.JUKEBOX);
    }

    public void attemptTaskStart() {
        if (taskID == -1) {
            taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
                if (mobileJukeboxes.size() == 0) {
                    //Cancel this.
                    Bukkit.getScheduler().cancelTask(taskID);
                    taskID = -1;
                } else {
                    mobileJukeboxes.forEach((block, juke) -> {
                        if (!block.getChunk().isLoaded()) {
                            //Remove to not keep processing..
                            mobileJukeboxes.remove(block);
                            return;
                        }

                        //How??
                        if (block.getType() != Material.JUKEBOX) {
                            mobileJukeboxes.remove(block);
                            return;
                        }
                        ParticleAPI.sendParticleToLocationAsync(ParticleAPI.ParticleEffect.NOTE, block.getLocation().add(0, .75D, 0), 1.4F, 1, 1.4F, 0.1F, 30);
                    });
                }
            }, 20, 5);
        }
    }

    @Override
    public void onClick(ItemClickEvent evt) {
        Player player = evt.getPlayer();
        evt.setCancelled(true);
        if (evt.hasBlock()) {
            //Trying to place?
            Block block = evt.getClickedBlock();
            if (block.getRelative(BlockFace.UP).getType() == Material.AIR && block.getType().isSolid()) {
                //can be placed?
                //Open menu?
            } else {

            }
        }
    }

    @Override
    protected String getDisplayName() {
        return ChatColor.AQUA.toString() + ChatColor.BOLD + "Mobile Musicbox";
    }

    @Override
    protected String[] getLore() {
        return new String[0];
    }

    @Override
    protected ItemUsage[] getUsage() {
        return new ItemUsage[0];
    }


    class MobileJukebox {

        private UUID uuid;
        private String owner;
        private Material recordPlaying;

    }
}
