package net.dungeonrealms.common.awt.reflect;

import java.lang.reflect.Field;

/**
 * Created by Giovanni on 25-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class BasicReflection
{
    public static Object accessFieldObject(String par1, Object par2, Class par3)
    {
        Object object = null;
        try
        {
            par3.getDeclaredField(par1).setAccessible(true);
            object = par3.getDeclaredField(par1).get(par2);
        } catch (NoSuchFieldException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return object;
    }

    public static Field accessField(String par1, Class par2)
    {
        Field field = null;
        try
        {
            field = par2.getDeclaredField(par1);
        } catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        return field;
    }
}
