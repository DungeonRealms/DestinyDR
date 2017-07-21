package net.dungeonrealms.game.item.items.functional;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.common.util.ChatUtil;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.tool.coupon.CouponCodeGenerator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Rar349 on 7/7/2017.
 */
public class ItemDiscountScroll extends FunctionalItem implements ItemClickEvent.ItemClickListener {
    @Setter
    @Getter
    private int discountPercent = -1;
    @Setter
    private String discountCode = null;

    public ItemDiscountScroll(ItemStack item) {
        super(item);

        if(hasTag("discountCode")) this.discountCode = getTagString("discountCode");
        if(hasTag("discountPercent")) this.discountPercent = getTagInt("discountPercent");
        setAntiDupe(true);
    }

    public ItemDiscountScroll() {
        super(ItemType.COUPON_VOUCHER);
        setAntiDupe(true);
    }

    public ItemDiscountScroll(int discountPercent) {
        super(ItemType.COUPON_VOUCHER);
        this.discountPercent = discountPercent;
        setAntiDupe(true);
    }

    @Override
    public void updateItem() {
        super.updateItem();

        if(discountCode != null)setTagString("discountCode", this.discountCode);
        setTagInt("discountPercent", this.discountPercent);
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.PAPER);
    }

    @Override
    protected String getDisplayName() {
        return ChatColor.GREEN + "Discount Code Voucher";
    }

    @Override
    protected String[] getLore() {
        if(isScratchedOff()) {
            return new String[] {ChatColor.GRAY + "Use this discount code on the store for " + discountPercent + "% off!", "", ChatColor.GRAY + "Code: " + discountCode};
        }
        return new String[] {ChatColor.GRAY + "Right click to show a " + discountPercent + "% discount code!"};
    }

    @Override
    protected ItemUsage[] getUsage() {
        return new ItemUsage[] {ItemUsage.RIGHT_CLICK_AIR, ItemUsage.RIGHT_CLICK_BLOCK};
    }

    public boolean isScratchedOff() {
        return discountCode != null;
    }

    @Override
    public void onClick(ItemClickEvent evt) {
        if(isScratchedOff()) return;
        if(discountPercent <= 0) return;
        if(!evt.isRightClick()) return;
        Utils.sendCenteredMessage(evt.getPlayer(), ChatColor.RED + "====" + ChatColor.RED + " WARNING " + ChatColor.RED + "====");
        evt.getPlayer().sendMessage(ChatColor.GRAY + "After revealing the coupon code this voucher will become soulbound");
        evt.getPlayer().sendMessage(ChatColor.GRAY + "and only you will be able to use the code which will " + ChatColor.RED + ChatColor.UNDERLINE.toString() + "expire" + ChatColor.GRAY + " in 1 week");
        evt.getPlayer().sendMessage("");
        Utils.sendCenteredMessage(evt.getPlayer(),ChatColor.GRAY + "Type " + ChatColor.GREEN + "Y " + ChatColor.GRAY + "to confirm!");
        Chat.promptPlayerConfirmation(evt.getPlayer(), () -> {
            CouponCodeGenerator.generatePercentCouponCode(discountPercent, evt.getPlayer().getUniqueId(),(code) -> {
                if(code == null) {
                    evt.getPlayer().sendMessage(ChatColor.RED + "Failed to generate coupon code! Please try again later!");
                    return;
                }

                ItemStack handItem = evt.getPlayer().getInventory().getItemInMainHand();
                if(handItem == null || !handItem.getType().equals(Material.PAPER)) return;
                if(!PersistentItem.isType(handItem, ItemType.COUPON_VOUCHER)) return;
                ItemDiscountScroll scroll = new ItemDiscountScroll(handItem);
                if(scroll.isScratchedOff() || scroll.getDiscountPercent() != discountPercent) return;
                scroll.setDiscountCode(code);
                evt.getPlayer().getInventory().setItemInMainHand(scroll.generateItem());
            });
        }, () -> {

        });
    }
}
