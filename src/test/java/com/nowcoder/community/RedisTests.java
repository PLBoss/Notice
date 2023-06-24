package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.CORBA.TIMEOUT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings() {
        String redisKey = "test:count";

        redisTemplate.opsForValue().set(redisKey, 1);

        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    //操作哈希对象
    @Test
    public void testhashes(){
        String redisKey="test:user";

        //存入值
        redisTemplate.opsForHash().put(redisKey,"id",1);
        redisTemplate.opsForHash().put(redisKey,"name","张三");

        //取出值
        System.out.println(redisTemplate.opsForHash().get(redisKey,"id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey,"name"));

    }

    //操做集合对象
    @Test
    public void testSets(){
        String redisKey="test:teachers";
        redisTemplate.opsForSet().add(redisKey,"刘备","关羽","张飞","赵云");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));

    }

    //操作有序集合
    @Test
    public  void testSortedSets(){
        String redisKey="test:students";
        redisTemplate.opsForZSet().add(redisKey,"八戒",34);
        redisTemplate.opsForZSet().add(redisKey,"沙师弟",54);
        redisTemplate.opsForZSet().add(redisKey,"悟空",64);


        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));//集合的长度
        System.out.println(redisTemplate.opsForZSet().score(redisKey,"八戒"));
        System.out.println(redisTemplate.opsForZSet().rank(redisKey,"八戒"));
        System.out.println(redisTemplate.opsForZSet().range(redisKey,0,2));
    }

    @Test
    public  void testKeys(){
        redisTemplate.delete("test:count");
        System.out.println(redisTemplate.hasKey("test:count"));
        redisTemplate.expire("test:students",10, TimeUnit.SECONDS);
    }

    //编程式事务的实现
    @Test
    public  void testTransaction(){
        Object result=redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey="tex:tx";

                //启用事务
                operations.multi();
                operations.opsForSet().add(redisKey,"zhangsan");
                operations.opsForSet().add(redisKey,"lisi");
                operations.opsForSet().add(redisKey,"wangwu");

                System.out.println(redisTemplate.opsForSet().members(redisKey));

                //提交事务
                return operations.exec();



            }
        });

        System.out.println(result);
    }

    //hperLog

    @Test
    public void  testHyperLog(){

        String redisKey="test:01";

        for (int i = 0; i < 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey,i);
        }

        for (int i = 0; i < 10000; i++) {
            int p= (int) (Math.random()*10000+1);
            redisTemplate.opsForHyperLogLog().add(redisKey,p);
        }
        Long size = redisTemplate.opsForHyperLogLog().size(redisKey);
        //统计不重复的数据
        System.out.println(size);
    }

    @Test
    public  void hyLogUnion (){
        String redisKey1="test:02";

        for (int i = 0; i < 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey1,i);
        }

        String redisKey2="test:03";

        for (int i = 5000; i < 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2,i);
        }

        String redisKey3="test:04";

        for (int i = 10000; i < 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3,i);
        }

        String unikey="test:unikey";
        //合并数据
        redisTemplate.opsForHyperLogLog().union(unikey,redisKey1, redisKey2, redisKey3);

        Long size = redisTemplate.opsForHyperLogLog().size(unikey);

        //统计不重复的数据
        System.out.println(size);


    }


//    bitmap

    @Test

    public  void bitmap(){

        String key="test:bitmap";

        redisTemplate.opsForValue().setBit(key,1,true);
        redisTemplate.opsForValue().setBit(key,3,true);
        redisTemplate.opsForValue().setBit(key,5,true);

        System.out.println(redisTemplate.opsForValue().getBit(key,1));

        //统计true的个数
        Object execute = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return redisConnection.bitCount(key.getBytes());
            }
        });

        System.out.println(execute);

    }

    //bitmap做位运算

    @Test
    public  void ops(){

        String key1="test:bitmap1";

        redisTemplate.opsForValue().setBit(key1,1,true);
        redisTemplate.opsForValue().setBit(key1,2,true);
        redisTemplate.opsForValue().setBit(key1,3,true);

        String key2="test:bitmap2";
        redisTemplate.opsForValue().setBit(key2,4,true);
        redisTemplate.opsForValue().setBit(key2,5,true);
        redisTemplate.opsForValue().setBit(key2,6,true);

        String key3="test:bitmap1";
        redisTemplate.opsForValue().setBit(key3,7,true);
        redisTemplate.opsForValue().setBit(key3,8,true);
        redisTemplate.opsForValue().setBit(key3,9,true);


        String redis_key="text:bitmap:or";

        Object execute = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR, redis_key.getBytes(), key1.getBytes(), key2.getBytes(), key3.getBytes());

                return redisConnection.bitCount(redis_key.getBytes());
            }
        });

        System.out.println(redisTemplate.opsForValue().getBit(redis_key,1));
        System.out.println(redisTemplate.opsForValue().getBit(redis_key,2));
        System.out.println(redisTemplate.opsForValue().getBit(redis_key,3));
        System.out.println(redisTemplate.opsForValue().getBit(redis_key,4));

        System.out.println(execute);




    }



}
