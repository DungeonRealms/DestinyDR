package net.dungeonrealms.common.old.network;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.dungeonrealms.common.old.network.enumeration.EnumShardType;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/12/2016
 */

@NoArgsConstructor
public enum ShardInfo implements Serializable
{
    // DEVELOPMENT SHARD //
    US0("US-0", "us0", EnumShardType.MASTER, new ServerAddress("158.69.122.139", 40007)),

    // US1 SHARD //
    US1("US-1", "us1", EnumShardType.BETA, new ServerAddress("64.38.250.64", 40007)),

    // US2 SHARD //
    US2("US-2", "us2", EnumShardType.BETA, new ServerAddress("64.38.250.64", 40008)),

    // US3 SHARD //
    US3("US-3", "us3", EnumShardType.BETA, new ServerAddress("131.153.27.8", 40001)),

    // US4 SHARD //
    US4("US-4", "us4", EnumShardType.BETA, new ServerAddress("131.153.27.8", 40002)),

    // US5 SHARD //
    US5("US-5", "us5", EnumShardType.BETA, new ServerAddress("64.38.249.176", 40007)),

    // BR 1 SHARD //
    BR1("BR-1", "br1", EnumShardType.BRAZILLIAN, new ServerAddress("64.38.249.176", 40008)),

    // SUB 1 SHARD //
    SUB1("SUB-1", "sub1", EnumShardType.SUBSCRIBER, new ServerAddress("131.153.27.42", 40008)),

    // CS 1 SHARD //
    CS1("CS-1", "cs1", EnumShardType.SUPPORT, new ServerAddress("192.99.200.110", 11250));


    @Getter
    private String shardID;

    @Getter
    private String pseudoName;

    @Getter
    private ServerAddress address;

    @Getter
    private EnumShardType shardType;

    ShardInfo(String shardID, String pseudoName, EnumShardType enumShardType, ServerAddress address)
    {
        this.shardID = shardID;
        this.pseudoName = pseudoName;
        this.shardType = enumShardType;
        this.address = address;
    }


    public static ShardInfo getByPseudoName(String pseudoName)
    {
        Optional<ShardInfo> query = Arrays.asList(ShardInfo.values()).stream().
                filter(info -> info.getPseudoName().equals(pseudoName)).findFirst();

        return query.isPresent() ? query.get() : null;
    }

    public static ShardInfo getByShardID(String shardID)
    {
        Optional<ShardInfo> query = Arrays.asList(ShardInfo.values()).stream().
                filter(info -> info.getShardID().equals(shardID)).findFirst();

        return query.isPresent() ? query.get() : null;
    }

    public static ShardInfo getByAddress(ServerAddress address)
    {
        Optional<ShardInfo> query = Arrays.asList(ShardInfo.values()).stream().
                filter(info -> info.getAddress().toString().equals(address.toString())).findFirst();

        return query.isPresent() ? query.get() : null;
    }
}
