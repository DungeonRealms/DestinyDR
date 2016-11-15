package net.dungeonrealms.common.old.game.database.concurrent;

import com.mongodb.client.MongoCollection;
import lombok.Getter;
import org.bson.conversions.Bson;

import java.util.function.Consumer;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/20/2016
 */

public abstract class Query<T> {

    @Getter
    private final Bson searchQuery;

    @Getter
    private final MongoCollection collection;

    @Getter
    private final Consumer<T> doAfter;

    /**
     * @param collection  Database collection
     * @param searchQuery Search query
     * @param doAfter     Consumer task to do after query is complete.
     */
    public Query(MongoCollection collection, Bson searchQuery, Consumer<T> doAfter) {
        this.collection = collection;
        this.searchQuery = searchQuery;
        this.doAfter = doAfter;
    }
}
