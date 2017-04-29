package net.dungeonrealms.database.punishment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum PunishType {
    BAN("ban"),
    MUTE("mute");

    @Getter
    private String type;
    public static PunishType getFromType(String type){
        for(PunishType pun : values()){
            if(pun.getType().equals(type))return pun;
        }
        return null;
    }
}
