package net.dungeonrealms.awt.database.direct;

import com.google.common.collect.Maps;
import lombok.Getter;
import net.dungeonrealms.awt.database.sql.MySQL;
import net.dungeonrealms.vgame.Game;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class SQLDatabase
{
    @Getter
    private MySQL database;

    public SQLDatabase(String ip, String port, String dbName, String pass, String user)
    {
        database = new MySQL(ip, port, dbName, pass, user);
        try
        {
            database.openConnection();
        } catch (ClassNotFoundException | SQLException e)
        {
            e.printStackTrace();
            // Shutdown the server, if there is no database it has no purpose.
            Game.getGame().getServer().shutdown();
        }
    }

    public void closeConnection()
    {
        if (database.connection == null)
            return;
        try
        {
            database.connection.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Getters in database
     */
    public int getIntByUUID(String table, String columnName, String uuid)
    {
        try
        {
            ResultSet set = database.query("SELECT * FROM " + table + " WHERE UUID = '" + uuid + "';");
            return set.getInt(columnName);
        } catch (Exception e)
        {
            Bukkit.getLogger().info(uuid + " data not found in Table: " + table + " Column: " + columnName);
            return 0;
        }
    }

    public String getStringByUUID(String table, String columnName, String uuid)
    {
        try
        {
            ResultSet set = database.query("SELECT * FROM " + table + " WHERE UUID = '" + uuid + "';");
            return set.getString(columnName);
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public Object getObjectByUUID(String table, String columnName, String uuid)
    {
        try
        {
            ResultSet set = database.query("SELECT * FROM " + table + " WHERE UUID = '" + uuid + "';");
            return set.getObject(columnName);
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public ResultSet getResultSetByUUID(String table, String uuid)
    {
        try
        {
            ResultSet set = database.query("SELECT * FROM " + table + " WHERE UUID = '" + uuid + "';");
            return set;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public ResultSet getSet(String query)
    {
        try
        {
            System.out.println(query);
            return database.query(query);
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public boolean exists(String table, String column, String value)
    {
        try
        {
            return database.query(
                    "SELECT * FROM " + table + " WHERE " + column + " = '" + StringEscapeUtils.escapeSql(value) + "';")
                    .next();
        } catch (Exception e)
        {
            return false;
        }
    }

    public void createTable(String table, List<String> keys)
    {
        String query = "";
        try
        {

            query = "CREATE TABLE IF NOT EXISTS " + table + "(`" + keys.get(0).split(Pattern.quote(";"))[0] + "` "
                    + keys.get(0).split(Pattern.quote(";"))[1];

            for (int i = 1; i < keys.size(); i++)
            {
                String[] s = keys.get(i).split(Pattern.quote(";"));
                query += ", `" + s[0] + "` " + s[1];
            }
            query += ", PRIMARY KEY(`" + keys.get(0).split(Pattern.quote(";"))[0] + "`));";
            database.update(query);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Setters in database
     */
    public void set(String table, HashMap<String, Object> map, String whereClause, String whereValue)
    {
        try
        {
            String[] keySet = map.keySet().toArray(new String[map.keySet().size()]);
            String query = "";
            if (exists(table, whereClause, whereValue))
            {
                query = "UPDATE " + table + " SET " + keySet[0] + " = '" + map.get(keySet[0]) + "'";
                for (int i = 1; i < map.keySet().size(); i++)
                {
                    String s = keySet[i];
                    query += ", " + s + " = '" + map.get(s) + "'";
                }

                query += " WHERE " + whereClause + " = '" + whereValue + "';";
            } else
            {
                query = "INSERT INTO " + table + " (`" + keySet[0] + "`";
                for (int i = 1; i < map.keySet().size(); i++)
                {
                    String s = keySet[i];
                    query += ", `" + s + "`";
                }
                query += ") VALUES ('" + map.get(keySet[0]) + "'";
                for (int i = 1; i < map.keySet().size(); i++)
                {
                    String s = keySet[i];
                    query += ", '" + map.get(s) + "'";
                }
                query += ");";
            }

            database.update(query);
        } catch (Exception e)
        {
            System.out.println("Vawke test 123");
            e.printStackTrace();
        }
    }

    public void setPlus(String table, String col, Integer val, String whereClause, String whereValue)
    {
        try
        {

            int current = val;
            ResultSet set = getSet("SELECT * FROM " + table + "WHERE " + whereClause + "='" + whereValue + "';");
            if (set.next())
            {
                current += set.getInt(col);
            }
            HashMap<String, Object> map = Maps.newHashMap();
            map.put(col, current);

            set(table, map, whereClause, whereValue);

        } catch (Exception e)
        {
            Bukkit.getLogger().info("Error setting+ in " + table);
        }
    }

    public void add(String table, HashMap<String, String> map)
    {
        String query;
        String[] keySet = map.keySet().toArray(new String[map.keySet().size()]);

        query = String.format("INSERT INTO %s (`%s`", table, keySet[0]);
        for (int i = 1; i < map.keySet().size(); i++)
        {
            String s = keySet[i];
            query += String.format(", `%s`", s);
        }
        query += String.format(") VALUES ('%s'", map.get(keySet[0]));
        for (int i = 1; i < map.keySet().size(); i++)
        {
            String s = keySet[i];
            query += String.format(", '%s'", map.get(s));
        }
        query += ");";
        try
        {
            database.update(query);
        } catch (ClassNotFoundException | SQLException e)
        {
            e.printStackTrace();
            Bukkit.getLogger().info("Error adding in " + table);
        }

    }

    public void update(String string)
    {
        try
        {
            database.update(string);
        } catch (ClassNotFoundException | SQLException e)
        {
            e.printStackTrace();
        }
    }
}
