package cn.sncoder.fv.bean;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Objects;

/**
 * 代表一个用户
 *
 * @author shibiao
 */
public class User {

    /**
     * 用户名
     */
    @NotEmpty(message = "用户名不能为空")
    private String username;

    /**
     * 密码，使用明文存储
     */
    @NotEmpty(message = "密码不能为空")
    private String password;

    /**
     * 用户根目录，此为磁盘的真实目录
     */
    @NotEmpty(message = "根目录不能为空")
    private String rootPath;

    /**
     * 用户创建的书签
     */
    private List<PathMark> pathMarks;

    /**
     * 用户角色，0：可创建新用户，1：不可创建新用户
     */
    private Integer role;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public List<PathMark> getPathMarks() {
        return pathMarks;
    }

    public void setPathMarks(List<PathMark> pathMarks) {
        this.pathMarks = pathMarks;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {

        return Objects.hash(username);
    }
}
