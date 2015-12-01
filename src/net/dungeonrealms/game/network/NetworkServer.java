package net.dungeonrealms.game.network;

import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.ArrayList;

/**
 * Created by Nick on 10/16/2015.
 */
public class NetworkServer implements GenericMechanic{

    static NetworkServer instance = null;

    public static NetworkServer getInstance() {
        if (instance == null) {
            instance = new NetworkServer();
        }
        return instance;
    }

    private static Document SERVER_DOCUMENT = null;

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }

    @Override
	public void startInitialization() {
        Utils.log.info("[NetworkServer] Starting up... STARTING");
        //Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), this::refreshDocument, 0, 20 * 8);
        Utils.log.info("[NetworkServer] Finished starting up ... OKAY");
    }

    @Override
    public void stopInvocation() {

    }

    /**
     * Grabs the new document from mongo!
     *
     * @since 1.0
     */
    private void refreshDocument() {
        /*
        Database.servers.find(Filters.eq("info.server", Bukkit.getMotd())).limit(1).first((document, throwable) -> {
            if (document != null) {
                SERVER_DOCUMENT = document;
            } else {
                Utils.log.warning("[NetworkServer] [ASYNC] Unable to retrieve valid [SERVER DOCUMENT] ... Attempting to create!");
                createNewNetworkDocument();
            }
        });
         */
    }

    /**
     * Creates a new document for the server. (looks at motd)
     *
     * @since 1.0
     */
    private void createNewNetworkDocument() {
        Document newPlayerDocument =
                new Document("info",
                        new Document("server", Bukkit.getMotd())
                                .append("created", System.currentTimeMillis() / 1000L)
                                .append("stimulationPacks", new ArrayList<>())

                );
        /*
        Database.servers.insertOne(newPlayerDocument, (aVoid, throwable) -> {
            Utils.log.info("[NetworkServer] [ASYNC] Created a SERVER Document to fill VOID!");
            refreshDocument();
        });
         */
    }

}
