package cn.sncoder.fv.controller;

import cn.sncoder.fv.bean.FileBean;
import cn.sncoder.fv.bean.User;
import cn.sncoder.fv.entity.Msg;
import cn.sncoder.fv.entity.Pager;
import cn.sncoder.fv.service.FileService;
import cn.sncoder.fv.service.UserService;
import cn.sncoder.fv.util.ServletUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static cn.sncoder.fv.constant.SessionKeys.LOGIN_USER;

/**
 * @author shibiao
 */
@Controller
@RequestMapping("file")
@SessionAttributes(LOGIN_USER)
public class FileController {

    @Resource
    private FileService fileService;

    @Resource
    private UserService userService;

    /**
     * 跳转到主页
     */
    @GetMapping({"index", ""})
    public String index(ModelMap modelMap) {
        User user = (User) modelMap.get(LOGIN_USER);
        modelMap.addAttribute("role", user.getRole());
        return "/jsp/file/index.jsp";
    }

    /**
     * 获取目录下的文件（夹）请求
     *
     * @param path 相对路径
     * @param page 页数
     * @param user 登录的用户
     */
    @ResponseBody
    @PostMapping({"listFiles", "listFiles.api"})
    public JSONObject listFiles(String path, @RequestParam(defaultValue = "1", required = false) int page, @ModelAttribute(LOGIN_USER) User user) {
        path = fileService.convertPath(path);
        JSONObject json = new JSONObject();
        Pager<FileBean[]> pager = fileService.listFiles(user, path, page);
        json.put("files", pager.getData());
        json.put("pager", pager.toJson());
        json.put("path", path);
        json.put("file", fileService.getFileBean(user, path));
        json.put("diskInfo", fileService.diskInfo(user));
        json.put("success", true);
        return json;
    }

    /**
     * 删除文件（夹）
     *
     * @param path 相对路径
     * @param user 登录的用户
     */
    @ResponseBody
    @PostMapping({"del", "del.api"})
    public Msg del(String path, @ModelAttribute(LOGIN_USER) User user) {
        if (StringUtils.isEmpty(path) || path.equals(FileService.FILE_SEPARATOR)) {
            return new Msg(false, "删除失败");
        }
        boolean success = fileService.del(user, path);
        String msg = success ? "删除成功" : "删除失败";
        return new Msg(success, msg);
    }

    /**
     * 批量删除文件（夹）
     *
     * @param paths 要删除的所有路径
     * @param user 登录的用户
     */
    @ResponseBody
    @PostMapping({"batchDel", "batchDel.api"})
    public Msg batchDel(String[] paths, @ModelAttribute(LOGIN_USER) User user) {
        if (ArrayUtils.isEmpty(paths)) {
            return new Msg(false, "请选择目录");
        }
        int successNum = 0;
        for (String path : paths) {
            if (fileService.del(user, path)) {
                ++successNum;
            }
        }
        boolean success = paths.length == successNum;
        String msg = success ? "批量删除成功" : ("成功删除" + successNum + "个，失败" + (paths.length - successNum));
        return new Msg(success, msg);
    }

    /**
     * 创建文件
     *
     * @param path 要创建的文件所在的目录
     * @param name 要创建的文件名
     * @param user 登录的用户
     */
    @ResponseBody
    @PostMapping({"touch", "touch.api"})
    public Msg touch(String path, String name, @ModelAttribute(LOGIN_USER) User user) {
        if (StringUtils.isEmpty(name)) {
            return new Msg(false, "请输入文件名称");
        }
        path = fileService.convertPath(path);
        try {
            fileService.touch(user, path + name);
            return new Msg(true, "新建文件成功");
        } catch (IOException e) {
            return new Msg(false, e.getMessage());
        }
    }

    /**
     * 创建文件夹
     *
     * @param path 要创建的文件夹所在的目录
     * @param name 要创建的文件夹名
     * @param user 登录的用户
     */
    @ResponseBody
    @PostMapping({"mkdir", "mkdir.api"})
    public Msg mkdir(String path, String name, @ModelAttribute(LOGIN_USER) User user) {
        if (StringUtils.isEmpty(name)) {
            return new Msg(false, "请输入文件夹名称");
        }
        path = fileService.convertPath(path);
        try {
            fileService.mkdir(user, path + name);
            return new Msg(true, "新建文件夹成功");
        } catch (IOException e) {
            return new Msg(false, e.getMessage());
        }
    }

