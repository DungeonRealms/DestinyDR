package net.dungeonrealms.vgame.item.construct.interaction.action.type;

import lombok.Getter;
import net.dungeonrealms.vgame.item.construct.Item;
import net.dungeonrealms.vgame.item.construct.interaction.InteractionItem;
import net.dungeonrealms.vgame.item.construct.interaction.action.EnumInteractionAction;
import net.dungeonrealms.vgame.item.construct.interaction.action.IAction;
import org.bukkit.entity.Player;

/**
 * Created by Giovanni on 30-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class AlterationAction implements IAction {


    @Getter
    private EnumInteractionAction action = EnumInteractionAction.ALTERATION;

    @Getter
    private InteractionItem interactionItem;

    @Getter
    private Player activityPlayer;

    @Getter
    private Item targetItem;

    public AlterationAction(InteractionItem interactionItem, Player player, Item targetItem) {
        this.interactionItem = interactionItem;
        this.activityPlayer = player;
        this.targetItem = targetItem;
    }

    @Override
    public void start() {

    }
}
