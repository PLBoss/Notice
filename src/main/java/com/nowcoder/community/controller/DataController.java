package com.nowcoder.community.controller;

import com.nowcoder.community.service.DateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller

public class DataController {

    @Autowired
    private DateService dateService;

    @RequestMapping(value = "/data",method ={RequestMethod.GET,RequestMethod.POST})
    public String getDataPage(){

        return "/site/admin/data";
    }
    //查独立访客
    @RequestMapping(value = "/data/getUV",method = RequestMethod.POST)
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                        @DateTimeFormat(pattern = "yyyy-MM-dd")  Date endDate, Model model){

        System.out.println(startDate+""+endDate);
        long uv = dateService.getUv(startDate, endDate);

        model.addAttribute("uvStartDate",startDate);
        model.addAttribute("uvEndDate",endDate);
        model.addAttribute("uvResult",uv);

        return "forward:/data";
    }

    //查活跃度
    @RequestMapping(value = "/data/getDAU",method = RequestMethod.POST)
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                        @DateTimeFormat(pattern = "yyyy-MM-dd")  Date endDate, Model model){

        long dau = dateService.getDau(startDate, endDate);
        model.addAttribute("dauStartDate",startDate);
        model.addAttribute("dauEndDate",endDate);
        model.addAttribute("dauResult",dau);

        return "forward:/data";
    }


}
