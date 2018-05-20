function addCopyByClassName(className) {
    let clipboard = new Clipboard('.' + className);
    clipboard.on("success", function () {
        Vue.prototype.$message.success('复制成功');
    });
    clipboard.on("error", function () {
        Vue.prototype.$message.error('复制失败，请手动复制');
    });
}

addCopyByClassName("clipCopy");