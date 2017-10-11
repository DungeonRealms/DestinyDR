package net.dungeonrealms.game.quests.gui;

import com.google.common.collect.Lists;
import net.dungeonrealms.game.quests.DialogueLine;
import net.dungeonrealms.game.quests.QuestStage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.List;

public class GuiPotionPicker extends GuiBase {

    private QuestStage stage;
    private DialogueLine dialogueLine;

    public GuiPotionPicker(Player player, QuestStage stage, DialogueLine dialogueLine) {
        super(player, "Select a Potion to add.", 4);
        this.stage = stage;
        this.dialogueLine = dialogueLine;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void createGUI() {
        int i = 0;
        for (PotionEffectType type : PotionEffectType.values()) {
            if (type == null)
                continue;
            ItemStack item = new ItemStack(Material.POTION);
            PotionType pType = PotionType.getByEffect(type);
            PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
            if (pType != null) {
                potionMeta.setBasePotionData(new PotionData(pType));
            }

            potionMeta.setDisplayName(ChatColor.AQUA + "Click Me to add this potion.");
            List<String> lore = potionMeta.getLore() == null ? Lists.newArrayList() : potionMeta.getLore();
            lore.add(ChatColor.AQUA + "Effect: " + type.getName());
            potionMeta.setLore(lore);
            item.setItemMeta(potionMeta);
            this.setSlot(i, item, (evt) -> {
                this.dialogueLine.getPotionEffects().add(new PotionEffect(type, 2, 100));
                new GuiPotionEditor(player, stage, dialogueLine);
            });
            i++;
        }
        this.setSlot(this.getSize() - 1, GO_BACK, (evt) -> new GuiPotionEditor(player, stage, dialogueLine));
    }
}
