package cn.sncoder.fv.service;

import cn.sncoder.fv.bean.DiskInfo;
import cn.sncoder.fv.bean.FileBean;
import cn.sncoder.fv.bean.FileKey;
import cn.sncoder.fv.bean.User;
import cn.sncoder.fv.db.FileKeyDB;
import cn.sncoder.fv.entity.Pager;
import cn.sncoder.fv.util.CompressUtil;
import cn.sncoder.fv.util.FileSorter;
import cn.sncoder.fv.util.FileUtil;
import cn.sncoder.fv.util.RandomUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * 用于操作文件的Service，{@link User#getRootPath()}和一个相对路径就能完全确定一个唯一的文件
 *
 * @author shibiao
 */
@Service
public class FileService {

    /**
     * 用char表示的目录分隔符
     */
    public static final char FILE_SEPARATOR_CHAR = '/';

    /**
     * 用String表示的目录分隔符
     */
    public static final String FILE_SEPARATOR = "/";

    /**
     * 每个文件都会有一个唯一的key，这代表了key的长度
     */
    public final int keyLength = FileKey.KEY_LENGTH;

    /**
     * 在线编辑文件的最大文件限制：500KB
     */
    private final int maxReadFileSize = 1024 * 500;

    /**
     * 文件每页显示的数量
     */
    public final int pageSize = 50;

    @Resource
    private FileKeyDB fileKeyDB;

    /**
     * 将{@link File}转换成{@link FileBean}
     *
     * @param file {@link File}
     * @param user 该文件所属的用户对象
     * @param parentPath 该文件所在的父目录
     * @return 返回对应的 {@link FileBean} 对象
     */
    private FileBean createFileBean(File file, User user, String parentPath) {
        //父目录必须是一个路径，要求以"/"结尾
        if (parentPath != null && !parentPath.endsWith(FILE_SEPARATOR)) {
            parentPath += FILE_SEPARATOR_CHAR;
        }

        //简单的创建Bean对象，并设置对应的属性值
        FileBean fileBean = new FileBean();
        fileBean.setUser(user.getUsername());
        fileBean.setDir(file.isDirectory());
        fileBean.setName(file.getName());
        fileBean.setLastModified(file.lastModified());
        fileBean.setLength(file.length());
        fileBean.setParentPath(parentPath);
        fileBean.setPath((parentPath == null ? "" : parentPath) + fileBean.getName() + (fileBean.isDir() ? FILE_SEPARATOR : ""));
        return fileBean;
    }

    /**
     * 根据{@link User#getRootPath()}和相对路径获取{@link FileBean}对象
     *
     * @param user 用户对象
     * @param path 相对路径
     * @return 返回对应的 {@link FileBean}对象
     */
    public FileBean getFileBean(User user, String path) {
        if (!path.endsWith(FILE_SEPARATOR)) {
            path += FILE_SEPARATOR_CHAR;
        }
        String parentPath = getParentPath(path);
        File file = getFile(user, path);
        return createFileBean(file, user, parentPath);
    }

    private FileBean[] getFileBeans(File[] files, User user, String path) {
        if (ArrayUtils.isEmpty(files)) { return new FileBean[0]; }

        int length = files.length;
        FileBean[] fileBeans = new FileBean[length];
        for (int i = 0; i < length; ++i) {
            fileBeans[i] = createFileBean(files[i], user, path);
        }
        return fileBeans;
    }

    private String getParentPath(String path) {
        int index = path.lastIndexOf(FILE_SEPARATOR_CHAR);
        if (index < 0) {
            return null;
        }
        index = path.lastIndexOf(FILE_SEPARATOR_CHAR, index - 1);
        if (index >= 0) {
            return path.substring(0, index + 1);
        } else {
            return null;
        }
    }

    /**
     * 检测路径是否合法，如果合法，不会有任何事情发生，否则直接抛出{@link RuntimeException}
     *
     * @param path 路径
     */
    private void checkPath(String path) {
        //用户不能访问自己根目录的上一层目录，所以路径中包含".."是不允许的
        if (path.contains("../") || path.contains("/..")) {
            throw new RuntimeException("路径不合法");
        }
    }

    /**
     * 根据{@link User#getRootPath()}和相对路径获取实际的{@link File}对象
     *
     * @param user 用户对象
     * @param path 相对路径
     * @return 返回实际文件的 {@link File}对象，该方法一定不会返回{@code null}，但是不保证该文件一定存在
     */
    public File getFile(User user, String path) {
        checkPath(path);
        return new File(user.getRootPath() + path);
    }

    /**
     * 如果前端传过来的相对路径不合法，则通过这个方法来进行转换。
     * 不合法的情况为目录没有以"/"开头或结尾。
     * 注意，此方法只用来转换文件夹的路径，不要用来转换文件的路径。
     *
     * @param path 文件夹的相对路径
     * @return 返回转换后的相对路径，如果路径合法，则返回原对象。
     */
    public String convertPath(String path) {
        if (StringUtils.isEmpty(path)) { return FILE_SEPARATOR; }

        path = path.endsWith(FILE_SEPARATOR) ? path : path + FILE_SEPARATOR_CHAR;
        return path.startsWith(FILE_SEPARATOR) ? path : FILE_SEPARATOR_CHAR + path;
    }

    /**
     * 根据页数列出用户一个目录下的所有文件（夹），会自动对文件进行排序，排序规则详见{@link FileSorter#sortFile(File[])}
     *
     * @param user 用户对象
     * @param path 相对路径
     * @param page 页数
     * @return 返回一个 {@link Pager<FileBean[]>} 对象，包含了分页的信息和已经排序的 {@link FileBean}数组
     */
    public Pager<FileBean[]> listFiles(User user, String path, int page) {
        File file = getFile(user, path);
        if (!file.exists()) { return new Pager<>(pageSize, 1, 0, new FileBean[0]); }
        File[] files = file.listFiles();
        if (ArrayUtils.isEmpty(files)) { return new Pager<>(pageSize, 1, 0, new FileBean[0]); }

        FileSorter.sortFile(files);
        int length = files.length;
        int from = Math.min((page - 1) * pageSize, length);
        int to = Math.min(from + pageSize, length);
        files = Arrays.copyOfRange(files, from, to);
        FileBean[] fileBeans = getFileBeans(files, user, path);
        return new Pager<>(pageSize, page, length, fileBeans);
    }

    /**
     * 获取用户根目录的磁盘占用信息
     *
     * @param user 用户对象
     * @return 返回 {@link DiskInfo}对象
     */
    public DiskInfo diskInfo(User user) {
        File file = getFile(user, FILE_SEPARATOR);
        long total = file.getTotalSpace();
        long usable = file.getUsableSpace();
        return new DiskInfo(total, usable, total - usable);
    }

    /**
     * 删除用户文件（夹）
     *
     * @param user 用户对象
     * @param path 相对路径
     * @return 删除成功返回 {@code true}，否则返回 {@link false}
     */
    public boolean del(User user, String path) {
        File file = getFile(user, path);
        // 删除的同时也删除文件的key
        fileKeyDB.removeByAbsPath(file.getAbsolutePath());
        return FileUtils.deleteQuietly(file);
    }

    /**
     * 创建一个文件，未发生IO异常则表示创建成功
     *
     * @param user 用户对象
     * @param path 相对路径
     * @throws IOException 创建过程中可能会发生IO异常
     */
    public void touch(User user, String path) throws IOException {
        File file = getFile(user, path);
        FileUtils.touch(file);
    }

    /**
     * 创建一个文件夹，未发生IO异常则表示创建成功
     *
     * @param user 用户对象
     * @param path 相对路径
     * @throws IOException 创建过程中可能会发生IO异常
     */
    public void mkdir(User user, String path) throws IOException {
        File file = getFile(user, path);
        FileUtils.forceMkdir(file);
    }

    /**
     * 判断长度为length的文件是否存在，如果是一个文件夹，则不判断length
     *
     * @param user 用户对象
     * @param path 相对路径
     * @param length 文件长度，单位：字节
     * @return 如果存在，返回 {@link true}，否则返回 {@link false}
     */
    public boolean exist(User user, String path, long length) {
        File file = getFile(user, path);
        return file.isDirectory() || (file.isFile() && file.length() == length);
    }

    /**
     * 复制文件（夹）至目标路径，如果源路径与目标路径相同，则直接结束方法
     *
     * @param user 用户对象
     * @param srcPath 源路径
     * @param destPath 目标路径，包含了要复制的文件（夹）名的路径
     * @throws IOException 若源路径不存在或目标路径不存在，亦或者复制过程中发生IO异常，则抛出此异常
     */
    public void copy(User user, String srcPath, String destPath) throws IOException {
        if (srcPath.equals(destPath)) { return; }

        File srcFile = getFile(user, srcPath);
        if (!srcFile.exists()) { throw new IOException("源路径不存在"); }

        File destFile = getFile(user, destPath);
        if (destFile.exists()) { throw new IOException("目标路径已存在"); }

        if (srcFile.isFile()) {
            FileUtils.copyFile(srcFile, destFile);
        } else {
            FileUtils.copyDirectory(srcFile, destFile);
        }
    }

    /**
     * 剪切文件（夹）至目标路径，如果源路径与目标路径相同，则直接结束方法
     *
     * @param user 用户对象
     * @param srcPath 源路径
     * @param destPath 目标路径，包含了要剪切的文件（夹）名的路径
     * @throws IOException 若源路径不存在或目标路径不存在，亦或者剪切过程中发生IO异常，则抛出此异常
     */
    public void cut(User user, String srcPath, String destPath) throws IOException {
        if (srcPath.equals(destPath)) { return; }

        File srcFile = getFile(user, srcPath);
        if (!srcFile.exists()) { throw new IOException("源路径不存在"); }

        File destFile = getFile(user, destPath);
        if (destFile.exists()) { throw new IOException("目标路径已存在"); }

        if (srcFile.isFile()) {
            FileUtils.moveFile(srcFile, destFile);
            // 剪切文件的同时也更新文件key对应的绝对路径
            fileKeyDB.updateAbsPath(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
        } else {
            FileUtils.moveDirectory(srcFile, destFile);
        }
    }

    /**
     * 重命名文件（夹）
     *
     * @param user 用户对象
     * @param oldPath 旧路径
     * @param newPath 新路径
     * @return 如果重命名成功，返回{@link true}，否则返回{@link false}
     * @throws IOException 如果旧路径不存在或新路径已存在，亦或者重命名过程中发生IO异常，则抛出此异常
     */
    public boolean rename(User user, String oldPath, String newPath) throws IOException {
        if (oldPath.equals(newPath)) { return true; }

        File oldFile = getFile(user, oldPath);
        if (!oldFile.exists()) { throw new IOException("源路径不存在"); }

        File newFile = getFile(user, newPath);
        if (newFile.exists()) { throw new IOException("此名称已存在"); }

        return oldFile.renameTo(newFile);
    }

    /**
     * 自动使用压缩算法压缩若干个文件（夹），该方法要求文件（夹）都处于同一个目录下，压缩后的文件也会创建在该目录下。
     * 压缩策略详见{@link CompressUtil#compress(File, File...)}
     *
     * @param user 用户对象
     * @param path 要压缩的所有文件（夹）的父目录
     * @param names 要压缩的所有文件（夹）的名称
     * @param zipName 压缩后的文件名
     * @throws IOException 压缩过程中可能会发生异常
     */
    public void compress(User user, String path, String[] names, String zipName) throws IOException {
        File destFile = getFile(user, path + zipName);
        if (destFile.exists()) { throw new IOException("此名称已存在"); }

        int length = names.length;
        File[] srcFiles = new File[names.length];
        for (int i = 0; i < length; ++i) {
            srcFiles[i] = getFile(user, path + names[i]);
        }
        CompressUtil.compress(destFile, srcFiles);
    }

    /**
     * 解压缩文件至压缩包所在的目录
     *
     * @param user 用户对象
     * @param path 压缩文件的相对路径
     * @throws IOException 解压缩过程中可能会发生异常
     */
    public void decompress(User user, String path) throws IOException {
        File srcFile = getFile(user, path);
        if (!srcFile.exists()) { throw new IOException("压缩文件不存在"); }

        CompressUtil.unCompress(srcFile, srcFile.getParentFile());
    }

    /**
     * 获取一个文件对应的key值，如果该文件还未生成key值，则生成key之后返回，否则直接返回key值
     * 关于key值的解释，详见{@link FileKey}
     *
     * @param user 用户对象
     * @param path 文件相对路径
     * @return 返回文件对应的key值，如果文件不存在或是一个目录，则返回{@link null}
     */
    public String getKey(User user, String path) {
        File file = getFile(user, path);
        if (!file.isFile()) { return null; }

        String absPath = file.getAbsolutePath();
        FileKey fileKey = fileKeyDB.byAbsPath(absPath);
        if (fileKey != null) { return fileKey.getKey(); }

        String key = RandomUtil.randomSequence(keyLength);
        fileKey = new FileKey(key, absPath);
        fileKeyDB.save(fileKey);
        return key;
    }

    /**
     * 根据文件对应的key值获取该文件的绝对路径
     * 关于key值的解释，详见{@link FileKey}
     *
     * @param key key值
     * @return 返回绝对路径，如果该key值不存在，则返回{@link null}
     */
    public String getAbsPath(String key) {
        return fileKeyDB.byKey(key).getAbsPath();
    }

    /**
     * 获取一个文件的文本内容，该文件最大不能超过{@link #maxReadFileSize}字节，否则会抛出异常。
     * 方法内部会自动判断文件编码，但并不是很完善，判断文件编码详见{@link FileUtil#guessFileEncode(File)}
     *
     * @param user 用户对象
     * @param path 文件相对路径
     * @return 返回文件的文本内容
     * @throws IOException 可能会发送IO异常
     */
    public String getContent(User user, String path) throws IOException {
        File file = getFile(user, path);
        if (!file.isFile()) { throw new IOException("文件不存在"); }
        if (file.length() > maxReadFileSize) { throw new IOException("文件大小不能超过500KB"); }

        String guessEncoding = FileUtil.guessFileEncode(file);
        String encoding;
        if (!StringUtils.isEmpty(guessEncoding)) {
            String temp = guessEncoding.toLowerCase();
            if (temp.startsWith("gb")) {
                encoding = "GBK";
            } else {
                encoding = "UTF-8";
            }
        } else {
            encoding = "UTF-8";
        }
        return FileUtils.readFileToString(file, encoding);
    }

    /**
     * 保存文本内容至文件中，直接会覆盖原来的内容
     *
     * @param user 用户对象
     * @param path 相对路径
     * @param content 要保存的文本内容
     * @throws IOException 保存过程中可能会发生异常
     */
    public void saveContent(User user, String path, String content) throws IOException {
        File file = getFile(user, path);
        if (!file.isFile()) { throw new IOException("文件不存在"); }

        FileUtils.writeStringToFile(file, content);
    }

}
