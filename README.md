# spring boot下使用 Mybatis-Plus
## 导入依赖
```java
 <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.3.0</version>
</dependency>
```
## 定义User类
```java
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
    @TableId(type = IdType.ASSIGN_ID)
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


    //逻辑删除,好像命名必须是delete,就是定义一个标志然后没有从数据库表中真正删除,在查询的时候判断一下就行,要在配置文件中配置,默认0没有删除,1是删除了
    @TableLogic
    private Integer deleted;


}

```
## 定义Mapper接口继承BaseMapper<?>
```java
@Repository
public interface UserMapper extends BaseMapper<User> {

}
```
**加上@Repository注解为了防止编译错误,@MapperScan("com/qkm/mybatisplus/Mapper")这个注解会扫描Mapper接口.BaseMapper<?>
有很多封装好的方法,直接使用就行,大大简化了开发**
### 测试插入
```java
    @Autowired
    private UserMapper userMapper;


    @Test
    public void insert(){
        User user = new User();
        user.setAge(18);
        user.setEmail("1563252248@qq.com");
        user.setName("戚凯萌");
        user.setId(1L);
        userMapper.insert(user);
    }
```
## 分页插件的简单使用
```java
import com.baomidou.mybatisplus.extension.plugins.OptimisticLockerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//扫描Mapper文件
@MapperScan("com/qkm/mybatisplus/Mapper")
public class MybatisConfig {

    //乐观锁注册Bean
    @Bean
    public OptimisticLockerInterceptor optimisticLockerInterceptor() {
        return new OptimisticLockerInterceptor();
    }

    //分页注册Bean
    @Bean
    public PaginationInterceptor paginationInterceptor(){
        return new PaginationInterceptor();
    }
    
}

```
### 测试分页
```java
 @Autowired
 private UserMapper userMapper;

  @Test
    public void Page(){
        //参数为当前页数和当页数量
        Page<User> userPage = new Page<>(1,2);
        //总数
        System.out.println(userPage.getTotal());
        userMapper.selectPage(userPage,null);
        List<User> records = userPage.getRecords();
        records.forEach(System.out::println);

    }
```
## 乐观锁的使用
## 逻辑删除
## 插入和更新字段的设置
## 全局唯一ID
