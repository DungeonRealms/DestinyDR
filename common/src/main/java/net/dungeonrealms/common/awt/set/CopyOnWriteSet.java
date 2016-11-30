package net.dungeonrealms.common.awt.set;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Giovanni on 16-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public abstract class CopyOnWriteSet<E> implements Set<E> {
    private final AtomicReference<Set<E>> ref;

    public CopyOnWriteSet(Collection<? extends E> c) {
        this.ref = new AtomicReference<Set<E>>(new HashSet<E>(c));
    }

    @Override
    public boolean contains(Object o) {
        return this.ref.get().contains(o);
    }

    @Override
    public boolean add(E e) {
        while (true) {
            Set<E> current = this.ref.get();
            if (current.contains(e)) {
                return false;
            }
            Set<E> modified = new HashSet<E>(current);
            modified.add(e);
            if (this.ref.compareAndSet(current, modified)) {
                return true;
            }
        }
    }

    @Override
    public boolean remove(Object o) {
        while (true) {
            Set<E> current = this.ref.get();
            if (!current.contains(o)) {
                return false;
            }
            Set<E> modified = new HashSet<E>(current);
            modified.remove(o);
            if (this.ref.compareAndSet(current, modified)) {
                return true;
            }
        }
    }
}