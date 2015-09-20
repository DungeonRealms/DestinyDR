package net.dungeonrealms.items;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 9/19/2015.
 */
public class NameGenerator {

    public List<String> next() {
        return getRandomItemName();
    }

    List<String> getRandomItemName() {
        List<String> name = new ArrayList<>();
        name.add("Weapon");
        name.add("Of");
        name.add("Trayvon Martin");
        return name;
    }
}
