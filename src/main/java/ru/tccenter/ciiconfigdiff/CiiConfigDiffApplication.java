package ru.tccenter.ciiconfigdiff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CiiConfigDiffApplication {

    /**
     * Запускает Spring Boot приложение сравнения конфигураций CII.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(CiiConfigDiffApplication.class, args);
    }
}
