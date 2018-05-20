package cn.sncoder.fv.bean;

import java.util.Objects;

public class FileKey {

    private String key;

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
