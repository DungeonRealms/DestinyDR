package net.dungeonrealms.game.listener.world;

import net.dungeonrealms.game.mechanic.CutSceneMechanic;
import net.dungeonrealms.game.mechanic.cutscenes.CutScene;
import net.dungeonrealms.game.mechanic.cutscenes.GsonLocation;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CutSceneListener implements Listener {


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        CutScene recording = CutSceneMechanic.get().getCreatingCutscene().get(player.getUniqueId());
        if (recording != null) {
            if (System.currentTimeMillis() - recording.getStartTime() >= recording.getLength() * 1_000) {
                //Done?
                CutSceneMechanic.get().getCreatingCutscene().remove(player.getUniqueId());
                player.sendMessage(CC.Red + "Cutscened saved with " + recording.getLocations().size() + " cutscene locations");
                CutSceneMechanic.get().saveCutscene(recording);
            } else {
                recording.getLocations().add(new GsonLocation(player.getLocation().clone()));
            }
        }
    }

    @EventHandler
    public void onPlayerLeavE(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CutSceneMechanic.PlayerCutScene scene = CutSceneMechanic.get().getPlayerCutScenes().remove(player);
        if (scene != null) {
            //Tp them back
            scene.endScene(player);
        }
    }
}
