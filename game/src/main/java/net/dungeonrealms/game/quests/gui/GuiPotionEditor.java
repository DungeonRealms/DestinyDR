package net.dungeonrealms.game.quests.gui;

import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.quests.DialogueLine;
import net.dungeonrealms.game.quests.QuestStage;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

public class GuiPotionEditor extends GuiBase{
	
	private QuestStage stage;
	private DialogueLine dialogueLine;
	
	public GuiPotionEditor(Player player, QuestStage stage, DialogueLine dialogueLine) {
		super(player, "Potion Editor", 1);
		this.stage = stage;
		this.dialogueLine = dialogueLine;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void createGUI(){
		int i;
		for(i = 0; i < this.dialogueLine.getPotionEffects().size(); i++){
			PotionEffect pe = this.dialogueLine.getPotionEffects().get(i);
			ItemStack potion = new ItemStack(Material.POTION);
			PotionMeta meta = (PotionMeta)potion.getItemMeta();
			meta.setDisplayName(ChatColor.BLUE + pe.getType().getName());
			//new String[] {/*"Level: " + ChatColor.BLUE + pe.getAmplifier(), "Duration: " + ChatColor.BLUE + pe.getDuration() + ChatColor.GRAY + " Ticks"*/});
			meta.setBasePotionData(new PotionData(PotionType.getByEffect(pe.getType())));
			potion.setItemMeta(meta);
			this.setSlot(i, potion, (evt) -> {
				if(evt.isRightClick()){
					this.dialogueLine.getPotionEffects().remove(evt.getRawSlot());
					this.player.sendMessage(ChatColor.RED + "Potion Deleted");
					new GuiPotionEditor(player, stage, dialogueLine);
					return;
				}
				player.sendMessage(ChatColor.YELLOW + "How long should this potion last in seconds?");
				Chat.listenForNumber(player, 0, 1000, (dur) -> {
					player.sendMessage(ChatColor.GREEN + "Potion Duration Updated. Please enter the new potion level.");
					Chat.listenForNumber(player, 0, 100, (lvl) -> {
						this.dialogueLine.getPotionEffects().set(evt.getRawSlot(), new PotionEffect(pe.getType(), dur * 20, lvl));
						player.sendMessage(ChatColor.GREEN + "Potion Level Updated.");
						new GuiPotionEditor(player, stage, dialogueLine);
					}, () -> new GuiPotionEditor(player, stage, dialogueLine));
				}, () -> new GuiPotionEditor(player, stage, dialogueLine));
			});
		}
		
		this.setSlot(i, Material.POTION, ChatColor.GREEN + "Add Effect", new String[]{"Click to add an effect."}, (evt) -> new GuiPotionPicker(player, stage, dialogueLine));
		
		this.setSlot(this.getInventory().getSize() - 1, GO_BACK, (evt) -> new GuiDialogueEditor(player, stage, dialogueLine));
	}
}
