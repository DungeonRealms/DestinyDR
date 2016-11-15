package net.dungeonrealms.common.old.game.database.concurrent.query;

import com.mongodb.client.MongoCollection;
import net.dungeonrealms.common.old.game.database.concurrent.Query;
import org.bson.conversions.Bson;

import java.util.function.Consumer;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/20/2016
 */

public class DocumentSearchQuery<Document> extends Query<Document> {

    /**
     * @param collection  Database collection
     * @param searchQuery Search query
     * @param doAfter     Consumer task to do after query is complete.
     */
    public DocumentSearchQuery(MongoCollection collection, Bson searchQuery, Consumer<Document> doAfter) {
        super(collection, searchQuery, doAfter);
    }

}
