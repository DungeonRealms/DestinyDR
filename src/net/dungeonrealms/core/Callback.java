package net.dungeonrealms.core;

/**
 * Created by Nick on 10/17/2015.
 */
public abstract class Callback<T> {

    private final Class<T> clazz;

    public Callback(Class<T> clazz) {
        this.clazz = clazz;
    }

    public abstract void callback(Throwable failCause, T result);

}