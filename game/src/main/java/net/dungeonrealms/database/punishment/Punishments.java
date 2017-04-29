package net.dungeonrealms.database.punishment;

import com.google.common.collect.Lists;
import net.dungeonrealms.database.LoadableData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class Punishments extends HashMap<PunishType, List<Punishment>> implements LoadableData {


    public int getPunishCount(PunishType type) {
        List<Punishment> punishments = get(type);
        if (punishments == null) return 0;
        return punishments.size();
    }

    @Override
    public void extractData(ResultSet resultSet) {
        try {
            List<Punishment> mutes = Lists.newArrayList();
            List<Punishment> bans = Lists.newArrayList();
            while (resultSet.next()) {
                Punishment punishment = new Punishment(resultSet.getInt("punisher_id"), resultSet.getLong("issued"),
                        resultSet.getLong("expiration"), resultSet.getString("reason"), resultSet.getBoolean("quashed"));
                if (resultSet.getString("type").equals("ban")) {
                    bans.add(punishment);
                } else {
                    mutes.add(punishment);
                }
            }

            put(PunishType.BAN, bans);
            put(PunishType.MUTE, mutes);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
