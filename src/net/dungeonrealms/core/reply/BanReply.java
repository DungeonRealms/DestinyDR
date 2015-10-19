package net.dungeonrealms.core.reply;

/**
 * Created by Nick on 10/17/2015.
 */
public class BanReply {

    private BanResult result;
    private BanReason reason;

    public BanReply(BanResult result, BanReason reason) {
        this.result = result;
        this.reason = reason;
    }

    public BanResult getResult() {
        return result;
    }

    public BanReason getReason() {
        return reason;
    }


    /**
     * BanReason
     *
     * @since 1.0
     */
    public enum BanReason {
        OTHER(-1, "Other"),
        DUPLICATIONS(1, "Duplications"),
        HACKING(0, "Hacking"),
        MIS_CONDUCT(3, "Misconduct");

        private int id;
        private String reason;

        BanReason(int id, String reason) {
            this.id = id;
            this.reason = reason;
        }

        public static BanReason getByInt(int id) {
            for (BanReason br : values()) {
                if (br.getId() == id) {
                    return br;
                }
            }
            return BanReason.OTHER;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return reason;
        }
    }

    /**
     * ARE DEM BOI BANNEDED?
     *
     * @since 1.0
     */
    public enum BanResult {
        YES(1),
        NO(0),
        TEMP_BANNED(3);

        private int id;

        BanResult(int id) {
            this.id = id;
        }

        public static BanResult getByInt(int id) {
            for (BanResult br : values()) {
                if (br.getId() == id) {
                    return br;
                }
            }
            return BanResult.NO;
        }

        public int getId() {
            return id;
        }
    }
}
