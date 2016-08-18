package net.dungeonrealms.common.network.ping;


import lombok.Data;

import java.util.List;

@Data
public class PingResponse {

    private String description;
    private Players players;
    private Version version;
    private String favicon;
    private int time;

    @Data
    public class Players {
        private int max;
        private int online;
        private List<PlayerInfo> sample;
    }

    @Data
    public class PlayerInfo {
        private String name;
        private String id;
    }

    @Data
    public class Version {
        private String name;
        private String protocol;
    }

}
