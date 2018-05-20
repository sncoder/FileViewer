package cn.sncoder.fv.interceptor;

import cn.sncoder.fv.bean.User;
import cn.sncoder.fv.constant.SessionKeys;
import cn.sncoder.fv.entity.Msg;
import cn.sncoder.fv.util.ServletUtil;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        User user = (User) request.getSession().getAttribute(SessionKeys.LOGIN_USER);
        if (user == null) {
            if (ServletUtil.isAjaxRequest(request)) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().print(new Msg(false, "您的登录时效已过，请重新登录"));
            } else {
                response.sendRedirect(ServletUtil.getBasePath(request) + "/user/login");
            }
            return false;
        }
        return true;
    }
}
