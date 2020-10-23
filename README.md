## 项目介绍
- 实现了牛客网社区模块的大部分功能。包括不限于权限管理，帖子，用户私信，回复，点赞，关注，系统通知，社区搜索，热帖排行，用户设置等功能模块
## 项目技术
- 项目整体采用Spring Boot，Spring MVC，Mybatis实现。并且使用Spring Security完成权限控制，使用Elasticsearch实现社区搜索，使用Kafka消息队列实现系统通知等模块，使用Quartz实现了热帖排行使用Caffeine，Redis优化了网站性能。
## 项目亮点
- 对部分模块采用Caffeine+Redis设计多级缓存，通过JMeter压测工具测试出性能提升十分明显。
## 项目访问
- 通过网址进行访问,http://www.codecx.top