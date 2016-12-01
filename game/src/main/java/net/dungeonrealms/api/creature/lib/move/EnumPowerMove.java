package net.dungeonrealms.api.creature.lib.move;

import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.List;
import java.util.Random;

/**
 * Created by Giovanni on 1-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public enum EnumPowerMove {

    // Dodging & blocking will be considered as power moves, except handled differently
    DODGE(0, "attributeDodge"), BLOCK(1, "attributeBlock"), WHIRLWIND(2, ("cristianoRonaldo")), ICE_BREATH(3, "ricardoQuaresma"), EMPTY(4, "empty");
    // Non item based powermoves don't need an identifier

    @Getter
    private int id;

    @Getter
    private String identifier;

    private static List<EnumPowerMove> combatMoves = Lists.newArrayList();

    EnumPowerMove(int id, String identifier) {
        this.id = id;
        this.identifier = identifier;
    }

    public static EnumPowerMove randomCombatMove() {
        // Flush
        combatMoves.clear();
        // Add
        combatMoves.add(EMPTY);
        combatMoves.add(DODGE);
        combatMoves.add(BLOCK);
        return combatMoves.get(new Random().nextInt(combatMoves.size()));
    }

    public static EnumPowerMove getById(int id) {
        for (EnumPowerMove powerMove : values()) {
            if (powerMove.getId() == id) {
                return powerMove;
            }
        }
        return null;
    }
}
