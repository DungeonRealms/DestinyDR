package net.dungeonrealms.game.item.items.functional.cluescrolls;

import lombok.Getter;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.item.items.functional.accessories.Trinket;
import net.dungeonrealms.game.item.items.functional.accessories.TrinketType;
import net.minecraft.server.v1_9_R2.NBTTagInt;
import net.minecraft.server.v1_9_R2.NBTTagList;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Rar349 on 6/13/2017.
 */
public class ClueScrollItem extends FunctionalItem {

    @Getter
    private ClueDifficulty difficulty;
    @Getter
    private ClueScrollType clueType;
    @Getter
    private Clue clue;
    @Getter
    private boolean hasBeenTranslated;
    private String jargon;
    @Getter
    private int progress;
    @Getter
    private int[] rolledRanges;
    private ClueRenderer renderer;
    private boolean isComplete;

    public ClueScrollItem(ClueScrollType scrollType) {
        this(scrollType, scrollType.getRandomClue());
    }

    public ClueScrollItem(ClueScrollType scrollType, Clue clue) {
        this(scrollType, scrollType.getRandomClue(), clue.getDefaultDifficulty());
    }

    public ClueScrollItem(ClueScrollType scrollType, Clue clue, ClueDifficulty difficulty) {
        super(ItemType.CLUE_SCROLL);
        setAntiDupe(true);
        this.clueType = scrollType;
        this.clue = clue;
        this.difficulty = difficulty;
        this.hasBeenTranslated = false;
        this.jargon = generateRandomJargon();
        this.progress = 0;
        if(clue.hasRangeStat())rollRanges();
        updateRenderer();
    }

    public ClueScrollItem(ItemStack stack) {
        super(stack);

        setAntiDupe(true);

        if (hasTag("difficulty")) this.difficulty = ClueDifficulty.valueOf(getTagString("difficulty"));

        if (hasTag("clueType")) this.clueType = ClueScrollType.valueOf(getTagString("clueType"));

        if (hasTag("clue")) this.clue = Clue.valueOf(getTagString("clue"));

        if(hasTag("translated")) this.hasBeenTranslated = getTagBool("translated");

        if(hasTag("jargon")) this.jargon = getTagString("jargon");

        if(hasTag("progress")) this.progress = getTagInt("progress");

        if(hasTag("ranges")) this.rolledRanges = getTag().getIntArray("ranges");

        if(hasTag("completed")) this.isComplete = getTagBool("completed");

        updateRenderer();
    }

