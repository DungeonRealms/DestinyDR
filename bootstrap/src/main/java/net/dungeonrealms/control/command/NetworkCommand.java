package net.dungeonrealms.control.command;

import net.dungeonrealms.control.DRControl;

/**
 * Created by Evoltr on 11/20/2016.
 */
public abstract class NetworkCommand {

    private String name;
    private String desc;

    public NetworkCommand(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    protected DRControl getDRControl() {
        return DRControl.getInstance();
    }

    public abstract void onCommand(String[] args);
}
