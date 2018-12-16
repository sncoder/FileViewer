package cn.sncoder.fv.bean;

import cn.sncoder.fv.util.RandomUtil;

import java.util.Objects;

/**
 * 手机端登录验证对象
 *
 * @author shibiao
 */
public class Token {

    /**
     * token 值
     */
    private final String token;
    private long lastAccessTime;
    private String username;

    public Token() {
        token = RandomUtil.randomUpperCasesOrDigits(32);
        lastAccessTime = System.currentTimeMillis();
    }

    public Token(String username) {
        this();
        this.username = username;
    }

    public Token(String token, long lastAccessTime) {
        this.token = token;
        this.lastAccessTime = lastAccessTime;
    }

    public String getToken() {
        return token;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token1 = (Token) o;
        return Objects.equals(token, token1.token);
    }

    @Override
    public int hashCode() {

        return Objects.hash(token);
    }
}
