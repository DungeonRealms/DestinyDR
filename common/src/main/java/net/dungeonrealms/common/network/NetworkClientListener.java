package net.dungeonrealms.common.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class NetworkClientListener extends Listener implements GenericMechanic {

    static NetworkClientListener instance = null;


    public static NetworkClientListener getInstance() {
        if (instance == null) {
            instance = new NetworkClientListener();
        }
        return instance;
    }

    @Override
    public void startInitialization() {

    }

    @Override
    public void stopInvocation() {

    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }


    @Override
    public void received(Connection connection, Object object) {


    }


}