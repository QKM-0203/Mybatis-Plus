package com.qkm.mybatisplus;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qkm.mybatisplus.Mapper.UserMapper;
import com.qkm.mybatisplus.POJO.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@SpringBootTest
class MybatisPlusApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    void contextLoads() {
        System.out.println(userMapper.selectList(null));
    }

    @Test
    public void insert(){
        User user = new User();
        user.setAge(18);
        user.setEmail("1563252248@qq.com");
        user.setName("戚凯萌");
        user.setId(1L);
        userMapper.insert(user);
    }





    @Test
    public void update(){
        User user = new User();

        user.setAge(20);
        user.setEmail("1563252248@qq.com");
        user.setName("戚凯萌");
        user.setId(1L);
    }


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


    @Test
    public void select(){
        //查询多个ID通过list(数组转list)来查询
        //SELECT id,name,age,email,create_time,update_time,version FROM user WHERE id IN ( ? , ? , ? , ? )
        List<User> users = userMapper.selectBatchIds(Arrays.asList(1L, 2L, 3L, 4L));

        //自定义查询条件
        HashMap<String, Object> map = new HashMap<>();
        map.put("name","戚凯萌");
        map.put("age","18");
        map.put("deleted","1");
        map.put("version",1);

        //SELECT id,name,age,email,create_time,update_time,version FROM user WHERE name = ? AND age = ?
        List<User> users1 = userMapper.selectByMap(map);
        System.out.println(users1);

    }


    //分页
    @Test
    public void Page(){
        //参数为第当前页数,当页数量
        Page<User> userPage = new Page<>(1,2,true);
        //总数
        System.out.println(userPage.getTotal());
        userMapper.selectPage(userPage,null);
        List<User> records = userPage.getRecords();
        records.forEach(System.out::println);
    }


    @Test
    public void delete(){
        int i = userMapper.deleteById(3);
        System.out.println(userMapper.selectById(3));

    }


    @Test
    public void wapperTest(){
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper
                .eq("name","杨倩")
                .ge("age",10)
                .isNotNull("email");
        userMapper.selectList(userQueryWrapper).forEach(System.out::println);
    }

}
