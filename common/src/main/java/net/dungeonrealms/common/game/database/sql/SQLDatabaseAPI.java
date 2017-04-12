package net.dungeonrealms.common.game.database.sql;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Cleanup;
import lombok.Getter;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SQLDatabaseAPI {

    private static SQLDatabaseAPI instance;

    @Getter
    private SQLDatabase database;

    private Map<UUID, Map<DataType, Object>> storedData = new HashMap<>();

    private final ExecutorService SERVER_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("SQL Thread").build());

    public static SQLDatabaseAPI getInstance() {
        if (instance == null) {
            instance = new SQLDatabaseAPI();
        }
        return instance;
    }


    public void init() {
        this.database = new SQLDatabase("158.69.121.40", "dev", "3HCKkPc6mWr63E924C", "dungeonrealms");
    }



    public void loadData(UUID uuid){
        CompletableFuture.runAsync(() -> {
            try{
                //Essentially make it pull
                @Cleanup PreparedStatement statement = this.getDatabase().getConnection().prepareStatement("SELECT * FROM characters LEFT JOIN attributes");
            }catch(Exception e){
                e.printStackTrace();
            }
        }, SERVER_EXECUTOR_SERVICE);
    }
}
