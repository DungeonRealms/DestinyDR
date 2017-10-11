package net.dungeonrealms.game.mechanic.cutscenes;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class CutScene {
    private String name, title, description;
    private double length;
    private transient long startTime;
    private int tickSpeed = 1, titleLength;
    private List<GsonLocation> locations;

    public CutScene() {
    }

    public GsonLocation getStart() {
        return locations.isEmpty() ? null : locations.get(0);
    }
}
