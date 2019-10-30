package atguigu.springcloud.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@SuppressWarnings("serial")
@Data
@NoArgsConstructor
@Accessors
public class Dept implements Serializable {

    private Long deptno;

    private String dname;
    private String db_source;

    public Dept(String dname) {
        super();
        this.dname = dname;
    }

}
