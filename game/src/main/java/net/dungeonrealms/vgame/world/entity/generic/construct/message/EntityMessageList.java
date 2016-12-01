package net.dungeonrealms.vgame.world.entity.generic.construct.message;

import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Created by Giovanni on 26-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class EntityMessageList {

    @Getter
    private List<String> messageList;

    public EntityMessageList(String... messages) {
        this.messageList = Lists.newArrayList();
        Collections.addAll(this.messageList, messages);
    }
}
