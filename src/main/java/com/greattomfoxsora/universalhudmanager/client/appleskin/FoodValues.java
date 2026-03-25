package com.greattomfoxsora.universalhudmanager.client.appleskin;

// Ported from AppleSkin (public domain) by squeek502
public class FoodValues
{
    public final int hunger;
    public final float saturationModifier;

    public FoodValues(int hunger, float saturationModifier)
    {
        this.hunger = hunger;
        this.saturationModifier = saturationModifier;
    }

    public float getSaturationIncrement()
    {
        return hunger * saturationModifier * 2.0F;
    }
}
