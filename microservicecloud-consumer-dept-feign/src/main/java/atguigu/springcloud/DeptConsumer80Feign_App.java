package atguigu.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableEurekaClient
@EnableFeignClients(basePackages ={"atguigu.springcloud"})
//@ComponentScan(basePackageClasses = atguigu.springcloud.service.DeptClientService.class)
@ComponentScan(basePackages = {"atguigu.springcloud.service","atguigu.springcloud"})//this is super very super important
@SpringBootApplication
public class DeptConsumer80Feign_App {
    public static void main(String[] args) {
        SpringApplication.run(DeptConsumer80Feign_App.class, args);
    }
}
