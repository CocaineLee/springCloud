放在最前面，这是在B站学习Springcloud的视频后学习的经验总结，代码都是一个个跟着视频敲进去的，为期一个礼拜，感觉视频对于初步了解springcloud微服务还不错，感谢尚硅谷，视频av号**22613028**，我在实践的过程中也遇到了各种大坑，一起的小伙伴如果有问题可以留言。
# 概述	
在学习springcloud的时候，最重要的是理解微服务的意义及其框架结构。本次学习仅仅是对springCloud进行了一种浅层次的学习，回顾一下学习中的对其框架的应用，基本路线如下：  
1. **maven**多模块项目的结构，是父项目 + n个子模块的模式。此处父项目中只用配置pom文件，在其pom中，会随着子模块的增加`<modules>`内容也会增加，都是子模块的artifactiID。除此之外，与子模块不同的是，父项目需要管理依赖的版本，设置了第三方依赖的GAV，这样子模块使用的时候，只需要指定ga，v自动和父保持一致。
2.  添加api模块，可以将此模块理解为公共部分，没有main，不需要运行，但是需要将其构建，供其他模块使用其定义的类。使用maven的功能，进行clean和install。
3.  注册中心**eureka**（**eurekaServer**）
4.  集群化eureka（eurekaServer）
5.  服务提供者（**eurekaClient**）提供了真实的与数据库的交互，将自己提供的服务注册到eureka
6.  将相同的服务通过不同的端口暴露出去，但是以同样的服务名称在eureka中注册。
6.  服务消费者 （eurekaClient）根据服务名称， 在eureka中找到服务，并调用
7.  在客户端添加负载均衡（**ribbon**）
8.  在服务提供者添加熔断（**hystrix**）
9.  在客户端，使用**feign**整合ribbon和hystrix，
10.  添加网关**zuul**
11.  将配置中心化，使用**springcloudConfig**，将git上的配置信息和本项目关联，对于其他服务而已，可以修改其yml读取git上配置信息。  

##   Eureka使用
###  服务注册中心
pom文件
``` xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka-server</artifactId>
</dependency>
```
application.yml
```yml
server:
  port: 7001 #这里，7001是服务中心管理页面的端口
eureka:
  instance:
    hostname: leeNode1 #服务中心的ip，其实仍然是本机127.0.0.1，在mac上做了映射，好区分
  client:
    register-with-eureka: false #false表示不向注册中心注册自己。
    fetch-registry: false #false表示自己端就是注册中心，我的职责就是维护服务实例，并不需要去检索服务
    service-url:
      defaultZone: http://leeNode2:7002/eureka/,http://leeNode3:7003/eureka/
#        defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/ 使用类似于这种格式组成的地址。对于集群化的注册中心，其指向其他服务中心。
```
启动类中：
```java
@SpringBootApplication //boot应用
@EnableEurekaServer  // server 表示这个是注册中心
public class EurekaServer7001_App {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServer7001_App.class, args);
    }
}
```
启动服务，访问127.0.0.1:7001 进入eureka管理页面

