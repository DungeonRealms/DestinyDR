package net.dungeonrealms.common.game.database;

import com.mongodb.client.result.UpdateResult;
import net.dungeonrealms.common.Constants;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Alan on 7/19/2016.
 */
public class MongoUpdateThread extends Thread {

    public static Queue<UpdateQuery<UpdateResult>> queries = new ConcurrentLinkedQueue<>();

    @Override
    public void run() {
        while (true) {

            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {
            }

            while (!queries.isEmpty()) {
                UpdateQuery<UpdateResult> query = queries.poll();
                if (query == null) continue;

                UpdateResult result = DatabaseDriver.collection.updateOne(query.getBson(), query.getBson1());
                if (Constants.debug) if (result.wasAcknowledged()) {
                    Constants.log.warning("[Mongo] ASYNC Executed query: " + query.getBson().toString() + " " + query.getBson1().toString());

                    if (query.getConsumer() != null)
                        query.getConsumer().accept(result);
                } else
                    Constants.log.warning("[Mongo] Update query failed: " + query.getBson().toString() + " " + query.getBson1().toString());

            }
        }
    }
}
