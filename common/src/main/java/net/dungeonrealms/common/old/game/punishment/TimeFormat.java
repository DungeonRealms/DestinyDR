package net.dungeonrealms.common.old.game.punishment;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/1/2016
 */

public enum TimeFormat {

    MIN("m", 60), HOUR("h", 3600), DAY("d", 86400), WEEK("w", 604800), YEAR("y", 31536000);

    private String key;
    private long convert;

    TimeFormat(String key, long convert) {
        this.key = key;
        this.convert = convert;
    }

    public String getKey() {
        return key;
    }

    public long convert() {
        return convert;
    }

}