### 注册服务
pom文件,没有server，代表这是客户端的包，
```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka</artifactId>
        </dependency>
```
yml
```yml
server:
  port: 8002 #该服务对外暴露端口，
mybatis:
  config-location: classpath:mybatis/mybatis.cfg.xml        # mybatis配置文件所在路径
  type-aliases-package: atguigu.springcloud.entities    # 所有Entity别名类所在包
  mapper-locations:
    - classpath:mybatis/mapper/**/*.xml    # mapper映射文件，可以看出我们可以把classpath理解为类似ip的东西
spring:
  application:
    name: microservicecloud-dept  # 很关键，这是在服务中心的注册名，相同功能的服务可以使用同样的名字，这样，对于调用方而言可以当作是一个服务，
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource            # 当前数据源操作类型
    driver-class-name: org.gjt.mm.mysql.Driver              # mysql驱动包
    url: jdbc:mysql://localhost:3307/cloudDB02             # 数据库名称
    username: root
    dbcp2:
      min-idle: 5                                           # 数据库连接池的最小维持连接数
      initial-size: 5                                       # 初始化连接数
      max-total: 5                                          # 最大连接数
      max-wait-millis: 200                                  # 等待连接获取的最大超时时间
eureka:
  client: #客户端注册进eureka服务列表内
    service-url:
      defaultZone: http://leeNode1:7001/eureka,http://leeNode2:7002/eureka,http://leeNode3:7003/eureka
  instance:
    instance-id: microservicecloud-dept8002  # 在注册中心的唯一id
    prefer-ip-address: true
info: #这里就很妙了，我们在父工程的pom中添加maven的插件，可以拿到子模块的av，其实没啥卵用在这里。
  app.name: atguigu-microservicecloud
  company.name: www.atguigu.com
  build.artifactId: $project.artifactId$
  build.version: $project.version$
```
然后就是代码上的处理了，springMVC的一套东西，service+dao+controller，在这里dao实现层使用的mybatis写了一个mapper。
主启动类
``` java
@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient 	//其实和EnableEurekaClient类似，他可以被其他中心发现
public class DeptProvider8002_App {
    public static void main(String[] args) {
        SpringApplication.run(DeptProvider8002_App.class, args);
    }
}
```
那么如何集群化服务呢，完全复制之前的，在yml中修改端口以及服务中心的instance-id，如果心情好，还可以改改数据库连接啊，可以连接其他数据库，在这里能操作的很多了就。

### 注册消费者，使用服务
pom
```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka</artifactId>
        </dependency>
        <dependency><!--这里可以使用ribbon做LB-->
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-ribbon</artifactId>
        </dependency>
```
application.yml
```yml
server:
  port: 80 #暴露端口
eureka:
  client:
    register-with-eureka: false #不把自己注册，
    service-url:
      defaultZone: http://localhost:7001/eureka,http://localhost:7002/eureka,http://localhost:7003/eureka
```
主启动类
```java
@SpringBootApplication
@EnableEurekaClient
public class DeptConsumer80_App {
    public static void main(String[] args) {
        SpringApplication.run(DeptConsumer80_App.class, args);
    }
}
```
这些和注册服务其实都很类似，就是不把自己注册，剩下的都一样。  
这样，我们在使用的时候，可以直接通过restTemplate来访问服务了，类似于`restTemplate.postForObject("http://MICROSERVICECLOUD-DEPT/dept/add",dept,boolean.class)`也就是xxxForObject方法，可以是post，get，put等，然后后面参数：1. url是服务名+uri 2.有可能有传入参数 3. 返回值的类型。这里，具体写啥肯定要根据服务提供者来喽。这种方式马上会进化成另一种。
这里调用，其实是本模块controller调服务提供者的controller（这时候，不就变成service了，哦吼）

