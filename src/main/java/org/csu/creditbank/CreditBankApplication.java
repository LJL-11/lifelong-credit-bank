package org.csu.creditbank;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.csu.creditbank.mapper")
public class CreditBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditBankApplication.class, args);
    }
}
