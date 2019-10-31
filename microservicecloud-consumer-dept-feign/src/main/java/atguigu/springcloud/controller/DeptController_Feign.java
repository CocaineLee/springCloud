//package atguigu.springcloud.controller;

//import atguigu.springcloud.service.DeptClientService;
//import atguigu.springcloud.entities.Dept;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
////@RequestMapping("/consumer")
//public class DeptController_Feign {
//
//    @Autowired
//    private DeptClientService deptClientService;
//
//    @RequestMapping("/dept/add")
//    public boolean add(Dept dept) {
//        return deptClientService.add(dept);
//    }
//
//    @RequestMapping("/dept/get/{id}")
//    public Dept get(@PathVariable("id") Long id) {
//        return deptClientService.get(id);
//    }
//
//    @RequestMapping("/consumer/dept/list")
//    public List<Dept> list() {
//        return deptClientService.list();
//    }
//
//}
package atguigu.springcloud.controller;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import atguigu.springcloud.entities.Dept;
import atguigu.springcloud.service.DeptClientService;

@RestController
public class DeptController_Feign {
    @Autowired
    private DeptClientService service;

    @RequestMapping(value = "/consumer/dept/get/{id}")
    public Dept get(@PathVariable("id") Long id) {
        return this.service.get(id);
    }

    @RequestMapping(value = "/consumer/dept/list")
    public List<Dept> list() {
        return this.service.list();
    }

    @RequestMapping(value = "/consumer/dept/add")
    public Object add(Dept dept) {
        return this.service.add(dept);
    }
}