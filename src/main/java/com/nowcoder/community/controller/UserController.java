package com.nowcoder.community.controller;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.*;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController  implements CommunityConstant {
    public static final Logger logger= LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService postService;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${quniu.bucket.header.url}")
    private String headerBucketUrl;

    @LoginRequired
    @RequestMapping(value = "setting",method = RequestMethod.GET)
    public String getSettingPage(Model model){

        //给表单带上凭证，上传到七牛云

        //上传文件名称
        String filename = CommunityUtil.generateUUID();

        //设置文件的响应格式
        StringMap policy=new StringMap();
        policy.put("returnBody",CommunityUtil.getJSONString(0));

        //上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, filename, 3600, policy);

        model.addAttribute("uploadToken",uploadToken);
        model.addAttribute("fileName",filename);

        return "/site/setting";

    }
    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){

        if (headerImage==null) {
            model.addAttribute("error","你还没有选择图片");
            return "/site/setting";
        }

        //获取文件后缀
        String originalFilename = headerImage.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error","文件格式不正确");
            return "/site/setting";
        }
        //生成随机文件名

        String filename=CommunityUtil.generateUUID()+suffix;
        //确定文件的存放路径
        File dest=new File(uploadPath+"/"+filename);

        try {
            //写入数据
            headerImage.transferTo(dest);
        } catch (Exception e) {
            logger.error("上传文件失败"+e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常",e);
        }

        //更新用户头像路径
        User user = hostHolder.getUser();
        String headerUrl=domain+contextPath+"/user/header/"+filename;
        userService.updateHeader(user.getId(),headerUrl);


        return "redirect:/index";



    }

    //读取用户头像,将网路请求地址转换成本地地址进行读取

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }

    @Autowired
    private LikeService likeService;

    @Autowired
    private followService followService;


    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }

        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }

    //我的帖子

    @RequestMapping(value = "/profile/post/{userId}",method = RequestMethod.GET)
    public String getMyPost(@PathVariable("userId") int userId, Model model, Page page){
       page.setRows(postService.findDiscussPostRows(userId));
       page.setPath("/user/profile/post/"+userId);
       page.setLimit(5);

      List<DiscussPost> posts= postService.findDiscussPosts(userId, page.getOffset(), page.getLimit(),0);
      int postcount = postService.findDiscussPostRows(userId);
      List<Map<String, Object>> discussPosts = new ArrayList<>();
      User user = userService.findUserById(userId);
        for (DiscussPost post : posts) {
            Map<String, Object>  map=new HashMap<>();
            map.put("post",post);
            long likecount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
            map.put("likecount",likecount);

            discussPosts.add(map);


        }


      model.addAttribute("discussPosts",discussPosts);
      model.addAttribute("user",user);
      model.addAttribute("postcount",postcount);


      return "/site/my-post";

    }


    //我的回复
    @Autowired
    private CommentService commentService;
    @Autowired
    private DiscussPostService discussPostService;
    @RequestMapping(value = "/profile/reply/{userId}",method = RequestMethod.GET)
    public String getMyreply(@PathVariable("userId") int userId,Model model,Page page){
        page.setRows(commentService.findCountById(userId));
        page.setPath("/user/profile/reply/"+userId);
        page.setLimit(5);

        List<Comment> list = commentService.findCommentsById(userId, page.getOffset(), page.getLimit());
        int replycount=commentService.findCountById(userId);

        List<Map<String,Object>>  comments=new ArrayList<>();
        for (Comment comment : list) {

            Map<String,Object> map=new HashMap<>();

            DiscussPost discussPost = discussPostService.findDiscussPostById(comment.getEntityId());

            
            map.put("discussPost",discussPost);

            map.put("comment",comment);


            comments.add(map);

        }

        User user = userService.findUserById(userId);
        model.addAttribute("user",user);
        model.addAttribute("comments",comments);
        model.addAttribute("replycount",replycount);


        return "/site/my-reply";
    }









}
