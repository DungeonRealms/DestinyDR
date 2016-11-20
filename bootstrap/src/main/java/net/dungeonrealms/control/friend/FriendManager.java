package net.dungeonrealms.control.friend;

import net.dungeonrealms.control.DRControl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Evoltr on 11/20/2016.
 */
public class FriendManager {

    private DRControl control;

    private Map<String, List<String>> friendLists = new HashMap<>();
    private Map<String, List<String>> friendRequests = new HashMap<>();

    public FriendManager(DRControl control) {
        this.control = control;
    }

    // TODO: Make this.
}
