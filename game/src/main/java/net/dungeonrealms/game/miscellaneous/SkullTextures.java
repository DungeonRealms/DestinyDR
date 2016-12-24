package net.dungeonrealms.game.miscellaneous;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * Created by Kieran Quigley (Proxying) on 06-Jun-16.
 */
public enum SkullTextures {

    /*
    * Takes a base 64 string of the skull, can be found here; http://heads.freshcoal.com/ or via the players texture page on Minecraft.
    */
    PUG("456de2b44c66a6b039b1a2e46561af76e829faa27626aaf8b948a5065c81262"),
    BEAGLE("aa266a9acd19cec3cb7e956e335774944f154633ce89993c566f766fcb0cd3d"),
    CORGI("725ed61167dd023babd33a391e9e26ecaafb29875e9ad605dc6d9fae99c30"),
    CHEWBACCA("91703822898b51ac1266a4d3837bc69294defe1c2bb17b03e2087fd5acf"),
    SLOTH("8e78fde0624d888a6e226be91706f9d18f1a4726c1292a356587de3874e8ad9"),
    TROLL("bcae421956c9469ff2558843392a13e5d7beb9ca4ac6cac7eeb18f4dd4b383"),
    TRICERATOPS("6bc473afa6be7dc18a79b495fa10e8ac48b14a3efe92c8f2f11e8b1016d0"),
    BB8("a867acfd786b666de87578fc1e632e6bed4d1bd7699c270ba9e13c55211"),
    PIRATE("46d6601240a9edb8a65f6fe434e36bfb783ab184af97979a9413e24b53de59"),
    BANDIT("aeef4bdafd3564984fc58fddbcbe456dc92328bf080ce77a26c15cd302576"),
    DEVIL("9b1df3bd22c3a49ed2f29ef4790dd6e21cdd736d33e3db9104bf562b7bf4c"),
    BANDIT_2("8251dc366d2b2edb3b03544e8b98db85cb3d6106440aef27ea79b9f39cd178b"),
    GOBLIN("7a55511a422ef9afde24f8639cf3dd8c202a324984fad30e03e47e3ad64ccd"),
    ICE_BOSS("69cf2691773922c397d572336f4216a3573c9e152daf67a8b19ee248819140"),
    NAGA("52fcf0814195a435b1a7bc8e05541bbb4eaf18debeda255ebdb38c2348cb1e3"),
    LIZARD("9a78e1ba93ee997882c853ebbadc531fca3d640c171c01d31eebc8df11ade"),
    ZOMBIE("36aae86da0cd317a47fa6668fd4785b5a7a7e4ed9e7bc68652bae27984b84c"),
    MONK("7d46793bdbce5cad5f37b124eaf1e3689bba18d59a8068567c74f4ff1a18"),
    TRIPOLI_SOLDIER("4c31753d3440b2a7d115f6a7542cd4b2487cb7ada0c945be4d4df62bb39d4067"),
    MAGE("15a4c33adda9ade4a2379eedb5e6cd0c5f3aaa772cd689cd6788c8314fab56"),
    IMP("15c23b79bedc5ca92d84878495d69dfc6b47224b51c57f1caea55e7c2a8537"),
    SKELETON("5cd713c5f5e46da436a8f54b523d43af29f7ae8fb184792cca73b1717feaa61"),
    FROZEN_SKELETON("041646c03f8b4cdbbf81f7164dd63a29c963a6c6cebfe1caf19a2ee92c");

    private String b64String;

    SkullTextures(String base64String) {
        this.b64String = base64String;
    }

    public String getURL() {
        return "http://textures.minecraft.net/texture/" + this.b64String;
    }

    public ItemStack getSkull() {
        String url = getURL();
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        if (url == null || url.isEmpty()) {
            return skull;
        }
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;
        try {
            profileField = skullMeta.getClass().getDeclaredField("profile");
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        assert profileField != null;
        profileField.setAccessible(true);
        try {
            profileField.set(skullMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        skull.setItemMeta(skullMeta);
        return skull;
    }
}
