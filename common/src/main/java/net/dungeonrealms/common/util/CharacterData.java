package net.dungeonrealms.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.WordUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Rar349 on 6/20/2017.
 */
@Getter
@AllArgsConstructor
public class CharacterData {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, YY hh:mm aa");

    private int characterID;
    private String characterName;
    private int level;
    private int currentHP;
    private long timeCreated;
    private String alignment;
    private String characterType;
    private boolean isManuallyLocked;



    public String getTimeCreatedString() {
        return dateFormat.format(new Date(timeCreated));
    }

    public String getAlignmentString() {
        return WordUtils.capitalizeFully(alignment.replace("_"," "));
    }


}
