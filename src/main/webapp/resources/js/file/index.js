let DEFAULT_REFRESH_INTERVAL = 180;

let app = new Vue({
    el: '#app',
    data: {
        alertDialog: { visible: false },
        confirmDialog: { visible: false, confirm: _ => {} },
        inputDialog: { visible: false, confirm: _ => {} },
        addUserDialog: { visible: false, form: {} },
        uploadDialog: { visible: false, dropzone: null, loading: false, uploaded: 0, total: 0, progress: 0 },
        downloadDialog: { visible: false, key: '' },
        editFileDialog: { visible: false, file: {}, editor: null },
        imgDialog: { visible: false, images: [], img: {}},
        buttonLoading: false,
        tableLoading: false,
        user: {},
        path: '/',
        isMark: false,
        files: [],
        file: {},
        diskInfo: { used: 0, total: 1, usable: 0 },
        pager: { currentPage: 1 },
        fileNum: 0,
        dirNum: 0,
        length: 0,
        pasteShow: false,
        batchShow: false,
        selectedFiles: [],
        refreshCountdown: DEFAULT_REFRESH_INTERVAL
    },
    methods: {
        beginRefreshCountdown: function() {
            if (this.refreshCountdown > 0) {
                --this.refreshCountdown;
            } else {
                this.refreshCountdown = DEFAULT_REFRESH_INTERVAL;
                this.getFiles();
            }
            setTimeout('app.beginRefreshCountdown()', 1000);
        },
        getUser: function () { axios.get('/user/getUser').then(({data}) => this.user = data); },
        getFiles: function (path = this.path, page = this.pager.currentPage) {
            this.tableLoading = true;
            axios.post('/file/listFiles', this.formatParams({ path: path, page: page }))
                .then(({data}) => {
                    this.tableLoading = false;
                    if (!data) { this.files.splice(0); return; }
                    if (!data.success) { this.$message.error(data.msg); return; }
                    this.copyArr(this.files, data.files);
                    this.pager = data.pager;
                    this.path = data.path;
                    this.isMark = this.getPathMark(this.path) != null;
                    this.file = data.file;
                    this.diskInfo = data.diskInfo;
                    setCookie('path', this.path);
                    this.refreshCountdown = DEFAULT_REFRESH_INTERVAL;
                });
        },
        changePage: function(page) { this.getFiles(undefined, page); },
        backPath: function () {
            if (this.file.parentPath === null) {
                this.$message.warning('已经是根目录了');
            } else {
                this.getFiles(this.file.parentPath, 1);
            }
        },
        getIconClass: function (name) { return 'ico ico-' + getExtName(name); },
        rowClick: function (file) { this.$refs.fileTable.toggleRowSelection(file); },
        selectionChange: function (selection) {
            this.selectedFiles = selection;
            this.batchShow = selection.length > 1;
        },
        del: function (file) {
            let title = file.dir ? '删除文件夹' : '删除文件';
            let msg = '确定要删除' + this.cutName(file.name, 15) + '吗？';
            this.openConfirm(title, msg, _ => { this.sendPost('/file/del', {path : file.path}, this.confirmDialog); });
        },
        batchDel: function() {
            let paths = [];
            for (let i in this.selectedFiles) { paths.push(this.selectedFiles[i].path); }
            this.openConfirm('批量删除', '确认要删除这些文件或文件夹吗？', _ => { this.sendPost('/file/batchDel', {paths: paths}, this.confirmDialog); });
        },
        touchAndMkdir: function(command) { if (command === 'touch') { this.touch(); } else if (command === 'mkdir') { this.mkdir() } },
        touch: function() {
            this.openInput({
                title: '新建文件',
                label: '文件名称',
                confirm: _ => {
                    this.sendPost('/file/touch', { path: this.path, name: this.inputDialog.value }, this.inputDialog);
                }
            });
        },
        mkdir: function() {
            this.openInput({
                title: '新建文件夹',
                label: '文件夹名称',
                confirm: _ => {
                    this.sendPost('/file/mkdir', { path: this.path, name: this.inputDialog.value }, this.inputDialog);
                }
            });
        },
        openUpload: function() {
            this.uploadDialog.loading = false;
            this.uploadDialog.uploaded = 0;
            this.uploadDialog.progress = 0;
            this.uploadDialog.total = 0;
            this.uploadDialog.visible = true;
            if (!this.uploadDialog.dropzone) {
                this.$nextTick(this.dropzoneInit);
            }
        },
        startUpload: function(file) {
            this.uploadDialog.total = this.uploadDialog.dropzone.files.length;
            if (this.uploadDialog.dropzone.getQueuedFiles().length > 0) {
                this.uploadDialog.loading = true;
                this.uploadDialog.dropzone.processQueue();
            } else if (!file) {
                this.$message.warning('没有需要上传的文件');
            }
        },
        closeUpload: function(done) {
            let dropzone = this.uploadDialog.dropzone;
            if (dropzone.getQueuedFiles().length > 0 || dropzone.getUploadingFiles().length > 0) {
                this.openConfirm('取消上传', '上传任务将中止，确定要取消上传吗？', _ => {
                    done();
                    this.uploadDialog.loading = false;
                    dropzone.removeAllFiles(true);
                    this.confirmDialog.visible = false;
                });
            } else {
                done();
                this.uploadDialog.loading = false;
                dropzone.removeAllFiles(true);
            }
        },
        dropzoneInit: function() {
            let dropzone = this.uploadDialog.dropzone = new Dropzone('#dropzone', {
                url: basePath + '/file/upload',
                timeout: 3600000,
                parallelUploads: 1,
                maxFilesize: 10240,
                autoProcessQueue: false,
                addRemoveLinks: true,
                previewsContainer: '#dropzone',
                dictDefaultMessage: '点击或拖拽文件上传',
                dictFileTooBig: '最大上传文件大小为{{maxFilesize}}',
                dictCancelUpload: '取消上传',
                dictUploadCanceled: '已取消上传',
                dictCancelUploadConfirmation: '确定要取消上传此文件吗',
                dictRemoveFile: '移除文件'
            });
            Dropzone.confirm = (question, accepted) => {
                this.openConfirm('', question, _ => {
                    this.confirmDialog.visible = false;
                    accepted();
                });
            };
            dropzone.on('sending', (file, xhr, formData) => {
                if (file.fullPath) {
                    let fullPath = file.fullPath;
                    let path = fullPath.substring(0, fullPath.lastIndexOf('/') + 1);
                    formData.append('path', this.file.path + path);
                } else {
                    formData.append('path', this.file.path);
                }
            });
            dropzone.on('canceled', this.startUpload);
            dropzone.on('complete', this.startUpload);
            dropzone.on('success', file => {
                ++this.uploadDialog.uploaded;
                this.startUpload(file);
            });
            dropzone.on('error', this.startUpload);
            dropzone.on('uploadprogress', (file, progress) => {
                this.uploadDialog.progress = Math.round(progress * 100) / 100;
            });
            dropzone.on('queuecomplete', _ => {
                this.uploadDialog.loading = false;
                this.$message.success('所有文件上传完成');
                this.getFiles();
            });
        },
        copy: function(file) {
            this.clearCopyCutCookie();
            setCookie('copyPath', file.path, 1);
            this.$message.success('记录成功');
            this.pasteShow = true;
        },
        batchCopy: function() { this.saveBatchNames(true); },
        cut: function(file) {
            this.clearCopyCutCookie();
            setCookie('cutPath', file.path, 1);
            this.$message.success('记录成功');
            this.pasteShow = true;
        },
        batchCut: function() { this.saveBatchNames(false); },
        saveBatchNames: function(isCopy) {
            let names = [];
            for (let i in this.selectedFiles) { names.push(this.selectedFiles[i].name); }
            this.clearCopyCutCookie();
            setCookie(isCopy ? 'batchCopyPath' : 'batchCutPath', this.path);
            setCookie('batchNames', JSON.stringify(names));
            this.$message.success('记录成功');
            this.pasteShow = true;
            this.$refs.fileTable.clearSelection();
        },
        paste: function() {
            let path;
            let url = null;
            let isBatch = false;
            if (path = getCookie('copyPath')) {
                url = '/file/copy';
            } else if (path = getCookie('cutPath')) {
                url = '/file/cut';
            } else if (path = getCookie('batchCopyPath')) {
                url = '/file/batchCopy';
                isBatch = true;
            } else if (path = getCookie('batchCutPath')) {
                url = '/file/batchCut';
                isBatch = true;
            }
            if (url) {
                if (!isBatch) {
                    if (path.endsWith('/')) { path = path.substring(0, path.length - 1); }
                    let index = path.lastIndexOf('/');
                    let srcPath = path.substring(0, index + 1);
                    let name = path.substring(index + 1, path.length);
                    this.sendPost(url, { srcPath: srcPath, destPath: this.path, name: name }, null, this.loading('努力操作中...'));
                } else {
                    let names = JSON.parse(getCookie('batchNames'));
                    if (names.length === 0) {
                        this.$message.error('请选择至少一个文件或文件夹');
                        return;
                    }
                    this.sendPost(url, { srcPath: path, destPath: this.path, names: names }, null, this.loading('努力操作中...'));
                }
                this.clearCopyCutCookie();
            }
            this.pasteShow = false;
        },
        rename: function(file) {
            this.openInput({
                title: '重命名',
                label: '新名称',
                value: file.name,
                confirm: _ => { this.sendPost('/file/rename', { path: file.parentPath, oldName: file.name, newName: this.inputDialog.value }, this.inputDialog); }
            });
        },
        compress: function(file) {
            this.openInput({
                title: '压缩',
                label: '压缩文件名称',
                value: file.name + '.zip',
                confirm: _ => {
                    this.sendPost('/file/compress', { path: file.parentPath, names: [file.name], zipName: this.inputDialog.value }, this.inputDialog, this.loading('努力压缩中...'));
                }
            });
        },
        compressSelected: function() {
            let selectedFiles = this.selectedFiles;
            if (selectedFiles.length === 0) {
                this.$message.error('请选择要压缩的文件');
                return;
            }
            let names = [];
            for (let i in selectedFiles) { names.push(selectedFiles[i].name); }
            this.openInput({
                title: '压缩',
                label: '压缩文件名称',
                value: names[0] + '.zip',
                confirm: _ => {
                    this.sendPost('/file/compress', { path: selectedFiles[0].parentPath, names: names, zipName: this.inputDialog.value }, this.inputDialog, this.loading('努力压缩中...'));
                }
            });
        },
        decompress: function(file) { this.openConfirm('解压', '确定要解压吗？', _ => { this.sendPost('/file/decompress', { path: file.path }, this.confirmDialog, this.loading('努力解压中...')); }); },
        openDownload: function(file) {
            let loading = this.loading('获取直链中...');
            this.sendPost('/file/fileKey', { path: file.path }, null, loading, false, false, data => {
                if (!data.success) { this.$message.error(data.msg); return; }
                this.downloadDialog.key = data.msg;
                this.downloadDialog.fileName = file.name;
                this.downloadDialog.visible = true;
            });
        },
        download: function() { window.open(basePath + '/download/' + this.downloadDialog.key); },
        openEdit: function(file) {
            let loading = this.loading('获取文件内容中...');
            this.sendPost('/file/content', { path: file.path }, null, loading, false, false, data => {
                if (!data.success) { this.$message.error(data.msg); return; }
                this.openCodeMirror(file, data.msg);
            });
        },
        openCodeMirror: function(file, content) {
            this.editFileDialog.file = file;
            this.editFileDialog.visible = true;
            this.$nextTick(_ => {
                this.initEditor();
                this.editFileDialog.editor.focus();
                let mode = CodeMirror.findModeByFileName(file.name);
                this.editFileDialog.editor.setOption('mode', mode ? mode.mime : 'text/plain');
                this.editFileDialog.editor.setValue(content);
            });
        },
        initEditor: function() {
            let ta = document.getElementById('editFileTextArea');
            if (!this.editFileDialog.editor) {
                this.editFileDialog.editor = CodeMirror.fromTextArea(ta, {
                    extraKeys: {
                        "Ctrl-F": "findPersistent",
                        "Ctrl-H": "replaceAll",
                        // "Ctrl-Space": "autocomplete",
                        "Ctrl-J": "toMatchingTag",
                        "Ctrl-S": _ => { this.saveFileContent(); }
                    },
                    theme: 'abcdef',
                    lineNumbers: true,
                    autoFocus: true,
                    matchBrackets: true,
                    autoCloseBrackets: true,
                    matchTags: { bothTags: true },
                    autoCloseTags: true,
                    indentWithTabs: true,
                    smartIndent: false
                });
            }
            this.editFileDialog.editor.setSize('auto', window.innerHeight * 0.6);
        },
        saveFileContent: function() {
            let data = {
                path: this.editFileDialog.file.path,
                content: this.editFileDialog.editor.getValue()
            };
            this.sendPost('/file/saveContent', data, null, this.loading('保存文件中...'), false);
        },
        showImg: function(file) {
            let images = this.imgDialog.images;
            images.splice(0);
            let active = 0;
            for (let i in this.files) {
                if (isImage(this.files[i].name)) { images.push(this.files[i]); }
                if (file.path === this.files[i].path) { active = images.length - 1; }
            }
            if (images.length === 0) {
                this.$message.error('没有图片');
            } else {
                this.imgDialog.initIndex = active;
                this.imgDialog.height = window.innerHeight * 0.8 + 'px';
                this.imgDialog.img.style = {
                    maxHeight: (window.innerHeight * 0.8 - 24) + 'px',
                    maxWidth: '100%'
                };
                this.imgDialog.visible = true;
            }
            this.$nextTick(_ => { this.$refs.images.setActiveItem(active); });
        },
        imageChange: function(index) {
            let img = document.getElementById('img-' + index);
            if (!img.src) {
                img.src = basePath + '/file/download?path=' + encodeURIComponent(this.imgDialog.images[index].path);
            }
        },
        pathMark: function(path) {
            let pathMark = this.getPathMark(path);
            if (pathMark) {
                this.cancelPathMark(pathMark);
            } else {
                this.makePathMark(path);
            }
        },
        makePathMark: function(path) {
            this.openInput({
                title: '添加书签',
                label: '书签名称',
                value: this.file.name,
                confirm: _ => {
                    this.sendPost('/file/makePathMark', { name: this.inputDialog.value, path: path }, this.inputDialog, null, false, true, data => {
                        if (data.success) {
                            this.getUser();
                            if (path === this.path) {
                                this.isMark = true;
                            }
                        }
                    });
                }
            });
        },
        cancelPathMark: function(pathMark) {
            this.openConfirm('移除书签', '确定要移除<' + pathMark.name + '>书签吗？', _ => {
                this.sendPost('/file/cancelPathMark', { path: pathMark.path }, this.confirmDialog, null, false, true, data => {
                    if (data.success) {
                        this.getUser();
                        if (pathMark.path === this.path) {
                            this.isMark = false;
                        }
                    }
                });
            });
        },
        video: function(file) { window.open(basePath + '/file/video?path=' + encodeURIComponent(file.path)); },
        logout: function() { location.href = basePath + '/user/logout' },
        getPathMark: function (path) {
            for (let i in this.user.pathMarks) {
                if (this.user.pathMarks[i].path === path) {
                    return this.user.pathMarks[i];
                }
            }
            return null;
        },
        clearCopyCutCookie: function() {
            delCookie('copyPath');
            delCookie('cutPath');
            delCookie('batchCopyPath');
            delCookie('batchCutPath');
            delCookie('batchNames');
        },
        formatParams: function(params) { return Qs.stringify(params, { arrayFormat: 'repeat' }); },
        sendPost: function (url, data, dialog, loading, refresh = true, showMsg = true, then, error) {
            this.buttonLoading = true;
            axios
                .post(url, this.formatParams(data))
                .then(({data, response}) => {
                    this.buttonLoading = false;
                    if (dialog) dialog.visible = false;
                    if (loading) loading.close();
                    if (data.success) {
                        if (refresh) this.getFiles();
                        if (showMsg) this.$message.success(data.msg);
                    } else {
                        this.$message.error(data.msg);
                    }
                    if (data && then && typeof then === 'function') then(data, response);
                })
                .catch(e => {
                    this.buttonLoading = false;
                    if (dialog) dialog.visible = false;
                    if (loading) loading.close();
                    if (error && typeof error === 'function') error(e);
                });
        }
    },
    watch: {
        files: function(newFiles) {
            this.fileNum = 0;
            this.dirNum = 0;
            this.length = 0;
            for (let i in newFiles) {
                let file = newFiles[i];
                if (file.dir) {
                    ++this.dirNum;
                } else {
                    ++this.fileNum;
                }
                this.length += file.length;
            }
        }
    },
    mounted: function () {
        this.getUser();
        let path = getCookie('path');
        this.getFiles(path ? path : '/');
        if (getCookie('copyPath')
            || getCookie('cutPath')
            || getCookie('batchCopyPath')
            || getCookie('batchCutPath')
            || getCookie('batchNames')) {
            this.pasteShow = true;
        }
        this.beginRefreshCountdown();
    }
});