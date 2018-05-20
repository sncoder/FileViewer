package cn.sncoder.fv.util;

import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZMethod;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.*;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;

/**
 * 压缩与解压缩的工具类，使用了commons-compress和tukaani.xz，目前来说有很多不足，以下是测试数据
 *
 * 测试压缩或打包：43.5 MB、25个文件、2个文件夹
 *  zip :           1790 ms     通过测试
 *  ar :            99 ms       通过测试
 *  7z :            17035 ms    通过测试，速度慢，不推荐使用
 *  tar :           116 ms      通过测试，打包后中文名乱码
 *  jar :           1607 ms     通过测试
 *  cpio :          95 ms       通过测试
 *  lzma :          19397 ms    通过测试，速度慢，不推荐使用
 *  xz :            18816 ms    通过测试，速度慢，不推荐使用
 *  snappy-framed : 2181 ms     通过测试
 *  lz4-block :     112458 ms   通过测试，速度极慢，强烈不推荐使用
 *  gz :            1709 ms     通过测试，速度慢，不推荐使用
 *  deflate :       1735 ms     通过测试，速度慢，不推荐使用
 *  bzip2 :         7421 ms     通过测试，速度慢，不推荐使用
 *  pack200 :       519 ms      通过测试
 *  lz4-framed :    89546 ms    通过测试，速度极慢，强烈不推荐使用
 *
 *  <b>推荐使用zip格式进行压缩，tar打包目前会导致中文乱码，不推荐使用其余的方式进行压缩或打包<b/>
 *
 * 测试解压：用上面的测试数据进行解压
 *  zip :           301 ms      通过测试
 *  ar :            失败        中文乱码，无法解压
 *  7z :            1174 ms     通过测试
 *  tar :           160 ms      通过测试
 *  arj :           未测试
 *  jar :           308 ms      通过测试
 *  dump :          未测试
 *  cpio :          失败        Unknown magic [      ]. Occured at byte: 31381
 *  snappy-raw :    未测试
 *  xz :            952 ms      通过测试
 *  snappy-framed : 342 ms      通过测试
 *  bzip2 :         4554 ms     通过测试，速度较慢，不推荐使用
 *  lz4-framed :    1072 ms     通过测试
 *  br :            未测试
 *  lzma :          4859 ms     通过测试，速度较慢，不推荐使用
 *  lz4-block :     205 ms      通过测试
 *  gz :            150 ms      通过测试
 *  deflate :       212 ms      通过测试
 *  z :             未测试
 *  pack200 :       9 ms        未通过测试，解压后的数据错误
 *
 */
public class CompressUtil {

    private static ArchiveStreamFactory archiveStreamFactory = new ArchiveStreamFactory();
    /**
     * [zip, ar, 7z, tar, arj, jar, dump, cpio]
     */
    private static Set<String> supportedInputArchiverNames = archiveStreamFactory.getInputStreamArchiveNames();
    /**
     * [zip, ar, 7z, tar, jar, cpio]
     */
    private static Set<String> supportedOutputArchiverNames = archiveStreamFactory.getOutputStreamArchiveNames();

    private static CompressorStreamFactory compressorStreamFactory = new CompressorStreamFactory();
    /**
     * [snappy-raw, xz, snappy-framed, bzip2, lz4-framed, br, lzma, lz4-block, gz, deflate, z, pack200]
     */
    private static Set<String> supportedInputCompressorNames = compressorStreamFactory.getInputStreamCompressorNames();
    /**
     * [lzma, xz, snappy-framed, lz4-block, gz, deflate, bzip2, pack200, lz4-framed]
     */
    private static Set<String> supportedOutputCompressorNames = compressorStreamFactory.getOutputStreamCompressorNames();