## ribbon负载均衡
pom
```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-ribbon</artifactId>
        </dependency>
```
ribbon 是个客户端的负载均衡，所以在做负载均衡的时候，服务提供者不需要做任何改动，但是需要重新配置一下restTemplate，可以新建一个configBean类
```java
@Configuration
public class ConfigBean {
    @Bean
    @LoadBalanced //ribbon 实现客户端的负载均衡,也就是把LB配置给了restTemplate
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

这个可以配置负载均衡的策略，好多种系统提供的，自己摸索吧，后面创建自定义的策略才是精髓啊
//    @Bean
//    public IRule myRule() {
//        return new RetryRule();
//    }
}
```
阶段二 自定义策略
大坑！！！！！ 自定义策略的bean这个配置类不能和主启动类同路径。所以我们把他往上提了一下，
```java
package atguigu.myrule; //看看，是不是往上提了

import com.netflix.loadbalancer.IRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MySelfRule {
    @Bean
    public IRule myRule() {
        return new RandomRule_LZ();  //这里就是我们自定义的啦，看看具体实现
    }
}
```
具体实现，省略大部分
```java
public class RandomRule_LZ extends AbstractLoadBalancerRule //这里就配置无关了，主要看代码咋写，把官方随机访问的代码复制修改了下，不过可能会发现，为啥官方没override initWithNiwsConfig这个方法，这方法干啥的我也不知道，我就自动生成了下。反正也能跑，先不管啦嘻嘻
```
最后最后，主启动类修改下
```java
@RibbonClient(name="MICROSERVICECLOUD-DEPT",configuration = MySelfRule.class) // 那个服务用了负载均衡，这个服务用的策略是啥，是不是就可以指定多个不同类型服务使用不同的LB策略了，具体咋用以后在扩展吧
```
## hystrix 熔断
这个是服务提供端的东西！！！  
pom
```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-hystrix</artifactId>
        </dependency>
```
在controller内（也能理解，服务真正被调用的时候）
```java
@RestController
@RequestMapping("/dept")
public class DeptController {
    @Autowired
    private DeptService service;
    @Autowired
    private DiscoveryClient discoveryClient;


    @PostMapping("/add")
    public boolean add(@RequestBody Dept dept) {
        return service.add(dept);
    }

    @GetMapping("/get/{id}")  //一下是重点，其他的都和以前一样
    @HystrixCommand(fallbackMethod = "processHystrix_Get") //这个注解表示，这个接口我们给他上熔断了，啥时候融呢，抛异常的时候，（这个熔断策略也太简单了吧，是不是以后会有复杂，甚至自定义的方式，咋整呢），融完了干啥，进入这个写在参数里的方法
    public Dept get(@PathVariable Long id) {

        Dept dept = service.get(id);
        if (null == dept) {
            throw new RuntimeException("不存在信息" + id);
        }
        return dept;
    }
	//熔断后就在我这里处理吧，这是不是像是处理异常 🤪
    public Dept processHystrix_Get(@PathVariable Long id) {
        return service.get(1L);
    }
```
接下来我们进阶一下：你想想，这也太麻烦了吧，一个接口写一个，而且不同服务实例还要重复写，我寻思这得写多少个啊。来，上批量处理。  
既然是大家的，那我们写到api里，这时候，哦吼，feign出来了
## feign负载均衡和熔断的整合
### api中
pom
```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-feign</artifactId>
        </dependency>
        <dependency> <!--我也不太确定用不用这个啊-->
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-hystrix</artifactId>
        </dependency>
    </dependencies>
```
这是一种高深思想，注解+接口。 接口上打注解，我感觉，是不是Spring帮我通过这俩搞了个bean出来。  
接口
```java
@FeignClient(value = "MICROSERVICECLOUD-DEPT", fallbackFactory = DeptClientFallbackFactory.class) //啥服务，熔断了谁接，更牛逼的是，这里还是服务和消费的中间传话人，你别看这里面只有方法，这tm把事情直接传给了服务提供者。其实把我绕的有点乱，但是好像比较利于消费者调用服务了，
@RequestMapping("/dept")
public interface DeptClientService {
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    List<Dept> list();

    @GetMapping("/get/{id}")
    Dept get(@PathVariable("id") Long id);

}
```
替身
```java
@Component //你虽然替身，但是 你还是演员
public class DeptClientFallbackFactory implements FallbackFactory<DeptClientService> { //你替身的剧本模版是这个
    @Override
    public DeptClientService create(Throwable throwable) {
        return new DeptClientService() {
            @Override
            public List<Dept> list() {
                return null;
            }

            @Override //看看，替身，要假装的一样，但是演的不一样
            public Dept get(Long id) {
                Dept dept = new Dept();
                dept.setDname("没找到不合法");
                dept.setDeptno(id);
                dept.setDb_source("not found");
                return dept;
            }
        };
    }
}
```
### consumer中
pom，好像一样啊
```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-feign</artifactId>
        </dependency>
```
application.yml
```yml
server:
  port: 80
eureka:
  client:
    register-with-eureka: false
    service-url:
      defaultZone: http://leeNode1:7001/eureka/,http://leeNode2:7002/eureka/,http://leeNode3:7003/eureka/
feign: #融断！ 开
  hystrix:
    enabled: true
```
controller
```java
@RestController
public class DeptController_Feign {
    @Autowired
    private DeptClientService deptClientService ;

//
    @GetMapping("/consumer/dept/list")
    public List<Dept> list() {
        return deptClientService.list();
    }

    @GetMapping("/consumer/get/{id}")
    public Dept get(@PathVariable("id") Long id) {
        return deptClientService.get(id);
    }
    @GetMapping("/test")
    public int test() {
        return 1;
    }
}
```
**关键啊啊啊啊啊，这个main函数头上的注解*
```java
@EnableEurekaClient
@EnableFeignClients(basePackages ={"atguigu.springcloud"})//feign的包
@ComponentScan(basePackages = {"atguigu.springcloud.service","atguigu.springcloud"})//this is super very super important，我寻思这个可能和我们每个模块的包名一致有点关系，缺一不可，缺前面不满足service这个bean的依赖，缺后面，整个页面都404，
@SpringBootApplication
public class DeptConsumer80Feign_App {
    public static void main(String[] args) {
        SpringApplication.run(DeptConsumer80Feign_App.class, args);
    }
}
```
讲道理，这个controller简单了一点点，老子不用管砸调服务了，直接调方法，以前还要写服务对外的url啊，好木乱。
### hystrixDashboard
pom
```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-hystrix-dashboard</artifactId>
        </dependency>

```
```java
@EnableHystrixDashboard
```
之后访问localhost：端口（自定义）/hystrix，按照网站指示输入东东，就能看服务了

