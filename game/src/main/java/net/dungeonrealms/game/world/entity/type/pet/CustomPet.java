package net.dungeonrealms.game.world.entity.type.pet;

import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.entity.util.PetUtils;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class CustomPet extends EntitySilverfish {

    public CustomPet(World world, Player owner) {
        super(world);
        setInvisible(true);

        EntityAPI.clearAI(goalSelector, targetSelector);
        goalSelector.a(0, new PetUtils.PathfinderGoalWalkToTile(this, owner, 1D));
    }

    @Override
    protected SoundEffect bT() {
        return SoundEffects.eI;
    }

    @Override
    protected SoundEffect bS() {
        return SoundEffects.eI;
    }

    @Override
    protected SoundEffect G() {
        return SoundEffects.eI;
    }

    //Take us with you.
    @Override
    public Entity teleportTo(Location exit, boolean portal) {
        //Clear passengers.
        az();
        Entity retr = super.teleportTo(exit, portal);
        setupArmorStands();
        return retr;
    }

    public void setupArmorStands() {
        createArmorStand();
        createArmorStand();
        createArmorStand();
    }

    @Override
    public boolean isInteractable() {
        return true;
    }

    @Override
    public EnumInteractionResult a(EntityHuman entityhuman, Vec3D vec3d, @Nullable net.minecraft.server.v1_9_R2.ItemStack itemstack, EnumHand enumhand) {
        Player player = (Player) entityhuman.getBukkitEntity();
        player.sendMessage(ChatColor.RED + "Please enter the index to set for the armor stands.");
        player.sendMessage("EX: 0 360 0 180 to set passenger 0 to that head pose.");
        Chat.listenForMessage(player, e -> {
            String[] args = e.getMessage().split(" ");
            int index = Integer.parseInt(args[0]);
            float x = Float.parseFloat(args[1]);
            float y = Float.parseFloat(args[2]);
            float z = Float.parseFloat(args[3]);

            if (index >= passengers.size()) {
                e.getPlayer().sendMessage(ChatColor.RED + "Passengers: " + passengers.size());
                return;
            }
            EntityArmorStand armorStand = (EntityArmorStand) passengers.get(index);
            armorStand.setHeadPose(new Vector3f(x, y, z));
            player.sendMessage(ChatColor.RED + "Set Pose to " + x + " Y: " + y + " Z: " + z);
        }, cancel -> {
            player.sendMessage(ChatColor.RED + " Cancelled.");
        });
        return EnumInteractionResult.SUCCESS;
//        return super.a(entityhuman, vec3d, itemstack, enumhand);
    }

    public Entity createArmorStand() {
        EntityArmorStand stand = new EntityArmorStand(this.world, this.locX, this.locY, this.locZ);
        this.world.addEntity(stand, CreatureSpawnEvent.SpawnReason.CUSTOM);
        //TODO, Customize pitch / yaw?
        stand.setLocation(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
        stand.startRiding(this);
        stand.setArms(false);
        stand.setSmall(true);
        stand.setInvisible(true);
        stand.setBasePlate(false);
        stand.setHeadPose(new Vector3f(360, 360, 360));
        stand.collides = false;

        ArmorStand armorStand = (ArmorStand) stand.getBukkitEntity();
        armorStand.setHelmet(new ItemStack(Material.SKULL_ITEM, 1, (short) 3));
        MetadataUtils.Metadata.ENTITY_TYPE.set(stand.getBukkitEntity(), EnumEntityType.PET_PART);
        return stand;
    }
}
