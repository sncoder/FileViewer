package cn.sncoder.fv.controller;

import cn.sncoder.fv.bean.User;
import cn.sncoder.fv.entity.Msg;
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

@Controller
@RequestMapping("user")
@SessionAttributes(LOGIN_USER)
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("login")
    public String login(ModelMap modelMap) {
        if (modelMap.get(LOGIN_USER) != null) {
            return "redirect:/file/index";
        }
        return "/jsp/user/login.jsp";
    }

    @ResponseBody
    @PostMapping("login")
    public Msg login(User user, ModelMap modelMap) {
        user = userService.loginCheck(user);
        if (user == null) {
            return new Msg(false, "账号或密码错误");
        }
        modelMap.addAttribute(LOGIN_USER, user);
        return new Msg(true, "登录成功");
    }

    @GetMapping("logout")
    public String logout(SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return "redirect:/user/login";
    }

    @ResponseBody
    @GetMapping("getUser")
    public JSONObject get(ModelMap modelMap) {
        User user = (User) modelMap.get(LOGIN_USER);
        JSONObject json = (JSONObject) JSONObject.toJSON(user);
        json.remove("password");
        json.remove("role");
        return json;
    }

    @ResponseBody
    @PostMapping("addUser")
    public Msg addUser(@Valid User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
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
