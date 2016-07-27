package net.dungeonrealms.common.game.database;

import lombok.Getter;
import org.bson.conversions.Bson;

import java.util.function.Consumer;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/27/2016
 */
public class UpdateQuery<T> {

    @Getter
    private Bson bson, bson1;

    @Getter
    private Consumer<T> consumer;

    public UpdateQuery(Bson bson, Bson bson1, Consumer<T> consumer) {
        this.bson = bson;
        this.bson1 = bson1;
        this.consumer = consumer;
    }


}
