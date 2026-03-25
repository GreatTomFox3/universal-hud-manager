package com.greattomfoxsora.universalhudmanager.client.appleskin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

// Ported from AppleSkin (public domain) by squeek502
public class FoodHelper
{
    public static boolean isFood(ItemStack itemStack, Player player)
    {
        return itemStack.getItem().getFoodProperties(itemStack, player) != null;
    }

    public static boolean canConsume(ItemStack itemStack, Player player)
    {
        if (!isFood(itemStack, player))
            return false;

        FoodProperties itemFood = itemStack.getItem().getFoodProperties(itemStack, player);
        if (itemFood == null)
            return false;

        return player.canEat(itemFood.canAlwaysEat());
    }

    public static FoodValues getDefaultFoodValues(ItemStack itemStack, Player player)
    {
        FoodProperties itemFood = itemStack.getItem().getFoodProperties(itemStack, player);
        int hunger = itemFood != null ? itemFood.getNutrition() : 0;
        float saturationModifier = itemFood != null ? itemFood.getSaturationModifier() : 0;
        return new FoodValues(hunger, saturationModifier);
    }

    public static FoodValues getModifiedFoodValues(ItemStack itemStack, Player player)
    {
        return getDefaultFoodValues(itemStack, player);
    }

    public static boolean isRotten(ItemStack itemStack, Player player)
    {
        if (!isFood(itemStack, player))
            return false;

        for (Pair<MobEffectInstance, Float> effect : itemStack.getItem().getFoodProperties(itemStack, player).getEffects())
        {
            if (effect.getFirst() != null && effect.getFirst().getEffect() != null
                && effect.getFirst().getEffect().getCategory() == MobEffectCategory.HARMFUL)
            {
                return true;
            }
        }
        return false;
    }

    public static float getEstimatedHealthIncrement(ItemStack itemStack, FoodValues modifiedFoodValues, Player player)
    {
        if (!isFood(itemStack, player))
            return 0;

        if (!player.isHurt())
            return 0;

        FoodData stats = player.getFoodData();
        Level world = player.getCommandSenderWorld();

        int foodLevel = Math.min(stats.getFoodLevel() + modifiedFoodValues.hunger, 20);
        float healthIncrement = 0;

        if (foodLevel >= 18.0F && world != null && world.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION))
        {
            float saturationLevel = Math.min(stats.getSaturationLevel() + modifiedFoodValues.getSaturationIncrement(), (float) foodLevel);
            float exhaustionLevel = stats.getExhaustionLevel();
            healthIncrement = getEstimatedHealthIncrement(foodLevel, saturationLevel, exhaustionLevel);
        }

        for (Pair<MobEffectInstance, Float> effect : itemStack.getItem().getFoodProperties(itemStack, player).getEffects())
        {
            MobEffectInstance effectInstance = effect.getFirst();
            if (effectInstance != null && effectInstance.getEffect() == MobEffects.REGENERATION)
            {
                int amplifier = effectInstance.getAmplifier();
                int duration = effectInstance.getDuration();
                healthIncrement += (float) Math.floor(duration / Math.max(50 >> amplifier, 1));
                break;
            }
        }

        return healthIncrement;
    }

    public static float REGEN_EXHAUSTION_INCREMENT = 6.0F;
    public static float MAX_EXHAUSTION = 4.0F;

    public static float getEstimatedHealthIncrement(int foodLevel, float saturationLevel, float exhaustionLevel)
    {
        float health = 0;

        if (!Float.isFinite(exhaustionLevel) || !Float.isFinite(saturationLevel))
            return 0;

        while (foodLevel >= 18)
        {
            while (exhaustionLevel > MAX_EXHAUSTION)
            {
                exhaustionLevel -= MAX_EXHAUSTION;
                if (saturationLevel > 0)
                    saturationLevel = Math.max(saturationLevel - 1, 0);
                else
                    foodLevel -= 1;
            }
            if (foodLevel >= 20 && Float.compare(saturationLevel, Float.MIN_NORMAL) > 0)
            {
                float limitedSaturationLevel = Math.min(saturationLevel, REGEN_EXHAUSTION_INCREMENT);
                float exhaustionUntilAboveMax = Math.nextUp(MAX_EXHAUSTION) - exhaustionLevel;
                int numIterationsUntilAboveMax = Math.max(1, (int) Math.ceil(exhaustionUntilAboveMax / limitedSaturationLevel));
                health += (limitedSaturationLevel / REGEN_EXHAUSTION_INCREMENT) * numIterationsUntilAboveMax;
                exhaustionLevel += limitedSaturationLevel * numIterationsUntilAboveMax;
            }
            else if (foodLevel >= 18)
            {
                health += 1;
                exhaustionLevel += REGEN_EXHAUSTION_INCREMENT;
            }
        }

        return health;
    }
}
