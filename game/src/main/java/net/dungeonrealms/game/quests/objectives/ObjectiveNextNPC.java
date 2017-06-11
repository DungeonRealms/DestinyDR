package net.dungeonrealms.game.quests.objectives;

import com.google.gson.JsonObject;
import net.dungeonrealms.game.quests.QuestNPC;
import net.dungeonrealms.game.quests.QuestStage;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.gui.GuiBase;
import net.dungeonrealms.game.quests.gui.GuiStageEditor;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ObjectiveNextNPC implements QuestObjective {

    private QuestStage stage;

    @Override
    public GuiBase createEditorGUI(Player player, QuestStage stage) {
        return new GuiStageEditor(player, stage);
    }

    @Override
    public boolean isCompleted(Player player, QuestStage stage, QuestNPC currentNPC) {
        return currentNPC == stage.getNPC();
    }

    @Override
    public String getName() {
        return "Speak";
    }

    @Override
    public Material getIcon() {
        return Material.LEASH;
    }

    @Override
    public String[] getDescription() {
        return new String[]{"Speak to the next NPC."};
    }

    @Override
    public String getTaskDescription(Player player, QuestStage stage) {
        if (stage == null || stage.getNPC() == null)
            return "Cannot find next NPC in Quest.";

        if (stage.getNPC().getLocation() != null)
            player.setCompassTarget(stage.getNPC().getLocation());

        return "Speak to " + stage.getNPC().getName() + " " + Quests.getRegionDirections(stage.getNPC().getLocation());
    }

    @Override
    public JsonObject saveJSON() {
        return new JsonObject();
    }

    @Override
    public void loadJSON(JsonObject o) {

    }

    @Override
    public void setQuestStage(QuestStage qs) {
        this.stage = qs;
    }

    @Override
    public void onStart(Player player) {
        //Does not work? Has to do with something scoreboard related.
        /*if(this.stage.getNPC() != null)
			this.stage.getNPC().setGlowing(player, ChatColor.GREEN);*/
//        if (stage.getNPC().getLocation() != null)
//            player.setCompassTarget(stage.getNPC().getLocation());
    }

    @Override
    public void onEnd(Player player) {
		/*if(this.stage.getNPC() != null && this.stage.getNPC().isLoaded())
			GlowAPI.setGlowing(this.stage.getNPC().getNPCEntity().getEntity(), false, player);*/
		player.setCompassTarget(TeleportLocation.CYRENNICA.getLocation());
    }
}
