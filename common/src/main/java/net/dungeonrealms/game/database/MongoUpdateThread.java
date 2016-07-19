package net.dungeonrealms.game.database;

import com.mongodb.client.result.UpdateResult;
import net.dungeonrealms.Constants;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Alan on 7/19/2016.
 */
public class MongoUpdateThread extends Thread {
    public static Queue<List<Bson>> queries = new ConcurrentLinkedQueue<>();
    public static boolean debug = false;

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(250);
            } catch(InterruptedException e) {}

            while (!queries.isEmpty()) {
                List<Bson> query = queries.poll();
                if (query == null) continue;

                UpdateResult result = DatabaseDriver.collection.updateOne(query.get(0), query.get(1));
                if (debug) {
                    if (result.wasAcknowledged()) {
                        Constants.log.warning("[Mongo] ASYNC Executed query: " + query.get(0).toString() + " " + query.get(1).toString());


                    } else {
                        Constants.log.warning("[Mongo] Update query failed: " + query.get(0).toString() + " " + query
                                .get
                                (1).toString());
                    }
                }
            }
        }
    }
}
