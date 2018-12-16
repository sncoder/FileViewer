package cn.sncoder.fv.service;

import cn.sncoder.fv.bean.Token;
import cn.sncoder.fv.db.TokenDB;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    /**
     * 三天之后 Token 过期
     */
    private static final long TOKEN_INVALID_TIME = 3600 * 1000 * 3;

    @Resource
    private ThreadPoolService threadPoolService;

    @Resource
    private TokenDB tokenDB;

    @PostConstruct
    public void init() {
        // 每十分钟检测一次 Token 是否过期
        threadPoolService.scheduleWithFixedDelay(new TokenInvalidRunnable(), 0, 10, TimeUnit.MINUTES);
    }

    /**
     * 更新 Token 上次访问时间为当前时间
     */
    public Token access(String tokenValue) {
        Token token = tokenDB.get(tokenValue);
        if (token == null) {
            return null;
        }
        token.setLastAccessTime(System.currentTimeMillis());
        return token;
    }

    /**
     * 添加 Token
     */
    public Token addToken(String username) {
        Token token = new Token(username);
        if (tokenDB.addToken(token)) {
            return token;
        }
        return null;
    }

    /**
     * 退出登录，清除 Token 信息
     */
    public boolean logout(String tokenValue) {
        return tokenDB.remove(tokenValue);
    }

    private class TokenInvalidRunnable implements Runnable {

        @Override
        public void run() {
            Set<Token> tokens = tokenDB.getAll();
            for (Token token : tokens) {
                if (System.currentTimeMillis() - token.getLastAccessTime() > TOKEN_INVALID_TIME) {
                    tokenDB.remove(token.getToken());
                }
            }
        }
    }

}
