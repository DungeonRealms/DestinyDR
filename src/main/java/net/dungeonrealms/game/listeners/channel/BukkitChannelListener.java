package net.dungeonrealms.game.listeners.channel;

import net.dungeonrealms.DungeonRealms;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

/**
 * Copyright © 2016 APOLLOSOFTWARE.IO
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law.
 */

public class BukkitChannelListener implements PluginMessageListener {

    private static BukkitChannelListener instance;

    public static BukkitChannelListener getInstance() {
        if (instance == null) {
            instance = new BukkitChannelListener(DungeonRealms.getInstance());
        }
        return instance;
    }

    public BukkitChannelListener(DungeonRealms plugin) {
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, "DungeonRealms");
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, "DungeonRealms", this);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {

        if (!channel.equals("DungeonRealms")) return;

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));

        try {
            String result = in.readUTF();


        } catch (EOFException e) {
            // Do nothing.
        } catch (IOException e) {
            // This should never happen.
            e.printStackTrace();
        }
    }
}