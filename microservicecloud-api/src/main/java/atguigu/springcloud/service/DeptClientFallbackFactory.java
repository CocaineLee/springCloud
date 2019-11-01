package atguigu.springcloud.service;

import atguigu.springcloud.entities.Dept;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeptClientFallbackFactory implements FallbackFactory<DeptClientService> {
    @Override
    public DeptClientService create(Throwable throwable) {
        return new DeptClientService() {
            @Override
            public List<Dept> list() {
                return null;
            }

            @Override
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