    /**
     * 压缩多个文件或文件夹
     *
     * 如果是compressor格式，则首先会将文件进行tar打包，然后再进行压缩
     *
     * @param targetFile 压缩后的目标文件
     * @param srcFiles 需要压缩的源文件
     * @throws IOException 如果不支持此压缩格式或发生IO错误，则抛出异常
     */
    public static void compress(File targetFile, File ... srcFiles) throws IOException {
        if (targetFile.exists()) {
            throw new IOException("目标文件已存在");
        } else if (srcFiles == null || srcFiles.length == 0) {
            throw new IOException("源文件为空");
        }
        String archiverName = getArchiverName(targetFile, false);
        if (archiverName != null) {
            try {
                archiverCompress(archiverName, targetFile, srcFiles);
            } catch (ArchiveException e) {
                throw new IOException("不支持的压缩类型");
            }
            return;
        }
        String compressorName = getCompressorName(targetFile, false);
        if (compressorName != null) {
            try {
                compressorCompress(compressorName, targetFile, srcFiles);
            } catch (CompressorException e) {
                throw new IOException("不支持的压缩类型");
            }
            return;
        }
        throw new IOException("不支持的压缩类型");
    }

    /**
     * 解压文件
     *
     * 如果是compressor格式，解压完成后如果是一个archiver格式，会继续进行解压操作
     *
     * @param srcFile 压缩文件
     * @param targetDirFile 解压的路径
     * @throws IOException 如果不支持此压缩格式或发生IO错误，则抛出异常
     */
    public static void unCompress(File srcFile, File targetDirFile) throws IOException {
        if (!srcFile.exists() || srcFile.isDirectory()) {
            throw new IOException("源文件不存在或者不是文件");
        } else if (targetDirFile == null) {
            throw new NullPointerException();
        } else if (targetDirFile.exists() && !targetDirFile.isDirectory()) {
            throw new IOException("目标文件不是文件夹");
        }
        FileUtils.forceMkdir(targetDirFile);
        BufferedInputStream in = null;

        try {
            in = new BufferedInputStream(new FileInputStream(srcFile));
            ArchiveInputStream archiveIn = createArchiveInputStream(in);
            archiverUnCompress(archiveIn, targetDirFile);
            return;
        } catch (ArchiveException ignore) {
            String archiverName = getArchiverName(srcFile, true);
            if (!StringUtils.isEmpty(archiverName)) {
                if (ArchiveStreamFactory.SEVEN_Z.equalsIgnoreCase(archiverName)) {
                    sevenZUnCompress(srcFile, targetDirFile);
                    return;
                }
                try {
                    ArchiveInputStream archiveIn = createArchiveInputStream(archiverName, in);
                    archiverUnCompress(archiveIn, targetDirFile);
                } catch (ArchiveException e) {
                    throw new IOException("不支持的解压缩类型");
                } finally {
                    IOUtils.closeQuietly(in);
                }
                return;
            }
        } finally {
            IOUtils.closeQuietly(in);
        }

        try {
            in = new BufferedInputStream(new FileInputStream(srcFile));
            CompressorInputStream compressorIn = createCompressorInputStream(in);
            compressorUnCompress(compressorIn, srcFile, targetDirFile);
        } catch (CompressorException ignore) {
            String compressorName = getCompressorName(srcFile, true);
            if (!StringUtils.isEmpty(compressorName)) {
                try {
                    CompressorInputStream compressorIn = createCompressorInputStream(compressorName, in);
                    compressorUnCompress(compressorIn, srcFile, targetDirFile);
                } catch (CompressorException ignore2) {} finally {
                    IOUtils.closeQuietly(in);
                }
                return;
            }
            throw new IOException("不支持的解压缩类型");
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * 多线程压缩zip文件
     */
    private static void parallelZipCompress(File targetFile, File ... srcFiles) throws IOException {
        ParallelScatterZipCreator scatterZipCreator = new ParallelScatterZipCreator();
        long now = System.currentTimeMillis();
        File tempFile = File.createTempFile(String.valueOf(now), ".tmp");
        ScatterZipOutputStream dirs = ScatterZipOutputStream.fileBased(tempFile);
        ZipArchiveOutputStream zipOut = null;
        try {
            zipOut = new ZipArchiveOutputStream(targetFile);
            for (File file : srcFiles) {
                parallelZipCompress(scatterZipCreator, dirs, targetFile, file, "");
            }
            dirs.writeTo(zipOut);
            dirs.close();
            scatterZipCreator.writeTo(zipOut);
        } catch (IOException | InterruptedException | ExecutionException e) {
            IOUtils.closeQuietly(zipOut);
            IOUtils.closeQuietly(dirs);
            FileUtils.deleteQuietly(targetFile);
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(zipOut);
            IOUtils.closeQuietly(dirs);
            FileUtils.deleteQuietly(tempFile);
        }
    }

    private static void parallelZipCompress(ParallelScatterZipCreator scatterZipCreator,
                                            ScatterZipOutputStream dirs,
                                            File targetFile,
                                            File file,
                                            String basePath) throws IOException {
        if (file.isFile()) {
            if (targetFile.equals(file)) {
                return;
            }
            ZipArchiveEntry entry = new ZipArchiveEntry(basePath + file.getName());
            entry.setMethod(ZipEntry.DEFLATED);
            scatterZipCreator.addArchiveEntry(entry, () -> {
                try {
                    return new BufferedInputStream(new FileInputStream(file));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return EmptyInputStream.INSTANCE;
            });
        } else {
            basePath += file.getName() + File.separator;
            ZipArchiveEntry entry = new ZipArchiveEntry(basePath);
            entry.setMethod(ZipEntry.DEFLATED);
            dirs.addArchiveEntry(ZipArchiveEntryRequest.createZipArchiveEntryRequest(entry, () -> EmptyInputStream.INSTANCE));
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File f : files) {
                    parallelZipCompress(scatterZipCreator, dirs, targetFile, f, basePath);
                }
            }
        }
    }

    /**
     * 根据文件数量来确定是否需要使用多线程压缩zip
     * 这里设定了只要有三个或三个以上的文件就返回true
     * 否则返回false
     */
    private static boolean checkUsingParallelZip(File ... srcFiles) {
        int maxCount = 3;
        if (srcFiles.length == 0) {
            return false;
        }
        Queue<File> queue = new LinkedList<>();
        queue.addAll(Arrays.asList(srcFiles));
        int count = 0;
        while (!queue.isEmpty() && count < maxCount) {
            File file = queue.poll();
            if (file.isFile()) {
                ++count;
            } else {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    int temp = 0;
                    for (File f : files) {
                        if (f.isFile()) {
                            ++count;
                            ++temp;
                            if (count >= maxCount) {
                                return true;
                            }
                        }
                        queue.add(f);
                    }
                    count -= temp;
                }
            }
        }
        return count >= maxCount;
    }

