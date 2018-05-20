let ONE_KB = 1024;
let ONE_MB = ONE_KB * 1024;
let ONE_GB = ONE_MB * 1024;
let ONE_TB = ONE_GB * 1024;
let ONE_PB = ONE_TB * 1024;

let chineseCharRegex = /[\u4E00-\u9FA5\uF900-\uFA2D]/;
let exts = ['folder', 'folder-unempty', 'sql', 'c', 'cpp', 'cs', 'flv', 'css', 'js', 'htm', 'html', 'java', 'log', 'mht', 'php', 'url', 'xml', 'ai', 'bmp', 'cdr', 'gif', 'ico', 'jpeg', 'jpg', 'JPG', 'png', 'psd', 'webp', 'ape', 'avi', 'flv', 'mkv', 'mov', 'mp3', 'mp4', 'mpeg', 'mpg', 'rm', 'rmvb', 'swf', 'wav', 'webm', 'wma', 'wmv', 'rtf', 'docx', 'fdf', 'potm', 'pptx', 'txt', 'xlsb', 'xlsx', '7z', 'cab', 'iso', 'rar', 'zip', 'gz', 'bt', 'file', 'apk', 'bookfolder', 'folder', 'folder-empty', 'folder-unempty', 'fromchromefolder', 'documentfolder', 'fromphonefolder', 'mix', 'musicfolder', 'picturefolder', 'videofolder', 'sefolder', 'access', 'mdb', 'accdb', 'sql', 'c', 'cpp', 'cs', 'js', 'fla', 'flv', 'htm', 'html', 'java', 'log', 'mht', 'php', 'url', 'xml', 'ai', 'bmp', 'cdr', 'gif', 'ico', 'jpeg', 'jpg', 'JPG', 'png', 'psd', 'webp', 'ape', 'avi', 'flv', 'mkv', 'mov', 'mp3', 'mp4', 'mpeg', 'mpg', 'rm', 'rmvb', 'swf', 'wav', 'webm', 'wma', 'wmv', 'doc', 'docm', 'dotx', 'dotm', 'dot', 'rtf', 'docx', 'pdf', 'fdf', 'ppt', 'pptm', 'pot', 'potm', 'pptx', 'txt', 'xls', 'csv', 'xlsm', 'xlsb', 'xlsx', '7z', 'gz', 'cab', 'iso', 'rar', 'zip', 'bt', 'file', 'apk', 'css'];

function sizeFormat(size) {
    if (size < ONE_KB) {
        return size + " B";
    } else if (size < ONE_MB) {
        return (size / ONE_KB).toFixed(2) + " KB";
    } else if (size < ONE_GB) {
        return (size / ONE_MB).toFixed(2) + " MB";
    } else if (size < ONE_TB) {
        return (size / ONE_GB).toFixed(2) + " GB";
    } else if (size < ONE_PB) {
        return (size / ONE_TB).toFixed(2) + " TB";
    } else {
        return (size / ONE_PB).toFixed(2) + " PB";
    }
}

function isChineseChar(ch) {
    return chineseCharRegex.test(ch);
}

function chineseCharNum(str) {
    let num = 0;
    for (let i in str) {
        if (isChineseChar(str[i])) {
            ++num;
        }
    }
    return num;
}

function cutName(name, maxLength = 100) {
    let index = 0;
    let num = 0;
    for (let i in name) {
        if (num >= maxLength) {
            break;
        } else if (isChineseChar(name[i])) {
            num += 2;
        } else {
            ++num;
        }
        ++index;
    }
    return index === name.length ? name : (name.substring(0, index) + "...");
}

function getExtName(fileName) {
    let extArr = fileName.split(".");
    let extLastName = extArr[extArr.length - 1];
    for (let i = 0; i < exts.length; i++) {
        if (exts[i] === extLastName) {
            return exts[i];
        }
    }
    return 'file';
}

//是否压缩文件
function isZip(fileName) {
    let exts = ['zip', 'gz', 'tgz', 'rar', 'tar', '7z'];
    return isExts(fileName, exts);
}

//是否文本文件
function isText(fileName) {
    let exts = ['rar', 'zip', 'tar.gz', 'gz', 'iso', 'xsl', 'doc', 'xdoc', 'jpeg', 'jpg', 'png', 'gif', 'bmp', 'tiff', 'exe', 'so', '7z', 'bz'];
    return !isExts(fileName, exts);
}

//是否图片文件
function isImage(fileName) {
    let exts = ['jpg', 'jpeg', 'png', 'bmp', 'gif', 'tiff', 'ico'];
    return isExts(fileName, exts);
}

function isVideo(fileName) {
    let exts = ["mkv", "mp4", "rmvb", "rm", "avi", "mov", "wmv"];
    return isExts(fileName, exts);
}

//是否为指定扩展名
function isExts(fileName, exts) {
    let ext = fileName.split('.');
    if (ext.length < 2) return false;
    let extName = ext[ext.length - 1].toLowerCase();
    for (let i = 0; i < exts.length; i++) {
        if (extName === exts[i]) return true;
    }
    return false;
}

Vue.prototype.sizeFormat = Vue.sizeFormat = sizeFormat;
Vue.prototype.cutName = Vue.cutName = cutName;
Vue.prototype.isZip = Vue.isZip = isZip;
Vue.prototype.isText = Vue.isText = isText;
Vue.prototype.isImage = Vue.isImage = isImage;
Vue.prototype.isVideo = Vue.isVideo = isVideo;