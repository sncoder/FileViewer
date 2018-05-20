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

@Service
public class FileService {

    public final int keyLength = 16;

    public final int maxReadFileSize = 1024 * 500;

    public final int pageSize = 100;

    @Resource
    private FileKeyDB fileKeyDB;

    public FileBean createFileBean(File file, User user, String parentPath) {
        if (!parentPath.endsWith("/")) {
            parentPath += "/";
        }
        FileBean fileBean = new FileBean();
        fileBean.setUser(user.getUsername());
        fileBean.setDir(file.isDirectory());
        fileBean.setName(file.getName());
        fileBean.setLastModified(file.lastModified());
        fileBean.setLength(file.length());
        fileBean.setParentPath(parentPath);
        fileBean.setPath(parentPath + fileBean.getName() + (fileBean.isDir() ? "/" : ""));
        return fileBean;
    }

    public FileBean toFileBean(User user, String path) {
        if (!path.endsWith("/")) {
            path += "/";
        }
        String parentPath;
        int index = path.lastIndexOf('/', path.length() - 2);
        if (index >= 0) {
            parentPath = path.substring(0, index + 1);
        } else {
            parentPath = null;
        }
        File file = getFile(user, path);
        FileBean fileBean = new FileBean();
        fileBean.setUser(user.getUsername());
        fileBean.setDir(file.isDirectory());
        fileBean.setName(file.getName());
        fileBean.setLastModified(file.lastModified());
        fileBean.setLength(file.length());
        fileBean.setParentPath(parentPath);
        fileBean.setPath(path);
        return fileBean;
    }

    private FileBean[] toFileBeans(File[] files, User user, String path) {
        if (ArrayUtils.isEmpty(files)) { return new FileBean[0]; }

        int length = files.length;
        FileBean[] fileBeans = new FileBean[length];
        for (int i = 0; i < length; ++i) {
            fileBeans[i] = createFileBean(files[i], user, path);
        }
        return fileBeans;
    }

    private void checkPath(String path) {
        if (path.contains("../") || path.contains("/..")) {
            throw new RuntimeException("路径不合法");
        }
    }

    public File getFile(User user, String path) {
        checkPath(path);
        return new File(user.getRootPath() + path);
    }

    public String convertPath(String path) {
        if (StringUtils.isEmpty(path)) { return "/"; }

        path = path.endsWith("/") ? path : path + "/";
        return path.startsWith("/") ? path : "/" + path;
    }

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
        FileBean[] fileBeans = toFileBeans(files, user, path);
        return new Pager<>(pageSize, page, length, fileBeans);
    }

    public DiskInfo diskInfo(User user) {
        File file = getFile(user, "/");
        long total = file.getTotalSpace();
        long usable = file.getUsableSpace();
        return new DiskInfo(total, usable, total - usable);
    }

    public boolean del(User user, String path) {
        File file = getFile(user, path);
        fileKeyDB.removeByAbsPath(file.getAbsolutePath());
        return FileUtils.deleteQuietly(file);
    }

    public void touch(User user, String path) throws IOException {
        File file = getFile(user, path);
        FileUtils.touch(file);
    }

    public void mkdir(User user, String path) throws IOException {
        File file = getFile(user, path);
        FileUtils.forceMkdir(file);
    }

    public boolean exist(User user, String path, long length) {
        File file = getFile(user, path);
        return file.isDirectory() || (file.isFile() && file.length() == length);
    }

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

    public void cut(User user, String srcPath, String destPath) throws IOException {
        if (srcPath.equals(destPath)) { return; }

        File srcFile = getFile(user, srcPath);
        if (!srcFile.exists()) { throw new IOException("源路径不存在"); }

        File destFile = getFile(user, destPath);
        if (destFile.exists()) { throw new IOException("目标路径已存在"); }

        if (srcFile.isFile()) {
            FileUtils.moveFile(srcFile, destFile);
            fileKeyDB.updateAbsPath(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
        } else {
            FileUtils.moveDirectory(srcFile, destFile);
        }
    }

    public boolean rename(User user, String oldPath, String newPath) throws IOException {
        if (oldPath.equals(newPath)) { return true; }

        File oldFile = getFile(user, oldPath);
        if (!oldFile.exists()) { throw new IOException("源路径不存在"); }

        File newFile = getFile(user, newPath);
        if (newFile.exists()) { throw new IOException("此名称已存在"); }

        return oldFile.renameTo(newFile);
    }

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

    public void decompress(User user, String path) throws IOException {
        File srcFile = getFile(user, path);
        if (!srcFile.exists()) { throw new IOException("压缩文件不存在"); }

        CompressUtil.unCompress(srcFile, srcFile.getParentFile());
    }

    public String getKey(User user, String path) {
        File file = getFile(user, path);
        if (!file.exists()) { return null; }

        String absPath = file.getAbsolutePath();
        FileKey fileKey = fileKeyDB.byAbsPath(absPath);
        if (fileKey != null) { return fileKey.getKey(); }

        String key = RandomUtil.randomSequence(keyLength);
        fileKey = new FileKey(key, absPath);
        fileKeyDB.save(fileKey);
        return key;
    }

    public String getAbsPath(String key) {
        return fileKeyDB.byKey(key).getAbsPath();
    }

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

    public void saveContent(User user, String path, String content) throws IOException {
        File file = getFile(user, path);
        if (!file.isFile()) { throw new IOException("文件不存在"); }

        FileUtils.writeStringToFile(file, content);
    }

}
