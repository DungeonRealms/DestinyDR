package net.dungeonrealms.game.quests.gui;

import net.dungeonrealms.game.player.inventory.ShopMenu;
import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.quests.QuestNPC;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.world.item.CC;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Consumer;

public class GuiNPCPicker extends GuiBase {

    private Quest quest;
    private Consumer<QuestNPC> onPick;

    private int page = 1;

    public GuiNPCPicker(Player player, Quest quest, Consumer<QuestNPC> onPick) {
        super(player, "Pick the NPC you'd like to add.", (ShopMenu.fitSize(Quests.getInstance().npcStore.getList()) + 9) / 9);
        this.quest = quest;
        this.onPick = onPick;
    }

    @Override
    public void createGUI() {
        getInventory().clear();
        clearCallbacks();
        List<QuestNPC> npcList = Quests.getInstance().npcStore.getList();

        int perPage = 54;
        int maxPages = (int) Math.ceil(npcList.size() / 54D);
        if (npcList.size() > 54) {
            //Handle more then 1  page?
            if (page > maxPages) page = maxPages;
        }

        int start = (page * perPage) - perPage;
        int slot = 0;
        for (int i = start; i < start + perPage; i++) {
            if (i >= npcList.size()) break;
            if(slot >= getSize())break;
            QuestNPC npc = npcList.get(i);
            this.setSlot(slot++, Quests.createSkull(npc.getSkinOwner(), ChatColor.GREEN + npc.getName(), new String[]{"Left Click to Add an NPC from the bank."}), (evt) -> this.onPick.accept(npc));
        }

        if (page >= 1 && page < maxPages) {
            //Next arrow
            this.setSlot(this.getSize() - 1, Material.ARROW, CC.Aqua + "Next Page", e -> {
                page = page + 1;
                createGUI();
            }, "Click to view next page.");
        }

        if(page > 1){
            //Previous Page?
            this.setSlot(this.getSize() - 8, Material.ARROW, CC.Aqua + "Previous Page", e -> {
                page = page - 1;
                createGUI();
            }, "Click to view previous page.");
        }

        this.setSlot(this.getSize() - 5, GO_BACK, (evt) -> new GuiQuestEditor(player, quest));
    }
}
