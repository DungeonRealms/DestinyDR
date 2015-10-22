package net.dungeonrealms.core.reply;

/**
 * Created by Nick on 10/22/2015.
 */
public class ProxyReply {

    private Result result;

    public ProxyReply(Result result) {
        this.result = result;
    }

    public Result getResult() {
        return result;
    }

    public enum Result {
        YES(1),
        NO(0),;

        private int id;

        Result(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static Result getByBoolean(boolean b) {
            if (b) {
                return Result.YES;
            } else {
                return Result.NO;
            }
        }
    }

}
