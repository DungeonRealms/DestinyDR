package net.dungeonrealms.common.old.game.util;

import java.util.concurrent.ExecutorService;

/**
 * Created by Nick on 9/17/2015.
 */
public class AsyncUtils {


    /**
     * The person who made this class is officially autistic, runtime will always be null. Commons is not a fucking runnable jar file.
     * - Vawke
     * <p>
     * public static int threadCount = Runtime.getRuntime().availableProcessors();
     */
    public static int threadCount;

    /*
    We'll use this instead of new Thread().start(); every time we want
    something to be on a different thread..
     */
    public static ExecutorService pool;
}
