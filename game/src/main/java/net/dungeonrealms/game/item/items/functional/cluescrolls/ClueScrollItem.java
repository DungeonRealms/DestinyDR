package net.dungeonrealms.game.item.items.functional.cluescrolls;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.item.items.functional.accessories.Trinket;
import net.dungeonrealms.game.item.items.functional.accessories.TrinketType;
import net.minecraft.server.v1_9_R2.NBTTagInt;
import net.minecraft.server.v1_9_R2.NBTTagList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Rar349 on 6/13/2017.
 */
public class ClueScrollItem extends FunctionalItem {

    private ClueDifficulty difficulty;
    private ClueScrollType clueType;
    private Clue clue;
    private boolean hasBeenTranslated;
    private String jargon;
    private int progress;
    private int[] rolledRanges;

    public ClueScrollItem(ClueScrollType scrollType, Clue clue, ClueDifficulty difficulty) {
        super(ItemType.CLUE_SCROLL);
        this.clueType = scrollType;
        this.clue = clue;
        this.difficulty = difficulty;
        this.hasBeenTranslated = false;
        this.jargon = generateRandomJargon();
        this.progress = 0;
    }

    public ClueScrollItem(ItemStack stack) {
        super(stack);
        if (hasTag("difficulty")) this.difficulty = ClueDifficulty.valueOf(getTagString("difficulty"));

        if (hasTag("clueType")) this.clueType = ClueScrollType.valueOf(getTagString("clueType"));

        if (hasTag("clue")) this.clue = Clue.valueOf(getTagString("clue"));

        if(hasTag("translated")) this.hasBeenTranslated = getTagBool("translated");

        if(hasTag("jargon")) this.jargon = getTagString("jargon");

        if(hasTag("progress")) this.progress = getTagInt("progress");

        if(hasTag("ranges")) this.rolledRanges = getTag().getIntArray("ranges");
    }

    public boolean hasRolledRanges() {
        return rolledRanges != null;
    }

    public void rollRanges() {
        int[] minRanges = clue.getMinRanges();
        int[] maxRanges = clue.getMaxRanges();
        int[] rolledRanges = new int[maxRanges.length];

        for(int index = 0; index < rolledRanges.length; index++) {
            int min = minRanges[index];
            int max = maxRanges[index];
            rolledRanges[index] = ThreadLocalRandom.current().nextInt(min,max + 1);
        }
    }


    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.EMPTY_MAP);
    }

    @Override
    protected String getDisplayName() {
        if(!hasBeenTranslated) return ChatColor.YELLOW + "Clue Scroll";
        return ChatColor.YELLOW + "Translated Clue Scroll";
    }

    @Override
    protected String[] getLore() {
        return new String[] {"", ChatColor.GRAY + clue.getFormattedHint(rolledRanges), ChatColor.GRAY + formatProgressString()};
    }

    @Override
    public void updateItem() {
        if (difficulty != null) setTagString("difficulty", this.difficulty.name());

        if (clueType != null) setTagString("clueType", this.clueType.name());

        if (clue != null) setTagString("clue", this.clue.name());

        if(rolledRanges != null) getTag().setIntArray("ranges", rolledRanges);

        setTagBool("translated", this.hasBeenTranslated);

        setTagString("jargon", this.jargon);

        setTagInt("progress", this.progress);

        super.updateItem();
    }

    @Override
    protected ItemUsage[] getUsage() {
        return new ItemUsage[] {};
    }

    public ClueScrollItem setTranslated(boolean translated) {
        this.hasBeenTranslated = translated;
        if(hasBeenTranslated && !hasRolledRanges()) rollRanges();
        return this;
    }

    public boolean isStarted() {
        return progress > 0;
    }


    public static String generateRandomJargon() {
        //Iwill make some basic jargon generator later
        return "Illegible text";
    }

    public String formatProgressString() {
        if(clue.getProgressString() == null) return "";
        int max = rolledRanges[clue.getProgressRangeIndex()];
        int current = this.progress;
        if(max == current) return "COMPLETE";
        return clue.getProgressString().replace("<progress>",String.valueOf(max - current));
    }
}
