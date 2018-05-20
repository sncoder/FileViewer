let app = new Vue({
    el: '#app',
    data: {
        alertDialog: { visible: false },
        buttonLoading: false,
        user: {}
    },
    methods: {
        login: function () {
            this.buttonLoading = true;
            axios.post('/user/login', Qs.stringify(this.user))
                .then(({data}) => {
                    if (!data) { this.buttonLoading = false; return; }
                    if (data.success) {
                        location.href = basePath + '/file/index';
                    } else {
                        this.buttonLoading = false;
                        this.openAlert('错误', data.msg);
                    }
                });
        }
    }
});