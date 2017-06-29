package net.dungeonrealms.game.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ReflectionAPI;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CommandCheckDupe extends BaseCommand {
    public CommandCheckDupe() {
        super("checkdupe", "/<command>", "Check dupe command");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!Rank.isDev(sender)) return false;

//        SQLDatabaseAPI.getInstance().executeQuery("SELECT * FROM characters LEFT JOIN users ON characters.account_id = users.account_id WHERE characters.character_id = users.selected_character_id;", rs -> {
        CompletableFuture.runAsync(() -> {
            try {
                String query = "SELECT * FROM characters LEFT JOIN users ON characters.account_id = users.account_id;";
//                String query = "SELECT * FROM characters LEFT JOIN users ON characters.account_id = users.account_id WHERE characters.character_id = users.selected_character_id;";

                PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement(query);
                ResultSet rs = statement.executeQuery();
                if (rs == null) {
                    sender.sendMessage(ChatColor.RED + "Null result set!");
                    return;
                }

                long start = System.currentTimeMillis();
                //ItemStack, CustomID
                HashMap<String, CharacterInventory> dupeMap = new HashMap<>();
                Set<ItemStack> items = new HashSet<>();
                int scanned = 0;
                while (rs.next()) {
                    Inventory inv = ItemSerialization.fromString(rs.getString("characters.inventory_storage"), 36);
                    Inventory armor = ItemSerialization.fromString(rs.getString("characters.armour_storage"), 9);
                    Inventory storage = ItemSerialization.fromString(rs.getString("characters.bank_storage"));
                    Inventory mule = ItemSerialization.fromString(rs.getString("characters.mule_storage"));
                    Inventory collection = ItemSerialization.fromString(rs.getString("characters.collection_storage"));

//                    UUID uuid = UUID.fromString(rs.getString("users.uuid"));
                    String username = rs.getString("users.username");
                    int id = rs.getInt("characters.character_id");
                    CharacterInventory map = dupeMap.computeIfAbsent(username, e -> new CharacterInventory(id));

                    addIdsToMap(map.getMap(), inv);
                    addIdsToMap(map.getMap(), armor);
                    addIdsToMap(map.getMap(), storage);
                    addIdsToMap(map.getMap(), mule);
                    addIdsToMap(map.getMap(), collection);
                    scanned++;
                }
                System.out.println("Loaded all items in " + (System.currentTimeMillis() - start) + "ms");

                start = System.currentTimeMillis();
                Set<String> alreadyAlerted = new HashSet<>();
                for (Map.Entry<String, CharacterInventory> entry : dupeMap.entrySet()) {
                    entry.getValue().getMap().forEach((is, id) -> {
                        if (alreadyAlerted.contains(id)) return;
                        Set<CharacterInfo> count = traceCount(id, dupeMap);
                        if (count.size() > 1) {
                            alreadyAlerted.add(id);
                            //First time its alerted.
                            sender.sendMessage(ChatColor.RED + "Found " + count.size() + " of " + Utils.getItemName(is) + ChatColor.RED + " on " + count.size() + " Accounts");
                            for (CharacterInfo info : count)
                                sender.sendMessage(ChatColor.RED + info.getUsername() + " (" + info.getCharacterID() + ")");

                        }
                    });
                }

                System.out.println("Loaded " + scanned + " players data / inventories in " + (System.currentTimeMillis() - start) + "ms");
                statement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
//        });
        return false;
    }


    public Set<CharacterInfo> traceCount(String idToFind, Map<String, CharacterInventory> map) {
        Set<CharacterInfo> found = new HashSet<>();
        for (Map.Entry<String, CharacterInventory> inv : map.entrySet()) {
            CharacterInfo info = new CharacterInfo(inv.getKey(), inv.getValue().getCharacterId());
            for (Map.Entry<ItemStack, String> item : inv.getValue().getMap().entrySet()) {
                if (item.getValue().equals(idToFind)) {
                    //Found a duped item..
                    found.add(info);
                }
            }
        }
        return found;
//        return (int) map.values().stream().flatMap(items -> items.getMap().values().stream()).filter(id -> id.equals(idToFind)).count();
    }

    public void addIdsToMap(Map<ItemStack, String> map, Inventory inventory) {
        if (inventory == null) return;
        for (ItemStack item : inventory) {
            if (item != null && item.getType() != Material.AIR) {
                CraftItemStack handle = (CraftItemStack) item;
                net.minecraft.server.v1_9_R2.ItemStack hand = (net.minecraft.server.v1_9_R2.ItemStack) ReflectionAPI.getObjectFromField("handle", CraftItemStack.class, handle);

                if (hand != null) {
                    NBTTagCompound tag = hand.getTag();
                    if (tag != null) {
                        String id = tag.getString("u");
                        if (id != null && !id.isEmpty()) {
                            map.put(item, id);
                        }
                    }
                }
            }
        }
    }

    class CharacterInventory {
        @Getter
        @Setter
        int characterId;

        @Getter
        Map<ItemStack, String> map = new HashMap<>();

        public CharacterInventory(int characterId) {
            this.characterId = characterId;
        }
    }

    @AllArgsConstructor
    class CharacterInfo {

        @Getter
        String username;

        @Getter
        int characterID;

    }
}
