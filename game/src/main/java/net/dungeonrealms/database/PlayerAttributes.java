package net.dungeonrealms.database;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.sql.ResultSet;

/**
 * Created by Rar349 on 4/15/2017.
 */
public class PlayerAttributes implements LoadableData, SaveableData {

    @Getter
    @Setter
    private int strength, dexterity, intellect, vitality, resets, pointsAvailable;

    @SneakyThrows
    public void extractData(ResultSet set)  {
        strength = set.getInt("attributes.strength");
        dexterity = set.getInt("attributes.dexterity");
        intellect = set.getInt("attributes.intellect");
        vitality = set.getInt("attributes.vitality");
        resets = set.getInt("attributes.resets_available");
        pointsAvailable = set.getInt("attributes.points_available");
    }

    public String getUpdateStatement() {
        return String.format("UPDATE attributes SET strength = '%s', dexterity = '%s', intellect = '%s', vitality = '%s', " +
                        "resets_available = '%s',  points_available = '%s' " +
                        "WHERE character_id = '%s';", getStrength(), getDexterity(), getIntellect(), getVitality(), getResets(), getPointsAvailable());
    }
}
