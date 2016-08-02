package net.dungeonrealms.common.game.database.concurrent;

import lombok.Data;
import org.bson.conversions.Bson;

import java.util.function.Consumer;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/27/2016
 */

@Data
public class SingleUpdateQuery<T> {

    private final Bson searchQuery, newDocument;

    private final Consumer<T> doAfterOptional;

    /**
     * @param searchQuery     Search query
     * @param newDocument     New Document to replace
     * @param doAfterOptional Consumer task to do after query is complete.
     */
    public SingleUpdateQuery(Bson searchQuery, Bson newDocument, Consumer<T> doAfterOptional) {
        this.searchQuery = searchQuery;
        this.newDocument = newDocument;

        this.doAfterOptional = doAfterOptional;
    }
}
