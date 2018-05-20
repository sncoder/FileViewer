package cn.sncoder.fv.controller;

import cn.sncoder.fv.service.FileService;
import cn.sncoder.fv.util.ServletUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static cn.sncoder.fv.constant.SessionKeys.LOGIN_USER;

@Controller
@SessionAttributes(LOGIN_USER)
public class BaseController {

    @Resource
    private FileService fileService;

    @GetMapping("download/{key}")
    public void download(@PathVariable String key, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (StringUtils.isEmpty(key) || key.length() != fileService.keyLength) {
            response.sendError(404);
            return;
        }
        String absPath = fileService.getAbsPath(key);
        File file = new File(absPath);
        ServletUtil.downloadFile(file, request, response);
    }

}
