package cn.edu.bjfu.nekocafe;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 扫描Mapper，正常启动
@SpringBootApplication
@MapperScan("cn.edu.bjfu.nekocafe.mapper")
public class NekocafeApplication {
    public static void main(String[] args) {
        SpringApplication.run(NekocafeApplication.class, args);
    }
}