axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded; charset=UTF-8';
axios.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

axios.interceptors.request.use(
    config => config,
    error => {
        console.log(error);
        app.openAlert('错误', '发生了一些错误');
        return Promise.reject(error);
    }
);

axios.interceptors.response.use(
    response => {
        return { data: response ? response.data : null, response: response };
    },
    error => {
        console.log(error);
        app.openAlert('错误', '发生了一些错误');
        return Promise.reject(error);
    }
);