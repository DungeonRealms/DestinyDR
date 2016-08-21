package net.dungeonrealms.common.game.database.concurrent.query;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dungeonrealms.common.game.database.concurrent.Query;
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
     * @param searchQuery     Search query
     * @param newDocument     New Document to replace
     * @param doAfterOptional Consumer task to do after query is complete.
     */
    public SingleUpdateQuery(Bson searchQuery, Bson newDocument, Consumer<UpdateResult> doAfterOptional) {
        super(searchQuery, doAfterOptional);
        this.newDocument = newDocument;
    }
}
