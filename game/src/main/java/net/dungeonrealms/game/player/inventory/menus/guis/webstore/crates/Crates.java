package net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates;

import lombok.Getter;
import net.dungeonrealms.common.game.database.player.PlayerRank;
import net.dungeonrealms.game.item.items.functional.ItemTeleportBook;
import net.dungeonrealms.game.item.items.functional.accessories.Trinket;
import net.dungeonrealms.game.item.items.functional.ecash.ItemGlobalMessager;
import net.dungeonrealms.game.item.items.functional.ecash.ItemLoreBook;
import net.dungeonrealms.game.item.items.functional.ecash.ItemRetrainingBook;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.Purchaseables;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates.impl.VoteCrate;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMountSkins;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by Rar349 on 7/6/2017.
 */
@Getter
public enum Crates {

    VOTE_CRATE(VoteCrate.class, "Mystery Vote Chest", new AbstractCrateReward[] {new TrinketCrateReward(Trinket.REDUCED_REPAIR),new RankCrateReward(PlayerRank.SUB,30),new PurchaseableCrateReward(Purchaseables.CLOUD_PLAYER_AURA),new PurchaseableCrateReward(Purchaseables.CLOUD_CHEST_AURA),new PurchaseableCrateReward(Purchaseables.CLOUD_REALM_AURA),new PurchaseableCrateReward(Purchaseables.GOLDEN_CURSE),new PurchaseableCrateReward(Purchaseables.PROFESSION_BUFF_40),new PurchaseableCrateReward(Purchaseables.LOOT_BUFF_40),new PurchaseableCrateReward(Purchaseables.DRAGON_MASK),new PurchaseableCrateReward(Purchaseables.CRATE_HAT), new DiscountCrateReward(75) },
            new AbstractCrateReward[] {new RankCrateReward(PlayerRank.SUB,7),new MountSkinCrateReward(EnumMountSkins.SKELETON_HORSE),new MountSkinCrateReward(EnumMountSkins.ZOMBIE_HORSE),new DiscountCrateReward(50), new PurchaseableCrateReward(Purchaseables.ITEM_NAME_TAG,5), new PurchaseableCrateReward(Purchaseables.PROFESSION_BUFF_30), new PurchaseableCrateReward(Purchaseables.LOOT_BUFF_30)},
            new AbstractCrateReward[] {new RankCrateReward(PlayerRank.SUB,1),new DiscountCrateReward(25), new PurchaseableCrateReward(Purchaseables.LIGHTNING_ROD), new PurchaseableCrateReward(Purchaseables.PROFESSION_BUFF_20), new PurchaseableCrateReward(Purchaseables.LOOT_BUFF_20), new RetrainingBookCrateReward(), new TrailCrateReward(ParticleAPI.ParticleEffect.FIREWORKS_SPARK), new TrailCrateReward(ParticleAPI.ParticleEffect.TOWN_AURA), new TrailCrateReward(ParticleAPI.ParticleEffect.CRIT), new TrailCrateReward(ParticleAPI.ParticleEffect.CRIT_MAGIC), new TrailCrateReward(ParticleAPI.ParticleEffect.SPELL_WITCH), new TrailCrateReward(ParticleAPI.ParticleEffect.NOTE), new TrailCrateReward(ParticleAPI.ParticleEffect.PORTAL), new TrailCrateReward(ParticleAPI.ParticleEffect.ENCHANTMENT_TABLE), new TrailCrateReward(ParticleAPI.ParticleEffect.FLAME), new TrailCrateReward(ParticleAPI.ParticleEffect.WATER_SPLASH), new TrailCrateReward(ParticleAPI.ParticleEffect.REDSTONE), new TrailCrateReward(ParticleAPI.ParticleEffect.SNOWBALL), new TrailCrateReward(ParticleAPI.ParticleEffect.SMOKE_NORMAL), new TrailCrateReward(ParticleAPI.ParticleEffect.CLOUD), new TrailCrateReward(ParticleAPI.ParticleEffect.VILLAGER_HAPPY), new TrailCrateReward(ParticleAPI.ParticleEffect.SNOW_SHOVEL), new TrailCrateReward(ParticleAPI.ParticleEffect.HEART)},
            new AbstractCrateReward[] {new PurchaseableCrateReward(Purchaseables.LOOT_AURA),new LoreBookCrateReward(),new DiscountCrateReward(10), new PurchaseableCrateReward(Purchaseables.ITEM_NAME_TAG), new GlobalMessengerCrateReward(), new PurchaseableCrateReward(Purchaseables.LEVEL_BUFF_20)},
            new AbstractCrateReward[] {new GemCrateReward(), new TeleportBookCrateReward(), new WisdomCrateReward()});

    Class<? extends Crate> clas;
    String displayName;
    AbstractCrateReward[] insaneRewards;
    AbstractCrateReward[] veryRareRewards;
    AbstractCrateReward[] rareRewards;
    AbstractCrateReward[] uncommonRewards;
    AbstractCrateReward[] commonRewards;
    Crates(Class<? extends Crate> clas, String displayName, AbstractCrateReward[] insaneRewards, AbstractCrateReward[] veryRareRewards, AbstractCrateReward[] rareRewards, AbstractCrateReward[] uncommonRewards, AbstractCrateReward[] commonRewards) {
        this.clas = clas;
        this.displayName = displayName;
        this.insaneRewards = insaneRewards;
        this.veryRareRewards = veryRareRewards;
        this.rareRewards = rareRewards;
        this.uncommonRewards = uncommonRewards;
        this.commonRewards = commonRewards;
    }

    public Crate getCrate(Player viewing, Location toPlay) {
        try {
            return clas.getConstructor(Player.class, Location.class).newInstance(viewing,toPlay);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }
}
