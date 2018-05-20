package cn.sncoder.fv.util;

import info.monitorenter.cpdetector.io.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class FileUtil {

    public static final long ONE_KB = 1024;

    public static final long ONE_MB = ONE_KB * 1024;

    public static final long ONE_GB = ONE_MB * 1024;

    public static final long ONE_TB = ONE_GB * 1024;

    public static final long ONE_PB = ONE_TB * 1024;

    /**
     * detector是探测器，它把探测任务交给具体的探测实现类的实例完成。
     * cpDetector内置了一些常用的探测实现类，这些探测实现类的实例可以通过add方法 加进来，
     * 如ParsingDetector、JChardetFacade、ASCIIDetector、UnicodeDetector。
     * detector按照“谁最先返回非空的探测结果，就以该结果为准”的原则返回探测到的
     * 字符集编码。使用需要用到三个第三方JAR包：antlr.jar、chardet.jar和cpdetector.jar
     * cpDetector是基于统计学原理的，不保证完全正确。
     */
    private static final CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();

    static {
        /*
         * ParsingDetector可用于检查HTML、XML等文件或字符流的编码,构造方法中的参数用于
         * 指示是否显示探测过程的详细信息，为false不显示。
         */
        detector.add(new ParsingDetector(false));
        /*
         * JChardetFacade封装了由Mozilla组织提供的JChardet，它可以完成大多数文件的编码
         * 测定。所以，一般有了这个探测器就可满足大多数项目的要求，如果你还不放心，可以
         * 再多加几个探测器，比如下面的ASCIIDetector、UnicodeDetector等。
         */
        detector.add(UnicodeDetector.getInstance());
        detector.add(ASCIIDetector.getInstance());
        detector.add(new ByteOrderMarkDetector());
        detector.add(JChardetFacade.getInstance());// 用到antlr.jar、chardet.jar
    }

    public static File[] listFiles(String path) {
        return listFiles(new File(path));
    }

    public static File[] listFiles(File file) {
        File[] files = file.listFiles();
        return files != null ? files : new File[0];
    }

    public static String[] list(File file) {
        String[] files = file.list();
        return files != null ? files : new String[0];
    }

    public static long getSize(File file) {
        if (file == null) {
            return 0L;
        } else {
            return getSize(file, 0L);
        }
    }

    private static long getSize(File file, long sum) {
        if (file.isFile()) {
            return sum + file.length();
        } else {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File f : files) {
                    sum = getSize(f, sum);
                }
            }
            return sum;
        }
    }

    public static String guessFileEncode(File file) throws IOException {
        Charset charset = detector.detectCodepage(file.toURI().toURL());
        if (charset != null) {
            return charset.name();
        } else {
            return null;
        }
    }

    public static String formatSize(long size) {
        if (size < ONE_KB) {
            return size + "B";
        } else if (size < ONE_MB) {
            return removeLastZero(String.format("%.2f", (double) size / ONE_KB)) + "KB";
        } else if (size < ONE_GB) {
            return removeLastZero(String.format("%.2f", (double) size / ONE_MB)) + "MB";
        } else if (size < ONE_TB) {
            return removeLastZero(String.format("%.2f", (double) size / ONE_GB)) + "GB";
        } else if (size < ONE_PB) {
            return removeLastZero(String.format("%.2f", (double) size / ONE_TB)) + "TB";
        } else {
            return removeLastZero(String.format("%.2f", (double) size / ONE_PB)) + "PB";
        }
    }

    public static String removeLastZero(String num) {
        int index = num.indexOf('.');
        if (index < 0) {
            return num;
        }
        int lastIndex = num.length();
        for (int i = num.length() - 1; i >= index; lastIndex = i, --i) {
            if (num.charAt(i) != '0' && num.charAt(i) != '.') {
                break;
            }
        }
        return num.substring(0, lastIndex);
    }

}
