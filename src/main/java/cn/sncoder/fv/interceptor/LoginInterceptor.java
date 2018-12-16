package cn.sncoder.fv.interceptor;

import cn.sncoder.fv.bean.Token;
import cn.sncoder.fv.bean.User;
import cn.sncoder.fv.constant.Constant;
import cn.sncoder.fv.constant.SessionKeys;
import cn.sncoder.fv.entity.Msg;
import cn.sncoder.fv.service.TokenService;
import cn.sncoder.fv.service.UserService;
import cn.sncoder.fv.util.ServletUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Resource
    private TokenService tokenService;

    @Resource
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String tokenValue = request.getHeader(Constant.TOKEN_HEADER);
        User user = (User) request.getSession().getAttribute(SessionKeys.LOGIN_USER);
        Token token = null;
        if (!StringUtils.isEmpty(tokenValue)) {
            token = tokenService.access(tokenValue);
        }
        if (user != null) {
            return true;
        }

        if (StringUtils.isEmpty(tokenValue) && !ServletUtil.isAjaxRequest(request)) {
            // 如果是电脑端并且不是 Ajax 访问
            response.sendRedirect(ServletUtil.getBasePath(request) + "/user/login");
            return false;
        } else if (token != null) {
            // 如果是手机端，检查 Token
            String username = token.getUsername();
            user = userService.getByUsername(username);
            if (user != null) {
                request.getSession().setAttribute(SessionKeys.LOGIN_USER, user);
                request.getSession().setAttribute(SessionKeys.TOKEN, token.getToken());
                return true;
            }
        }
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(new Msg(false, "您的登录时效已过，请重新登录"));
        return false;
    }
}
