package hgc.flowsyncapi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("hgc.flowsyncapi.mapper")
public class FlowsyncApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowsyncApiApplication.class, args);
    }
}
