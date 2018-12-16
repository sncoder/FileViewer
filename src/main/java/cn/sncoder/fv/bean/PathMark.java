package cn.sncoder.fv.bean;

import java.util.Objects;

/**
 * 此对象代表用户的一个书签，保存在user.db的pathMarks中
 *
 * @author shibiao
 */
public class PathMark {

    /**
     * 书签名
     */
    private String name;

    /**
     * 相对路径
     */
    private String path;

    public PathMark() {}

    public PathMark(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathMark pathMark = (PathMark) o;
        return Objects.equals(path, pathMark.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
