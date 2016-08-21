package net.dungeonrealms.common.game.database.concurrent.query;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOneModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dungeonrealms.common.game.database.concurrent.Query;
import org.bson.Document;

import java.util.List;
import java.util.function.Consumer;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/27/2016
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class BulkWriteQuery<BulkWriteResult> extends Query<BulkWriteResult> {

    private final List<UpdateOneModel<Document>> models;

    /**
     * @param collection  Database collection
     * @param doAfterOptional Consumer task to do after query is complete.
     * @param models          Write models
     */
    public BulkWriteQuery(MongoCollection collection, List<UpdateOneModel<Document>> models, Consumer<BulkWriteResult> doAfterOptional) {
        super(collection, null, doAfterOptional);
        this.models = models;
    }
}
