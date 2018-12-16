package cn.sncoder.fv.controller;

import cn.sncoder.fv.bean.Token;
import cn.sncoder.fv.bean.User;
import cn.sncoder.fv.constant.SessionKeys;
import cn.sncoder.fv.entity.Msg;
import cn.sncoder.fv.service.TokenService;
import cn.sncoder.fv.service.UserService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;

import static cn.sncoder.fv.constant.SessionKeys.LOGIN_USER;
import static cn.sncoder.fv.constant.SessionKeys.TOKEN;

/**
 * @author shibiao
 */
@Controller
@RequestMapping("user")
@SessionAttributes({LOGIN_USER, TOKEN})
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private TokenService tokenService;

    @GetMapping("")
    public String index(ModelMap modelMap) {
        if (modelMap.get(LOGIN_USER) != null) {
            return "redirect:/file/index";
        }
        return "/jsp/user/login.jsp";
    }

    /**
     * 跳转到登录页面
     */
    @GetMapping("login")
    public String login(ModelMap modelMap) {
        if (modelMap.get(LOGIN_USER) != null) {
            return "redirect:/file/index";
        }
        return "/jsp/user/login.jsp";
    }

    /**
     * 登录请求
     */
    @ResponseBody
    @PostMapping({"login", "login.api"})
    public Msg login(User user, ModelMap modelMap) {
        user = userService.loginCheck(user);
        if (user == null) {
            return new Msg(false, "账号或密码错误");
        }
        modelMap.addAttribute(LOGIN_USER, user);
        Token token = tokenService.addToken(user.getUsername());
        modelMap.addAttribute(TOKEN, token.getToken());
        return new Msg(true, "登录成功", token.getToken());
    }

    /**
     * 登出请求
     */
    @GetMapping("logout")
    public String logout(ModelMap modelMap, SessionStatus sessionStatus) {
        String tokenValue = (String) modelMap.get(SessionKeys.TOKEN);
        tokenService.logout(tokenValue);
        sessionStatus.setComplete();
        return "redirect:/user/login";
    }

    @ResponseBody
    @PostMapping({"logout", "logout.api"})
    public Msg logout_2(ModelMap modelMap, SessionStatus sessionStatus) {
        String tokenValue = (String) modelMap.get(SessionKeys.TOKEN);
        tokenService.logout(tokenValue);
        sessionStatus.setComplete();
        return new Msg(true, "已退出登录");
    }

    /**
     * 获取已登录的用户信息
     */
    @ResponseBody
    @RequestMapping({"getUser", "getUser.api"})
    public JSONObject get(ModelMap modelMap) {
        User user = (User) modelMap.get(LOGIN_USER);
        JSONObject json = (JSONObject) JSONObject.toJSON(user);
        // 移除敏感信息
        json.remove("password");
        json.remove("role");
        return json;
    }

    /**
     * 添加一个用户
     */
    @ResponseBody
    @PostMapping({"addUser", "addUser.api"})
    public Msg addUser(@Valid User user, BindingResult bindingResult) {
        if (!user.getRole().equals(0)) {
            return null;
        } else if (bindingResult.hasErrors()) {
            return new Msg(false, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        user.setPathMarks(new ArrayList<>());
        user.setRole(1);
        if (userService.addUser(user)) {
            return new Msg(true, "添加成功");
        }
        return new Msg(false, "用户名已存在");
    }

}