    /**
     * 上传文件，文件已存在会直接覆盖
     *
     * @param path 包含文件名的路径
     * @param file 上传的文件
     * @param user 登录的用户
     */
    @ResponseBody
    @PostMapping({"upload", "upload.api"})
    public Msg upload(String path, MultipartFile file, @ModelAttribute(LOGIN_USER) User user) {
        path = fileService.convertPath(path);
        File f = fileService.getFile(user, path + file.getOriginalFilename());
        try {
            FileUtils.forceMkdir(f.getParentFile());
            file.transferTo(f);
            return new Msg(true);
        } catch (IOException e) {
            return new Msg(false, e.getMessage());
        }
    }

    /**
     * 判断文件（夹）是否存在，如果是文件，则会判断length是否相等，如果是文件夹，不会判断length
     *
     * @param path 相对路径
     * @param length 文件长度
     * @param user 登录的用户
     */
    @ResponseBody
    @PostMapping({"exist", "exist.api"})
    public Msg exist(String path, long length, @ModelAttribute(LOGIN_USER) User user) {
        return new Msg(fileService.exist(user, path, length));
    }

    /**
     * 复制文件（夹）
     *
     * @param srcPath 源目录
     * @param destPath 目标目录
     * @param name 要复制的文件（夹）名
     * @param user 登录的用户
     */
    @ResponseBody
    @PostMapping({"copy", "copy.api"})
    public Msg copy(String srcPath, String destPath, String name, @ModelAttribute(LOGIN_USER) User user) {
        srcPath = fileService.convertPath(srcPath);
        destPath = fileService.convertPath(destPath);
        try {
            fileService.copy(user, srcPath + name, destPath + name);
            return new Msg(true, "复制成功");
        } catch (IOException e) {
            e.printStackTrace();
            return new Msg(false, e.getMessage());
        }
    }

