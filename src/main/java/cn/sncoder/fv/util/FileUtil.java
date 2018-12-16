package cn.sncoder.fv.util;

import info.monitorenter.cpdetector.io.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 封装了一些操作{@link File}的方法
 *
 * @author shibiao
 */
public class FileUtil {

    /**
     * detector是探测器，它把探测任务交给具体的探测实现类的实例完成。
     * cpDetector内置了一些常用的探测实现类，这些探测实现类的实例可以通过add方法 加进来，
     * 如ParsingDetector、JChardetFacade、ASCIIDetector、UnicodeDetector。
     * detector按照“谁最先返回非空的探测结果，就以该结果为准”的原则返回探测到的
     * 字符集编码。使用需要用到三个第三方JAR包：antlr.jar、chardet.jar和cpdetector.jar
     * cpDetector是基于统计学原理的，不保证完全正确。
     */
    private static final CodepageDetectorProxy DETECTOR = CodepageDetectorProxy.getInstance();

    static {
        /*
         * ParsingDetector可用于检查HTML、XML等文件或字符流的编码,构造方法中的参数用于
         * 指示是否显示探测过程的详细信息，为false不显示。
         */
        DETECTOR.add(new ParsingDetector(false));
        /*
         * JChardetFacade封装了由Mozilla组织提供的JChardet，它可以完成大多数文件的编码
         * 测定。所以，一般有了这个探测器就可满足大多数项目的要求，如果你还不放心，可以
         * 再多加几个探测器，比如下面的ASCIIDetector、UnicodeDetector等。
         */
        DETECTOR.add(UnicodeDetector.getInstance());
        DETECTOR.add(ASCIIDetector.getInstance());
        DETECTOR.add(new ByteOrderMarkDetector());
        // 用到antlr.jar、chardet.jar
//        DETECTOR.add(JChardetFacade.getInstance());
    }

    /**
     * 猜测一个文本文件的编码，不保证完全准确
     *
     * @param file {@link File}对象
     * @return 返回文件编码，可能会返回{@link null}
     * @throws IOException 检测过程中可能会发生异常
     */
    public static String guessFileEncode(File file) throws IOException {
        Charset charset = DETECTOR.detectCodepage(file.toURI().toURL());
        if (charset != null) {
            return charset.name();
        } else {
            return null;
        }
    }

}
