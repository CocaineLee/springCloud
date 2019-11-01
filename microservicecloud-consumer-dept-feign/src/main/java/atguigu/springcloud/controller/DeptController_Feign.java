package atguigu.springcloud.controller;

import atguigu.springcloud.entities.Dept;
import atguigu.springcloud.service.DeptClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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