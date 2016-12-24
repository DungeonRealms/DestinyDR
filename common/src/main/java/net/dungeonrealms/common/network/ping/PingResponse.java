package net.dungeonrealms.common.network.ping;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PingResponse {

    private Description description;

    private Players players;

    private Version version;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Players {

        private int max;

        private int online;

        private List<PlayerInfo> sample;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class PlayerInfo {

        private String name;

        private String id;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Version {

        private String name;
        private String protocol;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Description {

        private String text;
    }

}
