package net.dungeonrealms.common.network;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/12/2016
 */

public enum ShardInfo {

    // DEVELOPMENT SHARD //
    US0("US-0", "us0", new ServerAddress("158.69.122.139", 40007)),

    // US1 SHARD //
    US1("US-1", "us1", new ServerAddress("64.38.250.64", 40007)),

    // US2 SHARD //
    US2("US-2", "us2", new ServerAddress("64.38.250.64", 40008)),

    // US3 SHARD //
    US3("US-3", "us3", new ServerAddress("131.153.27.8", 40001)),

    // US4 SHARD //
    US4("US-4", "us4", new ServerAddress("131.153.27.8", 40002)),

    // US5 SHARD //
    US5("US-5", "us5", new ServerAddress("64.38.249.176", 40007)),

    // BR 1 SHARD //
    BR1("BR-1", "br1", new ServerAddress("64.38.249.176", 40008)),

    // SUB 1 SHARD //
    SUB1("SUB-1", "sub1", new ServerAddress("158.69.122.139", 40008)),

    // CS 1 SHARD //
    CS1("CS-1", "cs1", new ServerAddress("192.99.200.110", 11250));


    @Getter
    private final String shardID;

    @Getter
    private final String pseudoName;

    @Getter
    private final ServerAddress address;

    ShardInfo(String shardID, String pseudoName, ServerAddress address) {
        this.shardID = shardID;
        this.pseudoName = pseudoName;
        this.address = address;
    }


    public static ShardInfo getByPseudoName(String pseudoName) {
        Optional<ShardInfo> query = Arrays.asList(ShardInfo.values()).stream().
                filter(info -> info.getPseudoName().equals(pseudoName)).findFirst();

        return query.isPresent() ? query.get() : null;
    }

    public static ShardInfo getByShardID(String shardID) {
        Optional<ShardInfo> query = Arrays.asList(ShardInfo.values()).stream().
                filter(info -> info.getShardID().equals(shardID)).findFirst();

        return query.isPresent() ? query.get() : null;
    }

    public static ShardInfo getByAddress(ServerAddress address) {
        Optional<ShardInfo> query = Arrays.asList(ShardInfo.values()).stream().
                filter(info -> info.getAddress().toString().equals(address.toString())).findFirst();

        return query.isPresent() ? query.get() : null;
    }
}
