package net.dungeonrealms.game.command.moderation;

import com.google.common.collect.Lists;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.mechanic.ReflectionAPI;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.minecraft.server.v1_9_R2.EntityInsentient;
import net.minecraft.server.v1_9_R2.PathfinderGoal;
import net.minecraft.server.v1_9_R2.PathfinderGoalSelector;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;

public class CommandMobDebug extends BaseCommand {
    public CommandMobDebug() {
        super("mobdebug", "/<command>", "Mob debug", Lists.newArrayList("mdebug"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp()) return true;

        Player player = (Player) sender;

        if (player.hasMetadata("mob_debug"))
            player.removeMetadata("mob_debug", DungeonRealms.getInstance());
        else
            player.setMetadata("mob_debug", new FixedMetadataValue(DungeonRealms.getInstance(), ""));

        player.sendMessage(ChatColor.RED + "Mob debug toggled.");
        return false;
    }

    public static void debugEntity(Player player, Entity entity) {
        player.sendMessage(ChatColor.RED + "Entity Type: " + entity.getType());

        net.minecraft.server.v1_9_R2.Entity handle = ((CraftEntity) entity).getHandle();

        if (handle instanceof EntityInsentient) {
            EntityInsentient ent = (EntityInsentient) handle;
            player.sendMessage(ChatColor.AQUA + "Entity Class: " + handle.getClass());
            player.sendMessage(ChatColor.GREEN + "Alive Time: " + entity.getTicksLived());
            player.sendMessage(ChatColor.AQUA + "Attributes:");
            AttributeList list = EntityAPI.getAttributes(entity);
            list.keySet().forEach(attrb -> player.sendMessage(attrb.getNBTName() + " - " + list.getAttribute(attrb).getValue()));
            player.sendMessage(ChatColor.GREEN + "Health: " + HealthHandler.getHP(entity) + " / " + HealthHandler.getMaxHP(entity));
            player.sendMessage(ChatColor.GREEN + "Target: " + (ent.getGoalTarget() != null ? ent.getGoalTarget().getName() : null));
            player.sendMessage(ChatColor.AQUA + "Goal Selector:");
            sendGoalSelector(player, ent.goalSelector);

            player.sendMessage(ChatColor.RED + "Target Selector:");
            sendGoalSelector(player, ent.targetSelector);

        }
    }

    private static void sendGoalSelector(Player player, PathfinderGoalSelector selector) {
        Field bSet = ReflectionAPI.getDeclaredField(selector.getClass(), "b");
        Field cSet = ReflectionAPI.getDeclaredField(selector.getClass(), "c");

        try {
            //PathfinderGoalSelector.PathfinderGoalSelectorItem
            LinkedHashSet<?> b = (LinkedHashSet) bSet.get(selector);
            LinkedHashSet<?> c = (LinkedHashSet) cSet.get(selector);

            player.sendMessage(ChatColor.RED + "b");
            sendGoalList(player, b);

            player.sendMessage("");
            player.sendMessage(ChatColor.RED + "c");
            sendGoalList(player, c);

            player.sendMessage("");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Unable to get selectors: " + e.getMessage());
        }
    }

    private static void sendGoalList(Player player, LinkedHashSet<?> set) throws IllegalAccessException {

        for (Object o : set) {
            Field goal = ReflectionAPI.getDeclaredField(o.getClass(), "a");
            Field priorityField = ReflectionAPI.getDeclaredField(o.getClass(), "b");
            int priority = (int) priorityField.get(o);
            PathfinderGoal pathGoal = (PathfinderGoal) goal.get(o);

            player.sendMessage(ChatColor.RED.toString() + priority + " - " + pathGoal.getClass().getName());
        }
    }
}