    private static void sevenZCompress(File targetFile, File ... srcFiles) throws IOException {
        SevenZOutputFile sevenZOutFile = null;
        try {
            sevenZOutFile = new SevenZOutputFile(targetFile);
            sevenZOutFile.setContentCompression(SevenZMethod.DEFLATE);
            byte[] buf = new byte[1024 * 8];
            for (File file : srcFiles) {
                sevenZCompress(sevenZOutFile, targetFile, file, "", buf);
            }
        } catch (IOException e) {
            IOUtils.closeQuietly(sevenZOutFile);
            FileUtils.deleteQuietly(targetFile);
            throw e;
        } finally {
            IOUtils.closeQuietly(sevenZOutFile);
        }
    }

    private static void sevenZCompress(SevenZOutputFile sevenZOutFile, File targetFile, File file, String basePath, byte[] buf) throws IOException {
        if (file.isFile()) {
            if (targetFile.equals(file)) {
                return;
            }
            BufferedInputStream in = null;
            try {
                SevenZArchiveEntry entry = sevenZOutFile.createArchiveEntry(file, basePath + file.getName());
                entry.setSize(file.length());
                sevenZOutFile.putArchiveEntry(entry);
                in = new BufferedInputStream(new FileInputStream(file));
                int len;
                while ((len = in.read(buf)) != -1) {
                    sevenZOutFile.write(buf, 0, len);
                }
            } finally {
                IOUtils.closeQuietly(in);
                sevenZOutFile.closeArchiveEntry();
            }
        } else {
            basePath += file.getName() + File.separator;
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File f : files) {
                    sevenZCompress(sevenZOutFile, targetFile, f, basePath, buf);
                }
            }
        }
    }

    private static void sevenZUnCompress(File srcFile, File targetDirFile) throws IOException {
        SevenZFile sevenZFile = null;
        try {
            sevenZFile = new SevenZFile(srcFile);
            SevenZArchiveEntry entry;
            String path = targetDirFile.getAbsolutePath() + File.separator;
            byte[] buf = new byte[1024 * 8];
            while ((entry = sevenZFile.getNextEntry()) != null) {
                String name = entry.getName();
                File file = new File(path + name);
                if (entry.isDirectory()) {
                    FileUtils.forceMkdir(file);
                    continue;
                }
                FileUtils.forceMkdir(file.getParentFile());
                try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                    int len;
                    while ((len = sevenZFile.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(sevenZFile);
        }
    }

    private static void archiverCompress(String archiverName, File targetFile, File ... srcFiles) throws IOException, ArchiveException {
        if (ArchiveStreamFactory.SEVEN_Z.equalsIgnoreCase(archiverName)) {
            sevenZCompress(targetFile, srcFiles);
            return;
        } else if (ArchiveStreamFactory.ZIP.equalsIgnoreCase(archiverName) && checkUsingParallelZip(srcFiles)) {
            parallelZipCompress(targetFile, srcFiles);
            return;
        }
        BufferedOutputStream out = null;
        ArchiveOutputStream archiveOut = null;
        try {
            FileUtils.forceMkdir(targetFile.getParentFile());
            out = new BufferedOutputStream(new FileOutputStream(targetFile));
            archiveOut = createArchiveOutputStream(archiverName, out);
            for (File file : srcFiles) {
                archiverCompress(targetFile, archiveOut, file, "");
            }
        } catch (IOException | ArchiveException e) {
            IOUtils.closeQuietly(archiveOut);
            IOUtils.closeQuietly(out);
            FileUtils.deleteQuietly(targetFile);
            throw e;
        } finally {
            IOUtils.closeQuietly(archiveOut);
            IOUtils.closeQuietly(out);
        }
    }

    private static void archiverCompress(File target, ArchiveOutputStream archiveOut, File file, String basePath) throws IOException {
        if (!file.isDirectory()) {
            if (target.equals(file)) {
                return;
            }
            ArchiveEntry entry = archiveOut.createArchiveEntry(file, basePath + file.getName());
            archiveOut.putArchiveEntry(entry);
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
                IOUtils.copy(in, archiveOut);
            } finally {
                archiveOut.closeArchiveEntry();
            }
        } else {
            String fileName = file.getName();
            basePath += fileName + File.separator;
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File f : files) {
                    archiverCompress(target, archiveOut, f, basePath);
                }
            }
        }
    }

    private static void compressorCompress(String compressorName, File targetFile, File ... srcFiles) throws IOException, CompressorException {
        if (srcFiles.length == 1 && srcFiles[0].isFile()) {
            compressorCompressSingleFile(compressorName, targetFile, srcFiles[0]);
            return;
        }
        CompressorOutputStream compressOut = null;
        BufferedOutputStream out = null;
        TarArchiveOutputStream tarOut = null;
        try {
            FileUtils.forceMkdir(targetFile.getParentFile());
            out = new BufferedOutputStream(new FileOutputStream(targetFile));
            compressOut = createCompressorOutputStream(compressorName, out);
            tarOut = new TarArchiveOutputStream(compressOut);
            tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            tarOut.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
            for (File file : srcFiles) {
                archiverCompress(targetFile, tarOut, file, "");
            }
        } catch (IOException | CompressorException e) {
            IOUtils.closeQuietly(tarOut);
            IOUtils.closeQuietly(compressOut);
            IOUtils.closeQuietly(out);
            FileUtils.deleteQuietly(targetFile);
            throw e;
        } finally {
            IOUtils.closeQuietly(tarOut);
            IOUtils.closeQuietly(compressOut);
            IOUtils.closeQuietly(out);
        }
    }

    private static void compressorCompressSingleFile(String compressorName, File targetFile, File srcFile) throws IOException, CompressorException {
        CompressorOutputStream compressOut = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        try {
            FileUtils.forceMkdir(targetFile.getParentFile());
            out = new BufferedOutputStream(new FileOutputStream(targetFile));
            compressOut = createCompressorOutputStream(compressorName, out);
            in = new BufferedInputStream(new FileInputStream(srcFile));
            IOUtils.copy(in, out);
        } catch (IOException | CompressorException e) {
            IOUtils.closeQuietly(compressOut);
            IOUtils.closeQuietly(out);
            FileUtils.deleteQuietly(targetFile);
            throw e;
        } finally {
            IOUtils.closeQuietly(compressOut);
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
    }

    private static void archiverUnCompress(ArchiveInputStream archiveIn, File targetDirFile) throws IOException {
        try {
            String path = targetDirFile.getAbsolutePath() + File.separator;
            ArchiveEntry entry;
            while ((entry = archiveIn.getNextEntry()) != null) {
                String name = entry.getName();
                File file = new File(path + name);
                if (entry.isDirectory()) {
                    FileUtils.forceMkdir(file);
                    continue;
                }
                FileUtils.forceMkdir(file.getParentFile());
                try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                    IOUtils.copy(archiveIn, out);
                }
            }
        } finally {
            IOUtils.closeQuietly(archiveIn);
        }
    }

    private static void compressorUnCompress(CompressorInputStream compressorIn, File srcFile, File targetDirFile) throws IOException {
        String path = targetDirFile.getAbsolutePath() + File.separator;
        String newFileName = FilenameUtils.removeExtension(srcFile.getName());
        File targetFile = new File(path + newFileName);
        BufferedOutputStream out = null;
        try {
            FileUtils.forceMkdir(targetDirFile);
            out = new BufferedOutputStream(new FileOutputStream(targetFile));
            IOUtils.copy(compressorIn, out);
        } catch (IOException e) {
            IOUtils.closeQuietly(out);
            FileUtils.deleteQuietly(targetFile);
            throw e;
        } finally {
            IOUtils.closeQuietly(compressorIn);
            IOUtils.closeQuietly(out);
        }
        BufferedInputStream in = null;
        ArchiveInputStream archiveIn;
        try {
            in = new BufferedInputStream(new FileInputStream(targetFile));
            archiveIn = createArchiveInputStream(in);
            archiverUnCompress(archiveIn, targetDirFile);
            IOUtils.closeQuietly(archiveIn);
            IOUtils.closeQuietly(in);
            FileUtils.deleteQuietly(targetFile);
        } catch (ArchiveException ignore) {
            IOUtils.closeQuietly(in);
        }
    }

    private static ArchiveOutputStream createArchiveOutputStream(String archiverName, OutputStream out) throws ArchiveException {
        ArchiveOutputStream archiveOutputStream = archiveStreamFactory.createArchiveOutputStream(archiverName, out);
        if (archiveOutputStream instanceof ZipArchiveOutputStream) {
            ZipArchiveOutputStream zipOut = (ZipArchiveOutputStream) archiveOutputStream;
            zipOut.setUseZip64(Zip64Mode.AsNeeded);
        } else if (archiveOutputStream instanceof TarArchiveOutputStream) {
            TarArchiveOutputStream tarOut = (TarArchiveOutputStream) archiveOutputStream;
            tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            tarOut.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        } else if (archiveOutputStream instanceof ArArchiveOutputStream) {
            ArArchiveOutputStream arOut = (ArArchiveOutputStream) archiveOutputStream;
            arOut.setLongFileMode(ArArchiveOutputStream.LONGFILE_BSD);
        }
        return archiveOutputStream;
    }

    private static ArchiveInputStream createArchiveInputStream(InputStream in) throws ArchiveException {
        return archiveStreamFactory.createArchiveInputStream(in);
    }

    private static ArchiveInputStream createArchiveInputStream(String archiverName, InputStream in) throws ArchiveException {
        return archiveStreamFactory.createArchiveInputStream(archiverName, in);
    }

    private static CompressorOutputStream createCompressorOutputStream(String compressorName, OutputStream out) throws CompressorException {
        return compressorStreamFactory.createCompressorOutputStream(compressorName, out);
    }

    private static CompressorInputStream createCompressorInputStream(InputStream in) throws CompressorException {
        return compressorStreamFactory.createCompressorInputStream(in);
    }

    private static CompressorInputStream createCompressorInputStream(String compressorName, InputStream in) throws CompressorException {
        return compressorStreamFactory.createCompressorInputStream(compressorName, in);
    }

    private static String getArchiverName(File file, boolean isInput) {
        String suffix = getSuffix(file);
        if (StringUtils.isEmpty(suffix)) {
            return null;
        }
        if (isInput) {
            return containsIgnoreCase(supportedInputArchiverNames, suffix);
        } else {
            return containsIgnoreCase(supportedOutputArchiverNames, suffix);
        }
    }

    private static String getCompressorName(File file, boolean isInput) {
        String suffix = getSuffix(file);
        if (StringUtils.isEmpty(suffix)) {
            return null;
        }
        if (isInput) {
            return containsIgnoreCase(supportedInputCompressorNames, suffix);
        } else {
            return containsIgnoreCase(supportedOutputCompressorNames, suffix);
        }
    }

    private static String containsIgnoreCase(Collection<String> collection, String str) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        for (String string : collection) {
            if (string.equalsIgnoreCase(str)) {
                return string;
            }
        }
        return null;
    }

    private static String getSuffix(File file) {
        return FilenameUtils.getExtension(file.getName());
    }

    public static Set<String> getSupportedInputArchiverNames() {
        return new HashSet<>(supportedInputArchiverNames);
    }

    public static Set<String> getSupportedOutputArchiverNames() {
        return new HashSet<>(supportedOutputArchiverNames);
    }

    public static Set<String> getSupportedInputCompressorNames() {
        return new HashSet<>(supportedInputCompressorNames);
    }

    public static Set<String> getSupportedOutputCompressorNames() {
        return new HashSet<>(supportedOutputCompressorNames);
    }

    public static boolean isSupportedCompress(String name) {
        String suffix = FilenameUtils.getExtension(name);
        return StringUtils.isEmpty(containsIgnoreCase(supportedOutputArchiverNames, suffix))
                || StringUtils.isEmpty(containsIgnoreCase(supportedOutputCompressorNames, suffix));
    }

    public static boolean isSupportedUnCompress(String name) {
        String suffix = FilenameUtils.getExtension(name);
        return StringUtils.isEmpty(containsIgnoreCase(supportedInputArchiverNames, suffix))
                || StringUtils.isEmpty(containsIgnoreCase(supportedInputCompressorNames, suffix));
    }

    private static class EmptyInputStream extends InputStream {

        private static InputStream INSTANCE = new EmptyInputStream();

        @Override
        public int read() throws IOException {
            return -1;
        }
    }

}
