package cn.sncoder.fv.db;

import cn.sncoder.fv.bean.PathMark;
import cn.sncoder.fv.bean.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDB extends BaseDB<User> {

    private static final String dbPath = "/db/user.json";

    @Override
    protected String getDBPath() {
        return dbPath;
    }

    public User get(String username, String password) {
        Optional<User> optional = objs.stream().filter(user -> user.getUsername().equals(username) && user.getPassword().equals(password)).findFirst();
        return optional.orElse(null);
    }

    public User get(String username) {
        Optional<User> optional = objs.stream().filter(user -> user.getUsername().equals(username)).findFirst();
        return optional.orElse(null);
    }

    public boolean contains(String username) {
        Optional<User> optional = objs.stream().filter(user -> user.getUsername().equals(username)).findFirst();
        return optional.isPresent();
    }

    public boolean add(User user) {
        if (contains(user.getUsername())) {
            return false;
        }
        boolean success = objs.add(user);
        needUpdate(success);
        return success;
    }

    public boolean addPathMark(String username, PathMark pathMark) {
        User user = get(username);
        if (user == null) return false;

        if (user.getPathMarks().contains(pathMark)) {
            return false;
        }
        boolean success = user.getPathMarks().add(pathMark);
        needUpdate(success);
        return success;
    }

    public boolean removePathMark(String username, PathMark pathMark) {
        User user = get(username);
        if (user == null) return false;

        boolean success = user.getPathMarks().remove(pathMark);
        needUpdate(success);
        return success;
    }

}
