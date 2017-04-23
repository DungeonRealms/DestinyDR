package net.dungeonrealms.game.command.friend;

import lombok.Cleanup;
import lombok.NonNull;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by iFamasssxD on 4/19/2017.
 */
public class FriendsCommand extends BaseCommand implements CooldownCommand {

    public FriendsCommand(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player player = (Player) s;

        if (checkCooldown(player)) return true;
        getFriendsBook(player, (book) -> GameAPI.openBook(player, book));
        return false;
    }

    @Override
    public long getCooldown() {
        return TimeUnit.SECONDS.toMillis(10);
    }

    private static String new_line = "\n" + ChatColor.WHITE.toString() + "`" + "\n";
    private static String defaultHeader = (ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "    Friends List    " + new_line);

    private void getFriendsBook(Player player, @NonNull Consumer<ItemStack> bookCallback) {
        ItemStack stack = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bm = (BookMeta) stack.getItemMeta();
        List<String> pages = new ArrayList<>();


        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return;
        Map<UUID, Integer> friends = wrapper.getFriendsList();
        if (friends.isEmpty()) {
            pages.add(defaultHeader);
            bm.setAuthor("King Bulwar");
            bm.setPages(pages);
            stack.setItemMeta(bm);
            bookCallback.accept(stack);
        } else {
            CompletableFuture.runAsync(() -> {

                //Just pull all this data right here that we need for this.
                StringBuilder builder = new StringBuilder("SELECT is_online, currentShard, username, last_logout FROM users WHERE");

                friends.forEach((uuid, accountId) -> builder.append(" account_id = '").append(accountId).append("' ").append("OR"));

                String query = builder.toString();
                if (query.endsWith("OR")) {
                    query = query.substring(0, query.length() - 2).trim();
                }
                int count = 0;
                String nextLine = "\n";
                String friendsPage_string = ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "    Friends List    " + new_line;
                try {
                    @Cleanup PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement(query);
                    ResultSet rs = statement.executeQuery();
                    while (rs.next()) {
                        //Found friend info...
                        boolean online = rs.getBoolean("users.is_online");
                        String name = rs.getString("username");
                        String shard = rs.getString("users.currentShard");

                        long currentTime = System.currentTimeMillis();
                        long endTime = rs.getLong("users.last_logout");
                        long millis = currentTime - endTime;
                        String time = GameAPI.formatTime(millis);
                        time += nextLine;

                        if (name.length() >= 15)
                            name = name.substring(0, 15);
                        friendsPage_string += (online ? ChatColor.GREEN + ChatColor.BOLD.toString() + "O" : ChatColor.DARK_RED + ChatColor.BOLD.toString() + "O") + ChatColor.BLACK + ChatColor.BOLD.toString() + " " + name + nextLine;
                        friendsPage_string += online ? ChatColor.BLACK + "Shard: " + ChatColor.BOLD + shard + nextLine : ChatColor.BLACK + "Last On: " + time;


                        count++;
                        //Every 5 or if its the last UUID?
                        if (count == 5 || count == friends.size()) {
                            count = 0;
                            pages.add(friendsPage_string);
                            friendsPage_string = ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "   Friends List  " + new_line;
                            if (count == friends.size())
                                break;
                        }
                    }
                    bm.setAuthor("King Bulwar");
                    bm.setPages(pages);
                    stack.setItemMeta(bm);
                    bookCallback.accept(stack);
                } catch (Exception e) {
                    e.printStackTrace();
                    bookCallback.accept(stack);
                }
            }, ForkJoinPool.commonPool());
        }
    }

    @Override
    public String getName() {
        return "friend";
    }
}
