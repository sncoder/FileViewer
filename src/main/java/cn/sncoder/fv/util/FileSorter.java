package cn.sncoder.fv.util;

import cn.sncoder.fv.bean.FileBean;

import java.io.File;
import java.util.Arrays;

public class FileSorter {

    public static void sortFile(File[] files) {
        Arrays.sort(files, (f1, f2) -> f1.isDirectory() == f2.isDirectory() ? f1.getName().compareToIgnoreCase(f2.getName()) : f1.isDirectory() ? -1 : 1);
    }

    public static void sortFileBean(FileBean[] fileBeans) {
        Arrays.sort(fileBeans, (f1, f2) -> f1.isDir() == f2.isDir() ? f1.getName().compareToIgnoreCase(f2.getName()) : f1.isDir() ? -1 : 1);
    }

}
