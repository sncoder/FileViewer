package cn.sncoder.fv.bean;

import java.util.Objects;

/**
 * 每一个文件的绝对路径都会对应一个key值，key值的长度固定为{@link #KEY_LENGTH}
 * key主要用于生成下载链接，使用户无需登录即可下载此文件，并且不会暴露文件绝对路径
 *
 * @author shibiao
 */
public class FileKey {

    /**
     * key值的固定长度
     */
    public static final int KEY_LENGTH = 16;

    /**
     * key值
     */
    private String key;

    /**
     * 对应的文件绝对路径
     */
    private String absPath;

    public FileKey() {
    }

    public FileKey(String key, String absPath) {
        this.key = key;
        this.absPath = absPath;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAbsPath() {
        return absPath;
    }

    public void setAbsPath(String absPath) {
        this.absPath = absPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileKey fileKey = (FileKey) o;
        return Objects.equals(key, fileKey.key) &&
                Objects.equals(absPath, fileKey.absPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, absPath);
    }
}
