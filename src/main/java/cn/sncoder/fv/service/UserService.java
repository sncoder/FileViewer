package cn.sncoder.fv.service;

import cn.sncoder.fv.bean.PathMark;
import cn.sncoder.fv.bean.User;
import cn.sncoder.fv.db.UserDB;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserService {

    @Resource
    private UserDB userDB;

    public User loginCheck(User user) {
        return userDB.get(user.getUsername(), user.getPassword());
    }

    public boolean makePathMark(User user, String name, String path) {
        PathMark pathMark = new PathMark(name, path);
        return userDB.addPathMark(user.getUsername(), pathMark);
    }

    public void cancelPathMark(User user, String path) {
        userDB.removePathMark(user.getUsername(), new PathMark(null, path));
    }

    public boolean addUser(User user) {
        return userDB.add(user);
    }

}