    /**
     * 批量复制文件和文件夹
     *
     * @param srcPath 源目录
     * @param destPath 目标目录
     * @param names 源目录下的文件夹和文件名称
     */
    @ResponseBody
    @PostMapping({"batchCopy", "batchCopy.api"})
    public Msg batchCopy(String srcPath, String destPath, String[] names, @ModelAttribute(LOGIN_USER) User user) {
        if (ArrayUtils.isEmpty(names)) {
            return new Msg(false, "请选择要复制的内容");
        }
        srcPath = fileService.convertPath(srcPath);
        destPath = fileService.convertPath(destPath);
        int successCount = 0;
        for (String name : names) {
            try {
                fileService.copy(user, srcPath + name, destPath + name);
                ++successCount;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        boolean success = successCount == names.length;
        String msg;
        if (success) {
            msg = "成功复制" + successCount + "个";
        } else {
            int failCount = names.length - successCount;
            msg = "成功复制" + successCount + "个，失败" + failCount + "个";
        }
        return new Msg(success, msg);
    }

    @ResponseBody
    @PostMapping({"cut", "cut.api"})
    public Msg cut(String srcPath, String destPath, String name, @ModelAttribute(LOGIN_USER) User user) {
        srcPath = fileService.convertPath(srcPath);
        destPath = fileService.convertPath(destPath);
        try {
            fileService.cut(user, srcPath + name, destPath + name);
            return new Msg(true, "剪切成功");
        } catch (IOException e) {
            e.printStackTrace();
            return new Msg(false, e.getMessage());
        }
    }

    @ResponseBody
    @PostMapping({"batchCut", "batchCut.api"})
    public Msg batchCut(String srcPath, String destPath, String[] names, @ModelAttribute(LOGIN_USER) User user) {
        if (ArrayUtils.isEmpty(names)) {
            return new Msg(false, "请选择要复制的内容");
        }
        srcPath = fileService.convertPath(srcPath);
        destPath = fileService.convertPath(destPath);
        int successCount = 0;
        for (String name : names) {
            try {
                fileService.cut(user, srcPath + name, destPath + name);
                ++successCount;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        boolean success = successCount == names.length;
        String msg;
        if (success) {
            msg = "成功剪切" + successCount + "个";
        } else {
            int failCount = names.length - successCount;
            msg = "成功剪切" + successCount + "个，失败" + failCount + "个";
        }
        return new Msg(success, msg);
    }

    @ResponseBody
    @PostMapping({"rename", "rename.api"})
    public Msg rename(String path, String oldName, String newName, @ModelAttribute(LOGIN_USER) User user) {
        path = fileService.convertPath(path);
        try {
            if (fileService.rename(user, path + oldName, path + newName)) {
                return new Msg(true, "重命名成功");
            }
            return new Msg(false, "重命名失败");
        } catch (IOException e) {
            e.printStackTrace();
            return new Msg(false, e.getMessage());
        }
    }

    @ResponseBody
    @PostMapping({"compress", "compress.api"})
    public Msg compress(String path, String[] names, String zipName, @ModelAttribute(LOGIN_USER) User user) {
        if (ArrayUtils.isEmpty(names)) {
            return new Msg(false, "请选择要压缩的文件或文件夹");
        }
        path = fileService.convertPath(path);
        try {
            fileService.compress(user, path, names, zipName);
            return new Msg(true, "压缩成功");
        } catch (IOException e) {
            e.printStackTrace();
            return new Msg(false, e.getMessage());
        }
    }

    @ResponseBody
    @PostMapping({"decompress", "decompress.api"})
    public Msg decompress(String path, @ModelAttribute(LOGIN_USER) User user) {
        try {
            fileService.decompress(user, path);
            return new Msg(true, "解压成功");
        } catch (IOException e) {
            e.printStackTrace();
            return new Msg(false, e.getMessage());
        }
    }

    @ResponseBody
    @PostMapping({"fileKey", "fileKey.api"})
    public Msg fileKey(String path, @ModelAttribute(LOGIN_USER) User user) {
        String key = fileService.getKey(user, path);
        if (StringUtils.isEmpty(key)) {
            return new Msg(false, "文件不存在");
        }
        return new Msg(true, key);
    }

    @ResponseBody
    @PostMapping({"content", "content.api"})
    public Msg content(String path, @ModelAttribute(LOGIN_USER) User user) {
        try {
            String content = fileService.getContent(user, path);
            return new Msg(true, content);
        } catch (Throwable e) {
            e.printStackTrace();
            return new Msg(false, e.getMessage());
        }
    }

    @ResponseBody
    @PostMapping({"saveContent", "saveContent.api"})
    public Msg saveContent(String path, String content, @ModelAttribute(LOGIN_USER) User user) {
        try {
            fileService.saveContent(user, path, content);
            return new Msg(true, "保存成功");
        } catch (IOException e) {
            e.printStackTrace();
            return new Msg(false, e.getMessage());
        }
    }

    @GetMapping("download")
    public void download(String path, HttpServletRequest request, HttpServletResponse response, @ModelAttribute(LOGIN_USER) User user) {
        File file = fileService.getFile(user, path);
        if (!file.isFile()) {
            sendError(response, 404);
        } else {
            try {
                ServletUtil.downloadFile(file, request, response);
            } catch (IOException e) {
                e.printStackTrace();
                sendError(response, 500);
            }
        }
    }

    @ResponseBody
    @PostMapping({"makePathMark", "makePathMark.api"})
    public Msg makePathMark(String name, String path, @ModelAttribute(LOGIN_USER) User user) {
        path = fileService.convertPath(path);
        if (userService.makePathMark(user, name, path)) {
            return new Msg(true, "加入书签成功");
        } else {
            return new Msg(false, "该书签已存在");
        }
    }

    @ResponseBody
    @PostMapping({"cancelPathMark", "cancelPathMark.api"})
    public Msg cancelPathMark(String path, @ModelAttribute(LOGIN_USER) User user) {
        path = fileService.convertPath(path);
        userService.cancelPathMark(user, path);
        return new Msg(true, "移除书签成功");
    }

    @GetMapping("video")
    public String video(String path, ModelMap modelMap, HttpServletResponse response) {
        User user = (User) modelMap.get(LOGIN_USER);
        File file = fileService.getFile(user, path);
        if (!file.isFile()) {
            sendError(response, 404);
            return null;
        }
        String key = fileService.getKey(user, path);
        modelMap.addAttribute("videoName", file.getName());
        modelMap.addAttribute("fileKey", key);
        return "/jsp/file/video.jsp";
    }

    private void sendError(HttpServletResponse response, int sc) {
        try {
            response.sendError(sc);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
