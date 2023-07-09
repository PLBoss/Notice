

# 橙知道
## 项目描述
这是一个用于解决校园内消息逐级人工传递问题的消息通知系统，能让任何班级的学生能在第一时间看到消息，并且确认消息，还可以讨论消息

## 主要功能

注册与登陆，通知发布，通知详情，评论，点赞，关注，全局内容搜索，权限管理，后台数据统计，热度排行

## 核心功能的具体实现

- 通过对登录用户颁发登录凭证(token)，将登陆凭证存进 Redis 中来记录登录用户登录状态，使用拦截器进行登录状态检查，使用 Spring Security 实现权限控制，解决了 Http 无状态带来的缺陷，保护需登录或权限才能使用的特定资源。

- 使用 ThreadLocal 在当前线程中存储用户数据，代替 session 的功能便于分布式部署。在拦截器的 preHandle 中存储用户数据并构建用户认证的结果存入 SecurityContext，在 postHandle 中将用户数据存入 Model，在 afterCompletion 中清理用户数据。

- 使用 Redis 的集合数据类型来解决点赞、相互关注功能，采用事务管理，保证数据的正确，采用“先更新数据库，再删除缓存”策略保证数据库与缓存数据的一致性。采用 Redis 存储验证码，解决性能问题和分布式部署时的验证码需求。采用 Redis 的 HyperLogLog 存储每日 UV、Bitmap 存储 DAU，实现网站数据统计的需求。

- 使用 Kafka 作为消息队列，在用户被点赞、评论、关注后以系统通知的方式推送给用户，用户发布或删除帖子后向 elasticsearch 同步，wk 生成长图后将长图上传至云服务器，对系统进行解耦、削峰。

- 使用 elasticsearch + ik 分词插件实现全局搜索功能，当用户发布、修改或删除帖子时，使用 Kafka 消息队列去异步将帖子给 elasticsearch 同步。

- 使用分布式定时任务 Quartz 定时计算帖子分数，来实现热帖排行的业务功能。

- 对频繁需要访问的数据，如用户信息、帖子总数、热帖的单页帖子列表，使用 Caffeine 本地缓存 + Redis 分布式缓存的多级缓存，提高服务器性能，实现系统的高可用。

  

## 部分页面展示

通知展示主页

![](https://plboss-imges.oss-cn-chengdu.aliyuncs.com/master/20230624152233.png)

搜索

![image-20230624153447360](https://plboss-imges.oss-cn-chengdu.aliyuncs.com/master/image-20230624153447360.png)

登陆页面

![](https://plboss-imges.oss-cn-chengdu.aliyuncs.com/master/20230624152304.png)

注册页面

![](https://plboss-imges.oss-cn-chengdu.aliyuncs.com/master/20230624152320.png)

设置页面

![](https://plboss-imges.oss-cn-chengdu.aliyuncs.com/master/20230624152443.png)

通知页面

![](https://plboss-imges.oss-cn-chengdu.aliyuncs.com/master/20230624152407.png)

![image-20230709224506169](https://plboss-imges.oss-cn-chengdu.aliyuncs.com/master/image-20230709224506169.png)

