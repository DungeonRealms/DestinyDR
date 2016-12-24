package net.dungeonrealms.common.game.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Nick on 9/17/2015.
 */
public class AsyncUtils {


    /*
    The amount of threads available on the machine
     */
    public static int threadCount = Runtime.getRuntime().availableProcessors();

    /*
    We'll use this instead of new Thread().start(); every time we want
    something to be on a different thread..
     */
    public static ExecutorService pool = Executors.newFixedThreadPool(threadCount);

}
