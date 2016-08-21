package net.dungeonrealms.common.game.database.concurrent;

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
    private final Consumer<T> doAfter;

    /**
     * @param searchQuery     Search query
     * @param doAfter Consumer task to do after query is complete.
     */
    public Query(Bson searchQuery, Consumer<T> doAfter) {
        this.searchQuery = searchQuery;
        this.doAfter = doAfter;
    }
}
