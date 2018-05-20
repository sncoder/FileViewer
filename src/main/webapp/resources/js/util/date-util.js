let oneMinute = 1000 * 60;
let oneHour = oneMinute * 60;
let oneDay = oneHour * 24;
let oneWeek = oneDay * 7;
let oneMonth = oneDay * 30;

Date.prototype.minusDays = function (days) {
    let time = this.getTime() - (oneDay * days);
    let newDate = new Date();
    newDate.setTime(time);
    return newDate;
};

Date.prototype.plusDays = function (days) {
    let time = this.getTime() + (oneDay * days);
    let newDate = new Date();
    newDate.setTime(time);
    return newDate;
};

Date.prototype.setMinTime = function () {
    this.setHours(0);
    this.setMinutes(0);
    this.setSeconds(0);
    this.setMilliseconds(0);
};

Date.prototype.setMaxTime = function () {
    this.setHours(23);
    this.setMinutes(59);
    this.setSeconds(59);
    this.setMilliseconds(999);
};

function addZero(num) {
    if (num < 10) {
        return "0" + num;
    } else {
        return num;
    }
}

Date.prototype.formatDate = function () {
    return this.getFullYear() + "-" + addZero(this.getMonth() + 1) + "-" + addZero(this.getDate());
};

Date.prototype.formatTime = function () {
    return addZero(this.getHours()) + ":" + addZero(this.getMinutes()) + ":" + addZero(this.getSeconds());
};

Date.prototype.formatDateTime = function (split) {
    return this.formatDate() + (!split ? " " : split) + this.formatTime();
};