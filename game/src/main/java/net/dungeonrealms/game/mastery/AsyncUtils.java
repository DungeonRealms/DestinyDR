package net.dungeonrealms.game.mastery;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Nick on 9/17/2015.
 */
public class AsyncUtils {

    /*
    We'll use this instead of new Thread().start(); everytime we want
    something to be on a different thread..
     */
    public static ExecutorService pool = Executors.newCachedThreadPool();

}
