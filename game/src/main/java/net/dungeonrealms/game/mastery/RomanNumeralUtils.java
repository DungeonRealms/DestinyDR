package net.dungeonrealms.game.mastery;

import org.apache.commons.lang.Validate;

import java.util.HashMap;
import java.util.Map;

public class RomanNumeralUtils {

    private enum RomanNumber {
        // It is important that the order is in reverse
        M(
                1000),
        D(
                500),
        C(
                100),
        L(
                50),
        X(
                10),
        V(
                5),
        I(
                1);

        private final int valueInDec;

        /**
         * Maps the RomanNumbers to decimal equivalents
         *
         * @param decimal value which represents the roman number
         */
        private RomanNumber(int decimal) {
            this.valueInDec = decimal;
        }

        /**
         * Return the decimal representation
         *
         * @return decimal of the roman number
         */
        public int getInDecimal() {
            return valueInDec;
        }
    }


    private static Map<Integer, String> cachedNumerals = new HashMap<>();
    /**
     * Gets the Roman Numeral string representing the given value
     *
     * @param value value to be converted
     * @return Roman Numeral String
     */
    public static String numeralOf(int value) {
        Validate.isTrue(value > 0, "Roman numbers can't express zero or negative numbers!");

        String found = cachedNumerals.get(value);
        if(found != null)return found;
        StringBuilder builder = new StringBuilder();
        RomanNumber[] romanNumbers = RomanNumber.values();

        for (int i = 0; i < romanNumbers.length; i++) {
            RomanNumber romanNumber = romanNumbers[i];

            // Regular values
            while (value >= romanNumber.getInDecimal()) {
                value -= romanNumber.getInDecimal();
                builder.append(romanNumber.name());
            }

            // Subtraction values
            if (i < romanNumbers.length - 1) {
                int index = i - i % 2 + 2;
                RomanNumber subtractNum = romanNumbers[index];

                if (value >= romanNumber.getInDecimal() - subtractNum.getInDecimal()) {
                    value -= romanNumber.getInDecimal() - subtractNum.getInDecimal();
                    builder.append(subtractNum.name());
                    builder.append(romanNumber.name());
                }
            }
        }

        String str = builder.toString();
        cachedNumerals.put(value, str);
        return str;
    }
}
