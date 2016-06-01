package net.dungeonrealms;

import net.dungeonrealms.game.listeners.channel.ProxyChannelListener;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mongo.Database;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Copyright © 2016 APOLLOSOFTWARE.IO
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law.
 */

public class DungeonRealmsProxy extends Plugin {

    private static DungeonRealmsProxy instance;

    @Override
    public void onEnable() {
        instance = this;
        Utils.log.info("DungeonRealmsProxy onEnable() ... STARTING UP");
        Database.getInstance().startInitialization();

        this.getProxy().getPluginManager().registerListener(this, ProxyChannelListener.getInstance());
    }

    public static DungeonRealmsProxy getInstance() {
        return instance;
    }
}
