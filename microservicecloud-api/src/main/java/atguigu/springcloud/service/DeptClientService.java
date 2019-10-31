package atguigu.springcloud.service;

import atguigu.springcloud.entities.Dept;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value="MICROSERVICECLOUD-DEPT")
//@RequestMapping("/dept")
public interface DeptClientService {
    @GetMapping("/dept/get/{id}")
    Dept get(@PathVariable long id);

    @RequestMapping(value = "/dept/list",method = RequestMethod.GET)
    List<Dept> list();

    @PostMapping("/dept/add")
    boolean add(Dept dept);
}
