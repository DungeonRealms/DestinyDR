package net.dungeonrealms.control.database.cache;

import lombok.Getter;
import net.dungeonrealms.common.awt.data.DataPlayer;
import net.dungeonrealms.common.awt.data.verify.EnumVerificationResult;
import net.dungeonrealms.common.awt.data.verify.VerificationResult;
import net.dungeonrealms.control.DRControl;
import net.dungeonrealms.control.utils.UtilLogger;
import net.dungeonrealms.packet.player.PacketPlayerDataGet;
import net.dungeonrealms.packet.player.PacketPlayerDataSend;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Giovanni on 5-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class DataCache {

    @Getter
    private final AtomicReference<ConcurrentHashMap<UUID, DataPlayer>> playerCache = new AtomicReference<>();

    @Getter
    private boolean allowCaching = false;

    public DataCache() {
        if (DRControl.getInstance().getMongoConnection() != null) {
            this.playerCache.set(new ConcurrentHashMap<>());
            this.allowCaching = true;
            UtilLogger.info("Player Cache created, data can now be accepted");
        } else {
            UtilLogger.critical("FATAL: No Mongo connection detected, force shutdown");
            DRControl.getInstance().shutdown();
        }
    }

    public void flush() {
        UtilLogger.warn("Player Cache has been flushed, all player data has been sent to the Mongo database");
        this.saveAll(true);
        this.playerCache.get().clear();
    }

    public void saveAll(boolean flush) {
        for (DataPlayer dataPlayer : this.playerCache.get().values()) {
            // Save the player data
            DRControl.getInstance().getMongoConnection().getApi().saveDataPlayer(dataPlayer.getUniqueId(), flush);
        }
    }

    public void save(UUID uuid) {
        DRControl.getInstance().getMongoConnection().getApi().saveDataPlayer(uuid, false);
    }

    public void saveAndRemove(UUID uuid) {
        DRControl.getInstance().getMongoConnection().getApi().saveDataPlayer(uuid, true);
    }

    public DataPlayer getData(UUID uniqueId) {
        return this.playerCache.get().get(uniqueId);
    }

    public VerificationResult verifyAndCache(PacketPlayerDataSend packet) {
        if (packet != null) {
            if (!this.playerCache.get().containsKey(packet.getDataOwner())) {
                UUID owner = packet.getDataOwner();
                // Request the data out of the mongo
                DRControl.getInstance().getMongoConnection().getApi().requestPlayerData(owner);
                // Verify the data
                if (DRControl.getInstance().getMongoConnection().getApi().exists(owner)) {
                    DataPlayer dataPlayer = DRControl.getInstance().getMongoConnection().getApi().getPlayer(owner);
                    this.playerCache.get().put(owner, dataPlayer);
                    return new VerificationResult(EnumVerificationResult.SUCCESS, null);
                } else return new VerificationResult(EnumVerificationResult.FAILED, null);
            } else return new VerificationResult(EnumVerificationResult.FAILED, null);
        } else
            return new VerificationResult(EnumVerificationResult.FAILED, null);
    }

    public PacketPlayerDataGet verifyAndGet(UUID uuid) {
        if (this.playerCache.get().containsKey(uuid)) {
            DataPlayer dataPlayer = this.playerCache.get().get(uuid);
            return new PacketPlayerDataGet(new VerificationResult(EnumVerificationResult.SUCCESS, dataPlayer));
        } else return new PacketPlayerDataGet(new VerificationResult(EnumVerificationResult.FAILED, null));
    }
}
