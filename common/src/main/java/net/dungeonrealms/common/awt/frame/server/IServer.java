package net.dungeonrealms.common.awt.frame.server;

import com.google.common.collect.Lists;
import net.dungeonrealms.common.awt.frame.server.shard.ServerCore;
import net.dungeonrealms.common.awt.frame.server.shard.task.ServerTask;
import net.dungeonrealms.common.awt.frame.server.shard.task.data.EnumTaskPurpose;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Giovanni on 2-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface IServer extends Core {

    /**
     * Schedule a new server task
     *
     * @param serverTask The task to run
     */
    default void runTask(ServerTask serverTask) {
        switch (serverTask.getTaskSetting()) {
            case ASYNC:
                switch (serverTask.getTaskType()) {
                    case REPEATING:
                        this.getAccess().getServer().getScheduler().scheduleAsyncRepeatingTask(this.getAccess(), serverTask::onRun, 0, 20 * serverTask.getTimeX());
                        break;
                    case DELAYED:
                        this.getAccess().getServer().getScheduler().scheduleAsyncDelayedTask(this.getAccess(), serverTask::onRun, 20 * serverTask.getTimeX());
                        break;
                }
                break;
            case SYNC:
                switch (serverTask.getTaskType()) {
                    case REPEATING:
                        if (serverTask.getTaskPurpose() == EnumTaskPurpose.WORLD_MANIPULATION)
                            this.getAccess().getServer().getScheduler().scheduleSyncRepeatingTask(this.getAccess(), serverTask::onRun, 0, 20 * serverTask.getTimeX());
                        break;
                    case DELAYED:
                        this.getAccess().getServer().getScheduler().scheduleSyncDelayedTask(this.getAccess(), serverTask::onRun, 20 * serverTask.getTimeX());
                        break;
                }
                break;
            default:
                break;
        }
    }

    /**
     * Run a task after another task has finished
     *
     * @param serverTask1 The running task
     * @param serverTask2 The task to run after the other has finished
     */
    default void runTaskAfter(ServerTask serverTask1, ServerTask serverTask2) {
        if (serverTask1.isFinished()) {
            this.runTask(serverTask2);
        }
    }

    /**
     * Run a task after a group of other tasks has been finished
     *
     * @param taskList   The group of tasks to be finished
     * @param serverTask The new task to start
     */
    default void runTaskAfter(List<ServerTask> taskList, ServerTask serverTask) {
        List<ServerTask> finishedTasks = Lists.newArrayList();
        finishedTasks.addAll(taskList.stream().filter(ServerTask::isFinished).collect(Collectors.toList()));
        if (finishedTasks.size() == taskList.size()) {
            this.runTask(serverTask);
        }
    }
}
