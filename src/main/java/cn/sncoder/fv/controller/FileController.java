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

@Controller
@RequestMapping("file")
@SessionAttributes(LOGIN_USER)
public class FileController {

    @Resource
    private FileService fileService;

    @Resource
    private UserService userService;

    @GetMapping("index")
    public String index(ModelMap modelMap) {
        User user = (User) modelMap.get(LOGIN_USER);
        modelMap.addAttribute("role", user.getRole());
        return "/jsp/file/index.jsp";
    }

    @ResponseBody
    @PostMapping("listFiles")
    public JSONObject listFiles(String path, @RequestParam(defaultValue = "1", required = false) int page, @ModelAttribute(LOGIN_USER) User user) {
        path = fileService.convertPath(path);
        JSONObject json = new JSONObject();
        Pager<FileBean[]> pager = fileService.listFiles(user, path, page);
        json.put("files", pager.getData());
        json.put("pager", pager.toJson());
        json.put("path", path);
        json.put("file", fileService.toFileBean(user, path));
        json.put("diskInfo", fileService.diskInfo(user));
        json.put("success", true);
        return json;
    }

    @ResponseBody
    @PostMapping("del")
    public Msg del(String path, @ModelAttribute(LOGIN_USER) User user) {
        if (StringUtils.isEmpty(path) || path.equals("/")) {
            return new Msg(false, "删除失败");
        }
        boolean success = fileService.del(user, path);
        String msg = success ? "删除成功" : "删除失败";
        return new Msg(success, msg);
    }

    @ResponseBody
    @PostMapping("batchDel")
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

    @ResponseBody
    @PostMapping("touch")
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

    @ResponseBody
    @PostMapping("mkdir")
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

    @ResponseBody
    @PostMapping("upload")
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

    @ResponseBody
    @PostMapping("exist")
    public Msg exist(String path, long length, @ModelAttribute(LOGIN_USER) User user) {
        return new Msg(fileService.exist(user, path, length));
    }

    @ResponseBody
    @PostMapping("copy")
    public Msg copy(String srcPath, String destPath, String name, @ModelAttribute(LOGIN_USER) User user) {
        srcPath = fileService.convertPath(srcPath);
        destPath = fileService.convertPath(destPath);
        try {
            fileService.copy(user, srcPath + name, destPath + name);
            return new Msg(true, "复制成功");
        } catch (IOException e) {
            return new Msg(false, e.getMessage());
        }
    }

    @ResponseBody
    @PostMapping("batchCopy")
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
            } catch (IOException ignore) {}
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
    @PostMapping("cut")
    public Msg cut(String srcPath, String destPath, String name, @ModelAttribute(LOGIN_USER) User user) {
        srcPath = fileService.convertPath(srcPath);
        destPath = fileService.convertPath(destPath);
        try {
            fileService.cut(user, srcPath + name, destPath + name);
            return new Msg(true, "剪切成功");
        } catch (IOException e) {
            return new Msg(false, e.getMessage());
        }
    }

    @ResponseBody
    @PostMapping("batchCut")
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
            } catch (IOException ignore) {}
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
    @PostMapping("rename")
    public Msg rename(String path, String oldName, String newName, @ModelAttribute(LOGIN_USER) User user) {
        path = fileService.convertPath(path);
        try {
            if (fileService.rename(user, path + oldName, path + newName)) {
                return new Msg(true, "重命名成功");
            }
            return new Msg(false, "重命名失败");
        } catch (IOException e) {
            return new Msg(false, e.getMessage());
        }
    }

    @ResponseBody
    @PostMapping("compress")
    public Msg compress(String path, String[] names, String zipName, @ModelAttribute(LOGIN_USER) User user) {
        if (ArrayUtils.isEmpty(names)) {
            return new Msg(false, "请选择要压缩的文件或文件夹");
        }
        path = fileService.convertPath(path);
        try {
            fileService.compress(user, path, names, zipName);
            return new Msg(true, "压缩成功");
        } catch (IOException e) {
            return new Msg(false, e.getMessage());
        }
    }

    @ResponseBody
    @PostMapping("decompress")
    public Msg decompress(String path, @ModelAttribute(LOGIN_USER) User user) {
        try {
            fileService.decompress(user, path);
            return new Msg(true, "解压成功");
        } catch (IOException e) {
            return new Msg(false, e.getMessage());
        }
    }

    @ResponseBody
    @PostMapping("fileKey")
    public Msg fileKey(String path, @ModelAttribute(LOGIN_USER) User user) {
        String key = fileService.getKey(user, path);
        if (StringUtils.isEmpty(key)) {
            return new Msg(false, "文件不存在");
        }
        return new Msg(true, key);
    }

    @ResponseBody
    @PostMapping("content")
    public Msg content(String path, @ModelAttribute(LOGIN_USER) User user) {
        try {
            String content = fileService.getContent(user, path);
            return new Msg(true, content);
        } catch (IOException e) {
            return new Msg(false, e.getMessage());
        }
    }

    @ResponseBody
    @PostMapping("saveContent")
    public Msg saveContent(String path, String content, @ModelAttribute(LOGIN_USER) User user) {
        try {
            fileService.saveContent(user, path, content);
            return new Msg(true, "保存成功");
        } catch (IOException e) {
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
                sendError(response, 500);
            }
        }
    }

    @ResponseBody
    @PostMapping("makePathMark")
    public Msg makePathMark(String name, String path, @ModelAttribute(LOGIN_USER) User user) {
        path = fileService.convertPath(path);
        if (userService.makePathMark(user, name, path)) {
            return new Msg(true, "加入书签成功");
        } else {
            return new Msg(false, "该书签已存在");
        }
    }

    @ResponseBody
    @PostMapping("cancelPathMark")
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
        } catch (IOException ignore) { }
    }

}
