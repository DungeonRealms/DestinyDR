package net.dungeonrealms.bounty;

import net.dungeonrealms.core.Core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Created by Nick on 12/14/2015.
 */
public class Bounty {

    static Bounty instance = null;

    public static Bounty getInstance() {
        if (instance == null) {
            instance = new Bounty();
        }
        return instance;
    }

    /**
     * Returns the bounties placed on a person.
     *
     * @param uuid
     * @param action
     */
    public void getBountiesOn(UUID uuid, Consumer<HashMap<String, Integer>> action) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try (
                    PreparedStatement statement = Core.getInstance().connection.prepareStatement("SELECT placer, reward FROM `bounties` WHERE victim='" + uuid.toString() + "';");
                    ResultSet resultSet = statement.executeQuery()
            ) {
                HashMap<String, Integer> temp = new HashMap<>();
                while (resultSet.next()) {
                    temp.put(Core.getInstance().getNameFromUUID(UUID.fromString(resultSet.getString("placer"))), resultSet.getInt("reward"));
                }
                action.accept(temp);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Checks to see if a player has a bounty placed on them.
     *
     * @param uuid
     */
    public boolean hasBountyOn(UUID uuid) {
        Future<?> doesContain = Executors.newSingleThreadExecutor().submit(() -> {
            boolean contains = true;
            try (
                    PreparedStatement statement = Core.getInstance().connection.prepareStatement("SELECT placer FROM `bounties` WHERE victim='" + uuid.toString() + "';");
                    ResultSet resultSet = statement.executeQuery()
            ) {
                contains = resultSet.next();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return contains;
        });
        try {
            return (Boolean) doesContain.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get all bounties placed by a specified uuid
     *
     * @param uuid
     * @param action
     */
    public void getPlacedBounties(UUID uuid, Consumer<HashMap<String, Integer>> action) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try (
                    PreparedStatement statement = Core.getInstance().connection.prepareStatement("SELECT * FROM `bounties` WHERE placer='" + uuid.toString() + "';");
                    ResultSet resultSet = statement.executeQuery()
            ) {
                HashMap<String, Integer> temp = new HashMap<>();
                while (resultSet.next()) {
                    temp.put(Core.getInstance().getNameFromUUID(UUID.fromString(resultSet.getString("victim"))), resultSet.getInt("reward"));
                }

                action.accept(temp);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

}
