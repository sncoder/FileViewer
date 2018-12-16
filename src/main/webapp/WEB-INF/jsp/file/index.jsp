<%@ page import="cn.sncoder.fv.util.ServletUtil" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    pageContext.setAttribute("basePath", ServletUtil.getBasePath(request));
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>文件管理</title>
    <link href="${basePath}/resources/element-ui/2.3.2/theme-chalk/index.css" rel="stylesheet">
    <link href="${basePath}/resources/fontawesome-free-5.0.9/css/fontawesome-all.min.css" rel="stylesheet">
    <link href="${basePath}/resources/codemirror/codemirror.min.css" rel="stylesheet">
    <link href="${basePath}/resources/codemirror/addon/dialog/dialog.min.css" rel="stylesheet">
    <link href="${basePath}/resources/codemirror/theme/abcdef.min.css" rel="stylesheet">
    <link href="${basePath}/resources/dropzone/5.4.0/dropzone.min.css" rel="stylesheet">
    <link href="${basePath}/resources/css/custom.css" rel="stylesheet">
    <link href="${basePath}/resources/css/file-icon.css" rel="stylesheet">
    <style>
        .cursor {
            cursor: pointer;
        }
        .el-upload-dragger {
            width: 280px;
        }
        .el-main {
            padding-top: 0;
        }
        .el-tag {
            margin-left:10px;
        }
        .el-tag+.el-tag {
            margin-left:10px;
        }
        .dropzone {
            border: 2px dashed #0087F7;
            border-radius: 5px;
            background: white;
        }
    </style>
