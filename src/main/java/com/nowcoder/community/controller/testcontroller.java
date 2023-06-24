package com.nowcoder.community.controller;

import com.nowcoder.community.service.alphaservice;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/test")

public class testcontroller {
    @RequestMapping("/say")
    @ResponseBody
    public  String say(){
        return  "hello springbot";
    }
    @Autowired
    private alphaservice alphaservice;
    /*调用service*/
    @RequestMapping("/data")
    public @ResponseBody String getData(){
        return alphaservice.find();
    }

    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response) {
        // 获取请求数据
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name + ": " + value);
        }
        System.out.println(request.getParameter("code"));

        // 返回响应数据
        response.setContentType("text/html;charset=utf-8");
        try (
                //自动关闭流
                PrintWriter writer = response.getWriter();
        ) {
            writer.write("<h1>牛客网</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*获取传入的参数第一种只方式*/
    /*/student?age=32&name=boo*/
    @RequestMapping("/students")
    public @ResponseBody String student(
            @RequestParam(name = "age",required = false,defaultValue = "1") int age,
            @RequestParam(name = "name",required = false,defaultValue = "null") String name){
        System.out.println(age);
        System.out.println(name);
        return "some student";


    }
    /*获取传入的参数第二种只方式*/
    /*/student/32/boo/

     */
    @RequestMapping("/student/{age}/{name}")
    public @ResponseBody String student2(
           @PathVariable("age") int age,
            @PathVariable("name") String name){
        System.out.println(age);
        System.out.println(name);
        return "some student";


    }

    /*处理post的请求*/
    @RequestMapping(value = "/student",method = RequestMethod.POST)
    public @ResponseBody String student3(
            @RequestParam(name = "age",required = false,defaultValue = "1") int age,
            @RequestParam(name = "name",required = false,defaultValue = "null") String name){
        System.out.println(age);
        System.out.println(name);
        return "sucessce";


    }

    /*响应html*/

    @RequestMapping(value = "/teacher",method = RequestMethod.GET)
public ModelAndView getstudent(){
        ModelAndView model=new ModelAndView();
        model.addObject("name","zhangsang");
        model.addObject("age",30);
        model.setViewName("/demo/view");//templeft下的view模板

        return  model;
    }

    @RequestMapping(path = "/teacher1", method = RequestMethod.GET)
    public ModelAndView getTeacher() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("name", "张三");
        mav.addObject("age", 30);
        mav.setViewName("demo/view");
        return mav;
    }

    @RequestMapping(path = "/school", method = RequestMethod.GET)
    public String getSchool(Model model) {
        model.addAttribute("name", "北京大学");
        model.addAttribute("age", 80);
        return "/demo/view";
    }

    /*响应json*/
    @RequestMapping(path = "/emp", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getEmp() {
        Map<String, Object> emp = new HashMap<>();
        emp.put("name", "张三");
        emp.put("age", 23);
        emp.put("salary", 8000.00);
        return emp;
    }

    @RequestMapping(path = "/emps", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getEmps() {
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> emp = new HashMap<>();
        emp.put("name", "张三");
        emp.put("age", 23);
        emp.put("salary", 8000.00);
        list.add(emp);

        emp = new HashMap<>();
        emp.put("name", "李四");
        emp.put("age", 24);
        emp.put("salary", 9000.00);
        list.add(emp);

        emp = new HashMap<>();
        emp.put("name", "王五");
        emp.put("age", 25);
        emp.put("salary", 10000.00);
        list.add(emp);

        return list;
    }

    // cookie示例

    @RequestMapping(path = "/cookie/set", method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response) {
        // 创建cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        // 设置cookie生效的范围
        cookie.setPath("/community/test");
        // 设置cookie的生存时间
        cookie.setMaxAge(60 * 10);
        // 发送cookie
        response.addCookie(cookie);

        return "set cookie";
    }

    @RequestMapping(path = "/cookie/get", method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(@CookieValue("code") String code) {
        System.out.println(code);
        return "get cookie";
    }
    // ajax示例
    @RequestMapping(path = "/ajax", method = RequestMethod.POST)
    @ResponseBody
    public String testAjax(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return CommunityUtil.getJSONString(0, "操作成功!");
    }



}
