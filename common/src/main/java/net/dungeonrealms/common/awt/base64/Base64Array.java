package net.dungeonrealms.common.awt.base64;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Giovanni on 7-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class Base64Array {

    // TODO Rewrite this

    private Object[] targetCollection;

    private String array;

    private ByteArrayOutputStream outputStream;

    public Base64Array(Object[] collection) {
        this.targetCollection = collection;
    }

    public Base64Array(String array) {
        this.array = array;
    }

    public Base64Array stream() {
        this.outputStream = new ByteArrayOutputStream();
        return this;
    }

    public Object value() {
        if (this.array == null) {
            try {
                BukkitObjectOutputStream objectOutputStream = new BukkitObjectOutputStream(this.outputStream);
                objectOutputStream.writeInt(this.targetCollection.length);
                for (int i = 0; i < this.targetCollection.length; i++) {
                    objectOutputStream.writeObject(this.targetCollection[i]);
                }
                objectOutputStream.close();
                return Base64Coder.encodeLines(this.outputStream.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Object[] array() {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(this.array));
            BukkitObjectInputStream objectInputStream = new BukkitObjectInputStream(inputStream);
            Object[] objects = new Object[objectInputStream.readInt()];
            for (int i = 0; i < objects.length; i++) {
                objects[i] = objectInputStream.readObject();
            }
            objectInputStream.close();
            return objects;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ItemStack[] itemStackArray() {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(this.array));
            BukkitObjectInputStream objectInputStream = new BukkitObjectInputStream(inputStream);
            ItemStack[] objects = new ItemStack[objectInputStream.readInt()];
            for (int i = 0; i < objects.length; i++) {
                objects[i] = (ItemStack) objectInputStream.readObject();
            }
            objectInputStream.close();
            return objects;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
