package net.dungeonrealms.game.player.inventory.menus.staff;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;

/**
 * A GUI with all 'simple' custom items.
 * 
 * Created May 20th, 2017.
 * @author Kneesnap
 */
public class GUIItemBank extends GUIMenu {

	public GUIItemBank(Player player) {
		super(player, fitSize(getSimple()), "Item Bank");
		open(player, null);
	}
	
	@Override
	protected void setItems() {
		for (ItemType s : getSimple())
			addItem(new GUIItem(s.makeSimple().generateItem()).setClick(ice -> player.getInventory().addItem(ice.getCurrentItem())));
	}
	
	private static List<ItemType> getSimple() {
		List<ItemType> simple = new ArrayList<>();
		for (ItemType ty : ItemType.values())
			if (ty.isSimple())
				simple.add(ty);
		return simple;
	}

}
