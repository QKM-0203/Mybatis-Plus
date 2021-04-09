package com.qkm.mybatisplus.POJO;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user")
public class User {

    //主键设置,全局唯一ID,ID_WORKER使用的是雪花算法,如果自己自定义了值,则使用自定义的值
    //不会使用生成的全局的ID,记得要使用包装类,因为long会默认0,会造成生成全局ID失败
    @TableId(type = IdType.ID_WORKER)
    private Long id;

    private String name;
    private int age;
    private String email;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    //乐观锁的版本号实现,定义一个version,
    //每次查询都会查询出version,一旦更新就让version加1,每次更新会检查和之前查询的version和是否一致
    @Version
    private Long version;


}
