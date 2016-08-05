package net.dungeonrealms.proxy;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.BungeeTabListPlusAPI;
import codecrafter47.bungeetablistplus.api.bungee.tablist.Slot;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabList;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListProvider;
import codecrafter47.bungeetablistplus.data.DataKey;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import static codecrafter47.bungeetablistplus.common.BTLPDataKeys.ThirdPartyVariableDataKey;


/**
 * Class written by APOLLOSOFTWARE.IO on 8/4/2016
 */

public class GameTab implements TabListProvider {

    @Override
    public void fillTabList(ProxiedPlayer player, TabList tabList, TabListContext context) {
        // set the header
        tabList.setHeader("&6Hi &5" + player.getDisplayName());

        DataKey<String> key = ThirdPartyVariableDataKey.builder().id("fillguild").bukkit().build();


        String test = BungeeTabListPlus.getInstance().getConnectedPlayerManager().getPlayer(player).get(key).get();


//        // set a slot
//        tabList.setSlot(0, 0, );

        // set a slot with ping
        tabList.setSlot(0, 1, new Slot("Test ping", -1));

        // set a slot with ping and skin
        tabList.setSlot(0, 1, new Slot("&7Herobrine skin!", -1, BungeeTabListPlusAPI.getSkinForPlayer("Herobrine")));

        // set the ping to use for all slots we haven't filled
        tabList.setDefaultPing(0);

        // set the skin to use for all slots we haven't filled
        tabList.setDefaultSkin(BungeeTabListPlusAPI.getSkinForPlayer("MHF_ArrowRight"));
    }
}