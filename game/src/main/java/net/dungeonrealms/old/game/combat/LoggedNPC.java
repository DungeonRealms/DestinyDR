package net.dungeonrealms.old.game.combat;

import lombok.Getter;
import lombok.Setter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.old.game.database.DatabaseAPI;
import net.dungeonrealms.common.old.game.database.data.EnumData;
import net.dungeonrealms.old.game.handler.KarmaHandler;
import net.dungeonrealms.old.game.mastery.GamePlayer;
import net.dungeonrealms.old.game.mastery.ItemSerialization;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Matthew E on 10/29/2016 at 12:48 PM.
 */
public class LoggedNPC {

    @Getter
    private NPC npc;

    @Getter
    @Setter
    private int health;
    @Getter
    private final String name;
    @Getter
    private final UUID uuid;

    @Getter
    private int timeLeft;

    private Location location;

    private ItemStack[] contents;

    private ItemStack offhand;

    private ItemStack[] armor;

    private KarmaHandler.EnumPlayerAlignments alignment;

    private Player player;
    private GamePlayer gp;

    public LoggedNPC(GamePlayer gp) {
        this.player = gp.getPlayer();
        this.gp = gp;
        this.uuid = gp.getPlayer().getUniqueId();
        this.name = gp.getPlayer().getDisplayName();
        this.timeLeft = 10;
        this.location = gp.getPlayer().getLocation();
        this.alignment = gp.getPlayerAlignment();
        loadInventory();
        spawnNPC();
        new BukkitRunnable() {

            @Override
            public void run() {
                despawnNPC();
            }
        }.runTaskLater(DungeonRealms.getInstance(), (20L * timeLeft));
    }

    private DespawnCauseEnum despawnNPC() {
        if (isDead()) {
            kill();
            return DespawnCauseEnum.DEAD;
        } else {
            npc.destroy();
            return DespawnCauseEnum.SAFE;
        }
    }

    private boolean isDead() {
        return (health < 0);
    }

    private void kill() {

    }

    public enum DespawnCauseEnum {
        DEAD, SAFE

    }

    public void update() {

    }

    private void loadInventory() {
        String playerInv = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY, uuid);
        if (playerInv != null && playerInv.length() > 0 && !playerInv.equalsIgnoreCase("null")) {
            ItemStack[] items = ItemSerialization.fromString(playerInv, 36).getContents();
            this.contents = items;
        }
        List<String> playerArmor = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.ARMOR, uuid);
        int i = -1;
        ItemStack[] armorContents = new ItemStack[4];
        ItemStack offHand = new ItemStack(Material.AIR);
        for (String armor : playerArmor) {
            i++;
            if (i <= 3) { //Normal armor piece
                if (armor.equals("null") || armor.equals("")) {
                    armorContents[i] = new ItemStack(Material.AIR);
                } else {
                    armorContents[i] = ItemSerialization.itemStackFromBase64(armor);
                }
            } else {
                if (armor.equals("null") || armor.equals("")) {
                    offHand = new ItemStack(Material.AIR);
                } else {
                    offHand = ItemSerialization.itemStackFromBase64(armor);
                }
            }
        }
        this.armor = armorContents;
        this.offhand = offHand;
    }

    private void spawnNPC() {
        this.npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);
        npc.addTrait(Equipment.class);
        npc.getTrait(Equipment.class).set(1, (armor[1] != null) ?  armor[1] : new ItemStack(Material.AIR));
        npc.getTrait(Equipment.class).set(2, (armor[2] != null) ?  armor[2] : new ItemStack(Material.AIR));
        npc.getTrait(Equipment.class).set(3, (armor[3] != null) ?  armor[3] : new ItemStack(Material.AIR));
        npc.getTrait(Equipment.class).set(4, (armor[4] != null) ?  armor[4] : new ItemStack(Material.AIR));
        npc.getTrait(Equipment.class).set(Equipment.EquipmentSlot.OFF_HAND, (offhand != null) ? offhand : new ItemStack(Material.AIR));
        npc.getTrait(net.citizensnpcs.api.trait.trait.Inventory.class).setContents(contents);
        npc.data().setPersistent("combat_log_npc", true);
        npc.setName(alignment.getAlignmentColor() + name);
        npc.spawn(location);
    }
}