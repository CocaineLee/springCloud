package atguigu.springcloud.service;

import atguigu.springcloud.entities.Dept;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(value = "MICROSERVICECLOUD-DEPT", fallbackFactory = DeptClientFallbackFactory.class)
@RequestMapping("/dept")
public interface DeptClientService {

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    List<Dept> list();

    @GetMapping("/get/{id}")
    Dept get(@PathVariable("id") Long id);

}
