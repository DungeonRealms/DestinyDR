package net.dungeonrealms.game.item.items.functional.cluescrolls;

import org.bukkit.entity.Player;
import org.bukkit.map.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Rar349 on 6/14/2017.
 */
public class ClueRenderer extends MapRenderer {

    public static final int CHARACTERS_PER_LINE = 23, LINE_HEIGHT = MinecraftFont.Font.getHeight() + 2;

    private String[] lines;
    private boolean hasChanged = true;
    public ClueRenderer(String... lines) {
        super();
        this.lines = lines;
        hasChanged = true;
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        if(hasChanged) {
            int totalY = LINE_HEIGHT * lines.length;
            int startY = (128 - totalY) / 2;
            for(String line : lines) {
                int width = MinecraftFont.Font.getWidth(line);
                int centeredX = (128 - width) / 2;
                mapCanvas.drawText(centeredX,startY, MinecraftFont.Font, line);
                startY += LINE_HEIGHT;
            }
            hasChanged = false;
        }
    }

    public void setLines(String... lines) {
        this.lines = lines;
        hasChanged = true;
    }

    public static String[] wrapLines(String... unwrappedLines) {
        List<String> lines = new ArrayList<>();
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
}
