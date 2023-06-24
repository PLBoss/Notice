package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Controller
public class shareController implements CommunityConstant {


    public static final Logger logger= LoggerFactory.getLogger(shareController.class);
    /*利用kafka生成图片*/

    @Autowired
    private EventProducer eventProducer;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @RequestMapping(path = "/share", method = RequestMethod.GET)
    @ResponseBody
    public String share(String htmlUrl) {

        //生成文件名
        String filename = CommunityUtil.generateUUID();


        //产生事件
        Event event=new Event()
                .setTopic(TOPIC_SHARE)
                .setData("htmlUrl",htmlUrl)
                .setData("filename",filename)
                .setData("suffix",".png");
        eventProducer.fireEvent(event);



        Map<String,Object> map=new HashMap<>();

        map.put("url",domain+contextPath+"/share/image/"+filename);

        return CommunityUtil.getJSONString(0,null,map);

    }

    // 获取长图
    @RequestMapping(path = "/share/image/{fileName}", method = RequestMethod.GET)
    public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse response) {

        if(fileName.isEmpty()){
            throw new IllegalArgumentException("文件名不能为空");
        }

        File file=new File(wkImageStorage+"/"+fileName+".png");
        response.setContentType("image/png");
        try {
            OutputStream outputStream = response.getOutputStream();
            FileInputStream fis=new FileInputStream(file);
            byte[] buffer=new byte[1024];
            int p;

            while((p=fis.read(buffer))!=-1){
                outputStream.write(buffer,0,p);
            }

        } catch (Exception e) {
           logger.error("获取长图失败",e.getMessage());
        }


    }

}
