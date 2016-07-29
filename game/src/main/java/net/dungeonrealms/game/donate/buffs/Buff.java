package net.dungeonrealms.game.donate.buffs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.player.json.JsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

import java.io.Serializable;

/**
 * Created by Alan on 7/28/2016.
 */
@Getter
@Setter
public abstract class Buff implements Serializable {

    protected long timeUntilExpiry;
    protected float bonusAmount;
    protected int duration;
    protected String activatingPlayer;
    protected String fromServer;

    public void activateBuff() {
        this.timeUntilExpiry = System.currentTimeMillis() + duration * 1000;
        onActivateBuff();
        Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 10f, 1f));
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> deactivateBuff(), duration * 20L);
    }

    public abstract void onActivateBuff();

    public abstract void deactivateBuff();

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        JsonBuilder jb = new JsonBuilder();
        jb.setData("timeUntilExpiry", timeUntilExpiry).setData("bonusAmount", bonusAmount).setData("duration",
                duration).setData("activatingPlayer", activatingPlayer).setData("fromServer", fromServer);
        return jb.getJson().toString();
    }

    public static Buff deserialize(String serializedBuff, Class<? extends Buff> clazz) {
        if (serializedBuff == null || serializedBuff.equals("")) return null;

        JsonParser jsParser = new JsonParser();
        JsonObject jo = (JsonObject) jsParser.parse(serializedBuff);
        Buff instance = null;

        try {
            instance = clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (instance == null) return null;

        instance.setTimeUntilExpiry(jo.get("timeUntilExpiry").getAsLong());
        instance.setDuration(jo.get("duration").getAsInt());
        instance.setBonusAmount(jo.get("bonusAmount").getAsFloat());
        instance.setActivatingPlayer(jo.get("activatingPlayer").getAsString());
        instance.setFromServer(jo.get("fromServer").getAsString());

        return instance;
    }

}