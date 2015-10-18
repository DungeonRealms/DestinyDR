package net.dungeonrealms.core.reply;

import net.dungeonrealms.core.CoreAPI;

/**
 * Created by Nick on 10/17/2015.
 */
public class BanReply {

    private CoreAPI.BanResult result;
    private CoreAPI.BanReason reason;

    public BanReply(CoreAPI.BanResult result, CoreAPI.BanReason reason) {
        this.result = result;
        this.reason = reason;
    }

    public CoreAPI.BanResult getResult() {
        return result;
    }

    public CoreAPI.BanReason getReason() {
        return reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BanReply banReply = (BanReply) o;

        if (result != banReply.result) return false;
        return reason == banReply.reason;

    }

    @Override
    public int hashCode() {
        int result1 = result.hashCode();
        result1 = 31 * result1 + reason.hashCode();
        return result1;
    }
}
