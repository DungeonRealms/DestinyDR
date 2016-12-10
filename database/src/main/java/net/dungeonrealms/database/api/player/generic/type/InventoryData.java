package net.dungeonrealms.database.api.player.generic.type;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.database.api.player.generic.IData;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Giovanni on 19-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class InventoryData implements IData {

    @Getter
    private UUID owner;

    public InventoryData(UUID uuid, Document document) {
        this.owner = uuid;
        this.collectionBinBlob = document.getString("collectionBin");
        this.gameInventoryBlob = document.getString("inventory");
        this.armorContents = document.get("armorContents", ArrayList.class);
        this.storageLevel = document.getInteger("storageLevel");

        this.muleBlob = document.getString("mule");
        this.muleLevel = document.getInteger("muleLevel");
        this.storageBlob = document.getString("storage");

        this.hasShop = document.getBoolean("hasOpenShop");
        this.shopLevel = document.getInteger("shopLevel");
    }

    // Default inventory
    @Getter
    @Setter
    private String collectionBinBlob;
    @Getter
    @Setter
    private String gameInventoryBlob;
    @Getter
    @Setter
    private List<String> armorContents;
    @Getter
    @Setter
    private int storageLevel;

    // Other inventories
    @Getter
    @Setter
    private String muleBlob;
    @Getter
    @Setter
    private int muleLevel;
    @Getter
    @Setter
    private String storageBlob;

    // * Shop generic
    @Getter
    @Setter
    private boolean hasShop;
    @Getter
    @Setter
    private int shopLevel;
}
