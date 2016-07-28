package net.dungeonrealms.common.game.database.concurrent;

import com.mongodb.client.result.UpdateResult;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.DatabaseDriver;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Alan on 7/19/2016.
 */

public class UpdateThread extends Thread {

    public static Queue<SingleUpdateQuery<UpdateResult>> CONCURRENT_QUERIES = new ConcurrentLinkedQueue<>();

    @Override
    public void run() {
        while (true) {
            try {
                // ALLOW THREAD TO SLEEP FOR 250ms BEFORE CONTINUING QUEUE //
                Thread.sleep(250L);
            } catch (InterruptedException ignored) {
            }

            while (!CONCURRENT_QUERIES.isEmpty()) {
                SingleUpdateQuery<UpdateResult> query = CONCURRENT_QUERIES.poll();
                if (query == null) continue;

                UpdateResult result = DatabaseDriver.playerData.updateOne(query.getSearchQuery(), query.getNewDocument());

                if (result.wasAcknowledged()) {
                    if (Constants.debug)
                        Constants.log.info("[Database] ASYNC Executed query: " + query.getSearchQuery().toString() + " " + query.getNewDocument().toString());
                    if (query.getDoAfterOptional() != null)
                        query.getDoAfterOptional().accept(result);
                } else
                    Constants.log.info("[Database] Update query failed: " + query.getSearchQuery().toString() + " " + query.getNewDocument().toString());
            }
        }

    }
}
