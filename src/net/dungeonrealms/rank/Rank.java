package net.dungeonrealms.rank;

import com.mongodb.client.model.Filters;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mongo.Database;
import net.dungeonrealms.mongo.EnumOperators;
import org.bson.Document;

import java.util.ArrayList;

/**
 * Created by Nick on 9/27/2015.
 */
public class Rank {

    static Rank instance = null;

    public static Rank getInstance() {
        if (instance == null) {
            instance = new Rank();
        }
        return instance;
    }

    public void startInitialization() {
        //TODO: Read all documents in `ranks` collection. Store inside Hash or some shit.
    }

    /**
     * Creates a new rank on Mongo Collection ("rank")
     *
     * @param rankName
     * @since 1.0
     */
    public void createNewRank(String rankName, String prefix, String suffix) {
        Document blankRankDocument =
                new Document("rank",
                        new Document("name", rankName)
                                .append("created", System.currentTimeMillis() / 1000L)
                                .append("prefix", prefix)
                                .append("suffix", suffix)
                                .append("permissions", new ArrayList<String>())
                );
        Database.ranks.insertOne(blankRankDocument, (aVoid, throwable) -> Utils.log.warning("Created a new Rank " + rankName));
    }

    /**
     * Adds a <String>Permission</String> to a Rank's ArrayList</>
     *
     * @param rank
     * @param permission
     * @since 1.0
     */
    public void addPermission(String rank, String permission) {
        Database.ranks.updateOne(Filters.eq("rank.name", rank.toUpperCase()), new Document(EnumOperators.$PUSH.getUO(), new Document("rank.permissions", permission)),
                (result, t) -> {
                    Utils.log.info("DatabaseAPI update() called .. Updated ranks permissions(s)..");
                });
    }

}