</head>
<body>
<el-container id="app" v-cloak @keyup.enter="alert('1')">
    <el-header style="height: auto">
        <el-card>
            <div slot="header" style="width: 100%">
                <el-input style="width: 100%" v-model="path" size="mini" @keydown.enter.native="getFiles(path, 1)">
                    <template slot="prepend">
                        <el-button @click="backPath"><i class="fa fa-arrow-left"></i></el-button>
                    </template>
                    <template slot="append">
                        <el-button @click="getFiles(path)" icon="el-icon-refresh"></el-button>
                    </template>
                </el-input>
                <el-row>
                    <el-col :span="20">
                        <i class="fa-star cursor" :class="[isMark ? 'fas' : 'far']" :style="{ color: isMark ? 'rgb(77,125,239)' : '' }" @click="pathMark(path)"></i>
                        <span>（{{ dirNum }}个目录，{{ fileNum }}个文件，大小：{{ sizeFormat(length) }}）</span>
                    </el-col>
                    <el-col :span="4">
                        <el-button type="text" @click="logout" style="padding: 3px;float: right">退出</el-button>
                    </el-col>
                </el-row>
            </div>
            <div>
                <el-row>
                    <el-col :span="24">
                        <%--<c:if test="${role == 0}">
                            <span><el-button type="primary" size="mini" @click="openAddUser"><i class="fa fa-user-plus"></i></el-button></span>
                        </c:if>--%>
                        <span><el-button type="primary" size="mini" @click="openUpload"><i class="fa fa-upload"></i></el-button></span>
                        <el-dropdown type="primary" trigger="click" size="mini" @command="touchAndMkdir">
                            <el-button type="primary" size="mini">新建<i class="el-icon-arrow-down el-icon--right"></i></el-button>
                            <el-dropdown-menu slot="dropdown">
                                <el-dropdown-item command="touch">新建文件</el-dropdown-item>
                                <el-dropdown-item command="mkdir">新建文件夹</el-dropdown-item>
                            </el-dropdown-menu>
                        </el-dropdown>
                        <el-button type="primary" size="mini" @click="backPath"><i class="fa fa-arrow-left"></i></el-button>
                        <span><el-button type="primary" size="mini" @click="getFiles(path)" icon="el-icon-refresh"></el-button></span>
                        <transition name="el-zoom-in-center">
                            <span v-show="pasteShow"><el-button type="warning" size="mini" @click="paste"><i class="fa fa-paste"></i></el-button></span>
                        </transition>
                        <transition name="el-zoom-in-center">
                            <span v-show="batchShow"><el-button type="success" size="mini" @click="batchCopy(path)"><i class="fa fa-copy"></i></el-button></span>
                        </transition>
                        <transition name="el-zoom-in-center">
                            <span v-show="batchShow"><el-button type="success" size="mini" @click="batchCut(path)"><i class="fa fa-cut"></i></el-button></span>
                        </transition>
                        <transition name="el-zoom-in-center">
                            <span v-show="batchShow"><el-button type="success" size="mini" @click="compressSelected"><i class="fa fa-file-archive"></i></el-button></span>
                        </transition>
                        <transition name="el-zoom-in-center">
                            <span v-show="batchShow"><el-button type="success" size="mini" @click="batchDel"><i class="fa fa-trash-alt"></i></el-button></span>
                        </transition>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="24">
                        <i class="fa fa-hdd"></i> (共：{{ sizeFormat(diskInfo.total) }}，已用：{{ sizeFormat(diskInfo.used) }}，剩余：{{ sizeFormat(diskInfo.usable) }})
                    </el-col>
                </el-row>
                <el-row>
                    <el-col>
                        <el-tag
                                class="cursor"
                                v-for="pathMark in user.pathMarks"
                                :key="pathMark.path"
                                closable
                                tag-type="success"
                                @close="cancelPathMark(pathMark)"
                                @click.native="getFiles(pathMark.path)">{{ pathMark.name }}</el-tag>
                    </el-col>
                </el-row>
            </div>
        </el-card>
    </el-header>
    <el-main>
        <el-card>
            <transition name="el-zoom-in-center">
                <el-pagination
                        v-show="pager.total > pager.pageSize"
                        background
                        :current-page="pager.currentPage"
                        :page-size="pager.pageSize"
                        :total="pager.total"
                        layout="total, prev, pager, next, jumper"
                        @current-change="changePage"
                        style="white-space: initial"></el-pagination>
            </transition>
            <el-table
                ref="fileTable"
                :data="files"
                v-loading="tableLoading"
                empty-text="没有文件或文件夹"
                @row-click="rowClick"
                @selection-change="selectionChange">
                <el-table-column type="selection"></el-table-column>
                <el-table-column label="文件名" min-width="500px">
                    <template slot-scope="scope">
                        <span v-if="scope.row.dir" class="cursor" @click.stop="getFiles(scope.row.path, 1)">
                            <span class="ico ico-folder"></span>{{ cutName(scope.row.name) }}
                        </span>
                        <span v-if="!scope.row.dir">
                            <span :class="[getIconClass(scope.row.name)]"></span>{{ cutName(scope.row.name) }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column label="大小" width="100px">
                    <template slot-scope="scope">
                        {{ sizeFormat(scope.row.length) }}
                    </template>
                </el-table-column>
                <el-table-column label="修改时间" width="160px">
                    <template slot-scope="scope">
                        {{ new Date(scope.row.lastModified).formatDateTime() }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="300px">
                    <template slot-scope="scope">
                        <span><el-button type="text" size="mini" @click.stop="copy(scope.row)">复制</el-button></span>
                        <span><el-button type="text" size="mini" @click.stop="cut(scope.row)">剪切</el-button></span>
                        <span><el-button type="text" size="mini" @click.stop="rename(scope.row)">重命名</el-button></span>
                        <span><el-button type="text" size="mini" @click.stop="compress(scope.row)">压缩</el-button></span>
                        <span><el-button v-if="isZip(scope.row.name)" type="text" size="mini" @click.stop="decompress(scope.row)">解压</el-button></span>
                        <span><el-button v-if="!scope.row.dir && isText(scope.row.name)" type="text" size="mini" @click.stop="openEdit(scope.row)">编辑</el-button></span>
                        <span><el-button v-if="isImage(scope.row.name)" type="text" size="mini" @click.stop="showImg(scope.row)">预览</el-button></span>
                        <span><el-button v-if="isVideo(scope.row.name)" type="text" size="mini" @click.stop="video(scope.row)">播放</el-button></span>
                        <span><el-button v-if="!scope.row.dir" type="text" size="mini" @click.stop="openDownload(scope.row)">下载</el-button></span>
                        <span><el-button type="text" size="mini" @click.stop="del(scope.row)">删除</el-button></span>
                    </template>
                </el-table-column>
            </el-table>
            <transition name="el-zoom-in-center">
                <el-pagination
                        v-if="pager.total > pager.pageSize"
                        background
                        :current-page="pager.currentPage"
                        :page-size="pager.pageSize"
                        :total="pager.total"
                        layout="total, prev, pager, next, jumper"
                        @current-change="changePage"
                        style="white-space: initial"></el-pagination>
            </transition>
        </el-card>
    </el-main>
    <el-dialog title="上传文件" :visible.sync="uploadDialog.visible" width="260px" :before-close="closeUpload" :close-on-click-modal="false">
        <el-row>
            <el-col :span="24">
                <el-progress
                    v-show="uploadDialog.total >= 1"
                    :text-inside="true"
                    :stroke-width="18"
                    :status="uploadDialog.uploaded >= uploadDialog.total ? 'success' : ''"
                    :percentage="uploadDialog.total === 0 ? 0 : (Math.round(uploadDialog.uploaded / uploadDialog.total * 10000) / 100)"></el-progress>
            </el-col>
        </el-row>
        <el-row>
            <el-col :span="24">
                <el-progress
                        v-show="uploadDialog.total >= 1"
                        :text-inside="true"
                        :stroke-width="18"
                        color="rgba(142, 113, 199, 0.7)"
                        :percentage="uploadDialog.progress"></el-progress>
            </el-col>
        </el-row>
        <el-row>
            <el-col :span="24">
                <el-container>
                    <el-main id="dropzone" class="dropzone-previews dropzone" style="max-height: 300px"></el-main>
                </el-container>
            </el-col>
        </el-row>
        <div slot="footer">
            <el-button type="primary" :loading="uploadDialog.loading" @click="startUpload">{{ this.buttonLoading ? '上传中...' : '开始上传' }}</el-button>
        </div>
    </el-dialog>
    <el-dialog :visible.sync="downloadDialog.visible" :show-close="false" width="340px">
        <el-button type="primary" @click="download">下载</el-button>
        <el-button type="success" class="clipCopy" :data-clipboard-text="'${basePath}/download/' + downloadDialog.key">复制链接</el-button>
        <el-button type="success" class="clipCopy" :data-clipboard-text="'wget -O ' + downloadDialog.fileName + ' ${basePath}/download/' + downloadDialog.key">复制wget</el-button>
    </el-dialog>
    <el-dialog :title="'编辑文件[' + editFileDialog.file.path + ']'" :visible.sync="editFileDialog.visible" width="90%" top="25px" :close-on-click-modal="false">
        <div>
            <p style="color: red">提示：Ctrl+F 搜索关键字，Ctrl+G 查找下一个，Ctrl+S 保存，Ctrl+Shift+R 查找替换!</p>
            <div style="border: #ccc 1px solid;">
                <textarea id="editFileTextArea" style="width: 100%"></textarea>
            </div>
        </div>
        <div slot="footer">
            <el-button type="primary" @click="saveFileContent">保存</el-button>
            <el-button @click="editFileDialog.visible = false">关闭</el-button>
        </div>
    </el-dialog>
    <el-dialog title="预览图片" :visible.sync="imgDialog.visible" width="90%" top="25px" style="height: auto">
        <el-carousel ref="images" indicator-position="none" trigger="click" :height="imgDialog.height" :initial-index="imgDialog.initIndex" :autoplay="false" @change="imageChange">
            <el-carousel-item v-for="(img, index) in imgDialog.images" :key="img.path">
                <div style="text-align: center">
                    <p>{{ index + 1 }} / {{ imgDialog.images.length }}</p>
                    <img :id="'img-' + index" :style="imgDialog.img.style"/>
                </div>
            </el-carousel-item>
        </el-carousel>
    </el-dialog>
    <%--<c:if test="${role == 0}">
        <el-dialog title="添加用户" :visible.sync="addUserDialog.visible" width="300px">
            <el-form label-position="top" @submit.native.prevent>
                <el-form-item label="用户名">
                    <el-input v-model="addUserDialog.form.username"></el-input>
                </el-form-item>
                <el-form-item label="密码">
                    <el-input v-model="addUserDialog.form.password"></el-input>
                </el-form-item>
                <el-form-item label="根目录">
                    <el-input v-model="addUserDialog.form.rootPath"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="addUser" :loading="buttonLoading">添加</el-button>
                    <el-button>取消</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>
    </c:if>--%>
    <jsp:include page="../common/dialog/alert.jsp"/>
    <jsp:include page="../common/dialog/confirm.jsp"/>
    <jsp:include page="../common/dialog/input.jsp"/>
</el-container>
<script src="${basePath}/resources/vue/2.5.16/vue.js"></script>
<script src="${basePath}/resources/element-ui/2.3.2/index.js"></script>
<script src="${basePath}/resources/axios/0.18.0/axios.min.js"></script>
<script src="${basePath}/resources/qs/6.5.1/qs.min.js"></script>
<script src="${basePath}/resources/clipboard.js/clipboard.min.js"></script>

<script src="${basePath}/resources/codemirror/codemirror.min.js"></script>

<script src="${basePath}/resources/codemirror/addon/dialog/dialog.min.js"></script>

<script src="${basePath}/resources/codemirror/addon/fold/xml-fold.min.js"></script>

<script src="${basePath}/resources/codemirror/addon/mode/multiplex.min.js"></script>

<script src="${basePath}/resources/codemirror/addon/edit/matchbrackets.min.js"></script>
<script src="${basePath}/resources/codemirror/addon/edit/matchtags.min.js"></script>
<script src="${basePath}/resources/codemirror/addon/edit/closebrackets.min.js"></script>
<script src="${basePath}/resources/codemirror/addon/edit/closetag.min.js"></script>

<script src="${basePath}/resources/codemirror/addon/search/search.min.js"></script>
<script src="${basePath}/resources/codemirror/addon/search/searchcursor.min.js"></script>
<script src="${basePath}/resources/codemirror/addon/search/jump-to-line.min.js"></script>

<script src="${basePath}/resources/codemirror/mode/meta.min.js"></script>
<script src="${basePath}/resources/codemirror/mode/clike/clike.min.js"></script>
<script src="${basePath}/resources/codemirror/mode/css/css.min.js"></script>
<script src="${basePath}/resources/codemirror/mode/htmlembedded/htmlembedded.min.js"></script>
<script src="${basePath}/resources/codemirror/mode/htmlmixed/htmlmixed.min.js"></script>
<script src="${basePath}/resources/codemirror/mode/javascript/javascript.min.js"></script>
<script src="${basePath}/resources/codemirror/mode/python/python.min.js"></script>
<script src="${basePath}/resources/codemirror/mode/shell/shell.min.js"></script>
<script src="${basePath}/resources/codemirror/mode/sql/sql.min.js"></script>
<script src="${basePath}/resources/codemirror/mode/ttcn-cfg/ttcn-cfg.min.js"></script>
<script src="${basePath}/resources/codemirror/mode/xml/xml.min.js"></script>

<script src="${basePath}/resources/dropzone/5.4.0/dropzone.min.js"></script>

<script src="${basePath}/resources/js/util/vue-util.js"></script>
<script src="${basePath}/resources/js/util/date-util.js"></script>
<script src="${basePath}/resources/js/util/file-util.js"></script>
<script src="${basePath}/resources/js/util/axios-util.js"></script>
<script src="${basePath}/resources/js/util/cookie-util.js"></script>
<script src="${basePath}/resources/js/util/copy-util.js"></script>
<script>
    let basePath = '${basePath}';
    axios.defaults.baseURL = basePath;
</script>
<%--<c:if test="${role == 0}">
    <script>
        Vue.prototype.openAddUser = _ => {
            app.addUserDialog.form = {};
            app.addUserDialog.visible = true;
        };
        Vue.prototype.addUser = _ => {
            app.buttonLoading = true;
            axios
                .post('/user/addUser', app.formatParams(app.addUserDialog.form))
                .then(({data}) => {
                    app.buttonLoading = false;
                    if (data && data.success) {
                        app.addUserDialog.visible = false;
                        app.$message.success(data.msg);
                    } else if (data) {
                        app.$message.error(data.msg);
                    }
                })
                .error(_ => app.buttonLoading = false);
        };
    </script>
</c:if>--%>
<script src="${basePath}/resources/js/file/index.js"></script>
</body>
</html>
