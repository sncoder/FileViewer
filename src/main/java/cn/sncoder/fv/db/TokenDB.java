package cn.sncoder.fv.db;

import cn.sncoder.fv.bean.Token;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@Repository
public class TokenDB extends BaseDB<Token> {

    @Override
    protected String getDBPath() {
        return "/db/token.json";
    }

    /**
     * 检测 Token 值是否存在
     */
    public boolean containsToken(String tokenValue) {
        return objs.contains(new Token(tokenValue));
    }

    /**
     * 通过判断 Token 值来检测 Token 是否存在
     */
    public boolean containsToken(Token token) {
        return objs.contains(token);
    }

    /**
     * 添加一个 Token
     */
    public boolean addToken(Token token) {
        if (token == null || containsToken(token)) {
            return false;
        }
        objs.add(token);
        needUpdate();
        return true;
    }

    /**
     * 获取 Token 对象
     */
    public Token get(String tokenValue) {
        if (StringUtils.isEmpty(tokenValue)) {
            return null;
        }
        for (Token token : objs) {
            if (token.getToken().equals(tokenValue)) {
                return token;
            }
        }
        return null;
    }

    /**
     * 更新 Token
     */
    public boolean updateToken(Token token) {
        if (token == null) {
            return false;
        }
        for (Token t : objs) {
            if (t.equals(token)) {
                t.setLastAccessTime(token.getLastAccessTime());
                needUpdate();
                return true;
            }
        }
        return false;
    }

    /**
     * 获取所有 Token
     */
    public Set<Token> getAll() {
        return new HashSet<>(objs);
    }

    /**
     * 删除一个 Token
     */
    public boolean remove(String tokenValue) {
        boolean success = objs.removeIf(token -> token.getToken().equals(tokenValue));
        needUpdate(success);
        return success;
    }

}
