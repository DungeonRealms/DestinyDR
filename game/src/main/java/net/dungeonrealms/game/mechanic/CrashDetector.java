package net.dungeonrealms.game.mechanic;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import org.bukkit.Bukkit;

/**
 * Created by Alan on 8/1/2016.
 */
public class CrashDetector implements GenericMechanic {

    public static volatile long antiCrashTime = 0;
    public static volatile boolean crashDetected = false;
    public static Thread crashChecker;
    static CrashDetector instance = null;

    public static CrashDetector getInstance() {
        if (instance == null) instance = new CrashDetector();
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
    public void startInitialization() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> antiCrashTime = System
                .currentTimeMillis(), 5 * 20L, 20L);

        crashChecker = new Thread(() -> {
            long multithreadAntiCrash = 0;
            multithreadAntiCrash = antiCrashTime;
            boolean crashed = false; // Stop, stop, close your IDE, and never return

            while (true) { // You fucking idiot, crashed = always false
                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException e) {
                    continue;
                }
                if (multithreadAntiCrash == antiCrashTime) {
                    if (crashDetected) continue;

                    // No tick in last 30 seconds, upload local data and reboot.
                    Constants.log.warning("Detected no heartbeat in main thread for 30 seconds, uploading local data and locking server to prevent database issues.");

                    crashDetected = true;
                    GameAPI.handleCrash();

                    crashed = true;
                    break;
                } else if (multithreadAntiCrash != antiCrashTime) {
                    multithreadAntiCrash = antiCrashTime;
                    // Update time.
                }
            }
        }, "Crash Detection Thread");

        crashChecker.start();
    }

    @Override
    public void stopInvocation() {
    }
}
