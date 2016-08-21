package net.dungeonrealms.common.game.database.concurrent.query;

import net.dungeonrealms.common.game.database.concurrent.Query;
import org.bson.conversions.Bson;

import java.util.function.Consumer;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/20/2016
 */

public class DocumentSearchQuery<Document> extends Query<Document> {

    /**
     * @param searchQuery Search query
     * @param doAfter     Consumer task to do after query is complete.
     */
    public DocumentSearchQuery(Bson searchQuery, Consumer<Document> doAfter) {
        super(searchQuery, doAfter);
    }

}
