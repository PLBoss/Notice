package com.nowcoder.community.dao;

import org.springframework.stereotype.Repository;

@Repository("dapImpl")//将bean重命名
public class testDaoImpl implements testDao {
    @Override
    public String select() {

        return "dao..........";
    }
}
