package net.dungeonrealms.common.backend.player.data.type;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.common.backend.player.data.IData;
import org.bson.Document;

import java.util.UUID;

/**
 * Created by Giovanni on 19-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class SettingsData implements IData {
    @Getter
    private UUID owner;

    public SettingsData(UUID owner, Document document) {
        this.owner = owner;
        this.globalChatEnabled = document.getBoolean("enabledGlobalChat");
        this.tradeChatEnabled = document.getBoolean("enabledTradeChat");
        this.tradingEnabled = document.getBoolean("tradingEnabled");
        this.allowReceiveMessages = document.getBoolean("receiveMessages");
        this.allowPvP = document.getBoolean("pvp");
        this.enabledChaoticPrevention = document.getBoolean("enabledChaoticPrevention");
        this.allowDuels = document.getBoolean("duels");
        this.allowTips = document.getBoolean("tips");
    }

    // Values
    @Getter
    @Setter
    private boolean globalChatEnabled;
    @Getter
    @Setter
    private boolean tradeChatEnabled;
    @Getter
    @Setter
    private boolean tradingEnabled;
    @Getter
    @Setter
    private boolean allowReceiveMessages;
    @Getter
    @Setter
    private boolean allowPvP;
    @Getter
    @Setter
    private boolean enabledChaoticPrevention;
    @Getter
    @Setter
    private boolean allowDuels;
    @Getter
    @Setter
    private boolean allowTips;
}
