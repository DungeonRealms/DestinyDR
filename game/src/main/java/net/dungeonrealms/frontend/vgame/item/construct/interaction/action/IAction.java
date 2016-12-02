package net.dungeonrealms.frontend.vgame.item.construct.interaction.action;

/**
 * Created by Giovanni on 30-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface IAction {

    EnumInteractionAction getAction();

    void start();
}
