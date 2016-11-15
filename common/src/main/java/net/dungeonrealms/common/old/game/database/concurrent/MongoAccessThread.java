package net.dungeonrealms.common.old.game.database.concurrent;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.old.game.database.concurrent.query.BulkWriteQuery;
import net.dungeonrealms.common.old.game.database.concurrent.query.DocumentSearchQuery;
import net.dungeonrealms.common.old.game.database.concurrent.query.SingleUpdateQuery;
import org.bson.Document;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Alan on 7/19/2016.
 */

public class MongoAccessThread extends Thread {

    public static Queue<Query<?>> CONCURRENT_QUERIES = new ConcurrentLinkedQueue<>();


    public final static UpdateOptions uo = new UpdateOptions().upsert(true);

    private final ExecutorService CONSUMER_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();


    public static void submitQuery(Query<?> query) {
        CONCURRENT_QUERIES.add(query);
    }

    @Override
    public void run() {
        while (true) {
            try {
                // ALLOW THREAD TO SLEEP FOR 15ms BEFORE CONTINUING QUEUE //
                Thread.sleep(15L);
            } catch (InterruptedException ignored) {
            }

            while (!CONCURRENT_QUERIES.isEmpty()) {
                Query<?> query = CONCURRENT_QUERIES.poll();
                if (query == null) continue;

                if (query instanceof SingleUpdateQuery) {
                    SingleUpdateQuery<UpdateResult> updateQuery = (SingleUpdateQuery<UpdateResult>) query;
                    UpdateResult result = updateQuery.getCollection().updateOne(updateQuery.getSearchQuery(), updateQuery.getNewDocument(), uo);

                    if (result.wasAcknowledged()) {
                        if (Constants.debug)
                            Constants.log.info("[Database] ASYNC Executed update query: " + updateQuery.getSearchQuery().toString() + " " + updateQuery.getNewDocument().toString());
                        if (updateQuery.getDoAfter() != null)
                            CONSUMER_EXECUTOR_SERVICE.submit(() -> updateQuery.getDoAfter().accept(result));
                    } else
                        Constants.log.info("[Database] Update query failed: " + updateQuery.getSearchQuery().toString() + " " + updateQuery.getNewDocument().toString());
                } else if (query instanceof DocumentSearchQuery) {
                    DocumentSearchQuery documentSearchQuery = (DocumentSearchQuery) query;
                    Document doc = (Document) documentSearchQuery.getCollection().find(documentSearchQuery.getSearchQuery()).first();

                    if (Constants.debug)
                        Constants.log.info("[Database] ASYNC Executed search query: " + documentSearchQuery.getSearchQuery().toString() + " " + doc.toString());

                    CONSUMER_EXECUTOR_SERVICE.submit(() -> documentSearchQuery.getDoAfter().accept(doc));
                } else if (query instanceof BulkWriteQuery) {
                    BulkWriteQuery<BulkWriteResult> bulkWriteQuery = (BulkWriteQuery<BulkWriteResult>) query;
                    BulkWriteResult result = bulkWriteQuery.getCollection().bulkWrite(bulkWriteQuery.getModels());

                    if (Constants.debug)
                        Constants.log.info("[Database] ASYNC Executed bulk write operation. Modifications: " + result.getModifiedCount());

                    if (bulkWriteQuery.getDoAfter() != null)
                        CONSUMER_EXECUTOR_SERVICE.submit(() -> bulkWriteQuery.getDoAfter().accept(result));
                }
            }
        }

    }
}
