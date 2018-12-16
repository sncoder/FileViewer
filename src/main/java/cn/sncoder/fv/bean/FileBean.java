package cn.sncoder.fv.bean;

/**
 * 用于前后端传输信息的类，表示一个文件（夹）
 *
 * @author shibiao
 */
public class FileBean {

    /**
     * 文件所属者的用户名，来源参考
     */
    private String user;

    private String name;

    private String path;

    private String parentPath;

    private long length;

    private long lastModified;

    private boolean isDir;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
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

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }
}
