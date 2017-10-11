package net.dungeonrealms.game.quests.gui;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.inventory.ShopMenu;
import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.quests.QuestNPC;
import net.dungeonrealms.game.quests.QuestStage;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.world.item.CC;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class GuiNPCBank extends GuiBase {

    int page = 1;

    public GuiNPCBank(Player player) {
        super(player, "NPC Bank", (int) (ShopMenu.fitSize(Quests.getInstance().npcStore.getList().size()) + 9) / 9);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void createGUI() {

        this.clearCallbacks();
        getInventory().clear();

        List<QuestNPC> npcList = Quests.getInstance().npcStore.getList();

        int perPage = 54;
        int maxPages = (int) Math.ceil(npcList.size() / 54D);
        if (npcList.size() > 54) {
            //Handle more then 1  page?
            if (page > maxPages) page = maxPages;
        }

        int start = Math.max(0, page * perPage - perPage);
        int slot = 0;

        for (int i = start; i < start + perPage; i++) {
            if (i >= npcList.size()) break;
            if(slot >= getSize() - 9)break;
            QuestNPC npc = npcList.get(i);
            this.setSlot(slot++, Quests.createSkull(npc.getSkinOwner(), ChatColor.GREEN + npc.getName(),
                    new String[]{"Left Click to " + ChatColor.GREEN + "Edit" + ChatColor.GRAY + ".", "Right Click to " + ChatColor.RED + "Delete" + ChatColor.GRAY + "."}),
                    (evt) -> {
                if (evt.isRightClick()) {
                    player.sendMessage(ChatColor.RED + "Are you sure you want to delete " + npc.getName() + "?");
                    Chat.promptPlayerConfirmation(player, () -> {
                        for (Quest q : Quests.getInstance().questStore.getList()) {
                            for (QuestStage s : q.getStageList()) {
                                if (s.getNPC() != null && s.getNPC().getName().equals(npc.getName())) {
                                    player.sendMessage(ChatColor.RED + "Cannot Delete. This NPC is in use by " + q.getQuestName() + ".");
                                    new GuiNPCBank(player);
                                    return;
                                }
                            }
                        }
                        player.sendMessage(ChatColor.RED + "Deleted.");
                        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), npc.getNPCEntity()::destroy);
                        npcList.remove(evt.getRawSlot());
                        Quests.getInstance().npcStore.delete(npc);
                        new GuiNPCBank(player);
                    }, () -> new GuiNPCBank(player));
                    return;
                }

                new GuiNPCEditor(player, npc);
            });
        }
        if (page >= 1 && page < maxPages) {
            //Next arrow
            this.setSlot(this.getSize() - 1, Material.ARROW, CC.Aqua + "Next Page", e -> {
                page = page + 1;
                createGUI();
            }, "Click to view next page.");
        }

        if (page > 1) {
            //Previous Page?
            this.setSlot(this.getSize() - 9, Material.ARROW, CC.Aqua + "Previous Page", e -> {
                page = page - 1;
                createGUI();
            }, "Click to view previous page.");
        }
        this.setSlot(this.getSize() - 3, Material.WOOL, DyeColor.GREEN.getWoolData(), ChatColor.GREEN + "Create NPC", new String[]{"Click here to create a NPC!"}, (evt) -> {
            player.sendMessage(ChatColor.YELLOW + "Please enter the name of this new NPC.");
            Chat.listenForMessage(player, (event) -> {
                if (event.getMessage().length() > 16) {
                    player.sendMessage(ChatColor.RED + "Name cannot be larger than 16 characters.");
                    new GuiNPCBank(player);
                    return;
                }
                if (Quests.getInstance().getNPCByName(event.getMessage()) != null) {
                    player.sendMessage(ChatColor.RED + "There is already an NPC with this name.");
                    new GuiNPCBank(player);
                    return;
                }
                Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
                    QuestNPC newNPC = new QuestNPC(event.getMessage(), player.getLocation());
                    Quests.getInstance().npcStore.getList().add(newNPC);
                    new GuiNPCEditor(player, newNPC);
                });
            }, p -> new GuiNPCBank(player));
        });

        this.setSlot(this.getSize() - 5, Material.SIGN, ChatColor.YELLOW + "NPC Bank", new String[]{"Left Click = " + ChatColor.GREEN + "Edit", "Right Click = " + ChatColor.RED + "Delete"});
        this.setSlot(this.getSize() - 7, GO_BACK, (evt) -> new GuiQuestSelector(player));
    }
}
