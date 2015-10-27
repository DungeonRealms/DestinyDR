package net.dungeonrealms.mechanics;

import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by Nick on 10/26/2015.
 */
public class TwitterManager implements GenericMechanic {

    static TwitterManager instance = null;

    public static TwitterManager getInstance() {
        if (instance == null)
            instance = new TwitterManager();
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }

    //api = Api.builder().username("DungeonAlert").password("|1#M3!&[3|D[3++}&~8~").build();

    @Override
    public void startInitialization() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("*********************")
                .setOAuthConsumerSecret("******************************************")
                .setOAuthAccessToken("**************************************************")
                .setOAuthAccessTokenSecret("******************************************");
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();

        try {
            twitter.updateStatus("test");
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopInvocation() {

    }
}
