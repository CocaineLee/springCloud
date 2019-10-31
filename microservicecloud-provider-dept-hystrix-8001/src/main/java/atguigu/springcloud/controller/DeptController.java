package atguigu.springcloud.controller;

import atguigu.springcloud.entities.Dept;
import atguigu.springcloud.service.DeptService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/get/{id}")
    @HystrixCommand(fallbackMethod = "processHystrix_Get")
    public Dept get(@PathVariable Long id) {

        Dept dept = service.get(id);
        if (null == dept) {
            throw new RuntimeException("不存在信息" + id);
        }
        return dept;
    }

    public Dept processHystrix_Get(@PathVariable Long id) {
        return service.get(1L);
    }

    @GetMapping("/list")
    public List<Dept> list() {
        return service.list();
    }


    @GetMapping("/discovery")
    public Object discovery() {
        List<String> list = discoveryClient.getServices();
        System.out.println("************" + list);

        List<ServiceInstance> srvList = discoveryClient.getInstances("MICROSERVICECLOUD-DEPT");
        for (ServiceInstance serviceInstance : srvList) {
            System.out.println(serviceInstance.getServiceId() + "\t" + serviceInstance.getHost() + "\t" + serviceInstance.getPort() + "\t"
                    + serviceInstance.getUri());
        }
        return this.discoveryClient;
    }
}
