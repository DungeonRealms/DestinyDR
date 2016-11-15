package net.dungeonrealms.common.old.game.database.concurrent.query;

import com.mongodb.client.MongoCollection;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dungeonrealms.common.old.game.database.concurrent.Query;
import org.bson.conversions.Bson;

import java.util.function.Consumer;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/27/2016
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class SingleUpdateQuery<UpdateResult> extends Query<UpdateResult> {

    private final Bson newDocument;

    /**
     * @param collection  Database collection
     * @param searchQuery     Search query
     * @param newDocument     New Document to replace
     * @param doAfterOptional Consumer task to do after query is complete.
     */
    public SingleUpdateQuery(MongoCollection collection, Bson searchQuery, Bson newDocument, Consumer<UpdateResult> doAfterOptional) {
        super(collection, searchQuery, doAfterOptional);
        this.newDocument = newDocument;
    }
}
