package net.dungeonrealms.database.data;

import lombok.Getter;
import net.dungeonrealms.common.awt.data.DataPlayer;
import net.dungeonrealms.common.awt.data.verify.EnumVerificationResult;
import net.dungeonrealms.common.awt.data.verify.VerificationResult;
import net.dungeonrealms.database.Database;
import net.dungeonrealms.database.packet.PacketPipeline;
import net.dungeonrealms.packet.Packet;
import net.dungeonrealms.packet.player.out.PacketPlayerData;
import net.dungeonrealms.packet.player.out.PacketPlayerDataSend;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Giovanni on 5-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class DataCache extends PacketPipeline {

    // TODO

    @Getter
    private final AtomicReference<ConcurrentHashMap<UUID, DataPlayer>> playerCache = new AtomicReference<>();

    @Getter
    private boolean allowCaching = false;

    public DataCache() {
        if (Database.getInstance().getMongoConnection() != null) {
            this.playerCache.set(new ConcurrentHashMap<>());
            this.allowCaching = true;
        } else {
            Database.getInstance();
        }
    }

    public void flush() {
        this.saveAll(true);
        this.playerCache.get().clear();
    }

    public void saveAll(boolean flush) {
        for (DataPlayer dataPlayer : this.playerCache.get().values()) {
            // Save the player data
            Database.getInstance().getMongoConnection().getApi().saveDataPlayer(dataPlayer.getUniqueId(), flush);
        }
    }

    public void save(UUID uuid) {
        Database.getInstance().getMongoConnection().getApi().saveDataPlayer(uuid, false);
    }

    public void saveAndRemove(UUID uuid) {
        Database.getInstance().getMongoConnection().getApi().saveDataPlayer(uuid, true);
    }

    private VerificationResult verifyAndCache(PacketPlayerDataSend packet) {
        if (packet != null) {
            if (!this.playerCache.get().containsKey(packet.getDataOwner())) {
                UUID owner = packet.getDataOwner();
                // Request the data out of the mongo
                Database.getInstance().getMongoConnection().getApi().requestPlayerData(owner);
                // Verify the data
                if (Database.getInstance().getMongoConnection().getApi().exists(owner)) {
                    DataPlayer dataPlayer = Database.getInstance().getMongoConnection().getApi().getPlayer(owner);
                    this.playerCache.get().put(owner, dataPlayer);
                    return new VerificationResult(EnumVerificationResult.SUCCESS, null);
                } else return new VerificationResult(EnumVerificationResult.FAILED, null);
            } else return new VerificationResult(EnumVerificationResult.FAILED, null);
        } else
            return new VerificationResult(EnumVerificationResult.FAILED, null);
    }

    private PacketPlayerData verifyAndGet(UUID uuid) {
        if (this.playerCache.get().containsKey(uuid)) {
            DataPlayer dataPlayer = this.playerCache.get().get(uuid);
            return new PacketPlayerData(new VerificationResult(EnumVerificationResult.SUCCESS, dataPlayer));
        } else return new PacketPlayerData(new VerificationResult(EnumVerificationResult.FAILED, null));
    }

    @Override
    public void handlePacket(Packet packet) {
        if (packet instanceof PacketPlayerDataSend) {
            // Cache the data
            this.verifyAndCache((PacketPlayerDataSend) packet);
        }
    }
}
