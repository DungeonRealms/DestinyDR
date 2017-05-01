package net.dungeonrealms.game.world.entity.type.pet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class PetData {
    @Getter
    @Setter
    private String petName;

    @Getter
    @Setter
    private boolean unlocked = false;
}
