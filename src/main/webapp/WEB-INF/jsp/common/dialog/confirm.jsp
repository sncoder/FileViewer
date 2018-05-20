<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<el-dialog :title="confirmDialog.title" :visible.sync="confirmDialog.visible" width="300px">
    <span style="font-size: 20px"><i class="el-icon-warning" style="color: red"></i> {{ confirmDialog.msg }}</span>
    <span slot="footer">
        <el-button @click="confirmDialog.visible = false">取 消</el-button>
        <el-button type="primary" @click="confirmDialog.confirm" :loading="buttonLoading">确 定</el-button>
    </span>
</el-dialog>