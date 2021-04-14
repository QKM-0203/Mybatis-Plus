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
## Redis入门
**Redis 是一个开源（BSD许可）的，内存中的数据结构存储系统，它可以用作数据库、缓存和消息中间件。
它支持多种类型的数据结构，如 字符串（strings）， 散列（hashes）， 列表（lists）， 集合（sets）， 
有序集合（sorted sets） 与范围查询， bitmaps， hyperloglogs 和 地理空间（geospatial） 索引半径查询。 
Redis 内置了 复制（replication），LUA脚本（Lua scripting）， LRU驱动事件（LRU eviction），
事务（transactions） 和不同级别的 磁盘持久化（persistence）， 并通过 Redis哨兵（Sentinel）和
自动 分区（Cluster）提供高可用性（high availability）。**
## Redis的作用
**内存数据存储,持久化,效率高用于高速缓.**
## 连接本机redis
```java
redis-cli -p 6379 
```
## 关闭连接
```java
shutdown
```
**redis有16个数据库,默认使第0个**
## 切换数据库
```java
select 数字
```
## 数据库大小
```java
dbsize
```
## 清空数据
```java
flushdb
```
## 清空所有数据库的数据
```java
flushall
```
## key是否存在
```java
exists key
```
## 查询所有的key
```java
key *
```
## 移除key
```java
move key 数据库数字
```
##查询key的类型
```java
type key
```
## 设置过期时间
```java
expire key 时间
```
## 查询剩余时间
```java
ttl key
```
## redis是单线程
**读取数据的效率是cpu>内存>磁盘,在cpu中存取会出现多线程.虽然多线程会增加效率.但是cpu的调度会影响效率,
而redis相当于在内存中存取,没有上下文切换,效率就是最快的.**
## redis的五大基本数据类型
### String
**追加字符串**
```java
append  key "string"(如何key不存在,就相当于set key)
```
**字符串长度**
```java
strlen key
```
**自增1**
```java
incr key
```
**自减1**
```java
decr key
```
**自增自减指定步长**
```java
incrby key 数字
decrby key 数字
```
**截取[闭区间],替换[闭区间]**
```java
getrange key 0 3 (0,1,2,3)   (0,-1)就是全部的值
setrange key 0 xx (就会把从0开始的两个字符换成xx,后面的不变)
```
**设置一个过期时间的key**
```java
setex key 时间 value
```
**如果没有key就创建,否则创建失败**
```java
setnx key value
```
**批量创建key和获取key**
```java
mset k1 v1 k2 v2
mget k1 k2
```
**创建对象**
```java
方式1: set user:1 {name:qkm,age:18}   key就是user:1 value就是{name:qkm,age:18} 
方式2: mset user:1:name qkm user:1:age 18   key就是user:1:name ,user:1:age value 就是qkm,18
```
**组合使用**
```java
getset  先get在set 如果get不到就不输出结果同时set,第二次在get就会获取到
```
### List(都以l开头)
**list可以实现栈,队列,阻塞队列**
**从左边开始放值**
```java
lpush list one(以空格分开可以放多个值)
lpush list two
//查询list中所有的值    
lrange list 0 -1  // two one
```
**从右边开始放值**
```java
rpush list four
rpush list five
lrange list 0 -1 // teo one four five
```
**从左边/右边弹出值**
```java
lpop list 
rpop list
```
**获取指定下标的值**
```java
lindex list 0
```
**获取list长度**
```java
llen list
```
**移除指定的值**
```java
lrem list 数字 one   移除几个one
```
**截取list**
```java
ltrim list 1 2 只截取list的1和2
```
**将最后一个元素pop出去在push到另一个集合中**
```java
rpoplpush list mylist list是原先的集合,mylist是另外的一个集合
```
**替换集合中的某个值**
```java
lset list 0 itm  替换集合中第0个位置为itm,但前提必须是集合存在且有第0个元素,否则会报错
```
**插入值**
```java
linsert  list  before/after value newvalue    在某个值的前还是后插入一个值
```
### Set(都是以S开头)
**增加值**
```java
sadd set one
```
**获取所有的set值**
```java
smembers set
```
**获取set的元素个数**
```java
scard set
```
**获取是否有当前的元素**
```java
sismember set  one
```
**移除指定的值**
```java
srem set one
```
**移除随机的set中的值**
```java
spop set
```
**获取set中随机的多少个值**
```java
srandmembers set 数字
```
**移动指定的值到指定的集合**
```java
smove set myset one 将set中的one移动到myset 
```
**集合的并集**
```java
sunion set1 set2
```
**集合的差集**
```java
sdiff set1 set2
```
**集合的交集**
```java
sinter set1 set2
```
### Hash(哈希)
**key-map的形式**
**给hash中添加一个key-value**
```java
hset hash f1 v1
```
**设置多个键值对**
```java
hmset hash f2 v2 f3 v3
```
**获取hash中的key的value**
```java
hget hash f1   获取多个同样是加m
```
**获取hash中全部的key-value**
```java
hgetall hash
```
**删除hash中指定的key-value**
```java
hdel hash f1
```
**hash的长度**
```java
hlen hash
```
**判断hash是否存在指定的键**
```java
hexists hash f1
```
**获取hash中指定的键**
```java
hkeys hash
```
**获取hash中所有的值**
```java
hvals hash
```
**如果hash不存在指定的key-value就创建**
 ```java
hsetnx hash key value
```
**自增某个key对应的value**
```java
hincrby hash key 数字
```
### Zset(有序集合,就是在之前的set添加一个score属性)
**添加数据**
```java
zadd zset 1 qkm
```
**获取指定区间的数据**
```java
zrangebyscore zset -inf +inf withsorce(附score) 也可以是排序,也可以是显示全部的元素
```
**获取指定区间的索引下的元素**
```java
zrange zset 0 1 withsorce(附score)   这个有序集合默认添加进去的元素默认按照score排序递增,获取的是下标0和1号的对应元素
zrevrange是降序降序查找区间就得从大到小了,区间索引不需要
```
**移除元素**
```java
zrem zset qkm
```
**获取集合个数**
```java
zcard zset
```
**某个区间有多少个值**
```java
zcount zset -1 2   就是看-1到2这个区间中你的集合有多少个值
```
## 三种特殊的数据结构
### geospatial
**可以用于查找附近的的人,两地的距离**
**增添信息 key -纬度 -经度 名称**
```java
geoadd china:city  116.40 39.90 beijing 121.47 31.23 shanghai
        106.50 29.53 chongqing  114.05 22.52 shenzhen 120.16 30.24 hangzhou 
        108.96 34.26 xian
```
**获取当前城市的维度经度**
```java
geopos chins:city xian chongqing   查询多个城市
```
**查询两地的距离**
```java
geodist china:city xian beijing km   //910.0565
```
**查看附近的人(必须获取附近的人的定位)**
```java
//georadius 就是以一个指定的经纬度然后方圆多少km的地方
georadius china:city 110 30 500 km withcoord(带上经度纬度) count 3 查询指定的数量
```
**查找指定城市周围多少km的城市**
```java
georadiusbymember china:city beijing 500 km
```
**geohash 将经纬度转换成字符串**
```java
geohash china:city xian   //wqj6zky6bn0
```
**geo的底层实现原理是由Zset实现的,所以他的命令也适用于geo,zrange china:city 就可以查看里面有多少个元素**
### Hyperloglog
**用作基数统计的算法,基数就是一个集合中不重复的数据**
**优点:占用的内存是固定的,2的64次方的数据只需要12kb的内存**
**增加元素**
```java
pf add mykey a b c d d
pf add mykey1 a v f e d d
```
**统计mykey中的基数数量(没有重复的数量)**
```java
pfcount mykey   // 4 
```
**和并两个pf**
```java
pfmerge mykey3 mykey1 mykey  //a b c d v f e(同样和并之后没有重复的数量)
```
**当允许有容错出现时可以使用,可以统计哪个用户访问网站,重复也没关系因为技术就不算,set就比较耗内存**
### Bitmaps
**位存储 只有0 1 表示**
**使用位存储打卡记录**
```java
0没卡,1打卡
setbit sign 1 0
setbit sign 2 1
setbit sign 3 1
setbit sign 4 0
```
**看哪天是否打卡**
```java
getbit sign  4 //1
```
**统计打卡的天数**
```java
bitcount sign  //2
```










