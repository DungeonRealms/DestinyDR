package net.dungeonrealms.old.game.command.dungeon;

import net.dungeonrealms.common.game.command.BaseCommand;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kieran Quigley (Proxying) on 16-Jun-16.
 */
public class ReplaceNear extends BaseCommand {
    public ReplaceNear(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length != 3) { return true; }
        if(!(sender instanceof BlockCommandSender)) { return true; }

        BlockCommandSender cb = (BlockCommandSender) sender;

        int radius = Integer.parseInt(args[0]);
        int from_id = Integer.parseInt(args[1]);
        int to_id = Integer.parseInt(args[2]);

        for(Block b : getNearbyBlocks(cb.getBlock().getLocation(), radius)) {
            if(b.getTypeId() == from_id) {
                b.setType(Material.getMaterial(to_id));
            }
        }
        return true;
    }

    private List<Block> getNearbyBlocks(Location loc, int maxradius) {
        List<Block> return_list = new ArrayList<>();
        BlockFace[] faces = { BlockFace.UP, BlockFace.NORTH, BlockFace.EAST };
        BlockFace[][] orth = { { BlockFace.NORTH, BlockFace.EAST }, { BlockFace.UP, BlockFace.EAST }, { BlockFace.NORTH, BlockFace.UP } };
        for (int r = 0; r <= maxradius; r++) {
            for (int s = 0; s < 6; s++) {
                BlockFace f = faces[s % 3];
                BlockFace[] o = orth[s % 3];
                if (s >= 3)
                    f = f.getOppositeFace();
                if (!(loc.getBlock().getRelative(f, r) == null)) {
                    Block c = loc.getBlock().getRelative(f, r);

                    for (int x = -r; x <= r; x++) {
                        for (int y = -r; y <= r; y++) {
                            Block a = c.getRelative(o[0], x).getRelative(o[1], y);
                            return_list.add(a);
                        }
                    }
                }
            }
        }
        return return_list;
    }
}
