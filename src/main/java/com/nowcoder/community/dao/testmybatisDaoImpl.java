package com.nowcoder.community.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary//设置优先级
public class testmybatisDaoImpl implements testDao {
    @Override
    public String select() {
        return "mybatis demo...........";
    }
}
