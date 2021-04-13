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
**在配置文件中注入Bean,定义一个属性version加上@Version注解同时在数据库中也加入同样的字段**
### 模拟两个线程同时操作同一条数据
```java
  @Test
    public void updateOptimisticLocker(){
        //模拟线程1
        User user = userMapper.selectById(2L);
        user.setName("孙瑶");//UPDATE user SET name=?, age=?, email=?, update_time=?, version=? WHERE id=? AND version=?


        //模拟线程2
        User user1 = userMapper.selectById(2L);
        user1.setName("杨倩");//UPDATE user SET name=?, age=?, email=?, update_time=?, version=? WHERE id=? AND version=?
        userMapper.updateById(user1);

        userMapper.updateById(user);

        //总结:查询会带上version,判断version和之前是否一致,一致才更新,不一致就不更新,所以user就不能被更新


    }
```
**利用version来判断是否更新数据**
## 逻辑删除
**所谓逻辑删除,就是没有真正的从表中删除,而是利用一个字段来表示是否被删除,但其实还是存在在表中的.**
### 使用
**加入属性deleted,加上注解@TableLogic,表示该字段用来判断是否被删除**
**在配置文件中加入判断被删除的值和没被删除的值**
```java
mybatis-plus:
  global-config:
    db-config:
      logic-delete-value: 1 # 逻辑已删除值(默认为 1)
      logic-not-delete-value: 0 # 逻辑未删除值(默认为 0)
```
**这样数据库在操作数据的时候就会判断是否该数据存在,相当于加上And deleted = 0**
## 插入和更新字段的设置
**在建立一个表的的时候往往会加入两个属性,创建时间和更新时间,用来记录数据的插入和更新**
### 使用
**加入字段,同时加上注解,该注解相当于为该属性填充一个性质,createTime注解的含义是当有数据插入时,就会自动更新时间,后面的是属性的注解
意味着更新和插入都会更新时间**
```java
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
```
**需要自己处理器**
```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    //表示该方法会用在更新和插入的两个自定义属性上,时间可以i自定义格式化
    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName("createTime",new Date(),metaObject);
        this.setFieldValByName("updateTime",new Date(),metaObject);
    }

    
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime",new Date(),metaObject);
    }
}

```
**做好上面步骤之后每当你插入或更新数据的时候这两个自段也会随之更新**
## 全局唯一ID
**使用注解@TableId(type = IdType.ASSIGN_ID)**
**IdType有很多的字段,该字段为全局唯一Id,使用雪花算法实现,当你自定义值时就不会自动生成Id了.**


