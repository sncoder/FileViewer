<%@ page import="cn.sncoder.fv.util.ServletUtil" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    pageContext.setAttribute("basePath", ServletUtil.getBasePath(request));
%>
<!DOCTYPE html>
<html>
<head>
    <title>${videoName}</title>
    <meta charset="UTF-8">
    <%--<meta name="viewport" content="width=device-width, initial-scale=1.0">--%>
    <link href="${basePath}/resources/video.js/6.8.0/video-js.min.css" rel="stylesheet">
    <style>
        .video-js {
            margin: 0 auto;
        }
    </style>
</head>
<body style="text-align: center">
<div>
    <h1>${videoName}</h1>
    <video id="video" class="video-js" controls preload="auto" width="1000" height="600" data-setup="{}">
        <source src="${basePath}/download/${fileKey}" type='video/mp4'>
        <source src="${basePath}/download/${fileKey}" type='video/webm'>
        <source src="${basePath}/download/${fileKey}" type='video/ogg'>
        <p class="vjs-no-js">
            格式不支持
        </p>
    </video>
</div>

<script src="${basePath}/resources/video.js/6.8.0/video.min.js"></script>
</body>
</html>
