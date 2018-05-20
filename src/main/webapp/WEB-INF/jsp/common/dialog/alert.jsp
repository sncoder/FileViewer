<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<el-dialog :title="alertDialog.title" :visible.sync="alertDialog.visible" width="350px">
    <span style="font-size: 20px"><i class="el-icon-warning" style="color: red"></i> {{ alertDialog.msg }}</span>
    <span slot="footer">
        <el-button type="primary" @click="alertDialog.visible = false">确 定</el-button>
    </span>
</el-dialog>