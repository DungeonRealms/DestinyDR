package net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates;

import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.inventory.menus.guis.MountSkinSelectionGUI;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMountSkins;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Created by Rar349 on 7/9/2017.
 */
public class MountSkinCrateReward extends AbstractCrateReward {

    private EnumMountSkins skin;

    public MountSkinCrateReward(EnumMountSkins skin) {
        super(skin.getSelectionItem().getType(), (byte)skin.getSelectionItem().getDurability(), ChatColor.GREEN + skin.getDisplayName(), skin.getDescription());
        this.skin = skin;
    }

    @Override
    public void giveReward(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        wrapper.getMountSkins().add(skin);
    }

    @Override
    public boolean canReceiveReward(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        return !wrapper.getMountSkins().contains(skin);
    }
}