## zuul

作为网关，也需要在服务注册中心注册。

``` xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-zuul</artifactId>
        </dependency>
```

```yml
zuul: #这是zuul配置的关键，
  routes:
    mydept.serviceId: microservicecloud-dept ## eureka中服务的名字
    mydept.path: /mydept/** ## 映射到了什么路径，可以根据这个路径访问服务了，
  ignored-services: "*" ## 两种屏蔽路径的方式
#  ignored-services: microservicecloud-dept 禁用单个域名
  prefix: /atguigu #还可以添加前缀
```
主启动类
```java
@EnableZuulProxy也就这一个关键注解
```
经过gateway后，访问的路径变为：ip：端口/（prefix）/path/
## springcloudConfig
### 另一个项目
这个东西的思想就是，新建另外的一个项目，这个项目专门用来放yml配置文件，这个配置文件的结构是啥呢：
```yml
spring:   #高级了，总分结构。总指定默认哪个环境，
  profile:
    active:
      - dev
--- #分隔符，一个环境一个块，这也是分，具体当前什么profile，

server:
  port: 7001
spring:
  profiles: dev
  application:
    name: microservicecloud-config-eureka-client
eureka:
  instance:
    hostname: leeNode1
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defalutZone: http://leeNode1:7001/eureka/
---

server:
  port: 7001
spring:
  profiles: test
  application:
    name: microservicecloud-config-eureka-client
eureka:
  instance:
    hostname: leeNode1
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defalutZone: http://leeNode1:7001/eureka/
```
也就是说，我们可以给各类服务（包括注册中心）统一配置其ym，然后在自己的配置里面个性化配置。上面这个例子就是给eureka配置的。
### 主项目
#### 需要一个关于配置的服务
```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-server</artifactId>
        </dependency>
```
```yml
server:
  port: 3344 # 其他模块从这个端口内拿配置，

spring:
  application:
    name: microservicecloud-config
  cloud:
    config:
      server:
        git:
#          此处由ssh换成http
          uri: https://github.com/CocaineLee/mircroservicecloud-config.git
```
```java
@EnableConfigServer //主启动类通过这个注解开启服务
```
#### 子模块使用
```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
```
bootstrap.yml
```yml
spring:
  cloud:
    config:
      name: microservicecloud-config-eureka-client # 其实是配置服务里，你需要的yml文件名
      profile: test # yml中对应的块
      label: master # 该仓库的分支名
      uri: http://config-3344:3344 #和配置服务连接，获取
```
application.yml
```yml
spring:
  application:
    name: microservicecloud-config-eureka-client # 简单了，就只需要做下个性化设置
```
其他就不需要改变了，所以这个是和代码无关，和配置相关，
