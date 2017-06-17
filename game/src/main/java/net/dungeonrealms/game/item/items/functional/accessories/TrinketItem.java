package net.dungeonrealms.game.item.items.functional.accessories;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.Tuple;
import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.item.items.functional.ItemFish;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class TrinketItem extends FunctionalItem implements ItemInventoryEvent.ItemInventoryListener {

    @Getter
    public Integer value;

    @Getter
    private Trinket trinket;

    @Getter
    private TrinketType trinketType;

    private EnchantTrinketData storedData;

    public TrinketItem(ItemStack item) {
        super(item);
        if (hasTag("value"))
            this.value = getTagInt("value");

        if (hasTag("trinket"))
            this.trinket = Trinket.getFromName(getTagString("trinket"));

        if (hasTag("trinketType"))
            this.trinketType = TrinketType.valueOf(getTagString("trinketType"));

        if (this.trinket == Trinket.COMBAT && hasTag("attribute")) {
            //Load trinket data?
            this.storedData = new EnchantTrinketData(Item.AttributeType.getByName(getTagString("attribute")), -1, -1);
        }
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.SHEARS, 1, (short) trinketType.getMaterial().getData());
    }

    public TrinketItem(TrinketType trinketType) {
        this(trinketType, trinketType.getRandomTrinket());
    }

    public TrinketItem(TrinketType trinketType, Trinket trinket) {
        super(ItemType.TRINKET);

        this.trinketType = trinketType;
        this.trinket = trinket;
        if (this.trinket == Trinket.COMBAT) {
            Tuple<Item.AttributeType, Integer> value = ((RandomEnchantTrinketData) trinket.getData()).getRandomAttribute();
            this.storedData = new EnchantTrinketData(value.a(), -1, -1);
            this.value = value.b();
        } else {
            this.value = this.trinket.getValue();
        }
    }

    @Override
    public void updateItem() {
        if (value != null)
            setTagInt("value", value);

        if (trinket != null) {
            setTagString("trinket", trinket.name());
            if (trinket == Trinket.FISH_SCALER) {
                setUndroppable(true);
            }
        }
        if (trinketType != null)
            setTagString("trinketType", trinketType.name());

        if (this.storedData != null) {
            setTagString("attribute", storedData.getType().getNBTName());
        }
        super.updateItem();
    }

    @Override
    public ItemStack generateItem() {
        ItemStack item = super.generateItem();
        ItemMeta im = item.getItemMeta();
        im.spigot().setUnbreakable(true);
        im.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
        item.setDurability(getTrinketType().getMaterial().getData());
        item.setItemMeta(im);
        return item;
    }

    @Override
    public String getDisplayName() {
        if (trinket == Trinket.FISH_SCALER) return trinketType.getNameColor() + "Fish Scaler";

        String name = trinketType.getName();
        String prefix = trinket.getPrefix();
        if (prefix != null) {
            name = prefix + " " + name;
        }
        if (getTrinket() == Trinket.COMBAT) {
            String pre = ((EnchantTrinketData) getTrinketData()).getDisplayPrefix();
            if (pre != null && !pre.isEmpty())
                name = pre.trim() + " " + name;
        }

        if (trinket.getSuffix() != null)
            name = name + " of " + trinket.getSuffix();

        if (getTrinket() == Trinket.COMBAT) {

            String suff = ((EnchantTrinketData) getTrinketData()).getDisplaySuffix(false);
            if (suff != null && !suff.trim().isEmpty()) {
                name = name + " of " + suff.trim();
            }
        }
        return trinketType.getNameColor() + name;
    }

    public TrinketData getTrinketData() {
        if (this.storedData != null) return storedData;

        return getTrinket().getData();
    }

    @Override
    protected String[] getLore() {

        List<String> lore = Lists.newArrayList();
        if (getTrinketData() instanceof EnchantTrinketData && value != null) {
            EnchantTrinketData data = (EnchantTrinketData) getTrinketData();
            lore.add(ChatColor.RED.toString() + data.getType().getPrefix() + (data.getType() == Item.WeaponAttributeType.DAMAGE ? "+" : "") + getValue() + data.getType().getSuffix().trim());
        } else {
            lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString() + getTrinketData().getDescription());
        }
        lore.add("");
        lore.add(getTrinket().getItemRarity().getName());
        if (trinket != Trinket.FISH_SCALER) {
            lore.add(ChatColor.GRAY + "Usable only in Trinket Slot");
        } else {
            lore.add(ChatColor.GRAY + "Use on Raw Fish to scale");

        }
        return lore.toArray(new String[lore.size()]);
    }

    @Override
    protected ItemUsage[] getUsage() {
        return getTrinket() == Trinket.FISH_SCALER ? new ItemUsage[]{ItemUsage.INVENTORY_SWAP_PLACE} : new ItemUsage[]{};
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TrinketItem) {
            TrinketItem item = (TrinketItem) obj;
            if (getTrinket().equals(item.getTrinket()) && (getValue() != null && getValue().equals(item.getValue()) || getValue() == null && item.getValue() == null) && trinketType.equals(item.getTrinketType())) {
                return true;
            }
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return getTrinket().name() + " Val: " + getValue() + " Type: " + trinketType.name();
    }


    @Override
    public void onInventoryClick(ItemInventoryEvent evt) {
        if (getTrinket() == Trinket.FISH_SCALER) {
            evt.setCancelled(true);

            PersistentItem item = PersistentItem.constructItem(evt.getSwappedItem());

            if (item != null && item instanceof ItemFish) {
                ItemFish fish = (ItemFish) item;
                if (fish.isCooked()) {
                    evt.getPlayer().sendMessage(ChatColor.RED + "You can only scale Raw Fish!");
                    return;
                }
                if (fish.getFishBuff() != null) {
                    //Clear fish buff.
                    fish.setFishBuff(null);
                    fish.removeTag("buffType");
                    fish.removeTag("fishVal");

                    ItemStack is = fish.generateItem();
                    is.setAmount(evt.getSwappedItem().getAmount());
                    is.setDurability(evt.getSwappedItem().getDurability());
                    evt.setSwappedItem(is);

                    Player player = evt.getPlayer();
                    new BukkitRunnable() {
                        int timer = 0;

                        @Override
                        public void run() {
                            if (player.isDead() || !player.isOnline() || timer >= 3) {
                                cancel();
                                return;
                            }

                            timer++;
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1, 1.5F);
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 3, .9F);

                            ParticleAPI.spawnBlockParticles(player.getLocation().clone().add(.5, 2, .5), Material.CLAY);
//                            ParticleAPI.spawnBlockParticles(player.getLocation().clone().add(.5, 2, .5), Material.);
                        }
                    }.runTaskTimer(DungeonRealms.getInstance(), 5, 5);
                } else {
                    evt.getPlayer().sendMessage(ChatColor.GRAY + "This fish is already clean!");
                }
            }
        }
    }
}
