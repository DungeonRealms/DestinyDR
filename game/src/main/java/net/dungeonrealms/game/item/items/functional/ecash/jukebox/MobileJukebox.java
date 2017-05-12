package net.dungeonrealms.game.item.items.functional.ecash.jukebox;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@Getter
public class MobileJukebox {

    private Block jukebox;
    private UUID uuid;
    private String owner;
    private Material recordPlaying;

    private long timeoutTime;

    private Hologram hologram;

    public MobileJukebox(Block jukebox, UUID uuid, String owner, Material material) {
        this.jukebox = jukebox;
        this.uuid = uuid;
        this.owner = owner;
        this.recordPlaying = material;
    }

    public void setPlaying(Material material) {
        if (jukebox.getType() == Material.AIR && recordPlaying == null) {
            //Stays for 5 minutes?
            this.timeoutTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
            jukebox.setType(Material.JUKEBOX);
            jukebox.getWorld().playEffect(jukebox.getLocation().clone().add(0.5, .5, 0.5), Effect.STEP_SOUND, Material.JUKEBOX.getId());
            ItemJukebox.setJukebox(this.jukebox, this);
            ItemJukebox.attemptTaskStart();

            this.hologram = HologramsAPI.createHologram(DungeonRealms.getInstance(), jukebox.getLocation().add(0.5, 1.9, 0.5));
            this.hologram.appendTextLine(ChatColor.AQUA + owner + "'s");
            this.hologram.appendTextLine(ChatColor.AQUA.toString() + ChatColor.BOLD + "♫ Mobile Musicbox ♫");
        }

        Jukebox box = (Jukebox) jukebox.getState();
        this.recordPlaying = material;
        box.setPlaying(this.recordPlaying);
    }

    public void breakJukebox() {
        ItemJukebox.getMobileJukeboxes().remove(this.jukebox);
        if (jukebox.getType() == Material.JUKEBOX) {
            Jukebox box = (Jukebox) jukebox.getState();
            box.setPlaying(Material.AIR);
            box.update();
        }

        jukebox.setType(Material.AIR);
        jukebox.getWorld().playEffect(jukebox.getLocation().clone().add(0.5, .5, 0.5), Effect.STEP_SOUND, Material.JUKEBOX.getId());
        if (this.hologram != null && !this.hologram.isDeleted()) {
            this.hologram.delete();
        }
    }
}
