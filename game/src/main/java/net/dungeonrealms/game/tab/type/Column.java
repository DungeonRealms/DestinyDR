package net.dungeonrealms.game.tab.type;

import codecrafter47.bungeetablistplus.api.bukkit.Variable;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/4/2016
 */

public abstract class Column {

    @Getter
    protected List<Variable> variablesToRegister = new ArrayList<>();


    /**
     * Create all variables associated with this colman
     * @return Column instance
     */
    public abstract Column register();

}