    private void updateRenderer() {
        if(!hasBeenTranslated){
            if(renderer == null)this.renderer = new ClueRenderer(ClueRenderer.wrapLines(jargon));
            else this.renderer.setLines(ClueRenderer.wrapLines(jargon));
            return;
        }
        String line1 = this.clue.getFormattedHint(rolledRanges);
        String line2 = formatProgressString();

        if(renderer == null) {
            if (line2 != null && !line2.isEmpty())
                this.renderer = new ClueRenderer(ClueRenderer.wrapLines(line1, line2));
            else this.renderer = new ClueRenderer(ClueRenderer.wrapLines(line1));
        } else {
            if (line2 != null && !line2.isEmpty()) this.renderer.setLines(ClueRenderer.wrapLines(line1, line2));
            else this.renderer.setLines(ClueRenderer.wrapLines(line1));
        }
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

        this.rolledRanges = rolledRanges;
    }


    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.MAP);
    }

    @Override
    protected String getDisplayName() {
        if(!hasBeenTranslated) return ChatColor.YELLOW + "Tattered Parchment";
        return ChatColor.YELLOW + "Treasure Scroll";
    }

    @Override
    protected String[] getLore() {
        if(!hasBeenTranslated) return new String[] {ChatColor.GRAY + clueType.getTattered()};
        List<String> lore = new ArrayList<>();
        lore.add(" ");
        lore.add(ChatColor.GREEN + ChatColor.BOLD.toString() + "Objective");
        lore.add(ChatColor.GRAY + clue.getFormattedHint(rolledRanges));
        //String progress = ChatColor.GRAY + formatProgressString();
        //if(progress != null && progress.isEmpty())lore.add(progress);
        if(isComplete) lore.add(ChatColor.GREEN + ChatColor.BOLD.toString() + "COMPLETE");
        return wrapLines(lore.toArray(new String[lore.size()]));
        //return new String[] {ChatColor.GRAY + clue.getFormattedHint(rolledRanges), ChatColor.GRAY + formatProgressString()};
    }

    private String[] wrapLines(String... unwrappedLines) {
        List<String> lines = new ArrayList<>();
        int CHARACTERS_PER_LINE = 30;
        for(String line : unwrappedLines) {
            if(line.length() <= CHARACTERS_PER_LINE) {
                lines.add(line);
                lines.add(" ");
                continue;
            }
            String[] words = line.split(" ");
            String currentLine = words[0];
            for(int k = 1; k < words.length; k++) {
                String word = words[k];
                //Add 1 for the space.
                boolean fitsOnThisLine = currentLine.length() + word.length() + 1 <= CHARACTERS_PER_LINE;
                if(fitsOnThisLine) {
                    currentLine += " " + word;
                    if(k == words.length) {
                        lines.add(currentLine);
                        currentLine = "";
                        //lines.add(" ");
                    }
                    continue;
                }

                //currentLine += " " + word;

                lines.add(currentLine);
                currentLine = word;
            }
            if(!currentLine.isEmpty()) lines.add(currentLine);
            lines.add(" ");
        }

        return lines.stream().toArray(String[]::new);
    }

    @Override
    public void updateItem() {
        if (difficulty != null) setTagString("difficulty", this.difficulty.name());

        if (clueType != null) setTagString("clueType", this.clueType.name());

        if (clue != null) setTagString("clue", this.clue.name());

        if(rolledRanges != null) getTag().setIntArray("ranges", rolledRanges);

        if(isStarted() || isComplete()) setUntradeable(true);

        setTagBool("translated", this.hasBeenTranslated);

        setTagString("jargon", this.jargon);

        setTagInt("progress", this.progress);

        setTagBool("completed", this.isComplete);

        super.updateItem();
    }

    @Override
    public ItemStack generateItem() {
        ItemStack superItemStack = super.generateItem();
        MapView view = Bukkit.getServer().createMap(Bukkit.getWorlds().get(0));
        for(MapRenderer renderer : view.getRenderers()) view.removeRenderer(renderer);
        view.addRenderer(this.renderer);
        superItemStack.setDurability(view.getId());
        return superItemStack;
    }

    @Override
    protected ItemUsage[] getUsage() {
        return new ItemUsage[] {};
    }

    public ClueScrollItem setTranslated(boolean translated) {
        this.hasBeenTranslated = translated;
        if(this.renderer != null) updateRenderer();
        return this;
    }

    public void setProgress(int progress) {
        if(isComplete()) return;
        int maxProgress = rolledRanges[clue.getProgressRangeIndex()];
        this.progress = progress;
        updateRenderer();
        if(progress >= maxProgress) {
            //set
            this.isComplete = true;
        }
    }

    public boolean isComplete() {
        return this.isComplete;
    }

    public boolean isStarted() {
        return progress > 0;
    }


    protected static String generateRandomJargon() {
        String toReturn = RandomStringUtils.random(ThreadLocalRandom.current().nextInt(5,8), true, false);
        int words = ThreadLocalRandom.current().nextInt(3,6);
        for(int k = 0; k < words; k++) toReturn += " " + RandomStringUtils.random(ThreadLocalRandom.current().nextInt(5,8), true, false);
        return toReturn;
    }

    public String formatProgressString() {
        if(clue.getProgressString() == null || !clue.hasRangeStat()) return isComplete ? "COMPLETE" : "";
        int max = rolledRanges[clue.getProgressRangeIndex()];
        int current = this.progress;
        if(max == current) return "COMPLETE";
        return clue.getProgressString().replace("<progress>",String.valueOf(max - current));
    }

    public void handleCompleted() {
        this.isComplete = true;
        updateRenderer();
    }
}
