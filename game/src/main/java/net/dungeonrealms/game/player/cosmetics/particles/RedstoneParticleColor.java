package net.dungeonrealms.game.player.cosmetics.particles;

/**
 * Created by Rar349 on 6/30/2017.
 */
public enum RedstoneParticleColor {

    DARK_RED(1.0f,0f,0f),
    DARK_BLUE(0.0f,0.0f,1.0f),
    DARK_GREEN(0.0f,1.0f,0.0f),
    WHITE(1.0f,1.0f,1.0f),
    BLACK(0.0F,0.0F,0.0F);

    float red;
    float green;
    float blue;

    RedstoneParticleColor(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public float getRed() {
        if(red <= 0) return Float.MIN_VALUE;
        return red;
    }

    public float getGreen() {
        return green;
    }

    public float getBlue() {
        return blue;
    }
}
