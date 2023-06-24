package com.nowcoder.community.service;


import com.nowcoder.community.util.RedisKeyUtil;
import javafx.scene.input.DataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DateService {

    @Autowired
    private RedisTemplate redisTemplate;


    private SimpleDateFormat df=new SimpleDateFormat("yyyyMMdd");


    //更具ip统计
    public void recordUv(String  ip){
        String rediskey = RedisKeyUtil.getUVKey(df.format(new Date()));

        redisTemplate.opsForHyperLogLog().add(rediskey,ip);

    }

    //查询一段时间内的访问量

    public long getUv(Date startDate,Date endDate){
        if(startDate==null||endDate==null){
            throw  new IllegalArgumentException("参数不能为空");
        }


        //查询一段时间按内的key
        List<String> list=new ArrayList<>();
        Calendar calendar= Calendar.getInstance();
        calendar.setTime(startDate);

        while (!calendar.getTime().after(endDate)){
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            list.add(key);
            calendar.add(Calendar.DATE,1);
        }
        String redisKey = RedisKeyUtil.getUVKey(df.format(startDate), df.format(endDate));
        redisTemplate.opsForHyperLogLog().union(redisKey,list.toArray());


        return  redisTemplate.opsForHyperLogLog().size(redisKey);


    }

    //根据userId,统计日活跃
    public void recordDau(int  userId){
        String rediskey = RedisKeyUtil.getDAUKey(df.format(new Date()));

        redisTemplate.opsForValue().setBit(rediskey,userId,true);

    }
    //获取多日日活跃:只要在一段时间内活跃都算活跃
    public long getDau(Date startDate,Date endDate){
        if(startDate==null||endDate==null){
            throw  new IllegalArgumentException("参数不能为空");
        }


        //查询一段时间按内的key
        List<String> list=new ArrayList<>();
        Calendar calendar= Calendar.getInstance();
        calendar.setTime(startDate);

        while(!calendar.getTime().after(endDate)){
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            list.add(key);
            calendar.add(Calendar.DATE,1);
        }



        return  (long)redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                //何必并作or运算

                String rediskey = RedisKeyUtil.getDAUKey(df.format(startDate), df.format(endDate));
                connection.bitOp(RedisStringCommands.BitOperation.OR,rediskey.getBytes(),list.toArray(new byte[0][0]));

                return connection.bitCount(rediskey.getBytes());

            }
        });


    }



}
