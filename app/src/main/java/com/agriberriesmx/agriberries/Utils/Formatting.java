package com.agriberriesmx.agriberries.Utils;

import android.content.Context;
import android.graphics.Color;

import com.agriberriesmx.agriberries.POJO.Block;
import com.agriberriesmx.agriberries.POJO.Item;
import com.agriberriesmx.agriberries.POJO.Nutrient;
import com.agriberriesmx.agriberries.R;
import com.itextpdf.kernel.colors.DeviceRgb;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Formatting {

    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return "";

        // Convert the first character to upper case and the rest to lower case.
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    public static String capitalizeName(String input) {
        // Verify there is an input
        if (input == null || input.isEmpty()) return input;

        // Divide the phrase word by word
        StringBuilder capitalizedWords = new StringBuilder();
        String[] words = input.split("\\s+");
        for (String word : words) {
            String capitalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
            capitalizedWords.append(capitalizedWord).append(" ");
        }

        return capitalizedWords.toString().trim();
    }

    public static Date addSevenDaysAndFormat(Date date) {
        // Convert date to calendar
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, 7);

        // Beginning of the day
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public static List<Block> getSortedBlockList(List<Block> blocks) {
        blocks.sort((b1, b2) -> {
            if (b1.getRow() != b2.getRow()) return b1.getRow() - b2.getRow();
            else return b1.getCol() - b2.getCol();
        });

        return blocks;
    }

    public static List<Item> getSortedItemList(List<Item> items) {
        items.sort((b1, b2) -> {
            if (b1.getRow() != b2.getRow()) return b1.getRow() - b2.getRow();
            else return b1.getCol() - b2.getCol();
        });

        return items;
    }

    public static List<Nutrient> getSortedNutrientList(List<Nutrient> nutrients) {
        nutrients.sort((b1, b2) -> {
            if (b1.getRow() != b2.getRow()) return b1.getRow() - b2.getRow();
            else return b1.getCol() - b2.getCol();
        });

        return nutrients;
    }

    public static DeviceRgb getColorForIntensity(int intensity) {
        final int[] levelColors = {
                Color.parseColor("#676767"),
                Color.parseColor("#029443"),
                Color.parseColor("#8DC741"),
                Color.parseColor("#FFDE16"),
                Color.parseColor("#F46523"),
                Color.parseColor("#ED1B24")};

        if (intensity >= 0 && intensity < levelColors.length) {
            int colorInt = levelColors[intensity];
            int red = (colorInt >> 16) & 0xff;
            int green = (colorInt >> 8) & 0xff;
            int blue = colorInt & 0xff;
            return new DeviceRgb(red, green, blue);
        } else return new DeviceRgb();
    }

    public static DeviceRgb getColorForPercentage(int percentage) {
        final int[] levelColors = {
                Color.parseColor("#676767"),
                Color.parseColor("#ED1B24"),
                Color.parseColor("#F46523"),
                Color.parseColor("#FFDE16"),
                Color.parseColor("#8DC741"),
                Color.parseColor("#029443"),
                Color.parseColor("#676767")
        };

        // Check value between 0 and 100
        if (percentage < 0) percentage = 0;
        if (percentage > 100) percentage = 100;

        // Get index based on the percentage
        int index = percentage / 20;
        if (percentage != 0 && percentage != 100) index++;

        int colorInt = levelColors[index];
        int red = (colorInt >> 16) & 0xff;
        int green = (colorInt >> 8) & 0xff;
        int blue = colorInt & 0xff;
        return new DeviceRgb(red, green, blue);
    }

    public static DeviceRgb getColorForNutrient(Context context, String crop, double value, int type) {
        // Verify if there is a value
        if (value != 0) {
            // Define colors for nutrients
            final int redColor = Color.parseColor("#ED1B24");
            final int yellowColor = Color.parseColor("#FFDE16");
            final int greenColor = Color.parseColor("#8DC741");

            // Get actual color
            int nutrientColor;
            switch (type) {
                case 1:
                    // Sodium
                    if (value <= 200) nutrientColor = greenColor;
                    else nutrientColor = redColor;

                    break;
                case 2:
                    // pH
                    double lowerLimit = 5.8;
                    double upperLimit = 6.5;

                    // Change limits (if its necessary)
                    if (crop.equals(context.getResources().getString(R.string.blueberry))) {
                        lowerLimit = 4.8;
                        upperLimit = 5.8;
                    } else if (crop.equals(context.getResources().getString(R.string.fig))) {
                        lowerLimit = 5.5;
                        upperLimit = 7.5;
                    }

                    // Get color
                    if (value < lowerLimit) nutrientColor = redColor;
                    else if (value > upperLimit) nutrientColor = yellowColor;
                    else nutrientColor = greenColor;

                    break;
                default:
                    // Anything else
                    if (value < 100) nutrientColor = yellowColor;
                    else if (value > 200) nutrientColor = redColor;
                    else nutrientColor = greenColor;

                    break;
            }

            int colorInt = nutrientColor;
            int red = (colorInt >> 16) & 0xff;
            int green = (colorInt >> 8) & 0xff;
            int blue = colorInt & 0xff;

            return new DeviceRgb(red, green, blue);
        } else return null;
    }

}
