package net.dungeonrealms.common.awt.frame.server.shard.task;

import lombok.Getter;
import net.dungeonrealms.common.awt.frame.server.shard.task.data.EnumTaskPurpose;
import net.dungeonrealms.common.awt.frame.server.shard.task.data.EnumTaskSetting;
import net.dungeonrealms.common.awt.frame.server.shard.task.data.EnumTaskType;

/**
 * Created by Giovanni on 2-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public abstract class ServerTask {

    @Getter
    private EnumTaskSetting taskSetting;

    @Getter
    private EnumTaskType taskType;

    @Getter
    private EnumTaskPurpose taskPurpose;

    @Getter
    private int timeX;

    @Getter
    private boolean finished;

    public ServerTask(EnumTaskType taskType, EnumTaskSetting taskSetting, EnumTaskPurpose taskPurpose, int timeX) {
        this.taskType = taskType;
        this.taskSetting = taskSetting;
        this.taskPurpose = taskPurpose;
        this.timeX = timeX;
        this.finished = false;
    }

    /**
     * A task must always be set to FINISHED after it's done
     */
    public abstract void onRun();
}
