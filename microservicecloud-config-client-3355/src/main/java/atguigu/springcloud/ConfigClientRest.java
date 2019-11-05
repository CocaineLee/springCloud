package atguigu.springcloud;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigClientRest {
    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${eureka.client.service-url.defaultZone}")
    private String eurekaServices;

    @Value("${server.port}")
    private String port;

    @GetMapping("/config")
    public String getConfig() {
        String str = "applicationName::::" + applicationName + "eurekaService:::" + eurekaServices + "port:::" + port;
        return str;
    }
}
