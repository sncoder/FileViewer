<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<el-dialog :title="inputDialog.title" :visible.sync="inputDialog.visible" width="300px">
    <el-form label-position="top" @submit.native.prevent>
        <el-form-item :label="inputDialog.label">
            <el-input :type="inputDialog.type" v-model="inputDialog.value" @keydown.enter.native="inputDialog.confirm"></el-input>
        </el-form-item>
        <el-form-item>
            <el-button type="primary" :loading="buttonLoading" @click="inputDialog.confirm">{{ inputDialog.buttonLabel }}</el-button>
            <el-button @click="inputDialog.visible = false">取消</el-button>
        </el-form-item>
    </el-form>
</el-dialog>
