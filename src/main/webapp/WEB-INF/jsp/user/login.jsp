<%@ page import="cn.sncoder.fv.util.ServletUtil" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    pageContext.setAttribute("basePath", ServletUtil.getBasePath(request));
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录</title>
    <link href="${basePath}/resources/element-ui/2.3.2/theme-chalk/index.css" rel="stylesheet">
    <link href="${basePath}/resources/css/custom.css" rel="stylesheet">
</head>
<body>
<el-container id="app" v-cloak>
    <el-main>
        <el-row type="flex" justify="center">
            <el-col :xs="24" :sm="20" :md="12" :lg="8">
                <el-card>
                    <div slot="header">登录</div>
                    <el-form :model="user">
                        <el-form-item>
                            <el-input v-model="user.username" @keydown.enter.native="login" autofocus>
                                <template slot="prepend">账号</template>
                            </el-input>
                        </el-form-item>
                        <el-form-item>
                            <el-input type="password" v-model="user.password" @keydown.enter.native="login">
                                <template slot="prepend">密码</template>
                            </el-input>
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" @click="login" :loading="buttonLoading" style="width: 100%">{{ buttonLoading ? '登录中...' : '登录' }}</el-button>
                        </el-form-item>
                    </el-form>
                </el-card>
            </el-col>
        </el-row>
    </el-main>
    <jsp:include page="../common/dialog/alert.jsp"/>
</el-container>
<script src="${basePath}/resources/vue/2.5.16/vue.min.js"></script>
<script src="${basePath}/resources/element-ui/2.3.2/index.js"></script>
<script src="${basePath}/resources/axios/0.18.0/axios.min.js"></script>
<script src="${basePath}/resources/qs/6.5.1/qs.min.js"></script>
<script src="${basePath}/resources/js/util/vue-util.js"></script>
<script src="${basePath}/resources/js/util/axios-util.js"></script>
<script>
    let basePath = '${basePath}';
    axios.defaults.baseURL = basePath;
</script>
<script src="${basePath}/resources/js/user/login.js"></script>
</body>
</html>
