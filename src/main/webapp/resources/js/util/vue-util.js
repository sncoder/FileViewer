Vue.prototype.copyArr = function (dest, src) {
    dest.splice(0);
    for (let i in src) {
        dest.push(src[i]);
    }
};

Vue.prototype.openConfirm = function (title = '', msg = '', confirm = _ => {}) {
    this.confirmDialog.title = title;
    this.confirmDialog.msg = msg;
    this.confirmDialog.confirm = confirm;
    this.confirmDialog.visible = true;
};

Vue.prototype.openInput = function (option = {}) {
    this.inputDialog.title = option.title === undefined ? '' : option.title;
    this.inputDialog.label = option.label === undefined ? '' : option.label;
    this.inputDialog.buttonLabel = option.buttonLabel === undefined ? '确定' : option.buttonLabel;
    this.inputDialog.type = option.type === undefined ? 'text' : option.type;
    this.inputDialog.value = option.value === undefined ? '': option.value;
    this.inputDialog.confirm = option.confirm === undefined ? _ => {} : _ => { if (!this.buttonLoading) { option.confirm(); } };
    this.inputDialog.visible = true;
};

Vue.prototype.openAlert = function (title = '', msg = '') {
    this.alertDialog.title = title;
    this.alertDialog.msg = msg;
    this.alertDialog.visible = true;
};

Vue.prototype.loading = function (msg) {
    return this.$loading({
        lock: true,
        text: msg ? msg : '操作中...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
    });
};