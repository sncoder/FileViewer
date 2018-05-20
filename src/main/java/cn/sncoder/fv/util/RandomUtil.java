package cn.sncoder.fv.util;

import java.util.Random;

public class RandomUtil {

    private static final char[] SEQUENCE = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G',
            'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    private static final int MAX_NUM = 10000;

    public static String randomSequence(int num) {
        return randomSequence(0, SEQUENCE.length, num);
    }

    public static String randomLetters(int num) {
        return randomSequence(0, 52, num);
    }

    public static String randomLowerCases(int num) {
        return randomSequence(0, 26, num);
    }

    public static String randomUpperCases(int num) {
        return randomSequence(26, 52, num);
    }

    public static String randomUpperCasesOrDigits(int num) {
        return randomSequence(26, SEQUENCE.length, num);
    }

    public static String randomDigits(int num) {
        return randomSequence(52, SEQUENCE.length, num);
    }

    public static int randomInt() {
        Random random = new Random();
        return random.nextInt();
    }

    /**
     * 随机返回 0 至 (max - 1)之间的数字
     */
    public static int randomInt(int max) {
        Random random = new Random();
        return random.nextInt(max);
    }

    /**
     * 随机返回 min 至 (max - 1)之间的数字
     */
    public static int randomInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    private static String randomSequence(int begin, int end, int num) {
        checkNum(num);
        Random random = new Random();
        char[] chars = new char[num];
        int size = end - begin;
        for (int i = 0; i < num; ++i) {
            chars[i] = SEQUENCE[begin + random.nextInt(size)];
        }
        return new String(chars);
    }

    private static void checkNum(int num) {
        if (num <= 0) {
            throw new IllegalArgumentException("num can not be a negative number : " + num);
        } else if (num > MAX_NUM) {
            throw new IllegalArgumentException("num is too large : " + num + ", maxNum is " + MAX_NUM);
        }
    }

}
